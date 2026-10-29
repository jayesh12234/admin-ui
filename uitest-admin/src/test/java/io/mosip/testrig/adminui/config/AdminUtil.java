package io.mosip.testrig.adminui.config;

import io.mosip.testrig.apirig.utils.AdminTestUtil;
import io.mosip.testrig.apirig.utils.KernelAuthentication;
import io.mosip.testrig.uitestrig.core.MosipTestDataManager;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Masterdata API calls the suite needs outside the browser: mapping the run's admin user to a
 * zone (the portal shows nothing a user without a zone may manage) and reading the location
 * hierarchy depth that drives the Create Center form.
 */
public final class AdminUtil {

    private static final Logger logger = LoggerFactory.getLogger(AdminUtil.class);
    private static final String COOKIE = "Authorization";
    private static final String ROLE_ADMIN = "admin";
    private static final String ROLE_GLOBAL_ADMIN = "globaladmin";

    private AdminUtil() {}

    /** Maps the run's admin user to the environment's last leaf zone and activates the mapping. */
    public static void mapAdminUserToZone() {
        KernelAuthentication auth = MosipTestDataManager.newKernelAuth();
        String token = auth.getTokenByRole(ROLE_GLOBAL_ADMIN);
        String userId = AdminConfigManager.getAdminUserId();
        String zoneCode = fetchLeafZoneCode(token);
        if (zoneCode.isEmpty()) {
            throw new IllegalStateException("No leaf zone found — cannot map " + userId + " to a zone");
        }

        JSONObject request = new JSONObject()
                .put("zoneCode", zoneCode)
                .put("userId", userId)
                .put("langCode", AdminConfigManager.getApiLangCode())
                .put("isActive", true);
        JSONObject body = new JSONObject()
                .put("id", "string")
                .put("version", "string")
                .put("metadata", new JSONObject())
                .put("requesttime", AdminTestUtil.generateCurrentUTCTimeStamp())
                .put("request", request);

        Response mapResponse = RestAssured.given().relaxedHTTPSValidation()
                .cookie(COOKIE, token).contentType(ContentType.JSON).body(body.toString())
                .post(masterDataUrl(AdminConfigManager.getproperty("zoneMappingUrl")));
        logger.info("Zone mapping {} → {}: HTTP {} {}", userId, zoneCode, mapResponse.statusCode(), mapResponse.asString());

        Response activateResponse = RestAssured.given().relaxedHTTPSValidation()
                .cookie(COOKIE, token).contentType(ContentType.JSON)
                .queryParam("isActive", "true").queryParam("userId", userId)
                .patch(masterDataUrl(AdminConfigManager.getproperty("zoneMappingActivateUrl")));
        logger.info("Zone mapping activation for {}: HTTP {} {}", userId, activateResponse.statusCode(), activateResponse.asString());
    }

    private static String fetchLeafZoneCode(String token) {
        Response response = RestAssured.given().relaxedHTTPSValidation()
                .cookie(COOKIE, token).accept(ContentType.JSON)
                .get(masterDataUrl(AdminConfigManager.getproperty("leafzonesURL") + AdminConfigManager.getApiLangCode()));
        JSONArray zones = new JSONObject(response.asString()).optJSONArray("response");
        if (zones == null || zones.isEmpty()) {
            logger.error("Leaf zone lookup failed: HTTP {} {}", response.statusCode(), response.asString());
            return "";
        }
        return zones.getJSONObject(zones.length() - 1).getString("code");
    }

    /** Highest hierarchyLevel in the location hierarchy — the Create Center form shows one
     *  location dropdown per level. */
    public static int getLocationHierarchyDepth() {
        String token = MosipTestDataManager.newKernelAuth().getTokenByRole(ROLE_ADMIN);
        Response response = RestAssured.given().relaxedHTTPSValidation()
                .cookie(COOKIE, token).accept(ContentType.JSON)
                .get(masterDataUrl(AdminConfigManager.getproperty("locationHierarchyLevels")
                        + AdminConfigManager.getApiLangCode()));
        JSONObject data = new JSONObject(response.asString()).optJSONObject("response");
        JSONArray levels = data == null ? null : data.optJSONArray("locationHierarchyLevels");
        if (levels == null || levels.isEmpty()) {
            throw new IllegalStateException("Location hierarchy lookup failed: HTTP "
                    + response.statusCode() + " " + response.asString());
        }
        int max = 0;
        for (int i = 0; i < levels.length(); i++) {
            max = Math.max(max, levels.getJSONObject(i).optInt("hierarchyLevel"));
        }
        return max;
    }

    public static String masterDataUrl(String path) {
        return AdminConfigManager.getMasterDataBaseUrl() + (path.startsWith("/") ? path : "/" + path);
    }
}

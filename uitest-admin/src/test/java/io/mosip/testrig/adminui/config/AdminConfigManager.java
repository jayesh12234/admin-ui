package io.mosip.testrig.adminui.config;

import io.mosip.testrig.adminui.report.ScenarioRecorder;
import io.mosip.testrig.apirig.testrunner.BaseTestCase;
import io.mosip.testrig.apirig.utils.ConfigManager;
import io.mosip.testrig.uitestrig.core.UiConfigManager;
import io.mosip.testrig.uitestrig.listeners.CucumberHooks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Admin Portal config — four-layer loading (lowest → highest priority):
 *   1. Kernel.properties   (apitest-commons JAR — common MOSIP defaults)
 *   2. uitest.properties   (uitest-commons JAR  — UI/browser defaults)
 *   3. adminui.properties  (this module         — env-specific overrides)
 *   4. Environment variables / Rancher overrides
 *
 * Call AdminConfigManager.init() once at suite setup before anything reads config.
 */
public class AdminConfigManager extends UiConfigManager {

    private static final Logger logger = LoggerFactory.getLogger(AdminConfigManager.class);

    /** Pre-Playwright name of baseUrl, still set by existing Rancher deployments. */
    private static final String LEGACY_PORTAL_URL_KEY = "adminPortalPath";

    public static void init() {
        Map<String, Object> moduleProps = new HashMap<>();
        // Layer 2: uitest-commons UI defaults — loaded first so later layers override them
        moduleProps.putAll(loadProperties("config/uitest.properties"));
        // Layer 3: module-specific overrides
        moduleProps.putAll(loadProperties("config/adminui.properties"));

        bridgeLegacyPortalUrl(moduleProps);
        bridgeEnvEndpoint(moduleProps);

        // ConfigManager.init(): loads Kernel.properties first (layer 1), then putAll(moduleProps)
        // so layers 2-3 override Kernel; env vars (layer 4) override everything inside init().
        ConfigManager.init(moduleProps);

        bridgeScenarioToRun();
    }

    private static void bridgeLegacyPortalUrl(Map<String, Object> moduleProps) {
        if (System.getenv("baseUrl") != null || !isBlank((String) moduleProps.get("baseUrl"))) return;
        String legacy = System.getenv(LEGACY_PORTAL_URL_KEY);
        if (isBlank(legacy)) legacy = (String) moduleProps.get(LEGACY_PORTAL_URL_KEY);
        if (!isBlank(legacy)) {
            moduleProps.put("baseUrl", legacy.trim());
            logger.info("baseUrl taken from legacy {}: {}", LEGACY_PORTAL_URL_KEY, legacy.trim());
        }
    }

    /** ConfigManager.init() derives eSignetbaseurl/signupBaseUrl/injiCertifyBaseURL from the JVM
     *  system property "env.endpoint" whenever those keys are blank, and NPEs when it is unset.
     *  A -Denv.endpoint=... passed directly still wins over the derived value. */
    private static void bridgeEnvEndpoint(Map<String, Object> moduleProps) {
        if (System.getProperty("env.endpoint") != null) return;
        String apiInternalEndPoint = System.getenv("apiInternalEndPoint");
        if (isBlank(apiInternalEndPoint)) apiInternalEndPoint = (String) moduleProps.get("apiInternalEndPoint");
        if (!isBlank(apiInternalEndPoint)) {
            System.setProperty("env.endpoint", apiInternalEndPoint.trim());
        }
    }

    /** Bridges adminui.properties' scenarioToRun into the system property CucumberHooks reads.
     *  A -DscenarioToRun=... passed directly on the command line still wins over the file. */
    private static void bridgeScenarioToRun() {
        if (System.getProperty(CucumberHooks.SCENARIO_FILTER_PROPERTY) != null) return;
        String scenarioToRun = getproperty("scenarioToRun");
        if (!isBlank(scenarioToRun)) {
            System.setProperty(CucumberHooks.SCENARIO_FILTER_PROPERTY, scenarioToRun.trim());
            logger.info("Running only scenario(s) (scenarioToRun): {}", scenarioToRun.trim());
        }
    }

    private static Map<String, Object> loadProperties(String resourcePath) {
        Properties props = new Properties();
        try (InputStream in = AdminConfigManager.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (in != null) {
                props.load(in);
                logger.info("Loaded {} — {} properties", resourcePath, props.size());
            } else {
                logger.warn("{} not found on classpath", resourcePath);
            }
        } catch (IOException e) {
            logger.error("Failed to load {}: {}", resourcePath, e.getMessage());
        }
        Map<String, Object> map = new HashMap<>();
        for (String key : props.stringPropertyNames()) {
            map.put(key, props.getProperty(key));
        }
        return map;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    /** Keycloak user the portal logs in as — created per run by MosipTestDataManager. */
    public static String getAdminUserId() {
        return BaseTestCase.currentModule + "-" + getUserAdminName();
    }

    public static String getAdminPassword() {
        return getUserAdminPassword();
    }

    /** A portal on localhost cannot use the Keycloak login redirect (KER-ACP-009), so the suite
     *  injects an admin token cookie instead — see AdminLocalSession. */
    public static boolean isLocalPortal() {
        String url = getBaseUrl().toLowerCase();
        return url.contains("localhost") || url.contains("127.0.0.1");
    }

    /** Base URL for masterdata API calls — the local masterdata service when configured. */
    public static String getMasterDataBaseUrl() {
        String base = getproperty("masterdatabaseurl");
        if (isBlank(base)) base = getproperty("apiInternalEndPoint");
        base = base.trim();
        return base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
    }

    /** Login language as used for API language codes — "sin" runs the portal in English. */
    public static String getApiLangCode() {
        return getEffectiveLoginLang();
    }

    public static String getPreappend() {
        String val = getproperty("preappend");
        return val == null ? "" : val.trim();
    }

    public static boolean isCleanupMasterData() {
        return !"no".equalsIgnoreCase(getproperty("cleanupMasterData").trim());
    }

    public static ScenarioRecorder.VideoMode getVideoMode() {
        return ScenarioRecorder.VideoMode.parse(getproperty("recordVideo"));
    }

    public static boolean isActionScreenshots() {
        return !"no".equalsIgnoreCase(getproperty("screenshotEveryAction").trim());
    }

    public static boolean isZipReport() {
        return !"no".equalsIgnoreCase(getproperty("zipReport").trim());
    }

    public static boolean isTraceOnFailure() {
        return !"no".equalsIgnoreCase(getproperty("traceOnFailure").trim());
    }

    public static int getBulkUploadStatusWaitSeconds() {
        String val = getproperty("bulkUploadStatusWaitSeconds");
        return isBlank(val) ? 60 : Integer.parseInt(val.trim());
    }
}

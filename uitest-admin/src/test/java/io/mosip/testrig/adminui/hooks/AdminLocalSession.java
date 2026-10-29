package io.mosip.testrig.adminui.hooks;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Route;
import com.microsoft.playwright.options.Cookie;
import com.microsoft.playwright.options.SameSiteAttribute;
import io.mosip.testrig.adminui.config.AdminConfigManager;
import io.mosip.testrig.uitestrig.browser.PlaywrightFactory;
import io.mosip.testrig.uitestrig.core.MosipTestDataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Browser session for a portal served from localhost (ng serve) against a local masterdata
 * service. localhost is not in the auth manager's allowed redirect list, so the Keycloak login
 * page returns KER-ACP-009; instead the admin token is stored as a cookie on the masterdata host.
 * Registered with PlaywrightFactory as an external session, so CucumberHooks and teardown treat
 * it like any other page.
 */
final class AdminLocalSession {

    private static final Logger logger = LoggerFactory.getLogger(AdminLocalSession.class);
    private static final String AUDIT_LOG_ROUTE = "**/auditmanager/log**";
    private static final String AUDIT_LOG_RESPONSE = "{\"errors\":null,\"response\":{}}";

    private AdminLocalSession() {}

    static void open() {
        String token = MosipTestDataManager.newKernelAuth().getTokenByRole("admin");
        if (token == null || token.isEmpty()) {
            throw new IllegalStateException("Admin token was empty. Check the admin client id and secret.");
        }

        Playwright pw = Playwright.create();
        try {
            // The local masterdata service's auth adapter does not allow every header the UI
            // sends, so Chromium blocks the cross-origin call unless web security is off.
            Browser browser = pw.chromium().launch(new BrowserType.LaunchOptions()
                    .setHeadless(AdminConfigManager.isHeadless())
                    .setArgs(List.of("--disable-web-security", "--proxy-bypass-list=<-loopback>")));
            BrowserContext context = browser.newContext(new Browser.NewContextOptions()
                    .setViewportSize(1920, 1080)
                    .setIgnoreHTTPSErrors(AdminConfigManager.isIgnoreHttpsErrors()));
            context.addCookies(List.of(authCookie(token)));
            // The local admin service does not serve audit logs; a failed audit call makes the
            // UI open a Technical Error dialog.
            context.route(AUDIT_LOG_ROUTE, route -> route.fulfill(new Route.FulfillOptions()
                    .setStatus(200).setContentType("application/json").setBody(AUDIT_LOG_RESPONSE)));
            Page page = context.newPage();
            page.setDefaultTimeout(30_000);
            page.setDefaultNavigationTimeout(60_000);
            PlaywrightFactory.registerExternalSession(pw, browser, context, page);
            logger.info("Local portal session opened with admin Authorization cookie for {}", cookieUrl());
        } catch (RuntimeException e) {
            pw.close();
            throw e;
        }
    }

    private static Cookie authCookie(String token) {
        String url = cookieUrl();
        boolean https = url.startsWith("https://");
        return new Cookie("Authorization", token)
                .setUrl(url)
                .setSecure(https)
                .setSameSite(https ? SameSiteAttribute.NONE : SameSiteAttribute.LAX);
    }

    /** The UI calls masterdatabaseurl; a cookie stored on the API host is not sent with those requests. */
    private static String cookieUrl() {
        return AdminConfigManager.getMasterDataBaseUrl() + "/";
    }
}

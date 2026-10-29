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
import io.mosip.testrig.adminui.report.ScenarioRecorder;
import io.mosip.testrig.uitestrig.browser.PlaywrightFactory;
import io.mosip.testrig.uitestrig.core.MosipTestDataManager;
import io.mosip.testrig.uitestrig.reporting.UiExtentReportManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Opens each scenario's browser here rather than in PlaywrightFactory, because screen recording
 * has to be set when the context is created. Registered with PlaywrightFactory as an external
 * session, so CucumberHooks and teardown treat it like any other page.
 *
 * For a portal served from localhost (ng serve) against a local masterdata service, the
 * Keycloak login page returns KER-ACP-009 (localhost is not an allowed redirect), so the admin
 * token is stored as a cookie on the masterdata host instead.
 */
final class AdminBrowserSession {

    private static final Logger logger = LoggerFactory.getLogger(AdminBrowserSession.class);
    private static final String AUDIT_LOG_ROUTE = "**/auditmanager/log**";
    private static final String AUDIT_LOG_RESPONSE = "{\"errors\":null,\"response\":{}}";

    private AdminBrowserSession() {}

    static void open(ScenarioRecorder recorder) {
        boolean local = AdminConfigManager.isLocalPortal();
        String token = local ? adminToken() : null;

        Playwright pw = Playwright.create();
        try {
            String browserName = AdminConfigManager.getBrowserType();
            Browser browser = browserType(pw, browserName).launch(launchOptions(browserName, local));
            UiExtentReportManager.setBrowserVersion(browserName + " " + browser.version());

            Browser.NewContextOptions options = new Browser.NewContextOptions()
                    .setViewportSize(1920, 1080)
                    .setIgnoreHTTPSErrors(AdminConfigManager.isIgnoreHttpsErrors());
            Path videoDir = recorder.videoDir();
            if (videoDir != null) {
                options.setRecordVideoDir(videoDir).setRecordVideoSize(1280, 720);
            }
            BrowserContext context = browser.newContext(options);
            if (local) {
                context.addCookies(List.of(authCookie(token)));
                // The local admin service does not serve audit logs; a failed audit call makes
                // the UI open a Technical Error dialog.
                context.route(AUDIT_LOG_ROUTE, route -> route.fulfill(new Route.FulfillOptions()
                        .setStatus(200).setContentType("application/json").setBody(AUDIT_LOG_RESPONSE)));
            }
            Page page = context.newPage();
            page.setDefaultTimeout(30_000);
            page.setDefaultNavigationTimeout(60_000);
            recorder.watch(context, page);
            PlaywrightFactory.registerExternalSession(pw, browser, context, page);
            if (local) logger.info("Local portal session opened with admin Authorization cookie for {}", cookieUrl());
        } catch (RuntimeException e) {
            pw.close();
            throw e;
        }
    }

    private static BrowserType browserType(Playwright pw, String browser) {
        return switch (browser) {
            case "firefox" -> pw.firefox();
            case "webkit", "safari" -> pw.webkit();
            default -> pw.chromium();
        };
    }

    private static BrowserType.LaunchOptions launchOptions(String browser, boolean local) {
        BrowserType.LaunchOptions options = new BrowserType.LaunchOptions().setHeadless(AdminConfigManager.isHeadless());
        switch (browser) {
            case "chrome" -> options.setChannel("chrome");
            case "msedge", "edge" -> options.setChannel("msedge");
            default -> { }
        }
        List<String> args = new ArrayList<>();
        if (local) {
            // The local masterdata service's auth adapter does not allow every header the UI
            // sends, so Chromium blocks the cross-origin call unless web security is off.
            args.add("--disable-web-security");
            args.add("--proxy-bypass-list=<-loopback>");
        }
        return options.setArgs(args);
    }

    private static String adminToken() {
        String token = MosipTestDataManager.newKernelAuth().getTokenByRole("admin");
        if (token == null || token.isEmpty()) {
            throw new IllegalStateException("Admin token was empty. Check the admin client id and secret.");
        }
        return token;
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

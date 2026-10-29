package io.mosip.testrig.adminui.report;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Request;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.Tracing;
import com.microsoft.playwright.options.ScreenshotType;
import io.cucumber.java.Scenario;
import io.mosip.testrig.adminui.config.AdminConfigManager;
import io.mosip.testrig.uitestrig.reporting.UiExtentReportManager;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * Per-scenario report evidence beyond the step log: screen recording, a highlighted screenshot
 * before every element interaction, a Playwright trace for failed scenarios, and the browser
 * console / API errors seen while the scenario ran. Files go under
 * {@code <reportOutputDir>/artifacts/<run>/<scenario>/} and the report links to them relatively,
 * so keep the artifacts folder next to the HTML when sharing a report.
 */
public final class ScenarioRecorder {

    private static final Logger logger = LoggerFactory.getLogger(ScenarioRecorder.class);
    private static final String RUN_ID = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
    private static final int MAX_ISSUES = 50;
    private static final int MAX_TEXT = 300;
    private static final String HIGHLIGHT_ON =
            "el => { el.scrollIntoView({block: 'center', inline: 'nearest'});"
            + " el.setAttribute('data-qa-outline', el.style.outline || '');"
            + " el.style.outline = '3px solid #ff1744'; el.style.outlineOffset = '2px'; }";
    private static final String HIGHLIGHT_OFF =
            "el => { el.style.outline = el.getAttribute('data-qa-outline') || '';"
            + " el.style.outlineOffset = ''; el.removeAttribute('data-qa-outline'); }";

    private static final ThreadLocal<ScenarioRecorder> CURRENT = new ThreadLocal<>();
    private static final Map<String, ExtentTest> TESTS_BY_SCENARIO_ID = new ConcurrentHashMap<>();

    private final String scenarioId;
    private final Path dir;
    private final List<String> consoleErrors = Collections.synchronizedList(new ArrayList<>());
    private final List<String> apiErrors = Collections.synchronizedList(new ArrayList<>());
    private int shotCount = 0;
    private Path videoPath;
    private boolean tracing;
    private volatile boolean signedIn;

    private ScenarioRecorder(String scenarioId, Path dir) {
        this.scenarioId = scenarioId;
        this.dir = dir;
    }

    // ── Lifecycle (driven by AdminHooks) ───────────────────────────────────────

    public static ScenarioRecorder start(Scenario scenario) {
        String slug = scenario.getName().replaceAll("[^A-Za-z0-9]+", "-").replaceAll("(^-|-$)", "")
                + "-" + scenario.getLine();
        Path dir = reportDir().resolve("artifacts").resolve(RUN_ID).resolve(slug);
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot create report artifacts folder " + dir, e);
        }
        ScenarioRecorder recorder = new ScenarioRecorder(scenario.getId(), dir);
        CURRENT.set(recorder);
        return recorder;
    }

    public static ScenarioRecorder current() {
        return CURRENT.get();
    }

    /** Folder Playwright records this scenario's video into, or null when recording is off. */
    public Path videoDir() {
        return AdminConfigManager.getVideoMode() == VideoMode.OFF ? null : dir;
    }

    /** Starts tracing and listens for console/API errors — call on the new page before it navigates. */
    public void watch(BrowserContext context, Page page) {
        if (AdminConfigManager.isTraceOnFailure()) {
            context.tracing().start(new Tracing.StartOptions().setScreenshots(true).setSnapshots(true));
            tracing = true;
        }
        if (page.video() != null) videoPath = page.video().path();
        // The login redirect dance (401s before sign-in, aborted redirects, Keycloak page
        // warnings) is expected on every scenario, so only problems after it are listed.
        page.onConsoleMessage(message -> {
            if (!"error".equals(message.type()) || onKeycloak(page.url())) return;
            // Failed HTTP calls already appear under API errors.
            if (message.text().startsWith("Failed to load resource")) return;
            add(consoleErrors, message.text());
        });
        page.onPageError(error -> {
            if (!onKeycloak(page.url())) add(consoleErrors, "Uncaught: " + error);
        });
        page.onRequestFailed(request -> {
            String failure = String.valueOf(request.failure());
            if (failure.contains("ERR_ABORTED") || onKeycloak(request.url())) return;
            add(apiErrors, describe(request) + " → " + failure);
        });
        page.onResponse(this::checkResponse);
    }

    private static boolean onKeycloak(String url) {
        return startsWithBase(url, AdminConfigManager.getproperty("keycloak-external-url"));
    }

    private static boolean onApiHost(String url) {
        return startsWithBase(url, AdminConfigManager.getproperty("apiInternalEndPoint"))
                || startsWithBase(url, AdminConfigManager.getproperty("masterdatabaseurl"));
    }

    private static boolean startsWithBase(String url, String base) {
        return base != null && !base.isBlank() && url != null && url.startsWith(base.trim().replaceAll("/+$", ""));
    }

    /** Links the scenario to its Extent test so the step-text plugin can find it. */
    public void attachTest() {
        ExtentTest test = UiExtentReportManager.getTest();
        if (test != null) TESTS_BY_SCENARIO_ID.put(scenarioId, test);
    }

    static ExtentTest testFor(String scenarioId) {
        return TESTS_BY_SCENARIO_ID.get(scenarioId);
    }

    /** Before CucumberHooks.teardownScenario — the Extent test and browser context still exist. */
    public void beforeTeardown(Scenario scenario, BrowserContext context) {
        boolean failed = scenario.isFailed();
        if (tracing) {
            try {
                Path trace = failed ? dir.resolve("trace.zip") : null;
                context.tracing().stop(new Tracing.StopOptions().setPath(trace));
                if (trace != null) {
                    log("🧭 Playwright trace: <a href='" + rel(trace) + "'>trace.zip</a> — drag it into "
                            + "<a href='https://trace.playwright.dev' target='_blank'>trace.playwright.dev</a> "
                            + "(opens locally in your browser) to replay every action with DOM snapshots, "
                            + "network calls and console output.");
                }
            } catch (RuntimeException e) {
                logger.warn("Could not save the Playwright trace: {}", e.getMessage());
            }
        }
        logIssues("API errors", apiErrors, failed);
        logIssues("Browser console errors", consoleErrors, failed);
        if (videoPath != null && keepVideo(failed)) {
            String src = rel(videoPath);
            log("🎥 Screen recording<br><video src='" + src + "' controls preload='metadata' "
                    + "style='width:100%;max-width:960px;border:1px solid #444;'></video>"
                    + "<br><a href='" + src + "' download>Download video</a>");
        }
    }

    /** After CucumberHooks.teardownScenario — the context is closed, so the video file is final. */
    public void afterTeardown(Scenario scenario) {
        try {
            if (videoPath != null && !keepVideo(scenario.isFailed())) {
                Files.deleteIfExists(videoPath);
            }
            try (Stream<Path> files = Files.list(dir)) {
                if (files.findAny().isEmpty()) Files.delete(dir);
            }
        } catch (IOException e) {
            logger.debug("Artifact cleanup for {} failed: {}", dir, e.getMessage());
        } finally {
            TESTS_BY_SCENARIO_ID.remove(scenarioId);
            CURRENT.remove();
        }
    }

    // ── Element screenshots (from AdminBasePage) ───────────────────────────────

    /** Outlines the element about to be used and attaches a screenshot of it in context. Skipped
     *  without waiting when the element is not in the page yet (e.g. a dialog about to open) —
     *  the action itself still waits for it. */
    public static void captureAction(Page page, String selector, String description) {
        ScenarioRecorder recorder = CURRENT.get();
        if (recorder == null || !AdminConfigManager.isActionScreenshots()) return;
        Locator target = page.locator(selector).first();
        try {
            if (page.locator(selector).count() == 0) return;
            target.evaluate(HIGHLIGHT_ON, null, new Locator.EvaluateOptions().setTimeout(1_000));
        } catch (RuntimeException e) {
            return;
        }
        try {
            Path file = recorder.dir.resolve(String.format("action-%03d.jpg", ++recorder.shotCount));
            page.screenshot(new Page.ScreenshotOptions().setPath(file).setType(ScreenshotType.JPEG).setQuality(60));
            ExtentTest test = UiExtentReportManager.getTest();
            if (test != null) {
                synchronized (UiExtentReportManager.class) {
                    test.info(MediaEntityBuilder.createScreenCaptureFromPath(recorder.rel(file), description).build());
                }
            }
        } catch (RuntimeException e) {
            logger.debug("Action screenshot failed for {}: {}", selector, e.getMessage());
        } finally {
            try {
                target.evaluate(HIGHLIGHT_OFF, null, new Locator.EvaluateOptions().setTimeout(1_000));
            } catch (RuntimeException ignored) {
                // The element was replaced or removed meanwhile; nothing left to un-highlight.
            }
        }
    }

    // ── Internals ──────────────────────────────────────────────────────────────

    /** MOSIP APIs report business errors as HTTP 200 with a non-empty "errors" array. */
    private void checkResponse(Response response) {
        Request request = response.request();
        if (onKeycloak(response.url())) return;
        String type = request.resourceType();
        boolean apiCall = "xhr".equals(type) || "fetch".equals(type);
        // The portal also loads its own config/i18n JSON over XHR before sign-in.
        if (apiCall && response.status() < 300 && onApiHost(response.url())) signedIn = true;
        if (response.status() >= 400) {
            // Before sign-in the portal probes its session and gets 401s by design.
            if (response.status() == 401 && !signedIn) return;
            add(apiErrors, describe(request) + " → HTTP " + response.status());
            return;
        }
        if (!apiCall) return;
        String contentType = response.headers().getOrDefault("content-type", "");
        if (!contentType.contains("json")) return;
        try {
            JSONObject body = new JSONObject(response.text());
            JSONArray errors = body.optJSONArray("errors");
            if (errors == null || errors.isEmpty()) return;
            StringBuilder codes = new StringBuilder();
            for (int i = 0; i < errors.length(); i++) {
                JSONObject error = errors.optJSONObject(i);
                if (error == null) continue;
                if (codes.length() > 0) codes.append("; ");
                codes.append(error.optString("errorCode")).append(' ').append(error.optString("message"));
            }
            add(apiErrors, describe(request) + " → " + codes);
        } catch (RuntimeException e) {
            // Not a MOSIP response envelope (or body unavailable) — nothing to report.
        }
    }

    private static String describe(Request request) {
        return request.method() + " " + request.url();
    }

    private static void add(List<String> list, String text) {
        if (list.size() >= MAX_ISSUES) return;
        list.add(text.length() > MAX_TEXT ? text.substring(0, MAX_TEXT) + "…" : text);
    }

    private void logIssues(String title, List<String> issues, boolean expand) {
        if (issues.isEmpty()) return;
        StringBuilder html = new StringBuilder("<details").append(expand ? " open" : "").append(">")
                .append("<summary style='cursor:pointer;'>⚠️ ").append(title).append(" (")
                .append(issues.size()).append(")</summary><ol style='margin:4px 0 0 20px;'>");
        synchronized (issues) {
            for (String issue : issues) html.append("<li><code>").append(escape(issue)).append("</code></li>");
        }
        log(html.append("</ol></details>").toString());
    }

    private static boolean keepVideo(boolean failed) {
        VideoMode mode = AdminConfigManager.getVideoMode();
        return mode == VideoMode.ALWAYS || (mode == VideoMode.ON_FAILURE && failed);
    }

    private static void log(String html) {
        ExtentTest test = UiExtentReportManager.getTest();
        if (test == null) return;
        synchronized (UiExtentReportManager.class) {
            test.info(html);
        }
    }

    private String rel(Path file) {
        return reportDir().relativize(file.toAbsolutePath()).toString().replace('\\', '/');
    }

    private static Path reportDir() {
        String dir = AdminConfigManager.getproperty("reportOutputDir");
        return Paths.get(dir == null || dir.isBlank() ? "target/extent-reports/" : dir).toAbsolutePath().normalize();
    }

    private static String escape(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    public enum VideoMode {
        ALWAYS, ON_FAILURE, OFF;

        public static VideoMode parse(String value) {
            if (value == null || value.isBlank()) return ON_FAILURE;
            switch (value.trim().toLowerCase()) {
                case "always": case "yes": return ALWAYS;
                case "off": case "no": return OFF;
                default: return ON_FAILURE;
            }
        }
    }
}

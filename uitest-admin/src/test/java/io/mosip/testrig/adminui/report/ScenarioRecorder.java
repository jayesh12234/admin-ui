package io.mosip.testrig.adminui.report;

import com.aventstack.extentreports.ExtentTest;
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
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * Per-scenario report evidence beyond the step log: a highlighted screenshot before every
 * element interaction, a screen recording and Playwright trace for failed scenarios, and the
 * browser console / API errors seen while the scenario ran. Everything is embedded in the
 * report HTML itself, so the single file can be shared as is; recordings and traces only pass
 * through a temporary folder until they are embedded.
 */
public final class ScenarioRecorder {

    private static final Logger logger = LoggerFactory.getLogger(ScenarioRecorder.class);
    private static final int MAX_ISSUES = 50;
    private static final int MAX_TEXT = 300;
    private static final int SCREENSHOT_QUALITY = 50;
    private static final String HIGHLIGHT_ON =
            "el => { el.scrollIntoView({block: 'center', inline: 'nearest'});"
            + " el.setAttribute('data-qa-outline', el.style.outline || '');"
            + " el.style.outline = '3px solid #ff1744'; el.style.outlineOffset = '2px'; }";
    private static final String HIGHLIGHT_OFF =
            "el => { el.style.outline = el.getAttribute('data-qa-outline') || '';"
            + " el.style.outlineOffset = ''; el.removeAttribute('data-qa-outline'); }";
    // Click toggles between a thumbnail and full width.
    private static final String SCREENSHOT_STYLE =
            "width:480px;max-width:100%;border:1px solid #444;cursor:zoom-in;margin-top:4px;";
    private static final String SCREENSHOT_TOGGLE =
            "this.style.width=this.style.width==='100%'?'480px':'100%'";

    private static final ThreadLocal<ScenarioRecorder> CURRENT = new ThreadLocal<>();
    private static final Map<String, ExtentTest> TESTS_BY_SCENARIO_ID = new ConcurrentHashMap<>();

    private final String scenarioId;
    private final String slug;
    private final Path tempDir;
    private final List<String> consoleErrors = Collections.synchronizedList(new ArrayList<>());
    private final List<String> apiErrors = Collections.synchronizedList(new ArrayList<>());
    private ExtentTest test;
    private Path videoPath;
    private boolean tracing;
    private volatile boolean signedIn;

    private ScenarioRecorder(String scenarioId, String slug, Path tempDir) {
        this.scenarioId = scenarioId;
        this.slug = slug;
        this.tempDir = tempDir;
    }

    // ── Lifecycle (driven by AdminHooks) ───────────────────────────────────────

    public static ScenarioRecorder start(Scenario scenario) {
        String slug = scenario.getName().replaceAll("[^A-Za-z0-9]+", "-").replaceAll("(^-|-$)", "")
                + "-" + scenario.getLine();
        Path tempDir;
        try {
            tempDir = Files.createTempDirectory("admin-ui-evidence-");
        } catch (IOException e) {
            throw new IllegalStateException("Cannot create a temporary folder for report evidence", e);
        }
        ScenarioRecorder recorder = new ScenarioRecorder(scenario.getId(), slug, tempDir);
        CURRENT.set(recorder);
        return recorder;
    }

    public static ScenarioRecorder current() {
        return CURRENT.get();
    }

    /** Folder Playwright records this scenario's video into, or null when recording is off. */
    public Path videoDir() {
        return AdminConfigManager.getVideoMode() == VideoMode.OFF ? null : tempDir;
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

    /** Links the scenario to its Extent test — for the step-text plugin, and for the recording,
     *  which can only be embedded after CucumberHooks has detached the test from this thread. */
    public void attachTest() {
        test = UiExtentReportManager.getTest();
        if (test != null) TESTS_BY_SCENARIO_ID.put(scenarioId, test);
    }

    static ExtentTest testFor(String scenarioId) {
        return TESTS_BY_SCENARIO_ID.get(scenarioId);
    }

    /** Before CucumberHooks.teardownScenario — the browser context still exists. */
    public void beforeTeardown(Scenario scenario, BrowserContext context) {
        boolean failed = scenario.isFailed();
        if (tracing) {
            try {
                Path trace = failed ? tempDir.resolve("trace.zip") : null;
                context.tracing().stop(new Tracing.StopOptions().setPath(trace));
                if (trace != null) {
                    log("🧭 Playwright trace: <a download='trace-" + slug + ".zip' href='data:application/zip;base64,"
                            + base64(trace) + "'>download trace-" + slug + ".zip</a> — drop it onto "
                            + "<a href='https://trace.playwright.dev' target='_blank'>trace.playwright.dev</a> "
                            + "(opens locally in your browser) to replay every action with DOM snapshots, "
                            + "network calls and console output.");
                }
            } catch (RuntimeException | IOException e) {
                logger.warn("Could not save the Playwright trace: {}", e.getMessage());
            }
        }
        logIssues("API errors", apiErrors, failed);
        logIssues("Browser console errors", consoleErrors, failed);
    }

    /** After CucumberHooks.teardownScenario — the context is closed, so the video file is final. */
    public void afterTeardown(Scenario scenario) {
        try {
            if (videoPath != null && keepVideo(scenario.isFailed()) && Files.exists(videoPath)) {
                log("🎥 Screen recording<br><video controls preload='metadata' "
                        + "style='width:100%;max-width:960px;border:1px solid #444;' "
                        + "src='data:video/webm;base64," + base64(videoPath) + "'></video>");
            }
        } catch (IOException e) {
            logger.warn("Could not embed the screen recording: {}", e.getMessage());
        } finally {
            deleteTempDir();
            TESTS_BY_SCENARIO_ID.remove(scenarioId);
            CURRENT.remove();
        }
    }

    // ── Element screenshots (from AdminBasePage) ───────────────────────────────

    /** Outlines the element about to be used and embeds a screenshot of it in context. Skipped
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
            byte[] shot = page.screenshot(new Page.ScreenshotOptions()
                    .setType(ScreenshotType.JPEG).setQuality(SCREENSHOT_QUALITY));
            recorder.log("<img alt='" + escape(description) + "' title='Click to enlarge' style='" + SCREENSHOT_STYLE
                    + "' onclick=\"" + SCREENSHOT_TOGGLE + "\" src='data:image/jpeg;base64,"
                    + Base64.getEncoder().encodeToString(shot) + "'>");
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

    private void log(String html) {
        ExtentTest target = test != null ? test : UiExtentReportManager.getTest();
        if (target == null) return;
        synchronized (UiExtentReportManager.class) {
            target.info(html);
        }
    }

    private static String base64(Path file) throws IOException {
        return Base64.getEncoder().encodeToString(Files.readAllBytes(file));
    }

    private void deleteTempDir() {
        try (Stream<Path> files = Files.walk(tempDir)) {
            files.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException e) {
                    logger.debug("Could not delete {}: {}", path, e.getMessage());
                }
            });
        } catch (IOException e) {
            logger.debug("Could not clean up {}: {}", tempDir, e.getMessage());
        }
    }

    private static String escape(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("'", "&#39;");
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

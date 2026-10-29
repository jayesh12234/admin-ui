package io.mosip.testrig.adminui.hooks;

import io.cucumber.java.After;
import io.cucumber.java.AfterAll;
import io.cucumber.java.Before;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.BeforeStep;
import io.cucumber.java.Scenario;
import io.mosip.testrig.adminui.config.AdminConfigManager;
import io.mosip.testrig.adminui.report.ReportBundler;
import io.mosip.testrig.adminui.report.ScenarioRecorder;
import io.mosip.testrig.uitestrig.browser.PlaywrightFactory;
import io.mosip.testrig.uitestrig.core.MosipTestDataManager;
import io.mosip.testrig.uitestrig.listeners.CucumberHooks;
import io.mosip.testrig.uitestrig.reporting.KnownIssueManager;
import io.mosip.testrig.uitestrig.reporting.UiExtentReportManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdminHooks {

    private static final Logger logger = LoggerFactory.getLogger(AdminHooks.class);

    @BeforeAll(order = 5)
    public static void initSuite() {
        AdminConfigManager.init();
        MosipTestDataManager.initialize();
        UiExtentReportManager.initReport();
        logger.info("Admin suite ready — portal: {}, user: {}",
                AdminConfigManager.getBaseUrl(), AdminConfigManager.getAdminUserId());
    }

    @BeforeAll(order = 10)
    public static void initTestData() {
        AdminTestData.ensureReady();
    }

    @Before
    public void scenarioSetup(Scenario scenario) {
        // Must open before CucumberHooks.setupScenario so its initPage() picks this session up;
        // skipped for filtered-out/known-issue scenarios, which teardown never closes.
        ScenarioRecorder recorder = null;
        if (willRun(scenario)) {
            recorder = ScenarioRecorder.start(scenario);
            AdminBrowserSession.open(recorder);
        }
        CucumberHooks.setupScenario(scenario);
        if (recorder != null) recorder.attachTest();
    }

    @BeforeStep
    public void stepSetup() {
        CucumberHooks.beforeStep();
    }

    @After
    public void scenarioTeardown(Scenario scenario) {
        ScenarioRecorder recorder = ScenarioRecorder.current();
        if (recorder == null) {
            CucumberHooks.teardownScenario(scenario);
            return;
        }
        try {
            recorder.beforeTeardown(scenario, PlaywrightFactory.getPage().context());
        } catch (RuntimeException e) {
            logger.warn("Could not finish the scenario recording: {}", e.getMessage());
        }
        CucumberHooks.teardownScenario(scenario);
        recorder.afterTeardown(scenario);
    }

    @AfterAll
    public static void teardownSuite() {
        try {
            AdminDbCleanup.run();
        } finally {
            CucumberHooks.teardownSuite();
            ReportBundler.bundle();
        }
    }

    private static boolean willRun(Scenario scenario) {
        if (KnownIssueManager.isKnownIssue(scenario.getName())) return false;
        String only = System.getProperty(CucumberHooks.SCENARIO_FILTER_PROPERTY);
        if (only == null || only.isBlank()) return true;
        for (String name : only.split(",")) {
            if (scenario.getName().trim().equalsIgnoreCase(name.trim())) return true;
        }
        return false;
    }
}

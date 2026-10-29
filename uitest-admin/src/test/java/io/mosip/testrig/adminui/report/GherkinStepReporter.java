package io.mosip.testrig.adminui.report;

import com.aventstack.extentreports.ExtentTest;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.DataTableArgument;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.PickleStepTestStep;
import io.cucumber.plugin.event.Step;
import io.cucumber.plugin.event.TestStepStarted;
import io.mosip.testrig.uitestrig.reporting.UiExtentReportManager;

import java.util.List;

/**
 * Writes each Gherkin step's text (and data table) under the "STEP n:" divider that
 * CucumberHooks logs from @BeforeStep, which on its own does not know the step text.
 * A ConcurrentEventListener receives TestStepStarted on the scenario's thread as it happens.
 */
public class GherkinStepReporter implements ConcurrentEventListener {

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(TestStepStarted.class, this::onStepStarted);
    }

    private void onStepStarted(TestStepStarted event) {
        if (!(event.getTestStep() instanceof PickleStepTestStep pickleStep)) return;
        ExtentTest test = ScenarioRecorder.testFor(event.getTestCase().getId().toString());
        if (test == null) return;
        Step step = pickleStep.getStep();
        StringBuilder html = new StringBuilder("<b>").append(escape(step.getKeyword().trim()))
                .append(' ').append(escape(step.getText())).append("</b>");
        if (step.getArgument() instanceof DataTableArgument table) {
            html.append("<table style='margin:4px 0 0 20px;border-collapse:collapse;'>");
            for (List<String> row : table.cells()) {
                html.append("<tr>");
                for (String cell : row) {
                    html.append("<td style='border:1px solid #555;padding:1px 8px;'>").append(escape(cell)).append("</td>");
                }
                html.append("</tr>");
            }
            html.append("</table>");
        }
        synchronized (UiExtentReportManager.class) {
            test.info(html.toString());
        }
    }

    private static String escape(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}

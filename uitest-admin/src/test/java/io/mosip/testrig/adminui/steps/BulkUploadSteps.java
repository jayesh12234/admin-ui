package io.mosip.testrig.adminui.steps;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.mosip.testrig.adminui.config.AdminConfigManager;
import io.mosip.testrig.adminui.pages.BulkUploadPage;
import io.mosip.testrig.uitestrig.browser.PlaywrightFactory;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.time.Duration;

public class BulkUploadSteps {

    private static final String COMPLETED = "COMPLETED";

    // Cucumber creates a fresh instance per scenario, so this never crosses scenarios/threads.
    private String transactionId;

    private BulkUploadPage page() {
        return new BulkUploadPage(PlaywrightFactory.getPage());
    }

    @When("the admin opens the master data bulk upload page")
    public void openBulkUpload() {
        page().open();
    }

    @When("the admin uploads the {string} file with the {string} operation")
    public void upload(String table, String operation) {
        transactionId = page().upload(operation, table, csvFor(table));
    }

    @Then("the upload transaction should complete")
    public void assertCompleted() {
        BulkUploadPage page = page();
        String status = page.waitForStatus(transactionId, COMPLETED,
                Duration.ofSeconds(AdminConfigManager.getBulkUploadStatusWaitSeconds()));
        page.attachScreenshot("Bulk upload transaction " + transactionId);
        if (!COMPLETED.equalsIgnoreCase(status)) {
            throw new AssertionError("Bulk upload " + transactionId + " status is " + status + ", expected " + COMPLETED);
        }
    }

    /** CSVs live per login language under BulkUploadFiles/, matching the portal's language. */
    private static Path csvFor(String table) {
        String resource = "BulkUploadFiles/" + AdminConfigManager.getLoginLang() + "/" + table + ".csv";
        URL url = BulkUploadSteps.class.getClassLoader().getResource(resource);
        if (url == null) {
            throw new IllegalStateException("Bulk upload file not found on classpath: " + resource);
        }
        try {
            return Path.of(url.toURI());
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Bad bulk upload file URL: " + url, e);
        }
    }
}

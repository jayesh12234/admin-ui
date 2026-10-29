package io.mosip.testrig.adminui.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;
import io.mosip.testrig.uitestrig.reporting.UiExtentReportManager;

import java.nio.file.Path;
import java.time.Duration;

import static io.mosip.testrig.adminui.core.AdminGlobalConstants.*;

/** Bulk Upload → Master Data upload page. */
public class BulkUploadPage extends AdminBasePage {

    private static final String UPLOAD_DATA_ID = "Upload Data";
    private static final String OPERATION_ID = "operation";
    private static final String TABLE_NAME_ID = "tableName";
    private static final String FILE_INPUT_ID = "fileInput";
    private static final String TRANSACTION_STATUS = "xpath=//table[@class='mat-table']//tr[td[1][normalize-space()='%s']]/td[5]";
    private static final Duration STATUS_POLL_INTERVAL = Duration.ofSeconds(15);

    public BulkUploadPage(Page page) {
        super(page);
    }

    public void open() {
        clickFirst(byId(MENU_BULK_UPLOAD_ID), "Open the Bulk Upload menu");
        clickFirst(LINK_MASTER_DATA_UPLOAD, "Open the Master Data Upload page");
    }

    /** Uploads the CSV and returns the transaction id from the confirmation dialog. */
    public String upload(String operation, String table, Path csv) {
        clickFirst(byId(UPLOAD_DATA_ID), "Click Upload Data");
        selectOptionById(byId(OPERATION_ID), operation, "Select the operation");
        selectOptionById(byId(TABLE_NAME_ID), table, "Select the table");
        step("Attach " + csv.getFileName(), byId(FILE_INPUT_ID), () -> first(byId(FILE_INPUT_ID)).setInputFiles(csv));
        clickFirst(CREATE_BUTTON, "Click Upload");
        clickFirst(CONFIRM_POPUP, "Confirm the upload");

        Locator message = first(DIALOG_MESSAGE);
        message.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        String[] parts = message.innerText().split(":");
        if (parts.length < 2) {
            throw new AssertionError("Unexpected bulk upload message: " + message.innerText());
        }
        String transactionId = parts[1].trim();
        logger.info("Bulk upload {} {} → transaction {}", operation, table, transactionId);
        clickFirst(CONFIRM_MESSAGE_POPUP, "Close the upload message");
        return transactionId;
    }

    /** Refreshes the transaction list until the upload's row reaches {@code expected} or
     *  {@code maxWait} runs out; returns the last status seen. The row is found by transaction
     *  id, not position — parallel scenarios and other users upload to the same list. */
    public String waitForStatus(String transactionId, String expected, Duration maxWait) {
        String statusCell = String.format(TRANSACTION_STATUS, transactionId.replace("'", ""));
        long deadline = System.currentTimeMillis() + maxWait.toMillis();
        String status = cellText(statusCell);
        while (!expected.equalsIgnoreCase(status) && System.currentTimeMillis() < deadline) {
            logger.info("Bulk upload {} status is {} — refreshing in {} s", transactionId, status, STATUS_POLL_INTERVAL.toSeconds());
            page.waitForTimeout(STATUS_POLL_INTERVAL.toMillis());
            page.reload();
            status = cellText(statusCell);
        }
        return status;
    }

    private String cellText(String selector) {
        Locator cell = first(selector);
        cell.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        return cell.innerText().trim();
    }

    public void attachScreenshot(String title) {
        UiExtentReportManager.attachScreenshot(takeScreenshotBase64(), title);
    }
}

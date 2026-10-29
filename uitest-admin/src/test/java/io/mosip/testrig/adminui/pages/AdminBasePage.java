package io.mosip.testrig.adminui.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.WaitForSelectorState;
import io.mosip.testrig.adminui.report.ScenarioRecorder;
import io.mosip.testrig.uitestrig.browser.SmartWait;
import io.mosip.testrig.uitestrig.pages.BasePage;
import io.mosip.testrig.uitestrig.reporting.UiExtentReportManager;

import java.time.Duration;
import java.util.regex.Pattern;

import static io.mosip.testrig.adminui.core.AdminGlobalConstants.*;

/**
 * Admin Portal interactions shared by all page objects: Angular Material selects, the list
 * filter dialog, row action menus, the date picker and the confirm/message dialogs.
 *
 * Several portal ids repeat on a page (stacked dialogs, per-level location dropdowns), so every
 * helper acts on the first match the way the Selenium suite's findElement did, instead of
 * Playwright's strict single-match locators.
 */
public abstract class AdminBasePage extends BasePage {

    private static final double SHORT_TIMEOUT_MS = 10_000;
    private static final long SELECT_OPEN_TIMEOUT_MS = 30_000;
    private static final double FORM_SETTLE_MS = 1_500;

    protected AdminBasePage(Page page) {
        super(page);
    }

    /** Attribute selector for an element id — portal ids contain '/', ' ' and other characters
     *  that a '#id' CSS selector cannot hold. */
    public static String byId(String id) {
        return "[id='" + id.replace("'", "\\'") + "']";
    }

    protected Locator first(String selector) {
        return page.locator(selector).first();
    }

    // ── Reported actions ───────────────────────────────────────────────────────

    protected void step(String description, String selector, Runnable action) {
        UiExtentReportManager.logStepWithLocator(description, selector);
        ScenarioRecorder.captureAction(page, selector, description);
        try {
            action.run();
        } catch (RuntimeException | AssertionError e) {
            String screenshot = null;
            try {
                screenshot = takeScreenshotBase64();
            } catch (RuntimeException ignored) {
                logger.warn("Could not capture failure screenshot for: {}", description);
            }
            UiExtentReportManager.logFailureWithScreenshot(description, selector, e, screenshot);
            throw e;
        }
    }

    protected void clickFirst(String selector, String description) {
        step(description, selector, () -> first(selector).click());
    }

    /** Fills the field, filling it again if the value did not take on the first try. */
    protected void fillFirst(String selector, String value, String description) {
        step(description + " | Value → " + value, selector, () -> {
            Locator field = first(selector);
            field.fill(value);
            if (!value.equals(field.inputValue())) {
                field.fill(value);
            }
        });
    }

    /** Edit forms open empty and are filled from the API afterwards; typing before that lands
     *  gets overwritten, so wait for the saved value to appear first. */
    protected void waitForRecordToLoad(String fieldSelector) {
        step("Wait for the record to load into the form", fieldSelector, () -> {
            Locator field = first(fieldSelector);
            field.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
            SmartWait.untilTrue(() -> !field.inputValue().isEmpty(), Duration.ofSeconds(30));
        });
    }

    /** Fills the field, then re-checks after the form settles in case a late update reset it. */
    protected void fillAndHold(String selector, String value, String description) {
        fillFirst(selector, value, description);
        page.waitForTimeout(FORM_SETTLE_MS);
        if (!value.equals(first(selector).inputValue())) {
            logger.info("{} was reset after typing — entering it again", selector);
            fillFirst(selector, value, description + " (again)");
            page.waitForTimeout(FORM_SETTLE_MS);
            String actual = first(selector).inputValue();
            if (!value.equals(actual)) {
                throw new AssertionError(selector + " holds '" + actual + "' instead of '" + value + "'");
            }
        }
    }

    /** Waits up to {@code timeoutMs} for the element and clicks it; false if it never showed. */
    protected boolean clickIfVisible(String selector, String description, double timeoutMs) {
        Locator target = first(selector);
        try {
            target.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(timeoutMs));
        } catch (PlaywrightException e) {
            logger.debug("{} not shown within {} ms — skipping '{}'", selector, timeoutMs, description);
            return false;
        }
        clickFirst(selector, description);
        return true;
    }

    protected boolean isPresent(String selector, double timeoutMs) {
        try {
            first(selector).waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(timeoutMs));
            return true;
        } catch (PlaywrightException e) {
            return false;
        }
    }

    /** Escapes {@code text} for a Playwright regex. Pattern.quote() cannot be used: its \Q…\E
     *  is Java-only and Playwright evaluates the pattern as a JavaScript RegExp. */
    protected static String regexLiteral(String text) {
        return text.replaceAll("[\\\\^$.|?*+()\\[\\]{}/]", "\\\\$0");
    }

    /** Matches an element whose whole text is {@code text}, ignoring surrounding whitespace. */
    protected static Pattern exactText(String text) {
        return Pattern.compile("^\\s*" + regexLiteral(text) + "\\s*$");
    }

    // ── Navigation ─────────────────────────────────────────────────────────────

    public void openMasterDataCard(String cardId) {
        clickFirst(MENU_MASTER_DATA, "Open the Master Data menu");
        clickFirst(byId(cardId), "Open master data page " + cardId);
    }

    public void openResource(String linkSelector, String name) {
        clickFirst(byId(MENU_RESOURCES_ID), "Open the Resources menu");
        clickFirst(linkSelector, "Open the " + name + " page");
    }

    // ── Form fields ────────────────────────────────────────────────────────────

    public void fillField(String fieldId, String value) {
        fillFirst(byId(fieldId), value, "Enter " + fieldId);
    }

    /** Clicks a mat-select until its option panel shows. A mat-select ignores clicks while its
     *  options are still loading from the API, so a single early click opens nothing. */
    private Locator openSelect(String triggerSelector) {
        Locator trigger = first(triggerSelector);
        Locator options = page.locator(OVERLAY_OPTIONS);
        long deadline = System.currentTimeMillis() + SELECT_OPEN_TIMEOUT_MS;
        while (true) {
            // An open panel's backdrop covers the trigger, so never click it twice once open.
            if (options.first().isVisible()) return options;
            trigger.click();
            try {
                options.first().waitFor(new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.VISIBLE).setTimeout(3_000));
                return options;
            } catch (PlaywrightException e) {
                if (System.currentTimeMillis() > deadline) throw e;
                logger.info("{} has no options yet — clicking it again", triggerSelector);
            }
        }
    }

    /** Opens a mat-select and picks its first option. */
    public void selectFirstOption(String triggerSelector, String description) {
        step(description, triggerSelector, () -> {
            Locator trigger = first(triggerSelector);
            Locator options = openSelect(triggerSelector);
            String owns = trigger.getAttribute("aria-owns");
            Locator option = (owns == null || owns.isBlank())
                    ? options.first()
                    : page.locator(byId(owns.trim().split("\\s+")[0]));
            option.click();
            waitForPanelToClose(option);
        });
    }

    /** A single-select panel closes on its own; a multi-select one stays open until Escape. */
    private void waitForPanelToClose(Locator option) {
        try {
            option.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN).setTimeout(5_000));
        } catch (PlaywrightException e) {
            page.keyboard().press("Escape");
            option.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN));
        }
    }

    /** Opens a mat-select and picks the option whose text is {@code text} — as a whole word
     *  first, so "1:00 PM" does not pick "11:00 PM", else any option containing it. */
    public void selectOptionByText(String triggerSelector, String text, String description) {
        step(description + " | Option → " + text, triggerSelector, () -> {
            Locator options = openSelect(triggerSelector);
            Locator option = options.filter(new Locator.FilterOptions()
                    .setHasText(Pattern.compile("(^|\\s)" + regexLiteral(text) + "($|\\s)")));
            if (option.count() == 0) {
                option = options.filter(new Locator.FilterOptions().setHasText(text));
            }
            option.first().click();
            waitForPanelToClose(option.first());
        });
    }

    /** Opens a mat-select and picks the option with element id {@code optionId}. */
    public void selectOptionById(String triggerSelector, String optionId, String description) {
        step(description + " | Option → " + optionId, triggerSelector, () -> {
            openSelect(triggerSelector);
            Locator option = first(byId(optionId));
            option.click();
            waitForPanelToClose(option);
        });
    }

    /** Picks {@code date} (MM/dd/yyyy) in the form's mat-datepicker. Arabic calendars render
     *  Eastern Arabic digits, so year and day labels are converted for "ara". */
    public void pickDate(String date, String langCode) {
        String digits = date.replace("/", "");
        int month = Integer.parseInt(digits.substring(0, 2));
        String day = localizeDigits(String.valueOf(Integer.parseInt(digits.substring(2, 4))), langCode);
        String year = localizeDigits(digits.substring(4, 8), langCode);

        step("Pick date " + date, DATEPICKER_TOGGLE, () -> {
            first(DATEPICKER_TOGGLE).click();
            first(CALENDAR_ARROW).click();
            calendarCell(year).click();
            page.locator(CALENDAR_CELL).nth(month - 1).click();
            calendarCell(day).click();
            first(CALENDAR).waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN));
        });
    }

    private Locator calendarCell(String label) {
        return page.locator(CALENDAR_CELL)
                .filter(new Locator.FilterOptions().setHasText(exactText(label)))
                .first();
    }

    private static String localizeDigits(String number, String langCode) {
        if (!"ara".equalsIgnoreCase(langCode)) return number;
        StringBuilder out = new StringBuilder();
        for (char c : number.toCharArray()) {
            out.append(Character.isDigit(c) ? (char) ('\u0660' + (c - '0')) : c);
        }
        return out.toString();
    }

    // ── Create / confirm ───────────────────────────────────────────────────────

    /** Saves a create form and acknowledges the result dialog. The first click sometimes lands
     *  while the form is still validating and no dialog follows, so it is submitted once more. */
    public void submitCreateForm() {
        clickFirst(CREATE_BUTTON, "Click the Create button");
        clickIfVisible(CONFIRM_POPUP, "Confirm the submission", 8_000);
        if (isPresent(CONFIRM_MESSAGE_POPUP, 20_000)) {
            closeResultMessage("create");
        } else {
            logger.info("No result message after create — submitting the form again");
            clickFirst(CREATE_BUTTON, "Click the Create button again");
            clickIfVisible(CONFIRM_POPUP, "Confirm the submission", 8_000);
            closeResultMessage("create");
        }
    }

    protected void confirmAndClose(String what) {
        clickIfVisible(CONFIRM_POPUP, "Confirm " + what, 15_000);
        closeResultMessage(what);
    }

    /** Success and server errors share one dialog, so its text goes into the report — it is
     *  the only record of why a later step finds nothing to filter or edit. */
    private void closeResultMessage(String what) {
        first(CONFIRM_MESSAGE_POPUP).waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        try {
            String message = first(DIALOG_CONTENT).innerText(new Locator.InnerTextOptions().setTimeout(2_000)).trim();
            UiExtentReportManager.logStep("Portal message after " + what + ": " + message);
            logger.info("Portal message after {}: {}", what, message);
        } catch (PlaywrightException e) {
            logger.debug("Could not read the {} result message: {}", what, e.getMessage());
        }
        clickFirst(CONFIRM_MESSAGE_POPUP, "Close the " + what + " result message");
    }

    // ── List page: filter & row actions ────────────────────────────────────────

    /** Filters the list by a text field in the filter dialog. */
    public void filterBy(String fieldId, String value) {
        returnToList();
        clickFirst(byId(FILTER_BUTTON_ID), "Open the filter dialog");
        fillFirst(byId(fieldId), value, "Filter by " + fieldId);
        clickFirst(byId(APPLY_FILTER_ID), "Apply the filter");
    }

    /** Filters the list by a lookup field (Centers). Depending on the deployment's filter config
     *  the field is either a dropdown of existing values or a typed autocomplete. */
    public void filterByLookup(String fieldId, String value) {
        returnToList();
        clickFirst(byId(FILTER_BUTTON_ID), "Open the filter dialog");
        String field = byId(fieldId);
        String tag = (String) first(field).evaluate("el => el.tagName.toLowerCase()");
        if ("mat-select".equals(tag)) {
            selectOptionByText(field, value, "Filter by " + fieldId);
        } else {
            fillFirst(field, value, "Filter by " + fieldId);
            Locator suggestion = page.locator(OVERLAY_OPTIONS).filter(new Locator.FilterOptions().setHasText(value)).first();
            try {
                suggestion.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(20_000));
                suggestion.click();
            } catch (PlaywrightException e) {
                logger.info("No autocomplete suggestion for '{}' — filtering by the typed value", value);
            }
        }
        clickFirst(byId(APPLY_FILTER_ID), "Apply the filter");
        first(FIRST_ROW_ELLIPSIS).waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.ATTACHED).setTimeout(40_000));
    }

    /** Closes a leftover filter dialog or edit form so the list is reachable. Cancel on a form
     *  is a history back, so it is only clicked while the list's Filter button is not shown. */
    private void returnToList() {
        if (page.locator(byId(APPLY_FILTER_ID)).count() > 0) {
            Locator cancels = page.locator(byId(CANCEL_ID));
            if (cancels.count() > 0) cancels.last().click();
        }
        for (int i = 0; i < 4; i++) {
            if (isPresent(byId(FILTER_BUTTON_ID), 3_000)) return;
            Locator cancels = page.locator(byId(CANCEL_ID));
            if (cancels.count() == 0) return;
            cancels.first().click();
        }
    }

    /** Opens the first row's action menu and clicks {@code actionId}. The row often re-renders
     *  just after the filter applies and closes a freshly opened menu, hence the retries. */
    public void openRowAction(String actionId) {
        step("Open the row menu and click " + actionId, FIRST_ROW_ELLIPSIS, () -> {
            PlaywrightException last = null;
            for (int attempt = 1; attempt <= 3; attempt++) {
                try {
                    first(FIRST_ROW_ELLIPSIS).click();
                    first(ROW_MENU).waitFor(new Locator.WaitForOptions()
                            .setState(WaitForSelectorState.VISIBLE).setTimeout(8_000));
                    first(byId(actionId)).click(new Locator.ClickOptions().setTimeout(SHORT_TIMEOUT_MS));
                    return;
                } catch (PlaywrightException e) {
                    last = e;
                    logger.info("Row menu closed before {} was clicked, retry {}", actionId, attempt);
                }
            }
            throw last;
        });
    }

    public void activateFirstRecord() {
        openRowAction("Activate0");
        confirmAndClose("activation");
    }

    public void deactivateFirstRecord() {
        openRowAction("Deactivate0");
        confirmAndClose("deactivation");
    }

    public void decommissionFirstRecord() {
        openRowAction("Decommission0");
        confirmAndClose("decommission");
    }

    /** Edits one field of the first record and saves it. */
    public void editFirstRecord(String fieldId, String newValue) {
        openRowAction("Edit0");
        String field = byId(fieldId);
        waitForRecordToLoad(field);
        step("Check " + fieldId + " currently differs from the new value", field, () -> {
            String current = first(field).inputValue();
            if (newValue.equals(current)) {
                throw new AssertionError(fieldId + " already holds '" + newValue + "' before the edit");
            }
        });
        fillAndHold(field, newValue, "Change " + fieldId);
        clickFirst(CREATE_BUTTON, "Save the changes");
        confirmAndClose("update");
    }
}

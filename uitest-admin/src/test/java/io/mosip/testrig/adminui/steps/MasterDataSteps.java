package io.mosip.testrig.adminui.steps;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.When;
import io.mosip.testrig.adminui.config.AdminConfigManager;
import io.mosip.testrig.adminui.pages.MasterDataPage;
import io.mosip.testrig.uitestrig.browser.PlaywrightFactory;

import java.util.Map;

import static io.mosip.testrig.adminui.hooks.AdminTestData.resolve;

/**
 * CRUD steps shared by every master data and resource page. Values may contain
 * {@code ${name}} placeholders for the run's generated test data (see AdminTestData).
 */
public class MasterDataSteps {

    private MasterDataPage page() {
        return new MasterDataPage(PlaywrightFactory.getPage());
    }

    // ── Navigation ─────────────────────────────────────────────────────────────

    @When("the admin opens the {string} master data page")
    public void openMasterDataPage(String cardName) {
        page().openCard(cardName);
    }

    @When("the admin opens the {string} resource page")
    public void openResourcePage(String resourceName) {
        page().openResourcePage(resourceName);
    }

    @When("the admin opens the create dynamic field form")
    public void openCreateDynamicField() {
        page().openCreateDynamicField();
    }

    @When("the admin clicks {string}")
    public void clickButton(String buttonId) {
        page().clickButton(buttonId);
    }

    // ── Form ───────────────────────────────────────────────────────────────────

    @When("the admin enters the following values:")
    public void enterValues(DataTable fields) {
        for (Map.Entry<String, String> field : fields.asMap(String.class, String.class).entrySet()) {
            page().fillField(field.getKey(), resolve(field.getValue()));
        }
    }

    @When("the admin selects the first {string} option")
    public void selectFirstOption(String fieldId) {
        page().selectFirstOption(fieldId);
    }

    @When("the admin selects the first {string} option if it is available")
    public void selectFirstOptionIfAvailable(String fieldId) {
        page().selectFirstOptionIfAvailable(fieldId);
    }

    @When("the admin selects {string} in {string}")
    public void selectOption(String optionText, String fieldId) {
        page().selectOption(fieldId, resolve(optionText));
    }

    @When("the admin picks the date {string}")
    public void pickDate(String date) {
        page().pickDate(resolve(date), AdminConfigManager.getLoginLang());
    }

    @When("the admin submits the new record")
    public void submit() {
        page().submitCreateForm();
    }

    // ── List ───────────────────────────────────────────────────────────────────

    @When("the admin filters the list by {string} {string}")
    public void filter(String fieldId, String value) {
        page().filterBy(fieldId, resolve(value));
    }

    @When("the admin changes {string} of the first record to {string}")
    public void edit(String fieldId, String value) {
        page().editFirstRecord(fieldId, resolve(value));
    }

    @When("the admin activates the first record")
    public void activate() {
        page().activateFirstRecord();
    }

    @When("the admin deactivates the first record")
    public void deactivate() {
        page().deactivateFirstRecord();
    }

    @When("the admin decommissions the first record")
    public void decommission() {
        page().decommissionFirstRecord();
    }
}

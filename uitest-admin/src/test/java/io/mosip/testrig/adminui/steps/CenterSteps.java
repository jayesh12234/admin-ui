package io.mosip.testrig.adminui.steps;

import io.cucumber.java.en.When;
import io.mosip.testrig.adminui.config.AdminConfigManager;
import io.mosip.testrig.adminui.config.AdminUtil;
import io.mosip.testrig.adminui.pages.CenterPage;
import io.mosip.testrig.uitestrig.browser.PlaywrightFactory;

import static io.mosip.testrig.adminui.hooks.AdminTestData.resolve;

public class CenterSteps {

    private CenterPage page() {
        return new CenterPage(PlaywrightFactory.getPage());
    }

    @When("the admin selects the first location at every hierarchy level")
    public void selectLocations() {
        page().selectFirstLocationForEachLevel(AdminUtil.getLocationHierarchyDepth());
    }

    @When("the admin selects the first {int} working days")
    public void selectWorkingDays(int count) {
        page().selectFirstWorkingDays(count);
    }

    @When("the admin adds the exceptional holiday {string}")
    public void addExceptionalHoliday(String date) {
        page().addExceptionalHoliday(resolve(date), AdminConfigManager.getLoginLang());
    }

    @When("the admin filters the centers by name {string}")
    public void filterByName(String name) {
        page().filterByLookup("name", resolve(name));
    }

    @When("the admin changes the center name to {string}")
    public void editName(String name) {
        page().editCenterName(resolve(name));
    }
}

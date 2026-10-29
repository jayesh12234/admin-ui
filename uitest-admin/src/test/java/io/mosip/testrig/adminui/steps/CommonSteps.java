package io.mosip.testrig.adminui.steps;

import io.cucumber.java.en.Given;
import io.mosip.testrig.adminui.config.AdminConfigManager;
import io.mosip.testrig.adminui.pages.LoginPage;
import io.mosip.testrig.uitestrig.browser.PlaywrightFactory;

public class CommonSteps {

    private LoginPage page() {
        return new LoginPage(PlaywrightFactory.getPage());
    }

    @Given("the admin user is logged in to the admin portal")
    public void loginAsAdmin() {
        if (AdminConfigManager.isLocalPortal()) {
            // Already authenticated by the cookie AdminLocalSession stored.
            page().waitForPortalHome();
            return;
        }
        page().login(AdminConfigManager.getAdminUserId(), AdminConfigManager.getAdminPassword(),
                AdminConfigManager.getLoginLang());
    }
}

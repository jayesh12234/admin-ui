package io.mosip.testrig.adminui.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;
import io.mosip.testrig.uitestrig.reporting.UiExtentReportManager;

import java.util.regex.Pattern;

import static io.mosip.testrig.adminui.core.AdminGlobalConstants.*;

/** Keycloak login page the portal redirects to, and the portal home it lands on afterwards. */
public class LoginPage extends AdminBasePage {

    private static final String PORTAL_HOME = MENU_MASTER_DATA + ", " + byId(MENU_RESOURCES_ID) + ", " + byId(MENU_BULK_UPLOAD_ID);

    public LoginPage(Page page) {
        super(page);
    }

    public void login(String username, String password, String loginLang) {
        step("Wait for the Keycloak login page", KC_USERNAME, () -> first(KC_USERNAME)
                .waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE)));
        selectLanguage(loginLang);
        fillFirst(KC_USERNAME, username, "Enter the username");
        // Logged without the value — the password must not reach the report.
        UiExtentReportManager.logStepWithLocator("Enter the password", KC_PASSWORD);
        first(KC_PASSWORD).fill(password);
        clickFirst(KC_LOGIN_BUTTON, "Click Log In");
        waitForPortalHome();
    }

    /** Any of the role-dependent menus means the portal has loaded for this user. */
    public void waitForPortalHome() {
        step("Wait for the Admin Portal home page", PORTAL_HOME, () -> first(PORTAL_HOME)
                .waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(60_000)));
    }

    /** Keycloak only shows the language dropdown when the realm has more than one locale. */
    private void selectLanguage(String loginLang) {
        if (page.locator(KC_LOCALE_DROPDOWN).count() == 0) {
            logger.info("No Keycloak language dropdown — keeping the default login language");
            return;
        }
        String label = KC_LANGUAGE_LABELS.getOrDefault(loginLang.toLowerCase(), loginLang);
        clickFirst(KC_LOCALE_DROPDOWN, "Open the login language dropdown");
        Locator options = page.locator(KC_LOCALE_OPTIONS);
        Locator option = options.filter(new Locator.FilterOptions().setHasText(exactText(label)));
        if (option.count() == 0) {
            option = options.filter(new Locator.FilterOptions()
                    .setHasText(Pattern.compile(regexLiteral(loginLang), Pattern.CASE_INSENSITIVE)));
        }
        Locator chosen = option.first();
        step("Select login language " + label, KC_LOCALE_OPTIONS, () -> {
            chosen.click();
            first(KC_USERNAME).waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        });
    }
}

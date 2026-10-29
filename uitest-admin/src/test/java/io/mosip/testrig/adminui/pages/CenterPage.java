package io.mosip.testrig.adminui.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;

import static io.mosip.testrig.adminui.core.AdminGlobalConstants.*;

/** Create/edit Registration Center form — the parts that differ from the generic CRUD form. */
public class CenterPage extends MasterDataPage {

    private static final String LOCATION_FIELD = "fieldName";
    // Scoped to the form's selection list — the side navigation is also built from .mat-list-item.
    private static final String WORKING_DAY = "mat-selection-list .mat-list-item:nth-child(%d) > .mat-list-item-content";
    private static final String CREATE_EXCEPTIONAL_HOLIDAY_ID = "createExceptionalHoliday";

    public CenterPage(Page page) {
        super(page);
    }

    /** One location dropdown is rendered per hierarchy level, all with the same id. */
    public void selectFirstLocationForEachLevel(int levels) {
        for (int i = 1; i <= levels; i++) {
            selectFirstOption("xpath=(//*[@id='" + LOCATION_FIELD + "'])[" + i + "]",
                    "Select the first location at hierarchy level " + i);
        }
    }

    public void selectFirstWorkingDays(int count) {
        for (int i = 1; i <= count; i++) {
            clickFirst(String.format(WORKING_DAY, i), "Select working day " + i);
        }
    }

    public void addExceptionalHoliday(String date, String langCode) {
        pickDate(date, langCode);
        clickFirst(byId(CREATE_EXCEPTIONAL_HOLIDAY_ID), "Add the exceptional holiday");
    }

    public void editCenterName(String newName) {
        openRowAction("Edit0");
        waitForRecordToLoad(byId("name"));
        fillAndHold(byId("name"), newName, "Change the center name");
        clickFirst(CREATE_BUTTON, "Save the changes");
        confirmAndClose("update");
        backToList();
    }

    /** After an update the portal either returns to the list or opens the center's
     *  single-view page, whose buttons have no Cancel id; Cancel elsewhere is a history back that
     *  can overshoot into the Create Center form. The side menu's Centers link always works. */
    private void backToList() {
        if (isPresent(byId(FILTER_BUTTON_ID), 3_000)) return;
        String centersLink = RESOURCE_LINKS.get("Centers");
        if (!isPresent(centersLink, 1_000)) {
            clickFirst(byId(MENU_RESOURCES_ID), "Open the Resources menu");
        }
        clickFirst(centersLink, "Go back to the Centers list");
        clickIfVisible(CONFIRM_POPUP, "Leave the center page", 3_000);
        step("Wait for the Centers list", byId(FILTER_BUTTON_ID), () -> first(byId(FILTER_BUTTON_ID))
                .waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE)));
    }
}

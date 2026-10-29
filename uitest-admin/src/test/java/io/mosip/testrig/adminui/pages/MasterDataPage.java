package io.mosip.testrig.adminui.pages;

import com.microsoft.playwright.Page;

import static io.mosip.testrig.adminui.core.AdminGlobalConstants.*;

/**
 * Any master data or resource list page with its create/edit form — the CRUD flow is the same
 * for blocklisted words, center/device/machine types and specs, documents, holidays, templates,
 * dynamic fields, devices and machines.
 */
public class MasterDataPage extends AdminBasePage {

    public MasterDataPage(Page page) {
        super(page);
    }

    public void openCard(String cardName) {
        String cardId = MASTER_DATA_CARDS.get(cardName);
        if (cardId == null) {
            throw new IllegalArgumentException("Unknown master data page '" + cardName + "'. Known: " + MASTER_DATA_CARDS.keySet());
        }
        openMasterDataCard(cardId);
    }

    public void openResourcePage(String resourceName) {
        String link = RESOURCE_LINKS.get(resourceName);
        if (link == null) {
            throw new IllegalArgumentException("Unknown resource page '" + resourceName + "'. Known: " + RESOURCE_LINKS.keySet());
        }
        openResource(link, resourceName);
    }

    public void openCreateDynamicField() {
        clickFirst(MENU_MASTER_DATA, "Open the Master Data menu");
        clickFirst(byId(CREATE_DYNAMIC_FIELD_ID), "Open the Create Dynamic Field form");
    }

    public void clickButton(String buttonId) {
        clickFirst(byId(buttonId), "Click '" + buttonId + "'");
    }

    public void selectFirstOption(String fieldId) {
        selectFirstOption(byId(fieldId), "Select the first " + fieldId + " option");
    }

    /** For fields the form only offers in some configurations — e.g. zone, which is hidden or
     *  pre-filled and disabled when the chosen location already determines it. */
    public void selectFirstOptionIfAvailable(String fieldId) {
        String field = byId(fieldId);
        if (isPresent(field, 5_000) && first(field).isEnabled()) {
            selectFirstOption(fieldId);
        } else {
            logger.info("{} is not selectable on this form — skipping", fieldId);
        }
    }

    public void selectOption(String fieldId, String optionText) {
        selectOptionByText(byId(fieldId), optionText, "Select " + fieldId);
    }
}

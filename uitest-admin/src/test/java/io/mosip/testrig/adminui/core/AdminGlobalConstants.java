package io.mosip.testrig.adminui.core;

import java.util.Map;

/**
 * Admin Portal element ids and lookup tables shared by page objects and steps.
 * These are portal-specific and must not live in uitest-commons.
 */
public final class AdminGlobalConstants {

    // ── Navigation ─────────────────────────────────────────────────────────────
    public static final String MENU_MASTER_DATA  = "a[href='#/admin/masterdata']";
    public static final String MENU_RESOURCES_ID = "admin/resources";
    public static final String MENU_BULK_UPLOAD_ID = "admin/bulkupload";
    public static final String LINK_MASTER_DATA_UPLOAD = "a[href='#/admin/bulkupload/masterdataupload']";
    public static final String CREATE_DYNAMIC_FIELD_ID = "createDynamicField";

    /** Master data card name (as used in feature files) → card element id on the Master Data page. */
    public static final Map<String, String> MASTER_DATA_CARDS = Map.ofEntries(
            Map.entry("Blocklisted Words",      "admin/masterdata/blocklisted-words/view"),
            Map.entry("Center Types",           "admin/masterdata/center-type/view"),
            Map.entry("Device Specifications",  "admin/masterdata/device-specs/view"),
            Map.entry("Device Types",           "admin/masterdata/device-types/view"),
            Map.entry("Document Categories",    "admin/masterdata/document-categories/view"),
            Map.entry("Document Types",         "admin/masterdata/document-type/view"),
            Map.entry("Holidays",               "admin/masterdata/holiday/view"),
            Map.entry("Machine Specifications", "admin/masterdata/machine-specs/view"),
            Map.entry("Machine Types",          "admin/masterdata/machine-type/view"),
            Map.entry("Templates",              "admin/masterdata/templates/view"));

    /** Resource name (as used in feature files) → its link under the Resources menu. */
    public static final Map<String, String> RESOURCE_LINKS = Map.of(
            "Centers",  "[id='/admin/resources/centers']",
            "Devices",  "[id='/admin/resources/devices']",
            "Machines", "a[href='#/admin/resources/machines']");

    // ── List pages ─────────────────────────────────────────────────────────────
    public static final String FILTER_BUTTON_ID   = "Filter";
    public static final String APPLY_FILTER_ID    = "applyTxt";
    public static final String CANCEL_ID          = "cancel";
    public static final String FIRST_ROW_ELLIPSIS = "#ellipsis-button0";
    public static final String ROW_MENU           = ".cdk-overlay-container .mat-menu-panel";

    // ── Forms & dialogs ────────────────────────────────────────────────────────
    public static final String CREATE_BUTTON          = "button#createButton";
    public static final String CONFIRM_POPUP          = "#confirmpopup";
    public static final String CONFIRM_MESSAGE_POPUP  = "#confirmmessagepopup";
    public static final String OVERLAY_OPTIONS        = ".cdk-overlay-container mat-option";
    public static final String DIALOG_CONTENT         = ".mat-dialog-content";
    public static final String DIALOG_MESSAGE         = "div.mat-dialog-content div";

    // ── Date picker ────────────────────────────────────────────────────────────
    public static final String DATEPICKER_TOGGLE = ".mat-datepicker-toggle button";
    public static final String CALENDAR_ARROW    = ".mat-calendar-arrow";
    public static final String CALENDAR_CELL     = ".mat-calendar-body-cell-content";
    public static final String CALENDAR          = "mat-calendar";

    // ── Keycloak login page ────────────────────────────────────────────────────
    public static final String KC_LOCALE_DROPDOWN = "#kc-locale-dropdown";
    public static final String KC_LOCALE_OPTIONS  = "#kc-locale-dropdown li a";
    public static final String KC_USERNAME        = "#username";
    public static final String KC_PASSWORD        = "#password";
    public static final String KC_LOGIN_BUTTON    = "input[name='login']";

    /** loginlang → label shown in the Keycloak language dropdown. */
    public static final Map<String, String> KC_LANGUAGE_LABELS = Map.of(
            "sin", "English",
            "eng", "English",
            "fra", "Français",
            "ara", "عربي",
            "hin", "हिन्दी",
            "tam", "தமிழ்",
            "kan", "ಕನ್ನಡ");

    private AdminGlobalConstants() {}
}

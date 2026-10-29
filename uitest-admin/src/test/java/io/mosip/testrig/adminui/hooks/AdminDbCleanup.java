package io.mosip.testrig.adminui.hooks;

import io.mosip.testrig.adminui.config.AdminConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Deletes the mosip_master rows created by this run's admin user, so repeated runs do not pile
 * up test centers, devices, machines, etc. Rows are matched on cr_by, which is unique per run.
 */
final class AdminDbCleanup {

    private static final Logger logger = LoggerFactory.getLogger(AdminDbCleanup.class);

    private static final List<String> TABLES = List.of(
            "applicant_valid_document", "biometric_attribute", "biometric_type", "blocklisted_words",
            "device_master", "device_master_h", "device_spec", "device_type",
            "doc_category", "doc_type", "dynamic_field", "language", "location", "loc_hierarchy_list",
            "loc_holiday", "machine_master", "machine_master_h", "machine_spec", "machine_type",
            "module_detail", "reason_category", "reason_list",
            "registration_center", "registration_center_h", "reg_center_type",
            "reg_exceptional_holiday", "reg_working_nonworking",
            "template", "template_file_format", "template_type", "title",
            "user_detail", "user_detail_h", "valid_document", "zone", "zone_user", "zone_user_h",
            "bulkupload_transaction");

    // Tables reference each other (e.g. device_master → device_spec), so a delete that fails on
    // a foreign key is retried after the other tables have been cleared.
    private static final int MAX_PASSES = 3;

    private AdminDbCleanup() {}

    static void run() {
        if (!AdminConfigManager.isCleanupMasterData()) {
            logger.info("cleanupMasterData=no — leaving master data in place");
            return;
        }
        String password = AdminConfigManager.getMasterDbPass();
        String server = AdminConfigManager.getDbServer();
        if (password == null || password.isBlank() || server == null || server.isBlank()) {
            logger.warn("db-server/postgres-password not configured — skipping master data cleanup");
            return;
        }

        String createdBy = AdminConfigManager.getAdminUserId();
        String schema = AdminConfigManager.getMasterDbSchema();
        try (Connection connection = DriverManager.getConnection(
                AdminConfigManager.getMASTERDbUrl(), AdminConfigManager.getMasterDbUser(), password)) {
            List<String> pending = new ArrayList<>(TABLES);
            for (int pass = 1; pass <= MAX_PASSES && !pending.isEmpty(); pass++) {
                List<String> failed = new ArrayList<>();
                for (String table : pending) {
                    try (PreparedStatement delete = connection.prepareStatement(
                            "delete from " + schema + "." + table + " where cr_by = ?")) {
                        delete.setString(1, createdBy);
                        int rows = delete.executeUpdate();
                        if (rows > 0) logger.info("Deleted {} row(s) from {}", rows, table);
                    } catch (SQLException e) {
                        failed.add(table);
                        logger.debug("Cleanup of {} failed on pass {}: {}", table, pass, e.getMessage());
                    }
                }
                pending = failed;
            }
            if (!pending.isEmpty()) {
                logger.warn("Could not clean up master data tables {} for {}", pending, createdBy);
            }
        } catch (SQLException e) {
            logger.error("Master data cleanup failed to connect: {}", e.getMessage());
        }
    }
}

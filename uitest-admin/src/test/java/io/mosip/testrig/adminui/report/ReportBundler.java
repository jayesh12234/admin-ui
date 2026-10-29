package io.mosip.testrig.adminui.report;

import io.mosip.testrig.adminui.config.AdminConfigManager;
import io.mosip.testrig.apirig.utils.S3Adapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Packs the run's HTML report and its evidence (screenshots, recordings, traces) into one zip
 * with the same folder layout, so the report still shows them when it is shared — the HTML
 * only links to those files. Uploaded next to the HTML when push-reports-to-s3=yes.
 */
public final class ReportBundler {

    private static final Logger logger = LoggerFactory.getLogger(ReportBundler.class);

    private ReportBundler() {}

    /** Call after the Extent report has been flushed and renamed. */
    public static void bundle() {
        if (!AdminConfigManager.isZipReport()) return;
        Path reportDir = ScenarioRecorder.reportDir();
        Path artifacts = reportDir.resolve("artifacts").resolve(ScenarioRecorder.RUN_ID);
        if (!Files.isDirectory(artifacts)) {
            logger.info("No report evidence for this run — the HTML report is self-contained");
            return;
        }
        Optional<Path> report = latestReport(reportDir);
        if (report.isEmpty()) {
            logger.warn("No HTML report found in {} — skipping the report bundle", reportDir);
            return;
        }
        Path html = report.get();
        Path zip = reportDir.resolve(html.getFileName().toString().replaceFirst("\\.html$", ".zip"));
        try {
            writeZip(zip, html, reportDir, artifacts);
            logger.info("Shareable report bundle: {} ({} MB) — unzip and open {}",
                    zip, Files.size(zip) / (1024 * 1024), html.getFileName());
        } catch (IOException e) {
            logger.error("Could not create the report bundle {}: {}", zip, e.getMessage());
            return;
        }
        if ("yes".equalsIgnoreCase(AdminConfigManager.getproperty("push-reports-to-s3"))) {
            upload(zip);
        }
    }

    /** The report written by this JVM — UiExtentReportManager renames it on flush. */
    private static Optional<Path> latestReport(Path reportDir) {
        long jvmStart = ManagementFactory.getRuntimeMXBean().getStartTime();
        try (Stream<Path> files = Files.list(reportDir)) {
            return files.filter(p -> p.toString().endsWith(".html"))
                    .filter(p -> p.toFile().lastModified() >= jvmStart)
                    .max(Comparator.comparingLong(p -> p.toFile().lastModified()));
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    private static void writeZip(Path zip, Path html, Path reportDir, Path artifacts) throws IOException {
        List<Path> evidence;
        try (Stream<Path> files = Files.walk(artifacts)) {
            evidence = files.filter(Files::isRegularFile).sorted().toList();
        }
        try (OutputStream out = Files.newOutputStream(zip); ZipOutputStream zipOut = new ZipOutputStream(out)) {
            add(zipOut, html, html.getFileName().toString());
            for (Path file : evidence) {
                add(zipOut, file, reportDir.relativize(file).toString().replace('\\', '/'));
            }
        }
    }

    private static void add(ZipOutputStream zipOut, Path file, String entryName) throws IOException {
        zipOut.putNextEntry(new ZipEntry(entryName));
        Files.copy(file, zipOut);
        zipOut.closeEntry();
    }

    private static void upload(Path zip) {
        try {
            boolean uploaded = new S3Adapter().putObject(AdminConfigManager.getproperty("s3-account"),
                    "", null, null, zip.getFileName().toString(), zip.toFile());
            if (uploaded) logger.info("Report bundle uploaded to S3: {}", zip.getFileName());
            else logger.error("S3 upload returned false for {}", zip.getFileName());
        } catch (Exception e) {
            logger.error("Failed to upload the report bundle to S3: {}", e.getMessage());
        }
    }
}

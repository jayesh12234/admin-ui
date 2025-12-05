package io.mosip.testrig.adminui.utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;

import io.mosip.testrig.adminui.kernel.util.ConfigManager;
import io.mosip.testrig.adminui.kernel.util.S3Adapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdminExtentReportManager {

	private static ExtentReports extent;
	private static final ThreadLocal<ExtentTest> testThread = new ThreadLocal<>();
	private static final Logger LOGGER = LoggerFactory.getLogger(AdminExtentReportManager.class);

	private static boolean systemInfoAdded = false;
	private static String gitBranch = "unknown";
	private static String gitCommitId = "unknown";
	private static final String currentDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

	private static int passedCount = 0;
	private static int failedCount = 0;
	private static int skippedCount = 0;
	private static String reportPath;

	public synchronized static void initReport() {
		if (extent == null) {
			fetchGitDetails();

			String reportName = "Admin UI Test Execution Report";
			reportName += " ---- Admin UI ---- Report Date: " + currentDate + " ---- Tested Environment: "
					+ safeEnvName() + " ---- Branch: " + gitBranch + " & Commit: " + gitCommitId;

			String timestamp = new SimpleDateFormat("yyyy-MMM-dd_HH-mm").format(new Date()).toLowerCase();
			reportPath = "test-output/AdminReport_" + timestamp + ".html";

			ExtentSparkReporter spark = new ExtentSparkReporter(reportPath);
			spark.config().setTheme(Theme.DARK);
			spark.config().setDocumentTitle("Admin UI Automation Report");
			spark.config().setReportName(reportName);

			extent = new ExtentReports();
			extent.attachReporter(spark);

			addSystemInfo();
		}
	}

	public synchronized static ExtentTest createTest(String testName) {
		initReport();
		ExtentTest test = extent.createTest(testName);
		testThread.set(test);
		return test;
	}

	public static ExtentTest getTest() {
		return testThread.get();
	}

	public static void removeTest() {
		testThread.remove();
	}

	private synchronized static void addSystemInfo() {
		if (extent != null && !systemInfoAdded) {
			LOGGER.info("Adding Git info to extent report: Branch = {}, Commit = {}", gitBranch, gitCommitId);

			extent.setSystemInfo("Git Branch", gitBranch);
			extent.setSystemInfo("Git Commit ID", gitCommitId);
			extent.setSystemInfo("Generated On", currentDate);
			extent.setSystemInfo("Env", safeEnvName());

			try {
				extent.setSystemInfo("Run Language", ConfigManager.getloginlang());
			} catch (Exception ignored) {
			}

			systemInfoAdded = true;
		}
	}

	private static String runCommand(String... command) throws IOException {
		Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {

			String line = reader.readLine();
			int exit = 0;
			try {
				exit = process.waitFor();
			} catch (InterruptedException ie) {
			}
			if (exit == 0 && line != null) {
				return line.trim();
			}
		}
		throw new IOException("Command produced no output or non-zero exit code");
	}

	private static void fetchGitDetails() {
		try {
			gitBranch = runCommand("git", "rev-parse", "--abbrev-ref", "HEAD");
			gitCommitId = runCommand("git", "rev-parse", "--short", "HEAD");
			LOGGER.info("Fetched git via CLI: branch={}, commit={}", gitBranch, gitCommitId);
		} catch (Exception e) {
			LOGGER.warn("Failed to fetch git via CLI: {}. Falling back to git.properties", e.getMessage());
			loadFromGitProperties();
		}
	}

	private static void loadFromGitProperties() {
		Properties properties = new Properties();
		try (InputStream is = AdminExtentReportManager.class.getClassLoader().getResourceAsStream("git.properties")) {

			if (is != null) {
				properties.load(is);
				gitBranch = properties.getProperty("git.branch", "unknown");
				gitCommitId = properties.getProperty("git.commit.id.abbrev", "unknown");
				LOGGER.info("Loaded git.properties: branch={}, commit={}", gitBranch, gitCommitId);
			} else {
				LOGGER.warn("git.properties not found; using defaults");
			}
		} catch (IOException ex) {
			LOGGER.error("Error reading git.properties: {}", ex.getMessage());
		}
	}

	private static String safeEnvName() {
		try {
			String env = ConfigManager.getiam_adminportal_path();
			if (env != null && !env.isEmpty()) {
				env = env.toLowerCase().replace("https://", "").replace("http://", "");
				return env.replaceAll("^.*?\\.([^.]+)\\.mosip.*$", "$1");
			}
		} catch (Exception ignored) {
		}

		return "unknown";
	}

	public synchronized static void incrementPassed() {
		passedCount++;
	}

	public synchronized static void incrementFailed() {
		failedCount++;
	}

	public synchronized static void incrementSkipped() {
		skippedCount++;
	}

	public static int getPassedCount() {
		return passedCount;
	}

	public static int getFailedCount() {
		return failedCount;
	}

	public static int getSkippedCount() {
		return skippedCount;
	}

	public static int getTotalCount() {
		return passedCount + failedCount + skippedCount;
	}

	public static void logStep(String message) {
		ExtentTest test = testThread.get();
		if (test != null) {
			test.info(message);
		} else {
			LOGGER.warn("logStep called but no extent test active: {}", message);
		}
	}

	public static void logStepWithLocator(String message, String locatorHtml) {
		ExtentTest test = testThread.get();
		if (test != null) {
			String safeLocator = (locatorHtml == null) ? "[no locator]" : locatorHtml;
			String html = message + "<details><summary>Locator Details</summary><pre>" + escapeHtml(safeLocator)
					+ "</pre></details>";
			test.info(html);
		} else {
			LOGGER.warn("logStepWithLocator called but no extent test active: {} / {}", message, locatorHtml);
		}
	}

	public static void logLocator(String locatorHtml) {
		ExtentTest test = testThread.get();
		if (test != null) {
			String safeLocator = (locatorHtml == null) ? "[no locator]" : locatorHtml;
			String html = "<details><summary>Locator Details</summary><pre>" + escapeHtml(safeLocator)
					+ "</pre></details>";
			test.info(html);
		} else {
			LOGGER.warn("logLocator called but no extent test active: {}", locatorHtml);
		}
	}

	private static String escapeHtml(String s) {
		if (s == null)
			return "";
		return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
	}

	public static void attachScreenshotFromBase64(String base64, String title) {
		ExtentTest test = testThread.get();
		if (test != null && base64 != null) {
			try {
				test.info(title, MediaEntityBuilder.createScreenCaptureFromBase64String(base64).build());
			} catch (Exception e) {
				LOGGER.warn("Failed to attach screenshot to report: {}", e.getMessage());
			}
		}
	}

	public static synchronized void flushReport() {
	    if (extent != null) {
	        extent.flush();
	        LOGGER.info("Extent report flushed successfully.");

	        try {
	        	pushReportToS3(reportPath);
	        } catch (Exception e) {
	            LOGGER.error("Error while uploading report: ", e);
	        }
	    }
	}
	
	public static synchronized void pushReportToS3(String reportFilePath) {
	    if (ConfigManager.getPushReportsToS3().equalsIgnoreCase("yes")) {
	        S3Adapter s3Adapter = new S3Adapter();
	        File reportFile = new File(reportFilePath);
	        boolean isStoreSuccess = false;
	        try {
	            isStoreSuccess = s3Adapter.putObject(
	                    ConfigManager.getS3Account(), 
	                    "Adminui", 
	                    null, 
	                    null, 
	                    reportFile.getName(), 
	                    reportFile
	            );
	            if (isStoreSuccess) {
	            	LOGGER.info("Admin Extent report successfully pushed to S3/MinIO: {} "+reportFile.getName());
	            } else {
	            	LOGGER.error("Failed to push Admin Extent report to S3/MinIO: { } "+ reportFile.getName());
	            }
	        } catch (Exception e) {
	        	LOGGER.error("Exception while pushing Admin Extent report to S3/MinIO: {} "+e.getMessage());
	        }
	    }
	}


}

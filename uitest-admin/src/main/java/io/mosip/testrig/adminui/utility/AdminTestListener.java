package io.mosip.testrig.adminui.utility;

import org.testng.ITestListener;
import org.testng.ITestResult;

import com.aventstack.extentreports.ExtentTest;

import org.testng.ITestContext;

import java.io.File;

import org.apache.log4j.Logger;

import io.mosip.testrig.adminui.kernel.util.ConfigManager;

public class AdminTestListener implements ITestListener {
	static Logger logger = Logger.getLogger(EmailableReport.class);

	@Override
	public void onStart(ITestContext context) {
		AdminExtentReportManager.initReport();
	}

	@Override
	public void onTestStart(ITestResult result) {
		String className = result.getTestClass().getRealClass().getSimpleName();
		String methodName = result.getMethod().getMethodName();

		String lang = "unknown";
		try {
			lang = ConfigManager.getloginlang();
		} catch (Exception ignored) {
		}

		String testName = className + " | " + methodName + " | Language: " + lang;

		AdminExtentReportManager.createTest(testName);
		AdminExtentReportManager.logStep("🟦 Test Started : " + testName);
	}

	@Override
	public void onTestSuccess(ITestResult result) {
		ExtentTest test = AdminExtentReportManager.getTest();
		if (test != null) {
			AdminExtentReportManager.logStep("🟩 Test Passed : " + result.getName());
			AdminExtentReportManager.incrementPassed();
			test.pass("Test Passed Successfully!");
		}
	}

	@Override
	public void onTestFailure(ITestResult result) {
		ExtentTest test = AdminExtentReportManager.getTest();
		if (test != null) {
			AdminExtentReportManager.logStep("🟥 Test Failed : " + result.getName());
			AdminExtentReportManager.incrementFailed();
			test.fail(result.getThrowable());
		}
	}

	@Override
	public void onTestSkipped(ITestResult result) {
		ExtentTest test = AdminExtentReportManager.getTest();
		if (test != null) {
			if (TestRunner.knownIssues.contains(result.getName())) {
				AdminExtentReportManager.logStep("🟠 Known Issue : " + result.getName());
				AdminExtentReportManager.incrementKnownIssue();
				test.skip("Marked as Known Issue");

			} else {

				AdminExtentReportManager.logStep("⚠️ Test Skipped : " + result.getName());
				AdminExtentReportManager.incrementSkipped();
				test.skip("Skipped due to preconditions/Dependencies");

			}
		}
	}

	@Override
	public void onFinish(ITestContext context) {

		AdminExtentReportManager.flushReport();

		int passed = AdminExtentReportManager.getPassedCount();
		int failed = AdminExtentReportManager.getFailedCount();
		int skipped = AdminExtentReportManager.getSkippedCount();
		int knownIssues = AdminExtentReportManager.getKnownIssueCount();

		int total = passed + failed + skipped + knownIssues;

		String originalReport = AdminExtentReportManager.getReportPath();
		File oldFile = new File(originalReport);

		String newReportName = originalReport.replace(".html",
				"-T-" + total + "-P-" + passed + "-F-" + failed + "-S-" + skipped + "-KI-" + knownIssues + ".html");

		File newFile = new File(newReportName);

		if (oldFile.renameTo(newFile)) {
			logger.info("Renamed Report: " + newReportName);
		} else {
			logger.info("Report rename failed, still uploading original name.");
			newReportName = originalReport;
		}

		AdminExtentReportManager.pushReportToS3(newReportName);
	}

	@Override
	public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
	}
}

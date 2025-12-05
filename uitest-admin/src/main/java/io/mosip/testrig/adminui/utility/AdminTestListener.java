package io.mosip.testrig.adminui.utility;

import org.testng.ITestListener;
import org.testng.ITestResult;

import com.aventstack.extentreports.ExtentTest;

import org.testng.ITestContext;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import io.mosip.testrig.adminui.utility.BaseClass;

import io.mosip.testrig.adminui.utility.AdminExtentReportManager;

public class AdminTestListener implements ITestListener {

    @Override
    public void onStart(ITestContext context) {
        AdminExtentReportManager.initReport();
    }

    @Override
    public void onTestStart(ITestResult result) {
        String testName = result.getMethod().getMethodName();

        if (result.getParameters().length > 0) {
            testName = testName + " | Params: " + java.util.Arrays.toString(result.getParameters());
        }

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
            AdminExtentReportManager.logStep("⚠️ Test Skipped : " + result.getName());
            AdminExtentReportManager.incrementSkipped();
            test.skip("Skipped due to preconditions/Dependencies");
        }
    }
    
    @Override
    public void onFinish(ITestContext context) {
        AdminExtentReportManager.flushReport();
    }

    @Override public void onTestFailedButWithinSuccessPercentage(ITestResult result) {}
}

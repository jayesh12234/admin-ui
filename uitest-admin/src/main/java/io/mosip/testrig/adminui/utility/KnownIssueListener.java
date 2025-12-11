package io.mosip.testrig.adminui.utility;

import org.apache.log4j.Logger;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;
import org.testng.SkipException;

public class KnownIssueListener implements IInvokedMethodListener {
	static Logger logger = Logger.getLogger(KnownIssueListener.class);

    @Override
    public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
        if (method.isTestMethod()) {
            String tcName = testResult.getMethod().getMethodName();

            if (TestRunner.knownIssues.contains(tcName)) {
                logger.info("Skipping Known Issue TestCase: " + tcName);
                throw new SkipException("Known Issue - Skipped: " + tcName);
            }
        }
    }

    @Override
    public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
    }
}


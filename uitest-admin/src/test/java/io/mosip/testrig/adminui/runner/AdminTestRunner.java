package io.mosip.testrig.adminui.runner;

import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

// Glue and plugin are configured solely via junit-platform.properties — a @ConfigurationParameter
// here would take precedence over that file for the same key and silently override it.
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
public class AdminTestRunner {
}

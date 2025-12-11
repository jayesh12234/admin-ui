package io.mosip.testrig.adminui.utility;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import io.mosip.testrig.adminui.dbaccess.DBManager;
import io.mosip.testrig.adminui.fw.util.AdminTestUtil;
import io.mosip.testrig.adminui.kernel.util.ConfigManager;

public class TestRunner {
	private static final Logger logger = Logger.getLogger(TestRunner.class);
	static TestListenerAdapter tla = new TestListenerAdapter();

	public static String jarUrl = TestRunner.class.getProtectionDomain().getCodeSource().getLocation().getPath();
	public static String uin = "";
	public static String perpetualVid = "";
	public static String onetimeuseVid = "";
	public static String temporaryVid = "";

	static TestNG testNg;
	public static List<String> knownIssues = new ArrayList<>();

	public static void main(String[] args) throws Exception {

		AdminTestUtil.initialize();
		DBManager.clearMasterDbData();
		
		try (BufferedReader br = new BufferedReader(new FileReader(getResourcePath() + "/config/Known_Issues.txt"))) {
			String line;
			while ((line = br.readLine()) != null) {
				if (!line.trim().isEmpty()) {
					knownIssues.add(line.trim());
				}
			}
			logger.info("Known Issues Loaded: " + knownIssues);
		} catch (Exception e) {
			logger.warn("Known_Issues.txt not found or unreadable: " + e.getMessage());
		}

		startTestRunner();

	}

	public static void startTestRunner() throws Exception {
		File homeDir = null;
		TestNG runner = new TestNG();
		if (!ConfigManager.gettestcases().equals("")) {
			XmlSuite suite = new XmlSuite();
			suite.setName("ADMIN-UI-AUTOMATION");
			suite.addListener("io.mosip.testrig.adminui.utility.EmailableReport");
			suite.addListener("io.mosip.testrig.adminui.utility.AdminTestListener");
			XmlClass blocklistedwordsCRUD = new XmlClass("io.mosip.testrig.adminui.testcase.BlockListTest");
			XmlClass bulkUploadCRUD = new XmlClass("io.mosip.testrig.adminui.testcase.BulkUploadTest");
			XmlClass centerCRUD = new XmlClass("io.mosip.testrig.adminui.testcase.CenterTest");
			XmlClass centerTypeCRUD = new XmlClass("io.mosip.testrig.adminui.testcase.CenterTypeTest");
			XmlClass deviceSpecCRUD = new XmlClass("io.mosip.testrig.adminui.testcase.DeviceSpecificationTest");
			XmlClass deviceCRUD = new XmlClass("io.mosip.testrig.adminui.testcase.DeviceTest");
			XmlClass deviceTypesCRUD = new XmlClass("io.mosip.testrig.adminui.testcase.DeviceTypesTest");
			XmlClass documentCategoriesCRUD = new XmlClass("io.mosip.testrig.adminui.testcase.DocumentCategoriesTest");
			XmlClass documentTypesCRUD = new XmlClass("io.mosip.testrig.adminui.testcase.DocumentTypes");
			XmlClass dynamicFieldCRUD = new XmlClass("io.mosip.testrig.adminui.testcase.DynamicFieldTest");
			XmlClass holidaysCRUD = new XmlClass("io.mosip.testrig.adminui.testcase.HolidaysTest");
			XmlClass machineSpecCRUD = new XmlClass("io.mosip.testrig.adminui.testcase.MachineSpecificationTest");
			XmlClass machineCRUD = new XmlClass("io.mosip.testrig.adminui.testcase.MachineTest");
			XmlClass machineTypesCRUD = new XmlClass("io.mosip.testrig.adminui.testcase.MachineTypesTest");
			XmlClass templateCRUD = new XmlClass("io.mosip.testrig.adminui.testcase.TemplateTest");

			List<XmlClass> classes = new ArrayList<>();
			String[] Scenarioname = ConfigManager.gettestcases().split(",");
			for (String test : Scenarioname) {
				if (test.equals("blocklistedwordsCRUD"))
					classes.add(blocklistedwordsCRUD);

				if (test.equals("bulkUploadCRUD"))
					classes.add(bulkUploadCRUD);

				if (test.equals("centerCRUD"))
					classes.add(centerCRUD);

				if (test.equals("centerTypeCRUD"))
					classes.add(centerTypeCRUD);

				if (test.equals("deviceSpecCRUD"))
					classes.add(deviceSpecCRUD);

				if (test.equals("deviceCRUD"))
					classes.add(deviceCRUD);

				if (test.equals("deviceTypesCRUD"))
					classes.add(deviceTypesCRUD);

				if (test.equals("documentCategoriesCRUD"))
					classes.add(documentCategoriesCRUD);

				if (test.equals("documentTypesCRUD"))
					classes.add(documentTypesCRUD);

				if (test.equals("dynamicFieldCRUD"))
					classes.add(dynamicFieldCRUD);

				if (test.equals("holidaysCRUD"))
					classes.add(holidaysCRUD);

				if (test.equals("machineSpecCRUD"))
					classes.add(machineSpecCRUD);

				if (test.equals("machineCRUD"))
					classes.add(machineCRUD);

				if (test.equals("machineTypesCRUD"))
					classes.add(machineTypesCRUD);

				if (test.equals("templateCRUD"))
					classes.add(templateCRUD);

			}

			XmlTest test = new XmlTest(suite);
			test.setName("MyTest");
			test.setXmlClasses(classes);

			List<XmlSuite> suites = new ArrayList<>();
			suites.add(suite);

			runner.setXmlSuites(suites);

		} else {
			List<String> suitefiles = new ArrayList<String>();
			String os = System.getProperty("os.name");
			if (checkRunType().contains("IDE") || os.toLowerCase().contains("windows") == true) {
				homeDir = new File(getResourcePath() + "/testngFile");

			} else {
				homeDir = new File(getResourcePath() + "/testngFile");

			}

			for (File file : homeDir.listFiles()) {
				if (file.getName().toLowerCase() != null) {
					suitefiles.add(file.getAbsolutePath());
				}
			}
			runner.addListener(new KnownIssueListener());
			runner.setTestSuites(suitefiles);

		}
		// Set other properties and run TestNG
		System.getProperties().setProperty("testng.outpur.dir", "testng-report");
		runner.setOutputDirectory("testng-report");
		System.getProperties().setProperty("emailable.report2.name",
				"ADMINUI-" + BaseTestCaseFunc.environment + "-run-" + System.currentTimeMillis() + "-report.html");
		runner.run();
		DBManager.clearMasterDbData();
		System.exit(0);
	}

	public static String getGlobalResourcePath() {
		if (checkRunType().equalsIgnoreCase("JAR")) {
			return new File(jarUrl).getParentFile().getAbsolutePath().toString();
		} else if (checkRunType().equalsIgnoreCase("IDE")) {
			String path = new File(TestRunner.class.getClassLoader().getResource("").getPath()).getAbsolutePath()
					.toString();
			if (path.contains("test-classes"))
				path = path.replace("test-classes", "classes");
			return path;
		}
		return "Global Resource File Path Not Found";
	}

	public static String getResourcePath() {
		if (checkRunType().equalsIgnoreCase("JAR")) {
			return new File(jarUrl).getParentFile().getAbsolutePath().toString() + "/resources/";
		} else if (checkRunType().equalsIgnoreCase("IDE")) {
			String path = System.getProperty("user.dir") + System.getProperty("path.config");

			// String path = new
			// File(TestRunner.class.getClassLoader().getResource("").getPath()).getAbsolutePath()
			// .toString();
			if (path.contains("test-classes"))
				path = path.replace("test-classes", "classes");
			return path;
		}
		return "Global Resource File Path Not Found";
	}

	public static String checkRunType() {
		if (TestRunner.class.getResource("TestRunner.class").getPath().toString().contains(".jar"))
			return "JAR";
		else
			return "IDE";
	}

	public static String GetKernalFilename() {
		String path = System.getProperty("env.user");
		String kernalpath = null;
		if (System.getProperty("env.user") == null || System.getProperty("env.user").equals("")) {
			kernalpath = "Kernel.properties";

		} else {
			kernalpath = "Kernel_" + path + ".properties";
		}
		return kernalpath;
	}

}

package io.mosip.testrig.adminui.utility;

import java.io.File;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.Reporter;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.ITestResult;

import com.aventstack.extentreports.ExtentTest;

import io.github.bonigarcia.wdm.WebDriverManager;
import io.mosip.testrig.adminui.kernel.util.ConfigManager;
import io.mosip.testrig.adminui.kernel.util.KeycloakUserManager;

public class BaseClass {

	private static final Logger logger = Logger.getLogger(TestRunner.class);

	// Make driver thread-safe
	protected static ThreadLocal<WebDriver> driverThread = new ThreadLocal<>();
	protected Map<String, Object> vars;
	protected JavascriptExecutor js;
	protected String langcode;
	protected String envPath = ConfigManager.getiam_adminportal_path();
	protected String env = ConfigManager.getiam_apienvuser();
	public static String userid = KeycloakUserManager.moduleSpecificUser;
	protected String[] allpassword = ConfigManager.getIAMUsersPassword().split(",");
	protected String password = allpassword[0];
	protected String data = Commons.appendDate;

	// Get driver for current thread
	protected static WebDriver driver() {
		return driverThread.get();
	}

	@BeforeMethod
	public void setUp(Method testMethod) throws Exception {
		AdminExtentReportManager.initReport();

		Reporter.log("BaseClass setup started", true);
		logger.info("Start set up");

		if (System.getProperty("os.name").equalsIgnoreCase("Linux") && ConfigManager.getdocker().equals("yes")) {
			logger.info("Docker start");
			String configFilePath = "/usr/bin/chromedriver";
			System.setProperty("webdriver.chrome.driver", configFilePath);
		} else {
			WebDriverManager.chromedriver().setup();
			logger.info("window chrome driver start");
		}

		ChromeOptions options = new ChromeOptions();
		String headless = ConfigManager.getheadless();
		if (headless.equalsIgnoreCase("yes")) {
			logger.info("Running in headless mode");
			options.addArguments("--headless", "--disable-gpu", "--no-sandbox", "--window-size=1920x1080",
					"--disable-dev-shm-usage");
		}

		WebDriver threadDriver = new ChromeDriver(options);
		driverThread.set(threadDriver); // Set driver for this thread

		js = (JavascriptExecutor) driver();
		vars = new HashMap<String, Object>();
		driver().get(envPath);
		logger.info("Launch URL: " + envPath);
		driver().manage().window().maximize();
		Commons.wait(500);
		driver().manage().timeouts().implicitlyWait(Duration.ofSeconds(20));

		String language1 = ConfigManager.getloginlang();
		String loginlang;

		if (language1.equalsIgnoreCase("sin")) {
			loginlang = "English";
		} else {
			loginlang = JsonUtil.JsonObjArrayListParsing2(ConfigManager.getlangcode());
		}

		Commons.click(driver(), By.xpath("//*[@id='kc-locale-dropdown']"), "Clicked on language dropdown");

		String xpath = "//li/a[" + "normalize-space(text())='" + loginlang + "' "
				+ "or contains(translate(normalize-space(text()), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '"
				+ language1.toLowerCase() + "') " + "]";

		Commons.click(driver(), By.xpath(xpath), "Selected language: " + loginlang);

		Commons.enter(driver(), By.id("username"), userid, "Entered username");
		Commons.enterSensitive(driver(), By.id("password"), password, "Entered password");
		Commons.click(driver(), By.xpath("//input[@name='login']"), "Clicked login button");

		if (AdminExtentReportManager.getTest() == null) {
			String className = this.getClass().getSimpleName();
			String methodName = testMethod.getName();

			String lang = "unknown";
			try {
				lang = ConfigManager.getloginlang();
			} catch (Exception ignored) {
			}

			String cleanName = className + " | " + methodName + " | Language: " + lang;

			AdminExtentReportManager.createTest(cleanName);
			AdminExtentReportManager.logStep("Setup complete for " + cleanName);
		}
	}

	@AfterMethod(alwaysRun = true)
	public void tearDown(ITestResult result) {
		try {
			if (result != null && result.getStatus() == ITestResult.FAILURE) {
				try {
					WebDriver drv = driver();
					if (drv != null) {
						byte[] screenshot = ((org.openqa.selenium.TakesScreenshot) drv)
								.getScreenshotAs(org.openqa.selenium.OutputType.BYTES);
						String base64 = java.util.Base64.getEncoder().encodeToString(screenshot);

						AdminExtentReportManager.attachScreenshotFromBase64(base64,
								"Failure Screenshot - " + result.getName());
					}
				} catch (Exception e) {
					logger.warn("Unable to capture screenshot in tearDown: " + e.getMessage());
				}
			}
		} finally {
			if (driverThread.get() != null) {
				driverThread.get().quit();
				driverThread.remove();
			}
			AdminExtentReportManager.removeTest();
		}
	}

	@DataProvider(name = "data-provider")
	public Object[] dpMethod() {
		String listFilename[] = readFolderJsonList();
		for (int count = 0; count < listFilename.length; count++) {
			listFilename[count] = listFilename[count].replace(".csv", "");
		}
		return listFilename;
	}

	public static String[] readFolderJsonList() {
		String contents[] = null;
		try {
			String langcode = ConfigManager.getloginlang();
			File directoryPath = new File(TestRunner.getResourcePath() + "//BulkUploadFiles//" + langcode + "//");
			if (directoryPath.exists()) {
				contents = directoryPath.list();
				for (int i = 0; i < contents.length; i++) {
					logger.info(contents[i]);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return contents;
	}

	public static String convertDigits(String number, String locale) {
		if (locale.equalsIgnoreCase("ara")) {
			String[] arabicDigits = { "٠", "١", "٢", "٣", "٤", "٥", "٦", "٧", "٨", "٩" };
			StringBuilder result = new StringBuilder();
			for (char c : number.toCharArray()) {
				if (Character.isDigit(c)) {
					result.append(arabicDigits[c - '0']);
				} else {
					result.append(c);
				}
			}
			return result.toString();
		} else {
			return number;
		}
	}
}
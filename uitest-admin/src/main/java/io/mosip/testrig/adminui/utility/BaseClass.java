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
import io.mosip.testrig.adminui.kernel.util.KernelAuthentication;
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

		if (isLocalPortal(envPath)) {
			// Local UI (4200) calls local admin-service (8086). The auth adapter does not
			// allow every header the UI sends, so Chrome blocks the call and the UI shows
			// "No internet". Disable web security only for this local run.
			// Each thread needs its own profile. A shared --user-data-dir makes Chrome
			// fail to open localhost when tests run in parallel.
			String profileDir = System.getProperty("java.io.tmpdir") + "admin-ui-chrome-"
					+ Thread.currentThread().getId();
			options.addArguments("--disable-web-security", "--user-data-dir=" + profileDir,
					"--proxy-bypass-list=<-loopback>");
			Map<String, Object> prefs = new HashMap<String, Object>();
			prefs.put("profile.block_third_party_cookies", Boolean.FALSE);
			prefs.put("profile.cookie_controls_mode", Integer.valueOf(0));
			options.setExperimentalOption("prefs", prefs);
		}

		WebDriver threadDriver = new ChromeDriver(options);
		driverThread.set(threadDriver); // Set driver for this thread

		js = (JavascriptExecutor) driver();
		vars = new HashMap<String, Object>();
		driver().manage().window().maximize();
		driver().manage().timeouts().implicitlyWait(Duration.ofSeconds(20));

		if (isLocalPortal(envPath)) {
			injectAdminToken(threadDriver);
			logger.info("Localhost run: opened portal with admin Authorization cookie. URL: " + envPath);
			driver().get(envPath);
			Commons.waitForPageLoad(driver());
			return;
		}

		driver().get(envPath);
		logger.info("Launch URL: " + envPath);
		Commons.waitForPageLoad(driver());

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

	}

	/**
	 * localhost is not in the auth-manager allowed redirect list, so the Keycloak
	 * login page returns KER-ACP-009. Store the internal-auth token as a real
	 * cookie on the API host. Forcing a Cookie header instead replaces every other
	 * cookie, so later POST calls fail and the UI opens a Technical Error dialog
	 * for each one.
	 */
	private void injectAdminToken(WebDriver threadDriver) {
		if (BaseTestCaseFunc.ApplnURI == null || BaseTestCaseFunc.ApplnURI.isEmpty()) {
			BaseTestCaseFunc.ApplnURI = ConfigManager.getiam_apiinternalendpoint();
		}
		String token = new KernelAuthentication().getAuthForAdmin();
		if (token == null || token.isEmpty()) {
			throw new IllegalStateException("Admin token was empty. Check Kernel.properties client id and secret.");
		}
		String apiUrl = cookieHost();
		boolean https = apiUrl.startsWith("https://");
		Map<String, Object> cookie = new HashMap<String, Object>();
		cookie.put("name", BaseTestCaseFunc.COOKIENAME);
		cookie.put("value", token);
		cookie.put("url", apiUrl);
		cookie.put("path", "/");
		cookie.put("secure", Boolean.valueOf(https));
		cookie.put("sameSite", https ? "None" : "Lax");
		ChromeDriver chromeDriver = (ChromeDriver) threadDriver;
		chromeDriver.executeCdpCommand("Network.enable", new HashMap<String, Object>());
		chromeDriver.executeCdpCommand("Network.setCookie", cookie);
		logger.info("Stored Authorization cookie for " + apiUrl);
	}

	/**
	 * The UI calls masterdatabaseurl (local admin-service). A cookie stored on the
	 * dev2 API host is not sent with those requests.
	 */
	private String cookieHost() {
		String master = ConfigManager.getMasterdatabaseurl();
		String host = (master != null && !master.isEmpty()) ? master : BaseTestCaseFunc.ApplnURI;
		if (!host.endsWith("/")) {
			host = host + "/";
		}
		return host;
	}

	private boolean isLocalPortal(String portalUrl) {
		if (portalUrl == null) {
			return false;
		}
		String url = portalUrl.toLowerCase();
		return url.contains("localhost") || url.contains("127.0.0.1");
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
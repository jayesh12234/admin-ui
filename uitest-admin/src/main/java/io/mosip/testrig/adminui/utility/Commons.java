package io.mosip.testrig.adminui.utility;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import org.testng.Assert;
import org.testng.Reporter;

import io.mosip.testrig.adminui.kernel.util.ConfigManager;

public class Commons extends BaseClass {

	private static final Logger logger = Logger.getLogger(Commons.class);

	public static String appendDate = getPreAppend() + getDateTime();

	public static String getDateTime() {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
		LocalDateTime now = LocalDateTime.now();
		return dtf.format(now);
	}

	private static WebDriverWait getWait(WebDriver driver) {
		return new WebDriverWait(driver, Duration.ofSeconds(40));
	}

	public static void waitForElementClickable(WebDriver driver, By by) {
		getWait(driver).until(ExpectedConditions.elementToBeClickable(by));
	}

	public static void waitForElementVisible(WebDriver driver, By by) {
		getWait(driver).until(ExpectedConditions.visibilityOfElementLocated(by));
		if (isLocalRun(driver)) {
			pause(2000);
		}
	}

	private static boolean isLocalRun(WebDriver driver) {
		try {
			String url = driver.getCurrentUrl();
			return url != null && (url.contains("localhost") || url.contains("127.0.0.1"));
		} catch (Exception e) {
			return false;
		}
	}

	public static void waitForPageLoad(WebDriver driver) {
		getWait(driver).until(webDriver -> ((JavascriptExecutor) webDriver).executeScript("return document.readyState")
				.equals("complete"));
	}

	private static void logStep(String description, By by) {
		AdminExtentReportManager.logStepWithLocator(description, getLocator(by));
	}

	private static String getLocator(By by) {
		if (by == null)
			return "";
		String s = by.toString();
		if (s.contains(": ")) {
			String[] p = s.split(": ", 2);
			return "By." + p[0].replace("By.", "") + "(\"" + p[1] + "\")";
		}
		return s;
	}

	public static void filter(WebDriver driver, By by, String data, String description) throws IOException {
		AdminExtentReportManager.logStep(description + " | Filter Value → " + data);
		try {
			returnToList(driver);
			logger.info("Inside Filter " + by + " with data: " + data);
			Commons.click(driver, By.id("Filter"), "Click Filter");
			waitForElementVisible(driver, by);
			Commons.enter(driver, by, data, "Enter filter text");
			waitForElementClickable(driver, By.id("applyTxt"));
			Commons.click(driver, By.id("applyTxt"), "Apply filter");
		} catch (Exception e) {
			Reporter.log("<p><img src='data:image/png;base64," + Screenshot.ClickScreenshot(driver)
					+ "' width='900' height='450'/></p>");
			logger.info(e.getMessage());
			JavascriptExecutor executor = (JavascriptExecutor) driver;
			executor.executeScript("arguments[0].click();", driver.findElement(by));
		}
	}

	public static void filter(WebDriver driver, By by, String data) throws IOException {
		filter(driver, by, data, "Filter by text");
	}

	public static void filterCenter(WebDriver driver, By by, String data, String description) throws IOException {
		AdminExtentReportManager.logStep(description + " | Filter Value → " + data);
		try {
			returnToList(driver);
			Commons.click(driver, By.id("Filter"), "Click Filter");
			Commons.dropdowncenter(driver, by, data);
			Commons.click(driver, By.id("applyTxt"), "Apply Filter");
		} catch (Exception e) {
			Reporter.log("<p><img src='data:image/png;base64," + Screenshot.ClickScreenshot(driver)
					+ "' width='900' height='450'/></p>");
			logger.info(e.getMessage());
			JavascriptExecutor executor = (JavascriptExecutor) driver;
			executor.executeScript("arguments[0].click();", driver.findElement(by));
		}
		new WebDriverWait(driver, Duration.ofSeconds(40))
				.until(ExpectedConditions.presenceOfElementLocated(By.id("ellipsis-button0")));
	}

	public static void filterCenter(WebDriver driver, By by, String data) throws IOException {
		filterCenter(driver, by, data, "Filter Center by value");
	}

	/**
	 * Opens the first-row actions menu and leaves it open. The row is often still
	 * refreshing when the button first appears, and a second click toggles the menu shut.
	 */
	public static void clickEllipsis(WebDriver driver) throws IOException {
		openRowMenu(driver);
	}

	public static void openRowAction(WebDriver driver, String actionId) throws IOException {
		By action = By.id(actionId);
		TimeoutException last = null;
		for (int attempt = 0; attempt < 3; attempt++) {
			try {
				openRowMenu(driver);
				new WebDriverWait(driver, Duration.ofSeconds(5))
						.until(ExpectedConditions.elementToBeClickable(action));
				driver.findElement(action).click();
				logger.info("Clicked row action " + actionId);
				return;
			} catch (TimeoutException e) {
				last = e;
				logger.info("Row menu closed before " + actionId + ", retry " + (attempt + 1));
			}
		}
		throw last;
	}

	private static void openRowMenu(WebDriver driver) throws IOException {
		By ellipsis = By.id("ellipsis-button0");
		By menu = By.cssSelector(".cdk-overlay-container .mat-menu-panel");
		logStep("Clicked on Ellipsis button for the first record", ellipsis);
		logger.info("Clicking " + ellipsis);
		new WebDriverWait(driver, Duration.ofSeconds(40)).until(d -> ellipsisStable(d, ellipsis));
		WebElement button = driver.findElement(ellipsis);
		((JavascriptExecutor) driver).executeScript(
				"arguments[0].scrollIntoView({block:'center', inline:'nearest', behavior:'instant'});", button);
		if (isLocalRun(driver)) {
			pause(2000);
		}
		button = driver.findElement(ellipsis);
		((JavascriptExecutor) driver).executeScript("arguments[0].click();", button);
		new WebDriverWait(driver, Duration.ofSeconds(8))
				.until(ExpectedConditions.visibilityOfElementLocated(menu));
	}

	private static void returnToList(WebDriver driver) {
		By ellipsis = By.id("ellipsis-button0");
		if (!driver.findElements(By.id("applyTxt")).isEmpty()) {
			List<WebElement> dialogCancel = driver.findElements(By.id("cancel"));
			if (!dialogCancel.isEmpty()) {
				((JavascriptExecutor) driver).executeScript("arguments[0].click();",
						dialogCancel.get(dialogCancel.size() - 1));
				pause(500);
			}
		}
		for (int i = 0; i < 4 && driver.findElements(ellipsis).isEmpty(); i++) {
			List<WebElement> cancels = driver.findElements(By.id("cancel"));
			if (cancels.isEmpty()) {
				break;
			}
			((JavascriptExecutor) driver).executeScript("arguments[0].click();", cancels.get(0));
			pause(1500);
		}
	}

	private static void pause(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	private static boolean ellipsisStable(WebDriver driver, By ellipsis) {
		try {
			WebElement button = driver.findElement(ellipsis);
			return button.isEnabled();
		} catch (StaleElementReferenceException | NoSuchElementException e) {
			return false;
		}
	}

	public static void click(WebDriver driver, By by, String description) throws IOException {
		logStep(description, by);
		logger.info("Clicking " + by);
		try {
			waitForElementClickable(driver, by);
			driver.findElement(by).click();
		} catch (Exception e) {
			Reporter.log("<p><img src='data:image/png;base64," + Screenshot.ClickScreenshot(driver)
					+ "' width='900' height='450'/></p>");
			logger.error(e.getMessage());
			JavascriptExecutor executor = (JavascriptExecutor) driver;
			executor.executeScript("arguments[0].click();", driver.findElement(by));
		}
	}

	public static void click(WebDriver driver, By by) throws IOException {
		click(driver, by, "Clicked element");
	}

	private static void clickSave(WebDriver driver, By save) throws IOException {
		waitForElementVisible(driver, save);
		WebElement button = driver.findElement(save);
		((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", button);
		pause(2000);
		Commons.click(driver, save, "Clicked on Create button to save changes");
	}

	/**
	 * Types into the field and clicks save only after the full value is present and has stayed there.
	 */
	private static void enterThenSave(WebDriver driver, By field, String data) throws IOException {
		Commons.enter(driver, field, data, "Entered new data: " + data);
		new WebDriverWait(driver, Duration.ofSeconds(20)).until(d -> data.equals(readFieldValue(d, field)));
		logger.info("Field value entered, waiting 5 seconds before save");
		pause(5000);
		if (!data.equals(readFieldValue(driver, field))) {
			throw new TimeoutException("Field value was not stable before save: " + field);
		}
		clickSave(driver, By.id("createButton"));
	}

	private static String readFieldValue(WebDriver driver, By field) {
		try {
			WebElement element = driver.findElement(field);
			Object value = ((JavascriptExecutor) driver).executeScript("return arguments[0].value;", element);
			if (value == null || value.toString().isEmpty()) {
				String attribute = element.getAttribute("value");
				return attribute == null ? "" : attribute;
			}
			return value.toString();
		} catch (StaleElementReferenceException | NoSuchElementException e) {
			return "";
		}
	}

	public static void enter(WebDriver driver, By by, String value, String description) throws IOException {
		logStep(description + " | Value → " + value, by);
		logger.info("Entering " + by + value);
		if ("fileInput".equals(idOf(by))) {
			driver.findElement(by).sendKeys(value);
			return;
		}
		try {
			waitForElementVisible(driver, by);
			WebElement element = driver.findElement(by);
			element.clear();
			element.sendKeys(value);
			new WebDriverWait(driver, Duration.ofSeconds(15)).until(d -> fieldContains(d, by, value));
		} catch (StaleElementReferenceException sere) {
			Reporter.log("<p><img src='data:image/png;base64," + Screenshot.ClickScreenshot(driver)
					+ "' width='900' height='450'/></p>");
			driver.findElement(by).sendKeys(value);
		} catch (TimeoutException toe) {
			logger.info("Element " + by + " was not ready, waiting and retrying entry");
			pause(2000);
			waitForElementVisible(driver, by);
			WebElement element = driver.findElement(by);
			element.clear();
			element.sendKeys(value);
		} catch (Exception e) {
			Reporter.log("<p><img src='data:image/png;base64," + Screenshot.ClickScreenshot(driver)
					+ "' width='900' height='450'/></p>");
			JavascriptExecutor executor = (JavascriptExecutor) driver;
			executor.executeScript("arguments[0].click();", driver.findElement(by));
		}
	}

	public static void enter(WebDriver driver, By by, String value) throws IOException {
		enter(driver, by, value, "Entered text");
	}

	private static String idOf(By by) {
		String locator = by == null ? "" : by.toString();
		int split = locator.lastIndexOf(": ");
		return split < 0 ? locator : locator.substring(split + 2);
	}

	private static boolean fieldContains(WebDriver driver, By by, String value) {
		return value.equals(readFieldValue(driver, by));
	}

	public static void enterSensitive(WebDriver driver, By by, String value, String description) throws IOException {
		logStep(description + " | Value → ****", by);
		logger.info("Entering sensitive value into element " + by);

		try {
			driver.findElement(by).clear();
			driver.findElement(by).sendKeys(value);
		} catch (StaleElementReferenceException sere) {
			Reporter.log("<p><img src='data:image/png;base64," + Screenshot.ClickScreenshot(driver)
					+ "' width='900' height='450'/></p>");
			driver.findElement(by).sendKeys(value);
		} catch (TimeoutException toe) {
			Reporter.log("<p><img src='data:image/png;base64," + Screenshot.ClickScreenshot(driver)
					+ "' width='900' height='450'/></p>");
			driver.findElement(by).sendKeys(value);
			logger.info("Sensitive element " + by.toString() + " was not clickable after timeout");
		} catch (Exception e) {
			Reporter.log("<p><img src='data:image/png;base64," + Screenshot.ClickScreenshot(driver)
					+ "' width='900' height='450'/></p>");
			JavascriptExecutor executor = (JavascriptExecutor) driver;
			executor.executeScript("arguments[0].click();", driver.findElement(by));
		}
	}

	public static void dropdown(WebDriver driver, By by, String description) throws IOException {
		logStep(description + " | Selected index 0", by);
		logger.info("Selecting DropDown Index Zero Value " + by);
		try {
			waitForElementClickable(driver, by);
			click(driver, by, "Clicked dropdown to expand options");

			String att = driver.findElement(by).getAttribute("aria-owns");
			String[] list = att.split(" ");
			waitForElementClickable(driver, By.id(list[0]));
			click(driver, By.id(list[0]), "Clicked on first element from the list");

		} catch (Exception e) {
			Reporter.log("<p><img src='data:image/png;base64," + Screenshot.ClickScreenshot(driver)
					+ "' width='900' height='450'/></p>");
			JavascriptExecutor executor = (JavascriptExecutor) driver;
			executor.executeScript("arguments[0].click();", driver.findElement(by));
		}
	}

	public static void dropdown(WebDriver driver, By by, String value, String description) throws IOException {
		logStep(description + " | Select dropdown using value: " + value, by);
		logger.info("Selecting DropDown By Value " + by + value);

		try {
			waitForElementClickable(driver, by);
			click(driver, by, "Clicked dropdown to expand options");
			waitForElementClickable(driver, By.xpath("//span[contains(text(),'" + value + "')]"));
			String val = "'" + value + "'";
			click(driver, By.xpath("//span[contains(text()," + val + ")]"),
					"Selected value: " + value + " from the list");

		} catch (Exception e) {
			Reporter.log("<p><img src='data:image/png;base64," + Screenshot.ClickScreenshot(driver)
					+ "' width='900' height='450'/></p>");
			JavascriptExecutor executor = (JavascriptExecutor) driver;
			executor.executeScript("arguments[0].click();", driver.findElement(by));
		}
	}

	public static void dropdowncenter(WebDriver driver, By by, String value, String description) throws IOException {
		logStep(description + " | Select dropdown center using value: " + value, by);
		logger.info("Selecting DropDown By Value " + by + value);
		waitForElementVisible(driver, by);
		WebElement input = driver.findElement(by);
		input.click();
		input.clear();
		input.sendKeys(value);
		By option = By.xpath("//mat-option[contains(normalize-space(.),'" + value + "')]");
		try {
			new WebDriverWait(driver, Duration.ofSeconds(20))
					.until(ExpectedConditions.elementToBeClickable(option));
			driver.findElement(option).click();
		} catch (TimeoutException e) {
			logger.info("Autocomplete option not shown for " + value + ", continuing with typed value");
		}
	}

	public static void dropdowncenter(WebDriver driver, By by, String value) throws IOException {
		dropdowncenter(driver, by, value, "Select dropdown center value");
	}

	public static void dropdown(WebDriver driver, By by, By value, String description) throws IOException {
		logStep(description, by);
		logger.info("Selecting DropDown By Value " + by + value);
		try {
			waitForElementClickable(driver, by);
			click(driver, by, "Clicked on dropdown field to expand options");
			waitForElementClickable(driver, value);
			click(driver, value, "Selected value from the dropdown");
		} catch (Exception e) {
			Reporter.log("<p><img src='data:image/png;base64," + Screenshot.ClickScreenshot(driver)
					+ "' width='900' height='450'/></p>");
			JavascriptExecutor executor = (JavascriptExecutor) driver;
			executor.executeScript("arguments[0].click();", driver.findElement(by));
		}
	}

	public static String getTestData() {
		return JsonUtil.readJsonFileText("TestData.json");
	}

	public static void deactivate(WebDriver driver, String description) throws IOException {
		AdminExtentReportManager.logStep(description);
		Commons.clickEllipsis(driver);
		waitForElementClickable(driver, By.id("Deactivate0"));
		Commons.click(driver, By.id("Deactivate0"), "Clicked on Deactivate for the first record");
		if (isElementDisplayed(By.id("confirmpopup"))) {
			Commons.click(driver, By.id("confirmpopup"), "Clicked on Confirm popup if displayed");
		}
		Commons.click(driver, By.id("confirmmessagepopup"), "Clicked on Confirm Message popup after deactivation");
		logger.info("Click deactivate and Confirm");
	}

	public static void deactivate(WebDriver driver) throws IOException {
		deactivate(driver, "Deactivate Record");
	}

	public static void activate(WebDriver driver, String description) throws IOException {
		AdminExtentReportManager.logStep(description);
		Commons.clickEllipsis(driver);
		waitForElementClickable(driver, By.id("Activate0"));
		Commons.click(driver, By.id("Activate0"), "Clicked on Activate for the first record");
		if (isElementDisplayed(By.id("confirmpopup"))) {
			Commons.click(driver, By.id("confirmpopup"), "Clicked on Confirm popup if displayed");
		}
		Commons.click(driver, By.id("confirmmessagepopup"), "Clicked on Confirm Message popup after activation");
		logger.info("Click activate and Confirm");
	}

	public static void activate(WebDriver driver) throws IOException {
		activate(driver, "Activate Record");
	}

	public static void edit(WebDriver driver, String data, By by, String description) throws IOException {
		logStep(description + " | New Value → " + data, by);

		try {
			openRowAction(driver, "Edit0");
			waitForElementVisible(driver, by);
			Assert.assertNotEquals(data, driver.findElement(by).getAttribute("value"),
					"Verified existing value is different from new data");
			driver.findElement(by).clear();
			enterThenSave(driver, by, data);
			if (isElementDisplayed(By.id("confirmpopup")))
				Commons.click(driver, By.id("confirmpopup"), "Clicked on Confirm popup if displayed");
			Commons.click(driver, By.id("confirmmessagepopup"), "Clicked on Confirm Message popup after editing");

			logger.info("Click Edit and Confirm " + by + data);
		} catch (Exception e) {
			logger.info(e.getMessage());
			JavascriptExecutor executor = (JavascriptExecutor) driver;
			executor.executeScript("arguments[0].click();", driver.findElement(by));
		}
	}

	public static void edit(WebDriver driver, String data, By by) throws IOException {
		edit(driver, data, by, "Edit the record and confirm");
	}

	public static void editRes(WebDriver driver, String data, By by, String description) throws IOException {
		logStep(description + " | New Value → " + data, by);

		try {
			openRowAction(driver, "Edit0");
			waitForElementVisible(driver, by);
			Assert.assertNotEquals(data, driver.findElement(by).getAttribute("value"),
					"Verified existing value is different from new data");
			driver.findElement(by).clear();
			enterThenSave(driver, by, data);
			if (isElementDisplayed(By.id("confirmpopup")))
				Commons.click(driver, By.id("confirmpopup"), "Clicked on Confirm popup if displayed");
			Commons.click(driver, By.id("confirmmessagepopup"), "Clicked on Confirm Message popup after editing");
			logger.info("Click Edit and Confirm " + by + data);
		} catch (Exception e) {
			Reporter.log("<p><img src='data:image/png;base64," + Screenshot.ClickScreenshot(driver)
					+ "' width='900' height='450'/></p>");
			logger.info(e.getMessage());

			JavascriptExecutor executor = (JavascriptExecutor) driver;
			executor.executeScript("arguments[0].click();", driver.findElement(by));
		}
	}

	public static void editRes(WebDriver driver, String data, By by) throws IOException {
		editRes(driver, data, by, "Edit Resource");
	}

	public static void editCenter(WebDriver driver, String data, By by, String description) throws IOException {
		logStep(description + " | New Value → " + data, by);

		try {
			openRowAction(driver, "Edit0");
			waitForPageLoad(driver);
			Assert.assertNotEquals(data, driver.findElement(by).getText(),
					"Verified existing value is different from new data");
			driver.findElement(by).clear();
			Commons.enter(driver, by, data, "Entered new data: " + data);
			waitForElementClickable(driver, By.xpath("(//*[@id='createButton'])[1]"));
			Commons.click(driver, By.xpath("(//*[@id='createButton'])[1]"), "Clicked on Create button to save changes");
			if (isElementDisplayed(By.id("confirmpopup"))) {
				Commons.click(driver, By.id("confirmpopup"), "Clicked on Confirm popup if displayed");
			}
			Commons.click(driver, By.id("confirmmessagepopup"), "Clicked on Confirm Message popup after editing");
			waitForElementClickable(driver, By.xpath("(//*[@id='cancel'])[1]"));
			Commons.click(driver, By.xpath("(//*[@id='cancel'])[1]"), "Clicked on first Cancel button");
			Commons.click(driver, By.xpath("(//*[@id='cancel'])[1]"), "Clicked on second Cancel button");
			logger.info("Click editCenter and Confirm " + by + data);
		} catch (Exception e) {
			Reporter.log("<p><img src='data:image/png;base64," + Screenshot.ClickScreenshot(driver)
					+ "' width='900' height='450'/></p>");
			logger.info(e.getMessage());

			JavascriptExecutor executor = (JavascriptExecutor) driver;
			executor.executeScript("arguments[0].click();", driver.findElement(by));
		}
	}

	public static void editCenter(WebDriver driver, String data, By by) throws IOException {
		editCenter(driver, data, by, "Edit Center");
	}

	public static void create(WebDriver driver, String description) throws IOException {
		AdminExtentReportManager.logStep(description);
		confirmSubmit(driver);
		logger.info("Click create");
	}

	public static void create(WebDriver driver) throws IOException {
		create(driver, "Submit Create");
	}

	public static void createRes(WebDriver driver, String description) throws IOException {
		AdminExtentReportManager.logStep(description);
		confirmSubmit(driver);
		logger.info("Click and confirm");
	}

	private static void confirmSubmit(WebDriver driver) throws IOException {
		By create = By.xpath("//button[@id='createButton']");
		By confirm = By.id("confirmpopup");
		By message = By.id("confirmmessagepopup");
		Commons.click(driver, create, "Clicked on Create button");
		clickIfPresent(driver, confirm, 8);
		if (!clickIfPresent(driver, message, 20)) {
			logger.info("Success message not shown, submitting the form again");
			Commons.click(driver, create, "Clicked on Create button again");
			clickIfPresent(driver, confirm, 8);
			waitForElementVisible(driver, message);
			Commons.click(driver, message, "Clicked on Confirm Message popup");
		}
	}

	private static boolean clickIfPresent(WebDriver driver, By by, int seconds) {
		try {
			new WebDriverWait(driver, Duration.ofSeconds(seconds)).until(ExpectedConditions.elementToBeClickable(by));
			driver.findElement(by).click();
			logger.info("Clicked " + by);
			return true;
		} catch (TimeoutException e) {
			return false;
		}
	}

	public static void createRes(WebDriver driver) throws IOException {
		createRes(driver, "Create Resource");
	}

	public static void decommission(WebDriver driver, String description) throws IOException {
		AdminExtentReportManager.logStep(description);
		Commons.clickEllipsis(driver);
		waitForElementClickable(driver, By.id("Decommission0"));
		Commons.click(driver, By.id("Decommission0"), "Clicked on Decommission for the first record");
		if (isElementDisplayed(By.id("confirmpopup")))
			Commons.click(driver, By.id("confirmpopup"), "Clicked on Confirm popup if displayed");
		Commons.click(driver, By.id("confirmmessagepopup"), "Clicked on Confirm Message popup after decommission");
		logger.info("Click decommission and confirm");
	}

	public static void decommission(WebDriver driver) throws IOException {
		decommission(driver, "Decommission Record");
	}

	private static void selectCalendarDay(String dayText) throws IOException {
		By dayCell = By.xpath(
				"//div[contains(@class,'mat-calendar-body-cell-content') and normalize-space(text())='" + dayText
						+ "']");
		if (isLocalRun(driver())) {
			pause(2000);
		}
		new WebDriverWait(driver(), Duration.ofSeconds(15)).until(ExpectedConditions.elementToBeClickable(dayCell));
		driver().findElement(dayCell).click();
		new WebDriverWait(driver(), Duration.ofSeconds(10))
				.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector("mat-calendar")));
		logger.info("Selected day: " + dayText);
	}

	public static String getPreAppend() {
		String preappend = null;
		try {
			preappend = ConfigManager.getpreappend();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return preappend;
	}

	public static void calendar(String date, String description) throws IOException {
		AdminExtentReportManager.logStep(description + " | Date → " + date);
		String a = date.replaceAll("/", "");
		String mon = "";
		if (a.substring(0, 2).equals("10")) {
			mon = a.substring(0, 2);
		} else {
			mon = a.substring(0, 2).replace("0", "");
		}
		String d = "";
		if (a.substring(2, 4).equals("10") || a.substring(2, 4).equals("20") || a.substring(2, 4).equals("30")) {
			d = a.substring(2, 4);
		} else {
			d = a.substring(2, 4).replace("0", "");
		}

		int month = Integer.parseInt(mon);
		int day = Integer.parseInt(d);
		int year = Integer.parseInt(a.substring(4, 8));
		try {
			waitForElementClickable(driver(), By.xpath("//*[@class='mat-datepicker-toggle']//button"));
			Commons.click(driver(), By.xpath("//*[@class='mat-datepicker-toggle']//button"), "Opened the datepicker");

			waitForElementClickable(driver(), By.xpath("//*[@class='mat-calendar-arrow']"));
			Commons.click(driver(), By.xpath("//*[@class='mat-calendar-arrow']"),
					"Navigated to year selection in datepicker");

			waitForElementClickable(driver(), By.xpath("//*[text()='" + year + "']"));
			Commons.click(driver(), By.xpath("//*[text()='" + year + "']"), "Selected year: " + year);

			List<WebElement> cli = driver().findElements(By.xpath("//*[@class='mat-calendar-body-cell-content']"));
			cli.get(month - 1).click();
			selectCalendarDay(String.valueOf(day));
		} catch (Exception e) {
			Reporter.log("<p><img src='data:image/png;base64," + Screenshot.ClickScreenshot(driver())
					+ "' width='900' height='450'/></p>");
			logger.info(e.getMessage());
		}
	}

	public static void calendar(String date, String locale, String description) throws IOException {
		AdminExtentReportManager.logStep(description + " | Date → " + date);
		String a = date.replaceAll("/", "");
		int month = Integer.parseInt(a.substring(0, 2));
		int day = Integer.parseInt(a.substring(2, 4));
		int year = Integer.parseInt(a.substring(4, 8));

		try {
			// Open calendar
			waitForElementClickable(driver(), By.xpath("//*[@class='mat-datepicker-toggle']//button"));
			Commons.click(driver(), By.xpath("//*[@class='mat-datepicker-toggle']//button"), "Opened the datepicker");
			waitForElementClickable(driver(), By.xpath("//*[@class='mat-calendar-arrow']"));

			// Expand year/month selector
			Commons.click(driver(), By.xpath("//*[@class='mat-calendar-arrow']"),
					"Expanded year/month selector in datepicker");

			// Click the desired year
			String yearText = convertDigits(String.valueOf(year), locale);
			Commons.click(driver(), By.xpath("//*[normalize-space(text())='" + yearText + "']"),
					"Selected year: " + yearText);

			// Select month (index based to avoid translation issues)
			List<WebElement> months = driver().findElements(By.xpath("//*[@class='mat-calendar-body-cell-content']"));
			months.get(month - 1).click();
			selectCalendarDay(convertDigits(String.valueOf(day), locale));
		} catch (Exception e) {
			Reporter.log("<p><img src='data:image/png;base64," + Screenshot.ClickScreenshot(driver())
					+ "' width='900' height='450'/></p>");
			logger.info(e.getMessage());
		}
	}

	public static boolean isElementDisplayed(By by) {
		try {
			waitForElementVisible(driver(), by);
			return driver().findElement(by).isDisplayed();
		} catch (Exception e) {
			return false;
		}
	}

}
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
		return new WebDriverWait(driver, Duration.ofSeconds(20));
	}

	public static void waitForElementClickable(WebDriver driver, By by) {
		getWait(driver).until(ExpectedConditions.elementToBeClickable(by));
	}

	public static void waitForElementVisible(WebDriver driver, By by) {
		getWait(driver).until(ExpectedConditions.visibilityOfElementLocated(by));
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
	}

	public static void filterCenter(WebDriver driver, By by, String data) throws IOException {
		filterCenter(driver, by, data, "Filter Center by value");
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

	public static void enter(WebDriver driver, By by, String value, String description) throws IOException {
		logStep(description + " | Value → " + value, by);
		logger.info("Entering " + by + value);
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
			logger.info("Element identified by " + by.toString() + " was not clickable after 20 seconds");
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

		try {
			waitForElementClickable(driver, by);
			click(driver, by, "Clicked dropdown field to view options");
			waitForElementClickable(driver, By.id(value));
			click(driver, By.id(value), "Selected dropdown value with ID: " + value);

		} catch (Exception e) {
			Reporter.log("<p><img src='data:image/png;base64," + Screenshot.ClickScreenshot(driver)
					+ "' width='900' height='450'/></p>");
			JavascriptExecutor executor = (JavascriptExecutor) driver;
			executor.executeScript("arguments[0].click();", driver.findElement(by));
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
		Commons.click(driver, By.id("ellipsis-button0"), "Clicked on Ellipsis button for the first record");
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
		Commons.click(driver, By.id("ellipsis-button0"), "Clicked on Ellipsis button for the first record");
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
			Commons.click(driver, By.id("ellipsis-button0"), "Clicked on Ellipsis button for the first record");
			Commons.click(driver, By.id("Edit0"), "Clicked on Edit for the first record");
			Assert.assertNotEquals(data, driver.findElement(by).getText(),
					"Verified existing value is different from new data");
			driver.findElement(by).clear();
			Commons.enter(driver, by, data, "Entered new data: " + data);
			Commons.click(driver, By.id("createButton"), "Clicked on Create button to save changes");
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
			Commons.click(driver, By.id("ellipsis-button0"), "Clicked on Ellipsis button for the first record");
			Commons.click(driver, By.id("Edit0"), "Clicked on Edit for the first record");
			waitForElementVisible(driver, by);
			Assert.assertNotEquals(data, driver.findElement(by).getText(),
					"Verified existing value is different from new data");
			driver.findElement(by).clear();
			Commons.enter(driver, by, data, "Entered new data: " + data);
			Commons.click(driver, By.id("createButton"), "Clicked on Create button to save changes");
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
			Commons.click(driver, By.id("ellipsis-button0"), "Clicked on Ellipsis button for the first record");
			Commons.click(driver, By.id("Edit0"), "Clicked on Edit for the first record");
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
		Commons.click(driver, By.xpath("//button[@id='createButton']"), "Clicked on Create button");
		if (isElementDisplayed(By.id("confirmpopup")))
			Commons.click(driver, By.id("confirmpopup"), "Clicked on Confirm popup");
		Commons.click(driver, By.id("confirmmessagepopup"), "Clicked on Confirm Message popup");
		logger.info("Click create");
	}

	public static void create(WebDriver driver) throws IOException {
		create(driver, "Submit Create");
	}

	public static void createRes(WebDriver driver, String description) throws IOException {
		AdminExtentReportManager.logStep(description);
		Commons.click(driver, By.xpath("//button[@id='createButton']"), "Clicked on Create button");
		if (isElementDisplayed(By.id("confirmpopup")))
			Commons.click(driver, By.id("confirmpopup"), "Clicked on Confirm popup if displayed");
		Commons.click(driver, By.id("confirmmessagepopup"), "Clicked on Confirm Message popup");
		logger.info("Click and confirm");
	}

	public static void createRes(WebDriver driver) throws IOException {
		createRes(driver, "Create Resource");
	}

	public static void decommission(WebDriver driver, String description) throws IOException {
		AdminExtentReportManager.logStep(description);
		Commons.click(driver, By.id("ellipsis-button0"), "Clicked on Ellipsis button for the first record");
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

			waitForElementClickable(driver(), By.xpath("//*[text()='" + day + "']"));
			Commons.click(driver(), By.xpath("//*[text()='" + day + "']"), "Selected day: " + day);
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

			// Select day (convert if Arabic locale)
			String dayText = convertDigits(String.valueOf(day), locale);
			waitForElementClickable(driver(), By.xpath("//*[normalize-space(text())='" + dayText + "']"));
			Commons.click(driver(), By.xpath("//*[normalize-space(text())='" + dayText + "']"),
					"Selected day: " + dayText);
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
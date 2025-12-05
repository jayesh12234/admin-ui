package io.mosip.testrig.adminui.testcase;

import org.openqa.selenium.By;
import org.testng.Reporter;
import org.testng.annotations.Test;
import io.mosip.testrig.adminui.kernel.util.ConfigManager;
import io.mosip.testrig.adminui.utility.BaseClass;
import io.mosip.testrig.adminui.utility.BaseTestCaseFunc;
import io.mosip.testrig.adminui.utility.Commons;

public class CenterTest extends BaseClass {

	@Test

	public void centerCRUD() throws Exception {
		int totalHierarchyLevels = BaseTestCaseFunc.getHierarchyNumbers();
		Reporter.log("centerCRUD", true);
		String holidayDate = ConfigManager.getholidayDateCenter();

		Commons.click(driver(), By.id("admin/resources"), "Clicked on Resources menu");
		Commons.click(driver(), By.id("/admin/resources/centers"), "Opened Centers page");
		Commons.click(driver(), By.id("Create Center"), "Clicked on Create Center button");

		Commons.enter(driver(), By.id("name"), data, "Entered Center name");
		Commons.dropdown(driver(), By.id("centerTypeCode"), "Selected Center Type");
		Commons.enter(driver(), By.id("contactPerson"), data, "Entered contact person");
		Commons.enter(driver(), By.id("contactPhone"), data, "Entered contact phone");
		Commons.enter(driver(), By.id("longitude"), "1.1234", "Entered longitude");
		Commons.enter(driver(), By.id("latitude"), "2.2345", "Entered latitude");
		Commons.enter(driver(), By.id("addressLine1"), data, "Entered address line 1");
		Commons.enter(driver(), By.id("addressLine2"), data, "Entered address line 2");
		Commons.enter(driver(), By.id("addressLine3"), data, "Entered address line 3");

		for (int i = 1; i <= totalHierarchyLevels; i++) {
			Commons.dropdown(driver(), By.xpath("(//*[@id='fieldName'])[" + i + "]"), "Selected hierarchy level " + i);
		}

		try {
			Commons.dropdown(driver(), By.id("zone"), "Selected Zone");
		} catch (Exception e) {
			Reporter.log("Zone dropdown not available", true);
		}

		Commons.dropdown(driver(), By.id("holidayZone"), "Selected Holiday Zone");
		Commons.enter(driver(), By.id("noKiosk"), "10", "Entered number of kiosks");
		Commons.dropdown(driver(), By.id("processingTime"), "45", "Selected processing time");
		Commons.dropdown(driver(), By.id("startTime"), "9:00 AM", "Selected start time");
		Commons.dropdown(driver(), By.id("endTime"), "5:00 PM", "Selected end time");
		Commons.dropdown(driver(), By.id("lunchStartTime"), "1:00 PM", "Selected lunch start time");
		Commons.dropdown(driver(), By.id("lunchEndTime"), "2:00 PM", "Selected lunch end time");

		Commons.click(driver(), By.cssSelector(".mat-list-item:nth-child(1) .mat-pseudo-checkbox"),
				"Selected first service checkbox");
		Commons.click(driver(), By.cssSelector(".mat-list-item:nth-child(2) .mat-pseudo-checkbox"),
				"Selected second service checkbox");
		Commons.click(driver(), By.cssSelector(".mat-list-item:nth-child(3) > .mat-list-item-content"),
				"Selected third service");
		Commons.click(driver(), By.cssSelector(".mat-list-item:nth-child(4) > .mat-list-item-content"),
				"Selected fourth service");
		Commons.click(driver(), By.cssSelector(".mat-list-item:nth-child(5) > .mat-list-item-content"),
				"Selected fifth service");

		Commons.calendar(holidayDate, "Selected exceptional holiday date");
		Commons.click(driver(), By.id("createExceptionalHoliday"), "Created exceptional holiday");

		Commons.createRes(driver(), "Submitted Center creation");
		Commons.filterCenter(driver(), By.id("name"), data, "Filtered using created Center name");

		Commons.editCenter(driver(), data + 1, By.id("name"), "Edited Center name first time");
		Commons.filterCenter(driver(), By.id("name"), data + 1, "Filtered using updated Center name first time");

		Commons.activate(driver(), "Activated the Center");

		Commons.editCenter(driver(), data + 2, By.id("name"), "Edited Center name second time");
		Commons.filterCenter(driver(), By.id("name"), data + 2, "Filtered using updated Center name second time");

		Commons.deactivate(driver(), "Deactivated the Center");
		Commons.decommission(driver(), "Decommissioned the Center");
	}

}

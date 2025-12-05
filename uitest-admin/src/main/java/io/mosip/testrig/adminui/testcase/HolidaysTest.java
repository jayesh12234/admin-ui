package io.mosip.testrig.adminui.testcase;

import org.openqa.selenium.By;
import org.testng.annotations.Test;
import io.mosip.testrig.adminui.kernel.util.ConfigManager;
import io.mosip.testrig.adminui.utility.BaseClass;
import io.mosip.testrig.adminui.utility.Commons;

public class HolidaysTest extends BaseClass {

	@Test
	public void holidaysCRUD() throws Exception {
		String listofholidays = "admin/masterdata/holiday/view";
		String holidayDate = ConfigManager.getholidayDate();
		Commons.click(driver(), By.xpath("//a[@href='#/admin/masterdata']"), "Clicked on Master Data menu");
		Commons.click(driver(), By.id(listofholidays), "Opened List of Holidays page");
		Commons.click(driver(), By.id("Create"), "Clicked Create Holiday");

		Commons.enter(driver(), By.id("holidayName"), data, "Entered Holiday Name");
		Commons.enter(driver(), By.id("holidayDesc"), data, "Entered Holiday Description");

		Commons.calendar(holidayDate, "Selected Holiday Date");
		Commons.dropdown(driver(), By.id("locationCode"), "Selected Holiday Location");

		Commons.create(driver(), "Created Holiday");

		Commons.filter(driver(), By.id("holidayName"), data, "Filtered Holiday by Name");

		Commons.edit(driver(), data + 1, By.id("holidayName"), "Editing Holiday Name");
		Commons.filter(driver(), By.id("holidayName"), data + 1, "Filtered after edit");

		Commons.activate(driver(), "Activated Holiday");

		Commons.edit(driver(), data + 2, By.id("holidayName"), "Editing Holiday Name again");
		Commons.filter(driver(), By.id("holidayName"), data + 2, "Filtered after second edit");

		Commons.deactivate(driver(), "Deactivated Holiday");

	}
}

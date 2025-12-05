package io.mosip.testrig.adminui.testcase;

import java.io.IOException;
import org.openqa.selenium.By;
import org.testng.annotations.Test;
import io.mosip.testrig.adminui.utility.BaseClass;
import io.mosip.testrig.adminui.utility.Commons;

public class DeviceTypesTest extends BaseClass {

	@Test
	public void deviceTypesCRUD() throws IOException {
		String deviceTypes = "admin/masterdata/device-types/view";
		Commons.click(driver(), By.xpath("//a[@href='#/admin/masterdata']"), "Clicked on Master Data menu");
		Commons.click(driver(), By.id(deviceTypes), "Opened Device Types page");
		Commons.click(driver(), By.id("Create"), "Clicked on Create Device Type button");

		Commons.enter(driver(), By.id("code"), data, "Entered Device Type Code");
		Commons.enter(driver(), By.id("name"), data, "Entered Device Type Name");
		Commons.enter(driver(), By.id("description"), data, "Entered Device Type Description");
		Commons.create(driver(), "Created Device Type");

		Commons.filter(driver(), By.id("name"), data, "Filtered by device type name");

		Commons.edit(driver(), data + 1, By.id("name"), "Editing Device Type Name");

		Commons.filter(driver(), By.id("name"), data + 1, "Filtered after edit");

		Commons.activate(driver(), "Activated Device Type");

		Commons.edit(driver(), data + 2, By.id("name"), "Editing Device Type Name");

		Commons.filter(driver(), By.id("name"), data + 2, "Filtered after second edit");

		Commons.deactivate(driver(), "Deactivated Device Type");
	}
}

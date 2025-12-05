package io.mosip.testrig.adminui.testcase;

import java.io.IOException;
import org.openqa.selenium.By;
import org.testng.annotations.Test;
import io.mosip.testrig.adminui.utility.BaseClass;
import io.mosip.testrig.adminui.utility.Commons;

public class DeviceSpecificationTest extends BaseClass {

	@Test
	public void deviceSpecCRUD() throws IOException {
		String devicespec = "admin/masterdata/device-specs/view";
		Commons.click(driver(), By.xpath("//a[@href='#/admin/masterdata']"), "Clicked on Master Data menu");
		Commons.click(driver(), By.id(devicespec), "Opened Device Specification page");
		Commons.click(driver(), By.id("Create"), "Clicked on Create Device Specification");

		Commons.enter(driver(), By.id("name"), data, "Entered Device Name");
		Commons.enter(driver(), By.id("description"), data, "Entered Device Description");
		Commons.enter(driver(), By.id("brand"), data, "Entered Device Brand");
		Commons.enter(driver(), By.id("model"), data, "Entered Device Model");
		Commons.enter(driver(), By.id("minDriverversion"), data, "Entered Minimum Driver Version");

		Commons.dropdown(driver(), By.id("deviceTypeCode"), "Selected Device Type");

		Commons.create(driver(), "Saved the new Device Specification");

		Commons.filter(driver(), By.id("name"), data, "Filtered Device Spec by name");

		Commons.edit(driver(), data + 1, By.id("name"), "Edited Device Name");
		Commons.filter(driver(), By.id("name"), data + 1, "Filtered again after edit");

		Commons.activate(driver(), "Activated Device Specification");

		Commons.edit(driver(), data + 2, By.id("name"), "Edited Device Name again after activation");
		Commons.filter(driver(), By.id("name"), data + 2, "Filtered again after second edit");

		Commons.deactivate(driver(), "Deactivated Device Specification");
	}
}

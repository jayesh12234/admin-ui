package io.mosip.testrig.adminui.testcase;

import java.io.IOException;

import org.openqa.selenium.By;
import org.testng.annotations.Test;
import io.mosip.testrig.adminui.kernel.util.ConfigManager;
import io.mosip.testrig.adminui.utility.BaseClass;
import io.mosip.testrig.adminui.utility.Commons;

public class DeviceTest extends BaseClass {
	@Test
	public void deviceCRUD() throws InterruptedException, IOException {
		String validityDate;

		validityDate = ConfigManager.getvalidityDate();
		Commons.click(driver(), By.id("admin/resources"), "Clicked on Resources menu");
		Commons.click(driver(), By.id("/admin/resources/devices"), "Opened Devices page");
		Commons.click(driver(), By.id("Create Device"), "Clicked on Create Device");

		Commons.enter(driver(), By.id("name"), data, "Entered Device Name");
		Commons.enter(driver(), By.id("serialNumber"), data, "Entered Serial Number");
		Commons.enter(driver(), By.id("macAddress"), "1.1234", "Entered MAC Address");
		Commons.enter(driver(), By.id("ipAddress"), "2.2345", "Entered IP Address");

		Commons.calendar(validityDate, "Selected Device Validity Date");
		Commons.dropdown(driver(), By.id("deviceSpecId"), "Selected Device Specification");
		Commons.dropdown(driver(), By.id("regCenterId"), "Selected Registration Center");

		Commons.createRes(driver(), "Created Device Record Successfully");

		Commons.filter(driver(), By.id("name"), data, "Filtered device by name");

		Commons.editRes(driver(), data + 1, By.id("name"), "Edited Device Name");
		Commons.filter(driver(), By.id("name"), data + 1, "Filtered again after edit");

		Commons.activate(driver(), "Activated Device");

		Commons.editRes(driver(), data + 2, By.id("name"), "Edited Device again after activation");
		Commons.filter(driver(), By.id("name"), data + 2, "Filtered after second edit");

		Commons.deactivate(driver(), "Deactivated Device");
		Commons.decommission(driver(), "Decommissioned Device");
	}
}

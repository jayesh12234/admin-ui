package io.mosip.testrig.adminui.testcase;

import org.openqa.selenium.By;
import org.testng.annotations.Test;
import io.mosip.testrig.adminui.kernel.util.ConfigManager;
import io.mosip.testrig.adminui.utility.BaseClass;
import io.mosip.testrig.adminui.utility.Commons;

public class MachineTest extends BaseClass {

	@Test
	public void machineCRUD() throws Exception {
		String holidayDateCenter = ConfigManager.getholidayDateCenter();
		String publicKey = ConfigManager.getpublicKey();
		String signPublicKey = ConfigManager.getsignPublicKey();
		Commons.click(driver(), By.id("admin/resources"), "Clicked on Resources menu");
		Commons.click(driver(), By.xpath("//a[@href='#/admin/resources/machines']"), "Opened Machines page");
		Commons.click(driver(), By.id("Create Machine"), "Clicked Create Machine");

		Commons.enter(driver(), By.id("name"), data, "Entered Machine Name");
		Commons.enter(driver(), By.id("serialNumber"), "1234567", "Entered Serial Number");
		Commons.enter(driver(), By.id("macAddress"), "1.2.3.4.5.6", "Entered MAC Address");
		Commons.enter(driver(), By.id("ipAddress"), "2.3.4.5.6", "Entered IP Address");

		String loginLang = ConfigManager.getloginlang();
		Commons.calendar(holidayDateCenter, loginLang.toLowerCase(), "Selected Validity Date");

		Commons.dropdown(driver(), By.id("machineSpecId"), "Selected Machine Spec");
		Commons.enter(driver(), By.id("publicKey"), publicKey, "Entered Public Key");
		Commons.enter(driver(), By.id("signPublicKey"), signPublicKey, "Entered Sign Public Key");

		try {
			Commons.dropdown(driver(), By.id("zone"), "Selected Zone");
		} catch (Exception e) {
		}

		Commons.dropdown(driver(), By.id("regCenterId"), "Selected Registration Center");

		Commons.createRes(driver(), "Created Machine Record");

		Commons.filter(driver(), By.id("name"), data, "Filtered by Machine Name");

		Commons.editRes(driver(), data + 1, By.id("name"), "Edited Machine Name");
		Commons.filter(driver(), By.id("name"), data + 1, "Filtered after edit");

		Commons.activate(driver(), "Activated Machine Record");

		Commons.editRes(driver(), data + 2, By.id("name"), "Edited Machine Name again");
		Commons.filter(driver(), By.id("name"), data + 2, "Filtered after second edit");

		Commons.deactivate(driver(), "Deactivated Machine Record");

		Commons.decommission(driver(), "Decommissioned Machine Record");
	}
}

package io.mosip.testrig.adminui.testcase;

import java.io.IOException;
import org.openqa.selenium.By;
import org.testng.annotations.Test;
import io.mosip.testrig.adminui.utility.BaseClass;
import io.mosip.testrig.adminui.utility.Commons;

public class MachineSpecificationTest extends BaseClass {

	@Test
	public void machineSpecCRUD() throws IOException {
		String machinespec = "admin/masterdata/machine-specs/view";
		Commons.click(driver(), By.xpath("//a[@href='#/admin/masterdata']"), "Clicked on Master Data menu");
		Commons.click(driver(), By.id(machinespec), "Opened Machine Specs page");
		Commons.click(driver(), By.id("Create"), "Clicked Create Machine Spec");

		Commons.enter(driver(), By.id("name"), data, "Entered Machine Name");
		Commons.enter(driver(), By.id("brand"), data, "Entered Machine Brand");
		Commons.enter(driver(), By.id("model"), data, "Entered Machine Model");
		Commons.enter(driver(), By.id("minDriverversion"), data, "Entered Minimum Driver Version");
		Commons.enter(driver(), By.id("description"), data, "Entered Machine Description");

		Commons.dropdown(driver(), By.id("machineTypeCode"), "Selected Machine Type");

		Commons.create(driver(), "Created Machine Spec");

		Commons.filter(driver(), By.id("name"), data, "Filtered by Machine Name");

		Commons.edit(driver(), data + 1, By.id("name"), "Edited Machine Name");
		Commons.filter(driver(), By.id("name"), data + 1, "Filtered after edit");

		Commons.activate(driver(), "Activated Machine Spec");

		Commons.edit(driver(), data + 2, By.id("name"), "Edited Machine Name again");
		Commons.filter(driver(), By.id("name"), data + 2, "Filtered after second edit");

		Commons.deactivate(driver(), "Deactivated Machine Spec");

	}
}

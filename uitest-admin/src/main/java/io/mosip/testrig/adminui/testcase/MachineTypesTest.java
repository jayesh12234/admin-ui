package io.mosip.testrig.adminui.testcase;

import java.io.IOException;
import org.openqa.selenium.By;
import org.testng.annotations.Test;
import io.mosip.testrig.adminui.utility.BaseClass;
import io.mosip.testrig.adminui.utility.Commons;

public class MachineTypesTest extends BaseClass {

	@Test
	public void machineTypesCRUD() throws IOException {
		String machinetypes = "admin/masterdata/machine-type/view";
		Commons.click(driver(), By.xpath("//a[@href='#/admin/masterdata']"), "Clicked on Master Data menu");
		Commons.click(driver(), By.id(machinetypes), "Navigated to Machine Types page");
		Commons.click(driver(), By.id("Create"), "Clicked on Create button");

		Commons.enter(driver(), By.id("code"), data, "Entered Machine Type Code");
		Commons.enter(driver(), By.id("name"), data, "Entered Machine Type Name");
		Commons.enter(driver(), By.id("description"), data, "Entered Machine Type Description");

		Commons.create(driver(), "Clicked Create to save the Machine Type");

		Commons.filter(driver(), By.id("name"), data, "Filtered Machine Types by Name");

		Commons.edit(driver(), data + 1, By.id("name"), "Edited Machine Type Name to " + (data + 1));
		Commons.filter(driver(), By.id("name"), data + 1, "Filtered Machine Types by Edited Name");

		Commons.activate(driver(), "Activated the Machine Type");

		Commons.edit(driver(), data + 2, By.id("name"), "Edited Machine Type Name to " + (data + 2));
		Commons.filter(driver(), By.id("name"), data + 2, "Filtered Machine Types by Edited Name");

		Commons.deactivate(driver(), "Deactivated the Machine Type");

	}
}

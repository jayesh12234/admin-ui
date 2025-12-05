package io.mosip.testrig.adminui.testcase;

import java.io.IOException;
import org.openqa.selenium.By;
import org.testng.annotations.Test;
import io.mosip.testrig.adminui.utility.BaseClass;
import io.mosip.testrig.adminui.utility.Commons;

public class DynamicFieldTest extends BaseClass {

	@Test
	public void dynamicFieldCRUD() throws IOException {
		Commons.click(driver(), By.xpath("//a[@href='#/admin/masterdata']"), "Clicked on Master Data menu");
		Commons.click(driver(), By.id("createDynamicField"), "Opened Create Dynamic Field page");

		Commons.enter(driver(), By.id("code"), data, "Entered Dynamic Field Code");
		Commons.enter(driver(), By.id("name"), "Automation", "Entered Dynamic Field Name");
		Commons.enter(driver(), By.id("description"), "Automation", "Entered Dynamic Field Description");
		Commons.enter(driver(), By.id("value"), data, "Entered Dynamic Field Value");

		Commons.create(driver(), "Created Dynamic Field");
		Commons.filter(driver(), By.id("description"), "Automation", "Filtered by Dynamic Field Description");

		Commons.edit(driver(), data + 1, By.id("code"), "Editing Dynamic Field Code");
		Commons.filter(driver(), By.id("description"), "Automation", "Filtered after Dynamic Field edit");

		Commons.activate(driver(), "Activated Dynamic Field");

		Commons.edit(driver(), data + 2, By.id("code"), "Editing Dynamic Field Code again");
		Commons.filter(driver(), By.id("description"), "Automation", "Filtered after second edit");

		Commons.deactivate(driver(), "Deactivated Dynamic Field");

	}
}

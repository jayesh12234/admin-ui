package io.mosip.testrig.adminui.testcase;

import java.io.IOException;

import org.openqa.selenium.By;
import org.testng.annotations.Test;
import io.mosip.testrig.adminui.utility.BaseClass;
import io.mosip.testrig.adminui.utility.Commons;

public class DocumentTypes extends BaseClass {

	@Test
	public void documentTypesCRUD() throws IOException {
		String documentTypes = "admin/masterdata/document-type/view";
		Commons.click(driver(), By.xpath("//a[@href='#/admin/masterdata']"), "Clicked on Master Data menu");
		Commons.click(driver(), By.id(documentTypes), "Opened Document Types page");
		Commons.click(driver(), By.id("Create"), "Clicked on Create Document Type");

		Commons.enter(driver(), By.id("code"), data, "Entered Document Type Code");
		Commons.enter(driver(), By.id("name"), data, "Entered Document Type Name");
		Commons.enter(driver(), By.id("description"), data, "Entered Document Type Description");

		Commons.create(driver(), "Created Document Type");

		Commons.filter(driver(), By.id("name"), data, "Filtered by Document Type Name");

		Commons.edit(driver(), data + 1, By.id("name"), "Editing Document Type Name");
		Commons.filter(driver(), By.id("name"), data + 1, "Filtered after editing Document Type");

		Commons.activate(driver(), "Activated Document Type");

		Commons.edit(driver(), data + 2, By.id("name"), "Editing Document Type Name again");
		Commons.filter(driver(), By.id("name"), data + 2, "Filtered after second edit");

		Commons.deactivate(driver(), "Deactivated Document Type");

	}
}

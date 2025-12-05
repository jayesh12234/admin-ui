package io.mosip.testrig.adminui.testcase;

import java.io.IOException;
import org.openqa.selenium.By;
import org.testng.annotations.Test;

import io.mosip.testrig.adminui.utility.BaseClass;
import io.mosip.testrig.adminui.utility.Commons;

public class DocumentCategoriesTest extends BaseClass {

	@Test
	public void documentCategoriesCRUD() throws IOException {

		String documentCategories = "admin/masterdata/document-categories/view";
		Commons.click(driver(), By.xpath("//a[@href='#/admin/masterdata']"), "Clicked on Master Data menu");
		Commons.click(driver(), By.id(documentCategories), "Opened Document Categories page");
		Commons.click(driver(), By.id("Create"), "Clicked on Create Document Category");

		Commons.enter(driver(), By.id("code"), data, "Entered Document Category Code");
		Commons.enter(driver(), By.id("name"), data, "Entered Document Category Name");
		Commons.enter(driver(), By.id("description"), data, "Entered Document Category Description");

		Commons.create(driver(), "Created Document Category");

		Commons.filter(driver(), By.id("name"), data, "Filtered by Document Category Name");

		Commons.edit(driver(), data + 1, By.id("name"), "Editing Document Category Name");

		Commons.filter(driver(), By.id("name"), data + 1, "Filtered after edit");

		Commons.activate(driver(), "Activated Document Category");

		Commons.edit(driver(), data + 2, By.id("name"), "Editing Document Category again");

		Commons.filter(driver(), By.id("name"), data + 2, "Filtered after second edit");

		Commons.deactivate(driver(), "Deactivated Document Category");

	}
}

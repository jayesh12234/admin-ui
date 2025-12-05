package io.mosip.testrig.adminui.testcase;

import java.io.IOException;
import org.openqa.selenium.By;
import org.testng.annotations.Test;
import io.mosip.testrig.adminui.utility.BaseClass;
import io.mosip.testrig.adminui.utility.Commons;

public class TemplateTest extends BaseClass {

	@Test
	public void templateCRUD() throws IOException {
		String templatesid = "admin/masterdata/templates/view";
		Commons.click(driver(), By.xpath("//a[@href='#/admin/masterdata']"), "Clicked on Master Data menu");
		Commons.click(driver(), By.id(templatesid), "Navigated to Templates page");

		Commons.click(driver(), By.id("Create"), "Clicked on Create button");
		Commons.enter(driver(), By.id("name"), data, "Entered Template Name");
		Commons.enter(driver(), By.id("description"), data, "Entered Template Description");
		Commons.enter(driver(), By.id("model"), data, "Entered Template Model");
		Commons.enter(driver(), By.id("fileText"), data, "Entered File Text");

		Commons.dropdown(driver(), By.id("fileFormatCode"), "Selected File Format");
		Commons.dropdown(driver(), By.id("templateTypeCode"), "Selected Template Type");
		Commons.dropdown(driver(), By.id("moduleId"), "Selected Module");

		Commons.create(driver(), "Clicked Create to save the Template");

		Commons.filter(driver(), By.id("name"), data, "Filtered Templates by Name");

		Commons.edit(driver(), data + 1, By.id("name"), "Edited Template Name to " + (data + 1));
		Commons.filter(driver(), By.id("name"), data + 1, "Filtered Templates by Edited Name");

		Commons.activate(driver(), "Activated the Template");

		Commons.edit(driver(), data + 2, By.id("name"), "Edited Template Name to " + (data + 2));
		Commons.filter(driver(), By.id("name"), data + 2, "Filtered Templates by Edited Name");

		Commons.deactivate(driver(), "Deactivated the Template");
	}
}

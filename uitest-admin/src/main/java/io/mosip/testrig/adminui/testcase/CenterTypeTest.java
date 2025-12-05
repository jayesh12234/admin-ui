package io.mosip.testrig.adminui.testcase;

import java.io.IOException;

import org.openqa.selenium.By;
import org.testng.annotations.Test;

import io.mosip.testrig.adminui.utility.BaseClass;
import io.mosip.testrig.adminui.utility.Commons;

public class CenterTypeTest extends BaseClass {

	@Test
	public void centerTypeCRUD() throws IOException {
		String idCenterTypeCard = "admin/masterdata/center-type/view";

		Commons.click(driver(), By.xpath("//a[@href='#/admin/masterdata']"), "Clicked on Master Data menu");
		Commons.click(driver(), By.id(idCenterTypeCard), "Opened Center Type page");
		Commons.click(driver(), By.id("Create"), "Clicked on Create Center Type button");

		Commons.enter(driver(), By.id("code"), data, "Entered code for new Center Type");
		Commons.enter(driver(), By.id("name"), data, "Entered name for new Center Type");
		Commons.enter(driver(), By.id("descr"), data, "Entered description for new Center Type");

		Commons.create(driver(), "Submitted new Center Type creation");
		Commons.filter(driver(), By.id("name"), data, "Filtered using created Center Type name");

		Commons.edit(driver(), data + 1, By.id("name"), "Edited Center Type name first time");
		Commons.filter(driver(), By.id("name"), data + 1, "Filtered using updated Center Type name");

		Commons.activate(driver(), "Activated the Center Type");
		Commons.edit(driver(), data + 2, By.id("name"), "Edited Center Type name second time");
		Commons.filter(driver(), By.id("name"), data + 2, "Filtered using updated Center Type name second time");
		Commons.deactivate(driver(), "Deactivated the Center Type");

	}
}

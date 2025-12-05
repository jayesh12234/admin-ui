package io.mosip.testrig.adminui.testcase;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.Test;
import io.mosip.testrig.adminui.kernel.util.ConfigManager;
import io.mosip.testrig.adminui.utility.BaseClass;
import io.mosip.testrig.adminui.utility.Commons;
import io.mosip.testrig.adminui.utility.Screenshot;
import io.mosip.testrig.adminui.utility.TestRunner;

public class BulkUploadTest extends BaseClass {

	private static final Logger logger = Logger.getLogger(BulkUploadTest.class);

	@Test(dataProvider = "data-provider")
	public void bulkUploadCRUD(String table) throws Exception {

		Commons.click(driver(), By.id("admin/bulkupload"), "Clicked on Bulk Upload menu");
		Commons.click(driver(), By.xpath("//a[@href='#/admin/bulkupload/masterdataupload']"),
				"Opened Master Data Upload page");

		for (int count = 0; count <= 2; count++) {

			Commons.click(driver(), By.id("Upload Data"), "Clicked on Upload Data button");

			if (count == 0)
				Commons.dropdown(driver(), By.id("operation"), By.id("Insert"), "Selected Insert operation");
			if (count == 1)
				Commons.dropdown(driver(), By.id("operation"), By.id("Update"), "Selected Update operation");
			if (count == 2)
				Commons.dropdown(driver(), By.id("operation"), By.id("Delete"), "Selected Delete operation");

			Commons.dropdown(driver(), By.id("tableName"), By.id(table), "Selected table: " + table);

			String filePath = TestRunner.getResourcePath() + "//BulkUploadFiles//" + ConfigManager.getloginlang() + "//"
					+ table + ".csv";
			Commons.enter(driver(), By.id("fileInput"), filePath, "Entered file path for table: " + table);

			Commons.click(driver(), By.xpath("//button[@id='createButton']"), "Clicked on Create/Upload button");
			Commons.click(driver(), By.id("confirmpopup"), "Confirmed the upload in popup");
			Commons.waitForElementVisible(driver(), By.xpath("//div[@class='mat-dialog-content']//div"));

			String divText = driver().findElement(By.xpath("//div[@class='mat-dialog-content']//div")).getText();
			String divTextArr[] = divText.split(":");
			logger.info("Bulk Upload Result: " + divTextArr[1].trim());

			Commons.click(driver(), By.id("confirmmessagepopup"), "Closed the confirmation message popup");
			Commons.waitForElementVisible(driver(), By.xpath("//table[@class='mat-table']//tr[2]//td[1]"));
			String transId = driver().findElement(By.xpath("//table[@class='mat-table']//tr[2]//td[1]")).getText();
			String status = driver().findElement(By.xpath("//table[@class='mat-table']//tr[2]//td[5]")).getText();
			Reporter.log("<p><img src='data:image/png;base64," + Screenshot.ClickScreenshot(driver())
					+ "' width='900' height='450'/></p>");
			Assert.assertTrue(transId.equals(divTextArr[1].trim()));
			Assert.assertTrue(status.equalsIgnoreCase("COMPLETED"), "Status Should be COMPLETED");
		}
	}
}

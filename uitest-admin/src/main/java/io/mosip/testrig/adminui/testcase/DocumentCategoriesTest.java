package io.mosip.testrig.adminui.testcase;
import java.io.IOException;
import org.openqa.selenium.By;
import org.testng.annotations.Test;


import io.mosip.testrig.adminui.utility.BaseClass;
import io.mosip.testrig.adminui.utility.Commons;

public class DocumentCategoriesTest extends BaseClass{
	
	@Test
	public void documentCategoriesCRUD() throws IOException {
		
		String documentCategories="admin/masterdata/document-categories/view";
		Commons.click(driver,By.xpath("//a[@href='#/admin/masterdata']"));
		Commons.click(driver,By.id(documentCategories));
		Commons.click(driver,By.id("Create"));   
		Commons.enter(driver,By.id("code"),data);
		Commons.enter(driver,By.id("name"),data);
		Commons.enter(driver,By.id("description"),data);
		Commons.create(driver);  
		Commons.filter(driver, By.id("name"), data);
		Commons.edit(driver,data+1,By.id("name"));
		Commons.filter(driver, By.id("name"), data+1);
		Commons.activate(driver);
		Commons.edit(driver,data+2,By.id("name"));
		Commons.filter(driver, By.id("name"), data+2);
		Commons.deactivate(driver);

	}
}

package io.mosip.testrig.adminui.testcase;
import static org.testng.Assert.assertNotEquals;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Reporter;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.aventstack.extentreports.Status;

import io.mosip.testrig.adminui.utility.BaseClass;
import io.mosip.testrig.adminui.utility.Commons;
import io.mosip.testrig.adminui.utility.JsonUtil;
public class CenterTest extends BaseClass{

	@Test(groups = "CTR")
	
  public void centerCRUD() throws Exception {
	  
   Reporter.log("centerCRUD",true);
   test=extent.createTest("CenterTest", "verify Login");
   String holidayDate=JsonUtil.JsonObjParsing(Commons.getTestData(),"holidayDateCenter");
    Commons.click(test,driver,By.id("admin/resources"));

    Commons.click(test,driver,By.id("/admin/resources/centers"));
    
    Commons.click(test,driver, By.id("Create Center"));
    test.log(Status.INFO, "Click on Create Center");
    /*
     * Select Registration Center Type
     */
     
    /**
     * centerTypeCode dropdown
     */
   Commons.enter(test,driver, By.id("name"), data);
   
     Commons.dropdown(test,driver,By.id("centerTypeCode"));
     test.log(Status.INFO, "Click on dropdown");
     Commons.enter(test,driver, By.id("contactPerson"),data);
     Commons.enter(test,driver,By.id("contactPhone"),data);
    
     Commons.enter(test,driver,By.id("longitude"),"1.1234");
     Commons.enter(test,driver,By.id("latitude"),"2.2345");
     test.log(Status.INFO, "Enters Longitude And Latitude");
     Commons.enter(test,driver,By.id("addressLine1"),data);
     Commons.enter(test,driver,By.id("addressLine2"),data);
     Commons.enter(test,driver,By.id("addressLine3"),data);
     test.log(Status.INFO, "Enters Address");
    Commons.dropdown(test,driver, By.xpath("(//*[@id='fieldName'])[1]"));
    Commons.dropdown(test,driver, By.xpath("(//*[@id='fieldName'])[2]"));
    Commons.dropdown(test,driver, By.xpath("(//*[@id='fieldName'])[3]"));
    Commons.dropdown(test,driver, By.xpath("(//*[@id='fieldName'])[4]"));
    Commons.dropdown(test,driver, By.xpath("(//*[@id='fieldName'])[5]"));
   
  
 try{   Commons.dropdown(test,driver, By.id("zone"));
 
 }catch(Exception e) {
	 test.log(Status.INFO, e);
 }
    Commons.dropdown(test,driver, By.id("holidayZone"));
    test.log(Status.INFO, "Click on Holidayzon");

    
    
    Commons.enter(test,driver,By.id("noKiosk"),"10");
    
    Commons.dropdown(test,driver,By.id("processingTime"),"45");
    Commons.dropdown(test,driver,By.id("startTime"),"9:00 AM");
    Commons.dropdown(test,driver,By.id("endTime"),"5:00 PM");
    Commons.dropdown(test,driver,By.id("lunchStartTime"),"1:00 PM");
    Commons.dropdown(test,driver,By.id("lunchEndTime"),"2:00 PM");
    
     Commons.click(test,driver,By.cssSelector(".mat-list-item:nth-child(1) .mat-pseudo-checkbox"));
    Commons.click(test,driver,By.cssSelector(".mat-list-item:nth-child(2) .mat-pseudo-checkbox"));
    Commons.click(test,driver,By.cssSelector(".mat-list-item:nth-child(3) > .mat-list-item-content"));
    Commons.click(test,driver,By.cssSelector(".mat-list-item:nth-child(4) > .mat-list-item-content"));
    Commons.click(test,driver,By.cssSelector(".mat-list-item:nth-child(5) > .mat-list-item-content"));
    
  //  Commons.enter(test,driver,By.id("holidayDate"),holidayDate);
    Commons.calendar(holidayDate);
    Commons.click(test,driver, By.id("createExceptionalHoliday"));
    test.log(Status.INFO, "Click on Exceptional Holiday");
    
    Commons.createRes(test,driver);
   	Commons.filterCenter(test,driver, By.id("name"), data);
   	

   	Commons.editCenter(test,driver,data+1,By.id("name"));
   	
   	Commons.filterCenter(test,driver, By.id("name"), data+1);
   	
   	Commons.activate(test,driver);
   	test.log(Status.INFO, "Click on Active");
   	Commons.editCenter(test,driver,data+2,By.id("name"));
   	Commons.filterCenter(test,driver, By.id("name"), data+2);
   	Commons.deactivate(test,driver);
   	test.log(Status.INFO, "Click on Deactive");
   	Commons.decommission(test,driver);
  }
}

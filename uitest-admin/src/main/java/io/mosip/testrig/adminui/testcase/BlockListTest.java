package io.mosip.testrig.adminui.testcase;

import java.io.IOException;
import org.openqa.selenium.By;
import org.testng.annotations.Test;
import io.mosip.testrig.adminui.kernel.util.ConfigManager;
import io.mosip.testrig.adminui.utility.BaseClass;
import io.mosip.testrig.adminui.utility.Commons;

public class BlockListTest extends BaseClass {

	@Test
	public void blocklistedwordsCRUD() throws IOException {
		String blocklistedWord = ConfigManager.getdummyData();
		String idBlocklisted = "admin/masterdata/blocklisted-words/view";

		Commons.click(driver(), By.xpath("//a[@href='#/admin/masterdata']"), "Clicked on Master Data menu");

		Commons.click(driver(), By.id(idBlocklisted), "Opened Blocklisted Words page");

		Commons.click(driver(), By.id("Create"), "Clicked on Create Blocklisted Word button");

		Commons.enter(driver(), By.id("word"), blocklistedWord, "Entered new blocklisted word");

		Commons.enter(driver(), By.id("description"), data, "Entered description for blocklisted word");

		Commons.create(driver(), "Submitted Blocklisted Word creation");

		Commons.filter(driver(), By.id("word"), blocklistedWord, "Filtered using created word");

		Commons.edit(driver(), blocklistedWord + "auto", By.id("word"), "Edited blocklisted word first time");

		Commons.filter(driver(), By.id("word"), blocklistedWord + "A", "Filtered using updated word");

		Commons.activate(driver(), "Activated the word record");

		Commons.edit(driver(), blocklistedWord + "B", By.id("word"), "Edited blocklisted word second time");

		Commons.filter(driver(), By.id("word"), blocklistedWord + "B", "Filtered after second update");

		Commons.deactivate(driver(), "Deactivated the word record");

	}
}

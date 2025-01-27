/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.selenium.page.repository;

import java.io.File;

import org.junit.Assert;
import org.olat.selenium.page.course.CoursePageFragment;
import org.olat.selenium.page.course.CourseSettingsPage;
import org.olat.selenium.page.course.CourseWizardPage;
import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Page to control the author environnment.
 * 
 * 
 * Initial date: 20.06.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AuthoringEnvPage {
	
	public static final By createMenuBy = By.cssSelector("ul.o_sel_author_create");
	public static final By generaltabBy = By.className("o_sel_edit_repositoryentry");
	
	private WebDriver browser;
	
	public AuthoringEnvPage(WebDriver browser) {
		this.browser = browser;
	}
	
	/**
	 * Check that the segment for the "Search" in author environment is selected.
	 * 
	 * @return
	 */
	public AuthoringEnvPage assertOnGenericSearch() {
		By genericSearchBy = By.xpath("//ul[contains(@class,'o_segments')]/li/a[contains(@class,'btn-primary')][contains(@class,'o_sel_author_search')]");
		OOGraphene.waitElement(genericSearchBy, browser);
		WebElement genericSearchSegment = browser.findElement(genericSearchBy);
		Assert.assertTrue(genericSearchSegment.isDisplayed());
		return this;
	}
	
	public RepositorySettingsPage createCP(String title) {
		return openCreateDropDown()
			.clickCreate(ResourceType.cp)
			.fillCreateForm(title);
	}
	
	public RepositorySettingsPage createWiki(String title) {
		return openCreateDropDown()
			.clickCreate(ResourceType.wiki)
			.fillCreateForm(title);
	}
	
	public RepositorySettingsPage createSurvey(String title) {
		return openCreateDropDown()
			.clickCreate(ResourceType.survey)
			.fillCreateForm(title);
	}
	
	public CourseSettingsPage createCourse(String title) {
		return createCourse(title, false);
	}
	
	public CourseSettingsPage createCourse(String title, boolean learnPath) {
		openCreateDropDown()
			.clickCreate(ResourceType.course)
			.fillCreateCourseForm(title, learnPath)
			.assertOnInfos();
		return new CourseSettingsPage(browser);
	}
	
	public RepositoryEditDescriptionPage createPortfolioBinder(String title) {
		return openCreateDropDown()
			.clickCreate(ResourceType.portfolio)
			.fillCreateForm(title)
			.assertOnInfos();
	}
	
	public RepositoryEditDescriptionPage createQTI21Test(String title) {
		return openCreateDropDown()
			.clickCreate(ResourceType.qti21Test)
			.fillCreateForm(title)
			.assertOnInfos();
	}
	
	/**
	 * Open the drop-down to create a new resource.
	 * @return
	 */
	public AuthoringEnvPage openCreateDropDown() {
		By createMenuCaretBy = By.cssSelector("button.o_sel_author_create");
		OOGraphene.waitElement(createMenuCaretBy, browser);
		browser.findElement(createMenuCaretBy).click();
		OOGraphene.waitElement(createMenuBy, browser);
		return this;
	}

	/**
	 * Click the link to create a learning resource in the create drop-down
	 * @param type
	 * @return
	 */
	public AuthoringEnvPage clickCreate(ResourceType type) {
		WebElement createMenu = browser.findElement(createMenuBy);
		Assert.assertTrue(createMenu.isDisplayed());
		WebElement createLink = createMenu.findElement(By.className("o_sel_author_create-" + type.type()));
		Assert.assertTrue(createLink.isDisplayed());
		createLink.click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	/**
	 * Fill the create form and submit
	 * @param displayName The name of the learn resource
	 * @return Itself
	 */
	public RepositorySettingsPage fillCreateForm(String displayName) {
		OOGraphene.waitModalDialog(browser);
		By inputBy = By.cssSelector("div.modal.o_sel_author_create_popup div.o_sel_author_displayname input");
		browser.findElement(inputBy).sendKeys(displayName);
		By submitBy = By.cssSelector("div.modal.o_sel_author_create_popup .o_sel_author_create_submit");
		browser.findElement(submitBy).click();
		OOGraphene.waitBusy(browser);
		OOGraphene.waitModalDialogDisappears(browser);
		OOGraphene.waitElement(generaltabBy, browser);
		OOGraphene.waitTinymce(browser);
		return new RepositorySettingsPage(browser);
	}
	
	/**
	 * Fill the create form and submit
	 * @param displayName The name of the course
	 * @param learnPath true to use the new learn path course, false for the old model
	 * @return Itself
	 */
	public RepositorySettingsPage fillCreateCourseForm(String displayName, boolean learnPath) {
		OOGraphene.waitModalDialog(browser);
		By inputBy = By.cssSelector("div.modal.o_sel_author_create_popup div.o_sel_author_displayname input");
		browser.findElement(inputBy).sendKeys(displayName);
		// select node model for the course
		String type = learnPath ? "learningpath" : "condition";
		By typeBy = By.xpath("//div[@id='o_cocif_node_access']//input[@name='cif.node.access'][@value='" + type + "']");
		browser.findElement(typeBy).click();
		OOGraphene.waitBusy(browser);
		// create the course
		By submitBy = By.cssSelector("div.modal.o_sel_author_create_popup .o_sel_author_create_submit");
		browser.findElement(submitBy).click();
		OOGraphene.waitBusy(browser);
		OOGraphene.waitModalDialogDisappears(browser);
		OOGraphene.waitElement(generaltabBy, browser);
		OOGraphene.waitTinymce(browser);
		return new RepositorySettingsPage(browser);
	}
	
	/**
	 * Fill the create form and start the wizard
	 * @param displayName
	 * @return
	 */
	public CourseWizardPage fillCreateFormAndStartWizard(String displayName) {
		OOGraphene.waitModalDialog(browser);
		By inputBy = By.cssSelector("div.modal.o_sel_author_create_popup div.o_sel_author_displayname input");
		browser.findElement(inputBy).sendKeys(displayName);
		// select node model for the course
		By typeBy = By.xpath("//div[@id='o_cocif_node_access']//input[@name='cif.node.access'][@value='condition']");
		browser.findElement(typeBy).click();
		OOGraphene.waitBusy(browser);
		// select the simple wizard
		By simpleCourseWizardBy = By.xpath("//div[@id='o_cocsc_wizard']//input[@name='csc.wizard'][@value='simple.course']");
		browser.findElement(simpleCourseWizardBy).click();
		// create the course
		By createBy = By.cssSelector("div.modal.o_sel_author_create_popup .o_sel_author_create_submit");
		browser.findElement(createBy).click();
		OOGraphene.waitBusy(browser);
		return new CourseWizardPage(browser);
	}
	
	/**
	 * Short cut to create quickly a course
	 * @param title
	 */
	public void quickCreateCourse(String title) {
		RepositoryEditDescriptionPage editDescription = openCreateDropDown()
			.clickCreate(ResourceType.course)
			.fillCreateForm(title)
			.assertOnInfos();
			
		//from description editor, back to details and launch the course
		editDescription
			.clickToolbarBack();
	}
	
	/**
	 * Try to upload a resource if the type is recognized.
	 * 
	 * @param title The title of the learning resource
	 * @param resource The zip file to import
	 * @return Itself
	 */
	public AuthoringEnvPage uploadResource(String title, File resource) {
		By importBy = By.className("o_sel_author_import");
		OOGraphene.waitElement(importBy, browser);
		browser.findElement(importBy).click();
		OOGraphene.waitBusy(browser);
		
		By inputBy = By.cssSelector(".o_fileinput input[type='file']");
		OOGraphene.uploadFile(inputBy, resource, browser);
		OOGraphene.waitElement(By.className("o_sel_author_imported_name"), browser);
		
		By titleBy = By.cssSelector(".o_sel_author_imported_name input");
		WebElement titleEl = browser.findElement(titleBy);
		titleEl.sendKeys(title);
		
		//save
		By saveBy = By.cssSelector("div.o_sel_repo_save_details button.btn-primary");
		WebElement saveButton = browser.findElement(saveBy);
		if(saveButton.isEnabled()) {
			saveButton.click();
			OOGraphene.waitBusy(browser);
			OOGraphene.waitModalDialogDisappears(browser);
			OOGraphene.waitElement(generaltabBy, browser);
			OOGraphene.waitTinymce(browser);
		}
		return this;
	}
	
	public AuthoringEnvPage assertOnResourceType() {
		By typeEl = By.cssSelector(".o_sel_author_type");
		OOGraphene.waitElement(typeEl, 5, browser);
		return this;
	}
	
	public void selectResource(String title) {
		By selectBy = By.xpath("//div[contains(@class,'o_coursetable')]//a[contains(text(),'" + title + "')]");
		OOGraphene.waitElement(selectBy, browser);
		browser.findElement(selectBy).click();
		OOGraphene.waitBusy(browser);
	}
	
	public void editResource(String title) {
		By editBy = By.xpath("//div[contains(@class,'o_coursetable')]//tr[//a[contains(text(),'" + title + "')]]//a[contains(@onclick,'edit')]");
		browser.findElement(editBy).click();
		OOGraphene.waitBusy(browser);
	}
	
	/**
	 * Click back from the editor
	 * 
	 * @return
	 */
	public CoursePageFragment clickToolbarRootCrumb() {
		OOGraphene.clickBreadcrumbBack(browser);
		return new CoursePageFragment(browser);
	}
	
	public enum ResourceType {
		course("CourseModule"),
		cp("FileResource.IMSCP"),
		wiki("FileResource.WIKI"),
		portfolio("BinderTemplate"),
		qti21Test("FileResource.IMSQTI21"),
		survey("FileResource.FORM");
		
		private final String type;
		
		private ResourceType(String type) {
			this.type = type;
		}
		
		public String type() {
			return type;
		}
	}
}

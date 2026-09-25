import { expect, Locator, Page } from '@playwright/test';
import { BasePage } from '../basePage.ts';

export default class CitizenApplicationsPage extends BasePage{

  private readonly yourRequestAndApplications: Locator;
  private readonly respondToApplicationText: Locator;
  private readonly supportingMaterialRadioYes: Locator;
  private readonly supportingMaterialFile: Locator;
  private readonly uploadFileButton: Locator;
  private readonly copyToOtherPartyYes: Locator;
  private readonly copyToOtherPartyNo: Locator;
  private readonly addInfoToNoOption: Locator;
  private readonly checkYourAnswerHeading: Locator;

  constructor(page: Page) {
    super(page);
    this.yourRequestAndApplications = this.page.getByRole('link', { name: `Your request and applications` });
    this.respondToApplicationText = page.locator('#respond-to-application-text');
    this.supportingMaterialRadioYes = page.locator('#supporting-material-yes-no');
    this.supportingMaterialFile = page.locator('#supportingMaterialFile');
    this.uploadFileButton = page.locator('#upload');
    this.copyToOtherPartyYes = page.locator('#copyToOtherPartyYes');
    this.copyToOtherPartyNo = page.locator('#copyToOtherPartyYesOrNo-2');
    this.addInfoToNoOption = page.locator('#copyToOtherPartyText');
    this.checkYourAnswerHeading = page.locator('//h1[@class="govuk-panel__title"]');

  }

  async navigateToYourRequestAndApplicationsPage() {
    await this.page.waitForLoadState('load');
    await this.yourRequestAndApplications.click();
    await this.page.waitForLoadState('load');
  }

  async navigateToApplication(applicationName: string, status: string = `In progress` ) {
    await this.page.waitForLoadState('load');
    await expect(this.page.locator(`//td/a[normalize-space()='${applicationName}']/../../td[normalize-space()='${status}']`)).toBeVisible();
    await this.page.waitForLoadState('load');
    await this.page.locator(`//td/a[normalize-space()='${applicationName}']`).first().click();
  }

  async assertApplicationAndResponseDetails(application: string, details: string[]) {
    await this.page.waitForLoadState('load');
    const heading = this.page.getByRole('heading', { name: `Application to ${application}` });
    await expect(heading).toBeVisible();

    for (const detail of details) {
      const key = detail.split('-')[0].trim();
      const value = detail.split('-')[1].trim();
      const locators = this.page.locator(`//dt[normalize-space()='${key}']/following-sibling::dd[1]`);
      await expect(locators).toHaveCount(1);
      await expect(locators.first()).toHaveText(value);
    }
  }

  async enterDetailsForRespondingToApplication(correspondence: boolean = true) {
    await this.respondToApplicationText.isVisible();
    await this.respondToApplicationText.fill('Response from claimant');

    await this.supportingMaterialRadioYes.waitFor();
    await this.supportingMaterialRadioYes.check();
    await this.clickContinue();

    await this.supportingMaterialFile.waitFor();
    await this.commonActionsHelper.uploadWithRateLimitRetry(
      this.page,
      this.supportingMaterialFile,
      await this.commonActionsHelper.createAliasPDFPayload(
      `src/test/e2e/resources/test_file/welshTest.pdf`, 'citizenResponse.pdf')
    )
    await this.uploadFileButton.waitFor();
    await this.uploadFileButton.click();
    await this.clickContinue();

    if(correspondence) {
      await this.copyToOtherPartyYes.waitFor();
      await this.copyToOtherPartyYes.check();
    } else {
      await this.copyToOtherPartyNo.waitFor();
      await this.copyToOtherPartyNo.check();
      await this.addInfoToNoOption.waitFor();
      await this.addInfoToNoOption.fill('This is Correspondence No test');
    }
    await this.clickContinue();

    await this.clickSubmitButton();

    await this.checkYourAnswerHeading.waitFor();
    await this.clickCloseAndReturn();
  }

}

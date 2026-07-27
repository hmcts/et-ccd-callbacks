import { expect, Locator, Page } from '@playwright/test';
import { BasePage } from '../basePage.ts';

export default class ResClaimantsApplicationsPage extends BasePage{

  private readonly claimantsApplicationsLink: Locator;
  private readonly claimantsApplicationsTitle: Locator;
  private readonly applicationsListTable: Locator;
  private readonly responseTextArea: Locator;
  private readonly supportingMaterialYes: Locator;
  private readonly supportingFileUpload:Locator;

  constructor(page: Page) {
    super(page);
    this.claimantsApplicationsLink = this.page.locator('a[href="/claimants-applications"]');
    this.claimantsApplicationsTitle = this.page.locator(`//caption[normalize-space()="Claimant's applications"]`);
    this.applicationsListTable = this.page.locator(`//caption[normalize-space()="Claimant's applications"]/../tbody`);
    this.responseTextArea = this.page.locator(`#responseText`);
    this.supportingMaterialYes = this.page.locator('#hasSupportingMaterial');
    this.supportingFileUpload = this.page.locator('#supportingMaterialFile');
  }

  async navigateToClaimantsApplicationsPage() {
    await this.page.waitForLoadState('load');
    await expect(this.claimantsApplicationsLink).toBeVisible();
    await this.claimantsApplicationsLink.click();
    await this.page.waitForLoadState('load');
  }

  async assertClaimantsApplicationsPageVisibile() {
    await this.page.waitForLoadState('load');
    await expect(this.claimantsApplicationsTitle).toBeVisible();
  }

  async navigateToListedClaimantsApplication(application: string) {
    await this.page.waitForLoadState('load');
    await expect(this.applicationsListTable).toBeVisible();
    const applicationLink = this.applicationsListTable.getByRole('link', { name: application });
    await expect(applicationLink).toBeVisible();
    await applicationLink.click();
    await this.page.waitForLoadState('load');
  }

  async assertClaimantsApplicationInformation(application: string, details: string[]) {
    await this.page.waitForLoadState('load');
    const heading = this.page.getByRole('heading', { name: `Application to ${application}` });
    await expect(heading).toBeVisible();

    for (const detail of details) {
      const key = detail.split('-')[0].trim();
      const value = detail.split('-')[1].trim();
      const locators = this.page.locator(`//dt[normalize-space()='${key}']/following-sibling::dd[normalize-space()='${value}']`);
      await expect(locators).toHaveCount(1);
      await expect(locators.first()).toHaveText(value);
    }
  }

  async respondToApplication(application: string, copyToCorrespondenceFlag: boolean) {
    await this.page.waitForLoadState('load');
    const heading = this.page.getByRole('heading', { name: `Application to ${application}` });
    await expect(heading).toBeVisible();

    await this.clickRespondButton();

    await this.clickContinue("", 0,false);

    await expect(this.page.getByRole('link',{name:`Provide details of your response &/or upload supporting material`})).toBeVisible();

    await expect(this.responseTextArea).toBeVisible();
    await this.responseTextArea.fill('Response from Respondent');

    await expect(this.supportingMaterialYes).toBeVisible();
    await this.supportingMaterialYes.click();

    await this.clickContinue();
    await this.commonActionsHelper.uploadWithRateLimitRetry(this.page, this.supportingFileUpload, 'src/test/e2e/resources/test_file/test.txt')
    await this.page.getByRole('button', { name: 'Upload file' }).click();
    await this.clickContinue();
    if (copyToCorrespondenceFlag) {
      await this.page.locator('#copyToOtherPartyYesOrNo').isVisible();
      await this.page.getByLabel('Yes, I confirm I want to copy').check();
    } else {
      await this.page.locator('#copyToOtherPartyYesOrNo-2').isVisible();
      await this.page.getByLabel('No, I do not want to copy this correspondence to the other party').check();
      await this.page.locator(`#copyToOtherPartyText`).fill('This is Correspondence No test');
    }
    await this.clickContinue();
    await this.page.waitForSelector('text=Check your answers');
    await this.clickSubmitButton();
  }
}

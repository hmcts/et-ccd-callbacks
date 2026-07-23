import { BasePage } from '../basePage.ts';
import { expect, Locator, Page } from '@playwright/test';
import { CaseDetailsValues } from '../../config/case-data.ts';
import { CheckYourAnswersPage } from '../helpers/CheckYourAnswersPage.ts';

export class Et3DetailsPage extends BasePage {
  private readonly et3EmploymentDetailsLink: Locator;
  private readonly et3ResponseDetailsLink: Locator;
  private readonly whichRespondentDropdown: Locator;
  private readonly saveAsDraftEt3Button: Locator;
  private readonly submitEt3FormTitle: Locator;
  readonly submitEt3FormButton: Locator;
  private readonly submitCheckBox: Locator;
  private readonly submitToTribunalButton: Locator;

  constructor(page: Page) {
    super(page);
    this.et3EmploymentDetailsLink = this.page.locator(`//a[normalize-space()='ET3 - Employment Details']`);
    this.et3ResponseDetailsLink = this.page.locator(`//a[normalize-space()='ET3 - Response Details']`);
    this.whichRespondentDropdown = this.page.locator(`#submitEt3Respondent, #et3RepresentingRespondent_0_dynamicList`);
    this.saveAsDraftEt3Button = this.page.getByRole('button', { name: 'Save ET3 as draft' });
    this.submitEt3FormTitle = this.page.getByRole('heading', { name: 'Submit ET3 Form' });
    this.submitEt3FormButton = this.page.getByRole('button', { name: 'Submit ET3 Form' });
    this.submitCheckBox = this.page.locator('#confirmEt3Submit-Yes');
    this.submitToTribunalButton = this.page.getByRole('button', { name: 'Submit ET3 to Tribunal' });

  }

  async navigateToEt3EmploymentDetailsPage() {
    await this.page.waitForLoadState('load');
    await expect(this.et3ResponseDetailsLink).toBeVisible();
    await this.et3EmploymentDetailsLink.click();
  }

  async navigateToEt3ResponseDetailsPage() {
    await this.page.waitForLoadState('load');
    await expect(this.et3EmploymentDetailsLink).toBeVisible();
    await this.et3ResponseDetailsLink.click();
  }

  async selectRespondentName(respondentName: string = CaseDetailsValues.respondentName) {
    await expect(this.whichRespondentDropdown).toBeVisible();
    await this.whichRespondentDropdown.selectOption({ label: respondentName });
  }

  async clickET3SaveAsDraftButton() {
    await this.page.waitForLoadState('load');
    await this.saveAsDraftEt3Button.scrollIntoViewIfNeeded();

    await expect(this.saveAsDraftEt3Button).toBeVisible();
    const errorHeading = this.page.locator(`#edit-case-event_error-summary-heading`);

    const maxRetries = 3;
    let attempt = 0;
    while (attempt < maxRetries) {
      await this.saveAsDraftEt3Button.click({clickCount:2, force: true });
      await this.page.waitForLoadState('load', { timeout: 4000 });
      await this.waitForSpinner();
      // Check if error is visible
      const h3Visible = await errorHeading.isVisible().catch(() => false);
      if (h3Visible) {
        const errStr = await errorHeading.textContent();
        attempt++;
        console.log(`Error '${errStr}' detected after clicking Save As draft. Retrying... (Attempt ${attempt})`);
        await this.page.waitForTimeout(2000); // wait 2s before retry
        continue;
      }
      // No error, break out of loop
      break;
    }
    if (attempt === maxRetries) {
      console.warn('Save As draft button retried maximum times, but error still present.');
      throw new Error('Save As draft button retried maximum times, but error still present.');
    }
  }

  async submitEt3Form(cyaPage: CheckYourAnswersPage) {
    await this.page.waitForLoadState('load');
    await expect(this.submitEt3FormTitle).toBeVisible();
    await this.selectRespondentName();
    await this.clickContinue();

    await expect(this.submitCheckBox).toBeVisible();
    await this.submitCheckBox.check();
    await this.clickContinue();

    await cyaPage.assertCheckYourAnswersPage({
      tableName: 'Check your answers',
      rows: [
        { cellItem: 'Select which respondent this ET3 is for', value: CaseDetailsValues.respondentName },
        { cellItem: 'Do you want to submit this ET3?', value: 'Yes - I want to submit this ET3' },
      ],
    });

    await expect(this.submitToTribunalButton).toBeVisible();
    await this.submitToTribunalButton.click();
    await this.clickCloseAndReturn();
  }

  async assertSubmitEt3ButtonNotVisible() {
    await this.page.waitForLoadState('load');
    await expect(this.submitEt3FormButton).not.toBeVisible();
  }
}

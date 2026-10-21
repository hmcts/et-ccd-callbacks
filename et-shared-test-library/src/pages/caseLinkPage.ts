import { BasePage } from './basePage';
import { Locator, Page, expect } from '@playwright/test';

export class CaseLinkPage extends BasePage {
  private readonly createCaseLinkButton: Locator;
  private readonly nextButton: Locator;
  private readonly caseLinkProposeButton: Locator;
  private readonly beforeYouStartCaseLinking: Locator;
  private readonly caseReferenceField: Locator;
  private readonly bailOption: Locator;
  private readonly samePartyOption: Locator;
  private readonly sharedEvidenceOption: Locator;
  private readonly guardianOption: Locator;
  private readonly linkedForHearingOption: Locator;
  private readonly progressedAsPartofLeadCase: Locator;
  private readonly removeLinkedCase: Locator;
  private readonly submitButtonLink: Locator;
  private readonly checkedLinkedCases: Locator;
  private readonly cyaBody: Locator;
  private readonly unlinkCasesSuccessMessageAlert: Locator;

  constructor(page: Page) {
    super(page);
    this.createCaseLinkButton = this.page.getByRole('button', {name: 'Create Case Link'})
    this.nextButton = page.locator('#next-button');
    this.caseLinkProposeButton = page.locator('#propose');
    this.beforeYouStartCaseLinking = page.locator('.govuk-heading-xl');
    this.caseReferenceField = page.locator('#width-20');
    this.bailOption = page.locator('#CLRC010');
    this.samePartyOption = page.locator('#CLRC003');
    this.sharedEvidenceOption = page.locator('#CLRC008');
    this.guardianOption = page.locator('#CLRC006');
    this.linkedForHearingOption = page.locator('#CLRC017');
    this.progressedAsPartofLeadCase = page.locator('#CLRC016');
    this.removeLinkedCase = page.locator('//a[.="Remove"]');
    this.submitButtonLink = page.locator('//button[@class="button"]');
    this.checkedLinkedCases = page.locator('[name="linkedCases"]');
    this.cyaBody = page.locator('#fieldset-case-data');
    this.unlinkCasesSuccessMessageAlert = page.locator('//div[@class="alert-message"]');
  }

  async checksCaseLinkStartingPage() {
    await expect(this.beforeYouStartCaseLinking).toContainText('Before you start', { timeout: 10000 });
    await expect(this.page.getByText('Before you start')).toBeVisible();
    await expect(this.page.getByText('If a group of linked cases has a lead case, you must start from the lead case.')).toBeVisible();
    await expect(this.page.getByText('If the cases to be linked has no lead, you can start the linking journey from any of those cases.')).toBeVisible();
    await this.delay(3000);
    await this.createCaseLinkButton.click();
  }

  async enterCaseLinkReferenceWithHearing(submissionReference: string) {
    await this.caseLinkProposeButton.waitFor({ timeout: 10000 });
    await expect(this.page.getByText('Enter case reference')).toBeVisible();
    await expect(this.page.getByText('Why should these cases be linked?')).toBeVisible();
    await this.caseReferenceField.fill(submissionReference);
    await this.bailOption.check();
    await this.samePartyOption.check();
    await this.sharedEvidenceOption.check();
    await this.guardianOption.check();
    await this.linkedForHearingOption.check();
    await this.progressedAsPartofLeadCase.check();
    await this.delay(3000);
    await this.caseLinkProposeButton.click();
    await this.submitButtonLink.click();
    await this.submitButtonLink.waitFor();
    await expect(this.page.getByText('Check your answers')).toBeVisible();
    await this.submitButtonLink.click();
  }

  async enterCaseLinkReferenceWithoutHearing(submissionReference: string) {
    await this.caseLinkProposeButton.waitFor({ timeout: 20000 });
    await expect(this.page.getByText('Enter case reference')).toBeVisible();
    await expect(this.page.getByText('Why should these cases be linked?')).toBeVisible();
    await this.caseReferenceField.fill(submissionReference);
    await this.bailOption.check();
    await this.samePartyOption.check();
    await this.sharedEvidenceOption.check();
    await this.guardianOption.check();
    await this.progressedAsPartofLeadCase.check();
    await this.caseLinkProposeButton.click();
    await this.removeLinkedCase.waitFor({ timeout: 10000 });
    await this.caseReferenceField.fill(submissionReference);
    await this.bailOption.check();
    await this.samePartyOption.check();
    await this.sharedEvidenceOption.check();
    await this.guardianOption.check();
    await this.linkedForHearingOption.check();
    await this.progressedAsPartofLeadCase.check();
    await this.caseLinkProposeButton.click();
    await this.page.waitForTimeout(2000);
    await this.submitButtonLink.click();
    await this.submitButtonLink.waitFor();
    await expect(this.page.getByText('Check your answers')).toBeVisible();
    await this.submitButtonLink.click();
  }

  async unlinkedCase() {
    await this.nextButton.waitFor();
    await this.nextButton.click();
    await this.checkedLinkedCases.waitFor();
    await this.checkedLinkedCases.check();
    await this.nextButton.click();
    await this.cyaBody.waitFor({ timeout: 15000 });
    await expect(this.page.getByText('Check your answers')).toBeVisible();
    await expect(this.page.getByText('Cases to unlink')).toBeVisible();
    await this.submitButtonLink.click();
    await this.page.waitForTimeout(2000);
    await this.submitButtonLink.click();
    await this.unlinkCasesSuccessMessageAlert.waitFor({ timeout: 15000 });
  }
}

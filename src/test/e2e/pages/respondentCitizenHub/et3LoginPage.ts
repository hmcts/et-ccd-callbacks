import { expect, Page, Locator } from '@playwright/test';
import LoginPage from '../loginPage.ts';
import { config, UserCredentials, users } from '../../config/config.dynamic.ts';

export default class Et3LoginPage extends LoginPage {
  private readonly returnToExistingResponse: Locator;
  private readonly startNow: Locator;
  private readonly respondToNewClaim: Locator;
  private readonly caseNumber: Locator;
  private readonly submissionRefNumber: Locator;
  private readonly respName: Locator;
  private readonly claimantFirstName: Locator;
  private readonly claimantLastName: Locator;
  private readonly appointLegalRepLink: Locator;
  private readonly errorMessage = (text: string): Locator => this.page.getByText(text).first();
  private readonly etAccountRadio: Locator;
  private readonly signInOptionLink: Locator;

  constructor(page: Page) {
    super(page);
    this.returnToExistingResponse = page.locator('[href="/return-to-existing-response?lng=en"]');
    this.startNow = page.locator('[href="/case-number-check"]');
    this.respondToNewClaim = page.locator('[href="/case-number-check?lng=en&redirect=selfAssignment"]');
    this.caseNumber = page.locator('#ethosCaseReference');
    this.submissionRefNumber = page.locator('#caseReferenceId');
    this.respName = page.locator('#respondentName');
    this.claimantFirstName = page.locator('#claimantFirstName');
    this.claimantLastName = page.locator('#claimantLastName');
    this.appointLegalRepLink = page.locator('[href="/appoint-legal-representative"]');
    this.etAccountRadio = page.locator(`#return_number_or_account-2`);
    this.signInOptionLink = page.locator('//a[@href="/enter-email"]');
  }

  async processRespondentLogin(user: UserCredentials) {
    await this.page.goto(config.etSyrUiUrl);
    await expect(this.page.locator('h1')).toHaveText(/Introduction/);
    await this.returnToExistingResponse.click();
    await this.page.waitForTimeout(1000);
    await this.etAccountRadio.click();
    await this.clickContinue();
    const isNewIdam = await this.signInOptionLink.isVisible().catch(() => false);
    if(isNewIdam) { // Remove if condition when new IDAM is rolled out to all environments
      await this.signInOptionLink.click();
      await this.page.waitForLoadState('load');
    }
    await this.processLogin(user, config.etSyrUiUrl);
    await this.page.waitForLoadState('load');
  }

  async processRespondentLoginForExistingCase(user: UserCredentials, caseNumber: string) {
    await this.processRespondentLogin(user);
    await expect(this.page.locator('#main-content')).toContainText('ET3 Responses');
    await expect(this.respondToNewClaim).toBeVisible();
    await this.page.getByLabel('view ' + caseNumber.toString() + ':').click();
  }

  async replyToNewClaim(
    submissionRef: string,
    caseNumber: string,
    respName: string,
    firstName: string,
    lastName: string,
  ) {
    await this.page.goto(config.etSyrUiUrl + '/case-list');
    await expect(this.page.locator('#main-content')).toContainText('ET3 Responses');
    await this.respondToNewClaim.click();
    await this.enterCaseNumberDetail(caseNumber);
    await this.enterCaseDetails(submissionRef, respName, firstName, lastName);

    await expect.poll(
      async () => {
        await this.checkAndSubmitPage(caseNumber);
        const error = this.page.getByText('Unable to assign role. Please try again later');
        return error.isVisible().catch(() => false);
      },
      {
        intervals: [5_000],
        timeout: 120_000,
      }
    ).toBeFalsy()
    await this.navigateToCase(caseNumber, submissionRef);
  }

  async enterCaseNumberDetail(caseNumber: string) {
    await expect(this.page.locator('h1')).toContainText('Case Number');
    await this.caseNumber.fill(caseNumber.toString());
    await this.clickContinue();
  }

  async enterCaseDetails(submissionRef: string, respName: string, firstName: string, lastName: string) {
    await expect(this.page.locator('h1')).toContainText('Case Details');
    await this.submissionRefNumber.fill(submissionRef.toString());
    await this.respName.fill(respName);
    await this.claimantFirstName.fill(firstName);
    await this.claimantLastName.fill(lastName);
    await this.page.waitForTimeout(5000);
    await this.clickContinue();
  }

  async checkAndSubmitPage(caseNumber: string) {
    await expect(this.page.locator('h1')).toContainText('Check and submit');
    await this.page.locator('#confirmation').check();
    await this.clickSubmitButton();
  }

  async navigateToCase(caseNumber: string, caseId: string) {
    await this.page.goto(config.etSyrUiUrl + '/case-list');
    await this.page.waitForLoadState('load');
    await expect(this.page.locator('#main-content')).toContainText('ET3 Responses');
    await expect(this.respondToNewClaim).toBeVisible();
    await this.page.getByLabel('view ' + caseNumber + ': '+caseId.toString()).first().click();
  }

  async validateClaimantDetailsInRespondentApp(firstname: string, lastname: string) {
    await expect(this.page.locator('#main-content')).toContainText('Case overview');
    await this.page.getByText('View claimant contact details').click();
    await expect(this.page.locator('dl')).toContainText(firstname + ' ' + lastname);
    await expect(this.page.locator('dl')).toContainText('Address');
    await expect(this.page.locator('dl')).toContainText('Email');
  }

  async validateNocNotificationBanner() {
    await expect(this.page.locator('h3')).toContainText('You are now being legally represented by');
  }

  async stopLegalRepRepresentation() {
    await this.page.getByText('Change my legal representative').click();
    await expect(this.page.locator('legend')).toContainText('Do you want to change your legal representative?');
    await this.page.locator('#legalRep-2').click();
    await this.clickSubmitButton();
    await expect(this.page.locator('#main-content')).toContainText('Case overview');
    await expect(this.page.getByLabel('Important').filter({ hasText: 'You are no longer legally represented.' })).toBeVisible();
    await expect(this.appointLegalRepLink).toBeVisible();
  }

  async replyToClaimAsNewRespondent(
    submissionRef: string,
    caseNumber: string,
    respName: string,
    firstName: string,
    lastName: string,
  ) {
    await expect(this.page.locator('#main-content')).toContainText('ET3 Responses');
    await this.respondToNewClaim.click();
    await this.enterCaseNumberDetail(caseNumber);
    await this.enterCaseDetails(submissionRef, respName, firstName, lastName);
    await expect(this.page.locator('h1')).toContainText('Check and submit');
    await this.page.locator('#confirmation').check();
    await this.clickSubmitButton();
  }

  async assertErrorMessageIsVisible(message: string) {
    await expect(this.errorMessage(message)).toBeVisible();
  }
}

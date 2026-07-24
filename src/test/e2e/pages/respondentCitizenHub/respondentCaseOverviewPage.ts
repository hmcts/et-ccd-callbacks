import { expect, Page, Locator } from '@playwright/test';
import {BasePage} from "../basePage.ts";

export default class RespondentCaseOverviewPage extends BasePage {
  private readonly et1FormLink: Locator;
  private readonly et1FormSubLink: Locator;
  private readonly claimantContactDetails: Locator;
  private readonly contactApplicationFileUpload: Locator;
  private readonly allNotificationsFromTheTribunalLink: Locator;
  private readonly tribunalResponseField: Locator;
  private readonly supportingMaterialRadioYes: Locator;
  private readonly supportingFileUpload: Locator;

  constructor(page: Page) {
    super(page);
    this.et1FormLink = this.page.locator('[href="/claimant-et1-form?lng=en"]');
    this.et1FormSubLink = this.page.locator('[href="/claimant-et1-form-details?lng=en"]');
    this.claimantContactDetails = this.page.locator('[href="/claimant-contact-details"]');
    this.contactApplicationFileUpload = this.page.locator(`#contactApplicationFile`);
    this.allNotificationsFromTheTribunalLink = this.page.locator('[href="/notifications"]');
    this.tribunalResponseField = this.page.locator(`#responseText`);
    this.supportingMaterialRadioYes = this.page.locator('#hasSupportingMaterial');
    this.supportingFileUpload = this.page.locator('#supportingMaterialFile');

  }

  async validateRespondentCaseOverviewPage() {
    await expect(this.page.locator('#main-content')).toContainText('Case overview');
    await expect(this.page.locator('h3')).toContainText('The tribunal has acknowledged a claim against');
    await this.page.getByRole('link', { name: 'Respond to the claim' }).waitFor();
    await this.page.getByRole('link', { name: 'Respond to the claim' }).click();
    await this.clickStartNow();
  }

  async respondentMakeApplication(option: string, copyToCorrespondenceFlag: boolean) {
    await expect(this.page.locator('#main-content')).toContainText('Case overview');
    await expect(this.page.locator('h3')).toContainText('The tribunal has acknowledged a claim against');
    await this.page.getByRole('link', { name: 'Contact the tribunal about my case' }).click();
    await expect(this.page.locator('#contact-options')).toContainText('Show all sections');
    switch (option) {
      case 'TypeA':
        await this.page.getByRole('button', { name: 'Apply to amend my response ,' }).click();
        await this.page.getByRole('link', { name: 'Apply to amend my response' }).click();
        break;
      case 'TypeB':
        await this.page.getByRole('button', { name: 'I want to change my personal details ,' }).click();
        await this.page.getByRole('link', { name: 'I want to change my personal details' }).click();
        break;
      case 'TypeC':
        await this.page.getByRole('button', { name: 'Order a witness to give evidence ,' }).click();
        await this.page.getByRole('link', { name: 'Order a witness to give evidence' }).click();
        break;
      default:
        throw new Error('... Incorrect input, select correct application type');
    }
    await this.page.locator('#contactApplicationText').isVisible();
    await this.commonActionsHelper.uploadWithRateLimitRetry(this.page, this.contactApplicationFileUpload, 'src/test/e2e/resources/test_file/test.txt')
    await this.page.getByRole('button', { name: 'Upload file' }).click();
    await expect(this.page.locator('#contactApplicationFile-hint')).toContainText('You have previously uploaded: test.txt');
    await this.page.locator('#contactApplicationText').fill('this is respondent application');
    await this.clickContinue();
    if (copyToCorrespondenceFlag) {
      await this.page.locator('#copyToOtherPartyYesOrNo').isVisible();
      await this.page.getByLabel('Yes, I confirm I want to copy').check();
      await this.clickContinue();
    } else {
      if(!await this.page.getByRole('heading', {name: 'Check your answers'}).isVisible()) {
        await this.page.locator(`#copyToOtherPartyYesOrNo-2`).isVisible();
        await this.page.locator(`#copyToOtherPartyYesOrNo-2`).check();
        await this.page.locator(`#copyToOtherPartyText`).fill('Correspondence No Respondent');
        await this.clickContinue();
      }
    }
    await this.page.waitForSelector('text=Check your answers');
    await this.page.waitForLoadState('load');
    await this.clickSubmitButton();
    await expect(this.page.locator('h1')).toContainText('You have sent your application to the tribunal');
    await this.clickCloseAndReturn();
    await expect(this.page.locator('#main-content')).toContainText('Your request and applications');
  }

  async unrepresentedRespondentMakeApplication(option: string, copyToCorrespondenceFlag: boolean) {
    await expect(this.page.locator('#main-content')).toContainText('Case overview');
    await expect(this.page.locator('h3')).toContainText('The tribunal has acknowledged a claim against');
    await this.page.getByRole('link', { name: 'Contact the tribunal about my case' }).click();
    await expect(this.page.locator('#contact-options')).toContainText('Show all sections');
    switch (option) {
      case 'Rule92':
        await this.page.getByRole('button', { name: 'I want to change my personal details ,' }).click();
        await this.page.getByRole('link', { name: 'I want to change my personal details' }).click();
        break;
      default:
        throw new Error('... Incorrect input, select correct application type');
    }
    await this.page.locator('#contactApplicationText').isVisible();
    await this.commonActionsHelper.uploadWithRateLimitRetry(this.page, this.contactApplicationFileUpload, 'src/test/e2e/resources/test_file/test.txt')
    await this.page.getByRole('button', { name: 'Upload file' }).click();
    await expect(this.page.locator('#contactApplicationFile-hint')).toContainText('You have previously uploaded: test.txt');
    await this.page.locator('#contactApplicationText').fill('this is unrepresented respondent application');
    await this.clickContinue();
    if (copyToCorrespondenceFlag) {
      await this.page.locator('#copyToOtherPartyYesOrNo').isVisible();
      await this.page.getByLabel('Yes, I confirm I will copy this correspondence to the other party to satisfy the Employment Tribunal Rules of Procedure.').check();
      await this.clickContinue();
      await this.page.waitForSelector('text=Check your answers');
      await this.page.getByRole('button', { name: 'Store application' }).click();
      await expect(this.page.locator('h1')).toContainText('You have stored your application');
      await this.clickCloseAndReturn();
    } else {
      await this.page.locator('#copyToOtherPartyYesOrNo-2').isVisible();
      await this.page.getByLabel('No, I do not want to copy this correspondence to the other party.').check();
      await this.page.locator('#copyToOtherPartyText').fill('This is Correspondence No test');
      await this.clickContinue();
      await this.page.waitForSelector('text=Check your answers');
      await this.clickSubmitButton();
      await expect(this.page.locator('h1')).toContainText('You have sent your application to the tribunal');
      await this.clickCloseAndReturn();
    }
    await expect(this.page.locator('#main-content')).toContainText('Your request and applications');
  }

  async unrepresentedRespondentValidateApplication(copyToCorrespondenceFlag: boolean) {
    if (copyToCorrespondenceFlag) {
      await expect(this.page.getByText('You have stored correspondence which you have not submitted to the tribunal')).toBeVisible();
      await this.page.getByRole('link', { name: 'Your request and applications' }).click();
      await expect(this.page.getByRole('caption')).toContainText('Your applications to the tribunal');
      await expect(this.page.locator('tbody')).toContainText('Stored');
      await this.page.getByRole('link', { name: 'Change my personal details' }).click();
      await expect(this.page.locator('h1')).toContainText('Change my personal details');
      await this.page.locator('#confirmCopied').check();
      await this.clickSubmitButton();
      await expect(this.page.locator('h1')).toContainText('You have sent your application to the tribunal');
      await this.clickCloseAndReturn();
      await expect(this.page.locator('#main-content')).toContainText('Your request and applications');
      await this.page.getByRole('link', { name: 'Your request and applications' }).click();
      await expect(this.page.getByRole('caption')).toContainText('Your applications to the tribunal');
      await expect(this.page.locator('tbody')).toContainText('In progress');
    } else {
      await this.page.getByRole('link', { name: 'Your request and applications' }).click();
      await expect(this.page.getByRole('caption')).toContainText('Your applications to the tribunal');
      await expect(this.page.locator('tbody')).toContainText('In progress');
    }
  }

  async validateApplication(option: string) {
    await this.page.getByRole('link', { name: 'Your request and applications' }).click();
    await expect(this.page.getByRole('caption')).toContainText('Your applications to the tribunal');
    await expect(this.page.locator('tbody')).toContainText('In progress');
    switch (option) {
      case 'TypeA':
        await this.page.getByRole('link', { name: 'Amend my response' }).click();
        break;
      case 'TypeB':
        await this.page.getByRole('link', { name: 'Change my personal details' }).click();
        break;
      case 'TypeC':
        await this.page.getByRole('link', { name: 'Order a witness to attend' }).click();
        break;
      default:
        throw new Error('... Incorrect input, select correct application type');
    }
    await expect(this.page.locator('dl')).toContainText('Respondent');
  }

  async validateRespondentClaimantContactDetailsPage() {
    await expect(this.page.locator('#main-content')).toContainText('Case overview');
    await this.claimantContactDetails.click();
    await expect(this.page.locator('dl')).toContainText('Email');
  }

  async validateNotificationBanner(notificationName: string) {
    await this.page.waitForLoadState('load');
    await expect(this.page.getByRole('heading', {name: 'The tribunal has sent you a notification', level: 3})).toBeVisible();
    await expect(this.page.getByRole('link', { name: 'View the notification - ' + notificationName })).toBeVisible();
  }

  async respondToTribunalsNotification(notificationName: string, copyToCorrespondenceFlag: boolean = true) {
    await this.page.waitForLoadState('load');
    await expect(this.allNotificationsFromTheTribunalLink).toBeVisible();
    await this.allNotificationsFromTheTribunalLink.click();

    await this.page.waitForLoadState('load');
    const notification = this.page.getByRole('link', { name: notificationName });
    await expect(notification).toBeVisible();
    await notification.click();

    await this.page.waitForLoadState('load');
    await this.clickRespondButton();

    await this.tribunalResponseField.fill('Response to tribunal by Claimant');

    await this.supportingMaterialRadioYes.waitFor();
    await this.supportingMaterialRadioYes.check();

    await this.commonActionsHelper.uploadWithRateLimitRetry(
      this.page,
      this.supportingFileUpload,
      `src/test/e2e/resources/test_file/welshTest.pdf`
    )
    await this.page.getByRole('button', { name: 'Upload file' }).click();
    await this.page.waitForLoadState('load');
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
    await this.clickSubmitButton();
  }

}

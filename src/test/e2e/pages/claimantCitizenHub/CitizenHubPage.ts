import { BasePage } from "../basePage.ts";
import { expect, Locator, Page } from '@playwright/test';
import { config } from '../../config/config.dynamic.ts';

export default class CitizenHubPage extends BasePage {
  private readonly caseOverviewPageTitle: Locator;
  readonly caseNumberText: Locator;
  private readonly contactTribunalLink: Locator;
  private readonly appointALegalRepLink: Locator;
  private readonly haveYouCompletedThisSectionHeading: Locator;
  private readonly cuiRespondButton: Locator;
  private readonly responseTextElement: Locator;
  private readonly providingMaterialYes: Locator;
  private readonly addTextToResponse: Locator;
  private readonly supportingMaterialAttachment: Locator;
  private readonly uploadButton: Locator;
  private readonly contactTribunalAboutMyCase: Locator;
  private readonly changeMyLegalRep: Locator;
  private readonly linkToET3Response: Locator;
  private readonly contactTribunalLinkRegistered: Locator;
  private readonly appointLegalRepLink: Locator;
  private readonly openAllApplicationType: Locator;
  private readonly welshContactTribunalLinkRegistered: Locator;
  private readonly showAllApplicationType: Locator;
  private readonly withdrawClaimLink: Locator;
  private readonly applicationTextField: Locator;
  private readonly changePersonalDetail: Locator;
  private readonly postponeMyHearing: Locator;
  private readonly revokeAnOrder: Locator;
  private readonly reconsiderDecision: Locator;
  private readonly amendClaim: Locator;
  private readonly orderRespondent: Locator;
  private readonly orderWitness: Locator;
  private readonly respondentNotComplied: Locator;
  private readonly restrictPublicity: Locator;
  private readonly strikeOutResponse: Locator;
  private readonly reconsiderJudgment: Locator;
  private readonly somethingElse: Locator;
  private readonly submitHearingDocument: Locator;
  private readonly startPreparingHearingDoc: Locator;
  private readonly hearingDocAgreeDoc: Locator;
  private readonly firstListedCase: Locator;
  private readonly myDocumentOption: Locator;
  private readonly witnessStatementOnly: Locator;
  private readonly uploadHearingDocButton: Locator;
  private readonly uploadHearingFile: Locator;
  private readonly backButton: Locator;
  private readonly quidanceTextPayload: Locator;
  private readonly changeYourDocument: Locator;
  private readonly yesOptionOnRule92: Locator;
  private readonly noOptionOnRule92: Locator;
  private readonly addInfoToNoOption: Locator;
  private readonly submitApplicationButton: Locator;
  private readonly returntoCUIcaseOverviewButton: Locator;
  private readonly notificationFlagBefore: Locator;
  private readonly notificationLink: Locator;
  private readonly seeNotificationDetailsLink: Locator;
  private readonly sendNotifButton: Locator;
  private readonly tribunalResponseField: Locator;
  private readonly noSupportingMaterialOption: Locator;
  private readonly responseSubmitButton: Locator;
  private readonly yesRule92Button: Locator;
  private readonly closeStoredApplication: Locator;
  private readonly returnOverviewButton: Locator;
  private readonly notificationFlagAfter: Locator;
  private readonly viewCorrespondenceLink: Locator;
  private readonly confirmedCopyCheckBox: Locator;
  private readonly submit: Locator;
  private readonly respondToApplicationText: Locator;
  private readonly supportingMaterialRadioYes: Locator;
  private readonly supportingMaterialFile: Locator;
  private readonly uploadFielButton: Locator;
  private readonly copyToOtherPartyYesOrNo: Locator;
  private readonly checkYourAnswerHeading: Locator;
  private readonly responseHeading: Locator;
  private readonly respondentApplication: Locator;
  private readonly respondentContactDetailsLink: Locator;

  constructor(page: Page) {
    super(page);
    this.caseOverviewPageTitle = this.page.locator(`xpath=//h2[contains(normalize-space(),'Case overview')]`);
    this.caseNumberText = this.page.locator('#caseNumber');
    this.contactTribunalLink = this.page.locator(`xpath=//a[normalize-space()='Contact the tribunal about my case']`);
    this.appointALegalRepLink = this.page.locator(`xpath=//a[normalize-space()='Appoint a legal representative']`);
    this.haveYouCompletedThisSectionHeading = this.page.locator(
      `fieldset:has(legend:has(h1:text("Have you completed this section?")))`,
    );
    this.cuiRespondButton = this.page.locator('#respond-button');
    this.responseTextElement = this.page.locator('.govuk-label--m');
    this.providingMaterialYes = this.page.locator('#supporting-material-yes-no');
    this.addTextToResponse = this.page.locator('#respond-to-application-text');
    this.supportingMaterialAttachment = this.page.locator('#supportingMaterialFile');
    this.uploadButton = this.page.locator('#upload');
    this.contactTribunalAboutMyCase = this.page.locator('[href="/contact-the-tribunal"]');
    this.changeMyLegalRep = this.page.locator('[href="/change-legal-representative"]');
    this.linkToET3Response = this.page.locator('[href="/case-document/response-from-respondent"]');
    this.contactTribunalLinkRegistered = this.page.locator('[href="/contact-the-tribunal"]');
    this.appointLegalRepLink = this.page.locator('[href="/appoint-legal-representative"]');
    this.openAllApplicationType = this.page.locator('//span[@class="govuk-accordion__show-all-text"]');
    this.welshContactTribunalLinkRegistered = this.page.locator('[href="/contact-the-tribunal?lng=cy"]');
    this.showAllApplicationType = this.page.locator('#contact-options');
    this.withdrawClaimLink = this.page.locator('[href="/contact-the-tribunal/withdraw?lng=en"]');
    this.applicationTextField = this.page.locator('#Contact-Application-Text');
    this.changePersonalDetail = this.page.locator('[href="/contact-the-tribunal/change-details?lng=en"]');
    this.postponeMyHearing = this.page.locator('[href="/contact-the-tribunal/postpone"]');
    this.revokeAnOrder = this.page.locator('[href="/contact-the-tribunal/vary"]');
    this.reconsiderDecision = this.page.locator('[href="/contact-the-tribunal/reconsider-decision"]');
    this.amendClaim = this.page.locator('[href="/contact-the-tribunal/amend"]');
    this.orderRespondent = this.page.locator('[href="/contact-the-tribunal/respondent"]');
    this.orderWitness = this.page.locator('[href="/contact-the-tribunal/witness"]');
    this.respondentNotComplied = this.page.locator('[href="/contact-the-tribunal/non-compliance"]');
    this.restrictPublicity = this.page.locator('[href="/contact-the-tribunal/publicity"]');
    this.strikeOutResponse = this.page.locator('[href="/contact-the-tribunal/strike"]');
    this.reconsiderJudgment = this.page.locator('[href="/contact-the-tribunal/reconsider-judgement"]');
    this.somethingElse = this.page.locator('[href="/contact-the-tribunal/other"]');
    this.submitHearingDocument = this.page.locator('[href="/prepare-documents?lng=en"]');
    this.startPreparingHearingDoc = this.page.locator('//a[contains(.,"Start now")]');
    this.hearingDocAgreeDoc = this.page.locator('#bundlesRespondentAgreedDocWith');
    this.firstListedCase = page.locator('#about-hearing-documents1');
    this.myDocumentOption = page.locator('[value="My hearing documents only"]');
    this.witnessStatementOnly = page.locator('[value="Witness statements only"]');
    this.uploadHearingDocButton = page.locator('#hearingDocument');
    this.uploadHearingFile = page.locator('#upload');
    this.backButton = page.locator('//a[.="Back"]');
    this.quidanceTextPayload = page.locator('.govuk-template__body .govuk-grid-column-two-thirds > .govuk-body');
    this.changeYourDocument = page.locator('//a[contains(.,"Change Your documents")]');
    this.yesOptionOnRule92 = page.locator('#copyToOtherPartyYesOrNo');
    this.noOptionOnRule92 = page.locator('#copyToOtherPartyYesOrNo-2');
    this.addInfoToNoOption = page.locator('#copyToOtherPartyText');
    this.submitApplicationButton = page.locator('#main-form-submit');
    this.returntoCUIcaseOverviewButton = page.locator('//a[contains(.,"Close and return to case overview")]');
    this.notificationFlagBefore = page.locator('.govuk-tag--red');
    this.notificationLink = page.locator('[href="/tribunal-orders-and-requests"]');
    this.seeNotificationDetailsLink = page.locator('td:nth-of-type(2) > .govuk-link');
    this.sendNotifButton = page.locator('td:nth-of-type(2) > .govuk-link');
    this.tribunalResponseField = page.locator('#response-text');
    this.noSupportingMaterialOption = page.locator('[for="supporting-material-yes-no-2"]');
    this.responseSubmitButton = page.locator('#main-form-submit');
    this.yesRule92Button = page.locator('[for="copyToOtherPartyYesOrNo-2"]');
    this.closeStoredApplication = page.locator('#main-content .govuk-button');
    this.returnOverviewButton = page.locator('.govuk-template__body > .govuk-width-container > .govuk-button-group > .govuk-button');
    this.notificationFlagAfter = page.locator('.app-task-list > li:nth-of-type(5) .govuk-tag');
    this.viewCorrespondenceLink = page.locator('//a[.="View correspondence"]');
    this.confirmedCopyCheckBox = page.locator('#confirmCopied');
    this.submit = page.locator('[type="submit"]');
    this.respondToApplicationText = page.locator('#respond-to-application-text');
    this.supportingMaterialRadioYes = page.locator('#supporting-material-yes-no');
    this.supportingMaterialFile = page.locator('#supportingMaterialFile');
    this.uploadFielButton = page.locator('#upload');
    this.copyToOtherPartyYesOrNo = page.locator('#copyToOtherPartyYesOrNo');
    this.checkYourAnswerHeading = page.locator('//h1[@class="govuk-panel__title"]');
    this.responseHeading = page.locator('//h2[@class="govuk-summary-list__key govuk-heading-m govuk-!-margin-top-1"]');
    this.respondentApplication = page.locator('[href="/respondent-applications"]');
    this.respondentContactDetailsLink = page.locator('[href="/respondent-contact-details"]');
  };

  async navigateToSubmittedCaseOverviewOfClaimant(submissionReference: string) {
    await this.page.goto(config.etSyaUiUrl + '/citizen-hub/' + submissionReference);
    await this.page.waitForLoadState('load');
  }

  async citizenHubCaseOverviewPage(caseNumber: string) {
    await expect(this.caseOverviewPageTitle).toBeVisible();
    await expect(this.caseNumberText).toHaveText('Case number ' + caseNumber);
  }

  async navigateToContactTheTribunalPage() {
    await expect(this.contactTribunalLink).toBeVisible();
    await this.contactTribunalLink.click();
    await this.page.waitForLoadState('load');
  }

  async confirmHaveYouCompletedThisSection(option: string = 'Yes') {
    await this.page.waitForLoadState('load');
    await expect(this.haveYouCompletedThisSectionHeading).toBeVisible();
    switch (option) {
      case 'Yes':
        const yesOption = this.haveYouCompletedThisSectionHeading.locator('input[type="radio"][value="Yes"]');
        await expect(yesOption).toBeVisible();
        await yesOption.check();
        break;
      case 'No':
        const noOption = this.haveYouCompletedThisSectionHeading.locator('input[type="radio"][value="No"]');
        await expect(noOption).toBeVisible();
        await noOption.check();
        break;
      default:
        throw new Error(`Option: ${option} is not recognized. Please select either 'Yes' or 'No'.`);
    }
    await this.saveAndContinueButton();
  }

  async appointLegalRep(caseNumber: string, submissionReference: string) {
    await expect(this.appointALegalRepLink).toBeVisible();
    await this.appointALegalRepLink.click();
    await this.page.waitForLoadState('load');

    const caseNumberLocator = this.page.locator(`xpath=//dt[normalize-space()='Online case reference number']/following-sibling::dd`);
    await expect(caseNumberLocator).toBeVisible();
    const caseRefText = await caseNumberLocator.textContent();
    expect(caseRefText).toBeDefined();
    expect(caseRefText?.trim()).toEqual(submissionReference.toString());

    const claimantNameLocator = this.page.locator(`xpath=//dt[normalize-space()='Claimant name']/following-sibling::dd`);
    await expect(claimantNameLocator).toBeVisible();
    const claimantNameText = await claimantNameLocator.textContent();
    expect(claimantNameText).toBeDefined();

    const tribunalCaseNumberLocator = this.page.locator(`xpath=//dt[normalize-space()='Tribunal case number']/following-sibling::dd`);
    await expect(tribunalCaseNumberLocator).toBeVisible();
    const tribunalCaseNumberText = await tribunalCaseNumberLocator.textContent();
    expect(tribunalCaseNumberText).toBeDefined();
    expect(tribunalCaseNumberText?.trim()).toEqual(caseNumber);

    await expect(this.page.getByRole('heading', { name: 'Download documents'})).toBeVisible();

    const documents = this.page.locator(`xpath=//a[@href='/all-documents']`);
    await expect(documents).toBeVisible();
    await documents.click();

    const docName = `ET1 - ${claimantNameText?.trim()}.pdf`;
    const documentLinkLocator = this.page.locator(`xpath=//a[normalize-space()='${docName}']`);
    await expect(documentLinkLocator).toBeVisible();

    return { caseRef: caseRefText??'', claimantName: claimantNameText??'', tribunalCaseNumber: tribunalCaseNumberText??'' };
  }

  async respondToAnApplication() {
    await this.page.getByRole('link', { name: 'Respond to the application' }).isVisible();
    await this.page.getByRole('link', { name: 'Respond to the application' }).click();

    await this.cuiRespondButton.waitFor();
    await this.cuiRespondButton.click();

    await this.respondToApplicationText.waitFor();
    await this.respondToApplicationText.fill('This is response of an application');

    await this.supportingMaterialRadioYes.waitFor();
    await this.supportingMaterialRadioYes.check();
    await this.clickContinue();

    await this.supportingMaterialFile.waitFor();
    await this.commonActionsHelper.uploadWithRateLimitRetry(
      this.page,
      this.supportingMaterialFile,
      `playwrighte2e/resources/test_file/welshTest.pdf`
    )

    await this.uploadFielButton.waitFor();
    await this.uploadFielButton.click();
    await this.clickContinue();

    await this.copyToOtherPartyYesOrNo.waitFor();
    await this.copyToOtherPartyYesOrNo.check();
    await this.clickContinue();

    await this.clickSubmitButton();

    await this.clickCloseAndReturn();
  }

  async validateResponseOfResponse() {
    await this.page.getByRole('link', { name: 'View the response' }).isVisible();
    await this.page.getByRole('link', { name: 'View the response' }).click();
    await this.delay(3000);
    await expect(this.page.locator('body')).toContainText('Response of an application By Caseworker');
  }
  async validateRecordDecisionBanner() {
    await this.page.getByRole('link', { name: 'View the decision' }).click();
    await expect(this.page.locator('body')).toContainText('Decision');
  }

  async respondToRespondentApplication(option: string) {
    await this.page.getByRole('link', { name: "Respondent's applications" }).isVisible();
    await this.page.getByRole('link', { name: "Respondent's applications" }).click();
    await this.delay(2000);

    switch (option) {
      case 'TypeA':
        await this.page.getByRole('link', { name: 'Amend response' }).isVisible();
        await this.page.getByRole('link', { name: 'Amend response' }).click();
        break;
      case 'TypeB':
        await this.page.getByRole('link', { name: 'Change personal details' }).isVisible();
        await this.page.getByRole('link', { name: 'Change personal details' }).click();
        break;
      default:
        throw new Error('... Incorrect input, select correct application type');
    }

    await this.page.getByRole('button', { name: 'Respond' }).click();
    await this.respondToApplicationText.isVisible();
    await this.respondToApplicationText.fill('Response from claimant');
    await this.page.locator('#supporting-material-yes-no-2').check();
    await this.clickContinue();

    await this.page.locator('#copyToOtherPartyYesOrNo').isVisible();
    await this.page.getByText('Yes, I confirm I want to copy').check();
    await this.clickContinue();

    await this.page.waitForSelector('text=Check your answers');
    await this.clickSubmitButton();
  }

  async validateApplicationBanner() {
    await expect(this.page.getByLabel('Important')).toContainText('The respondent has applied to amend response');
    await expect(this.page.getByRole('link', { name: 'Respond to the application' })).toBeVisible();
  }

  async clickRespondentContactDetailsLink() {
    await this.respondentContactDetailsLink.click();
    await expect(this.page.locator('h1')).toContainText('Respondent contact details');
  }

  async verifyRespondentContactDetails() {
    await expect(this.page.locator('dl')).toContainText('Name');
    await expect(this.page.locator('dl')).toContainText('Employer name');
    await expect(this.page.locator('dl')).toContainText('Preferred method of contact');
  }

  async verifyNotificationBanner(notificationType: string) {
    switch (notificationType) {
      case 'ET1 claim':
      case 'CMO':
      case 'ECC':
        await expect(this.page.locator('#main-content')).toContainText('The tribunal has sent you a notification');
        await this.page.getByText('View the notification -').click();
        break;
      case 'Hearing':
        await expect(this.page.locator('#main-content')).toContainText(
          'The tribunal has sent you a notification about your hearing.',
        );
        await this.page.getByText('View the notification').click();
        break;
      default:
        throw new Error('... Notification Type not provided ...');
    }

    switch (notificationType) {
      case 'ET1 claim':
        await expect(this.page.locator('dl')).toContainText('Claim (ET1)');
        break;
      case 'CMO':
        await expect(this.page.locator('dl')).toContainText('Case management orders / requests');
        await expect(this.page.locator('dl')).toContainText('Case management order');
        break;
      case 'ECC':
        await expect(this.page.locator('dl')).toContainText('Employer Contract Claim');
        break;
      case 'Hearing':
        await this.page.getByText('test Notification').click();
        await expect(this.page.locator('dl')).toContainText('Hearing');
        break;
      default:
        throw new Error('... Please provide Notification type ...');
    }
    await expect(this.page.locator('dl')).toContainText('Tribunal');
  }

  async verifyNotificationBannerForNoticeOfClaim() {
    await expect(this.page.locator('#main-content')).toContainText('The tribunal has acknowledged your claim');
    await this.page.getByText('View the Acknowledgement of Claim').click();
    await expect(this.page.locator('#main-content')).toContainText('Notice of a Claim and Notice of Hearing');
  }

  async verifyLegalRepNotificationBanner() {
    await expect(this.page.locator('h3')).toContainText('You are now being legally represented');
  }

  async contactTheTribunalLink() {
    await this.contactTribunalLinkRegistered.click();
    await expect(this.page.locator('h1')).toContainText('Contact the tribunal about your case');
    await expect(this.page.locator('#main-content')).toContainText(
      'You are now being legally represented by',
    );
  }

  async clickChangeMyLegalRep() {
    await this.changeMyLegalRep.click();
    await this.page.locator('#legalRep-2').check();
    await this.clickSubmitButton();
  }

  async verifyLegalRepUnassignedNotificationBanner() {
    await expect(this.page.getByLabel('Important')).toContainText('You are no longer legally represented.');
    await expect(this.appointLegalRepLink).toBeVisible();
  }

  async closeAndReturnToCaseDetailsCui() {
    await expect(this.returntoCUIcaseOverviewButton).toBeVisible();
    this.returntoCUIcaseOverviewButton.click();
  }

  async verifyRespondToTheTribunalNotificationBanner(notificationType: string, notificationName: string = 'Test Notification1') {
    switch (notificationType) {
      case 'ET1 claim':
      case 'CMO':
      case 'ECC':
        await expect(this.page.locator('#main-content')).toContainText('The tribunal has sent you a notification');
        await this.page.getByText('Respond to the tribunal - '+ notificationName).click();
        break;
      case 'Hearing':
        await expect(this.page.locator('#main-content')).toContainText(
          'The tribunal has sent you a notification about your hearing.',
        );
        await this.page.getByText('Respond to the tribunal').click();
        break;
      default:
        throw new Error('... Notification Type not provided ...');
    }

    switch (notificationType) {
      case 'ET1 claim':
        await expect(this.page.locator('dl')).toContainText('Claim (ET1)');
        break;
      case 'CMO':
        await expect(this.page.locator(`//dt[normalize-space()='Notification Subject']/following-sibling::dd`)).toContainText('Case management orders / requests');
        await expect(this.page.locator(`//dt[normalize-space()='Case management order or request?']/following-sibling::dd`)).toContainText('Case management order');
        break;
      case 'ECC':
        await expect(this.page.locator('dl')).toContainText('Employer Contract Claim');
        break;
      case 'Hearing':
        await this.page.getByText('test Notification').click();
        await expect(this.page.locator('dl')).toContainText('Hearing');
        break;
      default:
        throw new Error('... Please provide Notification type ...');
    }
    await expect(this.page.locator(`//dt[normalize-space()='Sent by']/following-sibling::dd`)).toContainText('Tribunal');
  }

  async respondToTheTribunalsNotification() {
    await this.page.waitForLoadState('load');
    await expect(this.page.getByRole('heading', { name: 'Your response' })).toBeVisible();

    await this.tribunalResponseField.fill('Response to tribunal by Claimant');

    await this.supportingMaterialRadioYes.waitFor();
    await this.supportingMaterialRadioYes.check();
    await this.clickContinue();

    await this.supportingMaterialFile.waitFor();
    await this.commonActionsHelper.uploadWithRateLimitRetry(
      this.page,
      this.supportingMaterialFile,
      `playwrighte2e/resources/test_file/welshTest.pdf`
    )

    await this.uploadFielButton.waitFor();
    await this.uploadFielButton.click();
    await this.clickContinue();

    await this.copyToOtherPartyYesOrNo.waitFor();
    await this.copyToOtherPartyYesOrNo.check();
    await this.clickContinue();

    await this.clickSubmitButton();
  }
}

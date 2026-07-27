import { expect, Locator, Page } from '@playwright/test';
import { BasePage } from './basePage';
import { CommonActionsHelper } from './helpers/CommonActionsHelper.ts';
import { AxeUtils } from '@hmcts/playwright-common';
import { CheckYourAnswersPage } from './helpers/CheckYourAnswersPage.ts';

export class ApplicationTabPage extends BasePage {

  private readonly selectAnApplicationTitle: Locator;
  private readonly applicationTypeDropdown: Locator;
  private readonly fileUpload: Locator;
  private readonly informationInputTextArea: Locator;
  private readonly copyThisCorrespondenceTitle: Locator;
  private readonly copyThisCorrespondenceToOtherPartyYes: Locator;
  private readonly copyThisCorrespondenceToOtherPartyNo: Locator;
  private readonly copyThisCorrespondenceNoTextArea: Locator;
  private readonly openApplicationRadio: Locator;
  private readonly closedApplicationRadio: Locator;
  private readonly viewApplicationDropdown: Locator;
  private readonly notificationTitleText: Locator;
  private readonly decisionRadioGroup: Locator;
  private readonly typeOfDecisionRadioGroup: Locator;
  private readonly decisionMadeByRadioGroup: Locator;
  private readonly fullNameOfDeciderText: Locator;
  private readonly partieToNotify: Locator;
  private readonly responseTitleTextArea: Locator;
  private readonly madeByDropdown: Locator;
  private readonly fullNameText: Locator;
  private readonly responseToTribunalRequiredDropdown: Locator;
  private readonly partiesToRespondDropdown: Locator;

  constructor(page: Page) {
    super(page);
    this.selectAnApplicationTitle = this.page.getByRole('heading', { name: 'Select an application' });
    this.applicationTypeDropdown = this.page.locator(`#resTseSelectApplication, #tseAdminSelectApplication, #tseRespondSelectApplication, #claimantTseSelectApplication`);

    this.fileUpload = this.page.locator(`input[id^="resTseDocument"], input[id^="claimantTseDocument"]`);
    this.informationInputTextArea = this.page.locator(`[id^="resTseTextBox"], [id^="claimantTseTextBox"]`);
    this.copyThisCorrespondenceTitle = this.page.getByRole('heading', {
      name: 'Copy this correspondence to the other party', exact: true,
    });
    this.copyThisCorrespondenceToOtherPartyYes = this.page.locator(`#resTseCopyToOtherPartyYesOrNo-Yes, #tseResponseCopyToOtherParty-Yes, #claimantTseRule92-Yes`);
    this.copyThisCorrespondenceToOtherPartyNo = this.page.locator(`#resTseCopyToOtherPartyYesOrNo-No, #tseResponseCopyToOtherParty-No, #claimantTseRule92-No`);
    this.copyThisCorrespondenceNoTextArea = this.page.locator(`#resTseCopyToOtherPartyTextArea, #tseResponseCopyNoGiveDetails, #claimantTseRule92AnsNoGiveDetails`);
    this.openApplicationRadio = this.page.locator(`#tseViewApplicationOpenOrClosed-Open`);
    this.closedApplicationRadio = this.page.locator(`#tseViewApplicationOpenOrClosed-Closed`);
    this.viewApplicationDropdown = this.page.locator(`#tseViewApplicationSelect`);
    this.notificationTitleText = this.page.locator(`#tseAdminEnterNotificationTitle`);
    this.decisionRadioGroup = this.page.locator(`#tseAdminDecision`);
    this.typeOfDecisionRadioGroup = this.page.locator(`#tseAdminTypeOfDecision`)
    this.decisionMadeByRadioGroup = this.page.locator(`#tseAdminDecisionMadeBy`);
    this.fullNameOfDeciderText = this.page.locator(`#tseAdminDecisionMadeByFullName`);
    this.partieToNotify = this.page.locator(`#tseAdminSelectPartyNotify`);
    this.responseTitleTextArea = this.page.locator(`#tseAdmReplyEnterResponseTitle`);
    this.madeByDropdown = this.page.locator(`#tseAdmReplyRequestMadeBy:not([disabled]), #tseAdmReplyCmoMadeBy:not([disabled])`);
    this.fullNameText = this.page.locator(`#tseAdmReplyRequestEnterFullName:not([type="hidden"]) ,#tseAdmReplyCmoEnterFullName:not([type="hidden"])`);
    this.responseToTribunalRequiredDropdown = this.page.locator(`#tseAdmReplyRequestIsResponseRequired:not([disabled]), #tseAdmReplyCmoIsResponseRequired:not([disabled])`);
    this.partiesToRespondDropdown = this.page.locator(`#tseAdmReplyCmoSelectPartyRespond:not([disabled]), #tseAdmReplyRequestSelectPartyRespond:not([disabled])`);
  }

  async selectEitherViewOrMakeOrRespondOrRecordADecisionLink(linkName: string) {
    await this.page.waitForLoadState('load');
    const linkLocator = this.page.locator(`//a[normalize-space()="${linkName}"]`);
    await expect(linkLocator).toBeVisible();
    await linkLocator.click();
    await this.page.waitForLoadState('load');
  }

  async selectApplicationTypeForMakingApplication(applicationType: string) {
    await this.page.waitForLoadState('load');
    await expect(this.selectAnApplicationTitle).toBeVisible();
    await expect(this.applicationTypeDropdown).toBeVisible();
    await this.applicationTypeDropdown.selectOption(applicationType);
  }

  async selectApplicationTypeToRespondToApplication(applicationType: string) {
    await this.page.waitForLoadState('load');
    await expect(this.selectAnApplicationTitle).toBeVisible();
    await expect(this.applicationTypeDropdown).toBeVisible();
    const url = this.page.url();
    if (url.includes('claimantTSE1')) {
      await this.assertDropDownOptionsAreVisible([
        'Amend claim',
        'Change personal details', 'Consider decision afresh', 'Contact the tribunal', 'Order a witness to attend to give evidence',
        'Order other party', 'Postpone a hearing', 'Reconsider judgment', 'Respondent not complied', 'Restrict publicity',
        'Strike out all or part of the response', 'Vary or revoke an order', 'Withdraw all or part of claim'
      ], this.applicationTypeDropdown);
    }
    await this.applicationTypeDropdown.selectOption(`1 ${applicationType}`);
  }


  async selectApplicationType(applicationType: string) {
    await this.page.waitForLoadState('load');
    await expect(this.selectAnApplicationTitle).toBeVisible();
    await this.applicationTypeDropdown.selectOption(`1 - ${applicationType}`);
  }

  async fillApplicationDetails(details: string) {
    await expect(this.informationInputTextArea).toBeVisible();
    await this.informationInputTextArea.fill(details);

    await this.commonActionsHelper.uploadWithRateLimitRetry(
      this.page,
      this.fileUpload,
      'resources/test_file/welshTest.pdf',
    );
  }

  async selectCopyThisCorrespondence(option: string) {
    await this.page.waitForLoadState('load');
    await expect(this.copyThisCorrespondenceTitle).toBeVisible();
    switch (option.toLowerCase()) {
      case 'yes':
        await expect(this.copyThisCorrespondenceToOtherPartyYes).toBeVisible();
        await this.copyThisCorrespondenceToOtherPartyYes.check();
        break;
      case 'no':
        await expect(this.copyThisCorrespondenceToOtherPartyNo).toBeVisible();
        await this.copyThisCorrespondenceToOtherPartyNo.check();
        await this.copyThisCorrespondenceNoTextArea.fill("Reason for not copying to other party");
        break;
      default:
        throw new Error(`R92 option: ${option} is not recognized.`);
    }
  }

  async assertCheckYourAnswersPage(applicationType: string, details: string) {
    await this.page.waitForLoadState('load');
    const checkYourAnswersPageTitle = this.page.getByRole('heading', { name: 'Check your answers' });
    await expect(checkYourAnswersPageTitle).toBeVisible();

    const applicationTypeCya = this.page.locator(
      `xpath=//th/span[normalize-space()='Select an application']/../following-sibling::td[1]`,
    );
    await expect(applicationTypeCya).toBeVisible();
    await expect(applicationTypeCya).toHaveText(applicationType);

    const document = this.page.locator(`xpath=//th/span[normalize-space()='Document']/../following-sibling::td[1]`);
    await expect(document).toBeVisible();
    await expect(document).toHaveText('welshTest.pdf');

    const additionalInformation = this.page.locator(
      `xpath=//th/span[normalize-space()='Use this box for any accompanying information']/../following-sibling::td[1]`,
    );
    await expect(additionalInformation).toBeVisible();
    await expect(additionalInformation).toHaveText(details);
  }

  async enterDetailsForMakingApplication(applicationType: string, axeUtils?: AxeUtils) {
    await this.selectEitherViewOrMakeOrRespondOrRecordADecisionLink('Make an application');
    await this.selectApplicationTypeForMakingApplication(applicationType);
    if(axeUtils) await axeUtils.audit();
    await this.clickContinue();

    await this.page.waitForLoadState('load');
    const applicationTypeTitle = this.page.getByRole('heading', { name: applicationType });
    await expect(applicationTypeTitle).toBeVisible();

    await expect(this.page.getByRole('heading',{ name: 'Details to include in your application:'})).toBeVisible();
    await expect(this.page.getByRole('heading', { name: 'Consider when providing material that'})).toBeVisible();
    if(applicationType === 'Withdraw all or part of claim') {
      await expect(this.page.getByRole('heading',{ name: 'Withdrawal and dismissal'})).toBeVisible();
    } else {
      await expect(this.page.getByRole('heading',{ name: 'Withdrawal and dismissal'})).not.toBeVisible();
    }

    const details = 'Details of application for ' + applicationType;
    await this.fillApplicationDetails(details);
    if (axeUtils) await axeUtils.audit();
    await this.clickContinue();

    await this.selectCopyThisCorrespondence('Yes');
    if (axeUtils) await axeUtils.audit();
    await this.clickContinue();

    await this.assertCheckYourAnswersPage(applicationType, details);
    if (axeUtils) await axeUtils.audit();
    await this.clickSubmitButton();

    await expect(this.page.getByRole('heading' ,{name:'What happens next'})).toBeVisible();
    if (axeUtils) await axeUtils.audit();
    await this.clickCloseAndReturn();
  }

  // VIEW APPLICATION flow

  async selectWhatApplicationYouWishToView(openOrClosed: string) {
    await this.page.waitForLoadState('load');

    switch (openOrClosed.toLowerCase()) {
      case 'open':
        await expect(this.openApplicationRadio).toBeVisible();
        await this.openApplicationRadio.check();
        break;
      case 'closed':
        await expect(this.closedApplicationRadio).toBeVisible();
        await this.closedApplicationRadio.check();
        break;
      default:
        throw new Error(`Option: ${openOrClosed} is not recognized.`);
    }
  }

  async selectApplicationToView(applicationName: string) {
    await this.page.waitForLoadState('load');
    await expect(this.viewApplicationDropdown).toBeVisible();
    await this.viewApplicationDropdown.selectOption(`1 ${applicationName}`);
  }

  async assertApplicationPageDetails(details: string[]) {
    await this.page.waitForLoadState('load');
    const viewApplicationData = this.page.locator(`#tseApplicationSummaryAndResponsesMarkupLabel, #tseAdminTableLabel`);
    for(const detail of details) {
      const key = detail.split('-')[0].trim();
      const value = detail.split('-')[1].trim();
      const detailLocator = viewApplicationData.locator(
        `xpath=//td[normalize-space()='${key}']/following-sibling::td`,
      );
      await expect(detailLocator).toBeVisible();
      await expect(detailLocator).toHaveText(value);
    }
  }

  async viewApplicationAndAssertDetails(applicationType: string, openOrClosed: string, details: string[]) {
    await this.page.waitForLoadState('load');
    await this.selectEitherViewOrMakeOrRespondOrRecordADecisionLink('View an application');
    await expect(this.page.getByRole('heading', { name: 'View application' })).toBeVisible();
    await this.selectWhatApplicationYouWishToView(openOrClosed);
    await this.clickContinue();
    await this.selectApplicationToView(applicationType);
    await this.clickContinue();
    await this.assertApplicationPageDetails(details);
  }

  // RESPONDING to application or Recording a decision on application flows

  async enterNotificationTitle(title: string) {
    await expect(this.notificationTitleText).toBeVisible();
    await this.notificationTitleText.fill(title);
  }

  async selectDecisionType(decisionType: string) {
    await expect(this.decisionRadioGroup).toBeVisible();
    await this.decisionRadioGroup.getByRole('radio', { name: decisionType, exact: true }).check();
  }

  async selectTypeOfDecisionType(decisionType: string) {
    await expect(this.typeOfDecisionRadioGroup).toBeVisible();
    await this.typeOfDecisionRadioGroup.getByLabel(decisionType).check();

  }

  async uploadSupportingMaterial(filePaths: string[] = ['resources/test_file/welshTest.pdf']) {
    for (let i=0; i<filePaths.length; i++) {
      await this.addNewButtonClick();
      await this.page.waitForLoadState('load');
      const uploadLocator = this.page.locator(`#tseAdminResponseRequiredNoDoc_${i}_uploadedDocument, #tseResponseSupportingMaterial_${i}_uploadedDocument, #tseAdmReplyAddDocument_${i}_uploadedDocument`)
      await this.commonActionsHelper.uploadWithRateLimitRetry(
        this.page,
        uploadLocator,
        filePaths[i],
      );
      await this.page.waitForLoadState('load');
      await this.page.locator(`#tseAdminResponseRequiredNoDoc_${i}_shortDescription, #tseResponseSupportingMaterial_${i}_shortDescription, #tseAdmReplyAddDocument_${i}_shortDescription`).fill(`Supporting material ${i+1}`);
    }
  }

  async selectDecisionMadeBy(decisionMadeBy: string, name: string) {
    await this.page.waitForLoadState('load');
    await this.decisionMadeByRadioGroup.getByLabel(decisionMadeBy).check();
    await expect(this.fullNameOfDeciderText).toBeVisible();
    await this.fullNameOfDeciderText.fill(name);
  }

  async selectPartiesToNotify(parties: string) {
    await this.page.waitForLoadState('load');
    await this.partieToNotify.getByLabel(parties).check();
  }

  async enterDetailsForRecordADecision(
    cyaPage: CheckYourAnswersPage,
    applicationType:string,
    decision:string='Granted',
    typeOfDecision: string = 'Judgment',
  ) {

    await this.page.waitForLoadState('load');
    await this.selectEitherViewOrMakeOrRespondOrRecordADecisionLink('Record a decision');
    await this.selectApplicationType(applicationType);
    await this.clickContinue('tseAdmin', 2);

    await this.enterNotificationTitle(applicationType);
    await this.selectDecisionType(decision);
    await this.selectTypeOfDecisionType(typeOfDecision);
    await this.uploadSupportingMaterial();
    await this.selectDecisionMadeBy('Legal officer', 'LEGAL OFFICER')
    await this.selectPartiesToNotify('Both parties');
    await this.page.waitForLoadState('load');
    await this.clickContinue('tseAdmin/submit');

    await cyaPage.assertCheckYourAnswersPage(
      {
        tableName: 'Check your answers',
        rows: [
          { cellItem: 'Enter notification title', value: applicationType},
          { cellItem: 'Decision', value: decision},
          { cellItem: 'Type of decision', value: typeOfDecision},
          { cellItem: 'Document', value: 'welshTest.pdf' },
          { cellItem: 'Decision was made by', value: 'Legal officer'},
          { cellItem: 'Full name', value: 'LEGAL OFFICER'},
          { cellItem: 'Select the party or parties to notify', value: 'Both parties'},
        ]
      });

    await this.clickSubmitButton();
    await this.clickCloseAndReturn();
  }

  async caseWorkerRespondToAnApplication(applicationType:string, orderOrRequest: string = 'Case management order') {
    await this.page.waitForLoadState('load');
    await this.selectEitherViewOrMakeOrRespondOrRecordADecisionLink('Respond to an application');
    await this.selectApplicationType(applicationType);
    await this.clickContinue('tseAdmReply', 2);

    await expect(this.responseTitleTextArea).toBeVisible();
    await this.responseTitleTextArea.fill('Response of an application By Caseworker');
    await this.uploadSupportingMaterial();

    await this.page.getByRole('radio', { name: orderOrRequest }).check();
    await this.madeByDropdown.selectOption('1: Legal officer');
    await this.fullNameText.fill('LEGAL OFFICER');
    await this.responseToTribunalRequiredDropdown.selectOption('1: Yes');
    await this.partiesToRespondDropdown.selectOption('1: Both parties');

    // parties to notify
    await this.page.getByRole('radio', { name: 'Both parties' }).check();
    await this.page.waitForLoadState('load');
    await this.clickContinue('tseAdmReply/submit'); // IT is very flaky or UI takes more time to load

    await this.page.waitForLoadState('load');
    await this.clickSubmitButton();
    await this.clickCloseAndReturn();
  }

  async legalRepRespondToAnApplication(applicationType: string) {
    await this.page.waitForLoadState('load');
    await this.selectEitherViewOrMakeOrRespondOrRecordADecisionLink('Respond to an application');
    await this.selectApplicationTypeToRespondToApplication(applicationType);
    await this.clickContinue();

    const responseToApplication = this.page.locator(`#tseResponseText`);
    await expect(responseToApplication).toBeVisible();
    await responseToApplication.fill('Response of an application');
    await this.page.getByRole('radio', { name: 'Yes' }).check();

    const supportingMaterialGroup = this.page.locator(`#tseResponseHasSupportingMaterial`);
    await expect(supportingMaterialGroup).toBeVisible();
    await supportingMaterialGroup.getByText('Yes').check();
    await this.uploadSupportingMaterial();
    await this.clickContinue();

    await this.page.waitForLoadState('load');
    await this.selectCopyThisCorrespondence('Yes');
    await this.clickContinue();

    await this.clickSubmitButton();
    await expect(this.page.locator('h3')).toContainText('What happens next');
    await this.clickCloseAndReturn();
  }
}

import { BasePage } from './basePage';
import { Locator, Page } from '@playwright/test';

export class CaseOverviewPage extends BasePage {
  private readonly applicationTab: Locator;
  private readonly applicationDropdown: Locator;
  private readonly decisionForm: Locator;
  private readonly notificationTitleDecisionPage: Locator;
  private readonly decisionGranted: Locator;
  private readonly decisionGrantedInPart: Locator;
  private readonly decisionRefused: Locator;
  private readonly otherDecision: Locator;
  private readonly judgementDecisionType: Locator;
  private readonly caseManagementDecisionType: Locator;
  private readonly additionalDecisionInformation: Locator;
  private readonly madeByLegalOfficer: Locator;
  private readonly isResponseRequired_No: Locator;
  private readonly madeByAJudge: Locator;
  private readonly bothParties: Locator;
  private readonly claimantOnly: Locator;
  private readonly respondentOnly: Locator;
  private readonly fullNameDecisionMaker: Locator;
  private readonly confirmSubmission: Locator;
  private readonly returnToCaseOverview: Locator;

  constructor(page: Page) {
    super(page);
    this.applicationTab = page.locator('#mat-tab-label-0-9');
    this.applicationDropdown = page.locator('#tseAdminSelectApplication');
    this.decisionForm = page.locator('#caseEditForm');
    this.notificationTitleDecisionPage = page.locator('#tseAdminEnterNotificationTitle');
    this.decisionGranted = page.locator('#tseAdminDecision-Granted');
    this.decisionGrantedInPart = page.locator('#tseAdminDecision-Granted in part');
    this.decisionRefused = page.locator('#tseAdminDecision-Refused');
    this.otherDecision = page.locator('#tseAdminDecision-Other');
    this.judgementDecisionType = page.locator('#tseAdminTypeOfDecision-Judgment');
    this.caseManagementDecisionType = page.locator('[id="tseAdminTypeOfDecision-Case management order"]');
    this.additionalDecisionInformation = page.locator('#tseAdminAdditionalInformation');
    this.madeByLegalOfficer = page.locator('[id="tseAdminDecisionMadeBy-Legal officer"]');
    this.isResponseRequired_No = page.locator('#tseAdminIsResponseRequired-No');
    this.madeByAJudge = page.locator('#tseAdminDecisionMadeBy-Judge');
    this.bothParties = page.locator('[id="tseAdminSelectPartyNotify-Both parties"]');
    this.claimantOnly = page.locator('[id="tseAdminSelectPartyNotify-Claimant only"]');
    this.respondentOnly = page.locator('[id="tseAdminSelectPartyNotify-Respondent only"]');
    this.fullNameDecisionMaker = page.locator('#tseAdminDecisionMadeByFullName');
    this.confirmSubmission = page.locator('#confirmation-body');
    this.returnToCaseOverview = page.locator('//button[@class="button"]');
  }

  async recordAdecisionOnAcase(submissionReference: string, appOption: string, decision: string, decisionType: string, decisionMaker: string, respondingParties: string) {
    const recordDecisionLink = `/cases/case-details/${submissionReference}/trigger/tseAdmin/tseAdmin1`;
    await this.applicationTab.click();
    await this.page.click(`[href="${recordDecisionLink}"]`);
    await this.applicationDropdown.selectOption(appOption);
    await this.page.click('text=Continue');
    await this.decisionForm.waitFor({ timeout: 15000 });
    await this.notificationTitleDecisionPage.fill('adding a decision');

    switch (decision) {
      case 'granted':
        await this.decisionGranted.check();
        break;
      case 'granted in part':
        await this.decisionGrantedInPart.check();
        break;
      case 'refused':
        await this.decisionRefused.check();
        break;
      case 'other decision':
        await this.otherDecision.check();
        break;
      default:
        throw new Error('... decision option is invalid');
    }

    switch (decisionType) {
      case 'judgment':
        await this.judgementDecisionType.check();
        break;
      case 'cmo-responding':
        await this.page.waitForTimeout(2000);
        await this.page.evaluate(() => window.scrollTo(0, document.body.scrollHeight));
        await this.caseManagementDecisionType.check();
        await this.isResponseRequired_No.check();
        break;
      case 'cmo-no-response':
        await this.page.waitForTimeout(2000);
        await this.page.evaluate(() => window.scrollTo(0, document.body.scrollHeight));
        await this.caseManagementDecisionType.check();
        await this.isResponseRequired_No.check();
        break;
      default:
        throw new Error('... decision type is invalid');
    }

    await this.additionalDecisionInformation.fill('...test');

    switch (decisionMaker) {
      case 'legal officer':
        await this.madeByLegalOfficer.check();
        break;
      case 'judge':
        await this.madeByAJudge.check();
        break;
      default:
        throw new Error('... please select who made the decision');
    }

    await this.fullNameDecisionMaker.fill('ET Tester');

    switch (respondingParties) {
      case 'both':
        await this.bothParties.check();
        break;
      case 'claimant only':
        await this.claimantOnly.check();
        break;
      case 'respondent only':
        await this.respondentOnly.check();
        break;
      default:
        throw new Error('... please party to respond');
    }

    await this.page.click('text=Continue');
    await this.page.waitForSelector('.form-table', { timeout: 15000 });
    await this.page.waitForSelector('text=Check your answers');
    await this.page.click('text=Submit');
    await this.confirmSubmission.waitFor({ timeout: 15000 });
    await this.page.waitForSelector('text=What happens next');
    await this.returnToCaseOverview.click();
  }
}

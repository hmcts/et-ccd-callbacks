import { expect, Locator, Page } from '@playwright/test';
import { BasePage } from './basePage';
import { ReferralOption } from '../config/case-data.ts';

export default class ReferralPage extends BasePage {
  private readonly judgeReferralOption: Locator;
  private readonly adminReferralOption: Locator;
  private readonly legalOfficerReferralOption: Locator;
  private readonly isUrgentYes: Locator;
  private readonly referralSubjOption: Locator;
  private readonly referralDetails: Locator;
  private readonly docUploadEle: Locator;
  private readonly referralSelectEle: Locator;
  private readonly adminDirectionOption: Locator;
  private readonly isUrgentReplyYes: Locator;
  private readonly directionSubjEle: Locator;
  private readonly replyDocUploadEle: Locator;
  private readonly confirmCloseReferralYes: Locator;
  private readonly closeReferralGeneralNotes: Locator;
  private readonly markdownPara: Locator;

  constructor(page: Page) {
    super(page);
    this.judgeReferralOption = page.locator('#referCaseTo-Judge, #updateReferCaseTo-Judge, #directionTo-Judge');
    this.adminReferralOption = page.locator('#referCaseTo-Admin, #updateReferCaseTo-Admin, #directionTo-Admin');
    this.legalOfficerReferralOption = page.locator('[id="referCaseTo-Legal officer"], [id="updateReferCaseTo-Legal officer"], [id="directionTo-Legal officer"]');
    this.isUrgentYes = page.locator('#isUrgent_Yes');
    this.referralSubjOption = page.locator('#referralSubject');
    this.referralDetails = page.locator('#referralDetails, #updateReferralDetails');
    this.docUploadEle = page.locator('#referralDocument_0_uploadedDocument');
    this.referralSelectEle = page.locator('#selectReferral');
    this.adminDirectionOption = page.locator('#directionTo-Admin');
    this.isUrgentReplyYes = page.locator('#isUrgentReply_Yes');
    this.directionSubjEle = page.locator('#directionDetails');
    this.replyDocUploadEle = page.locator('#replyDocument_0_uploadedDocument');
    this.confirmCloseReferralYes = page.locator('#confirmCloseReferral-Yes');
    this.closeReferralGeneralNotes = page.locator('#closeReferralGeneralNotes');
    this.markdownPara = page.locator('markdown p');
  }

  async sendNewReferral(option: ReferralOption) {
    await expect(this.page.getByText('Refer to admin, legal officer or judge')).toBeVisible();

    switch (option) {
    case 'Admin':
      await this.adminReferralOption.click();
      break;
    case 'Legal Officer':
      await this.legalOfficerReferralOption.click();
      break;
    case 'Judge':
    default:
      await this.judgeReferralOption.click();
      break;
    }
    await this.isUrgentYes.check();
    await this.referralSubjOption.selectOption({ label: 'ET1' });
    await this.referralDetails.fill("This is a test referral");

    await this.addNewButtonClick();
    await this.docUploadEle.waitFor();
    await this.commonActionsHelper.uploadWithRateLimitRetry(this.page, this.docUploadEle, 'src/test/e2e/resources/test_file/test.txt');

    await this.clickContinue();
    await this.clickSubmitButton();

    await expect(this.markdownPara).toContainText("Your referral has been sent");
    await this.clickCloseAndReturn();
  }

  async replyToReferral(option: ReferralOption = 'Admin') {
    await this.referralSelectEle.waitFor();
    await this.referralSelectEle.selectOption({ label: '1 - ET1' });
    await this.clickContinue();

    switch (option) {
      case 'Admin':
        await this.adminReferralOption.click();
        break;
      case 'Legal Officer':
        await this.legalOfficerReferralOption.click();
        break;
      case 'Judge':
      default:
        await this.judgeReferralOption.click();
        break;
    }
    await this.isUrgentReplyYes.check();
    await this.directionSubjEle.fill("This is a test direction");

    await this.addNewButtonClick();
    await this.replyDocUploadEle.waitFor();
    await this.commonActionsHelper.uploadWithRateLimitRetry(
      this.page,
      this.replyDocUploadEle,
      'src/test/e2e/resources/test_file/test.txt'
    );

    await this.clickContinue();
    await this.clickSubmitButton();

    await expect(this.markdownPara).toContainText("We have recorded your reply");
    await this.clickCloseAndReturn();
  }

  async updateTheReferral(referral: string = '1 - ET1', option: ReferralOption = 'Admin') {
    await this.referralSelectEle.waitFor();
    await this.referralSelectEle.selectOption({ label: referral });
    await this.clickContinue();

    switch (option) {
      case 'Admin':
        await this.adminReferralOption.click();
        break;
      case 'Legal Officer':
        await this.legalOfficerReferralOption.click();
        break;
      case 'Judge':
      default:
        await this.judgeReferralOption.click();
        break;
    }
    await this.referralDetails.fill("This is a test referral UPDATE");

    await this.clickContinue();
    await this.clickSubmitButton();
  }

  async closeAReferral() {
    await this.referralSelectEle.waitFor();
    await this.referralSelectEle.selectOption({ label: '1 - ET1' });
    await this.clickContinue();

    let expText = await this.page.locator("//tr/th[contains(text(), 'Referral')]").textContent();
    await expect(expText).toEqual('Referral 1');
    await expect(this.page.getByText('Referral Replies', { exact: true })).toBeVisible();
    await this.confirmCloseReferralYes.check();
    await this.closeReferralGeneralNotes.fill("This is a test close referral");
    await this.clickContinue();
    await this.clickSubmitButton();

    await expect(this.markdownPara).toContainText("We have closed this referral");
    await this.clickCloseAndReturn();
  }
}

import { BasePage } from "./basePage";
import { expect, Locator, Page } from '@playwright/test';

export default class ET3ProcessPage extends BasePage {
  private readonly markdownPara: Locator;
  private readonly chooseRespondent: Locator;
  private readonly legendSpan: Locator;
  private readonly rule26No: Locator;
  private readonly labelSpan: Locator;
  private readonly confirmationHeader: Locator;

  constructor(page: Page) {
    super(page);
    this.markdownPara = page.locator('markdown p');
    this.chooseRespondent = page.locator('#et3ChooseRespondent');
    this.legendSpan = page.locator('legend span');
    this.rule26No = page.locator('#et3Rule26_No');
    this.labelSpan = page.locator('label span');
    this.confirmationHeader = page.locator('#confirmation-header markdown h1');
  }

  async submitET3Response() {
    await this.delay(20000);
    await expect(this.markdownPara).toContainText('To help you complete this, open the ET1 form');
    await this.clickContinue();

    await this.chooseRespondent.selectOption({ label: 'Mrs Test Auto' });
    await this.clickContinue();
    await this.delay(10000);
    await this.clickContinue();
    await this.delay(2000);

    await expect(this.legendSpan).toContainText('Did we receive the ET3 response in time?');
    await this.clickContinue();
    await this.delay(2000);

    await expect(this.legendSpan).toContainText("Do we have the respondent's name?");
    await this.clickContinue();
    await this.delay(2000);

    const elementMap = new Map<string, string>([
      ["Does the respondent's name match", '#et3DoesRespondentsNameMatch_Yes'],
      ["Do we have the respondent's address?", '#et3DoWeHaveRespondentsAddress_Yes'],
      ["Does the respondent's address match?", '#et3DoesRespondentsAddressMatch_Yes'],
      ["Does the respondent wish to contest any part of the claim?", '#et3ContestClaim-No'],
      ["Is there an Employer's Contract Claim in section 7 of the ET3 response?", '#et3ContractClaimSection7_Yes'],
      ["Is the case listed for hearing?", '#et3IsCaseListedForHearing_No'],
      ["Is this location correct?", '#et3IsThisLocationCorrect-Yes']
    ]);

    for (let [key, value] of elementMap) {
      await expect(this.legendSpan).toContainText(key);
      await this.page.locator(value).click();
      await this.clickContinue();
      await this.delay(2000);
    }

    await expect(this.rule26No).toBeVisible();
    await this.rule26No.click();
    await this.clickContinue();
    await this.delay(2000);

    await expect(this.labelSpan).toBeVisible();
    await this.clickContinue();
    await this.delay(2000);
    await this.clickSubmitButton();

    await expect(this.confirmationHeader).toContainText('ET3 Processing complete');
    await this.clickCloseAndReturn();
  }
}

import { BasePage } from "./basePage";
import { expect, Locator, Page } from "@playwright/test";
import { AxeUtils } from '@hmcts/playwright-common';
const today = new Date();

export default class Et1CaseServingPage extends BasePage {
  private readonly dateAcceptedDay: Locator;
  private readonly dateAcceptedMonth: Locator;
  private readonly dateAcceptedYear: Locator;
  private readonly yesRadio: Locator;
  private readonly servingDocType: Locator;
  private readonly servingDocUpload: Locator;
  private readonly servingDocShortDesc: Locator;

  constructor(page: Page) {
    super(page);
    this.dateAcceptedDay = page.locator('#dateAccepted-day');
    this.dateAcceptedMonth = page.locator('#dateAccepted-month');
    this.dateAcceptedYear = page.locator('#dateAccepted-year');
    this.yesRadio = page.getByLabel('Yes');
    this.servingDocType = page.locator('#servingDocumentCollection_0_typeOfDocument');
    this.servingDocUpload = page.locator('#servingDocumentCollection_0_uploadedDocument');
    this.servingDocShortDesc = page.locator('#servingDocumentCollection_0_shortDescription');
  }

  async processET1CaseServingPages(accessibilityEnabled?: boolean, axeUtils?: AxeUtils) {
    if (accessibilityEnabled) {
      // @ts-ignore
      await axeUtils.audit();
    }
    await this.yesRadio.check();
    await this.dateAcceptedDay.fill(String(today.getDate()));
    await this.dateAcceptedMonth.fill(String(today.getMonth() + 1));
    await this.dateAcceptedYear.fill(String(today.getFullYear()));
    await this.dateAcceptedMonth.click();
    await this.clickSubmitButton();
    await this.delay(5000);
  }

  async et1ServingEvent(canContinue: boolean = false) {
    await this.addNewButtonClick();
    await this.servingDocType.selectOption({ label: '7.7 In person preliminary hearing - notice of case management discussion' });
    await this.commonActionsHelper.uploadWithRateLimitRetry(
      this.page,
      this.servingDocUpload,
      `resources/test_file/welshTest.pdf`
    );
    await this.page.waitForTimeout(3000);
    await this.servingDocShortDesc.fill('ET1 serving');
    await this.clickContinue('', -1, canContinue);
  }

  async et1ServingEventNoticeOfClaim() {
    await this.addNewButtonClick();
    await this.servingDocType.selectOption({ label: '2.7 ET2 short track claim' });
    await this.commonActionsHelper.uploadWithRateLimitRetry(
      this.page,
      this.servingDocUpload,
      `resources/test_file/welshTest.pdf`
    );
    await this.page.waitForTimeout(3000);
    await this.servingDocShortDesc.fill('ET1 serving');
    await this.clickContinue();

    await expect(this.page.getByText('Print and send paper documents')).toBeVisible();
    await this.clickContinue();

    await expect(this.page.getByText('Email documents to Acas')).toBeVisible();
    await this.clickSubmitButton();

    await expect(this.page.getByText('Documents sent')).toBeVisible();
    await this.clickCloseAndReturn();
  }

  async validateEt1ErrorMessage() {
    await expect(this.page.getByLabel('Cannot continue because the').getByRole('listitem')).toContainText('You have only uploaded a notice of hearing. Please also upload the relevant service letter.');
  }
}

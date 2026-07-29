import { BasePage } from './basePage';
import dateUtilComponent from '../data-utils/DateUtilComponent';
import { Locator, Page } from '@playwright/test';

export default class UploadHearingBundlePage extends BasePage {
  private readonly futureHearing: Locator;
  private readonly hearingCombo: Locator;
  private readonly claimantRadio: Locator;
  private readonly uploadDocInput: Locator;
  private readonly whatHearingDocument: Locator;
  private readonly dateSubmittedDay: Locator;
  private readonly dateSubmittedMonth: Locator;
  private readonly dateSubmittedYear: Locator;

  constructor(page: Page, ) {
    super(page);
    this.futureHearing = page.locator('#uploadHearingDocumentsSelectPastOrFutureHearing-Future');
    this.hearingCombo = page.locator('#uploadHearingDocumentsSelectFutureHearing');
    this.claimantRadio = page.locator('#uploadHearingDocumentsWhoseDocuments-Claimant');
    this.uploadDocInput = page.locator('#uploadHearingDocumentType_0_document');
    this.whatHearingDocument = page.locator('#uploadHearingDocumentType_0_type');
    this.dateSubmittedDay = page.locator('#uploadHearingDocumentsDateSubmitted-day');
    this.dateSubmittedMonth = page.locator('#uploadHearingDocumentsDateSubmitted-month');
    this.dateSubmittedYear = page.locator('#uploadHearingDocumentsDateSubmitted-year');
  }

  async uploadHearingBundleDocuments() {
    await this.futureHearing.check();
    await this.hearingCombo.selectOption({ value: '1: 1' });
    await this.addNewButtonClick();
    await this.commonActionsHelper.uploadWithRateLimitRetry(this.page, this.uploadDocInput, `resources/test_file/welshTest.pdf`);
    await this.whatHearingDocument.getByRole('radio', { name: 'Hearing Bundle' }).check();
    await this.claimantRadio.check();
    await this.dateSubmittedDay.fill(dateUtilComponent.getCurrentDateParts().dd);
    await this.dateSubmittedMonth.fill(dateUtilComponent.getCurrentDateParts().mm);
    await this.dateSubmittedYear.fill(dateUtilComponent.getCurrentDateParts().yyyy);
    await this.page.waitForLoadState('load', {timeout: 1000});
    await this.clickSubmitButton();
    await this.page.waitForLoadState('load');
  }

  async validateHearingDocument() {
    //TODO
  }
}

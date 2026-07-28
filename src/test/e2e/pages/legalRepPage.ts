import { BasePage } from './basePage';
import { expect, Locator, Page } from '@playwright/test';
import { Events } from '../config/case-data.ts';

export class LegalRepPage extends BasePage {
  readonly changeDocuUploaded: Locator;
  readonly prepareDocPageTwoHeader: Locator;
  readonly prepDecYesOption: Locator;
  readonly prepDocAgreeWithRes: Locator;
  readonly prepDocAgreeWithResTextField: Locator;
  readonly prepDocNoAgreement: Locator;
  readonly prepDocNoAgreementTextField: Locator;
  readonly selectHearingFromDropdown: Locator;
  readonly respondentDocOnly: Locator;
  readonly bothPartiesDoc: Locator;
  readonly hearingDocumentIncludingWitnessStatement: Locator;
  readonly supplementaryHearingDocument: Locator;
  readonly witnessStatementOnly: Locator;
  readonly uploadBundleDocument: Locator;
  readonly loadingSpinner: Locator;
  readonly closeReturnToCaseDetails: Locator;

  constructor(page: Page) {
    super(page);
    this.changeDocuUploaded = page.locator('[aria-label="Change Upload document"]');
    this.prepareDocPageTwoHeader = page.locator('.govuk-heading-l');
    this.prepDecYesOption = page.locator('#bundlesRespondentAgreedDocWith-Yes');
    this.prepDocAgreeWithRes = page.locator('#bundlesRespondentAgreedDocWith-But');
    this.prepDocAgreeWithResTextField = page.locator('#bundlesRespondentAgreedDocWithBut');
    this.prepDocNoAgreement = page.locator('#bundlesRespondentAgreedDocWith-No');
    this.prepDocNoAgreementTextField = page.locator('#bundlesRespondentAgreedDocWithNo');
    this.selectHearingFromDropdown = page.locator('#bundlesRespondentSelectHearing');
    this.respondentDocOnly = page.locator('#bundlesRespondentWhoseDocuments div:nth-of-type(1) > .form-control');
    this.bothPartiesDoc = page.locator('#bundlesRespondentWhoseDocuments div:nth-of-type(2) > .form-control');
    this.hearingDocumentIncludingWitnessStatement = page.locator('#bundlesRespondentWhatDocuments div:nth-of-type(1) > .form-control');
    this.supplementaryHearingDocument = page.locator('#bundlesRespondentWhatDocuments div:nth-of-type(2) > .form-control');
    this.witnessStatementOnly = page.locator('#bundlesRespondentWhatDocuments div:nth-of-type(3) > .form-control');
    this.uploadBundleDocument = page.locator('#bundlesRespondentUploadFile');
    this.loadingSpinner = page.locator('.spinner-container');
    this.closeReturnToCaseDetails = page.locator('//button[@class="button"]');
  }

  async submitDocumentForHearingRespondent(
    agreement: string,
    whoseDocu: string,
    docuType: string,
    checkActiveHearing?: boolean,
  ) {
    const expUrl = Events.uploadDocumentsForHearing.ccdCallback;
    await expect(this.page.locator('text=Prepare and submit documents for a hearing')).toBeVisible({ timeout: 10000 });
    await this.clickContinue(expUrl, 2);
    await expect(this.prepareDocPageTwoHeader).toBeVisible({ timeout: 15000 });
    await this.waitForSpinner();
    await expect(this.page.locator('text=Have you agreed these documents with the other party?')).toBeVisible();
    await this.page.evaluate(() => window.scrollTo(0, document.body.scrollHeight));
    try {
      switch (agreement) {
        case 'Yes':
          await this.prepDecYesOption.click();
          await this.clickContinue(expUrl, 3);
          break;
        case 'Agreed':
          await this.prepDocAgreeWithRes.check();
          await this.prepDocAgreeWithResTextField.fill('Testing prep document for hearing -- Agree with Res');
          break;
        case 'NotAgreed':
          await this.prepDocNoAgreement.check();
          await this.prepDocNoAgreementTextField.fill('Testing prep document for hearing -- No agreement');
          break;
        default:
          // ... check your options or add new option
          break;
      }
    } catch (error) {
      console.error('invalid option', error);
    }
    await this.clickContinue(expUrl, 3);
    await this.waitForSpinner();
    await this.page.evaluate(() => window.scrollTo(0, document.body.scrollHeight));
    await expect(this.page.locator('text=About your hearing documents')).toBeVisible();
    if (checkActiveHearing) {
      const options = this.selectHearingFromDropdown.locator('option');
      const optionsCount = await options.count();
      expect(optionsCount).toBe(2);
      const optionText = await options.nth(1).textContent();
      expect(optionText).toContain('0 Costs Hearing - Leeds ET');
      expect(optionText).not.toContain('1 Costs Hearing - Leeds ET');
    }
    await this.selectHearingFromDropdown.selectOption({ index: 1 });
    // Whose hearing documents are you uploading
    try {
      switch (whoseDocu) {
        case 'Respondent':
          await this.respondentDocOnly.check();
          break;
        case 'Both Parties':
          await this.bothPartiesDoc.check();
          break;
        default:
          // ... check your options or add new option
          break;
      }
    } catch (error) {
      console.error('invalid option', error);
    }
    // What are these documents?
    try {
      switch (docuType) {
        case 'Hearing Document including witness statement':
          await this.hearingDocumentIncludingWitnessStatement.check();
          break;
        case 'Supplementary hearing documents':
          await this.supplementaryHearingDocument.check();
          break;
        case 'Witness statement only':
          await this.witnessStatementOnly.check();
          break;
        default:
          // ... check your options or add new option
          break;
      }
    } catch (error) {
      console.error('invalid option', error);
    }
    await this.clickContinue(expUrl, 4);
    await this.loadingSpinner.waitFor({ state: 'hidden', timeout: 10000 });
    await this.page.evaluate(() => window.scrollTo(0, document.body.scrollHeight));
    await expect(this.page.locator('text=Upload your file of documents')).toBeVisible();
    await this.commonActionsHelper.uploadWithRateLimitRetry(this.page, this.uploadBundleDocument, `resources/test_file/welshTest.pdf`);
    await this.clickContinue(expUrl +'/submit');
    await this.page.evaluate(() => window.scrollTo(0, document.body.scrollHeight));
    await expect(this.changeDocuUploaded).toBeVisible({ timeout: 10000 });
    await expect(this.page.locator('text=Check the information below carefully.')).toBeVisible();
    await expect(this.page.locator('text=Upload documents for hearing')).toBeVisible();
    await expect(this.page.locator('text=Check your answers')).toBeVisible();
    await this.clickSubmitButton();
    await this.closeReturnToCaseDetails.click();
    await this.page.waitForTimeout(10000);
  }
}

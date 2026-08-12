import { expect, Locator, Page } from '@playwright/test';
import { CheckYourAnswersPage } from '../helpers/CheckYourAnswersPage';
import { BaseEventPage } from './BaseEventPage.ts';

export class UploadDocumentsForHearingPage extends BaseEventPage {
  private readonly prepDocYesOption: Locator;
  private readonly prepDocAgreeWithResOption: Locator;
  private readonly prepDocAgreeWithResTextField : Locator;
  private readonly prepDocNoAgreementOption: Locator;
  private readonly prepDocNoAgreementTextField: Locator;
  private readonly hearingsList: Locator;
  private readonly respondentDocOnlyOption: Locator;
  private readonly bothPartiesDocOption: Locator;
  private readonly hearingDocWithWitnessStatementOption: Locator;
  private readonly supplementaryHearingDocOption: Locator;
  private readonly witnessStatementOption: Locator;
  private readonly uploadDocumentInput: Locator;
  private readonly closeAndReturnToCaseDetailsButton: Locator;

  public constructor(page: Page) {
    super(page);
    this.prepDocYesOption = page.locator('#bundlesRespondentAgreedDocWith-Yes');
    this.prepDocAgreeWithResOption = page.locator('#bundlesRespondentAgreedDocWith-But');
    this.prepDocAgreeWithResTextField = page.locator('#bundlesRespondentAgreedDocWithBut');
    this.prepDocNoAgreementOption = page.locator('#bundlesRespondentAgreedDocWith-No');
    this.prepDocNoAgreementTextField = page.locator('#bundlesRespondentAgreedDocWithNo');
    this.hearingsList = page.locator('#bundlesRespondentSelectHearing');
    this.respondentDocOnlyOption = page.locator("#bundlesRespondentWhoseDocuments-Respondent\\'s\\ documents\\ only");
    this.bothPartiesDocOption = page.locator("#bundlesRespondentWhoseDocuments-Both\\ parties\\'\\ hearing\\ documents\\ combined");
    this.witnessStatementOption = page.locator(`#bundlesRespondentWhatDocuments-Witness\\ statements\\ only`);
    this.supplementaryHearingDocOption = page.locator("#bundlesRespondentTypeOfDocument-Supplementary\\ hearing\\ document");
    this.hearingDocWithWitnessStatementOption = page.locator(`#bundlesRespondentWhatDocuments-Hearing documents,\\ including\\ witness\\ statements`);
    this.uploadDocumentInput = page.locator(`#bundlesRespondentUploadFile`);
    this.closeAndReturnToCaseDetailsButton = page.getByRole('button', { name: 'Close and Return to case details' });
  }

  async assertPageHeading() {
    const heading = this.page.getByRole('heading', { name: 'Prepare and submit documents for a hearing' });
    await expect(heading).toBeVisible();
  }

  async selectAgreementOption(option: string, details?: string) {
    switch (option) {
      case 'Yes':
        await expect(this.prepDocYesOption).toBeVisible();
        await this.prepDocYesOption.click();
        return 'Yes';
      case 'Agreed With restriction':
        await expect(this.prepDocAgreeWithResOption).toBeVisible();
        await this.prepDocAgreeWithResOption.click();
        await this.prepDocAgreeWithResTextField.fill(details ? details : 'Agreed with restrictions details');
        return 'We have agreed that this set of documents will be uploaded but we disagree about whether some of the documents should be referred to at the hearing.';
      case 'No':
        await expect(this.prepDocNoAgreementOption).toBeVisible();
        await this.prepDocNoAgreementOption.click();
        await this.prepDocNoAgreementTextField.fill(details ? details : 'NOT agreed details');
        return 'No, we have not agreed and I want to provide my own documents';
      default:
        throw new Error(`Invalid option: ${option}`);
    }
  }

  async assertHearingList(hearingList: string[] = ['0 Costs Hearing - Leeds ET - 6 January 2026']) {
    await expect(this.hearingsList).toBeVisible();
    const options = this.hearingsList.locator('option');
    const count = await options.count();
    const optionTextList = [];
    for (let i = 0; i < count; i++) {
      if(i===0) continue; //skip first option 'Select'
      optionTextList.push(await options.nth(i).textContent());
    }
    for (const expectedOption of hearingList) {
      if (!optionTextList.includes(expectedOption)) {
        throw new Error(`Hearing option "${expectedOption}" not found in the list.`);
      }
    }
  }

  async selectHearing(hearingOption?: string) {
    await expect(this.hearingsList).toBeVisible();
    await this.hearingsList.selectOption(
      hearingOption ? { label: hearingOption } : { index: 1 }
    );
  }

  async whoseDocumentsOption(option: string) {
    switch (option) {
      case 'Respondent':
        await expect(this.respondentDocOnlyOption).toBeVisible();
        await this.respondentDocOnlyOption.click();
        return `Respondent's documents only`;
      case 'Both Parties':
        await expect(this.bothPartiesDocOption).toBeVisible();
        await this.bothPartiesDocOption.click();
        return `Both parties' hearing documents combined`;
      default:
        throw new Error(`Invalid option: ${option}`);
    }
  }

  async selectDocumentType(docType: string) {
    switch (docType) {
      case 'Witness statement only':
        await expect(this.witnessStatementOption).toBeVisible();
        await this.witnessStatementOption.click();
        return `Witness statements (only upload these if the tribunal has said witness statements are to be used)`;
      case 'Supplementary hearing document':
        await expect(this.supplementaryHearingDocOption).toBeVisible();
        await this.supplementaryHearingDocOption.click();
        return `Documents that will be referred to by witnesses when giving evidence, sometimes called the "hearing bundle"`;
      case 'Hearing Document including witness statement':
        await expect(this.hearingDocWithWitnessStatementOption).toBeVisible();
        await this.hearingDocWithWitnessStatementOption.click();
        return `A combined hearing bundles and witness statements`;
      default:
        throw new Error(`Invalid document type: ${docType}`);
    }
  }

  async uploadDocuments() {
    await this.commonActionsHelper.uploadWithRateLimitRetry(
      this.page,
      this.uploadDocumentInput,
      await this.commonActionsHelper.createAliasPDFPayload(
        'src/test/e2e/resources/test_file/welshTest.pdf',
        'welshTest.pdf'
      )
    );
  }

  async confirmDocumentsSentToTribunal() {
    const confirmText = this.page.getByText('You have sent your hearing documents to the tribunal');
    await expect(confirmText).toBeVisible();
  }

  async closeAndReturnToCaseDetails() {
    await expect(this.closeAndReturnToCaseDetailsButton).toBeVisible();
    await this.closeAndReturnToCaseDetailsButton.click();
  }

  async submitDocumentForHearing(param:{
    agreementOption: string,
    agreementDetails?: string,
    hearingOption: string,
    whoseDocuments: string,
    documentType: string,
    hearingList?: string[],
  }, checkYourAnswersPage: CheckYourAnswersPage) {
    await this.assertPageHeading();
    await this.clickContinue();
    const agreementOption = await this.selectAgreementOption(param.agreementOption, param.agreementDetails);
    await this.clickContinue();
    if(param.hearingList) {
      await this.assertHearingList(param.hearingList);
    }
    await this.selectHearing(param.hearingOption);
    const whoseDoc = await this.whoseDocumentsOption(param.whoseDocuments);
    const docType = await this.selectDocumentType(param.documentType);
    await this.clickContinue();
    await this.uploadDocuments();
    await this.clickContinue();
    const rows = [
      { cellItem: `Have you agreed with the other party that this PDF set of documents will be used by both parties at the hearing and that no other documents will be referred to?`,
        value: agreementOption
      },
      { cellItem: `Select the hearing these documents are for`, value: param.hearingOption },
      { cellItem: `Whose hearing documents are you uploading?`, value: whoseDoc },
      { cellItem: `What are these documents?`, value: docType },
      { cellItem: `Upload document`, value: `welshTest.pdf`},
    ]
    if (param.agreementOption === 'No') {
      rows.push({
        cellItem: `Tell us why you’ve not been able to agree with the other party`,
        value: param.agreementDetails ? param.agreementDetails : 'NOT agreed details'
      });
    } else if (param.agreementOption === 'Agreed With restriction') {
      rows.push({
        cellItem: `Tell us which documents are disputed`,
        value: param.agreementDetails ? param.agreementDetails : 'Agreed with restrictions details'
      });
    }
    await checkYourAnswersPage.assertCheckYourAnswersPage({
      tableName: "Check your answers",
      rows: rows
    });
    await this.clickSubmitButton();
    await this.confirmDocumentsSentToTribunal();
    await this.closeAndReturnToCaseDetails();
  }
}

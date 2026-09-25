import { expect, Locator, Page } from '@playwright/test';
import ContactTheTribunalPage from './ContactTheTribunalPage.ts';

export default class PrepareAndSubmitDocumentPage extends ContactTheTribunalPage {

  private readonly prepareAndSubmitDocumentPageHeading: Locator;
  private readonly startPreparingHearingDocButton: Locator;
  private readonly agreeTheseDocumentsWithOtherPartyYesRadio: Locator;
  private readonly someDisputedDocumentsRadio: Locator;
  private readonly notAgreedRadio: Locator;

  private readonly aboutYourHearingHeading: Locator;
  private readonly firstHearingRadio: Locator;
  private readonly myHearingDocumentsRadio: Locator;
  private readonly bothPartiesDocumentsRadio: Locator;
  private readonly allHearingDocumentsRadio: Locator;
  private readonly supplementaryDocumentsRadio: Locator;
  private readonly witnessStatementsRadio: Locator;

  private readonly uploadYourFileOfDocumentsPage: Locator;
  private readonly chooseFile: Locator;
  private readonly uploadFile: Locator;

  constructor(page: Page) {
    super(page);
    this.startPreparingHearingDocButton = this.page.getByRole('button', { name: 'Start now' });
    this.prepareAndSubmitDocumentPageHeading = this.page.getByRole('heading', { name: 'Prepare and submit documents for a hearing' });
    this.agreeTheseDocumentsWithOtherPartyYesRadio = this.page.locator(`#bundlesRespondentAgreedDocWith`);
    this.someDisputedDocumentsRadio = this.page.locator(`#bundlesRespondentAgreedDocWith-2`);
    this.notAgreedRadio = this.page.locator(`#bundlesRespondentAgreedDocWith-3`);
    this.aboutYourHearingHeading = this.page.getByRole('heading', { name: 'About your hearing documents' });
    this.firstHearingRadio = this.page.locator(`#about-hearing-documents1`);
    this.myHearingDocumentsRadio = this.page.locator(`#about-hearing-documents2`);
    this.bothPartiesDocumentsRadio = this.page.locator(`#about-hearing-documents2-2`);
    this.allHearingDocumentsRadio = this.page.locator(`#about-hearing-documents3`);
    this.supplementaryDocumentsRadio = this.page.locator(`#about-hearing-documents3-2`);
    this.witnessStatementsRadio = this.page.locator(`#about-hearing-documents3-3`);
    this.uploadYourFileOfDocumentsPage = this.page.getByRole('heading', { name: 'Upload your file of documents' });
    this.chooseFile = this.page.locator(`#hearingDocument`);
    this.uploadFile = this.page.locator(`#upload`);
  }

  async assertPrepareAndSubmitDocumentsPage() {
    await this.page.waitForLoadState('load');
    await expect(this.prepareAndSubmitDocumentPageHeading).toBeVisible();
  }

  async selectHaveYouAgreedTheseDocumentsWithOtherParty(option: string) {
    switch (option.toLowerCase()) {
      case 'yes':
        await expect(this.agreeTheseDocumentsWithOtherPartyYesRadio).toBeVisible();
        await this.agreeTheseDocumentsWithOtherPartyYesRadio.click();
        return 'Yes';
      case 'some disputed documents':
        await expect(this.someDisputedDocumentsRadio).toBeVisible();
        await this.someDisputedDocumentsRadio.click();
        return 'We have agreed but there are some disputed documents';
      case 'no':
        await expect(this.notAgreedRadio).toBeVisible();
        await this.notAgreedRadio.click();
        return 'No, we have not agreed and I want to provide my own documents';
      default:
        throw new Error(`Unknown option '${option}'`);
    }
  }

  async assertAboutYourHearingDocumentPage() {
    await this.page.waitForLoadState('load');
    await expect(this.aboutYourHearingHeading).toBeVisible();
  }

  async selectFirstHearing() {
    await expect(this.firstHearingRadio).toBeVisible();
    const hearing = await this.firstHearingRadio.locator(`xpath=/following-sibling::label`).innerText();
    await this.firstHearingRadio.click();
    return hearing.trim();
  }

  async selectWhoseDocumentsAreYouUploading(option: string) {
    switch (option.toLowerCase()) {
      case 'my documents':
        await expect(this.myHearingDocumentsRadio).toBeVisible();
        await this.myHearingDocumentsRadio.click();
        return 'My hearing documents only';
      case 'both parties':
        await expect(this.bothPartiesDocumentsRadio).toBeVisible();
        await this.bothPartiesDocumentsRadio.click();
        return 'Both parties’ hearing documents combined';
      default:
        throw new Error(`Unknown option '${option}'`);
    }
  }

  async selectWhatAreTheseDocuments(option: string) {
    switch (option.toLowerCase()) {
      case 'all':
        await expect(this.allHearingDocumentsRadio).toBeVisible();
        await this.allHearingDocumentsRadio.click();
        return 'All hearing documents';
      case 'supplementary':
        await expect(this.supplementaryDocumentsRadio).toBeVisible();
        await this.supplementaryDocumentsRadio.click();
        return 'Supplementary or other documents';
      case 'witness':
        await expect(this.witnessStatementsRadio).toBeVisible();
        await this.witnessStatementsRadio.click();
        return 'Witness statements only';
      default:
        throw new Error(`Unknown option '${option}'`);
    }
  }

  async assertUploadYourFileOfDocumentsPage() {
    await this.page.waitForLoadState('load');
    await expect(this.uploadYourFileOfDocumentsPage).toBeVisible();
  }

  async uploadYourFileOfDocuments(files: string[] = ['src/test/e2e/resources/test_file/welshTest.pdf']) {
      for (let file of files) {
        await expect(this.chooseFile).toBeVisible();
        await this.commonActionsHelper.uploadWithRateLimitRetry(
          this.page,
          this.chooseFile,
          file
        );
        await this.page.waitForLoadState('load');
        await this.uploadFile.click();
        await this.page.waitForLoadState('load');
      }
  }

  async assertSubmitDocumentCheckYourAnswersPage(agreed: string, hearing: string, whoseDoc: string, whatDoc: string) {
    await this.page.waitForLoadState('load');
    const checkYourAnswersPageTitle = this.page.getByRole('heading', { name: 'Check your answers' });
    await expect(checkYourAnswersPageTitle).toBeVisible();

    const cyaList = [
      { key: 'Have you agreed these documents?', value: agreed},
      { key: 'Select the hearing this document is for', value: hearing},
      { key: 'Whose hearing document are you uploading?', value: whoseDoc},
      { key: 'What is this document?', value: whatDoc},
      { key: 'Your documents', value: 'welshTest.pdf'}
    ];

    for ( const cya of cyaList ) {
      const cyaValue = this.page.locator(`xpath=//dt[normalize-space()='${cya.key}']/following-sibling::dd[1]`);
      await expect(cyaValue).toBeVisible();
      await expect(cyaValue).toHaveText(cya.value);
    }
  }

  async assertConfirmDocumentsSubmittedSuccessfully() {
    await expect(this.page.getByRole('heading', { name: 'You have sent your hearing documents to the tribunal' }))
      .toBeVisible();
    await expect(
      this.page.getByText('Your documents are now uploaded. The tribunal will let you know if they have any ' +
        'questions about the documents you have submitted.')
    ).toBeVisible();
  }

  async submitDocumentsForHearing() {
      await this.page.waitForLoadState('load');
      await this.assertContactTheTribunalPageIsDisplayed();
      await this.selectApplicationTypeTobeMadeToTribunal('submit document for hearing');

      await this.assertPrepareAndSubmitDocumentsPage();

      await this.startPreparingHearingDocButton.click();

      const agreed = await this.selectHaveYouAgreedTheseDocumentsWithOtherParty('yes');
      await this.clickContinue();

      await this.assertAboutYourHearingDocumentPage();
      const hearing = await this.selectFirstHearing();
      const whoseDoc = await this.selectWhoseDocumentsAreYouUploading('my documents');
      const whatDoc = await this.selectWhatAreTheseDocuments('witness');
      await this.clickContinue();

      await this.assertUploadYourFileOfDocumentsPage();
      await this.uploadYourFileOfDocuments();
      await this.clickContinue();

      await this.assertSubmitDocumentCheckYourAnswersPage(agreed, hearing, whoseDoc, whatDoc);
      await this.clickSubmitButton();

      await this.assertConfirmDocumentsSubmittedSuccessfully();
      await this.clickCloseAndReturn();
  }

}

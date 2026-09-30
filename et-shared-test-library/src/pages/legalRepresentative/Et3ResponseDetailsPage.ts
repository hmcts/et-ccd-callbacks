import { Et3DetailsPage } from './Et3DetailsPage.ts';
import { expect, Locator, Page } from '@playwright/test';
import { CommonActionsHelper } from '../helpers/CommonActionsHelper.ts';
import { CheckYourAnswersPage } from '../helpers/CheckYourAnswersPage.ts';

export class Et3ResponseDetailsPage extends Et3DetailsPage {
  private readonly et3ResponseDetailsPageTitle: Locator;
  private readonly acasReconciliationTitle: Locator;
  private readonly acasReconciliationRadioGroup: Locator;
  private readonly respondentContestClaimTitle: Locator;
  private readonly respondentContestClaimYesOption: Locator;
  private readonly respondentContestClaimNoOption: Locator;
  private readonly employeeContractClaimTitle: Locator;
  private readonly employeeContractClaimRadioGroup: Locator;
  private readonly eccBackgroundTitle: Locator;
  private readonly eccBackgroundTextInput: Locator;
  private readonly eccFileUploadInput: Locator;

  constructor(page: Page) {
    super(page);
    this.et3ResponseDetailsPageTitle = this.page.getByText('ET3 - Response Details');
    this.acasReconciliationTitle = this.page.getByRole('heading', {
      name: 'Do you agree with the details given by the claimant about early conciliation with Acas?',
    });
    this.acasReconciliationRadioGroup = this.page.locator(`#et3ResponseAcasAgree`);
    this.respondentContestClaimTitle = this.page.getByRole('heading', {
      name: 'Does the respondent contest the claim?',
    });
    this.respondentContestClaimYesOption = this.page.locator(`#et3ResponseRespondentContestClaim-Yes`);
    this.respondentContestClaimNoOption = this.page.locator(`#et3ResponseRespondentContestClaim-No`);
    this.employeeContractClaimTitle = this.page.getByRole('heading', {
      name: "Does the respondent wish to make an Employer's Contract Claim?",
    });
    this.employeeContractClaimRadioGroup = this.page.locator(`#et3ResponseEmployerClaim`);
    this.eccBackgroundTitle = this.page.getByRole('heading', {
      name: "Provide the background and details of your Employer's Contract Claim",
    });
    this.eccBackgroundTextInput = this.page.locator(`#et3ResponseEmployerClaimDetails`);
    this.eccFileUploadInput = this.page.locator(`#et3ResponseEmployerClaimDocument`);
  }

  async assertEt3ResponseDetailsPageIsDisplayed() {
    await this.page.waitForLoadState('load');
    await expect(this.et3ResponseDetailsPageTitle).toBeVisible();
  }

  async selectDoYouAgreeWithAcasReconciliation(option: string) {
    await expect(this.acasReconciliationTitle).toBeVisible();
    await expect(this.acasReconciliationRadioGroup).toBeVisible();
    const optionLocator = this.acasReconciliationRadioGroup.getByRole('radio', { name: option });
    await expect(optionLocator).toBeVisible();
    await optionLocator.check();
  }

  async selectContestClaimOption(option: string) {
    await expect(this.respondentContestClaimTitle).toBeVisible();
    const optionLocator = option === 'Yes' ? this.respondentContestClaimYesOption : this.respondentContestClaimNoOption;
    await expect(optionLocator).toBeVisible();
    await optionLocator.check();
  }

  async selectEmployeeContractClaimOption(option: string) {
    await expect(this.employeeContractClaimTitle).toBeVisible();
    await expect(this.employeeContractClaimRadioGroup).toBeVisible();
    const optionLocator = this.employeeContractClaimRadioGroup.getByRole('radio', { name: option });
    await expect(optionLocator).toBeVisible();
    await optionLocator.check();
  }

  async enterEccBackgroundDetails(details: string) {
    await expect(this.eccBackgroundTitle).toBeVisible();
    await expect(this.eccBackgroundTextInput).toBeVisible();
    await this.eccBackgroundTextInput.fill(details);

    await this.commonActionsHelper.uploadWithRateLimitRetry(
      this.page,
      this.eccFileUploadInput,
      'src/test/e2e/resources/test_file/welshTest.pdf',
    );
  }

  async enterEt3ResponseDetails(cyaPage: CheckYourAnswersPage) {
    await this.assertEt3ResponseDetailsPageIsDisplayed();
    await this.clickContinue();

    await this.selectRespondentName();
    await this.clickContinue();

    await this.selectDoYouAgreeWithAcasReconciliation('Yes');
    await this.clickContinue();

    await this.selectContestClaimOption('No');
    await this.clickContinue();

    await this.selectEmployeeContractClaimOption('Yes');
    await this.clickContinue();

    await this.enterEccBackgroundDetails("This is the background and details of the Employer's Contract Claim");
    await this.clickContinue();

    await cyaPage.assertCheckYourAnswersPage({
      tableName: 'Check your answers',
      rows: [
        'Mrs Test Auto',
        {
          cellItem: 'Do you agree with the details given by the claimant about early conciliation with Acas?',
          value: 'Yes',
        },
        {
          cellItem: 'Does the respondent contest the claim?',
          value: 'No - the respondent does not contest any of the claim',
        },
        { cellItem: "Does the respondent wish to make an Employer's Contract Claim?", value: 'Yes' },
        {
          cellItem: "Provide the background and details of your Employer's Contract Claim",
          value: "This is the background and details of the Employer's Contract Claim",
        },
        {cellItem: 'Add a document', value:'welshTest.pdf' }
      ],
    });

    await this.clickET3SaveAsDraftButton();
    await expect(this.submitEt3FormButton).toBeVisible();

    await this.clickCloseAndReturn();
  }
}

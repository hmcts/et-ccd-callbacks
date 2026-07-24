import { BasePage } from './basePage';
import { expect, Locator, Page } from '@playwright/test';
import dateUtilComponent from '../data-utils/DateUtilComponent';
import { users } from '../config/config.dynamic.ts';

export default class CaseListPage extends BasePage {
  private readonly caseListLink: Locator;
  private readonly caseTypeDropdown: Locator;
  private readonly submissionReferenceLocator: Locator;
  private readonly applyButton: Locator;
  private readonly createCaseLink: string = 'Create case';
  private readonly jurisdictionDropdownLR: Locator;
  private readonly casetypeDropdownLR: Locator;
  private readonly eventLR: Locator;
  private readonly venueDropdown: Locator;
  private readonly causeListText: Locator;
  private readonly expandImgIcon: Locator;
  private readonly stateDropdown: Locator;
  private readonly managingOfficeDropdown: Locator;
  private readonly caseNumberField: Locator;
  private readonly submissionReferenceField: Locator;

  constructor(page: Page) {
    super(page);
    this.caseListLink = page.locator('[href="/cases"]');
    this.caseTypeDropdown = page.locator('#wb-case-type');
    this.submissionReferenceLocator = page.locator('#feeGroupReference');
    this.applyButton = page.locator('//button[@class="button workbasket-filters-apply"]');
    this.jurisdictionDropdownLR = page.locator('#cc-jurisdiction');
    this.casetypeDropdownLR = page.locator('#cc-case-type');
    this.eventLR = page.locator('#cc-event');
    this.venueDropdown = page.locator('#listingVenue');
    this.causeListText = page.locator('//div[@class="alert-message"]');
    this.expandImgIcon = page.locator('div a img');
    this.stateDropdown = page.getByLabel('State');
    this.managingOfficeDropdown = page.getByLabel('Managing Office');
    this.caseNumberField = page.getByLabel('Case Number');
    this.submissionReferenceField = page.getByLabel('Submission Reference');
  }

  async selectJurisdiction(jurisdiction: string) {
    const jurisdictionDropdown = this.page.getByLabel('Jurisdiction');
    await expect(jurisdictionDropdown).toBeVisible();
    await jurisdictionDropdown.selectOption(jurisdiction);
  }

  async selectCaseType(caseType: string) {
    const caseTypeDropdown = this.page.getByLabel('Case type');
    await expect(caseTypeDropdown).toBeVisible();
    await caseTypeDropdown.selectOption(caseType);
  }

  async selectState(state: string) {
    await expect(this.stateDropdown).toBeVisible();
    await this.stateDropdown.selectOption(state);
  }

  async selectTribunalOffice(office: string) {
    const tribunalOfficeDropdown = this.page.getByLabel('Tribunal office');
    await expect(tribunalOfficeDropdown).toBeVisible();
    await tribunalOfficeDropdown.selectOption(office);
  }

  async enterCaseNumber(caseNumber: string) {
    await expect(this.caseNumberField).toBeVisible();
    await this.caseNumberField.fill(caseNumber);
  }

  async enterSubmissionReference(submissionReference: string) {
    await expect(this.submissionReferenceField).toBeVisible();
    await this.submissionReferenceField.fill(submissionReference);
  }

  async selectManagingOffice(office: string) {
    await expect(this.managingOfficeDropdown).toBeVisible();
    await this.managingOfficeDropdown.selectOption(office);
  }

  // needed for share case feature
  async searchCaseApplicationWithSubmissionReference(option: string, submissionReference: string) {
    await this.page.reload();
    await expect(this.caseListLink).toBeVisible();
    await this.caseListLink.click();

    await this.page.waitForLoadState('load');
    await expect(this.page.locator('h1')).toContainText('Case list');
    await expect(this.caseTypeDropdown).toBeVisible();
    await expect(this.applyButton).toBeVisible();

    switch (option) {
      case 'Eng/Wales - Singles':
        await this.caseTypeDropdown.selectOption({label:'Eng/Wales - Singles'});
        break;
      case 'Scotland - Singles':
        await this.caseTypeDropdown.selectOption({label: 'Scotland - Singles (RET)'});
        break;
      default:
        throw new Error('... check you options or add new option');
    }
    await this.submissionReferenceLocator.fill(submissionReference.toString());
    await this.applyButton.click();
  }

  async checkAndShareCaseFromList(subRef: string) {
    await this.page.locator('#select-' + subRef).check();
    await this.clickShareCaseButton();
    await this.page.waitForLoadState('load', {timeout: 3000});

    await this.page.getByRole('combobox', { name: 'Search by name or email' }).pressSequentially(users.etManageOrgSuperUser.email, { delay: 100 });
    await this.page.locator(`//mat-option[@role='option']/span[contains(.,'${users.etManageOrgSuperUser.email}')]`).click();
    await this.page.getByRole('button', { name: 'Add user' }).click();
    await this.clickContinue();

    await this.page.getByRole('button', { name: 'Confirm' }).click();
    await expect(this.page.getByLabel('Your cases have been updated')).toContainText('Your cases have been updated');
    await this.page.getByRole('link', { name: 'Go back to the case list.' }).click();
  }

  async claimantRepCreateCase(jurisdiction: string, caseType: string, postcode: string) {
    await this.page.getByText(this.createCaseLink).click();
    await this.jurisdictionDropdownLR.selectOption({label:jurisdiction});

    await this.casetypeDropdownLR.selectOption({label: caseType});
    await this.eventLR.selectOption({ label: 'Create draft claim'});
    await this.page.getByRole('button', { name: 'Start' }).click();

    await this.enterPostCode(postcode);
    await this.clickSubmitButton();
    await this.page.waitForLoadState('load', {timeout: 3000});
  }

  async searchHearingReports(option: string, state: string, officeLocation: string) {
    await expect(this.caseListLink).toBeVisible();
    await this.caseListLink.click();
    await this.page.waitForLoadState('load');

    await expect(this.caseTypeDropdown).toBeVisible();
    await expect(this.applyButton).toBeVisible();
    await expect(this.page.locator('h1')).toContainText('Case list');

    try {
      switch (option) {
        case 'Eng/Wales - Hearings/Reports':
          await this.caseTypeDropdown.selectOption({label: 'Eng/Wales - Hearings/Reports'});
          break;
        default:
          throw new Error('invalid option  '+ option);
      }
    } catch (error) {
      console.error('invalid option', error);
      throw error;
    }
    //await this.webActions.selectByLabelFromDropDown(this.elements.state, state);
    await this.selectState(state);
    //await this.webActions.selectByLabelFromDropDown(this.elements.managingOffice, officeLocation);
    await this.selectManagingOffice(officeLocation);
    await this.applyButton.click();
  }

  async selectHearingReport() {
    await expect(this.page.getByText('ETCL - Cause List')).toBeVisible({ timeout: 10000 });
    await this.page.getByText('ETCL - Cause List').click();
  }

  async generateReport() {
    await this.venueDropdown.selectOption('Newcastle CFCTC');
    await this.clickSubmitButton();
  }

  async validateHearingReport(caseNumber: string) {
    await expect(this.causeListText).toContainText('has been updated with event: Generate Report');
    await expect(this.page.locator('ccd-read-complex-field-collection-table')).toContainText('Newcastle CFCTC');
  }

  async verifyAcasCertificateDetailsOnTab(documentValue: string, docTypeValue: string) {
    await this.delay(2000);
    await expect(this.page.locator(`//button[normalize-space()="${documentValue}"]`)).toBeVisible();

    await expect(this.page.locator(`//span[normalize-space()="${docTypeValue}"]`).first()).toBeVisible();
  }

  async verifyCaseDetailsOnTab(fieldLabel: string, fieldValue: string) {
    await expect(
      this.page.locator(`//*[normalize-space()="${fieldLabel}"]/../..//td[normalize-space()="${fieldValue}"]`)
    ).toBeVisible();
  }

  async verifyBFActionsTab(fieldLabel: string, fieldValue: string) {
    await expect(this.page.getByText(dateUtilComponent.addDaysAndMonths(29))).toBeVisible();
    await this.expandImgIcon.click();
    await expect(
      this.page.locator(`//*[normalize-space()="${fieldLabel}"]/../..//td[normalize-space()="${fieldValue}"]`)
    ).toBeVisible();
  }

  async verifyDepositOrderDetailsOnTab(fieldLabel: string, fieldValue: string) {
    await expect(
      this.page.locator(`//*[normalize-space()="${fieldLabel}"]/../../td[normalize-space()="${fieldValue}"]`)
    ).toBeVisible();
  }

  async verifyET3DetailsOnRespondentTab() {
    await this.expandImgIcon.click();
    const testDataMap = new Map<string, string>([
      ['Is there an ET3 response?', 'Yes'],
      ['Select the respondent you are processing', 'Mrs Test Auto'],
      ['Did we receive the ET3 response in time?', 'Yes'],
      ["Do we have the respondent's name?", 'Yes'],
      ["Does the respondent's name match?", 'Yes'],
      ["Do we have the respondent's address?", 'Yes'],
      ["Does the respondent's address match?", 'Yes'],
      ['Is the case listed for hearing?', 'No'],
      ['Is this location correct?', 'Yes'],
      ['Does the respondent wish to contest any part of the claim?', 'No'],
    ]);

    // @ts-ignore
    for (let [key, value] of testDataMap) {
      await expect(
        this.page.locator(`//*[normalize-space()="${key}"]/../../td[normalize-space()="${value}"]`),
      ).toBeVisible();
    }
  }

  async verifyNoCasesFoundMessage() {
    await this.page.waitForLoadState('load');
    await expect(this.page.getByText('No cases found. Try using different filters.')).toBeVisible();
  }
}

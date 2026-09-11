import { BasePage } from '../basePage.ts';
import { expect, Locator, Page } from '@playwright/test';

export default class ContactTheTribunalPage extends BasePage {

  private readonly contactTheTribunalPageTitle: Locator;
  private readonly showAllSectionsLink: Locator;
  private readonly applicationTextArea: Locator;
  private readonly applicationFileUploadInput: Locator;
  private readonly yesOptionR92: Locator;
  private readonly noOptionR92: Locator;
  private readonly storeButton: Locator;
  private readonly moreInfoR92No: Locator;

  constructor(page: Page) {
    super(page);
    this.contactTheTribunalPageTitle = this.page.getByRole('heading', {name: 'Contact the tribunal about your case'});
    this.showAllSectionsLink = this.page.getByRole('button', {name: 'Show all sections'});
    this.applicationTextArea = this.page.locator(`#Contact-Application-Text`);
    this.applicationFileUploadInput = this.page.locator(`#contactApplicationFile`);
    this.yesOptionR92 = this.page.locator(`#copyToOtherPartyYesOrNo`);
    this.noOptionR92 = this.page.locator(`#copyToOtherPartyYesOrNo-2`);
    this.storeButton = this.page.getByRole('button', { name: 'Store application' });
    this.moreInfoR92No = this.page.locator(`#copyToOtherPartyText`);
  }

  async assertContactTheTribunalPageIsDisplayed() {
    await this.page.waitForLoadState('load');
    await expect(this.contactTheTribunalPageTitle).toBeVisible();
    if (await this.showAllSectionsLink.isVisible()) {
      await this.showAllSectionsLink.click();
    }
    await this.page.waitForLoadState('load');
  }

  async selectApplicationTypeTobeMadeToTribunal(applicationType: string) {
    let applicationTypeLocator: Locator;
    switch (applicationType) {
      case 'withdraw':
        applicationTypeLocator = this.page.locator(`xpath=//a[@href='/contact-the-tribunal/withdraw?lng=en']`)
        break;
      case 'change personal details':
        applicationTypeLocator = this.page.locator(`xpath=//a[@href='/contact-the-tribunal/change-details?lng=en']`)
        break;
      case 'postpone my hearing':
        applicationTypeLocator = this.page.locator(`xpath=//a[@href='/contact-the-tribunal/postpone?lng=en']`)
        break;
      case 'vary or revoke an order':
        applicationTypeLocator = this.page.locator(`xpath=//a[@href='/contact-the-tribunal/vary?lng=en']`)
        break;
      case 'have a decision considered afresh':
        applicationTypeLocator = this.page.locator(`xpath=//a[@href='/contact-the-tribunal/reconsider-decision?lng=en']`)
        break;
      case 'amend my claim':
        applicationTypeLocator = this.page.locator(`xpath=//a[@href='/contact-the-tribunal/amend?lng=en']`)
        break;
      case 'order other party to do something':
        applicationTypeLocator = this.page.locator(`xpath=//a[@href='/contact-the-tribunal/respondent?lng=en']`)
        break;
      case 'order a witness to attend':
        applicationTypeLocator = this.page.locator(`xpath=//a[@href='/contact-the-tribunal/witness?lng=en']`)
        break;
      case 'other party not complied with an order':
        applicationTypeLocator = this.page.locator(`xpath=//a[@href='/contact-the-tribunal/non-compliance?lng=en']`)
        break;
      case 'restrict publicity':
        applicationTypeLocator = this.page.locator(`xpath=//a[@href='/contact-the-tribunal/publicity?lng=en']`)
        break;
      case 'strike out response':
        applicationTypeLocator = this.page.locator(`xpath=//a[@href='/contact-the-tribunal/strike?lng=en']`)
        break;
      case 'judgement to be reconsidered':
        applicationTypeLocator = this.page.locator(`xpath=//a[@href='/contact-the-tribunal/reconsider-judgement?lng=en']`)
        break;
      case 'something else':
        applicationTypeLocator = this.page.locator(`xpath=//a[@href='/contact-the-tribunal/other?lng=en']`)
        break;
      case 'submit document for hearing':
        applicationTypeLocator = this.page.locator(`xpath=//a[@href='/prepare-documents?lng=en']`)
        break;
      default:
        throw new Error(`Application type: ${applicationType} is not recognized.`);
    }
    await expect(applicationTypeLocator).toBeVisible();
    const applicationTypeText = await applicationTypeLocator.textContent();
    await applicationTypeLocator.click();
    await this.page.waitForLoadState('load');
    return applicationTypeText??'';
  }

  async fillApplicationDetails(details: string) {
    await expect(this.applicationTextArea).toBeVisible();
    await this.applicationTextArea.fill(details);

    await this.commonActionsHelper.uploadWithRateLimitRetry(
      this.page,
      this.applicationFileUploadInput,
      'src/test/e2e/resources/test_file/test.txt'
    );
  }

  async selectR92Option(option: string) {
    const correspondenceHeading = this.page.getByRole('heading', {name: 'Copy this correspondence to the other party'});
    await expect(correspondenceHeading).toBeVisible();

    switch (option.toLowerCase()) {
      case 'yes':
        await expect(this.yesOptionR92).toBeVisible();
        await this.yesOptionR92.check();
        break;
        case 'no':
        await expect(this.noOptionR92).toBeVisible();
        await this.noOptionR92.check();
        await this.moreInfoR92No.fill('This is Correspondence No Claimant')
        break;
      default:
        throw new Error(`R92 option: ${option} is not recognized.`);
    }
  }

  async assertCheckYourAnswersPage(applicationType: string, details: string, r92Option: string) {
    await this.page.waitForLoadState('load');
    const checkYourAnswersPageTitle = this.page.getByRole('heading', {name: 'Check your answers'});
    await expect(checkYourAnswersPageTitle).toBeVisible();

    const applicationTypeCya = this.page.locator(`xpath=//dt[normalize-space()='Application type']/following-sibling::dd[1]`);
    await expect(applicationTypeCya).toBeVisible();
    await expect(applicationTypeCya).toHaveText(applicationType);

    const whatToTellTribCya = this.page
      .locator(`xpath=//dt[normalize-space()='What do you want to tell or ask the tribunal?']/following-sibling::dd[1]`);
    await expect(whatToTellTribCya).toBeVisible();
    await expect(whatToTellTribCya).toHaveText(details);

    const supportMaterialCya = this.page
      .locator(`xpath=//dt[normalize-space()='Supporting material']/following-sibling::dd[1]`);
    await expect(supportMaterialCya).toBeVisible();
    await expect(supportMaterialCya).toHaveText('test.txt');

    const r92Cya = this.page
      .locator(`xpath=//dt[normalize-space()='Do you want to copy this correspondence to the other party to satisfy the Rules of Procedure?']/following-sibling::dd[1]`);
    await expect(r92Cya).toBeVisible();
    await expect(r92Cya).toContainText(r92Option);
  }

  async assertApplicationSentSuccessPageIsDisplayed() {
    await this.page.waitForLoadState('load');
    const applicationSentSuccessPageTitle = this.page.getByRole('heading', {name: 'You have sent your application to the tribunal'});
    await expect(applicationSentSuccessPageTitle).toBeVisible();
  }

  async makeApplicationToTribunal(applicationType: string, details: string, r92Option: string) {
    await this.assertContactTheTribunalPageIsDisplayed();

    let applicationTypeText = await this.selectApplicationTypeTobeMadeToTribunal(applicationType);
    await this.fillApplicationDetails(details);

    await this.clickContinue();

    await this.selectR92Option(r92Option);
    await this.clickContinue();

    await this.assertCheckYourAnswersPage(applicationTypeText, details, r92Option);
    await this.page.waitForLoadState('load');
  }

  async clickStoreApplication() {
    await this.storeButton.click();
  }
  async assertApplicationStoredSuccessPageIsDisplayed() {
    await this.page.waitForLoadState('load');
    const applicationSentSuccessPageTitle = this.page.getByRole('heading', {name: 'You have stored your application'});
    await expect(applicationSentSuccessPageTitle).toBeVisible();
  }

}

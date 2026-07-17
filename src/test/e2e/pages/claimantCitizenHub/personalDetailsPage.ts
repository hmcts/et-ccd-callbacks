import { expect, Locator, Page } from '@playwright/test';
import CitizenHubPage from './CitizenHubPage.ts';

export default class PersonalDetailsPage extends CitizenHubPage {

  private readonly personalDetailsLink: Locator;
  private readonly dobHeading: Locator;
  private readonly dobGroup: Locator;
  private readonly sexAndPreferredTitleHeading: Locator;
  private readonly homeAddressHeading: Locator;
  private readonly postCodeInput: Locator;
  private readonly selectAnAddressHeading: Locator;
  private readonly selectAddressDropdown: Locator;
  private readonly telephoneNumberHeading: Locator;
  private readonly telephoneNumberInput: Locator;
  private readonly communicationPreferenceHeading: Locator;
  private readonly formatOfContactRadioGroup: Locator;
  private readonly languageRadioGroup: Locator;
  private readonly speakingLanguageRadioGroup: Locator;
  private readonly hearingFormatHeading: Locator;
  private readonly hearingFormatQuestion: Locator;
  private readonly hearingFormatAudioOption: Locator;
  private readonly hearingFormatVideoOption: Locator
  private readonly extraSupportHeading: Locator;
  private readonly reasonableAdjustmentQuestion: Locator;
  private readonly reasonableAdjustmentNo: Locator;

  constructor(page: Page) {
    super(page);
    this.personalDetailsLink = this.page.locator(`a[href="/dob-details?lng=en"]`);
    this.dobHeading = this.page.locator(`fieldset:has(legend:text("What is your date of birth?"))`);
    this.dobGroup = this.page.locator('#dobDate');
    this.sexAndPreferredTitleHeading = this.page.getByRole('heading', { name: 'Sex and preferred title' });
    this.homeAddressHeading = this.page.getByRole('heading', { name: 'What is your contact or home address?' });
    this.postCodeInput = this.page.locator(`#addressEnterPostcode`);
    this.selectAnAddressHeading = this.page.getByRole('heading', { name: 'Select an address' });
    this.selectAddressDropdown = this.page.locator('#addressAddressTypes');
    this.telephoneNumberHeading = this.page.getByRole('heading', { name: 'What is your telephone number? (optional)' });
    this.telephoneNumberInput = this.page.locator('#telephone-number');
    this.communicationPreferenceHeading = this.page.getByRole('heading', { name: 'Communication preference (Optional)' });
    this.formatOfContactRadioGroup = page.locator('fieldset:has(legend:text("What format would you like to be contacted in?"))');
    this.languageRadioGroup = page.locator('fieldset:has(legend:text("What language do you want us to use when we contact you?"))');
    this.speakingLanguageRadioGroup = page.locator('fieldset:has(legend:text("If a hearing is required, what language do you want to speak at a hearing?"))');
    this.hearingFormatHeading = this.page.getByRole('heading', { name: 'Hearing format (Optional)' });
    this.hearingFormatQuestion = this.page.locator('fieldset:has(legend:text("Would you be able to take part in hearings by video and phone?"))');
    this.hearingFormatAudioOption = this.page.locator(`#hearingPreferences-2`);
    this.hearingFormatVideoOption = this.page.locator(`#hearingPreferences`);
    this.extraSupportHeading = this.page.getByRole('heading', { name: 'Extra support during your case (Optional)' });
    this.reasonableAdjustmentQuestion = this.page.locator('fieldset:has(legend:text("Do you have a physical, mental or learning disability or long term health condition that means you need support during your case?"))');
    this.reasonableAdjustmentNo = this.page.locator('#reasonableAdjustments-2');
  }

  //click personal details link and enter details
  async clickPersonalDetailsLink() {
    await this.page.waitForLoadState('load');
    await this.personalDetailsLink.click();
  }

  async enterDob() {
    await Promise.all([
      expect(this.dobHeading).toBeVisible(),
       expect(this.dobGroup).toBeVisible()
    ]);
    //enter date of birth
    await this.dobGroup.getByLabel('Day').fill('02');
    await this.dobGroup.getByLabel('Month').fill('01');
    await this.dobGroup.getByLabel('Year').fill('1990');
    await this.saveAndContinueButton();
  }

  async selectSexAndTitle() {
    await this.page.waitForLoadState('load');
    //select sex and enter title
    await expect(this.sexAndPreferredTitleHeading).toBeVisible();
    await this.page.locator(`#sex`).check();

    await this.page.locator('#preferredTitle').fill('Mr');
    await this.saveAndContinueButton();
  }

  async enterPostcode(postcode: string, addressOption: string) {
    await this.page.waitForLoadState('load');
    await expect(this.homeAddressHeading).toBeVisible();
    //Enter postcode for claimant address
    await this.postCodeInput.fill(postcode);
    await this.saveAndContinueButton();

    await expect(this.selectAnAddressHeading).toBeVisible();
    await this.selectAddressDropdown.selectOption(addressOption);
    await this.saveAndContinueButton();

    await expect(this.homeAddressHeading).toBeVisible();
    await this.saveAndContinueButton();
  }

  async enterTelephoneNumber() {
    await this.page.waitForLoadState('load');
    //Enter telephone number
    await expect(this.telephoneNumberHeading).toBeVisible();
    await this.telephoneNumberInput.fill('07898787676');
    await this.saveAndContinueButton();
  }

  private async selectFormatOfContact(option: string) {
    await this.page.waitForLoadState('load');
    await expect(this.formatOfContactRadioGroup).toBeVisible();
    const optionLocator = this.formatOfContactRadioGroup.getByRole('radio', { name: option });
    await expect(optionLocator).toBeVisible();
    await optionLocator.check();
  }

  private async selectLanguageTobeContactedIn(option: string) {
    await this.page.waitForLoadState('load');
    await expect(this.languageRadioGroup).toBeVisible();
    let optionLocator = this.languageRadioGroup.getByRole('radio', { name: option });
    switch (option.toLowerCase()) {
      case 'english':
        optionLocator = this.languageRadioGroup.locator(`#update-preference-language-2`);
        break;
      case 'welsh':
        optionLocator = this.languageRadioGroup.locator(`#update-preference-language`);
        break;
      default:
        throw new Error(
          '... check the provided language option. you have not specified a valid language such as English or Welsh',
        );
    }
    await expect(optionLocator).toBeVisible();
    await optionLocator.click();
  }

  private async selectHearingLanguage(option: string) {
    await this.page.waitForLoadState('load');
    await expect(this.speakingLanguageRadioGroup).toBeVisible();
    const optionLocator = this.speakingLanguageRadioGroup.getByRole('radio', { name: option });
    await expect(optionLocator).toBeVisible();
    await optionLocator.check();
  }

  async selectHowToBeContacted(location: string, format:string = 'Email', contactLanguage: string = 'English', hearingLanguage: string = 'English') {
    //select option for how to be contacted
    //the communication preference combination should be tested in functional-test
    await expect(this.communicationPreferenceHeading).toBeVisible();
    await this.selectFormatOfContact(format);

    switch (location) {
      case 'England':
      case 'Wales':
      case 'England & Wales':
        await Promise.all([
          this.selectLanguageTobeContactedIn(contactLanguage),
          this.selectHearingLanguage(hearingLanguage)
        ]);
        break;
      case 'Scotland':
        // do nothing
        break;
      default:
        throw new Error(
          '... check the provided location. you have not specified a valid location such as England, Wales or Scotland',
        );
    }
    await this.saveAndContinueButton();
  }

  async selectHearingPreference() {
    await this.page.waitForLoadState('load');
    //Select hearing preference option - video hearing
    await Promise.all([
      expect(this.hearingFormatHeading).toBeVisible(),
      expect(this.hearingFormatQuestion).toBeVisible(),
      expect(this.hearingFormatAudioOption).toBeVisible(),
      expect(this.hearingFormatVideoOption).toBeVisible()
    ]);


    await this.hearingFormatVideoOption.click();
    await this.saveAndContinueButton();
  }

  async selectReasonableAdjustment() {
    //Select No to reasonable adjustment question
    await this.page.waitForLoadState('load');
    await Promise.all([
      expect(this.extraSupportHeading).toBeVisible(),
      expect(this.reasonableAdjustmentQuestion).toBeVisible(),
      expect(this.reasonableAdjustmentNo).toBeVisible()

  ]);
    await this.reasonableAdjustmentNo.click();
    await this.saveAndContinueButton();
  }

  /**
   * Process personal details section
   * @param postcode
   * @param location
   * @param addressOption
   */
  async processPersonalDetails(postcode: string, location: string, addressOption: string) {
    await this.page.waitForLoadState('load');
    await this.clickPersonalDetailsLink();
    await this.enterDob();
    await this.selectSexAndTitle();
    await this.enterPostcode(postcode, addressOption);
    await this.enterTelephoneNumber();
    await this.selectHowToBeContacted(location);
    await this.selectHearingPreference();
    await this.selectReasonableAdjustment();
    await this.confirmHaveYouCompletedThisSection();
  }
}

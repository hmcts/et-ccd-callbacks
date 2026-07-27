import { BasePage } from '../basePage.ts';
import { expect, Locator, Page } from '@playwright/test';
import { CheckYourAnswersPage } from '../helpers/CheckYourAnswersPage.ts';
import { Events } from '../../config/case-data.ts';
import { AddressDetails } from '../../types/address.ts';

export default class AmendContactDetailsLrPage extends BasePage {

  private readonly amendContactDetailsTitle: Locator;
  private readonly representativeContactChangeOptionGroup: Locator;
  private readonly repPhoneNumberInput: Locator;
  private readonly repAddressPostCodeInput: Locator;
  private readonly repAddressListDropdown: Locator;
  private readonly repAddressLine1: Locator;
  private readonly repAddressLine2: Locator;
  private readonly repAddressLine3: Locator;
  private readonly repTownOrCityInput: Locator;
  private readonly repCountyInput: Locator;
  private readonly repPostCodeInput: Locator;
  private readonly repCountryInput: Locator;

  constructor(page: Page) {
    super(page);
    this.amendContactDetailsTitle = this.page.getByRole('heading', { name: 'Amend contact details' });
    this.representativeContactChangeOptionGroup = this.page.locator(`#representativeContactChangeOption`);
    this.repPhoneNumberInput = this.page.locator(`#representativePhoneNumber`);
    this.repAddressPostCodeInput = this.page.locator(`#representativeAddress_representativeAddress_postcodeInput`);
    this.repAddressListDropdown = this.page.locator(`#representativeAddress_representativeAddress_addressList`);
    this.repAddressLine1 = this.page.locator(`#representativeAddress__detailAddressLine1`);
    this.repAddressLine2 = this.page.locator(`#representativeAddress__detailAddressLine2`);
    this.repAddressLine3 = this.page.locator(`#representativeAddress__detailAddressLine3`);
    this.repTownOrCityInput = this.page.locator(`#representativeAddress__detailPostTown`);
    this.repCountyInput = this.page.locator(`#representativeAddress__detailCounty`);
    this.repPostCodeInput = this.page.locator(`#representativeAddress__detailPostCode`);
    this.repCountryInput = this.page.locator(`#representativeAddress__detailCountry`);

  }

  async assertAmendContactDetailsPage() {
    await this.page.waitForLoadState('load');
    await expect(this.amendContactDetailsTitle).toBeVisible();
  }

  async selectAmendContactDetailsOption(option: string) {
    await this.page.waitForLoadState('load');
    await expect(this.representativeContactChangeOptionGroup).toBeVisible();
    await this.representativeContactChangeOptionGroup.getByLabel(option).check();
  }

  async editContactDetails(phNumber: string) {
    await this.page.waitForLoadState('load');
    await this.repPhoneNumberInput.clear();
    await this.repPhoneNumberInput.fill(phNumber);
  }

  async enterUKAddress(option: string): Promise<AddressDetails> {
    await this.page.waitForLoadState('load');
    await this.commonActionsHelper.enterUkAddressWithPostcode(this.repAddressPostCodeInput, this.repAddressListDropdown);
    return  {
        addressLine1: (await this.repAddressLine1.inputValue()).toString(),
        addressLine2: (await this.repAddressLine2.inputValue()).toString(),
        addressLine3: (await this.repAddressLine3.inputValue()).toString(),
        townOrCity: (await this.repTownOrCityInput.inputValue()).toString(),
        county: (await this.repCountyInput.inputValue()).toString(),
        postcode: (await this.repPostCodeInput.inputValue()).toString(),
        country: (await this.repCountryInput.inputValue()).toString(),
      };
  }

  async getChosenMyHmctsAddress() {
    await this.page.waitForLoadState('load');
    const addressEle = this.page.locator(`//dt[normalize-space()='Chosen address']/following-sibling::dd`);
    const addressText = await addressEle.textContent();
    return addressText ? addressText : '';
  }

  async amendLegalRepContactDetails(
    changeOption:string,
    cyaPage: CheckYourAnswersPage,
    phNumber: string = '07234567890')
    : Promise<{addressDetails: AddressDetails, phNumber: string}>
  {
    let addressDetails;
    await this.assertAmendContactDetailsPage();
    await this.selectAmendContactDetailsOption(changeOption);
    if (changeOption === 'Amend contact details') {
      await this.clickContinue();
      await this.editContactDetails(phNumber);
      addressDetails = await this.enterUKAddress(changeOption);
      await this.clickContinue('/submit');

      await cyaPage.assertCheckYourAnswersPage({
        tableName: 'Check your answers',
        rows :[
          { cellItem: 'What is you contact phone number?', value: phNumber },
          { cellItem: 'Building and Street', value: addressDetails.addressLine1 },
          { cellItem: 'Town or City', value: addressDetails.townOrCity },
          { cellItem: 'Postcode/Zipcode', value: addressDetails.postcode },
        ]
      });
    } else {
      await this.clickContinue();
      const addStr = await this.getChosenMyHmctsAddress();
      await this.clickContinue('/submit');

      await cyaPage.assertCheckYourAnswersPage({
        tableName: 'Check your answers',
        rows :[
          { cellItem: 'Chosen address', value: addStr },
        ]
      });
      const brokenAddress = addStr.split('\n');
      addressDetails = {
        addressLine1: brokenAddress[0] ,
        townOrCity: brokenAddress[1],
        postcode: brokenAddress[2],
      }
    }
    console.log(addressDetails);
    await this.clickSubmitButton();
    return {addressDetails, phNumber};
  }

}

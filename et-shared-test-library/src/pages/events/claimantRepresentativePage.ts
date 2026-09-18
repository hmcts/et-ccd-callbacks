import { expect, Locator, Page } from '@playwright/test';
import { BaseEventPage } from './BaseEventPage.ts';
import { users } from '../../config/config.dynamic.ts';

export default class ClaimantRepresentativePage extends BaseEventPage {

  private readonly isClaimantRepresented: Locator;
  private readonly nameOfRepresentative: Locator;
  private readonly repOccupation: Locator;
  private readonly ukAddressPostCodeLookup: Locator;
  private readonly ukAddressList: Locator;
  private readonly searchOrganisationField: Locator;
  private readonly repEmailAddress: Locator;

  constructor(page: Page) {
    super(page);
    this.isClaimantRepresented = this.page.locator('#claimantRepresentedQuestion');
    this.nameOfRepresentative = this.page.locator('#representativeClaimantType_name_of_representative');
    this.repOccupation = this.page.locator('#representativeClaimantType_representative_occupation');
    this.ukAddressPostCodeLookup = this.page.locator('#representativeClaimantType_representative_address_representative_address_postcodeInput');
    this.ukAddressList = this.page.locator('#representativeClaimantType_representative_address_representative_address_addressList');
    this.searchOrganisationField = this.page.locator('#search-org-text');
    this.repEmailAddress = this.page.locator(`#representativeClaimantType_representative_email_address`);
  }

  async assertClaimantRepresentativePageIsDisplayed() {
    await expect(this.page.getByRole('heading',{name:'Claimant Representative', exact: true})).toBeVisible();
  }

  async selectIsClaimantRepresented(option: string) {
    await expect(this.isClaimantRepresented).toBeVisible();
    const optionLocator = this.isClaimantRepresented.getByRole('radio', { name: option });
    await expect(optionLocator).toBeVisible();
    await optionLocator.check();
  }

  async enterRepresentativeName(name: string) {
    await expect(this.nameOfRepresentative).toBeVisible();
    await this.nameOfRepresentative.fill(name);
  }

  async selectRepresentativeOccupation(occupation: string) {
    await expect(this.repOccupation).toBeVisible();
    await this.page.selectOption('#representativeClaimantType_representative_occupation', occupation);
  }

  async selectRepresentativeAddress() {
      await this.commonActionsHelper.enterUkAddressWithPostcode(this.ukAddressPostCodeLookup, this.ukAddressList, 'SN1 4ET');
  }

  async selectRepresentativeOrganisation(orgName: string) {
    await expect(this.searchOrganisationField).toBeVisible();
    await this.commonActionsHelper.selectOrganisation(this.page,  orgName);
  }

  async enterEmailAddress(email: string = users.etLegalRepresentative.email) {
    await expect(this.repEmailAddress).toBeVisible();
    await this.repEmailAddress.fill(email);
  }

  async addClaimantRepresentative() {
    await this.assertClaimantRepresentativePageIsDisplayed();
    await this.selectIsClaimantRepresented('Yes');
    await this.enterRepresentativeName('Test Claimant Representative');
    await this.selectRepresentativeOccupation('1: Solicitor');
    await this.selectRepresentativeAddress();
    await this.selectRepresentativeOrganisation('ET Test Factory Solicitor');
    await this.enterEmailAddress();
    await this.clickSubmitButton();
  }
}

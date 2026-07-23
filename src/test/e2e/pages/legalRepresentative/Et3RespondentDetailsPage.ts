import { expect, Locator, Page } from '@playwright/test';
import { CaseDetailsValues } from '../../config/case-data.ts';
import { CommonActionsHelper } from '../helpers/CommonActionsHelper.ts';
import { TableRowItem } from '../../types/table.ts';
import { CheckYourAnswersPage } from '../helpers/CheckYourAnswersPage.ts';
import { Et3DetailsPage } from './Et3DetailsPage.ts';

export default class Et3RespondentDetailsPage extends Et3DetailsPage {

  private readonly respondentDetailsPageTitle: Locator;
  private readonly isThisCorrectRespondentTitle: Locator;
  private readonly isThisCorrectRespondentRadioGroup: Locator;
  private readonly respondentNameTitle: Locator;
  private readonly respondentNameInput: Locator;
  private readonly respondentAddressTitle: Locator;
  private readonly respondentAddressInput: Locator;
  private readonly respondentAddressList: Locator;
  private readonly hearingFormatTitle: Locator;
  private readonly repVideoHearingOption: Locator;
  private readonly respondentVideoHearingOption: Locator;
  private readonly mentalHealthTitle: Locator;
  private readonly mentalHealthRadioGroup: Locator;

  constructor(page: Page) {
    super(page);
    this.respondentDetailsPageTitle = this.page.getByRole('heading', {
      name: 'ET3 - Response to Employment tribunal claim (ET1)',
    });

    this.isThisCorrectRespondentTitle = this.page.getByRole('heading', {
      name: "Is this the correct claimant for the claim you're responding to?",
    });
    this.isThisCorrectRespondentRadioGroup = this.page.locator(`#et3ResponseIsClaimantNameCorrect_radio`);
    this.respondentNameTitle = this.page.getByRole('heading', { name: "What is the respondent's name?" });
    this.respondentNameInput = this.page.locator(`#et3ResponseRespondentLegalName`);
    this.respondentAddressTitle = this.page.getByRole('heading', { name: 'Respondent address' });
    this.respondentAddressInput = this.page.locator(`#et3RespondentAddress_et3RespondentAddress_postcodeInput`);
    this.respondentAddressList = this.page.locator(`#et3RespondentAddress_et3RespondentAddress_addressList`);
    this.hearingFormatTitle = this.page.getByRole('heading', { name: 'Hearing format' });
    this.repVideoHearingOption = this.page.locator(`#et3ResponseHearingRepresentative-Video\\ hearings`);
    this.respondentVideoHearingOption = this.page.locator(`#et3ResponseHearingRespondent-Video\\ hearings`);
    this.mentalHealthTitle = this.page.getByRole('heading', {
      name: 'In the respondent party - are you aware of any physical, mental or learning disability or health conditions which requires support?',
    });
    this.mentalHealthRadioGroup = this.page.locator(`#et3ResponseRespondentSupportNeeded`);
  }

  async assertEt3RespondentDetailsPageIsDisplayed() {
    await this.page.waitForLoadState('load');
    await expect(this.respondentDetailsPageTitle).toBeVisible();
  }

  async selectIsThisTheCorrectRespondent(option: string) {
    await expect(this.isThisCorrectRespondentTitle).toBeVisible();
    const optionLocator = this.isThisCorrectRespondentRadioGroup.getByRole('radio', { name: option });
    await expect(optionLocator).toBeVisible();
    await optionLocator.check();
  }

  async enterRespondentLegalName(respondentLegalName: string = CaseDetailsValues.respondentLegalName) {
    await this.page.waitForLoadState('load');
    await expect(this.respondentNameTitle).toBeVisible();
    await expect(this.respondentNameInput).toBeVisible();
    await this.respondentNameInput.fill(respondentLegalName);
  }

  async enterRespondentAddressDetails() {
    await this.page.waitForLoadState('load');
    await expect(this.respondentAddressTitle).toBeVisible();
    await expect(this.respondentAddressInput).toBeVisible();
    await this.commonActionsHelper.enterUkAddressWithPostcode(this.respondentAddressInput, this.respondentAddressList);
  }

  async selectHearingFormatForRepresentativeAndRespondent() {
    await this.page.waitForLoadState('load');
    await expect(this.hearingFormatTitle).toBeVisible();
    await this.repVideoHearingOption.check();
    await this.respondentVideoHearingOption.check();
  }

  async selectMentalHealthSupportNeeded(option: string) {
    await this.page.waitForLoadState('load');
    await expect(this.mentalHealthTitle).toBeVisible();
    const optionLocator = this.mentalHealthRadioGroup.getByRole('radio', { name: option, exact: true });
    await expect(optionLocator).toBeVisible();
    await optionLocator.check();
  }

  async enterEt3RespondentDetails(cyaPage: CheckYourAnswersPage) {
    await this.assertEt3RespondentDetailsPageIsDisplayed();
    await this.clickContinue();

    await this.selectRespondentName();
    await this.clickContinue();

    await this.selectIsThisTheCorrectRespondent('Yes');
    await this.clickContinue();

    await this.enterRespondentLegalName();
    await this.clickContinue();

    await this.enterRespondentAddressDetails();
    await this.clickContinue();

    await this.page.waitForLoadState('load');
    await expect(this.page.getByRole('heading', { name: 'Your information (as the representative)' })).toBeVisible();
    await this.clickContinue();

    await this.selectHearingFormatForRepresentativeAndRespondent();
    await this.clickContinue();

    await this.selectMentalHealthSupportNeeded('No');
    await this.clickContinue();

    const rows: TableRowItem[] = [
      {
        cellItem: `Select which respondent this ET3 is for`,
        value: CaseDetailsValues.respondentName,
      },
      {
        cellItem: `Is this the correct claimant for the claim you're responding to?`,
        value: `Yes`,
      },
      {
        cellItem: `Enter the respondent's registered or legal name`,
        value: CaseDetailsValues.respondentLegalName,
      },
      {
        cellItem: `Which types of hearing can you, as the representative, attend?`,
        value: `Video hearings`,
      },
      {
        cellItem: `Which types of hearing can the respondent attend?`,
        value: `Video hearings`,
      },
      {
        cellItem: `In the respondent party - are you aware of any physical, mental or learning disability or health conditions which requires support?`,
        value: `No`,
      },
    ];

    await cyaPage.assertCheckYourAnswersPage({
      tableName: 'Check your answers',
      rows: rows,
    });

    await this.clickET3SaveAsDraftButton();
  }
}

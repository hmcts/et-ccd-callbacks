import { Et3DetailsPage } from './Et3DetailsPage.ts';
import { expect, Locator, Page } from '@playwright/test';
import { CheckYourAnswersPage } from '../helpers/CheckYourAnswersPage.ts';
import { AxeUtils } from '@hmcts/playwright-common';

export class Et3EmploymentDetailsPage extends Et3DetailsPage {
  private readonly et3EmploymentDetailsPageTitle: Locator;
  private readonly datesOfEmploymentTitle: Locator;
  private readonly datesOfEmploymentFromRadioGroup: Locator;
  private readonly claimantEmploymentContinuingTitle: Locator;
  private readonly claimantEmploymentContinuingFromRadioGroup: Locator;
  private readonly claimantJobDescriptionCorrectTitle: Locator;
  private readonly claimantJobDescriptionCorrectFromRadioGroup: Locator;
  private readonly claimantTotalWeeklyHoursTitle: Locator;
  private readonly claimantTotalWeeklyHoursRadioGroup: Locator;
  private readonly claimantEarningDetailsTitle: Locator;
  private readonly claimantEarningDetailsCorrectRadioGroup: Locator;
  private readonly claimantNoticeCorrectTitle: Locator;
  private readonly claimantNoticeCorrectRadioGroup: Locator;
  private readonly claimantPensionDetailsCorrectTitle: Locator;
  private readonly claimantPensionDetailsCorrectRadioGroup: Locator;

  constructor(page: Page) {
    super(page);
    this.et3EmploymentDetailsPageTitle = this.page.getByRole('heading', {
      name: 'ET3 - Response to Employment tribunal claim (ET1)',
    });
    this.datesOfEmploymentTitle = this.page.getByRole('heading', {
      name: 'Are the dates of employment given by the claimant correct?',
    });
    this.datesOfEmploymentFromRadioGroup = this.page.locator(`#et3ResponseAreDatesCorrect`);
    this.claimantEmploymentContinuingTitle = this.page.getByRole('heading', {
      name: "Is the claimant's employment with the respondent continuing?",
    });
    this.claimantEmploymentContinuingFromRadioGroup = this.page.locator(`#et3ResponseContinuingEmployment`);
    this.claimantJobDescriptionCorrectTitle = this.page.getByRole('heading', {
      name: "Is the claimant's description of their job or job title correct?",
    });
    this.claimantJobDescriptionCorrectFromRadioGroup = this.page.locator(`#et3ResponseIsJobTitleCorrect`);
    this.claimantTotalWeeklyHoursTitle = this.page.getByRole('heading', {
      name: "Are the claimant's total weekly work hours correct?",
    });
    this.claimantTotalWeeklyHoursRadioGroup = this.page.locator(`#et3ResponseClaimantWeeklyHours`);
    this.claimantEarningDetailsTitle = this.page.getByRole('heading', {
      name: 'Are the earnings details given by the claimant correct?',
    });
    this.claimantEarningDetailsCorrectRadioGroup = this.page.locator(`#et3ResponseEarningDetailsCorrect`);
    this.claimantNoticeCorrectTitle = this.page.getByRole('heading', {
      name: 'Is the information given by the claimant correct about their notice?',
    });
    this.claimantNoticeCorrectRadioGroup = this.page.locator(`#et3ResponseIsNoticeCorrect`);
    this.claimantPensionDetailsCorrectTitle = this.page.getByRole('heading', {
      name: 'Are the details about pension and other benefits correct?',
    });
    this.claimantPensionDetailsCorrectRadioGroup = this.page.locator(`#et3ResponseIsPensionCorrect`);
  }

  async assertEt3EmploymentDetailsPageIsDisplayed() {
    await this.page.waitForLoadState('load');
    await expect(this.et3EmploymentDetailsPageTitle).toBeVisible();
  }

  async selectAreTheDatesOfEmploymentCorrect(option: string) {
    await this.page.waitForLoadState('load');
    await expect(this.datesOfEmploymentTitle).toBeVisible();
    await expect(this.datesOfEmploymentFromRadioGroup).toBeVisible();
    const optionLocator = this.datesOfEmploymentFromRadioGroup.getByRole('radio', { name: option });
    await expect(optionLocator).toBeVisible();
    await optionLocator.check();
  }

  async selectIsTheClaimantsEmploymentContinuing(option: string) {
    await this.page.waitForLoadState('load');
    await expect(this.claimantEmploymentContinuingTitle).toBeVisible();
    await expect(this.claimantEmploymentContinuingFromRadioGroup).toBeVisible();
    const optionLocator = this.claimantEmploymentContinuingFromRadioGroup.getByRole('radio', { name: option });
    await expect(optionLocator).toBeVisible();
    await optionLocator.check();
  }

  async selectIsTheClaimantsJobDescriptionCorrect(option: string) {
    await this.page.waitForLoadState('load');
    await expect(this.claimantJobDescriptionCorrectTitle).toBeVisible();
    await expect(this.claimantJobDescriptionCorrectFromRadioGroup).toBeVisible();
    const optionLocator = this.claimantJobDescriptionCorrectFromRadioGroup.getByRole('radio', { name: option });
    await expect(optionLocator).toBeVisible();
    await optionLocator.check();
  }

  async selectAreTheClaimantsTotalWeeklyHoursCorrect(option: string) {
    await this.page.waitForLoadState('load');
    await expect(this.claimantTotalWeeklyHoursTitle).toBeVisible();
    await expect(this.claimantTotalWeeklyHoursRadioGroup).toBeVisible();
    const optionLocator = this.claimantTotalWeeklyHoursRadioGroup.getByRole('radio', { name: option });
    await expect(optionLocator).toBeVisible();
    await optionLocator.check();
  }

  async selectIsTheClaimantEarningDetailsCorrect(option: string) {
    await this.page.waitForLoadState('load');
    await expect(this.claimantEarningDetailsTitle).toBeVisible();
    await expect(this.claimantEarningDetailsCorrectRadioGroup).toBeVisible();
    const optionLocator = this.claimantEarningDetailsCorrectRadioGroup.getByRole('radio', { name: option });
    await expect(optionLocator).toBeVisible();
    await optionLocator.check();
  }

  async selectIsClaimantNoticeCorrect(option: string) {
    await this.page.waitForLoadState('load');
    await expect(this.claimantNoticeCorrectTitle).toBeVisible();
    await expect(this.claimantNoticeCorrectRadioGroup).toBeVisible();
    const optionLocator = this.claimantNoticeCorrectRadioGroup.getByRole('radio', { name: option });
    await expect(optionLocator).toBeVisible();
    await optionLocator.check();
  }

  async selectIsClaimantPensionDetailsCorrect(option: string) {
    await this.page.waitForLoadState('load');
    await expect(this.claimantPensionDetailsCorrectTitle).toBeVisible();
    await expect(this.claimantPensionDetailsCorrectRadioGroup).toBeVisible();
    const optionLocator = this.claimantPensionDetailsCorrectRadioGroup.getByRole('radio', { name: option });
    await expect(optionLocator).toBeVisible();
    await optionLocator.check();
  }

  async enterEt3EmploymentDetails(cyaPage: CheckYourAnswersPage, axeUtils?: AxeUtils) {
    await this.assertEt3EmploymentDetailsPageIsDisplayed();
    await this.clickContinue();

    await this.selectRespondentName();
    if(axeUtils) {
      await axeUtils.audit();
    }
    await this.clickContinue();

    await this.page.waitForLoadState('load');
    await expect(this.page.getByRole('heading', { name: "Respondent's workforce" })).toBeVisible();
    if (axeUtils) {
      await axeUtils.audit();
    }
    await this.clickContinue();

    await this.selectAreTheDatesOfEmploymentCorrect('Yes');
    if (axeUtils) {
      await axeUtils.audit();
    }
    await this.clickContinue();

    await this.selectIsTheClaimantsEmploymentContinuing('Yes');
    await this.clickContinue();

    await this.selectIsTheClaimantsJobDescriptionCorrect('Yes');
    await this.clickContinue();

    await this.selectAreTheClaimantsTotalWeeklyHoursCorrect('Yes');
    await this.clickContinue();

    await this.selectIsTheClaimantEarningDetailsCorrect('Yes');
    await this.clickContinue();

    await this.selectIsClaimantNoticeCorrect('Yes');
    await this.clickContinue();

    await this.selectIsClaimantPensionDetailsCorrect('Yes');
    await this.clickContinue();

    await cyaPage.assertCheckYourAnswersPage({
      tableName: 'Check your answers',
      rows: [
        'Mrs Test Auto',
        {
          cellItem: 'Are the dates of employment given by the claimant correct?',
          value: 'Yes',
        },
        {
          cellItem: "Is the claimant's employment with the respondent continuing?",
          value: 'Yes',
        },
        {
          cellItem: "Is the claimant's description of their job or job title correct?",
          value: 'Yes',
        },
        {
          cellItem: "Are the claimant's total weekly work hours correct?",
          value: 'Yes',
        },
        {
          cellItem: 'Are the earnings details given by the claimant correct?',
          value: 'Yes',
        },
        {
          cellItem: 'Is the information given by the claimant correct about their notice?',
          value: 'Yes',
        },
        {
          cellItem: 'Are the details about pension and other benefits correct?',
          value: 'Yes',
        },
      ],
    });

    await this.clickET3SaveAsDraftButton();
  }
}

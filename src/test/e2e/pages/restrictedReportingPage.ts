import { expect, Locator, Page } from "@playwright/test";
import { BasePage } from "./basePage";
import dateUtilComponent from "../data-utils/DateUtilComponent";

const today = new Date();
const restrictedMonth = today.getMonth() + 1;
const restrictedYear = today.getFullYear();

export class RestrictedReportingPage extends BasePage {
  private readonly headingEle: Locator;
  private readonly requestedByOption: Locator;
  private readonly restrictedReportingImposedNo: Locator;
  private readonly restrictedReportingRule503bYes: Locator;
  private readonly dateCeased: Locator;
  private readonly monthCeased: Locator;
  private readonly yearCeased: Locator;
  private readonly startDate: Locator;
  private readonly monthStartDate: Locator;
  private readonly yearStartDate: Locator;
  private readonly restrictedReportingExcludedRegister: Locator;
  private readonly restrictedReportingExcludedNames: Locator;
  private readonly restrictedReportingDeletedPhyRegisterNo: Locator;

  constructor(page: Page) {
    super(page);
    this.headingEle = page.locator('.heading-h2');
    this.requestedByOption = page.locator('#restrictedReporting_dynamicRequestedBy');
    this.restrictedReportingImposedNo = page.locator('#restrictedReporting_imposed_No');
    this.restrictedReportingRule503bYes = page.locator('#restrictedReporting_rule503b_Yes');
    this.dateCeased = page.locator('#dateCeased-day');
    this.monthCeased = page.locator('#dateCeased-month');
    this.yearCeased = page.locator('#dateCeased-year');
    this.startDate = page.locator('#startDate-day');
    this.monthStartDate = page.locator('#startDate-month');
    this.yearStartDate = page.locator('#startDate-year');
    this.restrictedReportingExcludedRegister = page.locator('#restrictedReporting_excludedRegister');
    this.restrictedReportingExcludedNames = page.locator('#restrictedReporting_excludedNames');
    this.restrictedReportingDeletedPhyRegisterNo = page.locator('#restrictedReporting_deletedPhyRegister_No');
  }

  public async selectRule49BOption() {
    //To always choose weekdays for hearing dates
    const today = new Date();
    const resultDate = dateUtilComponent.addWeekdays(today, 21);

    await expect(this.headingEle).toContainText('Restricted Case');
    await this.requestedByOption.selectOption({ label: 'Grayson Becker' });
    await this.restrictedReportingImposedNo.check();
    await this.restrictedReportingRule503bYes.check();

    console.log(`... setting up restricted dates ${resultDate.getDate()}`);
    await this.dateCeased.fill(`${resultDate.getDate()}`);
    await this.monthCeased.fill(restrictedMonth.toString());
    await this.yearCeased.fill(restrictedYear.toString());
    await this.startDate.fill(`${resultDate.getDate()}`);
    await this.monthStartDate.fill(restrictedMonth.toString());
    await this.yearStartDate.fill(restrictedYear.toString());
    await this.restrictedReportingExcludedRegister.selectOption({ label: 'No' });

    await this.delay(2000);
    await this.restrictedReportingExcludedNames.fill('Test Name');
    await this.restrictedReportingDeletedPhyRegisterNo.click();
    await this.clickSubmitButton();
  }

  async verifyRule49BFlag() {
    await expect(this.page.getByText('RULE 49(3)b').first()).toBeVisible();
  }
}

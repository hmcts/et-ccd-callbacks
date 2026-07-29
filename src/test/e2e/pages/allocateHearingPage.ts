import { BasePage } from './basePage.ts';
import { expect, Locator, Page } from '@playwright/test';
import dateUtilComponent from "../data-utils/DateUtilComponent";

export default class AllocateHearingPage extends BasePage {
  private readonly hearingDropDown: Locator;
  private readonly hearingJudge: Locator;
  private readonly hearingEmployerMember: Locator;
  private readonly hearingEmployeeMember: Locator;
  private readonly hearingTypeSingle: Locator;
  private readonly listingVenue: Locator;
  private readonly hearingDocType: Locator;
  private readonly listingDateDay: Locator;
  private readonly listingDateMonth: Locator;
  private readonly listingDateYear: Locator;
  private readonly employeeMemberCell: Locator;
  private readonly employerMemberCell: Locator;

  constructor(page: Page) {
    super(page);
    this.hearingDropDown = page.locator('#allocateHearingHearing');
    this.hearingJudge = page.locator('#allocateHearingJudge');
    this.hearingEmployerMember = page.locator('#allocateHearingEmployerMember');
    this.hearingEmployeeMember = page.locator('#allocateHearingEmployeeMember');
    this.hearingTypeSingle = page.locator('#printHearingDetails_hearingDateType-Single');
    this.listingVenue = page.locator('#printHearingDetails_listingVenue');
    this.hearingDocType = page.locator('#printHearingDetails_hearingDocType');
    this.listingDateDay = page.locator('#listingDate-day');
    this.listingDateMonth = page.locator('#listingDate-month');
    this.listingDateYear = page.locator('#listingDate-year');
    this.employeeMemberCell = page.locator("//th/span[text()='Employee Member']/../../td");
    this.employerMemberCell = page.locator("//th/span[text()='Employer Member']/../../td");
  }

  async allocateHearing() {
    await this.hearingDropDown.selectOption({ index: 1 });
    await this.clickContinue();
    await this.hearingJudge.selectOption('T Haddlesey');
    await this.hearingEmployerMember.selectOption('ER Member');
    await this.hearingEmployeeMember.selectOption('EE Member');
    await this.clickContinue();
    await this.clickSubmitButton();
    await this.delay(2000);
  }

  async validateHearingList() {
    await this.listingVenue.selectOption({ label: 'Newcastle CFCTC' });
    await this.hearingDocType.selectOption({ label: 'IT56 - List of Exhibits' });
    let [year, month, day] = dateUtilComponent.addWeekdays(new Date(), 21).toISOString().split('T')[0].split('-');
    await this.hearingTypeSingle.check();
    await this.listingDateDay.fill(day);
    await this.listingDateMonth.waitFor({ state: 'visible' });
    await this.listingDateMonth.fill(month);
    await this.listingDateYear.waitFor({ state: 'visible' });
    await this.listingDateYear.fill(year);
    await this.page.waitForLoadState('load');
    await this.clickContinue();
    await this.page.waitForLoadState('load');
    await this.delay(5000);
    await expect(this.employeeMemberCell).toBeEmpty();
    await expect(this.employerMemberCell).toBeEmpty();
  }
}

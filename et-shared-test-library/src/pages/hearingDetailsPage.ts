import { BasePage } from "./basePage";
import { expect, Locator, Page } from "@playwright/test";

export default class HearingDetailsPage extends BasePage {
  private readonly hearingDropdownOption: Locator;
  private readonly hearingDetailsStatus: Locator;
  private readonly hearingDetailsCaseDisposedNo: Locator;
  private readonly timingStartDay: Locator;
  private readonly timingStartMonth: Locator;
  private readonly timingStartYear: Locator;
  private readonly timingStartHour: Locator;
  private readonly timingStartMinute: Locator;
  private readonly timingStartSecond: Locator;
  private readonly timingFinishDay: Locator;
  private readonly timingFinishMonth: Locator;
  private readonly timingFinishYear: Locator;
  private readonly timingFinishHour: Locator;
  private readonly timingFinishMinute: Locator;
  private readonly timingFinishSecond: Locator;
  private readonly cutAlert: Locator;

  constructor(page: Page) {
    super(page);
    this.hearingDropdownOption = page.locator('#hearingDetailsHearing');
    this.hearingDetailsStatus = page.locator('#hearingDetailsCollection_0_hearingDetailsStatus');
    this.hearingDetailsCaseDisposedNo = page.locator('#hearingDetailsCollection_0_hearingDetailsCaseDisposed_No');
    this.timingStartDay = page.locator('#hearingDetailsTimingStart-day');
    this.timingStartMonth = page.locator('#hearingDetailsTimingStart-month');
    this.timingStartYear = page.locator('#hearingDetailsTimingStart-year');
    this.timingStartHour = page.locator('#hearingDetailsTimingStart-hour');
    this.timingStartMinute = page.locator('#hearingDetailsTimingStart-minute');
    this.timingStartSecond = page.locator('#hearingDetailsTimingStart-second');
    this.timingFinishDay = page.locator('#hearingDetailsTimingFinish-day');
    this.timingFinishMonth = page.locator('#hearingDetailsTimingFinish-month');
    this.timingFinishYear = page.locator('#hearingDetailsTimingFinish-year');
    this.timingFinishHour = page.locator('#hearingDetailsTimingFinish-hour');
    this.timingFinishMinute = page.locator('#hearingDetailsTimingFinish-minute');
    this.timingFinishSecond = page.locator('#hearingDetailsTimingFinish-second');
    this.cutAlert = page.locator('cut-alert');
  }

  async updateHearing() {
    await this.hearingDropdownOption.selectOption({ index: 1 });
    await this.clickContinue();

    // remove all dates so can list hearing as heard
    await this.hearingDetailsStatus.selectOption({ label: 'Heard' });
    await this.hearingDetailsCaseDisposedNo.check();
    await this.timingStartDay.fill('');
    await this.timingStartMonth.fill('');
    await this.timingStartYear.fill('');
    await this.timingStartHour.fill('');
    await this.timingStartMinute.fill('');
    await this.timingStartSecond.fill('');

    await this.timingFinishDay.fill('');
    await this.timingFinishMonth.fill('');
    await this.timingFinishYear.fill('');
    await this.timingFinishHour.fill('');
    await this.timingFinishMinute.fill('');
    await this.timingFinishSecond.fill('');
    await this.clickSubmitButton();

    await expect(this.cutAlert).toContainText('updated with event: Hearing Details');
  }
}

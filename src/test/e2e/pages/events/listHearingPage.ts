import { BasePage } from '../basePage';
import dateUtilComponent from '../../data-utils/DateUtilComponent';
import { expect } from '@playwright/test';
import { BaseEventPage } from './BaseEventPage.ts';

export class ListHearingPage extends BaseEventPage {

    async enterHearingNumber(hearingNumber: number) {
        const hearingNumberText = this.page.locator(`#hearingCollection_${hearingNumber}_hearingNumber`);
        await expect(hearingNumberText).toBeVisible();
        await hearingNumberText.fill((hearingNumber+1).toString());
    }

    async selectHearingType(hearingNumber: number, hearingType: string) {
        const hearingTypeDropdown = this.page.locator(`#hearingCollection_${hearingNumber}_Hearing_type`);
        await expect(hearingTypeDropdown).toBeVisible();
        await hearingTypeDropdown.selectOption({ label: hearingType });
    }

    async checkHearingFormat(hearingNumber: number, format: string) {
        const hearingFormatOption = this.page.locator(`#hearingCollection_${hearingNumber}_hearingFormat-${format}`);
        await expect(hearingFormatOption).toBeVisible();
        await hearingFormatOption.check();
    }

    async selectJudicialMediationRadio(hearingNumber: number, option: string) {
        const judicialMediationOption = this.page.locator(`#hearingCollection_${hearingNumber}_judicialMediation_${option}`);
        await expect(judicialMediationOption).toBeVisible();
        await judicialMediationOption.check();
    }

    async selectManagingOfficeAndVenueScotland(hearingNumber: number, office: string = 'Glasgow') {
        const managingOfficeDropdown = this.page.locator(`#hearingCollection_${hearingNumber}_Hearing_venue_Scotland`);
        await expect(managingOfficeDropdown).toBeVisible();
        const loc = await managingOfficeDropdown.selectOption({ index: 1 });
        const hearingVenueDropdown = this.page.locator(`#hearingCollection_${hearingNumber}_Hearing_${loc[0].substring(3)}`);
        await expect(hearingVenueDropdown).toBeVisible();
        await hearingVenueDropdown.selectOption({ index: 1 });
    }

    async selectHearingVenue(hearingNumber: number, venue: string = 'Leeds ET') {
        const hearingVenueDropdown = this.page.locator(`#hearingCollection_${hearingNumber}_Hearing_venue`);
        await expect(hearingVenueDropdown).toBeVisible();
        await hearingVenueDropdown.selectOption({label: venue});
    }

    async enterEstimatedHearingLengthAndType(hearingNumber: number, lengthNum: string, lengthType: string) {
        const hearingLengthNumField = this.page.locator(`#hearingCollection_${hearingNumber}_hearingEstLengthNum`);
        await expect(hearingLengthNumField).toBeVisible();
        await hearingLengthNumField.fill(lengthNum);
        const dayHourMinutesDropdown = this.page.locator(`#hearingCollection_${hearingNumber}_hearingEstLengthNumType`);
        await expect(dayHourMinutesDropdown).toBeVisible();
        await dayHourMinutesDropdown.selectOption({ label: lengthType });
    }

    async selectPanelType(hearingNumber: number, panelType: string) {
        const panelTypeOption = this.page.locator(`#hearingCollection_${hearingNumber}_hearingSitAlone-${panelType.replace(/ /g, '\\ ')}`);
        await expect(panelTypeOption).toBeVisible();
        await panelTypeOption.check();
    }

    async selectEQPStageHearing(hearingNumber: number, stage: string) {
        const hearingStageDropdown = this.page.locator(`#hearingCollection_${hearingNumber}_Hearing_stage`);
        await expect(hearingStageDropdown).toBeVisible();
        await hearingStageDropdown.selectOption({ label: stage });
    }

    async enterHearingNotes(hearingNumber: number, notes: string) {
        const hearingNotesField = this.page.locator(`#hearingCollection_${hearingNumber}_Hearing_notes`);
        await expect(hearingNotesField).toBeVisible();
        await hearingNotesField.fill(notes);
    }

    async clickAddNewDateButton(hearingNumber: number) {
        const addNewDateButton = this.page.locator(`#hearingCollection_${hearingNumber}_hearingDateCollection > div > button`);
        await expect(addNewDateButton).toBeVisible();
        await addNewDateButton.click();
    }

    async setHearingDate(hearingNumber: number, day: string, month: string, year: string) {
        const hearingListDayField = this.page.locator(`(//*[@id="listedDate-day"])[${hearingNumber + 1}]`);
        await expect(hearingListDayField).toBeVisible();
        await hearingListDayField.fill(day);
        const hearingListMonthField = this.page.locator(`(//*[@id="listedDate-month"])[${hearingNumber + 1}]`);
        await expect(hearingListMonthField).toBeVisible();
        await hearingListMonthField.fill(month);
        const hearingListYearField = this.page.locator(`(//*[@id="listedDate-year"])[${hearingNumber + 1}]`);
        await expect(hearingListYearField).toBeVisible();
        await hearingListYearField.fill(year);
    }

    async listCase(location: string, hearingNumber: number = 0, venue?: string, hearingType: string = 'Costs Hearing',panelType:string ="Sit Alone", office?: string) {
      let [year, month, day] = dateUtilComponent.addWeekdays(new Date(), 21).toISOString().split('T')[0].split('-');
      if (hearingNumber > 0) {
        await this.addNewHearingButtonClick();
      }
      await this.enterHearingNumber(hearingNumber);

      await this.checkHearingFormat(hearingNumber, 'Hybrid');
      await this.selectJudicialMediationRadio(hearingNumber, 'No');
      await this.selectHearingType(hearingNumber, hearingType);

      if (location === 'Scotland') {
        await this.selectManagingOfficeAndVenueScotland(hearingNumber, office);
      } else {
        await this.selectHearingVenue(hearingNumber, venue);
      }

      await this.enterEstimatedHearingLengthAndType(hearingNumber, '1', 'Hours');
      await this.selectPanelType(hearingNumber, panelType);
      await this.selectEQPStageHearing(hearingNumber, 'Stage 1');
      await this.clickAddNewDateButton(hearingNumber);
      if (hearingNumber == 1) {
        await this.setHearingDate(hearingNumber, '6', '1', '2025');
      } else {
        await this.setHearingDate(hearingNumber, day, month, year);
      }
      await this.enterHearingNotes(hearingNumber, 'The hearing should be help as soon as possible....');

      await this.page.evaluate(() => window.scrollTo(0, document.body.scrollHeight));
      await this.clickSubmitButton();
      if (hearingNumber == 1) {
        await expect(
          this.page.getByRole('heading', { level: 3, name: 'One of the listed dates are in the past.' }),
        ).toBeVisible();
        await this.clickSubmitButton();
      }
    }

    async updatePanelType(panelType: string) {
      await this.selectPanelType(0, panelType);
      await this.clickSubmitButton();
  }



}

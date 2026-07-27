import { expect, Page } from '@playwright/test';
import { BasePage } from "../basePage.ts";

export default class IssueJudgementPage extends BasePage {

  constructor(page: Page) {
    super(page);
  }

    async assertJudgementPageIsDisplayed() {
        await expect(this.page.getByRole('heading',{name:'Judgment', exact: true})).toBeVisible();
    }

    async selectNonHearingJudgement(option: string, position: number = 0) {
        const nonHearingJudgementLocator = this.page.locator(`#judgementCollection_${position}_non_hearing_judgment`).getByText(option);
        await expect(nonHearingJudgementLocator).toBeVisible();
        await nonHearingJudgementLocator.check();
    }

    async selectJudgmentType(option: string, position: number = 0) {
        const judgementTypeLocator = this.page.locator(`#judgementCollection_${position}_judgement_type`);
        await expect(judgementTypeLocator).toBeVisible();
        await this.page.selectOption(`#judgementCollection_${position}_judgement_type`, option);
    }

    async addJurisdiction(juristictionCode: string[], position: number = 0) {
        const jurisdictionGroup = this.page.locator(`#judgementCollection_${position}_jurisdictionCodes`);
        for (let i = 0; i < juristictionCode.length; i++) {
          await jurisdictionGroup.getByRole('button', { name: 'Add new' }).nth(0).click();
          const jurisdictionList = `#judgementCollection_${position}_jurisdictionCodes_${i}_juridictionCodesList`;
          await this.page.selectOption(jurisdictionList, juristictionCode[i]);
        }
    }

    async enterJudgmentMadeDate(day: string, month: string, year: string, position: number = 0) {
      const judgementMadeGroup = this.page.locator(`#date_judgment_made`).nth(position);
      await judgementMadeGroup.locator('input[name="date_judgment_made-day"]').fill(day);
      await judgementMadeGroup.locator('input[name="date_judgment_made-month"]').fill(month);
      await judgementMadeGroup.locator('input[name="date_judgment_made-year"]').fill(year);
    }

    async enterJudgementSentDate(day: string, month: string, year: string, position: number = 0) {
      const judgementSentGroup = this.page.locator(`#date_judgment_sent`).nth(position);
      await judgementSentGroup.locator('input[name="date_judgment_sent-day"]').fill(day);
      await judgementSentGroup.locator('input[name="date_judgment_sent-month"]').fill(month);
      await judgementSentGroup.locator('input[name="date_judgment_sent-year"]').fill(year);
    }

    async issueJudgement() {
        await this.assertJudgementPageIsDisplayed();
        await this.selectNonHearingJudgement('Yes');
        await this.selectJudgmentType('Case Management');
        await this.addJurisdiction(['DAG']);
        await this.enterJudgmentMadeDate('01', '01', '2025');
        await this.enterJudgementSentDate('02', '01', '2025');
        await this.clickSubmitButton();
    }
}

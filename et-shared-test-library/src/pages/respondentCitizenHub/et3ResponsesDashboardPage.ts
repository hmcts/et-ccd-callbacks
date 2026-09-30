import {BasePage} from "../basePage.ts";
import {expect, Page} from "@playwright/test";

export default class Et3ResponsesDashboardPage extends BasePage {

  constructor(page: Page) {
    super(page);
  }

  async assertCaseListedInAwaitingResponse(caseNumber: string, caseId: string) {
    await this.page.waitForLoadState('load');
    const table = this.page.locator(`//h2[normalize-space()='Awaiting Response']/following-sibling::div[1]/table`);
    await expect(table).toBeVisible();
    const caseRow = table.getByLabel('view ' + caseNumber + ': ' + caseId.toString());
    await expect(caseRow).toBeVisible();
  }

  async assertCaseListedInResponseSubmittedTable(caseNumber: string, caseId: string) {
    await this.page.waitForLoadState('load');
    const table = this.page.locator(`//h2[normalize-space()='Response Submitted']/following-sibling::div[1]/table`);
    await expect(table).toBeVisible();
    const caseRow = table.getByLabel('view ' + caseNumber + ': ' + caseId.toString());
    await expect(caseRow).toBeVisible();
  }

}

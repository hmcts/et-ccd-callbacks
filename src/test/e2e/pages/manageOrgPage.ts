import { expect, Locator, Page } from '@playwright/test';
import { BasePage } from './basePage';

export class ManageOrgPage extends BasePage {
  readonly casesLink: Locator;
  readonly showCasesFilterButton: Locator;
  readonly clickAssignedCases: Locator;
  readonly clickUnassignedCases: Locator;
  readonly allAssigneesRadio: Locator;
  readonly assigneeNameSearchBox: Locator;
  readonly assigneeNameRadio: Locator;
  readonly caseRefTextBox: Locator;
  readonly unassignedCaseRadio: Locator;
  readonly caseByReferenceRadio: Locator;
  readonly applyFilterButton: Locator;

  constructor(page: Page) {
    super(page);
    this.casesLink = this.page.locator('//a[.="Cases"]');
    this.showCasesFilterButton = this.page.getByRole('button', { name: 'Show cases filter' });
    this.clickAssignedCases = page.locator('//a[.="Assigned cases"]');
    this.clickUnassignedCases = page.locator('//a[.="Unassigned cases"]');
    this.allAssigneesRadio = page.locator(`#allAssignedCases`);
    this.assigneeNameSearchBox = page.locator('#assignee-person');
    this.assigneeNameRadio = page.locator(`#casesAssignedToAUser`);
    this.caseRefTextBox = page.locator('#case-reference-number');
    this.unassignedCaseRadio = page.locator('#casesNotAssignedToAUser');
    this.caseByReferenceRadio = page.locator('#findCaseByReferenceNumber');
    this.applyFilterButton = page.getByRole('button', { name: 'Apply filter' });
  }

  async allAssignedCases() {
    await this.delay(10);
    await this.page.waitForSelector('text=Organisation');
    await this.page.waitForSelector('text=Users');
    await this.page.waitForSelector('text=Cases not assigned to any users');
    await this.allAssigneesRadio.click();
    await this.applyFilterButton.click();
  }

  async filterByAssigneeName(assigneeName: string) {
    await this.page.waitForSelector('text=Filter cases');
    await this.page.waitForSelector('text=Cases assigned to a user');
    await this.assigneeNameRadio.click();
    await this.page.waitForSelector('text=Type the name and select an available match option');
    await this.assigneeNameSearchBox.fill(assigneeName);
    await this.page.locator(`xpath=//span[normalize-space()='${assigneeName}']`).click();
    await this.applyFilterButton.click();
    const firstResult = this.page.locator(`xpath=//table/tbody/tr[1]/td[2]`);
    await firstResult.waitFor({state: 'visible', timeout: 10000});
    return await firstResult.textContent();
  }

  async verifyResultsCount(expTotCount: number) {
    await this.page.locator('tbody tr').first().waitFor();
    const totRes = await this.page.locator('tbody tr').count();
    expect(totRes).toEqual(expTotCount);
  }

  async filterByReferenceNumber(caseReferenceNumber: string) {
    await this.page.waitForSelector('text=Filter cases');
    await this.page.waitForSelector('text=Find case by reference number');
    await this.caseByReferenceRadio.click();
    await this.caseRefTextBox.fill(caseReferenceNumber);
    await this.applyFilterButton.click();
    await this.delay(3000);
  }

  async unassignedCases() {
    await this.page.waitForSelector('text=Filter cases');
    await this.page.waitForSelector('text=Cases not assigned to any users');
    await this.unassignedCaseRadio.click();
    await this.applyFilterButton.click();
  }

  async assignCaseToSolicitor(assigneeName: string) {
    await this.casesLink.click();
    await this.page.waitForLoadState('load');
    await this.waitForSpinner();
    await this.showCasesFilterButton.click();
    await this.page.waitForLoadState('load');
    await this.waitForSpinner();

    await this.allAssignedCases();
    let caseReferenceNumber = await this.filterByAssigneeName(assigneeName);
    await this.filterByReferenceNumber(caseReferenceNumber?? '');
  }

  async unassignCaseFromSolicitor() {
    await this.unassignedCases();
  }
}

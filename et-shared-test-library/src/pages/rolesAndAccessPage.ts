import { BasePage } from "./basePage";
import { expect, Locator, Page } from "@playwright/test";

export default class RolesAndAccessPage extends BasePage {
  private readonly allocateCtscRoleLink: Locator;
  private readonly allocatedCtscCaseworker: Locator;
  private readonly allocateToMe: Locator;
  private readonly daysRadio: Locator;
  private readonly indefiniteRadio: Locator;
  private readonly confirmAllocationButton: Locator;
  private readonly tbody: Locator;
  private readonly allocateALegalOpsRoleLink: Locator;
  private readonly allocateTribunalCaseworkerRadio: Locator;


  constructor(page: Page) {
    super(page);
    this.allocateCtscRoleLink = page.getByRole('link', { name: 'Allocate a CTSC role' });
    this.allocatedCtscCaseworker = page.getByText('Allocated CTSC Caseworker');
    this.allocateToMe = page.getByText('Allocate to me');
    this.daysRadio = page.getByRole('radio', { name: 'days' });
    this.indefiniteRadio = page.getByRole('radio', { name: 'Indefinite' });
    this.confirmAllocationButton = page.getByRole('button', { name: 'Confirm allocation' });
    this.tbody = page.locator('tbody');
    this.allocateALegalOpsRoleLink = this.page.getByRole('link', { name: 'Allocate a Legal Ops role' });
    this.allocateTribunalCaseworkerRadio = this.page.getByText('Allocated Tribunal Caseworker');
  }

  async assignAccessToCtscUser() {
    await this.allocateCtscRoleLink.click();
    await this.allocatedCtscCaseworker.click();
    await this.clickContinue();
    await this.allocateToMe.click();
    await this.clickContinue();
    await this.daysRadio.check();
    await this.indefiniteRadio.check();
    await this.clickContinue();
    await this.confirmAllocationButton.click();
    await expect(this.tbody).toContainText('Allocated CTSC Caseworker');
  }

  async assignAccessToLegalOpsRoleToMe() {
    await this.allocateALegalOpsRoleLink.click();
    await this.allocateTribunalCaseworkerRadio.click();
    await this.clickContinue();
    await this.allocateToMe.click();
    await this.clickContinue();
    await this.indefiniteRadio.check();
    await this.clickContinue();
    await this.confirmAllocationButton.click();
    await expect(this.tbody).toContainText('Allocated Tribunal Caseworker');
  }
}

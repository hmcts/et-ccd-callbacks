import { BaseEventPage } from './BaseEventPage.ts';
import { expect, Locator, Page } from '@playwright/test';

export default class CaseTransferToEcmPage extends BaseEventPage {
  private readonly caseTransferToEcmTitle: Locator;
  private readonly caseTransferToEcmOffice: Locator;
  private readonly caseTransferToEcmReason: Locator;

  constructor(page: Page) {
    super(page);
    this.caseTransferToEcmTitle = this.page.getByText('Case Transfer to ECM', { exact: true });
    this.caseTransferToEcmOffice = this.page.locator('#ecmOfficeCT');
    this.caseTransferToEcmReason = this.page.locator('#reasonForCT');
  }

  async assertCaseTransferToEcmPage() {
    await this.page.waitForLoadState('load');
    await expect(this.caseTransferToEcmTitle).toBeVisible();
  }

  async transferCaseToEcm(office: string, reason: string, submitted: boolean = true) {
    await this.assertCaseTransferToEcmPage();

    await expect(this.caseTransferToEcmOffice).toBeVisible();
    await this.caseTransferToEcmOffice.selectOption({ label: office });
    await expect(this.caseTransferToEcmReason).toBeVisible();
    await this.caseTransferToEcmReason.fill(reason);
    await this.clickSubmitButton(submitted);
  }
}

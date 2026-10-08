import { BasePage } from "./basePage";
import { expect, Locator, Page } from "@playwright/test";
import {CheckYourAnswersPage} from "./helpers/CheckYourAnswersPage";
import {CaseDetailsPage} from "./caseDetailsPage";

export class CaseTransferPage extends BasePage {
  private readonly caseTransferReason: Locator;

  constructor(page: Page) {
    super(page);
    this.caseTransferReason = page.locator('#reasonForCT');
  }

  async progressCaseTransfer() {
    await expect(this.caseTransferReason).toBeVisible();
    await this.caseTransferReason.fill('Transfer case to Scotland RET');
    await this.clickContinue();
  }

  async checkYourAnswer(checkYourAnswersPage: CheckYourAnswersPage) {
    await checkYourAnswersPage.assertCheckYourAnswersPage({
      tableName: 'Check your answers',
    rows: [
      {
      cellItem: 'Select the office you want to transfer the case to', value : 'Glasgow'
    },
    {
      cellItem: 'Reason for Case Transfer', value: 'Transfer case to Scotland RET'
    }
    ]
    });
    await this.page.getByRole('button', { name: 'Transfer Case' }).click();
  }

  async assertCaseDetailsTabDataAfterCaseTransfer(caseDetailsPage: CaseDetailsPage) {
    await caseDetailsPage.assertTabData([
      {
        tabName: 'Case Details',
        tabContent:[
          'Case Status:  Transferred',
          'Case Transfer: Transferred to Glasgow',
          { tabItem: 'Current Position', value: 'Case transferred - other country' },
        ]
      },
    ]);
  }

  async getNewDigitalCaseReferenceNumber() {
    const newSubRef = await this.page.locator('#case-viewer-field-read--feeGroupReference').textContent();
    return newSubRef? newSubRef.trim() : '';
  }
}

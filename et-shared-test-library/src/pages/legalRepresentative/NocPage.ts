import { expect, Locator, Page } from '@playwright/test';
import { BasePage } from '../basePage.ts';
import { AxeUtils } from '@hmcts/playwright-common';

export class NocPage extends BasePage{
  private readonly nocTitle:Locator;
  private readonly caseIdText:Locator;
  private readonly fullNameText:Locator;
  private readonly tribCaseNumberText:Locator;
  private readonly alreadyAccessToCaseTitle: Locator;
  private readonly confirmCheckbox: Locator;
  private readonly serveNoticeOfChangeToEveryPartyCheckbox: Locator;

  constructor(page: Page) {
    super(page);
    this.nocTitle = this.page.getByRole('heading', { name: 'Notice of change' });
    this.caseIdText = this.page.locator(`#caseRef`);
    this.fullNameText = this.page.locator(`#respondentName`);
    this.tribCaseNumberText = this.page.locator(`#caseReference`);
    this.alreadyAccessToCaseTitle = this.page.getByRole('heading', {
      name: 'Your organisation already has access to this case',
    });
    this.confirmCheckbox = this.page.locator(`#affirmation`);
    this.serveNoticeOfChangeToEveryPartyCheckbox = this.page.locator(`#notifyEveryParty`);
  }

  async assertNocPage() {
    await this.page.waitForLoadState('load');
    await expect(this.nocTitle).toBeVisible();
  }

  async enterCaseIdText(caseRef: string) {
    await this.caseIdText.fill(caseRef.toString());
    await this.page.waitForLoadState('load');
  }

  async enterClientDetails(fullName: string, tribCaseNumber: string) {
    await this.page.waitForLoadState('load');
    await expect(this.page.getByRole('heading', { name: "Enter your client's details" })).toBeVisible();
    await this.fullNameText.fill(fullName);
    await this.tribCaseNumberText.fill(tribCaseNumber);
    await this.page.waitForLoadState('load');
  }

  async assertCheckAndSubmitPage(caseRef: string, fullName: string, tribCaseNumber: string) {
    await this.page.waitForLoadState('load');
    await expect(this.page.getByRole('heading', { name: 'Check and submit' })).toBeVisible();
    await expect(
      this.page
        .getByText('Request', { exact: true })
        .locator('xpath=./following-sibling::div')
        .getByText('Notice of change', { exact: true }),
    ).toBeVisible();
    await expect(
      this.page.getByText('Case number', { exact: true })
        .locator('xpath=./following-sibling::div')
        .getByText(caseRef),
    ).toBeVisible();
    await expect(
      this.page
        .locator('div')
        .filter({
          hasText:
            'Enter the full name of the party you are representing, exactly as it is written on the ET1 claim form',
        })
        .getByText(fullName),
    ).toBeVisible();
    await expect(
      this.page.locator('div').filter({ hasText: 'Employment tribunal case number' }).getByText(tribCaseNumber),
    ).toBeVisible();
  }

  async selectIConfirmAndServeNoticeOnEveryPartyCheckbox() {
    await this.page.waitForLoadState('load');
    await this.confirmCheckbox.check();
    await this.serveNoticeOfChangeToEveryPartyCheckbox.check();
  }

  async assertNocSuccessPage() {
    await this.page.waitForLoadState('load');
    await this.waitForSpinner();
    await expect(this.page.getByRole('heading', { name: /Notice of change successful/, exact: false })).toBeVisible();
    await expect(this.page.getByRole('heading', { name: /You're now representing a client on case/, exact: false })).toBeVisible();
  }

  async processNocRequest(caseId: string, fullName: string, tribCaseNumber: string, accessibilityEnabled?: boolean, axeUtils?: AxeUtils) {
    await this.page.waitForLoadState('load');
    if (accessibilityEnabled && axeUtils) await axeUtils.audit();

    await this.assertNocPage();
    await this.enterCaseIdText(caseId);
    if (accessibilityEnabled && axeUtils) await axeUtils.audit();
    await this.clickContinue();

    await this.enterClientDetails(fullName, tribCaseNumber);
    if (accessibilityEnabled && axeUtils) await axeUtils.audit();
    await this.clickContinue();

    await this.assertCheckAndSubmitPage(caseId, fullName, tribCaseNumber);
    await this.selectIConfirmAndServeNoticeOnEveryPartyCheckbox();
    if (accessibilityEnabled && axeUtils) await axeUtils.audit();
    await this.clickSubmitButton();

    await this.assertNocSuccessPage();
    if (accessibilityEnabled && axeUtils) await axeUtils.audit();
  }


}

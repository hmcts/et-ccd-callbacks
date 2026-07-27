import { expect, Locator, Page } from '@playwright/test';
import { BasePage } from '../basePage.ts';

export class Et1ClaimsListPage extends BasePage {
  private readonly myEt1Claims: Locator;
  private readonly draftClaimsTable: Locator;
  private readonly submittedClaimsTable: Locator;
  private readonly addAnExistingClaimLink: Locator;
  private readonly caseNumberText: Locator;
  private readonly caseIdText: Locator;
  private readonly fullNameText: Locator;
  private readonly confirmationCheckbox: Locator;

  constructor(page: Page) {
    super(page);
    this.myEt1Claims = this.page.getByRole('link', { name: 'My ET1 claims', exact: true });
    this.draftClaimsTable = this.page.locator(`//caption[normalize-space()='Draft claims']/../tbody`);
    this.submittedClaimsTable = this.page.locator(`//caption[normalize-space()='Submitted claims']/../tbody`);
    this.addAnExistingClaimLink = this.page.getByRole('link', { name: 'Add an existing claim', exact: true });
    this.caseNumberText = this.page.locator(`#ethosCaseReference`);
    this.caseIdText = this.page.locator(`#caseReferenceId`);
    this.fullNameText = this.page.locator(`#claimantName`);
    this.confirmationCheckbox = this.page.locator(`#confirmation`);
  }

  async clickMyEt1ClaimLink() {
    await this.page.waitForLoadState('load');
    await expect(this.myEt1Claims).toBeVisible();
    await this.myEt1Claims.click();
  }

  async assertCaseListedInDraftClaims(caseId: string) {
    await this.page.waitForLoadState('load');
    await expect(this.draftClaimsTable).toBeVisible();
    const targetHref = `/claimant-application/${caseId}?lng=en`;
    const targetLink = this.draftClaimsTable.locator(`a[href="${targetHref}"]`);
    await expect(targetLink).toBeVisible();
    return targetLink;
  }

  async continueToCaseFromDraftClaimsTable(caseId: string) {
    const link = await this.assertCaseListedInDraftClaims(caseId);
    await expect(link).toBeVisible();
    expect(await link.textContent()).toBe('Continue');
    await link.click();
  }

  async assertCaseListedInSubmittedClaims(caseId: string) {
    await this.page.waitForLoadState('load');
    await expect(this.submittedClaimsTable).toBeVisible();
    const targetHref = `/citizen-hub/${caseId}?lng=en`;
    const targetLink = this.submittedClaimsTable.locator(`a[href="${targetHref}"]`);
    await expect
      .poll(
        async() => {
          const visible = await targetLink.isVisible();
          if (!visible) {
            await this.page.reload();
          }
          return visible;
        },
        {
          intervals: [1_000],
          timeout: 7000,
          message: `Case with ID ${caseId} not found in Submitted claims table after waiting for 7 seconds.`,
        }
      )
      .toBeTruthy();
    return targetLink;
  }

  async navigateToCaseFromSubmittedClaimsTable(caseId: string) {
    await this.page.reload();
    await this.page.waitForLoadState('load');
    const caseNumberLink = await this.assertCaseListedInSubmittedClaims(caseId);
    await expect(caseNumberLink).toBeVisible();
    const text = await caseNumberLink.textContent();
    await caseNumberLink.click();
    return text? text: '';
  }

  async navigateToAddAnExistingClaimLink() {
    await this.page.waitForLoadState('load');
    await expect(this.addAnExistingClaimLink).toBeVisible();
    await this.addAnExistingClaimLink.click();
  }

  async enterCaseNumberText(caseNumber: string) {
    await this.page.waitForLoadState('load');
    await expect(this.caseNumberText).toBeVisible();
    await this.caseNumberText.clear();
    await this.page.waitForLoadState('load');
    await this.caseNumberText.fill(caseNumber.toString());
  }

  async enterYourCaseDetails(caseId: string, fullName: string) {
    await this.page.waitForLoadState('load');
    await this.caseIdText.fill(caseId.toString());
    await this.fullNameText.fill(fullName);
  }

  async confirmAndSubmitCaseDetails(submitted: boolean = true) {
    await this.page.waitForLoadState('load');
    await expect(this.confirmationCheckbox).toBeVisible();
    await expect(this.page.getByRole('heading', {name: 'Check and submit'})).toBeVisible();
    await this.confirmationCheckbox.check();
    await this.clickSubmitButton(submitted);
  }

  async assertCaseIdNotListedInET1ClaimsPage(caseId: string) {
    await this.page.waitForLoadState('load');
    const targetHref = `/claimant-application/${caseId}?lng=en`;
    const draftTargetLink = this.draftClaimsTable.locator(`a[href="${targetHref}"]`);
    await expect(draftTargetLink).not.toBeVisible();
    const submittedTargetLink = this.submittedClaimsTable.locator(`a[href="${targetHref}"]`);
    await expect(submittedTargetLink).not.toBeVisible();
}

  async viewYourET1Claims() {
    await expect(this.page.getByRole('heading', { name: 'Your claim has been saved' })).toBeVisible();
    await this.page.getByRole('button', { name: 'View your ET1 claims' }).click();
    await expect(this.page.getByRole('heading', { name: 'ET1 claims' })).toBeVisible();
  }
}

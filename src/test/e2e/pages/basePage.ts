import { expect, Locator, Page } from '@playwright/test';
import { CommonActionsHelper } from './helpers/CommonActionsHelper.ts';

export abstract class BasePage {
  readonly page: Page;
  readonly commonActionsHelper: CommonActionsHelper;
  readonly saveAsDraftButton: Locator;
  readonly closeAndReturnButton: Locator;
  readonly applyFilterButton: Locator;
  readonly addNewBtn: Locator;
  readonly newHearingBtn: string;
  readonly newUploadDocBtn: Locator;
  readonly continueButton: Locator;
  readonly submitButton: Locator;
  private readonly spinner: Locator;
  private readonly errorHeading: Locator;
  private readonly respondButton: Locator;

  constructor(page: Page) {
    this.page = page;
    this.commonActionsHelper = new CommonActionsHelper();
    this.saveAsDraftButton = page.getByRole('button', { name: 'Save as draft' });
    this.closeAndReturnButton = this.page.getByRole('button', { name: 'Close and Return to case' });
    this.applyFilterButton = this.page.getByRole('button', { name: 'Apply filter' });
    this.addNewBtn = page.getByRole('button', { name: 'Add new' });
    this.newHearingBtn = '#hearingCollection > div > button.button.write-collection-add-item__bottom.ng-star-inserted';
    this.newUploadDocBtn = page.locator('//*[@id="documentCollection"]/div/button[2]');
    this.continueButton = page.getByRole('button', { name: 'Continue' });
    this.submitButton = page.getByRole('button', { name: 'Submit' });
    this.spinner = this.page.locator('xuilib-loading-spinner');
    this.errorHeading = this.page.locator(`#edit-case-event_error-summary-heading`);
    this.respondButton = this.page.getByRole('button', { name: 'Respond' });
  }

  async wait(time: number) {
    await this.page.waitForTimeout(time);
  }

  async clickContinue(url: string = '', pageNum?: number, shouldContinue: boolean = true) {
    await this.page.waitForLoadState('load');
    const maxRetries = 3;
    let attempt = 0;
    while (attempt < maxRetries) {
      await this.continueButton.scrollIntoViewIfNeeded();
      await expect(this.continueButton).toBeVisible();
      await expect(this.continueButton).toBeEnabled();
      await this.continueButton.click({force: true});
      await this.page.waitForLoadState('load');
      await this.waitForSpinner(180_000); // 3 mins dynamic wait time for spinner to disappear as some CCD callbacks can take time
      if (!shouldContinue) return;
      // If url or url+pageNum is provided, check if current URL contains the expected string
      if (url || url?.length > 0) {
        const expected = `${url}${pageNum ? pageNum : ''}`;
        try{
          attempt++;
          await this.page.waitForURL(new RegExp(expected), {timeout: 2000});
          return;
        } catch (err) {
          if(!process.env.CI) console.log(`URL - ${expected} check failed after clicking Continue. Retrying... (Attempt ${attempt})`);
          continue;
        }
      }
      // Check if error is visible
      const h3Visible = await this.errorHeading.isVisible().catch(() => false);
      if (h3Visible) {
        // Check if Continue button is still visible
       const errStr = await this.errorHeading.textContent();
        attempt++;
        if(!process.env.CI) console.log(`Error '${errStr}' detected after clicking Continue. Retrying... (Attempt ${attempt})`);
        await this.page.waitForTimeout(2000); // wait 2s before retry
        continue;
      }
      if(!process.env.CI) console.log('No error detected after clicking Continue.');
      // No error and URL is as expected, break out of loop
      return;
    }
    if (shouldContinue && attempt === maxRetries) {
      console.warn('Continue button retried maximum times, but error or URL mismatch still present.');
      throw new Error('Continue button retried maximum times, but error or URL mismatch still present.');
    }
  }

  async waitForSpinner(timeoutWait: number = 60_000) {
    await expect
      .poll(
        async () => await this.spinner.count(),
        {
          intervals: [2_000],
          timeout: timeoutWait,
        }
        )
      .toBe(0);
  }

  async saveAsDraft() {
    await this.saveAsDraftButton.click();
    await this.waitForSpinner();
    await this.page.waitForLoadState('load');
  }

  async clickCloseAndReturn() {
    await this.closeAndReturnButton.click();
    await this.waitForSpinner();
    await this.page.waitForLoadState('load');
  }

  /**
   * Clicks the Submit button and waits for the submission to complete.
   *
   * - If `submitted` is true (default):
   *   - Retries up to 3 times if an error heading is detected after clicking Submit.
   *   - Waits for the spinner and page load after each attempt.
   *   - Throws an error if submission fails after 3 attempts.
   * - If `submitted` is false:
   *   - Clicks Submit, waits for spinner and page load, and returns immediately (no error checks).
   *
   * @param submitted - If true, ensures the page is submitted and no error is present. If false, just clicks and returns.
   */
  async clickSubmitButton(submitted: boolean = true) {
    await this.page.waitForLoadState('load');
    await this.submitButton.scrollIntoViewIfNeeded();
    await this.page.evaluate(el => el?.scrollIntoView({ block: 'center' }), await this.submitButton.elementHandle());
    await expect(this.submitButton).toBeVisible();
    await expect(this.submitButton).toBeEnabled();

    const maxRetries = submitted ? 3 : 1;
    let attempt = 0;

    while (attempt < maxRetries) {
      await this.submitButton.click({force: true });
      await this.page.waitForLoadState('load', { timeout: 4000 });
      await this.waitForSpinner(180_000);
      if (!submitted) return;
      // Check if error is visible
      const h3Visible = await this.errorHeading.isVisible().catch(() => false);
      if (h3Visible) {
        const errStr = await this.errorHeading.textContent();
        attempt++;
        if(!process.env.CI) console.log(`Error '${errStr}' detected after clicking Submit. Retrying... (Attempt ${attempt})`);
        await this.page.waitForTimeout(2000); // wait 2s before retry
        continue;
      }
      // No error, break out of loop
      return;
    }
    if (submitted && attempt === maxRetries) {
      console.warn('Submit button retried maximum times, but error still present.');
      throw new Error('Submit button retried maximum times, but error still present.');
    }
  }

  async clickIgnoreAndContinueButton() {
    await this.page.getByRole('button', { name: 'Ignore Warning and Continue' }).click();
    await this.page.waitForLoadState('load', { timeout: 5000 });
  }

  async delay(ms: number) {
    return new Promise(resolve => setTimeout(resolve, ms));
  }

  async clickNextButton() {
    await this.page.getByRole('button', { name: 'Next' }).click();
  }

  async clickShareCaseButton() {
    await this.page.getByRole('button', { name: ' Share Case ' }).click();
  }

  async enterPostCode(postcode: string) {
    await this.page.getByRole('textbox', { name: 'Enter a UK postcode' }).fill(postcode);
    await this.page.waitForLoadState('load');
    await this.page.getByRole('button', { name: 'Find address' }).click();
    await this.page.waitForLoadState('load');
    await this.waitForSpinner();
    await this.page.getByLabel('Select an address').selectOption('2: Object');
    await this.page.waitForLoadState('load');
  }

  async signoutButton() {
    await this.page.getByText('Sign out').click();
  }

  async signOutButtonSyr() {
    await this.page.getByRole('link', { name: 'Sign out' }).click();
  }

  async clickStartNow() {
    await this.page.getByRole('button', { name: 'Start now' }).click();
    await this.page.waitForLoadState('load');
    await this.waitForSpinner();
  }

  async saveAndContinueButton() {
    await this.page.getByRole('button', { name: 'Save and continue' }).click();
  }

  async addNewButtonClick() {
    await this.addNewBtn.click();
  }

  async addNewHearingButtonClick() {
    await this.page.locator(this.newHearingBtn).click();
  }

  async addNewUploadDocButtonClick() {
    await this.newUploadDocBtn.click();
  }

  async addRespondentButton() {
    await this.page.getByRole('button', { name: 'Add another respondent' }).click();
  }

  /**
   * Asserts that the dropdown contains the expected options.
   *
   * @param options - The expected list of option strings.
   * @param dropDownLocator - The Playwright Locator for the dropdown element.
   */
  async assertDropDownOptionsAreVisible(options: string[], dropDownLocator: Locator) {
    await expect(dropDownLocator).toBeVisible();
    const optionsInDropDown = (await dropDownLocator.locator('option').allTextContents())
      .filter(opt => {return opt.trim() !== '--Select a value--';});
    expect(optionsInDropDown.sort()).toEqual(options.sort());
  }

  async clickRespondButton() {
    await expect(this.respondButton).toBeVisible();
    await this.respondButton.click();
    await this.waitForSpinner();
  }

  /**
   * Asserts that each error message in the provided array is visible on the page.
   *
   * @param errorMessages - An array of error message strings to check for visibility.
   * Each message is expected to be present and visible on the current page.
   */
  async assertErrorMessage(errorMessages: string[]) {
    for (const errorMessage of errorMessages) {
      const errorLocators = this.page.getByText(errorMessage);
      const count = await errorLocators.count();
      const errorLocator = errorLocators.nth(0);
      await expect(errorLocator).toBeVisible();
    }
  }

  async assertTextNotVisibleInPage(texts: string[]) {
    for (const text of texts) {
      const textLocators = this.page.getByText(text);
      const count = await textLocators.count();
      expect(count).toBe(0);
    }
  }

  async waitForWorkAllocationTasksToDisappear() {
    await expect
      .poll(
        async () => {
          const tasks = await this.page.locator(`exui-case-task`).count();
          return tasks === 0;
        },
        {
          intervals: [10_000],
          timeout: 100_000,
        }
      )
      .toBeTruthy();
  }
}

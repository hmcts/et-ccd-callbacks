import { expect, Locator, Page } from '@playwright/test';
import { BasePage } from './basePage';
import { UserCredentials } from '../config/config.dynamic.ts';
import { CookieUtils } from '../data-utils/cookie.utils.ts';
import { staticConfig } from '../config/config.static.ts';

const aatUrl = staticConfig.manageCaseBaseUrl;

export default class LoginPage extends BasePage {
  private readonly username: Locator;
  private readonly password: Locator;
  private readonly signInOrContinueOrSubmitButton: Locator;
  readonly signOutLink: Locator;

  constructor(page: Page) {
    super(page);
    // given different locators for handling new and Old IDAM UI changes
    this.username = page.locator('[data-testid="idam-username-input"], #username, input[name="username"], #email ,input[type="email"]');
    this.password = page.locator('[data-testid="idam-password-input"], #password, input[name="password"], input[type="password"]');
    this.signInOrContinueOrSubmitButton = page.locator('[data-testid="idam-submit-button"], [name="save"], button[type="submit"], input[type="submit"]')
      .filter({ hasText: /Sign in|Continue/i });
    this.signOutLink = page.locator(`//a[normalize-space()='Sign out']`)
  }

  async headingText(): Promise<string> {
    await expect(this.page.locator('h1')).toBeVisible();
    return (await this.page.locator('h1').first().innerText()).trim();
  }

  async processLogin(user: UserCredentials, baseUrl : string = aatUrl) {
    await this.page.waitForTimeout(1000);
    await this.page.waitForLoadState('load');
    if (await this.signOutLink.count() > 0 || await this.username.count() === 0) {
      // Do not overwrite the canonical session file when a context is already authenticated.
      return;
    }
    const currentHeading = await this.headingText();

    if (currentHeading === 'Sign in or create an account' || currentHeading === 'Sign in') {
      await this.username.fill(user.email);
      await this.password.fill(user.password);
      await this.signInOrContinueOrSubmitButton.click();
    } else if(currentHeading === 'Enter your email address') {
      await this.username.fill(user.email);
      await this.signInOrContinueOrSubmitButton.click();
      await this.password.fill(user.password);
      await this.signInOrContinueOrSubmitButton.click();
    } else {
      throw new Error(`Unexpected login Page heading: '${currentHeading}'`)
    }

    await this.page.waitForURL(new RegExp(baseUrl), { timeout: 10000 });
    await this.saveSession(user.sessionFile);
    await CookieUtils.addSessionFreshnessCookie(user.sessionFile, baseUrl, user.email);
    await this.page.waitForTimeout(2000);
    await this.page.waitForLoadState('load');
  }

  async saveSession(sessionFile: string): Promise<void> {
    await this.page.context().storageState({ path: sessionFile });
  }
}

import { expect, Locator, Page } from '@playwright/test';
import { BasePage } from "../basePage.js";

export class CUIPreLoginPage extends BasePage{

  private readonly syaLandingPageTitle: Locator;
  private readonly startNewClaimButton: Locator;
  private readonly beforeYouContinueHeading: Locator;
  private readonly representingMyselfRadio: Locator;
  private readonly claimForSomeoneElseRadio: Locator;
  private readonly legalRepSingleClaimRadio: Locator;
  private readonly legalRepMultipleClaimRadio: Locator
  private readonly signInOptionLink: Locator;

  constructor(page: Page) {
    super(page);
    this.syaLandingPageTitle = this.page.getByRole('heading', {name: 'Make a claim to an employment tribunal'});
    this.startNewClaimButton = this.page.locator(`xpath=//a[normalize-space()='Start a new claim']`);
    this.beforeYouContinueHeading = this.page.getByRole('heading', {name: 'Before you continue'});
    this.representingMyselfRadio = this.page.locator('input[id=lip-or-representative]');
    this.claimForSomeoneElseRadio = this.page.locator('input[id=lip-or-representative-2]');
    this.legalRepSingleClaimRadio = this.page.locator('input[id=lip-or-representative-3]');
    this.legalRepMultipleClaimRadio = this.page.locator('input[id=lip-or-representative-4]');
    this.signInOptionLink = page.locator('//a[@href="/enter-email"]');
  }

  async processPreLoginPagesForTheDraftApplication(region: string, typeOfClaim:string) {
    await this.startDraftApplication();
    await this.processBeforeYourContinuePage();
    await this.processAreYouMakingTheClaimForYourselfPage(typeOfClaim);
    await this.processAreYouMakingTheClaimOnYourOwnPage();// Remove this once latest changes are made in all env
    await this.whereYouMakeTheClaimPage(region);
    await this.processDoYouHaveAnACASEarlyConciliation();

    const isSignInOptionAvailable = await this.signInOptionLink
      .waitFor({ state: 'visible', timeout: 3000 })
      .then(() => true)
      .catch(() => false);
    if(isSignInOptionAvailable) {
      await this.signInOptionLink.click();
      await this.page.waitForLoadState('load');
    }
    await this.page.waitForLoadState('load');

  }

  async startDraftApplication() {
    await this.page.waitForLoadState('load');
    await expect(this.syaLandingPageTitle).toBeVisible();
    await this.verifyMakeAClaimToAnEmploymentTribunal();
    await this.startNewClaimButton.click();
  }

  async processBeforeYourContinuePage() {
    await this.page.waitForLoadState('load');
    await expect(this.beforeYouContinueHeading).toBeVisible();
    await this.verifyBeforeYouContinueGuidanceText();
    await this.clickContinue();
  }

  async selectWhoIsMakingTheClaim(option: string) {
    switch(option) {
      case 'Claiming for myself':
        await this.representingMyselfRadio.click();
        break;
      case 'Claiming for someone else':
        await this.claimForSomeoneElseRadio.click();
        break;
      case 'legal representative representing claimant':
        await this.legalRepSingleClaimRadio.click();
        break;
       default:
         throw new Error(`Option ${option} not found for who is making the claim question`);
    }
  }

  async processAreYouMakingTheClaimForYourselfPage(typeOfClaim:string) {
    await this.page.waitForLoadState('load');
    await this.selectWhoIsMakingTheClaim(typeOfClaim);
    await this.clickContinue();
  }

  async processAreYouMakingTheClaimOnYourOwnPage() {
    await this.page.waitForLoadState('load');
    const heading = this.page.getByRole('heading', {
      name: /Claiming on your own or with others/i
    });

    const isOnThisPage = await heading
      .waitFor({ state: 'visible', timeout: 5000 })
      .then(() => true)
      .catch(() => false);
    if(isOnThisPage) {
      console.log('Claiming on your own or with other claim question');
      await this.page.locator('#single-or-multiple-claim').check();
      await this.clickContinue();
    }
  }

  async whereYouMakeTheClaimPage(location: string = 'EnglandWales') {
    await this.page.waitForLoadState('load');
    switch (location) {
      case 'EnglandWales':
        await this.page.check('input[id=claim-jurisdiction]');
        break;
      case 'Scotland':
        await this.page.check('input[id=claim-jurisdiction-2]');
        break;
      default:
        throw new Error(`Location ${location} not recognized for where you want to make the claim`);
    }
    await this.clickContinue();
  }

  async processDoYouHaveAnACASEarlyConciliation() {
    await this.page.waitForSelector('#main-form', { timeout: 5000 });
    await this.page.check('input[id=acas-multiple]');
    await this.clickContinue();
  }

  async verifyMakeAClaimToAnEmploymentTribunal(): Promise<void> {
    await expect(
      this.page.getByText(
        'Use this service to make claims to an employment tribunal when an employer, potential employer, trade union or'
      )
    ).toBeVisible();
    await expect(this.page.getByText('person at work has treated another person unlawfully.')).toBeVisible();
    await expect(
      this.page.getByText(
        "During the employment tribunal process the organisations or people you're making a claim against are known as"
      )
    ).toBeVisible();
    await expect(this.page.getByText('‘respondents’.')).toBeVisible();
    await expect(
      this.page.getByText('To make a claim you need to contact Acas and get an ‘Early conciliation certificate’ from them or give us a')
    ).toBeVisible();
    await expect(this.page.getByText('valid reason why you do not have one.')).toBeVisible();
    await expect(this.page.getByText('If you do not have a certificate or a reason you need to contact Acas (opens in new tab).')).toBeVisible();
    await expect(
      this.page.getByText('You do not have to make your claim in one go. You can save and return to it at any time before you submit it.')
    ).toBeVisible();
    await expect(this.page.getByText('months of employment ending or problems happening. If a claim is')).toBeVisible();
    await expect(this.page.getByText('late, you must explain why. A judge will then decide what happens next.')).toBeVisible();
    await expect(this.page.getByText('Help using the service')).toBeVisible();
    await expect(
      this.page.getByText('Call one of our Employment Tribunal customer contact centres. They cannot give you legal advice.')
    ).toBeVisible();
    await expect(this.page.getByText('Monday to Friday, 9am to 5pm')).toBeVisible();
    await expect(this.page.getByText('Telephone: 0300 323 0196')).toBeVisible();
    await expect(this.page.getByText('Telephone: 0300 303 5176 (Welsh language)')).toBeVisible();
    await expect(this.page.getByText('Telephone: 0300 790 6234 (Scotland)')).toBeVisible();
    await expect(this.page.getByText('Find out about call charges (opens in new tab)')).toBeVisible();
  }

  async verifyBeforeYouContinueGuidanceText(): Promise<void> {
    await expect(
      this.page.getByText('You’ll have the opportunity to provide detailed information about your claim, but you might want to have')
    ).toBeVisible();
    await expect(this.page.getByText('the minimum required information to hand:')).toBeVisible();
    await expect(this.page.getByText('The name and address of each claimant (you or the person you’re claiming for)')).toBeVisible();
    await expect(
      this.page.getByText('Name and address of each respondent you’re making the claim against - this can be found in any')
    ).toBeVisible();
    await expect(this.page.getByText('letter you received offering you the job, a contract of employment or payslips')).toBeVisible();
    await expect(
      this.page.getByText('Certificate number from the Acas early conciliation process for each respondent, or explain why you')
    ).toBeVisible();
    await expect(this.page.getByText('do not have one')).toBeVisible();
    await expect(
      this.page.getByText('You do not have to complete your claim in one session. You can save your progress and return to the claim')
    ).toBeVisible();
    await expect(this.page.getByText('at any time before submitting.')).toBeVisible();
  }

}

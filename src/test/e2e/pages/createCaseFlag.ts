import { BasePage } from "./basePage";
import { expect, Locator, Page } from "@playwright/test";

export default class CreateCaseFlag extends BasePage {
    private readonly caseLevelCheckbox: Locator;
    private readonly urgentCaseCheckbox: Locator;
    private readonly rroCheckbox: Locator;
    private readonly addCommentsLabel: Locator;
    private readonly addCommentsField: Locator;
    private readonly importantLabel: Locator;
    private readonly viewCaseFlagsLink: Locator;
    private readonly caseFlagField: Locator;

    constructor(page: Page) {
        super(page);
        this.caseLevelCheckbox = page.getByLabel('Case level');
        this.urgentCaseCheckbox = page.getByLabel('Urgent case');
        this.rroCheckbox = page.getByLabel('RRO (Restricted Reporting');
        this.addCommentsLabel = page.getByLabel('Add comments for this flag');
        this.addCommentsField = page.getByLabel('Add comments for this flag');
        this.importantLabel = page.getByLabel('Important');
        this.viewCaseFlagsLink = page.getByRole('link', { name: 'View case flags' });
        this.caseFlagField = page.locator('ccd-read-case-flag-field');
    }

    async createCaseFlag() {
        await expect(this.caseLevelCheckbox).toBeVisible();
        await this.caseLevelCheckbox.check();
        await this.clickContinue();
        await this.urgentCaseCheckbox.check();
        await this.rroCheckbox.check();
        await this.urgentCaseCheckbox.check();
        await this.clickContinue();
        await this.addCommentsLabel.click();
        await this.addCommentsField.fill('This is an urgent case.');
        await this.clickContinue();
        await this.clickSubmitButton();
        await expect(this.importantLabel.getByRole('paragraph')).toContainText(' There is 1 active flag on this case. View case flags');
        await this.viewCaseFlagsLink.click();
        await expect(this.caseFlagField).toContainText('Urgent case');
        await expect(this.caseFlagField).toContainText('Active');
    }
}

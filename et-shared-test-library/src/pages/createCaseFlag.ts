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
    private readonly claimantFlag: Locator;
    private readonly claimantRepOrRespondentFlag: Locator;
    private readonly respondentRepFlag: Locator;

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
        this.claimantFlag = page.locator('#flag-location-1');
        this.claimantRepOrRespondentFlag =page.locator('#flag-location-2');
        this.respondentRepFlag = page.locator('#flag-location-3');
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

    async createCaseFlagForClaimant(){
        await this.claimantFlag.check();
        await this.setFlagType('Confidential');
        await this.page.locator('#flagComments').fill('This is a claimant flag.');
        await this.setFlagStatus('Active');
        await expect(this.importantLabel.getByRole('paragraph')).toContainText(' There is 1 active flag on this case. View case flags');

    }

    async createCaseFlagForClaimantRep(){
        await this.claimantRepOrRespondentFlag.check();
        await this.selectCaseFlagType();
        await this.page.locator('#flagComments').fill('This is a claimant representative flag.');
        await this.setFlagStatus('Requested');
    }


    async validateCaseFlagForClaimantAndRep(activeFlag:boolean, banningOrder:boolean){
        if (banningOrder) {
            await expect(this.caseFlagField).toContainText('Banning order');
        } else {
            await expect(this.caseFlagField).toContainText('Guidance on how to complete forms');
        }
        await expect(this.caseFlagField).toContainText('Requested');
        await expect(this.caseFlagField).toContainText('Confidential party/address');
        if(activeFlag){
            await expect(this.caseFlagField).toContainText('Active');
        }
    }

    async createCaseFlagForRespondent(flagType:string, status:string){
        await this.claimantRepOrRespondentFlag.check();
        await this.setFlagType(flagType);
        await this.page.locator('#flagComments').fill('This is a respondent flag.');
        await this.setFlagStatus(status);
    }


    async createCaseFlagForRespondentRep(){
        await this.respondentRepFlag.check();
        await this.selectCaseFlagType();
        await this.page.locator('#flagComments').fill('This is a respondent representative flag.');
        await this.setFlagStatus('Requested');
    }

    async selectCaseFlagType(){
        await this.clickContinue();
        await expect(this.page.locator('#flag-type-0')).toBeVisible();
        await this.page.locator('#flag-type-0').check();
        await this.clickContinue();
        await expect(this.page.locator('#flag-type-1')).toBeVisible();
        await this.page.locator('#flag-type-1').check();
        await this.clickContinue();
        await expect(this.page.locator('#flag-type-0')).toBeVisible();
        await this.page.locator('#flag-type-0').check();
        await this.clickContinue();
        await expect(this.page.locator('#flagComments')).toBeVisible();
    }

    async setFlagStatus(flagType:string){
        await this.clickContinue();
        switch (flagType) {
            case 'Requested':
                await expect(this.page.locator('#flag-status-0')).toBeVisible();
                await this.page.locator('#flag-status-0').check();
                break;

            case 'Active':
                await expect(this.page.locator('#flag-status-1')).toBeVisible();
                await this.page.locator('#flag-status-1').check();
                break;

            default:
                throw new Error(`Invalid flagType: ${flagType}`);
        }
        await this.clickContinue();
        await expect(this.page.locator('dt', { hasText: 'Status' })).toBeVisible();
        await this.clickSubmitButton();
    }

    async setFlagType(flagType:string){
        await this.clickContinue();

        switch (flagType) {
            case 'Confidential':
                await expect(this.page.locator('#flag-type-2')).toBeVisible();
                await this.page.locator('#flag-type-2').check();
                break;

            case 'Banning order':
                await expect(this.page.locator('#flag-type-6')).toBeVisible();
                await this.page.locator('#flag-type-6').check();
                break;

            default:
                throw new Error(`Unsupported flag type: ${flagType}`);
        }

        await this.clickContinue();
        await expect(this.page.locator('#flagComments')).toBeVisible();
    }
}

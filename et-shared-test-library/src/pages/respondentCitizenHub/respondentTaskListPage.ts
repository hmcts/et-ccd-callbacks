import {BasePage} from "../basePage.ts";
import { expect, Page } from '@playwright/test';

export default class ResponseTaskListPage extends BasePage {

    constructor(page: Page) {
        super(page);
    }

    async validateTaskListPage(){
        await expect(this.page.locator('h1')).toContainText('Your response form (ET3)');
        await expect(this.page.locator('ol')).toContainText('1. Tell us about the respondent');
        await expect(this.page.locator('ol')).toContainText('2. Tell us about the claimant');
        await expect(this.page.locator('ol')).toContainText('3. Give us your response');
        await expect(this.page.locator('ol')).toContainText('4. Check and submit your response');
    }
}

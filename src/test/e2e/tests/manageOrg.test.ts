import { test } from '../fixtures/common.fixture';
import { config, users } from '../config/config.dynamic.ts';

const userDetailsData = require('../resources/payload/user-details.json');

test.describe('End to End Tests for Manage Organisation for Assigning and Non Assigning Cases', () => {
    test('Verify Assigned Cases for England and Wales', {tag: '@demo'}, async ({ page, loginPage, manageOrgPage }) => {
        await page.goto(config.TestUrlForManageOrg);
        await loginPage.processLogin(users.etManageOrgSuperUser, config.TestUrlForManageOrg);
        await manageOrgPage.assignCaseToSolicitor(userDetailsData.assigneeName);
        await manageOrgPage.unassignCaseFromSolicitor();
    });
});

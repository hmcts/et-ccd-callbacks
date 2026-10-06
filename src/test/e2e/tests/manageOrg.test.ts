import { test } from '../fixtures/et.test.fixture.ts';
import {config, users} from "@et-shared-test-library/core";

const userDetailsData = require('@et-shared-test-library/core/resources/payload/user-details.json');

test.describe('End to End Tests for Manage Organisation for Assigning and Non Assigning Cases', () => {
    test('Verify Assigned Cases for England and Wales', {tag: '@demo'}, async ({ page, loginPage, manageOrgPage }) => {
        await page.goto(config.TestUrlForManageOrg);
        await loginPage.processLogin(users.etManageOrgSuperUser, config.TestUrlForManageOrg);
        await manageOrgPage.assignCaseToSolicitor(userDetailsData.assigneeName);
        await manageOrgPage.unassignCaseFromSolicitor();
    });
});

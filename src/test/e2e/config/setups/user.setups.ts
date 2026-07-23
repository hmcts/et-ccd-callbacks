import { test } from '../../fixtures/common.fixture.ts';
import { users } from '../config.dynamic.ts';
import { CookieUtils } from '../../data-utils/cookie.utils.ts';

test.describe.serial("set up user context", () => {
  test.use({ storageState: { cookies: [], origins: [] } }); // start unauthenticated every setup test

  const cookieName = 'session-freshness-check';
  const xuiUsers = [
    { role: 'Case Worker', user: users.etCaseWorker },
    { role: 'Legal Rep1', user: users.etLegalRepresentative },
    { role: 'Legal Rep2', user: users.etLegalRepresentative2 },
    { role: 'Judge', user: users.etEnglandJudge },
    { role: 'Legal Ops User', user: users.etLegalOpsUser },
    { role: 'Judge Work Allocation', user: users.etWorkAllocationJudge}
  ];
  for (const { role, user } of xuiUsers) {
    test(
      `Set up ${role} user context`,
      async ({ loginPage, manageCaseDashboardPage }) => {
        if (CookieUtils.isSessionValid(user.sessionFile, cookieName, user.email)) {
          console.log(`Valid session already exists for ${role} (${user.email}), skipping login.`);
          return;
        }
        console.log(`Logging new session for ${role} (${user.email})`);
        await manageCaseDashboardPage.visit();
        await loginPage.processLogin(user);
      }
    );
  }

  test(
    'Set up Citizen Claimant user context',
    async({citizenHubLoginPage,})  => {
      const user = users.etClaimant;
      if(CookieUtils.isSessionValid(user.sessionFile, cookieName, user.email || undefined)) {
        console.log(`Valid session already exists for ${user.email}, skipping login.`);
        return;}
      await citizenHubLoginPage.processCitizenHubLogin(user);
    });

  test(
    'Set up Citizen Claimant2 user context',
    async({citizenHubLoginPage,})  => {
      const user = users.etClaimant2;
      if(CookieUtils.isSessionValid(user.sessionFile, cookieName, user.email || undefined)) {
        console.log(`Valid session already exists for ${user.email}, skipping login.`);
        return;}
      await citizenHubLoginPage.processCitizenHubLogin(user);
    });

  const respondentUsers = [
    { role: 'Respondent 1', user: users.etRespondent },
    { role: 'Respondent 2', user: users.etRespondent2 },
  ]

  for ( const { role, user } of respondentUsers) {
    test(
      `Set up Citizen ${role} user context`,
      async({et3LoginPage})  => {
        if(CookieUtils.isSessionValid(user.sessionFile, cookieName, user.email || undefined)) {
          console.log(`Valid session already exists for ${role} ${user.email}, skipping login.`);
          return;
        }
        await et3LoginPage.processRespondentLogin(user);
      });
  }

});

Feature('XUI is available');

const { I } = inject();
const xuiLogin = require('../pages/xuiLogin');
const xuiUrl = process.env.XUI_URL;

Scenario('login to XUI', () => {
  I.amOnPage(xuiUrl);
  xuiLogin.signInWithCredentials();
});

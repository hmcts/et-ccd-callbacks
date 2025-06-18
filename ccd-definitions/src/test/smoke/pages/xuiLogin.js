const { I } = inject();

const username = process.env.TEST_CASEWORKER_USERNAME;
const password = process.env.TEST_CASEWORKER_PASSWORD;

function signInWithCredentials() {
  I.seeElement('#username');
  I.fillField('#username', username);
  I.fillField('#password', password);
  I.click('Sign in');
  I.see('Case list');
}
module.exports = { signInWithCredentials };

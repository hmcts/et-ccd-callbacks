const { I } = inject();

const username = process.env.CCD_ADMIN_USERNAME;
const password = process.env.CCD_ADMIN_PASSWORD;

function signInWithCredentials() {
  I.seeElement('#username');
  I.fillField('#username', username);
  I.fillField('#password', password);
  I.click('Sign in');
  I.see('Welcome to CCD Admin Web', 'h2');
}
module.exports = { signInWithCredentials };

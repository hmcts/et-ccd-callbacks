'use strict';

module.exports = async function (userName, password) {

    const I = this;
    I.amOnPage('/', 10);
    I.waitForText('Sign in');
    I.fillField('username', userName);
    I.fillField('password', password);
    I.wait(3);
    I.wait(3);
    I.click('[type="submit"]');
    I.wait(3);
    I.refreshPage();
    I.see('Sign out');
};

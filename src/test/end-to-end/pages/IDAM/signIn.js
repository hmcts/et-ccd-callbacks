'use strict';

module.exports = async function (userName, password) {

    const I = this;
    I.amOnPage('/', 10);
    I.waitForText('Sign in');
    I.fillField('username', userName);
    I.fillField('password', password);
    I.wait(3);
    I.forceClick('[name="save"]');
    I.waitForElement('.hmcts-button--secondary',25);
    I.see('Case list');
};

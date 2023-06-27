'use strict';

module.exports = async function (userName, password) {

    const I = this;
    I.amOnPage('/', 10);
    I.waitForText('Sign in');
    I.fillField('username', userName);
    I.fillField('password', password);
    I.click('[name="save"]');
    I.waitForElement('.hmcts-button--secondary',30);
    I.see('Case list');
};

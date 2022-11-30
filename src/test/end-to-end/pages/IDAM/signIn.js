'use strict';

const testConfig = require('../../../config');

module.exports = async function (userName, password) {

    const I = this;
    I.amOnPage('/', 10);
    I.waitForText('Sign in');
    I.fillField('username', userName);
    I.click('[name="save"]');
    I.waitForText('Case list', 30);
    I.waitForClickable('.hmcts-button--secondary');
};

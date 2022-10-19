'use strict';

const testConfig = require('../../../config');

module.exports = async function (userName, password) {

    const I = this;
    I.amOnPage('/', 10);
    I.waitForText('Sign in');
    I.fillField('username', userName);
    I.fillField('password', password);
    I.click('input[value="Sign in"]');
    I.waitForText('Case list');
};

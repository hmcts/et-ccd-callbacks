'use strict';

const commonConfig = require('../../data/commonConfig.json');

module.exports =  async function () {
    const I = this;
    I.click(commonConfig.createCase);
    I.wait(commonConfig.time_interval_2_seconds);
}

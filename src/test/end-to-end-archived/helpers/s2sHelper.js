const {Logger} = require('@hmcts/nodejs-logging');
const axios = require('axios');
const testConfig = require('../../config.js');
const logger = Logger.getLogger('helpers/s2sHelper.js');
const env = testConfig.TestEnv;

async function getServiceToken() {
    //const serviceSecret = testConfig.TestS2SAuthSecret;
    const s2sBaseUrl = `http://rpe-service-auth-provider-${env}.service.core-compute-${env}.internal`;
    const s2sAuthPath = '/testing-support/lease';
    const oneTimePassword = testConfig.oneTimePassword

    const resp = await axios.post(
        s2sBaseUrl + s2sAuthPath,
        { microservice: 'xui_webapp', oneTimePassword },
        {
            headers: {
                'Content-Type': 'application/json'
            }
        }
    );

    const serviceToken = resp.data;
    logger.debug(serviceToken);
    return serviceToken;
}

module.exports = {
    getServiceToken
}

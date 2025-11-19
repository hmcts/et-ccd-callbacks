const {Logger} = require('@hmcts/nodejs-logging');
const axios = require('axios');
const testConfig = require('../../config.js');
const logger = Logger.getLogger('helpers/idamApi.js');

async function getUserToken() {
    const username = testConfig.TestEnvCWUser;
    const password = testConfig.TestEnvCWPassword;
    const redirectUri = testConfig.RedirectUri;
    const idamClientSecret = testConfig.TestIdamClientSecret;
    const idamBaseUrl = testConfig.IdamBaseUrl;
    const idamCodePath = `/oauth2/authorize?response_type=code&client_id=xuiwebapp&redirect_uri=${redirectUri}`;

    const codeResponse = await axios.post(
        idamBaseUrl + idamCodePath,
        null,
        {
            headers: {
                Authorization: 'Basic ' + Buffer.from(`${username}:${password}`).toString('base64'),
                'Content-Type': 'application/x-www-form-urlencoded'
            }
        }
    ).catch(error => {
        console.log(error);
    });

    const code = codeResponse.data.code;
    const idamAuthPath = `/oauth2/token?grant_type=authorization_code&client_id=xuiwebapp&client_secret=${idamClientSecret}&redirect_uri=${redirectUri}&code=${code}`;

    const authTokenResponse = await axios.post(
        idamBaseUrl + idamAuthPath,
        null,
        {
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            }
        }
    );

    logger.debug(authTokenResponse.data['access_token']);
    return authTokenResponse.data['access_token'];
}

async function getUserId(authToken) {
    const idamBaseUrl = testConfig.IdamBaseUrl;
    const idamDetailsPath = '/details';

    const userDetails = await axios.get(
        idamBaseUrl + idamDetailsPath,
        {
            headers: {
                Authorization: `Bearer ${authToken}`
            }
        }
    );

    logger.debug(userDetails.data.id);
    return userDetails.data.id;
}

module.exports = {
    getUserToken,
    getUserId
};

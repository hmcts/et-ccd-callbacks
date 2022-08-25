const {Logger} = require('@hmcts/nodejs-logging');
const testConfig = require('../../config.js');
const querystring = require('querystring');
const logger = Logger.getLogger('helpers/idamApi.js');

const { I } = inject()

async function getUserToken() {
    const username = testConfig.TestEnvCWUser;
    const password = testConfig.TestEnvCWPassword;
    const idamBaseUrl = 'https://idam-api.aat.platform.hmcts.net';
    const idamAuthPath = `/loginUser`;
    let url = idamBaseUrl + idamAuthPath;
    let payload = querystring.stringify({
        // eslint-disable-next-line no-undef
        username: username,
        // eslint-disable-next-line no-undef
        password: password,
    })
    const headers =  {
        'Content-Type': 'application/x-www-form-urlencoded'
    }
    const authTokenResponse = await I.sendPostRequest(url,payload,headers);

    const  authToken = authTokenResponse.data.access_token ;
    console.log('... The auth token is ...=>'+authToken);
    return authToken;
}

async function getUserId() {
    const idamBaseUrl = 'https://idam-api.aat.platform.hmcts.net';
    const idamDetailsPath = '/details';
    let token = await getUserToken();
    console.log('checking token' +token)
      let url = idamBaseUrl + idamDetailsPath;
      let headers =
    {
        'Authorization': `Bearer ${token}`
    };
    const userDetails = await I.sendGetRequest(url,headers);
    const userId =  userDetails.data.id
    logger.debug('... The user ID is ...=>'+userId);
    return { token, userId};
}

module.exports = {
    getUserToken,
    getUserId
};

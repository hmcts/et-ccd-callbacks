const {Logger} = require('@hmcts/nodejs-logging');
const requestModule = require('request-promise-native');
const request = requestModule.defaults();
const testConfig = require('../../config.js');
const querystring = require('querystring');
const logger = Logger.getLogger('helpers/idamApi.js');
const env = testConfig.TestEnv;

const { I } = inject()

async function getUserToken() {
    const username = testConfig.TestEnvCWUser;
    const password = testConfig.TestEnvCWPassword;
    const redirectUri = `https://manage-case.aat.platform.hmcts.net/oauth2/callback`;
    const idamClientSecret = testConfig.TestIdamClientSecret;
    const idamBaseUrl = 'https://idam-api.aat.platform.hmcts.net';
    //const idamCodePath = `/oauth2/authorize?response_type=code&client_id=xuiwebapp&redirect_uri=${redirectUri}`;

    //const codeResponse = await request.post({
    //    uri: idamBaseUrl + idamCodePath,
    //    headers: {
   //         Authorization: 'Basic ' + Buffer.from(`${username}:${password}`).toString('base64'),
    //        'Content-Type': 'application/x-www-form-urlencoded'
  //     }
   // }).catch(error => {
    //    console.log(error);
    //});

    //const code = JSON.parse(codeResponse).code;
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
    logger.debug('... The auth token is ...=>'+authToken);
    return authToken;
}

async function getUserId(authToken) {
    const idamBaseUrl = 'https://idam-api.aat.platform.hmcts.net';
    const idamDetailsPath = '/details';
      let url = idamBaseUrl + idamDetailsPath;
      let headers =
    {
        'Authorization': `Bearer ${authToken}`
    }
    const userDetails = await I.sendGetRequest(url,headers);
    const userId =  userDetails.data.id
    logger.debug('... The user ID is ...=>'+userId);
    return userId;
}

module.exports = {
    getUserToken,
    getUserId
};

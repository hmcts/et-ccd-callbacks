const {Logger} = require('@hmcts/nodejs-logging');
const requestModule = require('request-promise-native');
const fs = require('fs');
const request = requestModule.defaults();
const testConfig = require('../../config.js');
const querystring = require('querystring');
const logger = Logger.getLogger('helpers/idamApi.js');
const totp = require("totp-generator");
const {expect} = require('chai');
const env = testConfig.TestEnv;
const dataLocation = require('../data/et-ccd-basic-data.json')

const {I} = inject()
const location = 'ET_EnglandWales';
const etDataLocation = dataLocation.data;
const s2sBaseUrl = `http://rpe-service-auth-provider-${env}.service.core-compute-${env}.internal/testing-support/lease`;
const username = testConfig.TestEnvCWUser;
const password = testConfig.TestEnvCWPassword;
const idamBaseUrl = `https://idam-api.${env}.platform.hmcts.net/loginUser`;
const getUserIdurl = `https://idam-api.${env}.platform.hmcts.net/details`;
const ccdApiUrl = `http://ccd-data-store-api-${env}.service.core-compute-${env}.internal`;

async function getAuthToken() {

    let payload = querystring.stringify({
        // eslint-disable-next-line no-undef
        username: `${username}`,
        // eslint-disable-next-line no-undef
        password: `${password}`,
    })
    const headers = {
        'Content-Type': 'application/x-www-form-urlencoded'
    }
    const authTokenResponse = await I.sendPostRequest(idamBaseUrl, payload, headers);
    expect(authTokenResponse.status).to.eql(200);
    const authToken = authTokenResponse.data.access_token;
    logger.debug(authToken);
    return authToken
}

async function getS2SServiceToken() {

    const oneTimepwd = totp(testConfig.TestCcdGwSecret, {digits: 6, period: 30});
    // get s2s token
    console.log("checking OTP => :" + oneTimepwd);

    let s2sheaders = {
        'Content-Type': 'application/json'
    };
    let s2spayload = {
        'microservice': 'ccd_gw',
        'oneTimePassword': oneTimepwd
    }
    const s2sResponse = await I.sendPostRequest(s2sBaseUrl, s2spayload, s2sheaders);
    let serviceToken = s2sResponse.data;
    expect(s2sResponse.status).to.eql(200)
    logger.debug(serviceToken);
    return serviceToken
}

async function getUserDetails(authToken) {

    let getIdheaders = {
        'Authorization': `Bearer ${authToken}`
    };
    const userDetails = await I.sendGetRequest(getUserIdurl, getIdheaders);
    const userId = userDetails.data.id;

    console.log("checking userId =>" + userId);
    return userId;
}

async function createACase(authToken, serviceToken, userId) {

    const ccdStartCasePath = `/caseworkers/${userId}/jurisdictions/EMPLOYMENT/case-types/${location}/event-triggers/initiateCase/token`;
    const ccdSaveCasePath = `/caseworkers/${userId}/jurisdictions/EMPLOYMENT/case-types/${location}/cases?ignore-warning=false`;

    let initiateCaseUrl = ccdApiUrl + ccdStartCasePath
    let initiateCaseHeaders = {
        'Authorization': `Bearer ${authToken}`,
        'ServiceAuthorization': `Bearer ${serviceToken}`,
        'Content-Type': 'application/json'
    }
    let initiateCaseResponse = await I.sendGetRequest(initiateCaseUrl, initiateCaseHeaders);
    expect(initiateCaseResponse.status).to.eql(200);

    const initiateEventToken = initiateCaseResponse.data.token;
    console.log("checking eventToken" + initiateEventToken);

    // start case creation
    let createCasetemp = {
        data: etDataLocation,
        event: {
            id: 'initiateCase',
            summary: 'Creating Case',
            description: 'For CCD E2E Test'
        },
        'event_token': initiateEventToken
    };
    let createCaseUrl = ccdApiUrl + ccdSaveCasePath
    const createCaseHeaders = {
        'Authorization': `Bearer ${authToken}`,
        'ServiceAuthorization': `Bearer ${serviceToken}`,
        'Content-Type': 'application/json'
    };
    let createCasebody = `${JSON.stringify(createCasetemp)}`
    const createCaseResponse = await I.sendPostRequest(createCaseUrl, createCasebody, createCaseHeaders);

    expect(createCaseResponse.status).to.eql(201);
    const case_id = createCaseResponse.data.id
    console.log("checking case_id" + case_id);
    return case_id;
}

async function performCaseVettingEvent(authToken, serviceToken, case_id) {
    // initiate et1 vetting
    const initiateEvent = `/cases/${case_id}/event-triggers/et1Vetting?ignore-warning=false`;

    let et1VettingUrl = ccdApiUrl + initiateEvent;
    let et1VettingHeaders = {
        'Authorization': `Bearer ${authToken}`,
        'ServiceAuthorization': `Bearer ${serviceToken}`,
        'Content-Type': 'application/json'
    };

    let startVettingResponse = await I.sendGetRequest(et1VettingUrl, et1VettingHeaders);
    let case_data = startVettingResponse.data.case_details.case_data;
    let dataClassification = startVettingResponse.data.case_details.data_classification;
    let eventToken = startVettingResponse.data.token
    expect(startVettingResponse.status).to.eql(200)

    // execute et1 vetting
    const execuEt1teUrl = `http://ccd-data-store-api-${env}.service.core-compute-${env}.internal/cases/${case_id}/events`;

    const completeVettingHeader = {
        'Authorization': `Bearer ${authToken}`,
        'ServiceAuthorization': `Bearer ${serviceToken}`,
        'experimental': true,
        'Content-Type': 'application/json'
    };

    const executeEventBody = {
        data: case_data,
        data_classification: dataClassification,
        event: {
            id: 'et1Vetting',
            summary: '',
            description: ''
        },
        event_token: eventToken,
        ignore_warning: false,
        draft_id: null
    };

    console.log("... executing et1Vetting event ...")
    let executeEt1payload = JSON.stringify(executeEventBody);
    console.log("vetiing body => " + executeEt1payload);
    const eventExecutionResponse = await I.sendPostRequest(execuEt1teUrl, executeEt1payload, completeVettingHeader);
    expect(eventExecutionResponse.status).to.eql(201);
}

async function acceptTheCaseEvent(authToken, serviceToken, case_id, acceptedOrRejectedValue) {

    console.log("... application vetted, starting accept event...");
    const initiateNextEvent = `/cases/${case_id}/event-triggers/preAcceptanceCase?ignore-warning=false`;


    let initiateAcceptUrl = ccdApiUrl + initiateNextEvent;
    let startAcceptanceHeaders = {
        'Authorization': `Bearer ${authToken}`,
        'ServiceAuthorization': `Bearer ${serviceToken}`,
        'experimental': true,
        'Content-Type': 'application/json'
    };

    let startAcceptanceResponse = await I.sendGetRequest(initiateAcceptUrl, startAcceptanceHeaders);
    expect(startAcceptanceResponse.status).to.eql(200);
    let acceptEventToken = startAcceptanceResponse.data.token;


    // execute case
    let acceptBody = {
        "data": {
            "preAcceptCase": {
                "caseAccepted": `${acceptedOrRejectedValue}`,
                "dateAccepted": "2022-07-24"
            }
        },
        "event": {
            "id": "preAcceptanceCase",
            "summary": "",
            "description": ""
        },
        "event_token": acceptEventToken
    };

    console.log("... This is the payload for triggering next event" + acceptBody);
    let acceptUrl = `http://ccd-data-store-api-${env}.service.core-compute-${env}.internal/cases/${case_id}/events`
    let acceptHeaders = {
        'Authorization': `Bearer ${authToken}`,
        'ServiceAuthorization': `Bearer ${serviceToken}`,
        'experimental': true,
        'Content-Type': 'application/json'
    };
    let acceptPayload = JSON.stringify(acceptBody)

    const nextEventExecutionResponse = await I.sendPostRequest(acceptUrl, acceptPayload, acceptHeaders);
    expect(nextEventExecutionResponse.status).to.eql(201);
}

async function navigateToCaseDetailsScreen(case_id) {
    await I.authenticateWithIdam(username, password);
    await I.amOnPage('/case-details/' + case_id);
}

async function processCaseToSubmittedState() {
    // Login to IDAM to get the authentication token
    const authToken = await getAuthToken();
    const serviceToken = await getS2SServiceToken();

    //Getting the User Id based on the Authentication Token that is passed for this User.
    const userId = await getUserDetails(authToken);
    const case_id = await createACase(authToken, serviceToken, userId);
    //Navigate to the Case Detail Page
    await navigateToCaseDetailsScreen(case_id);
    return case_id;
}

async function processCaseToET1VettedState() {
    // Login to IDAM to get the authentication token
    const authToken = await getAuthToken();
    const serviceToken = await getS2SServiceToken();

    //Getting the User Id based on the Authentication Token that is passed for this User.
    const userId = await getUserDetails(authToken);
    const case_id = await createACase(authToken, serviceToken, userId);
    await performCaseVettingEvent(authToken, serviceToken, case_id);

    //Navigate to the Case Detail Pages
    await navigateToCaseDetailsScreen(case_id);
    return case_id;
}

async function processCaseToAcceptedState() {

    // Login to IDAM to get the authentication token
    const authToken = await getAuthToken();
    const serviceToken = await getS2SServiceToken();

    //Getting the User Id based on the Authentication Token that is passed for this User.
    const userId = await getUserDetails(authToken);
    const case_id = await createACase(authToken, serviceToken, userId);
    await performCaseVettingEvent(authToken, serviceToken, case_id);

    //Initiate accept case
    await acceptTheCaseEvent(authToken, serviceToken, case_id, 'Yes');

    //Navigate to the Case Detail Page
    await navigateToCaseDetailsScreen(case_id);
    return case_id;
}

async function processCaseToRejectedState() {

    // Login to IDAM to get the authentication token
    const authToken = await getAuthToken();
    const serviceToken = await getS2SServiceToken();

    //Getting the User Id based on the Authentication Token that is passed for this User.
    const userId = await getUserDetails(authToken);
    const case_id = await createACase(authToken, serviceToken, userId);
    await performCaseVettingEvent(authToken, serviceToken, case_id);

    //Initiate accept case
    await acceptTheCaseEvent(authToken, serviceToken, case_id, 'No');

    //Navigate to the Case Detail Page
    await navigateToCaseDetailsScreen(case_id);
    return case_id;
}

module.exports = {
    processCaseToSubmittedState,
    processCaseToET1VettedState,
    processCaseToAcceptedState,
    processCaseToRejectedState,

};

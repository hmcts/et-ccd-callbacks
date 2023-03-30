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

async function acceptTheCaseEvent(authToken, serviceToken, case_id) {

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
                "caseAccepted": "Yes",
                "dateAccepted": "2022-08-18"
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

async function rejectTheCaseEvent(authToken, serviceToken, case_id) {

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
                "caseAccepted": "No",
                "dateAccepted": null,
                "dateRejected": "2022-01-23",
                "rejectReason": [
                    "Not on Prescribed Form"
                ]
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

async function listTheCaseEvent(authToken, serviceToken, case_id, userId) {

    console.log("... application vetted, starting accept event...");
    const listingStartCasePath = `/caseworkers/${userId}/jurisdictions/EMPLOYMENT/case-types/${location}/cases/${case_id}/event-triggers/addAmendHearing/token`;
    const listingSaveCasePath = `/caseworkers/${userId}/jurisdictions/EMPLOYMENT/case-types/${location}/cases/${case_id}/events`;

    let initiateListUrl = ccdApiUrl + listingStartCasePath;
    let startListEventHeaders = {
        'Authorization': `Bearer ${authToken}`,
        'ServiceAuthorization': `Bearer ${serviceToken}`,
        'experimental': true,
        'Content-Type': 'application/json'
    };

    let startListingResponse = await I.sendGetRequest(initiateListUrl, startListEventHeaders);
    expect(startListingResponse.status).to.eql(200);
    let listEventToken = startListingResponse.data.token;
    const date = new Date(new Date().setHours(0, 0, 0, 0));
    switch (date.getDay()) {
        case 0: //Sunday
            date.setDate(date.getDate() + 1);
            break;
        case 6: //Saturday
            date.setDate(date.getDate() + 2);
            break;
        default:
    }
    const formattedDate = date.toJSON();
    console.log('The value of the formatted Date : ' + formattedDate);
    const listDate = formattedDate.substring(0, formattedDate.length - 1);
    console.log('The value of the listed Date : ' + listDate);

    let listEventBody = {
        "data": {
            "hearingCollection": [
                {
                    "value": {
                        "hearingNumber": "1",
                        "Hearing_type": "Preliminary Hearing",
                        "hearingPublicPrivate": "Public",
                        "judicialMediation": "Yes",
                        "Hearing_venue": {
                            "value": {
                                "code": "Hull Combined Court Centre",
                                "label": "Hull Combined Court Centre"
                            },
                            "selectedLabel": "Hull Combined Court Centre",
                            "selectedCode": "Hull Combined Court Centre",
                            "list_items": [
                                {
                                    "code": "Harrogate CJC",
                                    "label": "Harrogate CJC"
                                },
                                {
                                    "code": "Hull",
                                    "label": "Hull"
                                },
                                {
                                    "code": "Hull Combined Court Centre",
                                    "label": "Hull Combined Court Centre"
                                },
                                {
                                    "code": "IAC",
                                    "label": "IAC"
                                },
                                {
                                    "code": "Leeds",
                                    "label": "Leeds"
                                },
                                {
                                    "code": "Scarborough",
                                    "label": "Scarborough"
                                },
                                {
                                    "code": "Sheffield Combi",
                                    "label": "Sheffield Combined Court"
                                },
                                {
                                    "code": "Teesside ET",
                                    "label": "Teesside ET"
                                },
                                {
                                    "code": "Wakefield Count",
                                    "label": "Wakefield Civil and Family Justice Centre"
                                }
                            ]
                        },
                        "hearingEstLengthNum": "1",
                        "hearingEstLengthNumType": "Days",
                        "hearingSitAlone": "Full Panel",
                        "Hearing_stage": null,
                        "Hearing_notes": null,
                        "hearingShowDetails": null,
                        "judge": null,
                        "hearingERMember": null,
                        "hearingEEMember": null,
                        "hearingFormat": [
                            "In person"
                        ],
                        "hearingDateCollection": [
                            {
                                "value": {
                                    "listedDate": `${listDate}`,
                                    "Hearing_status": null,
                                    "Postponed_by": null,
                                    "postponedDate": null,
                                    "hearingVenueDay": null,
                                    "hearingRoom": null,
                                    "hearingClerk": null,
                                    "hearingCaseDisposed": null,
                                    "Hearing_part_heard": null,
                                    "Hearing_reserved_judgement": null,
                                    "attendee_claimant": null,
                                    "attendee_non_attendees": null,
                                    "attendee_resp_no_rep": null,
                                    "attendee_resp_&_rep": null,
                                    "attendee_rep_only": null,
                                    "hearingTimingStart": null,
                                    "hearingTimingBreak": null,
                                    "hearingTimingResume": null,
                                    "hearingTimingFinish": null,
                                    "hearingTimingDuration": null,
                                    "HearingNotes2": null
                                },
                                "id": "9f80414b-1679-49f5-b539-f169f1b7308b"
                            }
                        ]
                    },
                    "id": "62928f4f-5fbe-41c7-a2ae-ff73fd3b79ed"
                }
            ]
        },
        "event": {
            "id": "addAmendHearing",
            "summary": "",
            "description": ""
        },
        "event_token": listEventToken,
        "ignore_warning": false
    };

    let listSaveUrl = ccdApiUrl + listingSaveCasePath;
    let listHeaders = {
        'Authorization': `Bearer ${authToken}`,
        'ServiceAuthorization': `${serviceToken}`,
        'Content-Type': 'application/json'
    };

    let listPayload = JSON.stringify(listEventBody)

    const listEventExecutionResponse = await I.sendPostRequest(listSaveUrl, listPayload, listHeaders);
    expect(listEventExecutionResponse.status).to.eql(201);
}

async function jurisdictionToTheCaseEvent(authToken, serviceToken, case_id, userId) {

    console.log("... application vetted, starting accept event...");
    const jurisdictionStartCasePath = `/caseworkers/${userId}/jurisdictions/EMPLOYMENT/case-types/${location}/cases/${case_id}/event-triggers/addAmendJurisdiction/token`;
    const jurisdictionSaveCasePath = `/caseworkers/${userId}/jurisdictions/EMPLOYMENT/case-types/${location}/cases/${case_id}/events`;

    let initiateJurisdictionUrl = ccdApiUrl + jurisdictionStartCasePath;
    let startJurisdictionEventHeaders = {
        'Authorization': `Bearer ${authToken}`,
        'ServiceAuthorization': `Bearer ${serviceToken}`,
        'experimental': true,
        'Content-Type': 'application/json'
    };

    let startJurisdictionResponse = await I.sendGetRequest(initiateJurisdictionUrl, startJurisdictionEventHeaders);
    expect(startJurisdictionResponse.status).to.eql(200);
    let jurisdictionEventToken = startJurisdictionResponse.data.token;

    let jurisdictionEventBody = {
        "data": {
            "jurCodesCollection": [
                {
                    "value": {
                        "juridictionCodesList": "ADT",
                        "judgmentOutcome": "Input in error",
                        "dateNotified": null,
                        "disposalDate": null
                    },
                    "id": "ac8fe585-b37c-4e25-bc36-221275ca3c9a"
                }
            ]
        },
        "event": {
            "id": "addAmendJurisdiction",
            "summary": "",
            "description": ""
        },
        "event_token": jurisdictionEventToken,
        "ignore_warning": false
    };

    let jurisdictionSaveUrl = ccdApiUrl + jurisdictionSaveCasePath;
    let jurisdictionHeaders = {
        'Authorization': `Bearer ${authToken}`,
        'ServiceAuthorization': `${serviceToken}`,
        'Content-Type': 'application/json'
    };

    let jurisdictionPayload = JSON.stringify(jurisdictionEventBody)

    const jurisdictionEventExecutionResponse = await I.sendPostRequest(jurisdictionSaveUrl, jurisdictionPayload, jurisdictionHeaders);
    expect(jurisdictionEventExecutionResponse.status).to.eql(201);

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
    await acceptTheCaseEvent(authToken, serviceToken, case_id);

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

    //Initiate reject case
    await rejectTheCaseEvent(authToken, serviceToken, case_id);

    //Navigate to the Case Detail Page
    await navigateToCaseDetailsScreen(case_id);
    return case_id;
}

async function processCaseToListedState() {

    // Login to IDAM to get the authentication token
    const authToken = await getAuthToken();
    const serviceToken = await getS2SServiceToken();

    //Getting the User Id based on the Authentication Token that is passed for this User.
    const userId = await getUserDetails(authToken);
    const case_id = await createACase(authToken, serviceToken, userId);
    await performCaseVettingEvent(authToken, serviceToken, case_id);

    //Initiate accept case
    await acceptTheCaseEvent(authToken, serviceToken, case_id);
    await listTheCaseEvent(authToken, serviceToken, case_id, userId);

    //Navigate to the Case Detail Page
    await navigateToCaseDetailsScreen(case_id);
    return case_id;
}

async function processCaseToAcceptedWithAJurisdiction() {

    // Login to IDAM to get the authentication token
    const authToken = await getAuthToken();
    const serviceToken = await getS2SServiceToken();

    //Getting the User Id based on the Authentication Token that is passed for this User.
    const userId = await getUserDetails(authToken);
    const case_id = await createACase(authToken, serviceToken, userId);
    await performCaseVettingEvent(authToken, serviceToken, case_id);

    //Initiate accept case
    await acceptTheCaseEvent(authToken, serviceToken, case_id);
    await jurisdictionToTheCaseEvent(authToken, serviceToken, case_id, userId);

    //Navigate to the Case Detail Page
    await navigateToCaseDetailsScreen(case_id);
    return case_id;
}

async function processCaseToListedStateWithAJursdiction() {

    // Login to IDAM to get the authentication token
    const authToken = await getAuthToken();
    const serviceToken = await getS2SServiceToken();

    //Getting the User Id based on the Authentication Token that is passed for this User.
    const userId = await getUserDetails(authToken);
    const case_id = await createACase(authToken, serviceToken, userId);
    await performCaseVettingEvent(authToken, serviceToken, case_id);

    //Initiate accept case
    await acceptTheCaseEvent(authToken, serviceToken, case_id);
    await listTheCaseEvent(authToken, serviceToken, case_id, userId);
    await jurisdictionToTheCaseEvent(authToken, serviceToken, case_id, userId);

    //Navigate to the Case Detail Page
    await navigateToCaseDetailsScreen(case_id);
    return case_id;
}



module.exports = {
    processCaseToSubmittedState,
    processCaseToET1VettedState,
    processCaseToAcceptedState,
    processCaseToRejectedState,
    processCaseToListedState,
    processCaseToAcceptedWithAJurisdiction,
    processCaseToListedStateWithAJursdiction
};

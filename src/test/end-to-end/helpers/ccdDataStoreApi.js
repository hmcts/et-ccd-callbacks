const {Logger} = require('@hmcts/nodejs-logging');
const requestModule = require('request-promise-native');
const request = requestModule.defaults();
const fs = require('fs');
const testConfig = require('../../config.js');
const idamApi = require('./idamApi');
const s2sService = require('./s2sHelper');
const { expect } = require('chai');
const logger = Logger.getLogger('helpers/ccdDataStoreApi.js');
const env = testConfig.TestEnv;

const { I } = inject()

async function createCaseInCcd(dataLocation = 'src/test/end-to-end/data/et-ccd-basic-data.json', location = 'ET_EnglandWales') {
    const saveCaseResponse = await createECMCase(dataLocation, location).catch(error => {
        console.log(error);
    });
    const caseId = saveCaseResponse;
    logger.info('Created case: %s', caseId);
    return caseId;
}

async function createECMCase(dataLocation = 'src/test/end-to-end/data/et-ccd-basic-data.json',location = 'ET_EnglandWales') {
    let serviceToken = await s2sService.getServiceToken();
    let authToken =  await idamApi.getUserToken();
    let userId = await idamApi.getUserId();
    logger.debug('authToken ='+authToken)
    logger.debug('userId ='+userId)
    logger.debug('serviceToken ='+serviceToken)

    const ccdApiUrl = `http://ccd-data-store-api-${env}.service.core-compute-${env}.internal`;
    const ccdStartCasePath = `/caseworkers/${userId}/jurisdictions/EMPLOYMENT/case-types/${location}/event-triggers/initiateCase/token`;
    const ccdSaveCasePath = `/caseworkers/${userId}/jurisdictions/EMPLOYMENT/case-types/${location}/cases`;

    let url = ccdApiUrl + ccdStartCasePath;
    let headers = {
        'Authorization': `Bearer ${authToken}`,
        'ServiceAuthorization': `Bearer ${serviceToken}`,
        'Content-Type': 'application/json'
    };


    const startCaseResponse = await I.sendGetRequest(url,headers);
    expect(startCaseResponse.status).to.eql(200)
    logger.info('... status code is =>' + startCaseResponse.status)
    const eventToken = startCaseResponse.data.token;

    let dataset = fs.readFileSync(dataLocation);
    let data = JSON.parse(dataset);
    let body = {
        data: data,
        event: {
            id: 'initiateCase',
            summary: 'Creating Case',
            description: 'For CCD E2E Test'
        },
        'event_token': eventToken
    };
        url =  ccdApiUrl + ccdSaveCasePath;
        headers = {
            'Authorization': `Bearer ${authToken}`,
            'ServiceAuthorization': `Bearer ${serviceToken}`,
            'Content-Type': 'application/json'
        };
        let payload = JSON.stringify(body);


    let saveCaseResponse = await I.sendPostRequest(url,payload,headers);
    return saveCaseResponse;
}

async function updateECMCaseInCcd(caseId, dataLocation = 'data/ccd-accept-case.json') {

    const authToken = await idamApi.getUserToken();
    const userId = await idamApi.getUserId(authToken);
    const serviceToken = await s2sService.getServiceToken();
    logger.info('Updating case with id %s and event %s', caseId);

    const ccdApiUrl = `http://ccd-data-store-api-${env}.service.core-compute-${env}.internal`;
    const ccdStartEventPath = `${case_id}/event-triggers/${eventname}?ignore-warning=false`;
    const ccdSaveEventPath = `cases/${case_id}/events`;

    const startEventOptions = {
        method: 'GET',
        uri: ccdApiUrl + ccdStartEventPath,
        headers: {
            'Authorization': `Bearer ${authToken}`,
            'ServiceAuthorization': `Bearer ${serviceToken}`,
            'Content-Type': 'application/json'
        }
    };

    const startEventResponse = await request(startEventOptions);
    const eventToken = JSON.parse(startEventResponse).token;

    var data = fs.readFileSync(dataLocation);
    var saveBody = {
        data: JSON.parse(data),
        event: {
            id: eventId,
            summary: 'Updating Case',
            description: 'For CCD E2E Test'
        },
        'event_token': eventToken
    };

    const saveEventOptions = {
        method: 'POST',
        uri: ccdApiUrl + ccdSaveEventPath,
        headers: {
            'Authorization': `Bearer ${authToken}`,
            'ServiceAuthorization': `Bearer ${serviceToken}`,
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(saveBody)
    };

    let saveEventResponse = await request(saveEventOptions);
    return saveEventResponse;
}

async function initiateEt1Vetting() {
    const authToken = await idamApi.getUserToken();
    //const userId = await idamApi.getUserId.userId();
    const serviceToken = await s2sService.getServiceToken();
    const case_id = await createCaseInCcd();
    logger.info('Updating case with id %s and event %s', case_id);
    const ccdApiUrl = `http://ccd-data-store-api-${env}.service.core-compute-${env}.internal`;
    const initiateEvent = `/cases/${case_id}/event-triggers/et1Vetting?ignore-warning=false`;


       let url = ccdApiUrl + initiateEvent;
       let headers = {
            'Authorization': `Bearer ${authToken}`,
            'ServiceAuthorization': `Bearer ${serviceToken}`,
            'Content-Type': 'application/json'
        };

    let initiateEt1Response = await I.sendGetRequest(url, headers);
    expect(initiateEt1Response.status).to.eql(200)
    let caseData = initiateEt1Response.data.case_details.case_data;
    let data_classification = initiateEt1Response.data.case_details.data_classification;
    let eventToken = initiateEt1Response.data.token;
    return {case_id, caseData, data_classification, eventToken};
}

async function executeEt1Vetting() {
    const authToken = await idamApi.getUserToken();
    const serviceToken = await s2sService.getServiceToken();
    const case_data = await initiateEt1Vetting.caseData();
    const caseId = await initiateEt1Vetting.case_id();
    const dataClassification = await initiateEt1Vetting.data_classification();
    const eventToken = await initiateEt1Vetting.eventToken();
    const executeUrl = `/cases/${caseId}/events`;
    const baseUrl = `http://ccd-data-store-api-${env}.service.core-compute-${env}.internal`;

    let newurl =  baseUrl + executeUrl;
    let headers = {
        'Authorization': `Bearer ${authToken}`,
        'ServiceAuthorization': `Bearer ${serviceToken}`,
        'Content-Type': 'application/json'
    };
    const executeEventBody = JSON.parse({
        data: case_data,
        data_classification: dataClassification,
        event: {
            id: 'et1Vetting',
            summary: 'executing event via api before running xui tests',
            description: 'For ET XUI E2E Test'},
        event_token: eventToken,
        ignore_warning: false,
        draft_id: ""
    });


    let payload =  JSON.stringify(executeEventBody);

    const eventExecutionResponse = await I.sendPostRequest(newurl,payload,headers);
    expect(eventExecutionResponse.status).to.eql(201);
    return eventExecutionResponse;
}

async function initiateAcceptanceEvent() {
    const authToken = await idamApi.getUserToken();
    const userId = await idamApi.getUserId(authToken);
    const serviceToken = await s2sService.getServiceToken();
    let caseId = await initiateEt1Vetting.case_id();
    logger.info('using existing cased id =>', caseId);
    const ccdApiUrl = `http://ccd-data-store-api-${env}.service.core-compute-${env}.internal`;
    const initiateNextEvent = `/cases/${caseId}/event-triggers/?ignore-warning=false`;


    let url = ccdApiUrl + initiateNextEvent;
    let headers = {
        'Authorization': `Bearer ${authToken}`,
        'ServiceAuthorization': `Bearer ${serviceToken}`,
        'Content-Type': 'application/json'
    };

    let initiateNextEventResponse = await I.sendGetRequest(url, headers);
    expect(initiateNextEventResponse.status).to.eql(200);
    let case_data = initiateNextEventResponse.data.case_data;
    let data_classification = initiateNextEventResponse.data.data_classification;
    let preAccepteventToken = initiateNextEventResponse.data.token;
    return { case_data, data_classification, preAccepteventToken};
}

// preAcceptanceCase
async function executeAcceptanceEvent() {
  let caseId = await initiateEt1Vetting.case_id();
    logger.info('using existing cased id =>', caseId);
    let eventToken = await initiateAcceptanceEvent.preAccepteventToken;
    const executeUrl = `/cases/${caseId}/events`;
    const ccdApiUrl = `http://ccd-data-store-api-${env}.service.core-compute-${env}.internal`;
    logger.info('using existing cased id =>', caseId);
    let executeEventBody = {
        "preAcceptCase": {
            "caseAccepted": "Yes",
            "dateAccepted": "2022-07-24"
        },
        'event_token': eventToken
    };

    console.log("... This is the payload for triggering next event" + executeEventBody);
    let url = `http://ccd-data-store-api-aat.service.core-compute-aat.internal/cases/${caseId}/events`
    let headers = {
        'Authorization': `Bearer ${authToken}`,
        'ServiceAuthorization': `Bearer ${serviceToken}`,
        'Content-Type': 'application/json'
    };
    let payload =  JSON.stringify(executeEventBody)

    const nextEventExecutionResponse = await I.sendPostRequest(url,payload,headers);
    expect(nextEventExecutionResponse.status).to.eql(201);
    return nextEventExecutionResponse;
}

module.exports = {
    createCaseInCcd,
    createECMCase
};

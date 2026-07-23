/**
 * CuiApi provides helper methods for interacting with the ET SYA API for case creation, update, submission, vetting, and respondent assignment.
 * It supports both England/Wales and Scotland jurisdictions, and includes utilities for modifying payloads dynamically.
 */

import { axiosRequest } from './ApiHelper';
import engCase from '../../resources/payload/citizen/et-england-case-data.json';
import scotCase from '../../resources/payload/citizen/et-scotland-case-data.json';
import eng2RespondentCase from '../../resources/payload/citizen/et-england-case-data-2-repondent.json';
import et3 from '../../resources/payload/citizen/et3.json';
import { CaseTypeLocation } from '../../config/case-data.ts';
import { getUserAuthToken, getUserId } from './TokenHelperApi.ts';
import { set, unset } from 'lodash';
import { staticConfig } from '../../config/config.static.ts';

const syaApiBaseUrl = staticConfig.EtCosPreviewEtSyaApiUrl;
const payloadMap: Record<CaseTypeLocation, any> = {
  [CaseTypeLocation.EnglandAndWales]: engCase.data,
  [CaseTypeLocation.Scotland]: scotCase.data,
};
export class CuiApi {
  /**
   * Initiates a new CUI case for the given user and jurisdiction.
   * @param userName - The username for authentication.
   * @param password - The password for authentication.
   * @param caseTypeLocation - The jurisdiction (England/Wales or Scotland).
   * @returns The created case ID.
   */
  async initiateCuiCase(userName: string, password: string, caseTypeLocation: CaseTypeLocation): Promise<string> {
    const authToken = await getUserAuthToken(userName, password);

    const cuiDraftCasePath = 'cases/initiate-case';
    let initiateCaseUrl = syaApiBaseUrl + cuiDraftCasePath;

    //start case creation
    let createCaseBody = {
      case_type_id: caseTypeLocation.toString(),
      case_data: {
        caseType: 'Single',
        caseSource: 'Manually Created',
      },
    };

    let apiConfig = {
      method: 'post',
      maxBodyLength: Infinity,
      url: initiateCaseUrl,
      headers: {
        Authorization: `Bearer ${authToken}`,
        'Content-Type': 'application/json',
      },
      data: createCaseBody,
    };

    const response = await axiosRequest(apiConfig);
    return response.data.id;
  }

  /**
   * Updates a draft CUI case with the correct payload for the jurisdiction.
   * @param username - The username for authentication.
   * @param password - The password for authentication.
   * @param case_id - The case ID to update.
   * @param caseTypeLocation - The jurisdiction (England/Wales or Scotland).
   * @returns The updated case data.
   */
  async updateDraftCuiCase(
    username: string,
    password: string,
    case_id: string,
    caseTypeLocation: CaseTypeLocation,
    multipleRespondent: boolean = false
  ): Promise<any> {
    const authToken = await getUserAuthToken(username, password);
    let updateCaseUrl = `${syaApiBaseUrl}cases/update-case`;
    let updateCaseBody = {
      case_id: case_id.toString(),
      case_type_id: caseTypeLocation.toString(),
      case_data: multipleRespondent ? eng2RespondentCase.data : payloadMap[caseTypeLocation],
    };
    let apiConfig = {
      method: 'put',
      maxBodyLength: Infinity,
      url: updateCaseUrl,
      headers: {
        Authorization: `Bearer ${authToken}`,
        'Content-Type': 'application/json',
      },
      data: updateCaseBody,
    };
    const response = await axiosRequest(apiConfig);
    return response.data;
  }

  /**
   * Submits a draft CUI case for the given user and jurisdiction.
   * @param username - The username for authentication.
   * @param password - The password for authentication.
   * @param case_id - The case ID to submit.
   * @param caseTypeLocation - The jurisdiction (England/Wales or Scotland).
   * @returns The response data from the API after submission.
   */
  async submitDraftCuiCase(
    username: string,
    password: string,
    case_id: string,
    caseTypeLocation: CaseTypeLocation,
    multipleRespondent: boolean = false
  ): Promise<any> {
    const authToken = await getUserAuthToken(username, password);
    let submitCaseUrl = `${syaApiBaseUrl}cases/submit-case`;
    //start case creation
    let updateCaseBody = {
      case_id: case_id.toString(),
      case_type_id: caseTypeLocation.toString(),
      case_data: multipleRespondent ? eng2RespondentCase.data : payloadMap[caseTypeLocation],
    };

    let apiConfig = {
      method: 'put',
      maxBodyLength: Infinity,
      url: submitCaseUrl,
      headers: {
        Authorization: `Bearer ${authToken}`,
        'Content-Type': 'application/json',
      },
      data: updateCaseBody,
    };

    const response = await axiosRequest(apiConfig);
    return response.data;
  }

  /**
   * Vets and accepts a CUI case using the provided auth token and case ID.
   * @param username
   * @param password
   * @param case_id - The case ID to vet and accept.
   * @returns The vetted case ID.
   */
  async vetAndAcceptCuiCase(username: string, password: string, case_id: string): Promise<string> {
    const authToken = await getUserAuthToken(username, password);
    const url = `vetAndAcceptCase?caseId=${case_id}`;
    let vetCaseUrl = syaApiBaseUrl + url;

    let apiConfig = {
      method: 'post',
      maxBodyLength: Infinity,
      url: vetCaseUrl,
      headers: {
        Authorization: `Bearer ${authToken}`,
        'Content-Type': 'application/json',
      },
    };

    const response = await axiosRequest(apiConfig);
    return response.data.id;
  }

  /**
   * Retrieves case data for a given case ID and auth token.
   * @param username
   * @param password
   * @param case_id - The case ID to retrieve data for.
   * @returns The case data object.
   */
  async getCaseData(username: string, password: string, case_id: string): Promise<any> {
    const authToken = await getUserAuthToken(username, password);
    const getCaseDataUrl = `getCaseData?caseIds=${case_id}`;
    let etSyaApiGetCaseData = syaApiBaseUrl + getCaseDataUrl;
    let request = {
      method: 'GET',
      maxBodyLength: Infinity,
      url: etSyaApiGetCaseData,
      headers: {
        Authorization: `Bearer ${authToken}`,
        'Content-Type': 'application/json',
      },
    };

    const response = await axiosRequest(request);
    return response.data[0];
  }

  /**
   * Assigns a case to a respondent user.
   * @param username - The respondent's username.
   * @param password - The respondent's password.
   * @param case_id - The case ID to assign.
   * @returns The respondent collection ID after assignment.
   */
  async assignCaseToRespondent(username: string, password: string, case_id: string): Promise<string> {
    const authToken = await getUserAuthToken(username, password);
    const respondentUserId = await getUserId(authToken, username);
    const modifyCasePath = `manageCaseRole/modifyCaseUserRoles?modificationType=Assignment`;
    let createCaseUrl = syaApiBaseUrl + modifyCasePath;

    let data = JSON.stringify({
      case_users: [
        {
          case_type_id: 'ET_EnglandWales',
          respondent_name: 'Mrs Test Auto',
          case_id: `${case_id}`,
          user_id: `${respondentUserId}`,
          case_role: '[DEFENDANT]',
        },
      ],
    });

    let apiConfig = {
      method: 'post',
      maxBodyLength: Infinity,
      url: createCaseUrl,
      headers: {
        Authorization: `Bearer ${authToken}`,
        'Content-Type': 'application/json',
      },
      data: data,
    };

    const response = await axiosRequest(apiConfig);
    return response.data.caseDetails[0].case_data.respondentCollection[0].id;
  }

  /**
   * Submits an ET3 response for a respondent, modifying the ET3 payload as needed.
   * @param username - The respondent's username.
   * @param password - The respondent's password.
   * @param ccdId - The CCD respondent ID.
   * @param case_id - The case ID.
   * @param caseTypeLocation - The jurisdiction (England/Wales or Scotland).
   * @returns The ET3 submission ID.
   */
  async submitET3(
    username: string,
    password: string,
    ccdId: string,
    case_id: string,
    caseTypeLocation: CaseTypeLocation,
  ): Promise<string> {
    const authToken = await getUserAuthToken(username, password);
    const respondentUserId = await getUserId(authToken, username);

    const modifyCasePath = `et3/modifyEt3Data`;
    let createCaseUrl = syaApiBaseUrl + modifyCasePath;
    const replacementAction = [
      { action: 'delete', key: 'caseSubmissionReference' },
      { action: 'insert', key: 'caseSubmissionReference', value: case_id },
      { action: 'delete', key: 'caseTypeId' },
      { action: 'insert', key: 'caseTypeId', value: caseTypeLocation.toString() },
      { action: 'delete', key: 'respondent.id' },
      { action: 'insert', key: 'respondent.id', value: ccdId },
      { action: 'delete', key: 'respondent.value.idamId' },
      { action: 'insert', key: 'respondent.value.idamId', value: respondentUserId },
    ];

    this.makeModifications(replacementAction, et3);

    let apiConfig = {
      method: 'post',
      maxBodyLength: Infinity,
      url: createCaseUrl,
      headers: {
        Authorization: `Bearer ${authToken}`,
        'Content-Type': 'application/json',
      },
      data: et3,
    };

    const response = await axiosRequest(apiConfig);
    return response.data.id;
  }

  /**
   * Applies modifications to a JSON object based on the provided actions.
   * Supports 'delete' and 'insert' actions for nested keys.
   * @param dataModifications - An array of actions to modify the JSON object.
   * @param data - The JSON object to modify.
   */
  makeModifications(dataModifications: { action: string; key: string; value?: any }[], data: any): void {
    if (Array.isArray(dataModifications)) {
      dataModifications.forEach(modification => {
        const { action, key, value } = modification;
        if (!key) return;

        if (action === 'delete') {
          unset(data, key);
        } else if (action === 'insert') {
          set(data, key, value);
        }
      });
    }
  }
}

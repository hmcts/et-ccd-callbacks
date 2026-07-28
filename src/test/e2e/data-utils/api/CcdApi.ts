import { readFileSync } from 'fs';
import path from 'path';
import { set, unset } from 'lodash';
import { ReplacementAction } from '../../types/replacement-action';
import {axiosRequest} from './ApiHelper.ts';
import {getServiceToken, getUserId, getUserAuthToken} from './TokenHelperApi.ts';
import {AxiosResponse} from 'axios';
import { CaseTypeLocation } from '../../config/case-data.ts';
import { staticConfig } from '../../config/config.static.ts';

const ccdApiUrl = staticConfig.ccdDataStoreApi;

/**
 * CCD API utility class for interacting with the CCD Data Store API.
 * Provides methods to create, update, and save cases, as well as to retrieve event tokens and apply data modifications.
 */
export class CcdApi {
  /**
   * Retrieves a start event token for a given CCD event trigger path.
   *
   * @param ccdStartCasePath - The CCD API path for starting the event.
   * @param authToken - The user's authentication token.
   * @param serviceToken - The service authentication token.
   * @returns Promise resolving to the event token string.
   */
  async getStartEventToken(ccdStartCasePath: string, authToken: string, serviceToken: string): Promise<string> {
    const startCaseResponse = await axiosRequest({
      method: 'get',
      url: ccdApiUrl + ccdStartCasePath,
      headers: {
        Authorization: `Bearer ${authToken}`,
        ServiceAuthorization: `Bearer ${serviceToken}`,
        'Content-Type': 'application/json',
      },
    });
    return startCaseResponse.data.token;
  }

  /**
   * Saves a case to CCD using the provided path, tokens, and payload.
   *
   * @param ccdSaveCasePath - The CCD API path for saving the case.
   * @param authToken - The user's authentication token.
   * @param serviceToken - The service authentication token.
   * @param payload - The case data payload to save.
   * @returns Promise resolving to the Axios response from the save operation.
   */
  async saveCase(
    ccdSaveCasePath: string,
    authToken: string,
    serviceToken: string,
    payload: any,
  ): Promise<AxiosResponse> {
    return await axiosRequest({
      url: ccdApiUrl + ccdSaveCasePath,
      method: 'post',
      data: payload,
      headers: {
        Authorization: `Bearer ${authToken}`,
        ServiceAuthorization: `Bearer ${serviceToken}`,
        'Content-Type': 'application/json',
      },
    });
  }

  /**
   * Creates a new case in CCD by reading a data file, applying modifications, and submitting the case.
   *
   * @param userName - The username for authentication.
   * @param password - The password for authentication.
   * @param dataLocation - The file path to the case data JSON.
   * @param caseTypeLocation - The jurisdiction of the case.
   * @param eventId - The event ID to trigger case creation.
   * @param dataModifications - Optional array of modifications to apply to the case data.
   * @returns Promise resolving to the created case ID as a string.
   */
  async createCaseInCcd(
    userName: string,
    password: string,
    dataLocation: string,
    caseTypeLocation: CaseTypeLocation,
    eventId: string,
    dataModifications: ReplacementAction[] = [],
  ): Promise<any> {
    if (!process.env.CI) {
      console.info('Creating CCD case with event %s for type %s...', eventId, caseTypeLocation);
    }
    const authToken = await getUserAuthToken(userName, password);
    const userId = await getUserId(authToken, userName);
    const serviceToken = await getServiceToken();

    const ccdStartCasePath = `caseworkers/${userId}/jurisdictions/EMPLOYMENT/case-types/${caseTypeLocation.toString()}/event-triggers/${eventId}/token`;
    const ccdSaveCasePath = `caseworkers/${userId}/jurisdictions/EMPLOYMENT/case-types/${caseTypeLocation.toString()}/cases?ignore-warning=false`;

    const eventToken = await this.getStartEventToken(ccdStartCasePath, authToken, serviceToken);

    const content = readFileSync(path.resolve(dataLocation), 'utf-8');
    const data = JSON.parse(content);

    this.makeModifications(dataModifications, data);

    const payload = {
      data: data,
      event: {
        id: eventId,
        summary: 'Creating Basic Case',
        description: 'For CCD E2E Test',
      },
      event_token: eventToken,
    };

    const saveCaseResponse = await this.saveCase(ccdSaveCasePath, authToken, serviceToken, payload);
    const caseId = saveCaseResponse.data.id;
    const caseNumber = saveCaseResponse.data.case_data.ethosCaseReference;
    if (!process.env.CI) {
      console.info('Created case with id %s and number %s', caseId, caseNumber);
    }

    return saveCaseResponse?.data;
  }

  /**
   * Updates an existing case in CCD by reading a data file, applying modifications, and submitting the update event.
   *
   * @param userName - The username for authentication.
   * @param password - The password for authentication.
   * @param caseId - The ID of the case to update.
   * @param caseTypeLocation - The type of the case.
   * @param eventId - The event ID to trigger the update.
   * @param dataLocation - The file path to the case data JSON.
   * @param replacements - Optional array of modifications to apply to the case data.
   * @returns Promise resolving to the updated case data.
   */
  async updateCaseInCcd(
    userName: string,
    password: string,
    caseId: string,
    caseTypeLocation: CaseTypeLocation,
    eventId: string,
    dataLocation: string,
    replacements: ReplacementAction[] = [],
  ): Promise<any> {
    if (!process.env.CI) {
      console.info('Updating CCD case id %s %s with event %s...', caseId, caseTypeLocation, eventId);
    }

    const authToken = await getUserAuthToken(userName, password);
    const userId = await getUserId(authToken, userName);
    const serviceToken = await getServiceToken();

    const ccdStartEventPath = `caseworkers/${userId}/jurisdictions/EMPLOYMENT/case-types/${caseTypeLocation.toString()}/cases/${caseId}/event-triggers/${eventId}/token`;
    const ccdSaveEventPath = `caseworkers/${userId}/jurisdictions/EMPLOYMENT/case-types/${caseTypeLocation.toString()}/cases/${caseId}/events`;

    const eventToken = await this.getStartEventToken(ccdStartEventPath, authToken, serviceToken);

    const content = dataLocation ? readFileSync(path.resolve(dataLocation), 'utf-8') : '{}';
    const rawData = JSON.parse(content);

    // Apply the key-based mutations
    this.makeModifications(replacements, rawData);

    const payload = {
      data: rawData,
      event: {
        id: eventId,
        summary: 'Updating Case',
        description: 'For CCD E2E Test',
      },
      event_token: eventToken,
    };

    const saveCaseResponse = await this.saveCase(ccdSaveEventPath, authToken, serviceToken, payload);
    if (!process.env.CI) {
      console.info('Updated case with id %s and event %s', caseId, eventId);
    }
    return saveCaseResponse?.data;
  }

  /**
   * Applies modifications to a JSON object based on the provided actions.
   *
   * @param dataModifications - An array of actions to modify the JSON object.
   * @param data - The JSON object to modify.
   *
   * Supported actions:
   *   - 'delete': Removes the property at the specified key.
   *   - 'insert': Sets the property at the specified key to the given value.
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

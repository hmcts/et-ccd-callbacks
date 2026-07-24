import { ReplacementAction } from '../../types/replacement-action.ts';
import { ccdApi } from '../../fixtures/common.fixture.ts';
import { CaseTypeLocation, Events, PayloadPath } from '../../config/case-data.ts';
import DateUtilComponent from '../DateUtilComponent.ts';
import { users } from '../../config/config.dynamic.ts';

export class CaseEventApi {
  /**
   * Updates a case for a caseworker by performing a sequence of steps (events) on the case.
   * Each step can specify an event, an optional payload, and optional replacement actions.
   *
   * @param caseId - The ID of the case to update.
   * @param caseTypeLocation - The case type location (e.g., EnglandAndWales, Scotland).
   * @param steps - An array of steps, each containing an event, optional payload, and optional replacements.
   * @returns {Promise<any>} The response from the last update step.
   * @private
   */
  private static async updateCaseWorkerSteps(
    caseId: string,
    caseTypeLocation: CaseTypeLocation,
    steps: { event: string; payload?: string; replacements?: ReplacementAction[] }[],
  ): Promise<any> {
    let response;
    for (const step of steps) {
      response = await ccdApi.updateCaseInCcd(
        users.etCaseWorker.email,
        users.etCaseWorker.password,
        caseId,
        caseTypeLocation,
        step.event,
        step.payload || '',
        step.replacements || [],
      );
    }
    return response;
  }

  /**
   * Updates a case for a legal representative by performing a sequence of steps (events) on the case.
   * Each step can specify an event, an optional payload, and optional replacement actions.
   *
   * @param caseId - The ID of the case to update.
   * @param caseTypeLocation - The case type location (e.g., EnglandAndWales, Scotland).
   * @param steps - An array of steps, each containing an event, optional payload, and optional replacements.
   * @returns {Promise<any>} The response from the last update step.
   * @private
   */
  private static async updateLegalRepSteps(
    caseId: string,
    caseTypeLocation: CaseTypeLocation,
    steps: { event: string; payload?: string; replacements?: ReplacementAction[] }[],
  ): Promise<any> {
    let response;
    for (const step of steps) {
      response = await ccdApi.updateCaseInCcd(
        users.etLegalRepresentative.email,
        users.etLegalRepresentative.password,
        caseId,
        caseTypeLocation,
        step.event,
        step.payload || '',
        step.replacements || [],
      );
    }
    return response;
  }

  /**
   * Performs vetting and acceptance of an England and Wales case as a caseworker.
   * This includes running the ET1 vetting event and then accepting the case with the current date.
   *
   * @param caseId - The ID of the case to process.
   * @returns {Promise<any>} The response from the last update step.
   */
  static async caseWorkerDoesEt1VettingAndAcceptCaseEngland(
    caseId: string,
  ): Promise<{ caseId: string; caseNumber: string }> {
    const response = await this.updateCaseWorkerSteps(caseId, CaseTypeLocation.EnglandAndWales, [
      { event: Events.et1Vetting.ccdCallback, payload: PayloadPath.events.et1vetting },
      {
        event: Events.acceptRejectCase.ccdCallback,
        payload: PayloadPath.events.acceptCase,
        replacements: [
          { action: 'delete', key: 'preAcceptCase.dateAccepted' },
          { action: 'insert', key: 'preAcceptCase.dateAccepted', value: DateUtilComponent.getCurrentDate() },
        ],
      },
    ]);
    const caseNumber = response.case_data.ethosCaseReference.toString();
    return { caseId, caseNumber };
  }

  /**
   * Performs vetting and acceptance of a Scotland case as a caseworker.
   * This includes running the ET1 vetting event and then accepting the case with the current date.
   *
   * @param caseId - The ID of the case to process.
   * @returns {Promise<any>} The response from the last update step.
   */
  static async caseWorkerDoesEt1VettingAndAcceptCaseScotland(
    caseId: string,
  ): Promise<{ caseId: string; caseNumber: string }> {
    const response = await this.updateCaseWorkerSteps(caseId, CaseTypeLocation.Scotland, [
      { event: Events.et1Vetting.ccdCallback, payload: PayloadPath.events.et1vetting },
      {
        event: Events.acceptRejectCase.ccdCallback,
        payload: PayloadPath.events.acceptCase,
        replacements: [
          { action: 'delete', key: 'preAcceptCase.dateAccepted' },
          { action: 'insert', key: 'preAcceptCase.dateAccepted', value: DateUtilComponent.getCurrentDate() },
        ],
      },
    ]);
    const caseNumber = response.case_data.ethosCaseReference.toString();
    return { caseId, caseNumber };
  }

  static async legalRepProgressToSubmission(
    caseId: string,
    caseTypeLocation: CaseTypeLocation,
  ): Promise<{ caseId: string; caseNumber: string }> {
    const response = await this.updateLegalRepSteps(caseId, caseTypeLocation, [
      { event: Events.et1SectionOneClaimantDetails.ccdCallback, payload: PayloadPath.LegalRep.et1section1 },
      { event: Events.et1SectionTwoRespondentDetails.ccdCallback, payload: PayloadPath.LegalRep.et1Section2 },
      { event: Events.et1SectionThreeClaimDetails.ccdCallback, payload: PayloadPath.LegalRep.et1Section3 },
      { event: Events.et1SubmitClaim.ccdCallback, payload: PayloadPath.LegalRep.et1SubmitClaim },
    ]);
    let caseNumber = response.case_data.ethosCaseReference.toString();
    return { caseId, caseNumber };
  }
}

import { CaseDataBuilder } from '../CaseDataBuilder.ts';
import { CaseTypeLocation, Events, PayloadPath } from '../../../config/case-data.ts';
import { CaseEventApi } from '../../api/CaseEventApi.ts';
import { ReplacementAction } from '../../../types/replacement-action.ts';

export class CaseworkerCaseFactory {

  private static replacements: ReplacementAction[] = [
    { action: 'delete', key: 'claimantIndType.claimant_first_names' },
    { action: 'insert', key: 'claimantIndType.claimant_first_names', value: 'Grayson' },
    { action: 'delete', key: 'claimantIndType.claimant_last_name' },
    { action: 'insert', key: 'claimantIndType.claimant_last_name', value: 'Becker' },
    { action: 'delete', key: 'respondentCollection[0].value.respondent_name' },
    { action: 'insert', key: 'respondentCollection[0].value.respondent_name', value: 'Mrs Test Auto' },
  ];

  /**
   * Creates a new England and Wales case with default replacements for claimant and respondent details.
   * @returns {Promise<any>} The created case data.
   */
  static async createEnglandWalesCase() {
    return await new CaseDataBuilder(CaseTypeLocation.EnglandAndWales, Events.createCase.ccdCallback)
      .withCaseWorkerUser()
      .withPayload(PayloadPath.CaseWorker.england)
      .addReplacements(...this.replacements)
      .create();
  }

  /**
   * Creates a new Scotland case with default replacements for claimant and respondent details.
   * @returns {Promise<any>} The created case data.
   */
  static async createScotlandCase() {
    return await new CaseDataBuilder(CaseTypeLocation.Scotland, Events.createCase.ccdCallback)
      .withCaseWorkerUser()
      .withPayload(PayloadPath.CaseWorker.scotland)
      .addReplacements(...this.replacements)
      .create();
  }

  /**
   * Creates a new England and Wales case, then performs vetting and acceptance for the caseworker.
   * @returns {Promise<{ caseId: string, caseNumber: string }>} The case id and case number.
   */
  static async createEnglandAndAcceptCase() {
    const response= await this.createEnglandWalesCase();
    const caseId = response.id.toString();
    const caseNumber = response.case_data.ethosCaseReference.toString();
    await CaseEventApi.caseWorkerDoesEt1VettingAndAcceptCaseEngland(caseId);
    return { caseId, caseNumber };
  }

  /**
   * Creates a new Scotland case, then performs vetting and acceptance for the caseworker.
   * @returns {Promise<{ caseId: string, caseNumber: string }>} The case id and case number.
   */
  static async createScotlandAndAcceptCase() {
    const response = await this.createScotlandCase();
    const caseId = response.id.toString();
    const caseNumber = response.case_data.ethosCaseReference.toString();
    await CaseEventApi.caseWorkerDoesEt1VettingAndAcceptCaseScotland(caseId);
    return { caseId, caseNumber };
  }


}

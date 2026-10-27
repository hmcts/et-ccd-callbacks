import { CaseDataBuilder } from '../CaseDataBuilder.js';
import { CaseTypeLocation, Events, PayloadPath } from '../../../config/index.js';
import { CaseEventApi } from '../../api/CaseEventApi.js';

export class LegalRepCaseFactory {
  static async createAndProgressToSubmitEnglandWalesCase(): Promise<{ caseId: string; caseNumber: string }> {
    const response = await new CaseDataBuilder(CaseTypeLocation.EnglandAndWales, Events.createLegalRepCase.ccdCallback)
      .withLegalRepUser()
      .withPayload(PayloadPath.LegalRep.createEnglandWalesCase)
      .create();
    return await CaseEventApi.legalRepProgressToSubmission(response.id, CaseTypeLocation.EnglandAndWales);
  }
}

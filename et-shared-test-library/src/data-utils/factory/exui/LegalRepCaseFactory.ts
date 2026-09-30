import { CaseDataBuilder } from '../CaseDataBuilder';
import { CaseTypeLocation, Events, PayloadPath } from '../../../config/case-data';
import { CaseEventApi } from '../../api/CaseEventApi';

export class LegalRepCaseFactory {
  static async createAndProgressToSubmitEnglandWalesCase(): Promise<{ caseId: string; caseNumber: string }> {
    const response = await new CaseDataBuilder(CaseTypeLocation.EnglandAndWales, Events.createLegalRepCase.ccdCallback)
      .withLegalRepUser()
      .withPayload(PayloadPath.LegalRep.createEnglandWalesCase)
      .create();
    return await CaseEventApi.legalRepProgressToSubmission(response.id, CaseTypeLocation.EnglandAndWales);
  }
}

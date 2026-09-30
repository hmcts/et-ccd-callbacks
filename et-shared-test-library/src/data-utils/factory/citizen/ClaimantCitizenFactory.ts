import { cuiApi } from '../../../fixtures/common.fixture.ts';
import { CaseTypeLocation } from '../../../config/case-data.ts';
import { UserCredentials, users } from '../../../config/config.dynamic.ts';

/**
 * Factory class for creating and progressing Citizen Claimant cases via CUI APIs.
 * Provides utility methods to create, update, submit, and progress cases for test automation.
 */
export class CitizenClaimantFactory {

  static async createAndUpdateClaim(caseTypeLocation: CaseTypeLocation, multipleRespondent: boolean = false ,user: UserCredentials = users.etClaimant): Promise<string> {
    const case_id = await cuiApi.initiateCuiCase(user.email, user.password, caseTypeLocation);
    console.log('case Id is:' + case_id);

    // Update and submit the draft case
    const updateResponse = await cuiApi.updateDraftCuiCase(
      user.email,
      user.password,
      case_id,
      CaseTypeLocation.EnglandAndWales, multipleRespondent
    );
    console.log('CUI case updated successfully:' + updateResponse.id);
    return case_id;
  }

  /**
   * Creates a draft CUI case for a claimant, updates it, and submits it.
   *
   * @param caseTypeLocation - The jurisdiction for the case (e.g., EnglandAndWales).
   * @returns Promise resolving to the created case ID as a string.
   *
   * Steps:
   * 1. Initiates a draft CUI case for the claimant.
   * 2. Updates the draft case with required data.
   * 3. Submits the draft case for processing.
   *
   * Example usage:
   *   const caseId = await CitizenClaimantFactory.createAndSubmitClaim(CaseJurisdiction.EnglandAndWales);
   */
  static async createAndSubmitClaim(caseTypeLocation: CaseTypeLocation, multipleRespondent: boolean = false, user: UserCredentials = users.etClaimant): Promise<string> {
    // Create and update a draft case
    const case_id = await this.createAndUpdateClaim(caseTypeLocation, multipleRespondent, user);

    const submitResponse = await cuiApi.submitDraftCuiCase(
      user.email,
      user.password,
      case_id,
      CaseTypeLocation.EnglandAndWales, multipleRespondent
    );
    console.log('CUI case submitted successfully:' + submitResponse.id);
    return case_id;
  }

  /**
   * Progresses a CUI case from creation to ET3 submission for a respondent.
   *
   * @param caseTypeLocation - The jurisdiction for the case (e.g., EnglandAndWales).
   * @returns Promise resolving to the case ID as a string after ET3 submission.
   *
   * Steps:
   * 1. Creates and submits a claimant case.
   * 2. Vets and accepts the case as a caseworker.
   * 3. Assigns the case to a respondent.
   * 4. Submits the ET3 response for the respondent.
   *
   * Example usage:
   *   const caseId = await CitizenClaimantFactory.progressCaseFromCreateToEt3(CaseJurisdiction.EnglandAndWales);
   */
  static async progressCaseFromCreateToEt3(caseTypeLocation: CaseTypeLocation, multipleRespondent: boolean = false, user: UserCredentials = users.etClaimant): Promise<string> {
    const caseId = await this.createAndSubmitClaim(caseTypeLocation, multipleRespondent, user);
    await cuiApi.vetAndAcceptCuiCase(users.etApiUser.email, users.etApiUser.password, caseId);
    const respondentCcdId = await cuiApi.assignCaseToRespondent(
      users.etRespondent.email,
      users.etRespondent.password,
      caseId,
    );
    await cuiApi.submitET3(
      users.etRespondent.email,
      users.etRespondent.password,
      respondentCcdId,
      caseId,
      caseTypeLocation,
    );
    console.log('et3 completed successfully');
    return caseId;
  }

  static async getCaseDataForCaseWorker(case_id: string) {
    return await cuiApi.getCaseData(users.etApiUser.email, users.etApiUser.password, case_id);
  }
}

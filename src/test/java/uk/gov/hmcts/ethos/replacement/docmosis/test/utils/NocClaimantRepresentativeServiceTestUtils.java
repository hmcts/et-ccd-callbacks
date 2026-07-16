package uk.gov.hmcts.ethos.replacement.docmosis.test.utils;

import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CallbackRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignment;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignmentData;
import uk.gov.hmcts.et.common.model.ccd.types.ChangeOrganisationRequest;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantType;
import uk.gov.hmcts.et.common.model.ccd.types.Organisation;
import uk.gov.hmcts.et.common.model.ccd.types.OrganisationPolicy;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC;

import java.util.ArrayList;
import java.util.List;

public final class NocClaimantRepresentativeServiceTestUtils {

    private static final String CLAIMANT_SOLICITOR = "[CLAIMANTSOLICITOR]";
    private static final String ORGANISATION_ID_NEW = "ORG3_NEW";
    private static final String ORGANISATION_ID_OLD = "ORG_OLD";
    private static final String ET_ORG_1 = "ET Org 1";
    private static final String ET_ORG_2 = "ET Org 2";
    private static final String DUMMY_CLAIMANT_FIRST_NAME = "John";
    private static final String DUMMY_CLAIMANT_LAST_NAME = "Doe";
    private static final String NAME_OF_REPRESENTATIVE_1 = "John Brown";
    private static final String NAME_OF_REPRESENTATIVE_2 = "James Brown";
    public static final String DUMMY_CASE_ID = "1234567890123456";
    private static final String ENGLAND_WALES_CASE_TYPE_ID = "ET_EnglandWales";
    private static final String EMPLOYMENT_JURISDICTION = "EMPLOYMENT";
    public static final String REPRESENTATIVE_EMAIL_1 = "claimantrep@test.com";
    private static final String REPRESENTATIVE_EMAIL_2 = "james@test.com";
    private static final String USER_ID_ONE = "USER_ID_ONE";
    private static final String USER_ID_TWO = "USER_ID_TWO";
    private static final String CASE_ID_ONE = "CASE_ID_ONE";
    private static final String CLAIMANT = "claimant";
    private static final String ETHOS_CASE_REFERENCE = "caseRef";

    private NocClaimantRepresentativeServiceTestUtils() {
        // Utility classes should not have a public or default constructor.
    }

    public static CallbackRequest getCallBackCallbackRequest() {
        CallbackRequest callbackRequest = new CallbackRequest();
        CaseDetails caseDetailsBefore = new CaseDetails();
        caseDetailsBefore.setCaseData(getCaseDataBefore());
        callbackRequest.setCaseDetailsBefore(caseDetailsBefore);
        CaseDetails caseDetailsAfter = new CaseDetails();
        caseDetailsAfter.setCaseId(DUMMY_CASE_ID);
        caseDetailsAfter.setCaseTypeId(ENGLAND_WALES_CASE_TYPE_ID);
        caseDetailsAfter.setJurisdiction(EMPLOYMENT_JURISDICTION);
        caseDetailsAfter.setCaseData(getCaseDataAfter());
        callbackRequest.setCaseDetails(caseDetailsAfter);
        return callbackRequest;
    }

    public static CCDRequest getCCDRequest() {
        CCDRequest ccdRequest = new CCDRequest();
        CaseDetails caseDetailsAfter = new CaseDetails();
        caseDetailsAfter.setCaseData(getCaseDataAfter());
        ccdRequest.setCaseDetails(caseDetailsAfter);
        return ccdRequest;
    }

    public static CaseData getCaseDataAfter() {
        CaseData caseDataAfter = new CaseData();
        caseDataAfter.setRespondentCollection(new ArrayList<>());

        //Organisation
        Organisation org1 =
                Organisation.builder().organisationID(ORGANISATION_ID_NEW).organisationName(ET_ORG_2).build();
        OrganisationPolicy orgPolicy1 =
                OrganisationPolicy.builder().organisation(org1).orgPolicyCaseAssignedRole(CLAIMANT_SOLICITOR).build();
        caseDataAfter.setRespondentOrganisationPolicy0(orgPolicy1);

        // Claimant Representative
        RepresentedTypeC representedTypeC = new RepresentedTypeC();
        representedTypeC.setNameOfRepresentative(NAME_OF_REPRESENTATIVE_1);
        representedTypeC.setRepresentativeEmailAddress(REPRESENTATIVE_EMAIL_1);
        representedTypeC.setMyHmctsOrganisation(org1);
        caseDataAfter.setRepresentativeClaimantType(representedTypeC);

        caseDataAfter.setChangeOrganisationRequestField(createChangeOrganisationRequest());
        return caseDataAfter;
    }

    public static CaseData getCaseDataBefore() {
        CaseData caseDataBefore = new CaseData();

        caseDataBefore.setRespondentCollection(new ArrayList<>());
        caseDataBefore.setClaimant(CLAIMANT);
        caseDataBefore.setEthosCaseReference(ETHOS_CASE_REFERENCE);

        //Organisation
        Organisation org1 =
                Organisation.builder().organisationID(ORGANISATION_ID_OLD).organisationName(ET_ORG_1).build();
        OrganisationPolicy orgPolicy1 =
                OrganisationPolicy.builder().organisation(org1).orgPolicyCaseAssignedRole(CLAIMANT_SOLICITOR).build();
        caseDataBefore.setRespondentOrganisationPolicy0(orgPolicy1);

        // Claimant Representative
        RepresentedTypeC representedTypeC = new RepresentedTypeC();
        representedTypeC.setNameOfRepresentative(NAME_OF_REPRESENTATIVE_2);
        representedTypeC.setRepresentativeEmailAddress(REPRESENTATIVE_EMAIL_2);
        representedTypeC.setMyHmctsOrganisation(org1);
        caseDataBefore.setRepresentativeClaimantType(representedTypeC);

        return caseDataBefore;
    }

    public static CaseUserAssignmentData mockCaseAssignmentData() {
        List<CaseUserAssignment> caseUserAssignments = List.of(CaseUserAssignment.builder().userId(USER_ID_ONE)
                        .organisationId(ET_ORG_1)
                        .caseRole(CLAIMANT_SOLICITOR)
                        .caseId(CASE_ID_ONE)
                        .build(),
                CaseUserAssignment.builder().userId(USER_ID_TWO)
                        .organisationId(ET_ORG_2)
                        .caseRole(CLAIMANT_SOLICITOR)
                        .caseId(CASE_ID_ONE)
                        .build());

        return CaseUserAssignmentData.builder().caseUserAssignments(caseUserAssignments).build();
    }

    public static ChangeOrganisationRequest createChangeOrganisationRequest() {
        DynamicFixedListType caseRole = new DynamicFixedListType();
        DynamicValueType dynamicValueType = new DynamicValueType();
        dynamicValueType.setCode(CLAIMANT_SOLICITOR);
        dynamicValueType.setLabel(CLAIMANT_SOLICITOR);
        caseRole.setValue(dynamicValueType);

        Organisation organisationToRemove =
                Organisation.builder().organisationID(ORGANISATION_ID_OLD).organisationName(ET_ORG_1).build();

        Organisation organisationToAdd =
                Organisation.builder().organisationID(ORGANISATION_ID_NEW).organisationName(ET_ORG_2).build();

        return ChangeOrganisationRequest.builder()
                .organisationToAdd(organisationToAdd)
                .organisationToRemove(organisationToRemove)
                .caseRoleId(caseRole)
                .build();
    }

    public static CaseData createCaseData() {
        CaseData caseData = new CaseData();
        ClaimantType claimantType = new ClaimantType();
        caseData.setClaimantType(claimantType);
        caseData.setClaimantFirstName(DUMMY_CLAIMANT_FIRST_NAME);
        caseData.setClaimantLastName(DUMMY_CLAIMANT_LAST_NAME);
        return caseData;
    }

    public static CaseDetails createCaseDetailsWithCaseData(CaseData caseData) {
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseData(caseData);
        caseDetails.setCaseId(DUMMY_CASE_ID);
        return caseDetails;
    }
}

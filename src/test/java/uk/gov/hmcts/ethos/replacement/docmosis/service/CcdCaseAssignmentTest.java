package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CallbackRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.Organisation;
import uk.gov.hmcts.et.common.model.ccd.types.OrganisationPolicy;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseConverter;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import javax.swing.*;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@TestPropertySource(properties = {
       "apply_noc_access_api_assignments_path=/noc/apply-decision",
        "assign_case_access_api_url=http://localhost:4454"
})
class CcdCaseAssignmentTest {

    @MockBean
    private RestTemplate restTemplate;

    @MockBean
    private AuthTokenGenerator serviceAuthTokenGenerator;

    @InjectMocks
    private CcdCaseAssignment ccdCaseAssignment;

    private static final String RESPONDENT_NAME = "Harry Johnson";
    private static final String RESPONDENT_NAME_TWO = "Jane Green";
    private static final String RESPONDENT_NAME_THREE = "Bad Company Inc";
    private static final String RESPONDENT_REF = "7277";
    private static final String RESPONDENT_REF_TWO = "6887";
    private static final String RESPONDENT_REF_THREE = "9292";
    private static final String RESPONDENT_EMAIL = "h.johnson@corp.co.uk";
    private static final String RESPONDENT_EMAIL_TWO = "j.green@corp.co.uk";
    private static final String RESPONDENT_EMAIL_THREE = "info@corp.co.uk";
    private static final String RESPONDENT_REP_ID = "1111-2222-3333-1111";
    private static final String RESPONDENT_REP_ID_TWO = "1111-2222-3333-1112";
    private static final String RESPONDENT_REP_ID_THREE = "1111-2222-3333-1113";
    private static final String RESPONDENT_REP_NAME = "Legal One";
    private static final String RESPONDENT_REP_NAME_TWO = "Legal Two";
    private static final String RESPONDENT_REP_NAME_THREE = "Legal Three";
    private static final String SOLICITORA = "[SOLICITORA]";
    private static final String SOLICITORB = "[SOLICITORB]";
    private static final String SOLICITORC = "[SOLICITORC]";
    private static final String ORGANISATION_ID = "ORG1";
    private static final String ORGANISATION_ID_TWO = "ORG2";
    private static final String ORGANISATION_ID_THREE = "ORG3";
    private static final String ORGANISATION_ID_NEW = "ORG_NEW";
    public static final String ET_ORG_1 = "ET Org 1";
    private static final String ET_ORG_2 = "ET Org 2";
    private static final String ET_ORG_3 = "ET Org 3";
    private static final String ET_ORG_NEW = "ET Org New";
    private static final String USER_EMAIL = "test@hmcts.net";
    private static final String USER_FIRST_NAME = "John";
    private static final String USER_LAST_NAME = "Brown";
    private static final String USER_FULL_NAME = "John Brown";
    private CallbackRequest callbackRequest;

    @BeforeEach
    void setUp() {
        CaseData caseData = new CaseData();

        // Respondent
        caseData.setRespondentCollection(new ArrayList<>());

        RespondentSumTypeItem respondentSumTypeItem = new RespondentSumTypeItem();
        respondentSumTypeItem.setValue(RespondentSumType.builder().respondentName(RESPONDENT_NAME)
                .respondentEmail(RESPONDENT_EMAIL)
                .responseReference(RESPONDENT_REF)
                .build());
        caseData.getRespondentCollection().add(respondentSumTypeItem);

        respondentSumTypeItem = new RespondentSumTypeItem();
        respondentSumTypeItem.setValue(RespondentSumType.builder().respondentName(RESPONDENT_NAME_TWO)
                .respondentEmail(RESPONDENT_EMAIL_TWO)
                .responseReference(RESPONDENT_REF_TWO)
                .build());
        caseData.getRespondentCollection().add(respondentSumTypeItem);

        respondentSumTypeItem = new RespondentSumTypeItem();
        respondentSumTypeItem.setValue(RespondentSumType.builder().respondentName(RESPONDENT_NAME_THREE)
                .respondentEmail(RESPONDENT_EMAIL_THREE)
                .responseReference(RESPONDENT_REF_THREE)
                .build());
        caseData.getRespondentCollection().add(respondentSumTypeItem);

        //Organisation
        Organisation org1 =
                Organisation.builder().organisationID(ORGANISATION_ID).organisationName(ET_ORG_1).build();
        OrganisationPolicy orgPolicy1 =
                OrganisationPolicy.builder().organisation(org1).orgPolicyCaseAssignedRole(SOLICITORA).build();
        Organisation org2 =
                Organisation.builder().organisationID(ORGANISATION_ID_TWO).organisationName(ET_ORG_2).build();
        OrganisationPolicy orgPolicy2 =
                OrganisationPolicy.builder().organisation(org2).orgPolicyCaseAssignedRole(SOLICITORB).build();
        Organisation org3 =
                Organisation.builder().organisationID(ORGANISATION_ID_THREE).organisationName(ET_ORG_3).build();
        OrganisationPolicy orgPolicy3 =
                OrganisationPolicy.builder().organisation(org3).orgPolicyCaseAssignedRole(SOLICITORC).build();

        caseData.setRespondentOrganisationPolicy0(orgPolicy1);
        caseData.setRespondentOrganisationPolicy1(orgPolicy2);
        caseData.setRespondentOrganisationPolicy2(orgPolicy3);

        // Respondent Representative
        caseData.setRepCollection(new ArrayList<>());
        RepresentedTypeR representedType =
                RepresentedTypeR.builder()
                        .nameOfRepresentative(RESPONDENT_REP_NAME)
                        .respRepName(RESPONDENT_NAME)
                        .respondentOrganisation(org1).build();
        RepresentedTypeRItem representedTypeRItem = new RepresentedTypeRItem();
        representedTypeRItem.setId(RESPONDENT_REP_ID);
        representedTypeRItem.setValue(representedType);
        caseData.getRepCollection().add(representedTypeRItem);

        representedType =
                RepresentedTypeR.builder()
                        .nameOfRepresentative(RESPONDENT_REP_NAME_TWO)
                        .respRepName(RESPONDENT_NAME_TWO)
                        .respondentOrganisation(org2).build();
        representedTypeRItem = new RepresentedTypeRItem();
        representedTypeRItem.setId(RESPONDENT_REP_ID_TWO);
        representedTypeRItem.setValue(representedType);
        caseData.getRepCollection().add(representedTypeRItem);

        representedType =
                RepresentedTypeR.builder()
                        .nameOfRepresentative(RESPONDENT_REP_NAME_THREE)
                        .respRepName(RESPONDENT_NAME_THREE)
                        .respondentOrganisation(org3).build();
        representedTypeRItem = new RepresentedTypeRItem();
        representedTypeRItem.setId(RESPONDENT_REP_ID_THREE);
        representedTypeRItem.setValue(representedType);
        caseData.getRepCollection().add(representedTypeRItem);
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseData(caseData);
        callbackRequest = new CallbackRequest();
        callbackRequest.setCaseDetails(caseDetails);
    }

    @Test
    void applyNoc() {
        CCDCallbackResponse expected = new CCDCallbackResponse(callbackRequest.getCaseDetails().getCaseData());

        when(restTemplate
                .exchange(any(),
                        any(),
                        any(),
                        eq(CCDCallbackResponse.class))).thenReturn(ResponseEntity.ok(expected));

        when(serviceAuthTokenGenerator.generate()).thenReturn("token");

        CCDCallbackResponse actual = ccdCaseAssignment.applyNoc(callbackRequest,"token");
        assertThat(expected).isEqualTo(actual);

    }
}
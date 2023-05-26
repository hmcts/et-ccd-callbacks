package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.ChangeOrganisationRequest;
import uk.gov.hmcts.et.common.model.ccd.types.Organisation;
import uk.gov.hmcts.et.common.model.ccd.types.OrganisationPolicy;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.SolicitorRole;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static uk.gov.hmcts.et.common.model.ccd.types.ChangeOrganisationApprovalStatus.APPROVED;

class NocRespondentHelperTest {
    private static final String RESPONDENT_NAME = "Harry Johnson";
    private static final String RESPONDENT_NAME_TWO = "Jane Green";
    private static final String RESPONDENT_NAME_THREE = "Bad Company Inc";

    private static final String AMENDED_RESP_NAME = "Horrible Company Inc";
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

    public static final String ET_ORG_1 = "ET Org 1";
    private static final String ET_ORG_2 = "ET Org 2";
    private static final String ET_ORG_3 = "ET Org 3";

    private static final String RESPONDENT_ID_ONE = "106001";
    private static final String RESPONDENT_ID_TWO = "106002";
    private static final String RESPONDENT_ID_THREE = "106003";
    private static final String UNKNOWN_RESP_ID = "999999";
    private static final Map<String, Organisation> EXPECTED_RESPONDENT_ORGANISATIONS =
        Map.of(RESPONDENT_ID_ONE,
            Organisation.builder().organisationID(ORGANISATION_ID_THREE).organisationName(ET_ORG_3).build(),
            RESPONDENT_ID_TWO,
            Organisation.builder().organisationID(ORGANISATION_ID_TWO).organisationName(ET_ORG_2).build(),
            RESPONDENT_ID_THREE,
            Organisation.builder().organisationID(ORGANISATION_ID).organisationName(ET_ORG_1).build());

    private CaseData caseData;
    private Organisation org1;
    private Organisation org2;
    private RespondentSumTypeItem respondentSumTypeItem1;
    private RespondentSumTypeItem respondentSumTypeItem2;
    private RespondentSumTypeItem respondentSumTypeItem3;

    private RespondentSumTypeItem unknownRespondentSumTypeItem;
    private RepresentedTypeRItem respondentRep3;

    private NocRespondentHelper nocRespondentHelper;

    @BeforeEach
    void setUp() {
        nocRespondentHelper = new NocRespondentHelper();
        caseData = new CaseData();

        // Respondent
        caseData.setRespondentCollection(new ArrayList<>());

        respondentSumTypeItem1 = new RespondentSumTypeItem();
        respondentSumTypeItem1.setValue(RespondentSumType.builder().respondentName(RESPONDENT_NAME)
            .respondentEmail(RESPONDENT_EMAIL)
            .responseReference(RESPONDENT_REF)
            .build());
        respondentSumTypeItem1.setId(RESPONDENT_ID_ONE);
        caseData.getRespondentCollection().add(respondentSumTypeItem1);

        respondentSumTypeItem2 = new RespondentSumTypeItem();
        respondentSumTypeItem2.setValue(RespondentSumType.builder().respondentName(RESPONDENT_NAME_TWO)
            .respondentEmail(RESPONDENT_EMAIL_TWO)
            .responseReference(RESPONDENT_REF_TWO)
            .build());
        respondentSumTypeItem2.setId(RESPONDENT_ID_TWO);
        caseData.getRespondentCollection().add(respondentSumTypeItem2);

        respondentSumTypeItem3 = new RespondentSumTypeItem();
        respondentSumTypeItem3.setValue(RespondentSumType.builder().respondentName(RESPONDENT_NAME_THREE)
            .respondentEmail(RESPONDENT_EMAIL_THREE)
            .responseReference(RESPONDENT_REF_THREE)
            .build());
        respondentSumTypeItem3.setId(RESPONDENT_ID_THREE);
        caseData.getRespondentCollection().add(respondentSumTypeItem3);

        unknownRespondentSumTypeItem = new RespondentSumTypeItem();
        unknownRespondentSumTypeItem.setId(UNKNOWN_RESP_ID);

        //Organisation
        org1 =
            Organisation.builder().organisationID(ORGANISATION_ID).organisationName(ET_ORG_1).build();
        OrganisationPolicy orgPolicy1 =
            OrganisationPolicy.builder().organisation(org1).orgPolicyCaseAssignedRole(SOLICITORA).build();
        org2 =
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
        RepresentedTypeRItem respondentRep1 = new RepresentedTypeRItem();
        respondentRep1.setId(RESPONDENT_REP_ID);
        respondentRep1.setValue(representedType);
        respondentRep1.getValue().setRespondentId(RESPONDENT_ID_THREE);
        caseData.getRepCollection().add(respondentRep1);

        representedType =
            RepresentedTypeR.builder()
                .nameOfRepresentative(RESPONDENT_REP_NAME_TWO)
                .respRepName(RESPONDENT_NAME_TWO)
                .respondentOrganisation(org2).build();
        RepresentedTypeRItem respondentRep2 = new RepresentedTypeRItem();
        respondentRep2.setId(RESPONDENT_REP_ID_TWO);
        respondentRep2.setValue(representedType);
        respondentRep2.getValue().setRespondentId(RESPONDENT_ID_TWO);
        caseData.getRepCollection().add(respondentRep2);

        representedType =
            RepresentedTypeR.builder()
                .nameOfRepresentative(RESPONDENT_REP_NAME_THREE)
                .respRepName(RESPONDENT_NAME_THREE)
                .respondentOrganisation(org3).build();
        respondentRep3 = new RepresentedTypeRItem();
        respondentRep3.setId(RESPONDENT_REP_ID_THREE);
        respondentRep3.setValue(representedType);
        respondentRep3.getValue().setRespondentId(RESPONDENT_ID_ONE);
        caseData.getRepCollection().add(respondentRep3);
    }

    @Test
    void shouldReturnRespondentRepOrganisations() {
        Map<String, Organisation> respondentOrganisations = nocRespondentHelper.getRespondentOrganisations(caseData);

        assertThat(respondentOrganisations.get(RESPONDENT_ID_ONE))
            .usingRecursiveComparison().isEqualTo(EXPECTED_RESPONDENT_ORGANISATIONS.get(RESPONDENT_ID_ONE));
        assertThat(respondentOrganisations.get(RESPONDENT_ID_TWO))
            .usingRecursiveComparison().isEqualTo(EXPECTED_RESPONDENT_ORGANISATIONS.get(RESPONDENT_ID_TWO));
        assertThat(respondentOrganisations.get(RESPONDENT_ID_THREE))
            .usingRecursiveComparison().isEqualTo(EXPECTED_RESPONDENT_ORGANISATIONS.get(RESPONDENT_ID_THREE));
    }

    @Test
    void shouldReturnOrganisationOfRep() {
        Organisation orgFromRep = nocRespondentHelper.getOrgFromRep(respondentSumTypeItem3,
                caseData.getRepCollection());
        assertThat(orgFromRep).isEqualTo(org1);
    }

    @Test
    void shouldReturnIndexOfRep() {
        assertThat(nocRespondentHelper.getIndexOfRep(respondentSumTypeItem1,
            caseData.getRepCollection())).isEqualTo(2);
        assertThat(nocRespondentHelper.getIndexOfRep(respondentSumTypeItem2,
            caseData.getRepCollection())).isEqualTo(1);
        assertThat(nocRespondentHelper.getIndexOfRep(respondentSumTypeItem3,
            caseData.getRepCollection())).isZero();
        assertThat(nocRespondentHelper.getIndexOfRep(unknownRespondentSumTypeItem,
            caseData.getRepCollection())).isEqualTo(-1);
    }

    @Test
    void shouldReturnRespondentRep() {
        Optional<RepresentedTypeRItem> respondentRep =
            nocRespondentHelper.getRespondentRep(respondentSumTypeItem1, caseData.getRepCollection());
        assertThat(respondentRep).isPresent().hasValue(respondentRep3);
    }

    @Test
    void shouldCreateChangeRequest() {
        ChangeOrganisationRequest changeRequest =
            nocRespondentHelper.createChangeRequest(org1, org2, SolicitorRole.SOLICITORD);
        assertThat(changeRequest).usingRecursiveComparison()
            .ignoringFields("requestTimestamp")
            .isEqualTo(expectedChangeRequest());
    }

    private ChangeOrganisationRequest expectedChangeRequest() {
        SolicitorRole role = SolicitorRole.SOLICITORD;
        DynamicFixedListType roleItem = new DynamicFixedListType();
        DynamicValueType dynamicValueType = new DynamicValueType();
        dynamicValueType.setCode(role.getCaseRoleLabel());
        dynamicValueType.setLabel(role.getCaseRoleLabel());
        roleItem.setValue(dynamicValueType);

        return ChangeOrganisationRequest.builder()
            .approvalStatus(APPROVED)
            .requestTimestamp(LocalDateTime.now())
            .caseRoleId(roleItem)
            .organisationToRemove(org2)
            .organisationToAdd(org1)
            .build();
    }

    @Test
    void shouldReturnRespondent() {
        RespondentSumType respondent = nocRespondentHelper.getRespondent(RESPONDENT_NAME_THREE, caseData);
        Assertions.assertThat(respondent.getResponseReference()).isEqualTo(RESPONDENT_REF_THREE);
    }

    @Test
    void amendRespondentNameRepresentativeNames() {
        RespondentSumTypeItem amendedRespondent = caseData.getRespondentCollection().get(2);
        amendedRespondent.getValue().setRespondentName(AMENDED_RESP_NAME);
        nocRespondentHelper.amendRespondentNameRepresentativeNames(caseData);

        RepresentedTypeR rep = caseData.getRepCollection().get(0).getValue();
        assertThat(rep.getDynamicRespRepName().getSelectedCode()).isEqualTo("R: " + AMENDED_RESP_NAME);
        assertThat(rep.getDynamicRespRepName().getSelectedLabel()).isEqualTo(AMENDED_RESP_NAME);
        assertThat(rep.getRespRepName()).isEqualTo(AMENDED_RESP_NAME);

        rep = caseData.getRepCollection().get(1).getValue();
        assertThat(rep.getDynamicRespRepName().getSelectedCode()).isEqualTo("R: " + RESPONDENT_NAME_TWO);
        assertThat(rep.getDynamicRespRepName().getSelectedLabel()).isEqualTo(RESPONDENT_NAME_TWO);
        assertThat(rep.getRespRepName()).isEqualTo(RESPONDENT_NAME_TWO);

        rep = caseData.getRepCollection().get(2).getValue();
        assertThat(rep.getDynamicRespRepName().getSelectedCode()).isEqualTo("R: " + RESPONDENT_NAME);
        assertThat(rep.getDynamicRespRepName().getSelectedLabel()).isEqualTo(RESPONDENT_NAME);
        assertThat(rep.getRespRepName()).isEqualTo(RESPONDENT_NAME);
    }

    @Test
    void generateNewRepDetails() {
        UserDetails userDetails = new UserDetails();
        userDetails.setLastName("Smith");
        userDetails.setFirstName("John");
        userDetails.setEmail("j.smith@solicitors.com");
        RepresentedTypeR representedTypeR =
            nocRespondentHelper.generateNewRepDetails(expectedChangeRequest(), Optional.of(userDetails),
                respondentSumTypeItem2);
        assertThat(representedTypeR).isEqualTo(expectedNewRep());
    }

    private RepresentedTypeR expectedNewRep() {
        return RepresentedTypeR.builder()
            .nameOfRepresentative("John Smith")
            .representativeEmailAddress("j.smith@solicitors.com")
            .respondentOrganisation(org1)
            .respRepName(RESPONDENT_NAME_TWO)
            .respondentId(RESPONDENT_ID_TWO)
            .myHmctsYesNo("Yes")
            .build();
    }
}
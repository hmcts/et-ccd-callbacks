package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.NoticeOfChangeAnswers;
import uk.gov.hmcts.et.common.model.ccd.types.OrganisationPolicy;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.SolicitorRole;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class NoticeOfChangeFieldPopulatorTest {
    private CaseData caseData;
    private static final OrganisationPolicy ORG_POLICY_A = mock(OrganisationPolicy.class);
    private static final OrganisationPolicy ORG_POLICY_B = mock(OrganisationPolicy.class);
    private static final OrganisationPolicy ORG_POLICY_C = mock(OrganisationPolicy.class);
    private static final OrganisationPolicy ORG_POLICY_D = mock(OrganisationPolicy.class);
    private static final OrganisationPolicy ORG_POLICY_E = mock(OrganisationPolicy.class);
    private static final OrganisationPolicy ORG_POLICY_F = mock(OrganisationPolicy.class);
    private static final OrganisationPolicy ORG_POLICY_G = mock(OrganisationPolicy.class);
    private static final OrganisationPolicy ORG_POLICY_H = mock(OrganisationPolicy.class);
    private static final OrganisationPolicy ORG_POLICY_I = mock(OrganisationPolicy.class);
    private static final OrganisationPolicy ORG_POLICY_J = mock(OrganisationPolicy.class);
    private static final NoticeOfChangeAnswers ANSWERS_1 = mock(NoticeOfChangeAnswers.class);
    private static final NoticeOfChangeAnswers ANSWERS_2 = mock(NoticeOfChangeAnswers.class);
    private static final NoticeOfChangeAnswers ANSWERS_3 = mock(NoticeOfChangeAnswers.class);
    private RepresentedTypeRItem respondentRep1;
    private RepresentedTypeRItem respondentRep2;
    private RepresentedTypeRItem respondentRep3;

    private RespondentSumTypeItem respondent1;
    private RespondentSumTypeItem respondent2;
    private RespondentSumTypeItem respondent3;

    private static final String RESPONDENT_ID_ONE = "106001";
    private static final String RESPONDENT_ID_TWO = "106002";
    private static final String RESPONDENT_ID_THREE = "106003";
    private final NoticeOfChangeAnswersConverter answersConverter = mock(NoticeOfChangeAnswersConverter.class);
    private final RespondentPolicyConverter policyConverter = mock(RespondentPolicyConverter.class);
    private final NoticeOfChangeFieldPopulator noticeOfChangeFieldPopulator = new NoticeOfChangeFieldPopulator(
        policyConverter, answersConverter
    );

    @BeforeEach
    void setUp() {
        caseData = new CaseData();
        caseData.setRespondentCollection(new ArrayList<>());

        respondent1 = new RespondentSumTypeItem();
        respondent1.setValue(RespondentSumType.builder()
                       .build());
        respondent1.setId(RESPONDENT_ID_ONE);
        caseData.getRespondentCollection().add(respondent1);

        respondent2 = new RespondentSumTypeItem();
        respondent2.setValue(RespondentSumType.builder()
            .build());
        respondent2.setId(RESPONDENT_ID_TWO);
        caseData.getRespondentCollection().add(respondent2);

        respondent3 = new RespondentSumTypeItem();
        respondent3.setValue(RespondentSumType.builder()
            .build());
        respondent3.setId(RESPONDENT_ID_THREE);
        caseData.getRespondentCollection().add(respondent3);

        caseData.setRepCollection(new ArrayList<>());

        respondentRep1 = new RepresentedTypeRItem();
        respondentRep1.setValue(RepresentedTypeR.builder().build());
        respondentRep1.getValue().setRespondentId(RESPONDENT_ID_ONE);
        caseData.getRepCollection().add(respondentRep1);

        respondentRep2 = new RepresentedTypeRItem();
        respondentRep2.setValue(RepresentedTypeR.builder().build());
        respondentRep2.getValue().setRespondentId(RESPONDENT_ID_TWO);
        caseData.getRepCollection().add(respondentRep2);

        respondentRep3 = new RepresentedTypeRItem();
        respondentRep3.setValue(RepresentedTypeR.builder().build());
        respondentRep3.getValue().setRespondentId(RESPONDENT_ID_THREE);
        caseData.getRepCollection().add(respondentRep3);

    }

    @Test
    void shouldGenerateRespondentOrganisationPolicesAndNocAnswers() {
        when(policyConverter.generate(SolicitorRole.SOLICITORA, Optional.of(respondentRep1))).thenReturn(
            ORG_POLICY_A);
        when(policyConverter.generate(SolicitorRole.SOLICITORB, Optional.of(respondentRep2))).thenReturn(
            ORG_POLICY_B);
        when(policyConverter.generate(SolicitorRole.SOLICITORC, Optional.of(respondentRep3))).thenReturn(
            ORG_POLICY_C);

        when(policyConverter.generate(SolicitorRole.SOLICITORD, Optional.empty())).thenReturn(ORG_POLICY_D);
        when(policyConverter.generate(SolicitorRole.SOLICITORE, Optional.empty())).thenReturn(ORG_POLICY_E);
        when(policyConverter.generate(SolicitorRole.SOLICITORF, Optional.empty())).thenReturn(ORG_POLICY_F);
        when(policyConverter.generate(SolicitorRole.SOLICITORG, Optional.empty())).thenReturn(ORG_POLICY_G);
        when(policyConverter.generate(SolicitorRole.SOLICITORH, Optional.empty())).thenReturn(ORG_POLICY_H);
        when(policyConverter.generate(SolicitorRole.SOLICITORI, Optional.empty())).thenReturn(ORG_POLICY_I);
        when(policyConverter.generate(SolicitorRole.SOLICITORJ, Optional.empty())).thenReturn(ORG_POLICY_J);

        when(answersConverter.generateForSubmission(eq(respondent1), any())).thenReturn(ANSWERS_1);
        when(answersConverter.generateForSubmission(eq(respondent2), any())).thenReturn(ANSWERS_2);
        when(answersConverter.generateForSubmission(eq(respondent3), any())).thenReturn(ANSWERS_3);

        final Map<String, Object> data = noticeOfChangeFieldPopulator.generate(caseData);

        assertThat(data).isEqualTo(Map.ofEntries(
            entry("noticeOfChangeAnswers0", ANSWERS_1),
            entry("noticeOfChangeAnswers1", ANSWERS_2),
            entry("noticeOfChangeAnswers2", ANSWERS_3),
            entry("respondentOrganisationPolicy0", ORG_POLICY_A),
            entry("respondentOrganisationPolicy1", ORG_POLICY_B),
            entry("respondentOrganisationPolicy2", ORG_POLICY_C),
            entry("respondentOrganisationPolicy3", ORG_POLICY_D),
            entry("respondentOrganisationPolicy4", ORG_POLICY_E),
            entry("respondentOrganisationPolicy5", ORG_POLICY_F),
            entry("respondentOrganisationPolicy6", ORG_POLICY_G),
            entry("respondentOrganisationPolicy7", ORG_POLICY_H),
            entry("respondentOrganisationPolicy8", ORG_POLICY_I),
            entry("respondentOrganisationPolicy9", ORG_POLICY_J)
        ));
    }
}
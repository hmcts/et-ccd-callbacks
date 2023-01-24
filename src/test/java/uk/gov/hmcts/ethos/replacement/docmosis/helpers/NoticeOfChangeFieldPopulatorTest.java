package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantIndType;
import uk.gov.hmcts.et.common.model.ccd.types.NoticeOfChangeAnswers;
import uk.gov.hmcts.et.common.model.ccd.types.OrganisationPolicy;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.SolicitorRole;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class NoticeOfChangeFieldPopulatorTest {
    private final CaseData caseData = mock(CaseData.class);
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
    private static final RepresentedTypeRItem RESPONDENT_REP_1 = mock(RepresentedTypeRItem.class);
    private static final RepresentedTypeRItem RESPONDENT_REP_2 = mock(RepresentedTypeRItem.class);
    private static final RepresentedTypeRItem RESPONDENT_REP_3 = mock(RepresentedTypeRItem.class);
    private static final ClaimantIndType CLAIMANT = mock(ClaimantIndType.class);
    private final NoticeOfChangeAnswersConverter answersConverter = mock(NoticeOfChangeAnswersConverter.class);
    private final RespondentPolicyConverter policyConverter = mock(RespondentPolicyConverter.class);
    private final NoticeOfChangeFieldPopulator noticeOfChangeFieldPopulator = new NoticeOfChangeFieldPopulator(
        policyConverter, answersConverter
    );

    @Test
    void shouldGenerateRespondentOrganisationPolicesAndNocAnswers() {
        when(caseData.getRepCollection()).thenReturn(List.of(RESPONDENT_REP_1, RESPONDENT_REP_2, RESPONDENT_REP_3));
        when(caseData.getClaimantIndType()).thenReturn(CLAIMANT);
        
        when(policyConverter.generate(SolicitorRole.SOLICITORA, Optional.of(RESPONDENT_REP_1))).thenReturn(
            ORG_POLICY_A);
        when(policyConverter.generate(SolicitorRole.SOLICITORB, Optional.of(RESPONDENT_REP_2))).thenReturn(
            ORG_POLICY_B);
        when(policyConverter.generate(SolicitorRole.SOLICITORC, Optional.of(RESPONDENT_REP_3))).thenReturn(
            ORG_POLICY_C);

        when(policyConverter.generate(SolicitorRole.SOLICITORD, Optional.empty())).thenReturn(ORG_POLICY_D);
        when(policyConverter.generate(SolicitorRole.SOLICITORE, Optional.empty())).thenReturn(ORG_POLICY_E);
        when(policyConverter.generate(SolicitorRole.SOLICITORF, Optional.empty())).thenReturn(ORG_POLICY_F);
        when(policyConverter.generate(SolicitorRole.SOLICITORG, Optional.empty())).thenReturn(ORG_POLICY_G);
        when(policyConverter.generate(SolicitorRole.SOLICITORH, Optional.empty())).thenReturn(ORG_POLICY_H);
        when(policyConverter.generate(SolicitorRole.SOLICITORI, Optional.empty())).thenReturn(ORG_POLICY_I);
        when(policyConverter.generate(SolicitorRole.SOLICITORJ, Optional.empty())).thenReturn(ORG_POLICY_J);

        when(answersConverter.generateForSubmission(RESPONDENT_REP_1, CLAIMANT)).thenReturn(ANSWERS_1);
        when(answersConverter.generateForSubmission(RESPONDENT_REP_2, CLAIMANT)).thenReturn(ANSWERS_2);
        when(answersConverter.generateForSubmission(RESPONDENT_REP_3, CLAIMANT)).thenReturn(ANSWERS_3);

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
package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.FlagDetailType;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationType;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.ListTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.TseAdminRecordDecisionTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.CaseFlagsType;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.et.common.model.ccd.types.RestrictedReportingType;
import uk.gov.hmcts.et.common.model.ccd.types.TseAdminRecordDecisionType;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.ecm.common.model.helper.CaseFlagConstants.ACTIVE;
import static uk.gov.hmcts.ecm.common.model.helper.CaseFlagConstants.SIGN_LANGUAGE_INTERPRETER;
import static uk.gov.hmcts.ecm.common.model.helper.CaseFlagConstants.VEXATIOUS_LITIGANT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.TSE_APP_RESTRICT_PUBLICITY;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.CaseFlagsService.CLAIMANT;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.CaseFlagsService.EXTERNAL;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.CaseFlagsService.INTERNAL;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.CaseFlagsService.RESPONDENT;

@ExtendWith(SpringExtension.class)
class CaseFlagsServiceTest {
    public static final String CLAIMANT_NAME = "Claimant Name";
    public static final String RESPONDENT_NAME = "Respondent Name";
    private CaseFlagsService caseFlagsService;
    private CaseData caseData;

    @BeforeEach
    void setUp() {
        caseFlagsService = new CaseFlagsService();
        caseData = CaseDataBuilder.builder().build();
        caseData.setClaimant(CLAIMANT_NAME);
        caseData.setRespondent(RESPONDENT_NAME);

        List<RespondentSumTypeItem> respondentCollection = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            RespondentSumTypeItem respondentSumTypeItem = new RespondentSumTypeItem();
            RespondentSumType respondentSumType = new RespondentSumType();
            respondentSumType.setRespondentName(i == 0 ? RESPONDENT_NAME : RESPONDENT_NAME + " " + (i + 1));
            respondentSumTypeItem.setId(UUID.randomUUID().toString());
            respondentSumTypeItem.setValue(respondentSumType);
            respondentCollection.add(respondentSumTypeItem);
        }
        caseData.setRespondentCollection(respondentCollection);
    }

    @Test
    void caseFlagsSetupRequired() {
        assertTrue(caseFlagsService.caseFlagsSetupRequired(caseData));
        caseFlagsService.setupCaseFlags(caseData);
        assertFalse(caseFlagsService.caseFlagsSetupRequired(caseData));
    }

    @Test
    void setupCaseFlags_setsShellCaseFlags() {
        caseFlagsService.setupCaseFlags(caseData);

        CaseFlagsType caseFlags = caseData.getCaseFlags();
        assertThat(caseFlags.getPartyName(), is(nullValue()));
        assertThat(caseFlags.getRoleOnCase(), is(nullValue()));

        CaseFlagsType claimantFlags = caseData.getClaimantFlags();
        assertThat(claimantFlags.getPartyName(), is(CLAIMANT_NAME));
        assertThat(claimantFlags.getRoleOnCase(), is(CLAIMANT));
        assertThat(claimantFlags.getGroupId(), is(CLAIMANT));
        assertThat(claimantFlags.getVisibility(), is(INTERNAL));

        CaseFlagsType respondentFlags = caseData.getRespondentFlags();
        assertThat(respondentFlags.getPartyName(), is(RESPONDENT_NAME));
        assertThat(respondentFlags.getRoleOnCase(), is(RESPONDENT));
        assertThat(respondentFlags.getGroupId(), is(RESPONDENT));
        assertThat(respondentFlags.getVisibility(), is(INTERNAL));

        CaseFlagsType claimantExternalFlags = caseData.getClaimantExternalFlags();
        assertThat(claimantExternalFlags.getPartyName(), is(CLAIMANT_NAME));
        assertThat(claimantExternalFlags.getRoleOnCase(), is(CLAIMANT));
        assertThat(claimantExternalFlags.getGroupId(), is(CLAIMANT));
        assertThat(claimantExternalFlags.getVisibility(), is(EXTERNAL));

        CaseFlagsType respondentExternalFlags = caseData.getRespondentExternalFlags();
        assertThat(respondentExternalFlags.getPartyName(), is(RESPONDENT_NAME));
        assertThat(respondentExternalFlags.getRoleOnCase(), is(RESPONDENT));
        assertThat(respondentExternalFlags.getGroupId(), is(RESPONDENT));
        assertThat(respondentExternalFlags.getVisibility(), is(EXTERNAL));
    }

    @Test
    void rollbackCaseFlags_shouldSetToNull() {
        caseFlagsService.setupCaseFlags(caseData);
        caseFlagsService.rollbackCaseFlags(caseData);

        assertThat(caseData.getCaseFlags(), is(nullValue()));
        assertThat(caseData.getClaimantFlags(), is(nullValue()));
        assertThat(caseData.getRespondentFlags(), is(nullValue()));
        assertThat(caseData.getClaimantExternalFlags(), is(nullValue()));
        assertThat(caseData.getRespondentExternalFlags(), is(nullValue()));
    }

    @Test
    void setupCaseFlags_shouldUpdateExistingClaimantFlagsWhenNameChanges() {
        caseFlagsService.setupCaseFlags(caseData);
        String updatedClaimantName = "Updated Claimant Name";
        caseData.setClaimant(updatedClaimantName);

        caseFlagsService.setupCaseFlags(caseData);

        CaseFlagsType claimantFlags = caseData.getClaimantFlags();
        CaseFlagsType claimantExternalFlags = caseData.getClaimantExternalFlags();
        assertSame(claimantFlags, caseData.getClaimantFlags());
        assertCaseFlags(caseData.getClaimantFlags(), updatedClaimantName, CLAIMANT, CLAIMANT, INTERNAL);
        assertSame(claimantExternalFlags, caseData.getClaimantExternalFlags());
        assertCaseFlags(caseData.getClaimantExternalFlags(), updatedClaimantName, CLAIMANT, CLAIMANT, EXTERNAL);
        assertFalse(caseFlagsService.caseFlagsSetupRequired(caseData));
    }

    @Test
    void setupCaseFlags_shouldUpdateExistingRespondentFlagsWhenNameChanges() {
        caseFlagsService.setupCaseFlags(caseData);
        String updatedRespondentName = "Updated Respondent Name";
        caseData.getRespondentCollection().getFirst().getValue().setRespondentName(updatedRespondentName);

        caseFlagsService.setupCaseFlags(caseData);

        CaseFlagsType respondentFlags = caseData.getRespondentFlags();
        CaseFlagsType respondentExternalFlags = caseData.getRespondentExternalFlags();
        assertSame(respondentFlags, caseData.getRespondentFlags());
        assertCaseFlags(caseData.getRespondentFlags(), updatedRespondentName, RESPONDENT, RESPONDENT, INTERNAL);
        assertSame(respondentExternalFlags, caseData.getRespondentExternalFlags());
        assertCaseFlags(caseData.getRespondentExternalFlags(), updatedRespondentName, RESPONDENT, RESPONDENT, EXTERNAL);
        assertFalse(caseFlagsService.caseFlagsSetupRequired(caseData));
    }

    @Test
    void processNewlySetCaseFlags_shouldSetInterpreterRequiredTrue() {
        caseData.setRespondentFlags(CaseFlagsType.builder().build());
        caseData.setClaimantFlags(CaseFlagsType.builder()
                .details(
                        ListTypeItem.from(
                                FlagDetailType.builder()
                                        .name(SIGN_LANGUAGE_INTERPRETER)
                                        .status(ACTIVE)
                                        .flagCode("RA00010")
                                        .build()))
                .build());
        caseData.setClaimantExternalFlags(CaseFlagsType.builder().build());

        caseFlagsService.processNewlySetCaseFlags(caseData);

        assertEquals(YES, caseData.getCaseInterpreterRequiredFlag());
    }

    @Test
    void processNewlySetCaseFlags_shouldSetAdditionalSecurityFlagTrue() {
        caseData.setRespondentFlags(CaseFlagsType.builder().build());
        caseData.setClaimantFlags(CaseFlagsType.builder()
                .details(
                        ListTypeItem.from(
                                FlagDetailType.builder()
                                        .name(VEXATIOUS_LITIGANT)
                                        .status(ACTIVE)
                                        .flagCode("CA00010")
                                        .build()))
                .build());
        caseData.setClaimantExternalFlags(CaseFlagsType.builder().build());

        caseFlagsService.processNewlySetCaseFlags(caseData);

        assertEquals(YES, caseData.getCaseAdditionalSecurityFlag());
    }

    @Test
    void setPrivateHearingFlag_shouldBeTruthy_whenRestrictPublicityTseApplicationIsGranted() {
        List<TseAdminRecordDecisionTypeItem> granted = List.of(TseAdminRecordDecisionTypeItem.builder()
                .value(TseAdminRecordDecisionType.builder()
                        .decision("Granted")
                        .build())
                .build());

        List<GenericTseApplicationTypeItem> build = List.of(GenericTseApplicationTypeItem.builder()
                .value(GenericTseApplicationType.builder()
                        .type(TSE_APP_RESTRICT_PUBLICITY)
                        .adminDecision(granted)
                        .build())
                .build());

        caseData.setGenericTseApplicationCollection(build);

        caseFlagsService.setPrivateHearingFlag(caseData);

        assertEquals(YES, caseData.getPrivateHearingRequiredFlag());
    }

    @Test
    void setPrivateHearingFlag_shouldBeFalsy_whenRestrictPublicityTseApplicationIsRejected() {
        List<TseAdminRecordDecisionTypeItem> granted = List.of(TseAdminRecordDecisionTypeItem.builder()
                .value(TseAdminRecordDecisionType.builder()
                        .decision("Refused")
                        .build())
                .build());

        List<GenericTseApplicationTypeItem> build = List.of(GenericTseApplicationTypeItem.builder()
                .value(GenericTseApplicationType.builder()
                        .type(TSE_APP_RESTRICT_PUBLICITY)
                        .adminDecision(granted)
                        .build())
                .build());

        caseData.setGenericTseApplicationCollection(build);

        caseFlagsService.setPrivateHearingFlag(caseData);

        assertEquals(NO, caseData.getPrivateHearingRequiredFlag());
    }

    @Test
    void setPrivateHearingFlag_shouldBeTruthy_whenFlaggedForRestrictedReportingRule503b() {
        RestrictedReportingType restrictedReportingType = new RestrictedReportingType();
        restrictedReportingType.setRule503b(YES);
        caseData.setRestrictedReporting(restrictedReportingType);
        caseFlagsService.setPrivateHearingFlag(caseData);

        assertEquals(YES, caseData.getPrivateHearingRequiredFlag());
    }

    @Test
    void setPrivateHearingFlag_shouldBeTruthy_whenFlaggedForRestrictedReportingImposed() {
        RestrictedReportingType restrictedReportingType = new RestrictedReportingType();
        restrictedReportingType.setImposed(YES);
        caseData.setRestrictedReporting(restrictedReportingType);
        caseFlagsService.setPrivateHearingFlag(caseData);

        assertEquals(YES, caseData.getPrivateHearingRequiredFlag());
    }

    @Test
    void setPrivateHearingFlag_shouldBeFalsy_whenRestrictedReportingExistsButNotFlagged() {
        RestrictedReportingType restrictedReportingType = new RestrictedReportingType();
        restrictedReportingType.setRule503b(NO);
        restrictedReportingType.setImposed(NO);
        caseData.setRestrictedReporting(restrictedReportingType);
        caseFlagsService.setPrivateHearingFlag(caseData);

        assertEquals(NO, caseData.getPrivateHearingRequiredFlag());
    }

    @Test
    void setPrivateHearingFlag_shouldBeTruthy_whenIcListingPreliminaryHearing() {
        caseData.setIcListingPreliminaryHearing(YES);
        caseFlagsService.setPrivateHearingFlag(caseData);

        assertEquals(YES, caseData.getPrivateHearingRequiredFlag());
    }

    @Test
    void setPrivateHearingFlag_shouldBeFalsy_whenIcListingPreliminaryHearingIsFalse() {
        caseData.setIcListingPreliminaryHearing(NO);
        caseFlagsService.setPrivateHearingFlag(caseData);

        assertEquals(NO, caseData.getPrivateHearingRequiredFlag());
    }

    private void assertCaseFlags(
            CaseFlagsType flags, String partyName, String roleOnCase, String groupId, String visibility) {
        assertThat(flags.getPartyName(), is(partyName));
        assertThat(flags.getRoleOnCase(), is(roleOnCase));
        assertThat(flags.getGroupId(), is(groupId));
        assertThat(flags.getVisibility(), is(visibility));
    }
}

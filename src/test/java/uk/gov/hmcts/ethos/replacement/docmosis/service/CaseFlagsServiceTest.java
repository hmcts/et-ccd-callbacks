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
import uk.gov.hmcts.et.common.model.ccd.items.TseAdminRecordDecisionTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.CaseFlagsType;
import uk.gov.hmcts.et.common.model.ccd.types.RestrictedReportingType;
import uk.gov.hmcts.et.common.model.ccd.types.TseAdminRecordDecisionType;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.ecm.common.model.helper.CaseFlagConstants.ACTIVE;
import static uk.gov.hmcts.ecm.common.model.helper.CaseFlagConstants.ROLE_CLAIMANT;
import static uk.gov.hmcts.ecm.common.model.helper.CaseFlagConstants.ROLE_RESPONDENT;
import static uk.gov.hmcts.ecm.common.model.helper.CaseFlagConstants.SIGN_LANGUAGE_INTERPRETER;
import static uk.gov.hmcts.ecm.common.model.helper.CaseFlagConstants.VEXATIOUS_LITIGANT;
import static uk.gov.hmcts.ecm.common.model.helper.CaseFlagConstants.VISIBILITY_EXTERNAL;
import static uk.gov.hmcts.ecm.common.model.helper.CaseFlagConstants.VISIBILITY_INTERNAL;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.TSE_APP_RESTRICT_PUBLICITY;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

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

        CaseFlagsType claimantInternalFlags = caseData.getClaimantInternalFlags();
        assertThat(claimantInternalFlags.getPartyName(), is(CLAIMANT_NAME));
        assertThat(claimantInternalFlags.getRoleOnCase(), is(ROLE_CLAIMANT));
        assertThat(claimantInternalFlags.getVisibility(), is(VISIBILITY_INTERNAL));

        CaseFlagsType respondentInternalFlags = caseData.getRespondentInternalFlags();
        assertThat(respondentInternalFlags.getPartyName(), is(RESPONDENT_NAME));
        assertThat(respondentInternalFlags.getRoleOnCase(), is(ROLE_RESPONDENT));
        assertThat(respondentInternalFlags.getVisibility(), is(VISIBILITY_INTERNAL));

        CaseFlagsType claimantExternalFlags = caseData.getClaimantExternalFlags();
        assertThat(claimantExternalFlags.getPartyName(), is(CLAIMANT_NAME));
        assertThat(claimantExternalFlags.getRoleOnCase(), is(ROLE_CLAIMANT));
        assertThat(claimantExternalFlags.getVisibility(), is(VISIBILITY_EXTERNAL));

        CaseFlagsType respondentExternalFlags = caseData.getRespondentExternalFlags();
        assertThat(respondentExternalFlags.getPartyName(), is(RESPONDENT_NAME));
        assertThat(respondentExternalFlags.getRoleOnCase(), is(ROLE_RESPONDENT));
        assertThat(respondentExternalFlags.getVisibility(), is(VISIBILITY_EXTERNAL));
    }

    @Test
    void rollbackCaseFlags_shouldSetToNull() {
        caseFlagsService.setupCaseFlags(caseData);
        caseFlagsService.rollbackCaseFlags(caseData);

        assertThat(caseData.getCaseFlags(), is(nullValue()));
        assertThat(caseData.getClaimantInternalFlags(), is(nullValue()));
        assertThat(caseData.getRespondentInternalFlags(), is(nullValue()));
        assertThat(caseData.getClaimantExternalFlags(), is(nullValue()));
        assertThat(caseData.getRespondentExternalFlags(), is(nullValue()));
    }

    @Test
    void processNewlySetCaseFlags_shouldSetInterpreterRequiredTrue() {
        caseData.setRespondentInternalFlags(CaseFlagsType.builder().build());
        caseData.setClaimantInternalFlags(CaseFlagsType.builder()
                .details(
                        ListTypeItem.from(
                                FlagDetailType.builder()
                                        .name(SIGN_LANGUAGE_INTERPRETER)
                                        .status(ACTIVE)
                                        .flagCode("RA00010")
                                        .build()))
                .build());
        caseFlagsService.processNewlySetCaseFlags(caseData);

        assertEquals(YES, caseData.getCaseInterpreterRequiredFlag());
    }

    @Test
    void processNewlySetCaseFlags_shouldSetAdditionalSecurityFlagTrue() {
        caseData.setRespondentInternalFlags(CaseFlagsType.builder().build());
        caseData.setClaimantInternalFlags(CaseFlagsType.builder()
                .details(
                        ListTypeItem.from(
                                FlagDetailType.builder()
                                        .name(VEXATIOUS_LITIGANT)
                                        .status(ACTIVE)
                                        .flagCode("CA00010")
                                        .build()))
                .build());
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
}

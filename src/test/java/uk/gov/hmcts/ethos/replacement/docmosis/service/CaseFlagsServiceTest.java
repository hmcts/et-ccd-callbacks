package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.FlagDetailType;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.ListTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.TseAdminRecordDecisionTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.CaseFlagsType;
import uk.gov.hmcts.et.common.model.ccd.types.RestrictedReportingType;
import uk.gov.hmcts.et.common.model.ccd.types.TseAdminRecordDecisionType;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;
import uk.gov.hmcts.ethos.utils.TseApplicationBuilder;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.ecm.common.model.helper.CaseFlagConstants.ACTIVE;
import static uk.gov.hmcts.ecm.common.model.helper.CaseFlagConstants.DISRUPTIVE_CUSTOMER;
import static uk.gov.hmcts.ecm.common.model.helper.CaseFlagConstants.INACTIVE;
import static uk.gov.hmcts.ecm.common.model.helper.CaseFlagConstants.LANGUAGE_INTERPRETER;
import static uk.gov.hmcts.ecm.common.model.helper.CaseFlagConstants.SIGN_LANGUAGE_INTERPRETER;
import static uk.gov.hmcts.ecm.common.model.helper.CaseFlagConstants.VEXATIOUS_LITIGANT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.TSE_APP_CONSIDER_A_DECISION_AFRESH;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.TSE_APP_RESTRICT_PUBLICITY;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@ExtendWith(SpringExtension.class)
class CaseFlagsServiceTest {
    private static final String CLAIMANT_NAME = "Claimant Name";
    private static final String RESPONDENT_NAME = "Respondent Name";
    private static final String GRANTED = "Granted";
    private static final String REFUSED = "Refused";

    private CaseFlagsService caseFlagsService;
    private CaseData caseData;

    @BeforeEach
    void setUp() {
        caseFlagsService = new CaseFlagsService();
        caseData = CaseDataBuilder.builder().build();
        caseData.setClaimant(CLAIMANT_NAME);
        caseData.setRespondent(RESPONDENT_NAME);

        caseData.setClaimantFlags(CaseFlagsType.builder().details(ListTypeItem.from()).build());
        caseData.setRespondentFlags(CaseFlagsType.builder().details(ListTypeItem.from()).build());
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
        assertThat(claimantFlags.getRoleOnCase(), is("claimant"));

        CaseFlagsType respondentFlags = caseData.getRespondentFlags();
        assertThat(respondentFlags.getPartyName(), is(RESPONDENT_NAME));
        assertThat(respondentFlags.getRoleOnCase(), is("respondent"));
    }

    @Test
    void setDefaultFlags_setsDefaultFlags() {
        caseFlagsService.setDefaultFlags(caseData);

        assertThat(caseData.getCaseRestrictedFlag(), is(NO));
        assertThat(caseData.getAutoListFlag(), is(YES));
        assertThat(caseData.getCaseInterpreterRequiredFlag(), is(NO));
        assertThat(caseData.getCaseAdditionalSecurityFlag(), is(NO));
    }

    @Test
    void processNewlySetCaseFlags_claimantNeedsLanguageInterpreter_setsInterpreterRequiredFlagTrue() {
        caseData.getClaimantFlags().getDetails().add(
                GenericTypeItem.from(FlagDetailType.builder().name(LANGUAGE_INTERPRETER).status(ACTIVE).build())
        );

        caseFlagsService.processNewlySetCaseFlags(caseData);

        assertThat(caseData.getCaseInterpreterRequiredFlag(), is(YES));
    }

    @Test
    void processNewlySetCaseFlags_respondentNeedsLanguageInterpreter_setsInterpreterRequiredFlagTrue() {
        caseData.getRespondentFlags().getDetails().add(
                GenericTypeItem.from(FlagDetailType.builder().name(LANGUAGE_INTERPRETER).status(ACTIVE).build())
        );

        caseFlagsService.processNewlySetCaseFlags(caseData);

        assertThat(caseData.getCaseInterpreterRequiredFlag(), is(YES));
    }

    @Test
    void processNewlySetCaseFlags_claimantNeedsSignLanguageInterpreter_setsInterpreterRequiredFlagTrue() {
        caseData.getClaimantFlags().getDetails().add(
                GenericTypeItem.from(FlagDetailType.builder().name(SIGN_LANGUAGE_INTERPRETER).status(ACTIVE).build())
        );

        caseFlagsService.processNewlySetCaseFlags(caseData);

        assertThat(caseData.getCaseInterpreterRequiredFlag(), is(YES));
    }

    @Test
    void processNewlySetCaseFlags_respondentNeedsSignLanguageInterpreter_setsInterpreterRequiredFlagTrue() {
        caseData.getRespondentFlags().getDetails().add(
                GenericTypeItem.from(FlagDetailType.builder().name(SIGN_LANGUAGE_INTERPRETER).status(ACTIVE).build())
        );

        caseFlagsService.processNewlySetCaseFlags(caseData);

        assertThat(caseData.getCaseInterpreterRequiredFlag(), is(YES));
    }

    @Test
    void processNewlySetCaseFlags_noInterpretersNeeded_setsInterpreterRequiredFlagFalse() {
        caseData.getClaimantFlags().getDetails().add(
                GenericTypeItem.from(FlagDetailType.builder().name(LANGUAGE_INTERPRETER).status(INACTIVE).build())
        );

        caseData.getRespondentFlags().getDetails().add(
                GenericTypeItem.from(FlagDetailType.builder().name(SIGN_LANGUAGE_INTERPRETER).status(INACTIVE).build())
        );

        caseFlagsService.processNewlySetCaseFlags(caseData);

        assertThat(caseData.getCaseInterpreterRequiredFlag(), is(NO));
    }

    @Test
    void processNewlySetCaseFlags_claimantIsVexatious_setsAdditionalSecurityFlagTrue() {
        caseData.getClaimantFlags().getDetails().add(
                GenericTypeItem.from(FlagDetailType.builder().name(VEXATIOUS_LITIGANT).status(ACTIVE).build())
        );

        caseFlagsService.processNewlySetCaseFlags(caseData);

        assertThat(caseData.getCaseAdditionalSecurityFlag(), is(YES));
    }

    @Test
    void processNewlySetCaseFlags_respondentIsVexatious_setsAdditionalSecurityFlagTrue() {
        caseData.getRespondentFlags().getDetails().add(
                GenericTypeItem.from(FlagDetailType.builder().name(VEXATIOUS_LITIGANT).status(ACTIVE).build())
        );

        caseFlagsService.processNewlySetCaseFlags(caseData);

        assertThat(caseData.getCaseAdditionalSecurityFlag(), is(YES));
    }

    @Test
    void processNewlySetCaseFlags_claimantIsDisruptive_setsAdditionalSecurityFlagTrue() {
        caseData.getClaimantFlags().getDetails().add(
                GenericTypeItem.from(FlagDetailType.builder().name(DISRUPTIVE_CUSTOMER).status(ACTIVE).build())
        );

        caseFlagsService.processNewlySetCaseFlags(caseData);

        assertThat(caseData.getCaseAdditionalSecurityFlag(), is(YES));
    }

    @Test
    void processNewlySetCaseFlags_respondentIsDisruptive_setsAdditionalSecurityFlagTrue() {
        caseData.getRespondentFlags().getDetails().add(
                GenericTypeItem.from(FlagDetailType.builder().name(DISRUPTIVE_CUSTOMER).status(ACTIVE).build())
        );

        caseFlagsService.processNewlySetCaseFlags(caseData);

        assertThat(caseData.getCaseAdditionalSecurityFlag(), is(YES));
    }

    @Test
    void processNewlySetCaseFlags_noPartyIsVexatiousOrDisruptive_setsAdditionalSecurityFlagFalse() {
        caseData.getClaimantFlags().getDetails().add(
                GenericTypeItem.from(FlagDetailType.builder().name(VEXATIOUS_LITIGANT).status(INACTIVE).build())
        );

        caseData.getRespondentFlags().getDetails().add(
                GenericTypeItem.from(FlagDetailType.builder().name(DISRUPTIVE_CUSTOMER).status(INACTIVE).build())
        );

        caseFlagsService.processNewlySetCaseFlags(caseData);

        assertThat(caseData.getCaseInterpreterRequiredFlag(), is(NO));
    }

    private void setSingleApplicationOfTypeAndDecision(CaseData caseData, String applicationType, String decision) {
        caseData.setGenericTseApplicationCollection(
                List.of(GenericTseApplicationTypeItem.builder()
                        .id(UUID.randomUUID().toString())
                        .value(TseApplicationBuilder.builder()
                                .withNumber("4")
                                .withType(applicationType)
                                .build())
                        .build())
        );

        caseData.setTseAdminSelectApplication(DynamicFixedListType.of(DynamicValueType.create("4", "")));
        caseData.getGenericTseApplicationCollection().get(0).getValue().setAdminDecision(List.of(
                TseAdminRecordDecisionTypeItem.builder()
                        .value(TseAdminRecordDecisionType.builder().decision(decision).build())
                        .build()
                )
        );
    }

    @Test
    void setPrivateHearingFlag_defaultCaseData_doesNotSetFlag() {
        caseFlagsService.setPrivateHearingFlag(caseData);

        assertEquals(NO, caseData.getPrivateHearingRequiredFlag());
    }

    @Test
    void setPrivateHearingFlag_notRestrictedHearingType_doesNotSetFlag() {
        setSingleApplicationOfTypeAndDecision(caseData, TSE_APP_CONSIDER_A_DECISION_AFRESH, GRANTED);
        caseFlagsService.setPrivateHearingFlag(caseData);

        assertEquals(NO, caseData.getPrivateHearingRequiredFlag());
    }

    @Test
    void setPrivateHearingFlag_whenGrantedRestrictPublicityApplication_setsFlag() {
        setSingleApplicationOfTypeAndDecision(caseData, TSE_APP_RESTRICT_PUBLICITY, GRANTED);

        caseFlagsService.setPrivateHearingFlag(caseData);

        assertEquals(YES, caseData.getPrivateHearingRequiredFlag());
    }

    @Test
    void setPrivateHearingFlag_whenRefusedRestrictPublicityApplication_doesNotSetFlag() {
        setSingleApplicationOfTypeAndDecision(caseData, TSE_APP_RESTRICT_PUBLICITY, REFUSED);

        caseFlagsService.setPrivateHearingFlag(caseData);

        assertEquals(NO, caseData.getPrivateHearingRequiredFlag());
    }

    @Test
    void setPrivateHearingFlag_whenRule503bIsYes_setsFlag() {
        RestrictedReportingType restrictedReporting = new RestrictedReportingType();
        restrictedReporting.setRule503b(YES);
        caseData.setRestrictedReporting(restrictedReporting);

        caseFlagsService.setPrivateHearingFlag(caseData);

        assertEquals(YES, caseData.getPrivateHearingRequiredFlag());
    }

    @Test
    void setPrivateHearingFlag_whenImposedIsYes_setsFlag() {
        RestrictedReportingType restrictedReporting = new RestrictedReportingType();
        restrictedReporting.setImposed(YES);
        caseData.setRestrictedReporting(restrictedReporting);

        caseFlagsService.setPrivateHearingFlag(caseData);

        assertEquals(YES, caseData.getPrivateHearingRequiredFlag());
    }

    @Test
    void setPrivateHearingFlag_whenRule503bAndImposedAreNo_setsFlag() {
        RestrictedReportingType restrictedReporting = new RestrictedReportingType();
        restrictedReporting.setRule503b(NO);
        restrictedReporting.setImposed(NO);
        caseData.setRestrictedReporting(restrictedReporting);

        caseFlagsService.setPrivateHearingFlag(caseData);

        assertEquals(NO, caseData.getPrivateHearingRequiredFlag());
    }

    @Test
    void setPrivateHearingFlag_withPreliminaryHearing_setsFlag() {
        caseData.setIcListingPreliminaryHearing(YES);

        caseFlagsService.setPrivateHearingFlag(caseData);

        assertEquals(YES, caseData.getPrivateHearingRequiredFlag());
      
    @Test  
    void rollbackCaseFlags_shouldSetToNull() {
        caseFlagsService.setupCaseFlags(caseData);
        caseFlagsService.rollbackCaseFlags(caseData);

        assertThat(caseData.getCaseFlags(), is(nullValue()));
        assertThat(caseData.getClaimantFlags(), is(nullValue()));
        assertThat(caseData.getRespondentFlags(), is(nullValue()));
    }
}

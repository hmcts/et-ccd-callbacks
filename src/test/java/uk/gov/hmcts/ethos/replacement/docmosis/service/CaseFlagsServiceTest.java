package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.FlagDetailType;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationType;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTypeItem;
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
import static uk.gov.hmcts.ecm.common.model.helper.CaseFlagConstants.DISRUPTIVE_CUSTOMER;
import static uk.gov.hmcts.ecm.common.model.helper.CaseFlagConstants.INACTIVE;
import static uk.gov.hmcts.ecm.common.model.helper.CaseFlagConstants.LANGUAGE_INTERPRETER;
import static uk.gov.hmcts.ecm.common.model.helper.CaseFlagConstants.SIGN_LANGUAGE_INTERPRETER;
import static uk.gov.hmcts.ecm.common.model.helper.CaseFlagConstants.VEXATIOUS_LITIGANT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.TSE_APP_RESTRICT_PUBLICITY;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.CaseFlagsService.CLAIMANT;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.CaseFlagsService.EXTERNAL;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.CaseFlagsService.INTERNAL;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.CaseFlagsService.RESPONDENT;

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

        CaseFlagsType[] internalRespondentFlags = internalRespondentFlags();
        CaseFlagsType[] externalRespondentFlags = externalRespondentFlags();
        for (int index = 0; index < internalRespondentFlags.length; index++) {
            String expectedRespondentName = index == 0 ? RESPONDENT_NAME : RESPONDENT_NAME + " " + (index + 1);
            String expectedGroupId = expectedRespondentGroupId(index);

            assertCaseFlags(internalRespondentFlags[index], expectedRespondentName, RESPONDENT, expectedGroupId,
                    INTERNAL);
            assertCaseFlags(externalRespondentFlags[index], expectedRespondentName, RESPONDENT, expectedGroupId,
                    EXTERNAL);
        }
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
        for (CaseFlagsType flags : internalRespondentFlags()) {
            assertThat(flags, is(nullValue()));
        }
        for (CaseFlagsType flags : externalRespondentFlags()) {
            assertThat(flags, is(nullValue()));
        }
    }

    @Test
    void caseFlagsSetupRequired_shouldBeTrueWhenPartyNameChanges() {
        caseFlagsService.setupCaseFlags(caseData);

        caseData.setClaimant("Updated Claimant Name");

        assertTrue(caseFlagsService.caseFlagsSetupRequired(caseData));
    }

    @Test
    void caseFlagsSetupRequired_shouldDetectClaimantMetadataMismatches() {
        caseFlagsService.setupCaseFlags(caseData);

        caseData.getClaimantFlags().setRoleOnCase("incorrect-role");
        assertTrue(caseFlagsService.caseFlagsSetupRequired(caseData));

        caseFlagsService.setupCaseFlags(caseData);
        caseData.getClaimantFlags().setGroupId("incorrect-group");
        assertTrue(caseFlagsService.caseFlagsSetupRequired(caseData));

        caseFlagsService.setupCaseFlags(caseData);
        caseData.getClaimantFlags().setVisibility("incorrect-visibility");
        assertTrue(caseFlagsService.caseFlagsSetupRequired(caseData));

        caseFlagsService.setupCaseFlags(caseData);
        caseData.getClaimantExternalFlags().setGroupId("incorrect-external-group");
        assertTrue(caseFlagsService.caseFlagsSetupRequired(caseData));
    }

    @Test
    void caseFlagsSetupRequired_shouldBeTrueWhenClaimantFlagObjectsAreMissing() {
        caseData.setCaseFlags(CaseFlagsType.builder().build());
        caseData.setClaimantExternalFlags(CaseFlagsType.builder()
                .partyName(CLAIMANT_NAME)
                .roleOnCase(CLAIMANT)
                .groupId(CLAIMANT)
                .visibility(EXTERNAL)
                .build());

        assertTrue(caseFlagsService.caseFlagsSetupRequired(caseData));

        caseData.setClaimantFlags(CaseFlagsType.builder()
                .partyName(CLAIMANT_NAME)
                .roleOnCase(CLAIMANT)
                .groupId(CLAIMANT)
                .visibility(INTERNAL)
                .build());
        caseData.setClaimantExternalFlags(null);

        assertTrue(caseFlagsService.caseFlagsSetupRequired(caseData));
    }

    @Test
    void caseFlagsSetupRequired_shouldDetectRespondentMetadataMismatches() {
        caseFlagsService.setupCaseFlags(caseData);

        caseData.getRespondent4Flags().setGroupId("incorrect-group");
        assertTrue(caseFlagsService.caseFlagsSetupRequired(caseData));

        caseFlagsService.setupCaseFlags(caseData);
        caseData.getRespondent4ExternalFlags().setVisibility("incorrect-visibility");
        assertTrue(caseFlagsService.caseFlagsSetupRequired(caseData));
    }

    @Test
    void setupCaseFlags_shouldKeepExistingCaseFlagsAndHandleNullRespondentCollection() {
        CaseFlagsType existingCaseFlags = CaseFlagsType.builder().build();
        caseData.setCaseFlags(existingCaseFlags);
        caseData.setRespondentCollection(null);

        caseFlagsService.setupCaseFlags(caseData);

        assertSame(existingCaseFlags, caseData.getCaseFlags());
        assertCaseFlags(caseData.getClaimantFlags(), CLAIMANT_NAME, CLAIMANT, CLAIMANT, INTERNAL);
        assertCaseFlags(caseData.getClaimantExternalFlags(), CLAIMANT_NAME, CLAIMANT, CLAIMANT, EXTERNAL);
        assertThat(caseData.getRespondentFlags(), is(nullValue()));
        assertThat(caseData.getRespondentExternalFlags(), is(nullValue()));
        assertFalse(caseFlagsService.caseFlagsSetupRequired(caseData));
    }

    @Test
    void setupCaseFlags_shouldNotRequireRespondentFlagsWhenRespondentCollectionIsEmpty() {
        caseData.setRespondentCollection(List.of());

        caseFlagsService.setupCaseFlags(caseData);

        assertThat(caseData.getRespondentFlags(), is(nullValue()));
        assertThat(caseData.getRespondentExternalFlags(), is(nullValue()));
        assertFalse(caseFlagsService.caseFlagsSetupRequired(caseData));
    }

    @Test
    void setupCaseFlags_shouldHandleRespondentsWithoutValues() {
        RespondentSumTypeItem respondentWithNullValue = new RespondentSumTypeItem();
        List<RespondentSumTypeItem> respondents = new ArrayList<>();
        respondents.add(null);
        respondents.add(respondentWithNullValue);
        caseData.setRespondentCollection(respondents);

        caseFlagsService.setupCaseFlags(caseData);

        assertCaseFlags(caseData.getRespondentFlags(), null, RESPONDENT, RESPONDENT, INTERNAL);
        assertCaseFlags(caseData.getRespondentExternalFlags(), null, RESPONDENT, RESPONDENT, EXTERNAL);
        assertCaseFlags(caseData.getRespondent1Flags(), null, RESPONDENT, RESPONDENT + "1", INTERNAL);
        assertCaseFlags(caseData.getRespondent1ExternalFlags(), null, RESPONDENT, RESPONDENT + "1", EXTERNAL);
        assertFalse(caseFlagsService.caseFlagsSetupRequired(caseData));
    }

    @Test
    void setupCaseFlags_shouldIgnoreRespondentsBeyondSupportedFields() {
        caseData.getRespondentCollection().add(respondent());

        caseFlagsService.setupCaseFlags(caseData);

        assertFalse(caseFlagsService.caseFlagsSetupRequired(caseData));
        assertEquals(RESPONDENT_NAME + " 10", caseData.getRespondent9Flags().getPartyName());
        assertEquals(RESPONDENT_NAME + " 10", caseData.getRespondent9ExternalFlags().getPartyName());
    }

    @Test
    void setupCaseFlags_shouldRepairIncompleteMetadataWithoutLosingDetails() {
        caseFlagsService.setupCaseFlags(caseData);
        ListTypeItem<FlagDetailType> details = ListTypeItem.from(FlagDetailType.builder()
                .name(SIGN_LANGUAGE_INTERPRETER)
                .status(ACTIVE)
                .build());
        caseData.getClaimantFlags().setDetails(details);
        caseData.getClaimantFlags().setRoleOnCase(null);
        caseData.getClaimantFlags().setGroupId(null);
        caseData.getClaimantFlags().setVisibility(null);

        assertTrue(caseFlagsService.caseFlagsSetupRequired(caseData));

        caseFlagsService.setupCaseFlags(caseData);

        assertFalse(caseFlagsService.caseFlagsSetupRequired(caseData));
        assertEquals(CLAIMANT, caseData.getClaimantFlags().getRoleOnCase());
        assertEquals(CLAIMANT, caseData.getClaimantFlags().getGroupId());
        assertEquals(INTERNAL, caseData.getClaimantFlags().getVisibility());
        assertEquals(details, caseData.getClaimantFlags().getDetails());
    }

    @Test
    void setupCaseFlags_shouldRepairRespondentMetadataWithoutLosingDetails() {
        caseFlagsService.setupCaseFlags(caseData);
        ListTypeItem<FlagDetailType> details = ListTypeItem.from(flag(LANGUAGE_INTERPRETER, ACTIVE));
        caseData.getRespondent5ExternalFlags().setDetails(details);
        caseData.getRespondent5ExternalFlags().setRoleOnCase(null);
        caseData.getRespondent5ExternalFlags().setGroupId(null);
        caseData.getRespondent5ExternalFlags().setVisibility(null);

        assertTrue(caseFlagsService.caseFlagsSetupRequired(caseData));

        caseFlagsService.setupCaseFlags(caseData);

        assertFalse(caseFlagsService.caseFlagsSetupRequired(caseData));
        assertCaseFlags(caseData.getRespondent5ExternalFlags(), RESPONDENT_NAME + " 6", RESPONDENT,
                RESPONDENT + "5", EXTERNAL);
        assertEquals(details, caseData.getRespondent5ExternalFlags().getDetails());
    }

    @Test
    void setupCaseFlags_shouldUpdateClaimantFlagsWhenNameChanges() {
        caseFlagsService.setupCaseFlags(caseData);
        String updatedClaimantName = "Updated Claimant Name";
        caseData.setClaimant(updatedClaimantName);

        caseFlagsService.setupCaseFlags(caseData);

        assertEquals(updatedClaimantName, caseData.getClaimantFlags().getPartyName());
        assertEquals(CLAIMANT, caseData.getClaimantFlags().getRoleOnCase());
        assertEquals(CLAIMANT, caseData.getClaimantFlags().getGroupId());
        assertEquals(INTERNAL, caseData.getClaimantFlags().getVisibility());

        assertEquals(updatedClaimantName, caseData.getClaimantExternalFlags().getPartyName());
        assertEquals(CLAIMANT, caseData.getClaimantExternalFlags().getRoleOnCase());
        assertEquals(CLAIMANT, caseData.getClaimantExternalFlags().getGroupId());
        assertEquals(EXTERNAL, caseData.getClaimantExternalFlags().getVisibility());
    }

    @Test
    void setupCaseFlags_shouldUpdateRespondentFlagsWhenNameChanges() {
        caseFlagsService.setupCaseFlags(caseData);
        String updatedRespondentName = "Updated Respondent Name";
        caseData.getRespondentCollection().getFirst().getValue().setRespondentName(updatedRespondentName);

        caseFlagsService.setupCaseFlags(caseData);

        assertEquals(updatedRespondentName, caseData.getRespondentFlags().getPartyName());
        assertEquals(RESPONDENT, caseData.getRespondentFlags().getRoleOnCase());
        assertEquals(RESPONDENT, caseData.getRespondentFlags().getGroupId());
        assertEquals(INTERNAL, caseData.getRespondentFlags().getVisibility());

        assertEquals(updatedRespondentName, caseData.getRespondentExternalFlags().getPartyName());
        assertEquals(RESPONDENT, caseData.getRespondentExternalFlags().getRoleOnCase());
        assertEquals(RESPONDENT, caseData.getRespondentExternalFlags().getGroupId());
        assertEquals(EXTERNAL, caseData.getRespondentExternalFlags().getVisibility());
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
    void processNewlySetCaseFlags_shouldUseEquivalentActiveFlagsFromAnyParty() {
        caseData.setClaimantFlags(CaseFlagsType.builder().build());
        caseData.setClaimantExternalFlags(CaseFlagsType.builder().build());
        caseData.setRespondent9Flags(CaseFlagsType.builder()
                .details(ListTypeItem.from(flag(LANGUAGE_INTERPRETER, ACTIVE)))
                .build());
        caseData.setRespondent9ExternalFlags(CaseFlagsType.builder()
                .details(ListTypeItem.from(flag(DISRUPTIVE_CUSTOMER, ACTIVE)))
                .build());

        caseFlagsService.processNewlySetCaseFlags(caseData);

        assertEquals(YES, caseData.getCaseInterpreterRequiredFlag());
        assertEquals(YES, caseData.getCaseAdditionalSecurityFlag());
    }

    @Test
    void processNewlySetCaseFlags_shouldSetInterpreterRequiredTrueWhenLaterMatchingFlagIsActive() {
        caseData.setClaimantFlags(CaseFlagsType.builder()
                .details(ListTypeItem.from(FlagDetailType.builder()
                        .name(SIGN_LANGUAGE_INTERPRETER)
                        .status(INACTIVE)
                        .build()))
                .build());
        caseData.setClaimantExternalFlags(CaseFlagsType.builder()
                .details(ListTypeItem.from(FlagDetailType.builder()
                        .name(SIGN_LANGUAGE_INTERPRETER)
                        .status(ACTIVE)
                        .build()))
                .build());

        caseFlagsService.processNewlySetCaseFlags(caseData);

        assertEquals(YES, caseData.getCaseInterpreterRequiredFlag());
    }

    @Test
    void processNewlySetCaseFlags_shouldIgnoreInactiveUnknownAndMalformedFlagDetails() {
        ListTypeItem<FlagDetailType> malformedDetails = new ListTypeItem<>();
        malformedDetails.add(null);
        malformedDetails.add(GenericTypeItem.from(null));
        malformedDetails.add(GenericTypeItem.from(flag(SIGN_LANGUAGE_INTERPRETER, INACTIVE)));
        malformedDetails.add(GenericTypeItem.from(flag("Unknown flag", ACTIVE)));

        caseData.setClaimantFlags(CaseFlagsType.builder().details(malformedDetails).build());
        caseData.setClaimantExternalFlags(CaseFlagsType.builder().build());

        caseFlagsService.processNewlySetCaseFlags(caseData);

        assertEquals(NO, caseData.getCaseInterpreterRequiredFlag());
        assertEquals(NO, caseData.getCaseAdditionalSecurityFlag());
    }

    @Test
    void processNewlySetCaseFlags_shouldSetFlagsToNoWhenPartyFlagsAreMissing() {
        caseFlagsService.processNewlySetCaseFlags(caseData);

        assertEquals(NO, caseData.getCaseInterpreterRequiredFlag());
        assertEquals(NO, caseData.getCaseAdditionalSecurityFlag());
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
    void setPrivateHearingFlag_shouldBeTruthy_whenRestrictPublicityTseApplicationDecisionStartsWithGranted() {
        caseData.setGenericTseApplicationCollection(List.of(
                tseApplication(TSE_APP_RESTRICT_PUBLICITY, List.of(decision("Granted with conditions")))));

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
    void setPrivateHearingFlag_shouldBeFalsy_whenRestrictPublicityApplicationsAreMalformedOrIrrelevant() {
        List<TseAdminRecordDecisionTypeItem> malformedDecisions = new ArrayList<>();
        malformedDecisions.add(null);
        malformedDecisions.add(TseAdminRecordDecisionTypeItem.builder().build());
        malformedDecisions.add(decision(null));
        malformedDecisions.add(decision("granted"));

        List<GenericTseApplicationTypeItem> applications = new ArrayList<>();
        applications.add(null);
        applications.add(GenericTseApplicationTypeItem.builder().build());
        applications.add(tseApplication("Other application", List.of(decision("Granted"))));
        applications.add(tseApplication(TSE_APP_RESTRICT_PUBLICITY, null));
        applications.add(tseApplication(TSE_APP_RESTRICT_PUBLICITY, malformedDecisions));
        caseData.setGenericTseApplicationCollection(applications);

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

    private RespondentSumTypeItem respondent() {
        RespondentSumTypeItem respondentSumTypeItem = new RespondentSumTypeItem();
        RespondentSumType respondentSumType = new RespondentSumType();
        respondentSumType.setRespondentName("Unsupported Respondent");
        respondentSumTypeItem.setId(UUID.randomUUID().toString());
        respondentSumTypeItem.setValue(respondentSumType);
        return respondentSumTypeItem;
    }

    private void assertCaseFlags(
            CaseFlagsType flags, String partyName, String roleOnCase, String groupId, String visibility) {
        assertThat(flags.getPartyName(), is(partyName));
        assertThat(flags.getRoleOnCase(), is(roleOnCase));
        assertThat(flags.getGroupId(), is(groupId));
        assertThat(flags.getVisibility(), is(visibility));
    }

    private String expectedRespondentGroupId(int index) {
        return index == 0 ? RESPONDENT : RESPONDENT + index;
    }

    private FlagDetailType flag(String name, String status) {
        return FlagDetailType.builder()
                .name(name)
                .status(status)
                .build();
    }

    private GenericTseApplicationTypeItem tseApplication(
            String type, List<TseAdminRecordDecisionTypeItem> adminDecision) {
        return GenericTseApplicationTypeItem.builder()
                .value(GenericTseApplicationType.builder()
                        .type(type)
                        .adminDecision(adminDecision)
                        .build())
                .build();
    }

    private TseAdminRecordDecisionTypeItem decision(String decision) {
        return TseAdminRecordDecisionTypeItem.builder()
                .value(TseAdminRecordDecisionType.builder()
                        .decision(decision)
                        .build())
                .build();
    }

    private CaseFlagsType[] internalRespondentFlags() {
        return new CaseFlagsType[] {
                caseData.getRespondentFlags(),
                caseData.getRespondent1Flags(),
                caseData.getRespondent2Flags(),
                caseData.getRespondent3Flags(),
                caseData.getRespondent4Flags(),
                caseData.getRespondent5Flags(),
                caseData.getRespondent6Flags(),
                caseData.getRespondent7Flags(),
                caseData.getRespondent8Flags(),
                caseData.getRespondent9Flags()
        };
    }

    private CaseFlagsType[] externalRespondentFlags() {
        return new CaseFlagsType[] {
                caseData.getRespondentExternalFlags(),
                caseData.getRespondent1ExternalFlags(),
                caseData.getRespondent2ExternalFlags(),
                caseData.getRespondent3ExternalFlags(),
                caseData.getRespondent4ExternalFlags(),
                caseData.getRespondent5ExternalFlags(),
                caseData.getRespondent6ExternalFlags(),
                caseData.getRespondent7ExternalFlags(),
                caseData.getRespondent8ExternalFlags(),
                caseData.getRespondent9ExternalFlags()
        };
    }
}

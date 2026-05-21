package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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
import static uk.gov.hmcts.ethos.replacement.docmosis.service.CaseFlagsService.RESPONDENT1;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.CaseFlagsService.RESPONDENT10;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.CaseFlagsService.RESPONDENT2;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.CaseFlagsService.RESPONDENT3;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.CaseFlagsService.RESPONDENT4;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.CaseFlagsService.RESPONDENT5;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.CaseFlagsService.RESPONDENT6;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.CaseFlagsService.RESPONDENT7;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.CaseFlagsService.RESPONDENT8;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.CaseFlagsService.RESPONDENT9;

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

        caseData.setRespondentCollection(respondentCollection(10));
    }

    @Test
    void caseFlagsSetupRequired_shouldBeFalseAfterAllRequiredCaseFlagsAreSetup() {
        assertTrue(caseFlagsService.caseFlagsSetupRequired(caseData));

        caseFlagsService.setupCaseFlags(caseData);

        assertFalse(caseFlagsService.caseFlagsSetupRequired(caseData));
    }

    @Test
    void caseFlagsSetupRequired_shouldBeTrueWhenCaseFlagsAreMissing() {
        caseFlagsService.setupCaseFlags(caseData);
        caseData.setCaseFlags(null);

        assertTrue(caseFlagsService.caseFlagsSetupRequired(caseData));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("allPartyFlagSlots")
    void caseFlagsSetupRequired_shouldBeTrueWhenAnyRequiredPartyFlagIsMissing(FlagSlot slot) {
        caseFlagsService.setupCaseFlags(caseData);

        slot.setter.accept(caseData, null);

        assertTrue(caseFlagsService.caseFlagsSetupRequired(caseData));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("allPartyFlagSlots")
    void caseFlagsSetupRequired_shouldBeTrueWhenAnyRequiredPartyFlagRoleIsMissing(FlagSlot slot) {
        caseFlagsService.setupCaseFlags(caseData);
        slot.getter.apply(caseData).setRoleOnCase("");

        assertTrue(caseFlagsService.caseFlagsSetupRequired(caseData));
    }

    @Test
    void caseFlagsSetupRequired_shouldNotRequireRespondentFlagsWhenThereAreNoRespondents() {
        caseData.setRespondentCollection(new ArrayList<>());

        caseFlagsService.setupCaseFlags(caseData);

        assertFalse(caseFlagsService.caseFlagsSetupRequired(caseData));
        assertNull(caseData.getRespondentFlags());
        assertNull(caseData.getRespondentExternalFlags());
    }

    @Test
    void setupCaseFlags_setsShellCaseFlagsForClaimantAndEveryRespondent() {
        caseFlagsService.setupCaseFlags(caseData);

        CaseFlagsType caseFlags = caseData.getCaseFlags();
        assertThat(caseFlags.getPartyName(), is(nullValue()));
        assertThat(caseFlags.getRoleOnCase(), is(nullValue()));

        allPartyFlagSlots().forEach(slot -> assertCaseFlag(slot, slot.getter.apply(caseData)));
    }

    @Test
    void setupCaseFlags_shouldOnlyCreateRespondentFlagsForExistingRespondents() {
        caseData.setRespondentCollection(respondentCollection(3));

        caseFlagsService.setupCaseFlags(caseData);

        respondentPartyFlagSlots().forEach(slot -> {
            if (slot.respondentIndex < 3) {
                assertNotNull(slot.getter.apply(caseData), slot + " should be populated");
            } else {
                assertNull(slot.getter.apply(caseData), slot + " should not be populated");
            }
        });
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("allPartyFlagSlots")
    void setupCaseFlags_shouldRecreatePartyFlagsWhenRoleIsMissing(FlagSlot slot) {
        caseFlagsService.setupCaseFlags(caseData);
        slot.setter.accept(caseData, CaseFlagsType.builder()
                .partyName("Old name")
                .roleOnCase("")
                .groupId("old-group")
                .visibility("old-visibility")
                .build());

        caseFlagsService.setupCaseFlags(caseData);

        assertCaseFlag(slot, slot.getter.apply(caseData));
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
        allPartyFlagSlots().forEach(slot -> assertNull(slot.getter.apply(caseData), slot + " should be cleared"));
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

    @ParameterizedTest(name = "{0}")
    @MethodSource("respondentPartyFlagSlots")
    void setupCaseFlags_shouldUpdateRespondentFlagsWhenNameChanges(FlagSlot slot) {
        caseFlagsService.setupCaseFlags(caseData);
        String updatedRespondentName = "Updated " + slot;
        caseData.getRespondentCollection()
                .get(slot.respondentIndex)
                .getValue()
                .setRespondentName(updatedRespondentName);

        caseFlagsService.setupCaseFlags(caseData);

        assertEquals(updatedRespondentName, slot.getter.apply(caseData).getPartyName());
        assertEquals(slot.roleOnCase, slot.getter.apply(caseData).getRoleOnCase());
        assertEquals(slot.roleOnCase, slot.getter.apply(caseData).getGroupId());
        assertEquals(slot.visibility, slot.getter.apply(caseData).getVisibility());
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

    @ParameterizedTest(name = "{0}")
    @MethodSource("allPartyFlagSlots")
    void processNewlySetCaseFlags_shouldReadInterpreterFlagsFromAnyPartyFlagCollection(FlagSlot slot) {
        caseFlagsService.setupCaseFlags(caseData);
        slot.getter.apply(caseData).setDetails(ListTypeItem.from(activeFlag(SIGN_LANGUAGE_INTERPRETER)));

        caseFlagsService.processNewlySetCaseFlags(caseData);

        assertEquals(YES, caseData.getCaseInterpreterRequiredFlag());
        assertEquals(NO, caseData.getCaseAdditionalSecurityFlag());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("allPartyFlagSlots")
    void processNewlySetCaseFlags_shouldReadSecurityFlagsFromAnyPartyFlagCollection(FlagSlot slot) {
        caseFlagsService.setupCaseFlags(caseData);
        slot.getter.apply(caseData).setDetails(ListTypeItem.from(activeFlag(VEXATIOUS_LITIGANT)));

        caseFlagsService.processNewlySetCaseFlags(caseData);

        assertEquals(NO, caseData.getCaseInterpreterRequiredFlag());
        assertEquals(YES, caseData.getCaseAdditionalSecurityFlag());
    }

    @Test
    void processNewlySetCaseFlags_shouldSupportAlternativeInterpreterAndSecurityFlagNames() {
        caseFlagsService.setupCaseFlags(caseData);
        caseData.getClaimantExternalFlags().setDetails(ListTypeItem.from(activeFlag(LANGUAGE_INTERPRETER)));
        caseData.getRespondent9ExternalFlags().setDetails(ListTypeItem.from(activeFlag(DISRUPTIVE_CUSTOMER)));

        caseFlagsService.processNewlySetCaseFlags(caseData);

        assertEquals(YES, caseData.getCaseInterpreterRequiredFlag());
        assertEquals(YES, caseData.getCaseAdditionalSecurityFlag());
    }

    @Test
    void processNewlySetCaseFlags_shouldSetFlagsToNoWhenMatchingFlagsAreInactiveOrAbsent() {
        caseFlagsService.setupCaseFlags(caseData);
        caseData.getClaimantFlags().setDetails(ListTypeItem.from(FlagDetailType.builder()
                .name(SIGN_LANGUAGE_INTERPRETER)
                .status(INACTIVE)
                .build()));
        caseData.getRespondentFlags().setDetails(ListTypeItem.from(FlagDetailType.builder()
                .name(VEXATIOUS_LITIGANT)
                .status(INACTIVE)
                .build()));
        caseData.getRespondentExternalFlags().setDetails(ListTypeItem.from(activeFlag("A different flag")));

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
        caseData.setGenericTseApplicationCollection(List.of(tseApplication(
                TSE_APP_RESTRICT_PUBLICITY,
                List.of(adminDecision("Granted - application approved"))
        )));

        caseFlagsService.setPrivateHearingFlag(caseData);

        assertEquals(YES, caseData.getPrivateHearingRequiredFlag());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("nonGrantedTseApplications")
    void setPrivateHearingFlag_shouldBeFalsy_whenNoRestrictPublicityTseApplicationIsGranted(
            String scenario, List<GenericTseApplicationTypeItem> applications) {
        caseData.setGenericTseApplicationCollection(applications);

        caseFlagsService.setPrivateHearingFlag(caseData);

        assertEquals(NO, caseData.getPrivateHearingRequiredFlag(), scenario);
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

    private static Stream<FlagSlot> allPartyFlagSlots() {
        return Stream.of(
                slot("claimant internal flags", CaseData::getClaimantFlags, CaseData::setClaimantFlags,
                        CLAIMANT_NAME, CLAIMANT, INTERNAL, -1),
                slot("claimant external flags", CaseData::getClaimantExternalFlags,
                        CaseData::setClaimantExternalFlags, CLAIMANT_NAME, CLAIMANT, EXTERNAL, -1),
                slot("respondent 1 internal flags", CaseData::getRespondentFlags, CaseData::setRespondentFlags,
                        respondentName(0), RESPONDENT1, INTERNAL, 0),
                slot("respondent 1 external flags", CaseData::getRespondentExternalFlags,
                        CaseData::setRespondentExternalFlags, respondentName(0), RESPONDENT1, EXTERNAL, 0),
                slot("respondent 2 internal flags", CaseData::getRespondent1Flags, CaseData::setRespondent1Flags,
                        respondentName(1), RESPONDENT2, INTERNAL, 1),
                slot("respondent 2 external flags", CaseData::getRespondent1ExternalFlags,
                        CaseData::setRespondent1ExternalFlags, respondentName(1), RESPONDENT2, EXTERNAL, 1),
                slot("respondent 3 internal flags", CaseData::getRespondent2Flags, CaseData::setRespondent2Flags,
                        respondentName(2), RESPONDENT3, INTERNAL, 2),
                slot("respondent 3 external flags", CaseData::getRespondent2ExternalFlags,
                        CaseData::setRespondent2ExternalFlags, respondentName(2), RESPONDENT3, EXTERNAL, 2),
                slot("respondent 4 internal flags", CaseData::getRespondent3Flags, CaseData::setRespondent3Flags,
                        respondentName(3), RESPONDENT4, INTERNAL, 3),
                slot("respondent 4 external flags", CaseData::getRespondent3ExternalFlags,
                        CaseData::setRespondent3ExternalFlags, respondentName(3), RESPONDENT4, EXTERNAL, 3),
                slot("respondent 5 internal flags", CaseData::getRespondent4Flags, CaseData::setRespondent4Flags,
                        respondentName(4), RESPONDENT5, INTERNAL, 4),
                slot("respondent 5 external flags", CaseData::getRespondent4ExternalFlags,
                        CaseData::setRespondent4ExternalFlags, respondentName(4), RESPONDENT5, EXTERNAL, 4),
                slot("respondent 6 internal flags", CaseData::getRespondent5Flags, CaseData::setRespondent5Flags,
                        respondentName(5), RESPONDENT6, INTERNAL, 5),
                slot("respondent 6 external flags", CaseData::getRespondent5ExternalFlags,
                        CaseData::setRespondent5ExternalFlags, respondentName(5), RESPONDENT6, EXTERNAL, 5),
                slot("respondent 7 internal flags", CaseData::getRespondent6Flags, CaseData::setRespondent6Flags,
                        respondentName(6), RESPONDENT7, INTERNAL, 6),
                slot("respondent 7 external flags", CaseData::getRespondent6ExternalFlags,
                        CaseData::setRespondent6ExternalFlags, respondentName(6), RESPONDENT7, EXTERNAL, 6),
                slot("respondent 8 internal flags", CaseData::getRespondent7Flags, CaseData::setRespondent7Flags,
                        respondentName(7), RESPONDENT8, INTERNAL, 7),
                slot("respondent 8 external flags", CaseData::getRespondent7ExternalFlags,
                        CaseData::setRespondent7ExternalFlags, respondentName(7), RESPONDENT8, EXTERNAL, 7),
                slot("respondent 9 internal flags", CaseData::getRespondent8Flags, CaseData::setRespondent8Flags,
                        respondentName(8), RESPONDENT9, INTERNAL, 8),
                slot("respondent 9 external flags", CaseData::getRespondent8ExternalFlags,
                        CaseData::setRespondent8ExternalFlags, respondentName(8), RESPONDENT9, EXTERNAL, 8),
                slot("respondent 10 internal flags", CaseData::getRespondent9Flags, CaseData::setRespondent9Flags,
                        respondentName(9), RESPONDENT10, INTERNAL, 9),
                slot("respondent 10 external flags", CaseData::getRespondent9ExternalFlags,
                        CaseData::setRespondent9ExternalFlags, respondentName(9), RESPONDENT10, EXTERNAL, 9)
        );
    }

    private static Stream<FlagSlot> respondentPartyFlagSlots() {
        return allPartyFlagSlots().filter(slot -> slot.respondentIndex >= 0);
    }

    private static Stream<Arguments> nonGrantedTseApplications() {
        return Stream.of(
                Arguments.of("different TSE application type",
                        List.of(tseApplication("Another application", List.of(adminDecision("Granted"))))),
                Arguments.of("missing admin decision",
                        List.of(tseApplication(TSE_APP_RESTRICT_PUBLICITY, null))),
                Arguments.of("missing decision value",
                        List.of(tseApplication(TSE_APP_RESTRICT_PUBLICITY, List.of(adminDecision(null))))),
                Arguments.of("refused restricted publicity decision",
                        List.of(tseApplication(TSE_APP_RESTRICT_PUBLICITY, List.of(adminDecision("Refused")))))
        );
    }

    private static FlagSlot slot(String name, Function<CaseData, CaseFlagsType> getter,
                                BiConsumer<CaseData, CaseFlagsType> setter, String partyName,
                                String roleOnCase, String visibility, int respondentIndex) {
        return new FlagSlot(name, getter, setter, partyName, roleOnCase, visibility, respondentIndex);
    }

    private static List<RespondentSumTypeItem> respondentCollection(int numberOfRespondents) {
        List<RespondentSumTypeItem> respondentCollection = new ArrayList<>();
        for (int i = 0; i < numberOfRespondents; i++) {
            RespondentSumTypeItem respondentSumTypeItem = new RespondentSumTypeItem();
            RespondentSumType respondentSumType = new RespondentSumType();
            respondentSumType.setRespondentName(respondentName(i));
            respondentSumTypeItem.setId(String.valueOf(i + 1));
            respondentSumTypeItem.setValue(respondentSumType);
            respondentCollection.add(respondentSumTypeItem);
        }
        return respondentCollection;
    }

    private static String respondentName(int index) {
        return index == 0 ? RESPONDENT_NAME : RESPONDENT_NAME + " " + (index + 1);
    }

    private static void assertCaseFlag(FlagSlot slot, CaseFlagsType actual) {
        assertNotNull(actual, slot + " should be populated");
        assertAll(slot.toString(),
                () -> assertEquals(slot.partyName, actual.getPartyName()),
                () -> assertEquals(slot.roleOnCase, actual.getRoleOnCase()),
                () -> assertEquals(slot.roleOnCase, actual.getGroupId()),
                () -> assertEquals(slot.visibility, actual.getVisibility())
        );
    }

    private static FlagDetailType activeFlag(String name) {
        return FlagDetailType.builder()
                .name(name)
                .status(ACTIVE)
                .build();
    }

    private static GenericTseApplicationTypeItem tseApplication(
            String type, List<TseAdminRecordDecisionTypeItem> adminDecisions) {
        return GenericTseApplicationTypeItem.builder()
                .value(GenericTseApplicationType.builder()
                        .type(type)
                        .adminDecision(adminDecisions)
                        .build())
                .build();
    }

    private static TseAdminRecordDecisionTypeItem adminDecision(String decision) {
        return TseAdminRecordDecisionTypeItem.builder()
                .value(TseAdminRecordDecisionType.builder()
                        .decision(decision)
                        .build())
                .build();
    }

    private static final class FlagSlot {
        private final String name;
        private final Function<CaseData, CaseFlagsType> getter;
        private final BiConsumer<CaseData, CaseFlagsType> setter;
        private final String partyName;
        private final String roleOnCase;
        private final String visibility;
        private final int respondentIndex;

        private FlagSlot(String name, Function<CaseData, CaseFlagsType> getter,
                         BiConsumer<CaseData, CaseFlagsType> setter, String partyName,
                         String roleOnCase, String visibility, int respondentIndex) {
            this.name = name;
            this.getter = getter;
            this.setter = setter;
            this.partyName = partyName;
            this.roleOnCase = roleOnCase;
            this.visibility = visibility;
            this.respondentIndex = respondentIndex;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}

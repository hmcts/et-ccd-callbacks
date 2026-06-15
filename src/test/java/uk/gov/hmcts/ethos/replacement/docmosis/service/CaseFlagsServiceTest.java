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
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.TseAdminRecordDecisionTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.CaseFlagsType;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;
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
import static uk.gov.hmcts.ethos.replacement.docmosis.service.CaseFlagsService.CLAIMANT_REPRESENTATIVE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.CaseFlagsService.EXTERNAL;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.CaseFlagsService.INTERNAL;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.CaseFlagsService.REPRESENTATIVE1;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.CaseFlagsService.REPRESENTATIVE10;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.CaseFlagsService.REPRESENTATIVE2;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.CaseFlagsService.REPRESENTATIVE3;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.CaseFlagsService.REPRESENTATIVE4;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.CaseFlagsService.REPRESENTATIVE5;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.CaseFlagsService.REPRESENTATIVE6;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.CaseFlagsService.REPRESENTATIVE7;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.CaseFlagsService.REPRESENTATIVE8;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.CaseFlagsService.REPRESENTATIVE9;
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
    public static final String CLAIMANT_REPRESENTATIVE_NAME = "Claimant Representative Name";
    public static final String REPRESENTATIVE_NAME = "Representative Name";
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
        caseData.setRepresentativeClaimantType(RepresentedTypeC.builder()
                .nameOfRepresentative(CLAIMANT_REPRESENTATIVE_NAME)
                .build());
        caseData.setRepCollection(representativeCollection(10));
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
    void caseFlagsSetupRequired_shouldNotRequireRespondentFlagsWhenRespondentCollectionIsNull() {
        caseData.setRespondentCollection(null);

        caseFlagsService.setupCaseFlags(caseData);

        assertFalse(caseFlagsService.caseFlagsSetupRequired(caseData));
        respondentPartyFlagSlots()
                .forEach(slot -> assertNull(slot.getter.apply(caseData), slot + " should not be populated"));
    }

    @Test
    void caseFlagsSetupRequired_shouldNotRequireRespondentRepresentativeFlagsWhenThereAreNoRepresentatives() {
        caseData.setRepCollection(null);

        caseFlagsService.setupCaseFlags(caseData);

        assertFalse(caseFlagsService.caseFlagsSetupRequired(caseData));
        respondentRepresentativePartyFlagSlots()
                .forEach(slot -> assertNull(slot.getter.apply(caseData), slot + " should not be populated"));
    }

    @Test
    void caseFlagsSetupRequired_shouldRequireClaimantRepresentativeFlagsEvenWhenClaimantRepresentativeIsAbsent() {
        caseData.setRepresentativeClaimantType(null);

        caseFlagsService.setupCaseFlags(caseData);

        assertTrue(caseFlagsService.caseFlagsSetupRequired(caseData));
        assertNull(caseData.getClaimantRepresentativeFlags());
        assertNull(caseData.getClaimantRepresentativeExternalFlags());
    }

    @Test
    void setupCaseFlags_setsShellCaseFlagsForClaimantRespondentAndRepresentativeParties() {
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

    @Test
    void setupCaseFlags_shouldOnlyCreateRespondentRepresentativeFlagsForExistingRepresentatives() {
        caseData.setRepCollection(representativeCollection(3));

        caseFlagsService.setupCaseFlags(caseData);

        respondentRepresentativePartyFlagSlots().forEach(slot -> {
            if (slot.representativeIndex < 3) {
                assertNotNull(slot.getter.apply(caseData), slot + " should be populated");
            } else {
                assertNull(slot.getter.apply(caseData), slot + " should not be populated");
            }
        });
    }

    @Test
    void setupCaseFlags_shouldNotCreateClaimantRepresentativeFlagsWhenClaimantRepresentativeIsAbsent() {
        caseData.setRepresentativeClaimantType(null);

        caseFlagsService.setupCaseFlags(caseData);

        assertNull(caseData.getClaimantRepresentativeFlags());
        assertNull(caseData.getClaimantRepresentativeExternalFlags());
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
    void setupCaseFlags_shouldUpdateClaimantRepresentativeFlagsWhenNameChanges() {
        caseFlagsService.setupCaseFlags(caseData);
        String updatedRepresentativeName = "Updated Claimant Representative Name";
        caseData.getRepresentativeClaimantType().setNameOfRepresentative(updatedRepresentativeName);

        caseFlagsService.setupCaseFlags(caseData);

        assertEquals(updatedRepresentativeName, caseData.getClaimantRepresentativeFlags().getPartyName());
        assertEquals(CLAIMANT_REPRESENTATIVE, caseData.getClaimantRepresentativeFlags().getRoleOnCase());
        assertEquals(CLAIMANT_REPRESENTATIVE, caseData.getClaimantRepresentativeFlags().getGroupId());
        assertEquals(INTERNAL, caseData.getClaimantRepresentativeFlags().getVisibility());

        assertEquals(updatedRepresentativeName, caseData.getClaimantRepresentativeExternalFlags().getPartyName());
        assertEquals(CLAIMANT_REPRESENTATIVE, caseData.getClaimantRepresentativeExternalFlags().getRoleOnCase());
        assertEquals(CLAIMANT_REPRESENTATIVE, caseData.getClaimantRepresentativeExternalFlags().getGroupId());
        assertEquals(EXTERNAL, caseData.getClaimantRepresentativeExternalFlags().getVisibility());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("respondentRepresentativePartyFlagSlots")
    void setupCaseFlags_shouldUpdateRespondentRepresentativeFlagsWhenNameChanges(FlagSlot slot) {
        caseFlagsService.setupCaseFlags(caseData);
        String updatedRepresentativeName = "Updated " + slot;
        caseData.getRepCollection()
                .get(slot.representativeIndex)
                .getValue()
                .setNameOfRepresentative(updatedRepresentativeName);

        caseFlagsService.setupCaseFlags(caseData);

        assertEquals(updatedRepresentativeName, slot.getter.apply(caseData).getPartyName());
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
        caseData.getClaimantRepresentativeExternalFlags()
                .setDetails(ListTypeItem.from(activeFlag(LANGUAGE_INTERPRETER)));
        caseData.getRepresentative9ExternalFlags().setDetails(ListTypeItem.from(activeFlag(DISRUPTIVE_CUSTOMER)));

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
    void processNewlySetCaseFlags_shouldSetFlagsToNoWhenPartyFlagCollectionsAreMissing() {
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
                slot("claimant representative internal flags", CaseData::getClaimantRepresentativeFlags,
                        CaseData::setClaimantRepresentativeFlags, CLAIMANT_REPRESENTATIVE_NAME,
                        CLAIMANT_REPRESENTATIVE, INTERNAL, -1),
                slot("claimant representative external flags", CaseData::getClaimantRepresentativeExternalFlags,
                        CaseData::setClaimantRepresentativeExternalFlags, CLAIMANT_REPRESENTATIVE_NAME,
                        CLAIMANT_REPRESENTATIVE, EXTERNAL, -1),
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
                        CaseData::setRespondent9ExternalFlags, respondentName(9), RESPONDENT10, EXTERNAL, 9),
                representativeSlot("representative 1 internal flags", CaseData::getRepresentativeFlags,
                        CaseData::setRepresentativeFlags, representativeName(0), REPRESENTATIVE1, INTERNAL, 0),
                representativeSlot("representative 1 external flags", CaseData::getRepresentativeExternalFlags,
                        CaseData::setRepresentativeExternalFlags, representativeName(0), REPRESENTATIVE1, EXTERNAL, 0),
                representativeSlot("representative 2 internal flags", CaseData::getRepresentative1Flags,
                        CaseData::setRepresentative1Flags, representativeName(1), REPRESENTATIVE2, INTERNAL, 1),
                representativeSlot("representative 2 external flags", CaseData::getRepresentative1ExternalFlags,
                        CaseData::setRepresentative1ExternalFlags, representativeName(1), REPRESENTATIVE2,
                        EXTERNAL, 1),
                representativeSlot("representative 3 internal flags", CaseData::getRepresentative2Flags,
                        CaseData::setRepresentative2Flags, representativeName(2), REPRESENTATIVE3, INTERNAL, 2),
                representativeSlot("representative 3 external flags", CaseData::getRepresentative2ExternalFlags,
                        CaseData::setRepresentative2ExternalFlags, representativeName(2), REPRESENTATIVE3,
                        EXTERNAL, 2),
                representativeSlot("representative 4 internal flags", CaseData::getRepresentative3Flags,
                        CaseData::setRepresentative3Flags, representativeName(3), REPRESENTATIVE4, INTERNAL, 3),
                representativeSlot("representative 4 external flags", CaseData::getRepresentative3ExternalFlags,
                        CaseData::setRepresentative3ExternalFlags, representativeName(3), REPRESENTATIVE4,
                        EXTERNAL, 3),
                representativeSlot("representative 5 internal flags", CaseData::getRepresentative4Flags,
                        CaseData::setRepresentative4Flags, representativeName(4), REPRESENTATIVE5, INTERNAL, 4),
                representativeSlot("representative 5 external flags", CaseData::getRepresentative4ExternalFlags,
                        CaseData::setRepresentative4ExternalFlags, representativeName(4), REPRESENTATIVE5,
                        EXTERNAL, 4),
                representativeSlot("representative 6 internal flags", CaseData::getRepresentative5Flags,
                        CaseData::setRepresentative5Flags, representativeName(5), REPRESENTATIVE6, INTERNAL, 5),
                representativeSlot("representative 6 external flags", CaseData::getRepresentative5ExternalFlags,
                        CaseData::setRepresentative5ExternalFlags, representativeName(5), REPRESENTATIVE6,
                        EXTERNAL, 5),
                representativeSlot("representative 7 internal flags", CaseData::getRepresentative6Flags,
                        CaseData::setRepresentative6Flags, representativeName(6), REPRESENTATIVE7, INTERNAL, 6),
                representativeSlot("representative 7 external flags", CaseData::getRepresentative6ExternalFlags,
                        CaseData::setRepresentative6ExternalFlags, representativeName(6), REPRESENTATIVE7,
                        EXTERNAL, 6),
                representativeSlot("representative 8 internal flags", CaseData::getRepresentative7Flags,
                        CaseData::setRepresentative7Flags, representativeName(7), REPRESENTATIVE8, INTERNAL, 7),
                representativeSlot("representative 8 external flags", CaseData::getRepresentative7ExternalFlags,
                        CaseData::setRepresentative7ExternalFlags, representativeName(7), REPRESENTATIVE8,
                        EXTERNAL, 7),
                representativeSlot("representative 9 internal flags", CaseData::getRepresentative8Flags,
                        CaseData::setRepresentative8Flags, representativeName(8), REPRESENTATIVE9, INTERNAL, 8),
                representativeSlot("representative 9 external flags", CaseData::getRepresentative8ExternalFlags,
                        CaseData::setRepresentative8ExternalFlags, representativeName(8), REPRESENTATIVE9,
                        EXTERNAL, 8),
                representativeSlot("representative 10 internal flags", CaseData::getRepresentative9Flags,
                        CaseData::setRepresentative9Flags, representativeName(9), REPRESENTATIVE10, INTERNAL, 9),
                representativeSlot("representative 10 external flags", CaseData::getRepresentative9ExternalFlags,
                        CaseData::setRepresentative9ExternalFlags, representativeName(9), REPRESENTATIVE10,
                        EXTERNAL, 9)
        );
    }

    private static Stream<FlagSlot> respondentPartyFlagSlots() {
        return allPartyFlagSlots().filter(slot -> slot.respondentIndex >= 0);
    }

    private static Stream<FlagSlot> respondentRepresentativePartyFlagSlots() {
        return allPartyFlagSlots().filter(slot -> slot.representativeIndex >= 0);
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
        return new FlagSlot(name, getter, setter, partyName, roleOnCase, visibility, respondentIndex, -1);
    }

    private static FlagSlot representativeSlot(String name, Function<CaseData, CaseFlagsType> getter,
                                               BiConsumer<CaseData, CaseFlagsType> setter, String partyName,
                                               String roleOnCase, String visibility, int representativeIndex) {
        return new FlagSlot(name, getter, setter, partyName, roleOnCase, visibility, -1, representativeIndex);
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

    private static List<RepresentedTypeRItem> representativeCollection(int numberOfRepresentatives) {
        List<RepresentedTypeRItem> representativeCollection = new ArrayList<>();
        for (int i = 0; i < numberOfRepresentatives; i++) {
            RepresentedTypeR representative = RepresentedTypeR.builder()
                    .nameOfRepresentative(representativeName(i))
                    .build();
            RepresentedTypeRItem representativeItem = new RepresentedTypeRItem();
            representativeItem.setId(String.valueOf(i + 1));
            representativeItem.setValue(representative);
            representativeCollection.add(representativeItem);
        }
        return representativeCollection;
    }

    private static String respondentName(int index) {
        return index == 0 ? RESPONDENT_NAME : RESPONDENT_NAME + " " + (index + 1);
    }

    private static String representativeName(int index) {
        return index == 0 ? REPRESENTATIVE_NAME : REPRESENTATIVE_NAME + " " + (index + 1);
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
        private final int representativeIndex;

        private FlagSlot(String name, Function<CaseData, CaseFlagsType> getter,
                         BiConsumer<CaseData, CaseFlagsType> setter, String partyName,
                         String roleOnCase, String visibility, int respondentIndex, int representativeIndex) {
            this.name = name;
            this.getter = getter;
            this.setter = setter;
            this.partyName = partyName;
            this.roleOnCase = roleOnCase;
            this.visibility = visibility;
            this.respondentIndex = respondentIndex;
            this.representativeIndex = representativeIndex;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}

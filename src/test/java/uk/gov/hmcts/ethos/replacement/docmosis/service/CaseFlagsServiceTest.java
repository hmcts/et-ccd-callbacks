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
import uk.gov.hmcts.et.common.model.ccd.types.AllPartyFlags;
import uk.gov.hmcts.et.common.model.ccd.types.CaseFlagsType;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.et.common.model.ccd.types.RestrictedReportingType;
import uk.gov.hmcts.et.common.model.ccd.types.TseAdminRecordDecisionType;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
import static uk.gov.hmcts.ecm.common.model.helper.CaseFlagConstants.CLAIMANT;
import static uk.gov.hmcts.ecm.common.model.helper.CaseFlagConstants.CLAIMANT_REPRESENTATIVE;
import static uk.gov.hmcts.ecm.common.model.helper.CaseFlagConstants.DISRUPTIVE_CUSTOMER;
import static uk.gov.hmcts.ecm.common.model.helper.CaseFlagConstants.EXTERNAL;
import static uk.gov.hmcts.ecm.common.model.helper.CaseFlagConstants.INACTIVE;
import static uk.gov.hmcts.ecm.common.model.helper.CaseFlagConstants.INTERNAL;
import static uk.gov.hmcts.ecm.common.model.helper.CaseFlagConstants.LANGUAGE_INTERPRETER;
import static uk.gov.hmcts.ecm.common.model.helper.CaseFlagConstants.REPRESENTATIVE1;
import static uk.gov.hmcts.ecm.common.model.helper.CaseFlagConstants.REPRESENTATIVE10;
import static uk.gov.hmcts.ecm.common.model.helper.CaseFlagConstants.REPRESENTATIVE2;
import static uk.gov.hmcts.ecm.common.model.helper.CaseFlagConstants.REPRESENTATIVE3;
import static uk.gov.hmcts.ecm.common.model.helper.CaseFlagConstants.REPRESENTATIVE4;
import static uk.gov.hmcts.ecm.common.model.helper.CaseFlagConstants.REPRESENTATIVE5;
import static uk.gov.hmcts.ecm.common.model.helper.CaseFlagConstants.REPRESENTATIVE6;
import static uk.gov.hmcts.ecm.common.model.helper.CaseFlagConstants.REPRESENTATIVE7;
import static uk.gov.hmcts.ecm.common.model.helper.CaseFlagConstants.REPRESENTATIVE8;
import static uk.gov.hmcts.ecm.common.model.helper.CaseFlagConstants.REPRESENTATIVE9;
import static uk.gov.hmcts.ecm.common.model.helper.CaseFlagConstants.RESPONDENT1;
import static uk.gov.hmcts.ecm.common.model.helper.CaseFlagConstants.RESPONDENT10;
import static uk.gov.hmcts.ecm.common.model.helper.CaseFlagConstants.RESPONDENT2;
import static uk.gov.hmcts.ecm.common.model.helper.CaseFlagConstants.RESPONDENT3;
import static uk.gov.hmcts.ecm.common.model.helper.CaseFlagConstants.RESPONDENT4;
import static uk.gov.hmcts.ecm.common.model.helper.CaseFlagConstants.RESPONDENT5;
import static uk.gov.hmcts.ecm.common.model.helper.CaseFlagConstants.RESPONDENT6;
import static uk.gov.hmcts.ecm.common.model.helper.CaseFlagConstants.RESPONDENT7;
import static uk.gov.hmcts.ecm.common.model.helper.CaseFlagConstants.RESPONDENT8;
import static uk.gov.hmcts.ecm.common.model.helper.CaseFlagConstants.RESPONDENT9;
import static uk.gov.hmcts.ecm.common.model.helper.CaseFlagConstants.SIGN_LANGUAGE_INTERPRETER;
import static uk.gov.hmcts.ecm.common.model.helper.CaseFlagConstants.VEXATIOUS_LITIGANT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.TSE_APP_RESTRICT_PUBLICITY;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

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
        allPartyFlags(caseData).setCaseFlags(null);

        assertTrue(caseFlagsService.caseFlagsSetupRequired(caseData));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("allPartyFlagSlots")
    void caseFlagsSetupRequired_shouldBeTrueWhenAnyRequiredPartyFlagIsMissing(FlagSlot slot) {
        caseFlagsService.setupCaseFlags(caseData);

        slot.set(caseData, null);

        assertTrue(caseFlagsService.caseFlagsSetupRequired(caseData));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("allPartyFlagSlots")
    void caseFlagsSetupRequired_shouldBeTrueWhenAnyRequiredPartyFlagRoleIsMissing(FlagSlot slot) {
        caseFlagsService.setupCaseFlags(caseData);
        Objects.requireNonNull(slot.get(caseData)).setRoleOnCase("");

        assertTrue(caseFlagsService.caseFlagsSetupRequired(caseData));
    }

    @Test
    void caseFlagsSetupRequired_shouldNotRequireRespondentFlagsWhenThereAreNoRespondents() {
        caseData.setRespondentCollection(new ArrayList<>());

        caseFlagsService.setupCaseFlags(caseData);

        assertFalse(caseFlagsService.caseFlagsSetupRequired(caseData));
        assertNull(allPartyFlags(caseData).getRespondentFlags());
        assertNull(allPartyFlags(caseData).getRespondentExternalFlags());
    }

    @Test
    void caseFlagsSetupRequired_shouldNotRequireRespondentFlagsWhenRespondentCollectionIsNull() {
        caseData.setRespondentCollection(null);

        caseFlagsService.setupCaseFlags(caseData);

        assertFalse(caseFlagsService.caseFlagsSetupRequired(caseData));
        respondentPartyFlagSlots()
                .forEach(slot -> assertNull(slot.get(caseData), slot + " should not be populated"));
    }

    @Test
    void caseFlagsSetupRequired_shouldNotRequireRespondentRepresentativeFlagsWhenThereAreNoRepresentatives() {
        caseData.setRepCollection(null);

        caseFlagsService.setupCaseFlags(caseData);

        assertFalse(caseFlagsService.caseFlagsSetupRequired(caseData));
        respondentRepresentativePartyFlagSlots()
                .forEach(slot -> assertNull(slot.get(caseData), slot + " should not be populated"));
    }

    @Test
    void caseFlagsSetupRequired_shouldRequireClaimantRepresentativeFlagsEvenWhenClaimantRepresentativeIsAbsent() {
        caseData.setRepresentativeClaimantType(null);

        caseFlagsService.setupCaseFlags(caseData);

        assertTrue(caseFlagsService.caseFlagsSetupRequired(caseData));
        assertNull(allPartyFlags(caseData).getClaimantRepresentativeFlags());
        assertNull(allPartyFlags(caseData).getClaimantRepresentativeExternalFlags());
    }

    @Test
    void setupCaseFlags_setsShellCaseFlagsForClaimantRespondentAndRepresentativeParties() {
        caseFlagsService.setupCaseFlags(caseData);

        CaseFlagsType caseFlags = allPartyFlags(caseData).getCaseFlags();
        assertThat(caseFlags.getPartyName(), is(nullValue()));
        assertThat(caseFlags.getRoleOnCase(), is(nullValue()));

        allPartyFlagSlots().forEach(slot -> assertCaseFlag(slot, slot.get(caseData)));
    }

    @Test
    void setupCaseFlags_shouldCreateAllPartyFlagsHolderWhenAbsent() {
        assertNull(caseData.getAllPartyFlags());

        caseFlagsService.setupCaseFlags(caseData);

        assertNotNull(caseData.getAllPartyFlags());
        assertNotNull(allPartyFlags(caseData).getCaseFlags());
        assertNotNull(allPartyFlags(caseData).getClaimantFlags());
        assertNotNull(allPartyFlags(caseData).getClaimantExternalFlags());
    }

    @Test
    void setupCaseFlags_shouldOnlyCreateRespondentFlagsForExistingRespondents() {
        caseData.setRespondentCollection(respondentCollection(3));

        caseFlagsService.setupCaseFlags(caseData);

        respondentPartyFlagSlots().forEach(slot -> {
            if (slot.respondentIndex < 3) {
                assertNotNull(slot.get(caseData), slot + " should be populated");
            } else {
                assertNull(slot.get(caseData), slot + " should not be populated");
            }
        });
    }

    @Test
    void setupCaseFlags_shouldOnlyCreateRespondentRepresentativeFlagsForExistingRepresentatives() {
        caseData.setRepCollection(representativeCollection(3));

        caseFlagsService.setupCaseFlags(caseData);

        respondentRepresentativePartyFlagSlots().forEach(slot -> {
            if (slot.representativeIndex < 3) {
                assertNotNull(slot.get(caseData), slot + " should be populated");
            } else {
                assertNull(slot.get(caseData), slot + " should not be populated");
            }
        });
    }

    @Test
    void setupCaseFlags_shouldNotCreateClaimantRepresentativeFlagsWhenClaimantRepresentativeIsAbsent() {
        caseData.setRepresentativeClaimantType(null);

        caseFlagsService.setupCaseFlags(caseData);

        assertNull(allPartyFlags(caseData).getClaimantRepresentativeFlags());
        assertNull(allPartyFlags(caseData).getClaimantRepresentativeExternalFlags());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("allPartyFlagSlots")
    void setupCaseFlags_shouldRecreatePartyFlagsWhenRoleIsMissing(FlagSlot slot) {
        caseFlagsService.setupCaseFlags(caseData);
        slot.set(caseData, CaseFlagsType.builder()
                .partyName("Old name")
                .roleOnCase("")
                .groupId("old-group")
                .visibility("old-visibility")
                .build());

        caseFlagsService.setupCaseFlags(caseData);

        assertCaseFlag(slot, slot.get(caseData));
    }

    @Test
    void rollbackCaseFlags_shouldSetToNull() {
        caseFlagsService.setupCaseFlags(caseData);
        caseFlagsService.rollbackCaseFlags(caseData);

        assertThat(allPartyFlags(caseData).getCaseFlags(), is(nullValue()));
        assertThat(allPartyFlags(caseData).getClaimantFlags(), is(nullValue()));
        assertThat(allPartyFlags(caseData).getRespondentFlags(), is(nullValue()));
        assertThat(allPartyFlags(caseData).getClaimantExternalFlags(), is(nullValue()));
        assertThat(allPartyFlags(caseData).getRespondentExternalFlags(), is(nullValue()));
        allPartyFlagSlots().forEach(slot -> assertNull(slot.get(caseData), slot + " should be cleared"));
    }

    @Test
    void rollbackCaseFlags_shouldNotCreateAllPartyFlagsWhenAbsent() {
        caseFlagsService.rollbackCaseFlags(caseData);

        assertNull(caseData.getAllPartyFlags());
    }

    @Test
    void setupCaseFlags_shouldUpdateClaimantFlagsWhenNameChanges() {
        caseFlagsService.setupCaseFlags(caseData);
        String updatedClaimantName = "Updated Claimant Name";
        caseData.setClaimant(updatedClaimantName);

        caseFlagsService.setupCaseFlags(caseData);

        CaseFlagsType claimantFlags = allPartyFlags(caseData).getClaimantFlags();
        assertEquals(updatedClaimantName, claimantFlags.getPartyName());
        assertEquals(CLAIMANT, claimantFlags.getRoleOnCase());
        assertEquals(CLAIMANT, claimantFlags.getGroupId());
        assertEquals(INTERNAL, claimantFlags.getVisibility());

        CaseFlagsType claimantExternalFlags = allPartyFlags(caseData).getClaimantExternalFlags();
        assertEquals(updatedClaimantName, claimantExternalFlags.getPartyName());
        assertEquals(CLAIMANT, claimantExternalFlags.getRoleOnCase());
        assertEquals(CLAIMANT, claimantExternalFlags.getGroupId());
        assertEquals(EXTERNAL, claimantExternalFlags.getVisibility());
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

        assertEquals(updatedRespondentName, Objects.requireNonNull(slot.get(caseData)).getPartyName());
        assertEquals(slot.roleOnCase, Objects.requireNonNull(slot.get(caseData)).getRoleOnCase());
        assertEquals(slot.roleOnCase, Objects.requireNonNull(slot.get(caseData)).getGroupId());
        assertEquals(slot.visibility, Objects.requireNonNull(slot.get(caseData)).getVisibility());
    }

    @Test
    void setupCaseFlags_shouldUpdateClaimantRepresentativeFlagsWhenNameChanges() {
        caseFlagsService.setupCaseFlags(caseData);
        String updatedRepresentativeName = "Updated Claimant Representative Name";
        caseData.getRepresentativeClaimantType().setNameOfRepresentative(updatedRepresentativeName);

        caseFlagsService.setupCaseFlags(caseData);

        CaseFlagsType claimantRepresentativeFlags = allPartyFlags(caseData).getClaimantRepresentativeFlags();
        assertEquals(updatedRepresentativeName, claimantRepresentativeFlags.getPartyName());
        assertEquals(CLAIMANT_REPRESENTATIVE, claimantRepresentativeFlags.getRoleOnCase());
        assertEquals(CLAIMANT_REPRESENTATIVE, claimantRepresentativeFlags.getGroupId());
        assertEquals(INTERNAL, claimantRepresentativeFlags.getVisibility());

        CaseFlagsType claimantRepresentativeExternalFlags =
                allPartyFlags(caseData).getClaimantRepresentativeExternalFlags();
        assertEquals(updatedRepresentativeName, claimantRepresentativeExternalFlags.getPartyName());
        assertEquals(CLAIMANT_REPRESENTATIVE, claimantRepresentativeExternalFlags.getRoleOnCase());
        assertEquals(CLAIMANT_REPRESENTATIVE, claimantRepresentativeExternalFlags.getGroupId());
        assertEquals(EXTERNAL, claimantRepresentativeExternalFlags.getVisibility());
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

        assertEquals(updatedRepresentativeName, Objects.requireNonNull(slot.get(caseData)).getPartyName());
        assertEquals(slot.roleOnCase, Objects.requireNonNull(slot.get(caseData)).getRoleOnCase());
        assertEquals(slot.roleOnCase, Objects.requireNonNull(slot.get(caseData)).getGroupId());
        assertEquals(slot.visibility, Objects.requireNonNull(slot.get(caseData)).getVisibility());
    }

    @Test
    void processNewlySetCaseFlags_shouldSetInterpreterRequiredTrue() {
        getOrCreateAllPartyFlags(caseData).setRespondentFlags(CaseFlagsType.builder().build());
        getOrCreateAllPartyFlags(caseData).setClaimantFlags(CaseFlagsType.builder()
                .details(
                        ListTypeItem.from(
                                FlagDetailType.builder()
                                        .name(SIGN_LANGUAGE_INTERPRETER)
                                        .status(ACTIVE)
                                        .flagCode("RA00010")
                                        .build()))
                .build());
        getOrCreateAllPartyFlags(caseData).setClaimantExternalFlags(CaseFlagsType.builder().build());

        caseFlagsService.processNewlySetCaseFlags(caseData);

        assertEquals(YES, caseData.getCaseInterpreterRequiredFlag());
    }

    @Test
    void processNewlySetCaseFlags_shouldSetAdditionalSecurityFlagTrue() {
        getOrCreateAllPartyFlags(caseData).setRespondentFlags(CaseFlagsType.builder().build());
        getOrCreateAllPartyFlags(caseData).setClaimantFlags(CaseFlagsType.builder()
                .details(
                        ListTypeItem.from(
                                FlagDetailType.builder()
                                        .name(VEXATIOUS_LITIGANT)
                                        .status(ACTIVE)
                                        .flagCode("CA00010")
                                        .build()))
                .build());
        getOrCreateAllPartyFlags(caseData).setClaimantExternalFlags(CaseFlagsType.builder().build());

        caseFlagsService.processNewlySetCaseFlags(caseData);

        assertEquals(YES, caseData.getCaseAdditionalSecurityFlag());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("allPartyFlagSlots")
    void processNewlySetCaseFlags_shouldReadInterpreterFlagsFromAnyPartyFlagCollection(FlagSlot slot) {
        caseFlagsService.setupCaseFlags(caseData);
        Objects.requireNonNull(slot.get(caseData)).setDetails(ListTypeItem.from(activeFlag(SIGN_LANGUAGE_INTERPRETER)));

        caseFlagsService.processNewlySetCaseFlags(caseData);

        assertEquals(YES, caseData.getCaseInterpreterRequiredFlag());
        assertEquals(NO, caseData.getCaseAdditionalSecurityFlag());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("allPartyFlagSlots")
    void processNewlySetCaseFlags_shouldReadSecurityFlagsFromAnyPartyFlagCollection(FlagSlot slot) {
        caseFlagsService.setupCaseFlags(caseData);
        Objects.requireNonNull(slot.get(caseData)).setDetails(ListTypeItem.from(activeFlag(VEXATIOUS_LITIGANT)));

        caseFlagsService.processNewlySetCaseFlags(caseData);

        assertEquals(NO, caseData.getCaseInterpreterRequiredFlag());
        assertEquals(YES, caseData.getCaseAdditionalSecurityFlag());
    }

    @Test
    void processNewlySetCaseFlags_shouldSupportAlternativeInterpreterAndSecurityFlagNames() {
        caseFlagsService.setupCaseFlags(caseData);
        allPartyFlags(caseData).getClaimantRepresentativeExternalFlags()
                .setDetails(ListTypeItem.from(activeFlag(LANGUAGE_INTERPRETER)));
        allPartyFlags(caseData).getRepresentative9ExternalFlags()
                .setDetails(ListTypeItem.from(activeFlag(DISRUPTIVE_CUSTOMER)));

        caseFlagsService.processNewlySetCaseFlags(caseData);

        assertEquals(YES, caseData.getCaseInterpreterRequiredFlag());
        assertEquals(YES, caseData.getCaseAdditionalSecurityFlag());
    }

    @Test
    void processNewlySetCaseFlags_shouldSetFlagsToNoWhenMatchingFlagsAreInactiveOrAbsent() {
        caseFlagsService.setupCaseFlags(caseData);
        allPartyFlags(caseData).getClaimantFlags().setDetails(ListTypeItem.from(FlagDetailType.builder()
                .name(SIGN_LANGUAGE_INTERPRETER)
                .status(INACTIVE)
                .build()));
        allPartyFlags(caseData).getRespondentFlags().setDetails(ListTypeItem.from(FlagDetailType.builder()
                .name(VEXATIOUS_LITIGANT)
                .status(INACTIVE)
                .build()));
        allPartyFlags(caseData).getRespondentExternalFlags()
                .setDetails(ListTypeItem.from(activeFlag("A different flag")));

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
                slot("claimant internal flags", AllPartyFlags::getClaimantFlags, AllPartyFlags::setClaimantFlags,
                        CLAIMANT_NAME, CLAIMANT, INTERNAL, -1),
                slot("claimant external flags", AllPartyFlags::getClaimantExternalFlags,
                        AllPartyFlags::setClaimantExternalFlags, CLAIMANT_NAME, CLAIMANT, EXTERNAL, -1),
                slot("claimant representative internal flags", AllPartyFlags::getClaimantRepresentativeFlags,
                        AllPartyFlags::setClaimantRepresentativeFlags, CLAIMANT_REPRESENTATIVE_NAME,
                        CLAIMANT_REPRESENTATIVE, INTERNAL, -1),
                slot("claimant representative external flags", AllPartyFlags::getClaimantRepresentativeExternalFlags,
                        AllPartyFlags::setClaimantRepresentativeExternalFlags, CLAIMANT_REPRESENTATIVE_NAME,
                        CLAIMANT_REPRESENTATIVE, EXTERNAL, -1),
                slot("respondent 1 internal flags", AllPartyFlags::getRespondentFlags,
                        AllPartyFlags::setRespondentFlags,
                        respondentName(0), RESPONDENT1, INTERNAL, 0),
                slot("respondent 1 external flags", AllPartyFlags::getRespondentExternalFlags,
                        AllPartyFlags::setRespondentExternalFlags, respondentName(0), RESPONDENT1, EXTERNAL, 0),
                slot("respondent 2 internal flags", AllPartyFlags::getRespondent1Flags,
                        AllPartyFlags::setRespondent1Flags,
                        respondentName(1), RESPONDENT2, INTERNAL, 1),
                slot("respondent 2 external flags", AllPartyFlags::getRespondent1ExternalFlags,
                        AllPartyFlags::setRespondent1ExternalFlags, respondentName(1), RESPONDENT2, EXTERNAL, 1),
                slot("respondent 3 internal flags", AllPartyFlags::getRespondent2Flags,
                        AllPartyFlags::setRespondent2Flags,
                        respondentName(2), RESPONDENT3, INTERNAL, 2),
                slot("respondent 3 external flags", AllPartyFlags::getRespondent2ExternalFlags,
                        AllPartyFlags::setRespondent2ExternalFlags, respondentName(2), RESPONDENT3, EXTERNAL, 2),
                slot("respondent 4 internal flags", AllPartyFlags::getRespondent3Flags,
                        AllPartyFlags::setRespondent3Flags,
                        respondentName(3), RESPONDENT4, INTERNAL, 3),
                slot("respondent 4 external flags", AllPartyFlags::getRespondent3ExternalFlags,
                        AllPartyFlags::setRespondent3ExternalFlags, respondentName(3), RESPONDENT4, EXTERNAL, 3),
                slot("respondent 5 internal flags", AllPartyFlags::getRespondent4Flags,
                        AllPartyFlags::setRespondent4Flags,
                        respondentName(4), RESPONDENT5, INTERNAL, 4),
                slot("respondent 5 external flags", AllPartyFlags::getRespondent4ExternalFlags,
                        AllPartyFlags::setRespondent4ExternalFlags, respondentName(4), RESPONDENT5, EXTERNAL, 4),
                slot("respondent 6 internal flags", AllPartyFlags::getRespondent5Flags,
                        AllPartyFlags::setRespondent5Flags,
                        respondentName(5), RESPONDENT6, INTERNAL, 5),
                slot("respondent 6 external flags", AllPartyFlags::getRespondent5ExternalFlags,
                        AllPartyFlags::setRespondent5ExternalFlags, respondentName(5), RESPONDENT6, EXTERNAL, 5),
                slot("respondent 7 internal flags", AllPartyFlags::getRespondent6Flags,
                        AllPartyFlags::setRespondent6Flags,
                        respondentName(6), RESPONDENT7, INTERNAL, 6),
                slot("respondent 7 external flags", AllPartyFlags::getRespondent6ExternalFlags,
                        AllPartyFlags::setRespondent6ExternalFlags, respondentName(6), RESPONDENT7, EXTERNAL, 6),
                slot("respondent 8 internal flags", AllPartyFlags::getRespondent7Flags,
                        AllPartyFlags::setRespondent7Flags,
                        respondentName(7), RESPONDENT8, INTERNAL, 7),
                slot("respondent 8 external flags", AllPartyFlags::getRespondent7ExternalFlags,
                        AllPartyFlags::setRespondent7ExternalFlags, respondentName(7), RESPONDENT8, EXTERNAL, 7),
                slot("respondent 9 internal flags", AllPartyFlags::getRespondent8Flags,
                        AllPartyFlags::setRespondent8Flags,
                        respondentName(8), RESPONDENT9, INTERNAL, 8),
                slot("respondent 9 external flags", AllPartyFlags::getRespondent8ExternalFlags,
                        AllPartyFlags::setRespondent8ExternalFlags, respondentName(8), RESPONDENT9, EXTERNAL, 8),
                slot("respondent 10 internal flags", AllPartyFlags::getRespondent9Flags,
                        AllPartyFlags::setRespondent9Flags,
                        respondentName(9), RESPONDENT10, INTERNAL, 9),
                slot("respondent 10 external flags", AllPartyFlags::getRespondent9ExternalFlags,
                        AllPartyFlags::setRespondent9ExternalFlags, respondentName(9), RESPONDENT10, EXTERNAL, 9),
                representativeSlot("representative 1 internal flags", AllPartyFlags::getRepresentativeFlags,
                        AllPartyFlags::setRepresentativeFlags, representativeName(0), REPRESENTATIVE1, INTERNAL, 0),
                representativeSlot("representative 1 external flags", AllPartyFlags::getRepresentativeExternalFlags,
                        AllPartyFlags::setRepresentativeExternalFlags, representativeName(0), REPRESENTATIVE1,
                        EXTERNAL, 0),
                representativeSlot("representative 2 internal flags", AllPartyFlags::getRepresentative1Flags,
                        AllPartyFlags::setRepresentative1Flags, representativeName(1), REPRESENTATIVE2, INTERNAL, 1),
                representativeSlot("representative 2 external flags", AllPartyFlags::getRepresentative1ExternalFlags,
                        AllPartyFlags::setRepresentative1ExternalFlags, representativeName(1), REPRESENTATIVE2,
                        EXTERNAL, 1),
                representativeSlot("representative 3 internal flags", AllPartyFlags::getRepresentative2Flags,
                        AllPartyFlags::setRepresentative2Flags, representativeName(2), REPRESENTATIVE3, INTERNAL, 2),
                representativeSlot("representative 3 external flags", AllPartyFlags::getRepresentative2ExternalFlags,
                        AllPartyFlags::setRepresentative2ExternalFlags, representativeName(2), REPRESENTATIVE3,
                        EXTERNAL, 2),
                representativeSlot("representative 4 internal flags", AllPartyFlags::getRepresentative3Flags,
                        AllPartyFlags::setRepresentative3Flags, representativeName(3), REPRESENTATIVE4, INTERNAL, 3),
                representativeSlot("representative 4 external flags", AllPartyFlags::getRepresentative3ExternalFlags,
                        AllPartyFlags::setRepresentative3ExternalFlags, representativeName(3), REPRESENTATIVE4,
                        EXTERNAL, 3),
                representativeSlot("representative 5 internal flags", AllPartyFlags::getRepresentative4Flags,
                        AllPartyFlags::setRepresentative4Flags, representativeName(4), REPRESENTATIVE5, INTERNAL, 4),
                representativeSlot("representative 5 external flags", AllPartyFlags::getRepresentative4ExternalFlags,
                        AllPartyFlags::setRepresentative4ExternalFlags, representativeName(4), REPRESENTATIVE5,
                        EXTERNAL, 4),
                representativeSlot("representative 6 internal flags", AllPartyFlags::getRepresentative5Flags,
                        AllPartyFlags::setRepresentative5Flags, representativeName(5), REPRESENTATIVE6, INTERNAL, 5),
                representativeSlot("representative 6 external flags", AllPartyFlags::getRepresentative5ExternalFlags,
                        AllPartyFlags::setRepresentative5ExternalFlags, representativeName(5), REPRESENTATIVE6,
                        EXTERNAL, 5),
                representativeSlot("representative 7 internal flags", AllPartyFlags::getRepresentative6Flags,
                        AllPartyFlags::setRepresentative6Flags, representativeName(6), REPRESENTATIVE7, INTERNAL, 6),
                representativeSlot("representative 7 external flags", AllPartyFlags::getRepresentative6ExternalFlags,
                        AllPartyFlags::setRepresentative6ExternalFlags, representativeName(6), REPRESENTATIVE7,
                        EXTERNAL, 6),
                representativeSlot("representative 8 internal flags", AllPartyFlags::getRepresentative7Flags,
                        AllPartyFlags::setRepresentative7Flags, representativeName(7), REPRESENTATIVE8, INTERNAL, 7),
                representativeSlot("representative 8 external flags", AllPartyFlags::getRepresentative7ExternalFlags,
                        AllPartyFlags::setRepresentative7ExternalFlags, representativeName(7), REPRESENTATIVE8,
                        EXTERNAL, 7),
                representativeSlot("representative 9 internal flags", AllPartyFlags::getRepresentative8Flags,
                        AllPartyFlags::setRepresentative8Flags, representativeName(8), REPRESENTATIVE9, INTERNAL, 8),
                representativeSlot("representative 9 external flags", AllPartyFlags::getRepresentative8ExternalFlags,
                        AllPartyFlags::setRepresentative8ExternalFlags, representativeName(8), REPRESENTATIVE9,
                        EXTERNAL, 8),
                representativeSlot("representative 10 internal flags", AllPartyFlags::getRepresentative9Flags,
                        AllPartyFlags::setRepresentative9Flags, representativeName(9), REPRESENTATIVE10, INTERNAL, 9),
                representativeSlot("representative 10 external flags", AllPartyFlags::getRepresentative9ExternalFlags,
                        AllPartyFlags::setRepresentative9ExternalFlags, representativeName(9), REPRESENTATIVE10,
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

    private static FlagSlot slot(String name, Function<AllPartyFlags, CaseFlagsType> getter,
                                BiConsumer<AllPartyFlags, CaseFlagsType> setter, String partyName,
                                String roleOnCase, String visibility, int respondentIndex) {
        return new FlagSlot(name, getter, setter, partyName, roleOnCase, visibility, respondentIndex, -1);
    }

    private static FlagSlot representativeSlot(String name, Function<AllPartyFlags, CaseFlagsType> getter,
                                               BiConsumer<AllPartyFlags, CaseFlagsType> setter, String partyName,
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

    private static AllPartyFlags allPartyFlags(CaseData caseData) {
        return caseData.getAllPartyFlags();
    }

    private static AllPartyFlags getOrCreateAllPartyFlags(CaseData caseData) {
        if (caseData.getAllPartyFlags() == null) {
            caseData.setAllPartyFlags(new AllPartyFlags());
        }
        return caseData.getAllPartyFlags();
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
        private final Function<AllPartyFlags, CaseFlagsType> getter;
        private final BiConsumer<AllPartyFlags, CaseFlagsType> setter;
        private final String partyName;
        private final String roleOnCase;
        private final String visibility;
        private final int respondentIndex;
        private final int representativeIndex;

        private FlagSlot(String name, Function<AllPartyFlags, CaseFlagsType> getter,
                         BiConsumer<AllPartyFlags, CaseFlagsType> setter, String partyName,
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

        private CaseFlagsType get(CaseData caseData) {
            AllPartyFlags allPartyFlags = caseData.getAllPartyFlags();
            return allPartyFlags == null ? null : getter.apply(allPartyFlags);
        }

        private void set(CaseData caseData, CaseFlagsType flags) {
            setter.accept(getOrCreateAllPartyFlags(caseData), flags);
        }

        @Override
        public String toString() {
            return name;
        }
    }
}

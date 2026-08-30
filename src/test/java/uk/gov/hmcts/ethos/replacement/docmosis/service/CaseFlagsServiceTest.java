package uk.gov.hmcts.ethos.replacement.docmosis.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.FlagDetailType;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationType;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTypeItem;
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

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
import static org.junit.jupiter.api.Assertions.assertSame;
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

class CaseFlagsServiceTest {
    private static final Path EXISTING_CASE_SAMPLE =
            Path.of("src", "test", "resources", "requests", "caseFlagMigrationRequest.json");
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
    void setupLegacyCaseFlags_shouldOnlyCreateV1CaseClaimantAndRespondentFlags() {
        caseFlagsService.setupLegacyCaseFlags(caseData);

        AllPartyFlags flags = allPartyFlags(caseData);
        assertNotNull(flags.getCaseFlags());
        assertThat(flags.getClaimantFlags().getPartyName(), is(CLAIMANT_NAME));
        assertThat(flags.getClaimantFlags().getRoleOnCase(), is(CLAIMANT));
        assertThat(flags.getRespondentFlags().getPartyName(), is(RESPONDENT_NAME));
        assertThat(flags.getRespondentFlags().getRoleOnCase(), is("respondent"));
        assertNull(flags.getClaimantExternalFlags());
        assertNull(flags.getRespondentExternalFlags());
        assertNull(flags.getClaimantRepresentativeFlags());
        assertNull(flags.getRepresentativeFlags());
        assertFalse(caseFlagsService.legacyCaseFlagsSetupRequired(caseData));
    }

    @Test
    void rollbackLegacyCaseFlags_shouldLeaveV2OnlyFieldsUnchanged() {
        caseFlagsService.setupCaseFlags(caseData);
        final CaseFlagsType claimantExternalFlags = allPartyFlags(caseData).getClaimantExternalFlags();
        final CaseFlagsType representativeFlags = allPartyFlags(caseData).getRepresentativeFlags();

        caseFlagsService.rollbackLegacyCaseFlags(caseData);

        assertNull(allPartyFlags(caseData).getCaseFlags());
        assertNull(allPartyFlags(caseData).getClaimantFlags());
        assertNull(allPartyFlags(caseData).getRespondentFlags());
        assertSame(claimantExternalFlags, allPartyFlags(caseData).getClaimantExternalFlags());
        assertSame(representativeFlags, allPartyFlags(caseData).getRepresentativeFlags());
    }

    @Test
    void migrateExistingClaimantAndRespondentCaseFlags_shouldMoveFlagsToInternalAndExternalSections() {
        caseData.setRespondentCollection(respondentCollection(2));
        FlagDetailType claimantInternal = flagDetail("Claimant internal", ACTIVE, NO);
        FlagDetailType claimantExternal = flagDetail("Claimant external", INACTIVE, YES);
        FlagDetailType respondent1Internal = flagDetail("Respondent 1 internal", INACTIVE, NO);
        FlagDetailType respondent1External = flagDetail("Respondent 1 external", ACTIVE, YES);
        FlagDetailType respondent2Internal = flagDetail("Respondent 2 internal", ACTIVE, "false");
        FlagDetailType respondent2External = flagDetail("Respondent 2 external", INACTIVE, "true");

        AllPartyFlags allPartyFlags = getOrCreateAllPartyFlags(caseData);
        allPartyFlags.setClaimantFlags(legacyCaseFlags(claimantInternal, claimantExternal));
        allPartyFlags.setRespondentFlags(legacyCaseFlags(respondent1External, respondent1Internal));
        allPartyFlags.setRespondent1Flags(legacyCaseFlags(respondent2Internal, respondent2External));

        caseFlagsService.migrateExistingClaimantAndRespondentCaseFlags(
                caseData,
                Map.of(
                        claimantInternal.getName(), INTERNAL,
                        claimantExternal.getName(), EXTERNAL,
                        respondent1Internal.getName(), INTERNAL,
                        respondent1External.getName(), EXTERNAL,
                        respondent2Internal.getName(), INTERNAL,
                        respondent2External.getName(), EXTERNAL
                )
        );

        AllPartyFlags migratedFlags = allPartyFlags(caseData);
        assertMigratedFlag(migratedFlags.getClaimantFlags(), CLAIMANT_NAME, CLAIMANT, INTERNAL, claimantInternal);
        assertMigratedFlag(migratedFlags.getClaimantExternalFlags(), CLAIMANT_NAME, CLAIMANT, EXTERNAL,
                claimantExternal);
        assertMigratedFlag(migratedFlags.getRespondentFlags(), RESPONDENT_NAME, RESPONDENT1, INTERNAL,
                respondent1Internal);
        assertMigratedFlag(migratedFlags.getRespondentExternalFlags(), RESPONDENT_NAME, RESPONDENT1, EXTERNAL,
                respondent1External);
        assertMigratedFlag(migratedFlags.getRespondent1Flags(), respondentName(1), RESPONDENT2, INTERNAL,
                respondent2Internal);
        assertMigratedFlag(migratedFlags.getRespondent1ExternalFlags(), respondentName(1), RESPONDENT2, EXTERNAL,
                respondent2External);
    }

    @Test
    void migrateExistingClaimantAndRespondentCaseFlags_shouldKeepFlagsMissingFromReferenceDataInternal() {
        FlagDetailType claimantFlagMissingFromReferenceData = flagDetail("Missing claimant flag", ACTIVE, YES);
        FlagDetailType respondentFlagMissingFromReferenceData = flagDetail("Missing respondent flag", ACTIVE, YES);
        AllPartyFlags allPartyFlags = getOrCreateAllPartyFlags(caseData);
        allPartyFlags.setClaimantFlags(legacyCaseFlags(claimantFlagMissingFromReferenceData));
        allPartyFlags.setRespondentFlags(legacyCaseFlags(respondentFlagMissingFromReferenceData));

        caseFlagsService.migrateExistingClaimantAndRespondentCaseFlags(
                caseData,
                Map.of("Current reference data flag", EXTERNAL)
        );

        assertMigratedFlag(allPartyFlags(caseData).getClaimantFlags(), CLAIMANT_NAME, CLAIMANT, INTERNAL,
                claimantFlagMissingFromReferenceData);
        assertNull(allPartyFlags(caseData).getClaimantExternalFlags().getDetails());
        assertMigratedFlag(allPartyFlags(caseData).getRespondentFlags(), RESPONDENT_NAME, RESPONDENT1, INTERNAL,
                respondentFlagMissingFromReferenceData);
        assertNull(allPartyFlags(caseData).getRespondentExternalFlags().getDetails());
    }

    @Test
    void migrateExistingClaimantAndRespondentCaseFlags_shouldMigrateExistingCaseSample() throws IOException {
        CaseData existingCase = new ObjectMapper().readValue(EXISTING_CASE_SAMPLE.toFile(), CaseData.class);
        final ListTypeItem<FlagDetailType> existingClaimantDetails =
                existingCase.getAllPartyFlags().getClaimantFlags().getDetails();
        final ListTypeItem<FlagDetailType> existingRespondentDetails =
                existingCase.getAllPartyFlags().getRespondentFlags().getDetails();

        caseFlagsService.migrateExistingClaimantAndRespondentCaseFlags(
                existingCase,
                Map.of(
                        "PF0002", INTERNAL,
                        "RA0010", EXTERNAL
                )
        );

        AllPartyFlags migratedFlags = existingCase.getAllPartyFlags();
        assertEquals(List.of("PF0015", "PF0002"), flagCodes(migratedFlags.getClaimantFlags()));
        assertNull(migratedFlags.getClaimantExternalFlags().getDetails());
        assertEquals(List.of("PF0015"), flagCodes(migratedFlags.getRespondentFlags()));
        assertEquals(List.of("RA0010"), flagCodes(migratedFlags.getRespondentExternalFlags()));
        assertSame(existingClaimantDetails.get(0), migratedFlags.getClaimantFlags().getDetails().get(0));
        assertSame(existingClaimantDetails.get(1), migratedFlags.getClaimantFlags().getDetails().get(1));
        assertSame(existingRespondentDetails.get(0),
                migratedFlags.getRespondentFlags().getDetails().getFirst());
        assertSame(existingRespondentDetails.get(1),
                migratedFlags.getRespondentExternalFlags().getDetails().getFirst());
    }

    @Test
    void migrateExistingClaimantAndRespondentCaseFlags_shouldUseReferenceDataVisibilityFirst() {
        FlagDetailType internalByReferenceData = flagDetail("Internal by reference data", "PF0002", ACTIVE, YES);
        FlagDetailType externalByReferenceData = flagDetail("External by reference data", "RA0010", INACTIVE, NO);
        getOrCreateAllPartyFlags(caseData).setClaimantFlags(
                legacyCaseFlags(internalByReferenceData, externalByReferenceData));

        caseFlagsService.migrateExistingClaimantAndRespondentCaseFlags(
                caseData,
                Map.of(
                        "PF0002", INTERNAL,
                        "RA0010", EXTERNAL
                )
        );

        assertMigratedFlag(allPartyFlags(caseData).getClaimantFlags(), CLAIMANT_NAME, CLAIMANT, INTERNAL,
                internalByReferenceData);
        assertMigratedFlag(allPartyFlags(caseData).getClaimantExternalFlags(), CLAIMANT_NAME, CLAIMANT, EXTERNAL,
                externalByReferenceData);
    }

    @Test
    void migrateExistingClaimantAndRespondentCaseFlags_shouldKeepFlagsWithNamedRespondentAfterReorder() {
        caseData.setRespondentCollection(respondentCollection(2));
        FlagDetailType respondent1External = flagDetail("Respondent 1 external", ACTIVE, YES);
        FlagDetailType respondent2Internal = flagDetail("Respondent 2 internal", INACTIVE, NO);

        AllPartyFlags allPartyFlags = getOrCreateAllPartyFlags(caseData);
        allPartyFlags.setRespondentFlags(legacyCaseFlags(respondentName(1), respondent2Internal));
        allPartyFlags.setRespondent1Flags(legacyCaseFlags(RESPONDENT_NAME, respondent1External));

        caseFlagsService.migrateExistingClaimantAndRespondentCaseFlags(
                caseData,
                Map.of(
                        respondent1External.getName(), EXTERNAL,
                        respondent2Internal.getName(), INTERNAL
                )
        );

        AllPartyFlags migratedFlags = allPartyFlags(caseData);
        assertNull(migratedFlags.getRespondentFlags().getDetails());
        assertMigratedFlag(migratedFlags.getRespondentExternalFlags(), RESPONDENT_NAME, RESPONDENT1, EXTERNAL,
                respondent1External);
        assertMigratedFlag(migratedFlags.getRespondent1Flags(), respondentName(1), RESPONDENT2, INTERNAL,
                respondent2Internal);
        assertNull(migratedFlags.getRespondent1ExternalFlags().getDetails());
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
    void setupCaseFlags_shouldUseSolicitorRoleForRepresentativeFlagSlotWhenRespondentHasNoRepresentative() {
        caseData.setRespondentCollection(respondentCollection(4));
        List<RespondentSumTypeItem> respondents = caseData.getRespondentCollection();
        caseData.setRepCollection(List.of(
                representativeItemForRespondent(
                        "1", "Representative 1", "rep1@example.com", respondents.get(0), "[SOLICITORA]"),
                representativeItemForRespondent(
                        "2", "Representative 2", "rep2@example.com", respondents.get(1), "[SOLICITORB]"),
                representativeItemForRespondent(
                        "4", "Representative 3", "rep3@example.com", respondents.get(3), "[SOLICITORD]")
        ));

        caseFlagsService.setupCaseFlags(caseData);

        AllPartyFlags allPartyFlags = allPartyFlags(caseData);
        assertAll(
                () -> assertEquals("Representative 1", allPartyFlags.getRepresentativeFlags().getPartyName()),
                () -> assertEquals("Representative 2", allPartyFlags.getRepresentative1Flags().getPartyName()),
                () -> assertNull(allPartyFlags.getRepresentative2Flags()),
                () -> assertNull(allPartyFlags.getRepresentative2ExternalFlags()),
                () -> assertEquals("Representative 3", allPartyFlags.getRepresentative3Flags().getPartyName()),
                () -> assertEquals(REPRESENTATIVE4, allPartyFlags.getRepresentative3Flags().getRoleOnCase()),
                () -> assertEquals(INTERNAL, allPartyFlags.getRepresentative3Flags().getVisibility()),
                () -> assertEquals(
                        "Representative 3", allPartyFlags.getRepresentative3ExternalFlags().getPartyName()),
                () -> assertEquals(
                        REPRESENTATIVE4, allPartyFlags.getRepresentative3ExternalFlags().getRoleOnCase()),
                () -> assertEquals(EXTERNAL, allPartyFlags.getRepresentative3ExternalFlags().getVisibility()),
                () -> assertFalse(caseFlagsService.caseFlagsSetupRequired(caseData))
        );
    }

    @Test
    void setupCaseFlags_shouldReuseRespondentRepresentativeFlagsForTheSameRepresentativeWithoutCompactingSlots() {
        String sharedRepresentativeName = "Shared Representative";
        String otherRepresentativeName = "Other Representative";
        caseData.setRepCollection(List.of(
                representativeItem("1", sharedRepresentativeName),
                representativeItem("2", sharedRepresentativeName),
                representativeItem("3", otherRepresentativeName)
        ));

        caseFlagsService.setupCaseFlags(caseData);

        AllPartyFlags allPartyFlags = allPartyFlags(caseData);
        assertAll(
                () -> assertEquals(sharedRepresentativeName, allPartyFlags.getRepresentativeFlags().getPartyName()),
                () -> assertEquals(REPRESENTATIVE1, allPartyFlags.getRepresentativeFlags().getRoleOnCase()),
                () -> assertEquals(INTERNAL, allPartyFlags.getRepresentativeFlags().getVisibility()),
                () -> assertEquals(sharedRepresentativeName,
                        allPartyFlags.getRepresentativeExternalFlags().getPartyName()),
                () -> assertEquals(REPRESENTATIVE1, allPartyFlags.getRepresentativeExternalFlags().getRoleOnCase()),
                () -> assertEquals(EXTERNAL, allPartyFlags.getRepresentativeExternalFlags().getVisibility()),
                () -> assertNull(allPartyFlags.getRepresentative1Flags()),
                () -> assertNull(allPartyFlags.getRepresentative1ExternalFlags()),
                () -> assertEquals(otherRepresentativeName, allPartyFlags.getRepresentative2Flags().getPartyName()),
                () -> assertEquals(REPRESENTATIVE3, allPartyFlags.getRepresentative2Flags().getRoleOnCase()),
                () -> assertEquals(INTERNAL, allPartyFlags.getRepresentative2Flags().getVisibility()),
                () -> assertEquals(otherRepresentativeName,
                        allPartyFlags.getRepresentative2ExternalFlags().getPartyName()),
                () -> assertEquals(REPRESENTATIVE3,
                        allPartyFlags.getRepresentative2ExternalFlags().getRoleOnCase()),
                () -> assertEquals(EXTERNAL, allPartyFlags.getRepresentative2ExternalFlags().getVisibility()),
                () -> assertFalse(caseFlagsService.caseFlagsSetupRequired(caseData))
        );
    }

    @Test
    void setupCaseFlags_shouldRetainSharedRepresentativeDetailsUntilFinalRepresentationIsRemoved() {
        caseData.setRespondentCollection(respondentCollection(2));
        List<RespondentSumTypeItem> respondents = caseData.getRespondentCollection();
        RepresentedTypeRItem respondent1Representative = representativeItemForRespondent(
                "1", "Shared Representative", "shared@example.com", respondents.get(0), "[SOLICITORA]");
        RepresentedTypeRItem respondent2Representative = representativeItemForRespondent(
                "2", "Shared Representative", "shared@example.com", respondents.get(1), "[SOLICITORB]");
        caseData.setRepCollection(List.of(respondent1Representative, respondent2Representative));

        caseFlagsService.setupCaseFlags(caseData);
        AllPartyFlags allPartyFlags = allPartyFlags(caseData);
        allPartyFlags.getRepresentativeFlags().setDetails(
                ListTypeItem.from(activeFlag("Representative internal flag")));
        allPartyFlags.getRepresentativeExternalFlags().setDetails(
                ListTypeItem.from(activeFlag("Representative external flag")));

        caseData.setRepCollection(List.of(respondent2Representative));
        caseFlagsService.setupCaseFlags(caseData);

        assertAll(
                () -> assertNull(allPartyFlags.getRepresentativeFlags()),
                () -> assertEquals("Representative internal flag",
                        allPartyFlags.getRepresentative1Flags().getDetails().getFirst().getValue().getName()),
                () -> assertEquals("Representative external flag",
                        allPartyFlags.getRepresentative1ExternalFlags().getDetails().getFirst().getValue().getName())
        );

        caseData.setRepCollection(List.of());
        caseFlagsService.setupCaseFlags(caseData);

        assertAll(
                () -> assertNull(allPartyFlags.getRepresentative1Flags()),
                () -> assertNull(allPartyFlags.getRepresentative1ExternalFlags())
        );
    }

    @Test
    void setupCaseFlags_shouldCreateSeparateRespondentRepresentativeFlagsForSameNameWithDifferentEmails() {
        String sharedRepresentativeName = "Shared Representative";
        caseData.setRepCollection(List.of(
                representativeItem("1", sharedRepresentativeName, "first@example.com"),
                representativeItem("2", sharedRepresentativeName, "second@example.com")
        ));

        caseFlagsService.setupCaseFlags(caseData);
        AllPartyFlags allPartyFlags = allPartyFlags(caseData);
        allPartyFlags.getRepresentativeFlags().setDetails(ListTypeItem.from(activeFlag("First representative flag")));
        allPartyFlags.getRepresentative1Flags().setDetails(ListTypeItem.from(activeFlag("Second representative flag")));

        caseFlagsService.setupCaseFlags(caseData);

        assertAll(
                () -> assertEquals(sharedRepresentativeName, allPartyFlags.getRepresentativeFlags().getPartyName()),
                () -> assertEquals(REPRESENTATIVE1, allPartyFlags.getRepresentativeFlags().getRoleOnCase()),
                () -> assertEquals("First representative flag",
                        allPartyFlags.getRepresentativeFlags().getDetails().getFirst().getValue().getName()),
                () -> assertEquals(sharedRepresentativeName, allPartyFlags.getRepresentative1Flags().getPartyName()),
                () -> assertEquals(REPRESENTATIVE2, allPartyFlags.getRepresentative1Flags().getRoleOnCase()),
                () -> assertEquals("Second representative flag",
                        allPartyFlags.getRepresentative1Flags().getDetails().getFirst().getValue().getName()),
                () -> assertFalse(caseFlagsService.caseFlagsSetupRequired(caseData))
        );
    }

    @Test
    void removeRespondentRepresentativeFlags_shouldDeleteOnlyRemovedRepresentativeWithSameName() {
        caseData.setRespondentCollection(respondentCollection(2));
        List<RespondentSumTypeItem> respondents = caseData.getRespondentCollection();
        RepresentedTypeRItem firstRepresentative = representativeItemForRespondent(
                "1", "Shared Name", "first@example.com", respondents.get(0), "[SOLICITORA]");
        RepresentedTypeRItem secondRepresentative = representativeItemForRespondent(
                "2", "Shared Name", "second@example.com", respondents.get(1), "[SOLICITORB]");
        caseData.setRepCollection(new ArrayList<>(List.of(firstRepresentative, secondRepresentative)));
        caseFlagsService.setupCaseFlags(caseData);
        AllPartyFlags allPartyFlags = allPartyFlags(caseData);
        allPartyFlags.getRepresentativeFlags().setDetails(ListTypeItem.from(activeFlag("First flag")));
        allPartyFlags.getRepresentative1Flags().setDetails(ListTypeItem.from(activeFlag("Second flag")));

        caseFlagsService.removeRespondentRepresentativeFlags(caseData, List.of(firstRepresentative));
        caseData.getRepCollection().remove(firstRepresentative);
        caseFlagsService.setupCaseFlags(caseData);

        assertAll(
                () -> assertNull(allPartyFlags.getRepresentativeFlags()),
                () -> assertEquals("second@example.com",
                        caseData.getRepCollection().getFirst().getValue().getRepresentativeEmailAddress()),
                () -> assertEquals("Second flag",
                        allPartyFlags.getRepresentative1Flags().getDetails().getFirst().getValue().getName())
        );
    }

    @Test
    void removeRespondentRepresentativeFlags_shouldRetainFlagsWhenRepresentativeStillRepresentsAnotherRespondent() {
        caseData.setRespondentCollection(respondentCollection(2));
        List<RespondentSumTypeItem> respondents = caseData.getRespondentCollection();
        RepresentedTypeRItem firstRepresentation = representativeItemForRespondent(
                "1", "Shared Representative", "shared@example.com", respondents.get(0), "[SOLICITORA]");
        RepresentedTypeRItem secondRepresentation = representativeItemForRespondent(
                "2", "Shared Representative", "shared@example.com", respondents.get(1), "[SOLICITORB]");
        caseData.setRepCollection(new ArrayList<>(List.of(firstRepresentation, secondRepresentation)));
        caseFlagsService.setupCaseFlags(caseData);
        AllPartyFlags allPartyFlags = allPartyFlags(caseData);
        allPartyFlags.getRepresentativeFlags().setDetails(ListTypeItem.from(activeFlag("Shared flag")));

        caseFlagsService.removeRespondentRepresentativeFlags(caseData, List.of(firstRepresentation));
        caseData.getRepCollection().remove(firstRepresentation);
        caseFlagsService.setupCaseFlags(caseData);

        assertAll(
                () -> assertNull(allPartyFlags.getRepresentativeFlags()),
                () -> assertEquals("Shared flag",
                        allPartyFlags.getRepresentative1Flags().getDetails().getFirst().getValue().getName())
        );
    }

    @Test
    void caseFlagsSetupRequired_shouldBeTrueWhenCompactedRepresentativeNeedsMovingToRoleAlignedSlot() {
        String representative1Name = "Representative 1";
        String representative2Name = "Representative 2";
        caseData.setRepCollection(List.of(
                representativeItem("1", representative1Name),
                representativeItem("2", representative1Name),
                representativeItem("3", representative2Name)
        ));
        AllPartyFlags allPartyFlags = getOrCreateAllPartyFlags(caseData);
        allPartyFlags.setCaseFlags(CaseFlagsType.builder().build());
        allPartyFlags.setRepresentativeFlags(representativeFlags(
                representative1Name, REPRESENTATIVE1, INTERNAL, "Representative 1 internal flag"));
        allPartyFlags.setRepresentativeExternalFlags(representativeFlags(
                representative1Name, REPRESENTATIVE1, EXTERNAL, "Representative 1 external flag"));
        allPartyFlags.setRepresentative1Flags(representativeFlags(
                representative2Name, REPRESENTATIVE2, INTERNAL, "Representative 2 compacted internal flag"));
        allPartyFlags.setRepresentative1ExternalFlags(representativeFlags(
                representative2Name, REPRESENTATIVE2, EXTERNAL, "Representative 2 compacted external flag"));

        assertTrue(caseFlagsService.caseFlagsSetupRequired(caseData));

        caseFlagsService.setupCaseFlags(caseData);

        assertAll(
                () -> assertEquals(representative1Name, allPartyFlags.getRepresentativeFlags().getPartyName()),
                () -> assertEquals(representative1Name,
                        allPartyFlags.getRepresentativeExternalFlags().getPartyName()),
                () -> assertNull(allPartyFlags.getRepresentative1Flags()),
                () -> assertNull(allPartyFlags.getRepresentative1ExternalFlags()),
                () -> assertEquals(representative2Name, allPartyFlags.getRepresentative2Flags().getPartyName()),
                () -> assertEquals(REPRESENTATIVE3, allPartyFlags.getRepresentative2Flags().getRoleOnCase()),
                () -> assertEquals(INTERNAL, allPartyFlags.getRepresentative2Flags().getVisibility()),
                () -> assertEquals("Representative 2 compacted internal flag",
                        allPartyFlags.getRepresentative2Flags().getDetails().getFirst().getValue().getName()),
                () -> assertEquals(representative2Name,
                        allPartyFlags.getRepresentative2ExternalFlags().getPartyName()),
                () -> assertEquals(REPRESENTATIVE3,
                        allPartyFlags.getRepresentative2ExternalFlags().getRoleOnCase()),
                () -> assertEquals(EXTERNAL, allPartyFlags.getRepresentative2ExternalFlags().getVisibility()),
                () -> assertEquals("Representative 2 compacted external flag",
                        allPartyFlags.getRepresentative2ExternalFlags().getDetails().getFirst().getValue().getName()),
                () -> assertFalse(caseFlagsService.caseFlagsSetupRequired(caseData))
        );
    }

    @Test
    void setupCaseFlags_shouldPreserveExistingDetailsWhenClearingDuplicateRepresentativeFlags() {
        String sharedRepresentativeName = "Shared Representative";
        caseData.setRepCollection(List.of(
                representativeItem("1", sharedRepresentativeName),
                representativeItem("2", sharedRepresentativeName)
        ));
        caseFlagsService.setupCaseFlags(caseData);
        allPartyFlags(caseData).getRepresentativeFlags().setDetails(null);
        allPartyFlags(caseData).getRepresentativeExternalFlags().setDetails(null);
        allPartyFlags(caseData).setRepresentative1Flags(CaseFlagsType.builder()
                .partyName(sharedRepresentativeName)
                .visibility(INTERNAL)
                .details(ListTypeItem.from(activeFlag("Duplicate internal flag")))
                .build());
        allPartyFlags(caseData).setRepresentative1ExternalFlags(CaseFlagsType.builder()
                .partyName(sharedRepresentativeName)
                .visibility(EXTERNAL)
                .details(ListTypeItem.from(activeFlag("Duplicate external flag")))
                .build());

        caseFlagsService.setupCaseFlags(caseData);

        assertAll(
                () -> assertEquals("Duplicate internal flag",
                        allPartyFlags(caseData).getRepresentativeFlags().getDetails().getFirst().getValue().getName()),
                () -> assertEquals("Duplicate external flag",
                        allPartyFlags(caseData).getRepresentativeExternalFlags().getDetails().getFirst()
                                .getValue().getName()),
                () -> assertNull(allPartyFlags(caseData).getRepresentative1Flags()),
                () -> assertNull(allPartyFlags(caseData).getRepresentative1ExternalFlags())
        );
    }

    @Test
    void setupCaseFlags_shouldKeepRoleAlignedSlotsWhenClearingDuplicateRepresentativeFlags() {
        String representative1Name = "Representative 1";
        String representative2Name = "Representative 2";
        String representative3Name = "Representative 3";
        caseData.setRepCollection(List.of(
                representativeItem("1", representative1Name),
                representativeItem("2", representative2Name),
                representativeItem("3", representative2Name),
                representativeItem("4", representative3Name)
        ));
        AllPartyFlags allPartyFlags = getOrCreateAllPartyFlags(caseData);
        allPartyFlags.setRepresentativeFlags(representativeFlags(
                representative1Name, REPRESENTATIVE1, INTERNAL, "Representative 1 internal flag"));
        allPartyFlags.setRepresentative1Flags(representativeFlags(
                representative2Name, REPRESENTATIVE2, INTERNAL, "Representative 2 internal flag"));
        allPartyFlags.setRepresentative2Flags(representativeFlags(
                representative2Name, REPRESENTATIVE3, INTERNAL, "Duplicate representative 2 internal flag"));
        allPartyFlags.setRepresentative3Flags(representativeFlags(
                representative3Name, REPRESENTATIVE4, INTERNAL, "Representative 3 internal flag"));
        allPartyFlags.setRepresentativeExternalFlags(representativeFlags(
                representative1Name, REPRESENTATIVE1, EXTERNAL, "Representative 1 external flag"));
        allPartyFlags.setRepresentative1ExternalFlags(representativeFlags(
                representative2Name, REPRESENTATIVE2, EXTERNAL, "Representative 2 external flag"));
        allPartyFlags.setRepresentative2ExternalFlags(representativeFlags(
                representative2Name, REPRESENTATIVE3, EXTERNAL, "Duplicate representative 2 external flag"));
        allPartyFlags.setRepresentative3ExternalFlags(representativeFlags(
                representative3Name, REPRESENTATIVE4, EXTERNAL, "Representative 3 external flag"));

        caseFlagsService.setupCaseFlags(caseData);

        assertAll(
                () -> assertEquals(representative1Name, allPartyFlags.getRepresentativeFlags().getPartyName()),
                () -> assertEquals("Representative 1 internal flag",
                        allPartyFlags.getRepresentativeFlags().getDetails().getFirst().getValue().getName()),
                () -> assertEquals(representative2Name, allPartyFlags.getRepresentative1Flags().getPartyName()),
                () -> assertEquals("Representative 2 internal flag",
                        allPartyFlags.getRepresentative1Flags().getDetails().getFirst().getValue().getName()),
                () -> assertEquals("Duplicate representative 2 internal flag",
                        allPartyFlags.getRepresentative1Flags().getDetails().get(1).getValue().getName()),
                () -> assertNull(allPartyFlags.getRepresentative2Flags()),
                () -> assertNull(allPartyFlags.getRepresentative2ExternalFlags()),
                () -> assertEquals("Duplicate representative 2 external flag",
                        allPartyFlags.getRepresentative1ExternalFlags().getDetails().get(1).getValue().getName()),
                () -> assertEquals(representative3Name, allPartyFlags.getRepresentative3Flags().getPartyName()),
                () -> assertEquals(REPRESENTATIVE4, allPartyFlags.getRepresentative3Flags().getRoleOnCase()),
                () -> assertEquals("Representative 3 internal flag",
                        allPartyFlags.getRepresentative3Flags().getDetails().getFirst().getValue().getName()),
                () -> assertEquals(representative3Name,
                        allPartyFlags.getRepresentative3ExternalFlags().getPartyName()),
                () -> assertEquals(REPRESENTATIVE4,
                        allPartyFlags.getRepresentative3ExternalFlags().getRoleOnCase()),
                () -> assertEquals("Representative 3 external flag",
                        allPartyFlags.getRepresentative3ExternalFlags().getDetails().getFirst().getValue().getName())
        );
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
    void rollbackCaseFlags_shouldConsolidateV2PartyFlagsAndKeepCaseFlags() {
        caseFlagsService.setupCaseFlags(caseData);
        AllPartyFlags flags = allPartyFlags(caseData);
        CaseFlagsType caseFlags = flags.getCaseFlags();
        caseFlags.setDetails(ListTypeItem.from(activeFlag("Case flag")));

        flags.getClaimantFlags().setDetails(ListTypeItem.from(activeFlag("Claimant internal flag")));
        flags.getClaimantExternalFlags().setDetails(ListTypeItem.from(activeFlag("Claimant external flag")));

        flags.getRespondentFlags().setDetails(ListTypeItem.from(activeFlag("Respondent 1 internal flag")));
        flags.getRespondentExternalFlags().setDetails(ListTypeItem.from(activeFlag("Respondent 1 external flag")));
        flags.getRespondent1Flags().setDetails(ListTypeItem.from(activeFlag("Respondent 2 internal flag")));
        flags.getRespondent1ExternalFlags().setDetails(ListTypeItem.from(activeFlag("Respondent 2 external flag")));
        flags.getRespondent2Flags().setDetails(ListTypeItem.from(activeFlag("Respondent 3 internal flag")));
        flags.getRespondent2ExternalFlags().setDetails(ListTypeItem.from(activeFlag("Respondent 3 external flag")));

        flags.getClaimantRepresentativeFlags().setDetails(
                ListTypeItem.from(activeFlag("Claimant representative flag")));
        flags.getRepresentativeFlags().setDetails(ListTypeItem.from(activeFlag("Respondent representative flag")));

        caseFlagsService.rollbackCaseFlags(caseData);

        assertSame(caseFlags, flags.getCaseFlags());
        assertEquals(List.of("Case flag"), flagNames(flags.getCaseFlags()));
        assertEquals(List.of("Claimant internal flag", "Claimant external flag"),
                flagNames(flags.getClaimantFlags()));
        assertEquals(CLAIMANT_NAME, flags.getClaimantFlags().getPartyName());
        assertEquals(CLAIMANT, flags.getClaimantFlags().getRoleOnCase());
        assertNull(flags.getClaimantFlags().getGroupId());
        assertNull(flags.getClaimantFlags().getVisibility());
        assertNull(flags.getClaimantExternalFlags());
        assertEquals(List.of(
                "Respondent 1 internal flag",
                "Respondent 1 external flag",
                "Respondent 2 internal flag",
                "Respondent 2 external flag",
                "Respondent 3 internal flag",
                "Respondent 3 external flag"), flagNames(flags.getRespondentFlags()));
        assertEquals(RESPONDENT_NAME, flags.getRespondentFlags().getPartyName());
        assertEquals("respondent", flags.getRespondentFlags().getRoleOnCase());
        assertNull(flags.getRespondentFlags().getGroupId());
        assertNull(flags.getRespondentFlags().getVisibility());
        assertNull(flags.getRespondentExternalFlags());

        respondentPartyFlagSlots()
                .filter(slot -> slot.respondentIndex > 0)
                .forEach(slot -> assertNull(slot.get(caseData), slot + " should be cleared"));
        assertNull(flags.getClaimantRepresentativeFlags());
        assertNull(flags.getClaimantRepresentativeExternalFlags());
        respondentRepresentativePartyFlagSlots()
                .forEach(slot -> assertNull(slot.get(caseData), slot + " should be cleared"));
    }

    @Test
    void rollbackCaseFlags_shouldNotCreateAllPartyFlagsWhenAbsent() {
        caseFlagsService.rollbackCaseFlags(caseData);

        assertNull(caseData.getAllPartyFlags());
    }

    @Test
    void rollbackCaseFlags_shouldCreateMissingConsolidationSectionsWithoutLosingFlags() {
        AllPartyFlags flags = AllPartyFlags.builder()
                .claimantExternalFlags(legacyCaseFlags(activeFlag("Claimant external flag")))
                .respondent1Flags(legacyCaseFlags(activeFlag("Respondent 2 internal flag")))
                .respondent1ExternalFlags(legacyCaseFlags(activeFlag("Respondent 2 external flag")))
                .build();
        caseData.setAllPartyFlags(flags);

        caseFlagsService.rollbackCaseFlags(caseData);

        assertEquals(List.of("Claimant external flag"), flagNames(flags.getClaimantFlags()));
        assertEquals(CLAIMANT_NAME, flags.getClaimantFlags().getPartyName());
        assertEquals(CLAIMANT, flags.getClaimantFlags().getRoleOnCase());
        assertNull(flags.getClaimantFlags().getGroupId());
        assertNull(flags.getClaimantFlags().getVisibility());
        assertEquals(List.of("Respondent 2 internal flag", "Respondent 2 external flag"),
                flagNames(flags.getRespondentFlags()));
        assertEquals(RESPONDENT_NAME, flags.getRespondentFlags().getPartyName());
        assertEquals("respondent", flags.getRespondentFlags().getRoleOnCase());
        assertNull(flags.getRespondentFlags().getGroupId());
        assertNull(flags.getRespondentFlags().getVisibility());
        assertNull(flags.getRespondentExternalFlags());
        assertNull(flags.getClaimantExternalFlags());
        assertNull(flags.getRespondent1Flags());
        assertNull(flags.getRespondent1ExternalFlags());
    }

    @Test
    void inactivateRespondentCaseFlags_shouldSetAllMatchingRespondentFlagDetailsInactive() {
        caseFlagsService.setupCaseFlags(caseData);
        CaseFlagsType respondentInternalFlags = allPartyFlags(caseData).getRespondent1Flags();
        respondentInternalFlags.setDetails(ListTypeItem.from(
                activeFlag("Internal flag 1"),
                activeFlag("Internal flag 2")
        ));

        CaseFlagsType respondentExternalFlags = allPartyFlags(caseData).getRespondent1ExternalFlags();
        CaseFlagsType otherRespondentFlags = allPartyFlags(caseData).getRespondent2Flags();
        CaseFlagsType claimantFlags = allPartyFlags(caseData).getClaimantFlags();
        respondentExternalFlags.setDetails(ListTypeItem.from(activeFlag("External flag")));
        otherRespondentFlags.setDetails(ListTypeItem.from(activeFlag("Other respondent flag")));
        claimantFlags.setDetails(ListTypeItem.from(activeFlag("Claimant flag")));

        caseFlagsService.inactivateRespondentCaseFlags(caseData, respondentName(1));

        assertEquals(INACTIVE, respondentInternalFlags.getDetails().get(0).getValue().getStatus());
        assertEquals(INACTIVE, respondentInternalFlags.getDetails().get(1).getValue().getStatus());
        assertEquals(INACTIVE, respondentExternalFlags.getDetails().getFirst().getValue().getStatus());
        assertNull(respondentInternalFlags.getDetails().get(0).getValue().getFlagComment());
        assertNull(respondentInternalFlags.getDetails().get(1).getValue().getFlagComment());
        assertNull(respondentExternalFlags.getDetails().getFirst().getValue().getFlagComment());
        assertEquals(ACTIVE, otherRespondentFlags.getDetails().getFirst().getValue().getStatus());
        assertEquals(ACTIVE, claimantFlags.getDetails().getFirst().getValue().getStatus());
        assertNull(otherRespondentFlags.getDetails().getFirst().getValue().getFlagComment());
        assertNull(claimantFlags.getDetails().getFirst().getValue().getFlagComment());
    }

    @Test
    void inactivateRespondentCaseFlags_shouldIgnoreMissingPartyFlagsAndDetails() {
        caseFlagsService.inactivateRespondentCaseFlags(caseData, RESPONDENT_NAME);

        assertNull(caseData.getAllPartyFlags());

        caseFlagsService.setupCaseFlags(caseData);
        allPartyFlags(caseData).getRespondentFlags().setDetails(null);

        caseFlagsService.inactivateRespondentCaseFlags(caseData, RESPONDENT_NAME);

        assertNull(allPartyFlags(caseData).getRespondentFlags().getDetails());
    }

    @Test
    void inactivateRespondentRepresentativeCaseFlags_shouldInactivateRepresentativeWithNoOtherActiveRespondents() {
        setupRepresentativeAccessScenario();
        RespondentSumTypeItem respondent1 = caseData.getRespondentCollection().getFirst();
        AllPartyFlags allPartyFlags = allPartyFlags(caseData);
        allPartyFlags.getRepresentativeFlags()
                .setDetails(ListTypeItem.from(activeFlag("Representative 1 internal flag")));
        allPartyFlags.getRepresentativeExternalFlags()
                .setDetails(ListTypeItem.from(activeFlag("Representative 1 external flag")));
        allPartyFlags.getRepresentative1Flags()
                .setDetails(ListTypeItem.from(activeFlag("Representative 2 internal flag")));

        respondent1.getValue().setResponseStruckOut(YES);

        caseFlagsService.inactivateRespondentRepresentativeCaseFlags(caseData, respondent1);

        assertAll(
                () -> assertEquals(INACTIVE, firstFlagStatus(allPartyFlags.getRepresentativeFlags())),
                () -> assertEquals(INACTIVE, firstFlagStatus(allPartyFlags.getRepresentativeExternalFlags())),
                () -> assertNull(firstFlagComment(allPartyFlags.getRepresentativeFlags())),
                () -> assertNull(firstFlagComment(allPartyFlags.getRepresentativeExternalFlags())),
                () -> assertEquals(ACTIVE, firstFlagStatus(allPartyFlags.getRepresentative1Flags()))
        );
    }

    @Test
    void inactivateRespondentRepresentativeCaseFlags_shouldKeepSharedRepresentativeActiveUntilAllInactive() {
        setupRepresentativeAccessScenario();
        RespondentSumTypeItem respondent3 = caseData.getRespondentCollection().get(2);
        AllPartyFlags allPartyFlags = allPartyFlags(caseData);
        allPartyFlags.getRepresentative1Flags()
                .setDetails(ListTypeItem.from(activeFlag("Representative 2 internal flag")));
        allPartyFlags.getRepresentative1ExternalFlags()
                .setDetails(ListTypeItem.from(activeFlag("Representative 2 external flag")));

        respondent3.getValue().setResponseContinue(NO);
        caseFlagsService.inactivateRespondentRepresentativeCaseFlags(caseData, respondent3);

        assertAll(
                () -> assertEquals(ACTIVE, firstFlagStatus(allPartyFlags.getRepresentative1Flags())),
                () -> assertEquals(ACTIVE, firstFlagStatus(allPartyFlags.getRepresentative1ExternalFlags()))
        );

        RespondentSumTypeItem respondent2 = caseData.getRespondentCollection().get(1);
        respondent2.getValue().setResponseStruckOut(YES);
        caseFlagsService.inactivateRespondentRepresentativeCaseFlags(caseData, respondent2);

        assertAll(
                () -> assertEquals(INACTIVE, firstFlagStatus(allPartyFlags.getRepresentative1Flags())),
                () -> assertEquals(INACTIVE, firstFlagStatus(allPartyFlags.getRepresentative1ExternalFlags())),
                () -> assertNull(firstFlagComment(allPartyFlags.getRepresentative1Flags())),
                () -> assertNull(firstFlagComment(allPartyFlags.getRepresentative1ExternalFlags()))
        );
    }

    @Test
    void setupCaseFlags_shouldKeepDetailsWhenRepresentativeSlotsSwapAfterRespondentReorder() {
        caseData.setRespondentCollection(respondentCollection(4));
        List<RespondentSumTypeItem> respondents = caseData.getRespondentCollection();
        caseData.setRepCollection(List.of(
                representativeItemForRespondent("1", "Representative 1", "rep1@example.com", respondents.get(0)),
                representativeItemForRespondent("4", "Representative 3", "rep3@example.com", respondents.get(3)),
                representativeItemForRespondent("2", "Representative 2", "rep2@example.com", respondents.get(1)),
                representativeItemForRespondent("3", "Representative 2", "rep2@example.com", respondents.get(2))
        ));
        AllPartyFlags allPartyFlags = getOrCreateAllPartyFlags(caseData);
        allPartyFlags.setCaseFlags(CaseFlagsType.builder().build());
        allPartyFlags.setRepresentativeFlags(representativeFlags(
                "Representative 1", REPRESENTATIVE1, INTERNAL, "Representative 1 internal flag"));
        allPartyFlags.setRepresentative1Flags(representativeFlags(
                "Representative 2", REPRESENTATIVE2, INTERNAL, "Representative 2 internal flag"));
        allPartyFlags.setRepresentative2Flags(representativeFlags(
                "Representative 3", REPRESENTATIVE3, INTERNAL, "Representative 3 internal flag"));
        allPartyFlags.setRepresentativeExternalFlags(representativeFlags(
                "Representative 1", REPRESENTATIVE1, EXTERNAL, "Representative 1 external flag"));
        allPartyFlags.setRepresentative1ExternalFlags(representativeFlags(
                "Representative 2", REPRESENTATIVE2, EXTERNAL, "Representative 2 external flag"));
        allPartyFlags.setRepresentative2ExternalFlags(representativeFlags(
                "Representative 3", REPRESENTATIVE3, EXTERNAL, "Representative 3 external flag"));
        inactivateCaseFlags(allPartyFlags.getRepresentative1Flags());
        inactivateCaseFlags(allPartyFlags.getRepresentative1ExternalFlags());

        caseFlagsService.setupCaseFlags(caseData);

        assertAll(
                () -> assertRepresentativeFlags(
                        allPartyFlags.getRepresentative1Flags(),
                        "Representative 3",
                        REPRESENTATIVE2,
                        ACTIVE,
                        "Representative 3 internal flag"
                ),
                () -> assertRepresentativeFlags(
                        allPartyFlags.getRepresentative1ExternalFlags(),
                        "Representative 3",
                        REPRESENTATIVE2,
                        ACTIVE,
                        "Representative 3 external flag"
                ),
                () -> assertRepresentativeFlags(
                        allPartyFlags.getRepresentative2Flags(),
                        "Representative 2",
                        REPRESENTATIVE3,
                        INACTIVE,
                        "Representative 2 internal flag"
                ),
                () -> assertRepresentativeFlags(
                        allPartyFlags.getRepresentative2ExternalFlags(),
                        "Representative 2",
                        REPRESENTATIVE3,
                        INACTIVE,
                        "Representative 2 external flag"
                ),
                () -> assertNull(allPartyFlags.getRepresentative3Flags()),
                () -> assertNull(allPartyFlags.getRepresentative3ExternalFlags())
        );
    }

    @Test
    void clearRespondentRepresentativeFlags_shouldRemoveMatchingRepresentativeIndexFlagDetailsOnly() {
        caseFlagsService.setupCaseFlags(caseData);
        CaseFlagsType representativeInternalFlags = allPartyFlags(caseData).getRepresentative1Flags();
        CaseFlagsType representativeExternalFlags = allPartyFlags(caseData).getRepresentative1ExternalFlags();
        CaseFlagsType otherRepresentativeFlags = allPartyFlags(caseData).getRepresentative2Flags();
        representativeInternalFlags.setDetails(ListTypeItem.from(activeFlag("Representative internal flag")));
        representativeExternalFlags.setDetails(ListTypeItem.from(activeFlag("Representative external flag")));
        otherRepresentativeFlags.setDetails(ListTypeItem.from(activeFlag("Other representative flag")));
        CaseFlagsType respondentFlags = allPartyFlags(caseData).getRespondent1Flags();
        respondentFlags.setDetails(ListTypeItem.from(activeFlag("Respondent flag")));

        caseFlagsService.clearRespondentRepresentativeFlags(caseData, List.of(1));

        assertNull(representativeInternalFlags.getDetails());
        assertNull(representativeExternalFlags.getDetails());
        assertEquals(ACTIVE, otherRepresentativeFlags.getDetails().getFirst().getValue().getStatus());
        assertEquals(ACTIVE, respondentFlags.getDetails().getFirst().getValue().getStatus());
    }

    @Test
    void clearRespondentRepresentativeFlags_shouldClearTheRepresentativesRoleAlignedFlagSlot() {
        caseData.setRespondentCollection(respondentCollection(4));
        List<RespondentSumTypeItem> respondents = caseData.getRespondentCollection();
        caseData.setRepCollection(List.of(
                representativeItemForRespondent(
                        "1", "Representative 1", "rep1@example.com", respondents.get(0), "[SOLICITORA]"),
                representativeItemForRespondent(
                        "2", "Representative 2", "rep2@example.com", respondents.get(1), "[SOLICITORB]"),
                representativeItemForRespondent(
                        "4", "Representative 3", "rep3@example.com", respondents.get(3), "[SOLICITORD]")
        ));
        caseFlagsService.setupCaseFlags(caseData);
        AllPartyFlags allPartyFlags = allPartyFlags(caseData);
        CaseFlagsType representativeInternalFlags = allPartyFlags.getRepresentative3Flags();
        CaseFlagsType representativeExternalFlags = allPartyFlags.getRepresentative3ExternalFlags();
        representativeInternalFlags.setDetails(ListTypeItem.from(activeFlag("Representative internal flag")));
        representativeExternalFlags.setDetails(ListTypeItem.from(activeFlag("Representative external flag")));

        caseFlagsService.clearRespondentRepresentativeFlags(caseData, List.of(2));

        assertAll(
                () -> assertNull(representativeInternalFlags.getDetails()),
                () -> assertNull(representativeExternalFlags.getDetails()),
                () -> assertNotNull(allPartyFlags.getRepresentativeFlags()),
                () -> assertNotNull(allPartyFlags.getRepresentative1Flags()),
                () -> assertNull(allPartyFlags.getRepresentative2Flags())
        );
    }

    @Test
    void setupCaseFlags_shouldMoveRespondentFlagsWithRespondentWhenCollectionIsReordered() {
        caseData.setRespondentCollection(respondentCollection(3));
        caseFlagsService.setupCaseFlags(caseData);
        allPartyFlags(caseData).getRespondent1Flags()
                .setDetails(ListTypeItem.from(activeFlag("Respondent 2 internal flag")));
        allPartyFlags(caseData).getRespondent1ExternalFlags()
                .setDetails(ListTypeItem.from(activeFlag("Respondent 2 external flag")));
        allPartyFlags(caseData).getRespondent2Flags()
                .setDetails(ListTypeItem.from(activeFlag("Respondent 3 internal flag")));
        allPartyFlags(caseData).getRespondent2ExternalFlags()
                .setDetails(ListTypeItem.from(activeFlag("Respondent 3 external flag")));

        caseFlagsService.inactivateRespondentCaseFlags(caseData, respondentName(1));
        caseData.setRespondentCollection(List.of(
                caseData.getRespondentCollection().get(0),
                caseData.getRespondentCollection().get(2),
                caseData.getRespondentCollection().get(1)
        ));
        caseFlagsService.setupCaseFlags(caseData);

        CaseFlagsType secondRespondentInternalFlags = allPartyFlags(caseData).getRespondent1Flags();
        assertEquals(respondentName(2), secondRespondentInternalFlags.getPartyName());
        assertEquals(RESPONDENT2, secondRespondentInternalFlags.getRoleOnCase());
        assertEquals(RESPONDENT2, secondRespondentInternalFlags.getGroupId());
        assertEquals(ACTIVE, secondRespondentInternalFlags.getDetails().getFirst().getValue().getStatus());
        assertEquals("Respondent 3 internal flag",
                secondRespondentInternalFlags.getDetails().getFirst().getValue().getName());

        CaseFlagsType secondRespondentExternalFlags = allPartyFlags(caseData).getRespondent1ExternalFlags();
        assertEquals(respondentName(2), secondRespondentExternalFlags.getPartyName());
        assertEquals(RESPONDENT2, secondRespondentExternalFlags.getRoleOnCase());
        assertEquals(RESPONDENT2, secondRespondentExternalFlags.getGroupId());
        assertEquals(ACTIVE, secondRespondentExternalFlags.getDetails().getFirst().getValue().getStatus());
        assertEquals("Respondent 3 external flag",
                secondRespondentExternalFlags.getDetails().getFirst().getValue().getName());

        CaseFlagsType discontinuedRespondentInternalFlags = allPartyFlags(caseData).getRespondent2Flags();
        assertEquals(respondentName(1), discontinuedRespondentInternalFlags.getPartyName());
        assertEquals(RESPONDENT3, discontinuedRespondentInternalFlags.getRoleOnCase());
        assertEquals(RESPONDENT3, discontinuedRespondentInternalFlags.getGroupId());
        assertEquals(INACTIVE, discontinuedRespondentInternalFlags.getDetails().getFirst().getValue().getStatus());
        assertNull(discontinuedRespondentInternalFlags.getDetails().getFirst().getValue().getFlagComment());
        assertEquals("Respondent 2 internal flag",
                discontinuedRespondentInternalFlags.getDetails().getFirst().getValue().getName());

        CaseFlagsType discontinuedRespondentExternalFlags = allPartyFlags(caseData).getRespondent2ExternalFlags();
        assertEquals(respondentName(1), discontinuedRespondentExternalFlags.getPartyName());
        assertEquals(RESPONDENT3, discontinuedRespondentExternalFlags.getRoleOnCase());
        assertEquals(RESPONDENT3, discontinuedRespondentExternalFlags.getGroupId());
        assertEquals(INACTIVE, discontinuedRespondentExternalFlags.getDetails().getFirst().getValue().getStatus());
        assertNull(discontinuedRespondentExternalFlags.getDetails().getFirst().getValue().getFlagComment());
        assertEquals("Respondent 2 external flag",
                discontinuedRespondentExternalFlags.getDetails().getFirst().getValue().getName());
    }

    @Test
    void setupCaseFlags_shouldMoveRepresentativeFlagsWithRepresentativeWhenCollectionIsReordered() {
        caseData.setRepCollection(representativeCollection(3));
        caseFlagsService.setupCaseFlags(caseData);
        allPartyFlags(caseData).getRepresentative1Flags()
                .setDetails(ListTypeItem.from(activeFlag("Representative 2 internal flag")));
        allPartyFlags(caseData).getRepresentative1ExternalFlags()
                .setDetails(ListTypeItem.from(activeFlag("Representative 2 external flag")));
        allPartyFlags(caseData).getRepresentative2Flags()
                .setDetails(ListTypeItem.from(activeFlag("Representative 3 internal flag")));
        allPartyFlags(caseData).getRepresentative2ExternalFlags()
                .setDetails(ListTypeItem.from(activeFlag("Representative 3 external flag")));

        caseData.setRepCollection(List.of(
                caseData.getRepCollection().get(0),
                caseData.getRepCollection().get(2),
                caseData.getRepCollection().get(1)
        ));
        caseFlagsService.setupCaseFlags(caseData);

        CaseFlagsType secondRepresentativeInternalFlags = allPartyFlags(caseData).getRepresentative1Flags();
        assertEquals(representativeName(2), secondRepresentativeInternalFlags.getPartyName());
        assertEquals(REPRESENTATIVE2, secondRepresentativeInternalFlags.getRoleOnCase());
        assertEquals(REPRESENTATIVE2, secondRepresentativeInternalFlags.getGroupId());
        assertEquals("Representative 3 internal flag",
                secondRepresentativeInternalFlags.getDetails().getFirst().getValue().getName());

        CaseFlagsType secondRepresentativeExternalFlags = allPartyFlags(caseData).getRepresentative1ExternalFlags();
        assertEquals(representativeName(2), secondRepresentativeExternalFlags.getPartyName());
        assertEquals(REPRESENTATIVE2, secondRepresentativeExternalFlags.getRoleOnCase());
        assertEquals(REPRESENTATIVE2, secondRepresentativeExternalFlags.getGroupId());
        assertEquals("Representative 3 external flag",
                secondRepresentativeExternalFlags.getDetails().getFirst().getValue().getName());

        CaseFlagsType thirdRepresentativeInternalFlags = allPartyFlags(caseData).getRepresentative2Flags();
        assertEquals(representativeName(1), thirdRepresentativeInternalFlags.getPartyName());
        assertEquals(REPRESENTATIVE3, thirdRepresentativeInternalFlags.getRoleOnCase());
        assertEquals(REPRESENTATIVE3, thirdRepresentativeInternalFlags.getGroupId());
        assertEquals("Representative 2 internal flag",
                thirdRepresentativeInternalFlags.getDetails().getFirst().getValue().getName());

        CaseFlagsType thirdRepresentativeExternalFlags = allPartyFlags(caseData).getRepresentative2ExternalFlags();
        assertEquals(representativeName(1), thirdRepresentativeExternalFlags.getPartyName());
        assertEquals(REPRESENTATIVE3, thirdRepresentativeExternalFlags.getRoleOnCase());
        assertEquals(REPRESENTATIVE3, thirdRepresentativeExternalFlags.getGroupId());
        assertEquals("Representative 2 external flag",
                thirdRepresentativeExternalFlags.getDetails().getFirst().getValue().getName());
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

    @Test
    void clearClaimantRepresentativeFlagsIfRepresentativeChanged_shouldRemoveDetailsWhenNameChanges() {
        CaseData caseDataBefore = CaseDataBuilder.builder().build();
        caseDataBefore.setRepresentativeClaimantType(RepresentedTypeC.builder()
                .nameOfRepresentative(CLAIMANT_REPRESENTATIVE_NAME)
                .representativeEmailAddress("claimant.rep@example.com")
                .build());
        caseData.getRepresentativeClaimantType().setRepresentativeEmailAddress("claimant.rep@example.com");
        caseFlagsService.setupCaseFlags(caseData);
        allPartyFlags(caseData).getClaimantRepresentativeFlags()
                .setDetails(ListTypeItem.from(activeFlag("Previous internal rep flag")));
        allPartyFlags(caseData).getClaimantRepresentativeExternalFlags()
                .setDetails(ListTypeItem.from(activeFlag("Previous external rep flag")));
        caseData.getRepresentativeClaimantType().setNameOfRepresentative("Updated Claimant Representative Name");

        caseFlagsService.clearClaimantRepresentativeFlagsIfRepresentativeChanged(caseData, caseDataBefore);
        caseFlagsService.setupCaseFlags(caseData);

        assertNull(allPartyFlags(caseData).getClaimantRepresentativeFlags().getDetails());
        assertNull(allPartyFlags(caseData).getClaimantRepresentativeExternalFlags().getDetails());
        assertEquals("Updated Claimant Representative Name",
                allPartyFlags(caseData).getClaimantRepresentativeExternalFlags().getPartyName());
    }

    @Test
    void clearClaimantRepresentativeFlagsIfRepresentativeChanged_shouldRemoveDetailsWhenEmailChanges() {
        CaseData caseDataBefore = CaseDataBuilder.builder().build();
        caseDataBefore.setRepresentativeClaimantType(RepresentedTypeC.builder()
                .nameOfRepresentative(CLAIMANT_REPRESENTATIVE_NAME)
                .representativeEmailAddress("old.claimant.rep@example.com")
                .build());
        caseData.getRepresentativeClaimantType().setRepresentativeEmailAddress("new.claimant.rep@example.com");
        caseFlagsService.setupCaseFlags(caseData);
        allPartyFlags(caseData).getClaimantRepresentativeFlags()
                .setDetails(ListTypeItem.from(activeFlag("Previous internal rep flag")));
        allPartyFlags(caseData).getClaimantRepresentativeExternalFlags()
                .setDetails(ListTypeItem.from(activeFlag("Previous external rep flag")));

        caseFlagsService.clearClaimantRepresentativeFlagsIfRepresentativeChanged(caseData, caseDataBefore);
        caseFlagsService.setupCaseFlags(caseData);

        assertNull(allPartyFlags(caseData).getClaimantRepresentativeFlags().getDetails());
        assertNull(allPartyFlags(caseData).getClaimantRepresentativeExternalFlags().getDetails());
        assertEquals(CLAIMANT_REPRESENTATIVE_NAME,
                allPartyFlags(caseData).getClaimantRepresentativeExternalFlags().getPartyName());
    }

    @Test
    void clearClaimantRepresentativeFlagsIfRepresentativeChanged_shouldKeepDetailsWhenOnlyOrganisationChanges() {
        CaseData caseDataBefore = CaseDataBuilder.builder().build();
        caseDataBefore.setRepresentativeClaimantType(RepresentedTypeC.builder()
                .nameOfRepresentative(CLAIMANT_REPRESENTATIVE_NAME)
                .representativeEmailAddress("claimant.rep@example.com")
                .build());
        caseData.getRepresentativeClaimantType().setRepresentativeEmailAddress("claimant.rep@example.com");
        caseFlagsService.setupCaseFlags(caseData);
        allPartyFlags(caseData).getClaimantRepresentativeExternalFlags()
                .setDetails(ListTypeItem.from(activeFlag("Existing external rep flag")));

        caseFlagsService.clearClaimantRepresentativeFlagsIfRepresentativeChanged(caseData, caseDataBefore);
        caseFlagsService.setupCaseFlags(caseData);

        assertNotNull(allPartyFlags(caseData).getClaimantRepresentativeExternalFlags().getDetails());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("respondentRepresentativePartyFlagSlots")
    void setupCaseFlags_shouldRemoveRespondentRepresentativeFlagDetailsWhenNameChanges(FlagSlot slot) {
        caseFlagsService.setupCaseFlags(caseData);
        Objects.requireNonNull(slot.get(caseData)).setDetails(ListTypeItem.from(activeFlag("Previous rep flag")));
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
        assertNull(Objects.requireNonNull(slot.get(caseData)).getDetails());
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

    private static RepresentedTypeRItem representativeItem(String id, String name) {
        RepresentedTypeR representative = RepresentedTypeR.builder()
                .nameOfRepresentative(name)
                .build();
        return representativeItem(id, representative);
    }

    private static RepresentedTypeRItem representativeItem(String id, String name, String email) {
        RepresentedTypeR representative = RepresentedTypeR.builder()
                .nameOfRepresentative(name)
                .representativeEmailAddress(email)
                .build();
        return representativeItem(id, representative);
    }

    private static RepresentedTypeRItem representativeItem(String id, RepresentedTypeR representative) {
        RepresentedTypeRItem representativeItem = new RepresentedTypeRItem();
        representativeItem.setId(id);
        representativeItem.setValue(representative);
        return representativeItem;
    }

    private static RepresentedTypeRItem representativeItemForRespondent(
            String id, String name, String email, RespondentSumTypeItem respondent) {
        return representativeItemForRespondent(id, name, email, respondent, null);
    }

    private static RepresentedTypeRItem representativeItemForRespondent(
            String id, String name, String email, RespondentSumTypeItem respondent, String role) {
        RepresentedTypeR representative = RepresentedTypeR.builder()
                .nameOfRepresentative(name)
                .representativeEmailAddress(email)
                .respondentId(respondent.getId())
                .respRepName(respondent.getValue().getRespondentName())
                .role(role)
                .build();
        return representativeItem(id, representative);
    }

    private void setupRepresentativeAccessScenario() {
        caseData.setRespondentCollection(respondentCollection(4));
        List<RespondentSumTypeItem> respondents = caseData.getRespondentCollection();
        caseData.setRepCollection(List.of(
                representativeItemForRespondent("1", "Representative 1", "rep1@example.com", respondents.get(0)),
                representativeItemForRespondent("2", "Representative 2", "rep2@example.com", respondents.get(1)),
                representativeItemForRespondent("3", "Representative 2", "rep2@example.com", respondents.get(2)),
                representativeItemForRespondent("4", "Representative 3", "rep3@example.com", respondents.get(3))
        ));
        caseFlagsService.setupCaseFlags(caseData);
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

    private static CaseFlagsType representativeFlags(
            String partyName, String roleOnCase, String visibility, String flagName) {
        return CaseFlagsType.builder()
                .partyName(partyName)
                .roleOnCase(roleOnCase)
                .groupId(roleOnCase)
                .visibility(visibility)
                .details(ListTypeItem.from(activeFlag(flagName)))
                .build();
    }

    private static CaseFlagsType legacyCaseFlags(FlagDetailType...details) {
        return CaseFlagsType.builder()
                .details(ListTypeItem.from(details))
                .build();
    }

    private static CaseFlagsType legacyCaseFlags(String partyName, FlagDetailType...details) {
        return CaseFlagsType.builder()
                .partyName(partyName)
                .details(ListTypeItem.from(details))
                .build();
    }

    private static FlagDetailType flagDetail(String name, String status, String availableExternally) {
        return FlagDetailType.builder()
                .name(name)
                .status(status)
                .availableExternally(availableExternally)
                .build();
    }

    private static FlagDetailType flagDetail(String name, String flagCode, String status, String availableExternally) {
        return FlagDetailType.builder()
                .name(name)
                .flagCode(flagCode)
                .status(status)
                .availableExternally(availableExternally)
                .build();
    }

    private static FlagDetailType activeFlag(String name) {
        return FlagDetailType.builder()
                .name(name)
                .status(ACTIVE)
                .build();
    }

    private static String firstFlagStatus(CaseFlagsType flags) {
        return flags.getDetails().getFirst().getValue().getStatus();
    }

    private static List<String> flagNames(CaseFlagsType flags) {
        return flags.getDetails().stream()
                .map(GenericTypeItem::getValue)
                .map(FlagDetailType::getName)
                .toList();
    }

    private static String firstFlagComment(CaseFlagsType flags) {
        return flags.getDetails().getFirst().getValue().getFlagComment();
    }

    private static void inactivateCaseFlags(CaseFlagsType flags) {
        flags.getDetails()
                .forEach(flag -> flag.getValue().setStatus(INACTIVE));
    }

    private static void assertRepresentativeFlags(
            CaseFlagsType flags, String partyName, String roleOnCase, String status, String flagName) {
        assertEquals(partyName, flags.getPartyName());
        assertEquals(roleOnCase, flags.getRoleOnCase());
        assertEquals(roleOnCase, flags.getGroupId());
        assertEquals(status, firstFlagStatus(flags));
        assertEquals(flagName, flags.getDetails().getFirst().getValue().getName());
    }

    private static void assertMigratedFlag(
            CaseFlagsType flags, String partyName, String roleOnCase, String visibility, FlagDetailType detail) {
        assertNotNull(flags);
        assertAll(roleOnCase + " " + visibility,
                () -> assertEquals(partyName, flags.getPartyName()),
                () -> assertEquals(roleOnCase, flags.getRoleOnCase()),
                () -> assertEquals(roleOnCase, flags.getGroupId()),
                () -> assertEquals(visibility, flags.getVisibility()),
                () -> assertEquals(1, flags.getDetails().size()),
                () -> assertSame(detail, flags.getDetails().getFirst().getValue())
        );
    }

    private static List<String> flagCodes(CaseFlagsType flags) {
        return flags.getDetails().stream()
                .map(item -> item.getValue().getFlagCode())
                .toList();
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

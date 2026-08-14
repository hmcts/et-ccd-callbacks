package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
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
import uk.gov.hmcts.ethos.replacement.docmosis.utils.noc.RoleUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import javax.annotation.Nullable;

import static uk.gov.hmcts.ecm.common.model.helper.CaseFlagConstants.ACTIVE;
import static uk.gov.hmcts.ecm.common.model.helper.CaseFlagConstants.CLAIMANT;
import static uk.gov.hmcts.ecm.common.model.helper.CaseFlagConstants.CLAIMANT_REPRESENTATIVE;
import static uk.gov.hmcts.ecm.common.model.helper.CaseFlagConstants.DISRUPTIVE_CUSTOMER;
import static uk.gov.hmcts.ecm.common.model.helper.CaseFlagConstants.EXTERNAL;
import static uk.gov.hmcts.ecm.common.model.helper.CaseFlagConstants.GRANTED;
import static uk.gov.hmcts.ecm.common.model.helper.CaseFlagConstants.INACTIVE;
import static uk.gov.hmcts.ecm.common.model.helper.CaseFlagConstants.INTERNAL;
import static uk.gov.hmcts.ecm.common.model.helper.CaseFlagConstants.LANGUAGE_INTERPRETER;
import static uk.gov.hmcts.ecm.common.model.helper.CaseFlagConstants.NOT_INDEXED;
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

@Slf4j
@Service
public class CaseFlagsService {
    private static final List<PartyFlag> PARTY_FLAGS = List.of(
            claimantFlag(AllPartyFlags::getClaimantFlags, AllPartyFlags::setClaimantFlags, INTERNAL),
            claimantFlag(AllPartyFlags::getClaimantExternalFlags, AllPartyFlags::setClaimantExternalFlags, EXTERNAL),

            respondentFlag(AllPartyFlags::getRespondentFlags, AllPartyFlags::setRespondentFlags,
                    RESPONDENT1, INTERNAL, 0),
            respondentFlag(AllPartyFlags::getRespondentExternalFlags, AllPartyFlags::setRespondentExternalFlags,
                    RESPONDENT1, EXTERNAL, 0),
            respondentFlag(AllPartyFlags::getRespondent1Flags, AllPartyFlags::setRespondent1Flags,
                    RESPONDENT2, INTERNAL, 1),
            respondentFlag(AllPartyFlags::getRespondent1ExternalFlags, AllPartyFlags::setRespondent1ExternalFlags,
                    RESPONDENT2, EXTERNAL, 1),
            respondentFlag(AllPartyFlags::getRespondent2Flags, AllPartyFlags::setRespondent2Flags,
                    RESPONDENT3, INTERNAL, 2),
            respondentFlag(AllPartyFlags::getRespondent2ExternalFlags, AllPartyFlags::setRespondent2ExternalFlags,
                    RESPONDENT3, EXTERNAL, 2),
            respondentFlag(AllPartyFlags::getRespondent3Flags, AllPartyFlags::setRespondent3Flags,
                    RESPONDENT4, INTERNAL, 3),
            respondentFlag(AllPartyFlags::getRespondent3ExternalFlags, AllPartyFlags::setRespondent3ExternalFlags,
                    RESPONDENT4, EXTERNAL, 3),
            respondentFlag(AllPartyFlags::getRespondent4Flags, AllPartyFlags::setRespondent4Flags,
                    RESPONDENT5, INTERNAL, 4),
            respondentFlag(AllPartyFlags::getRespondent4ExternalFlags, AllPartyFlags::setRespondent4ExternalFlags,
                    RESPONDENT5, EXTERNAL, 4),
            respondentFlag(AllPartyFlags::getRespondent5Flags, AllPartyFlags::setRespondent5Flags,
                    RESPONDENT6, INTERNAL, 5),
            respondentFlag(AllPartyFlags::getRespondent5ExternalFlags, AllPartyFlags::setRespondent5ExternalFlags,
                    RESPONDENT6, EXTERNAL, 5),
            respondentFlag(AllPartyFlags::getRespondent6Flags, AllPartyFlags::setRespondent6Flags,
                    RESPONDENT7, INTERNAL, 6),
            respondentFlag(AllPartyFlags::getRespondent6ExternalFlags, AllPartyFlags::setRespondent6ExternalFlags,
                    RESPONDENT7, EXTERNAL, 6),
            respondentFlag(AllPartyFlags::getRespondent7Flags, AllPartyFlags::setRespondent7Flags,
                    RESPONDENT8, INTERNAL, 7),
            respondentFlag(AllPartyFlags::getRespondent7ExternalFlags, AllPartyFlags::setRespondent7ExternalFlags,
                    RESPONDENT8, EXTERNAL, 7),
            respondentFlag(AllPartyFlags::getRespondent8Flags, AllPartyFlags::setRespondent8Flags,
                    RESPONDENT9, INTERNAL, 8),
            respondentFlag(AllPartyFlags::getRespondent8ExternalFlags, AllPartyFlags::setRespondent8ExternalFlags,
                    RESPONDENT9, EXTERNAL, 8),
            respondentFlag(AllPartyFlags::getRespondent9Flags, AllPartyFlags::setRespondent9Flags, RESPONDENT10,
                    INTERNAL, 9),
            respondentFlag(AllPartyFlags::getRespondent9ExternalFlags, AllPartyFlags::setRespondent9ExternalFlags,
                    RESPONDENT10, EXTERNAL, 9),

            claimantRepresentativeFlag(AllPartyFlags::getClaimantRepresentativeFlags,
                    AllPartyFlags::setClaimantRepresentativeFlags, INTERNAL),
            claimantRepresentativeFlag(AllPartyFlags::getClaimantRepresentativeExternalFlags,
                    AllPartyFlags::setClaimantRepresentativeExternalFlags, EXTERNAL),

            representativeFlag(AllPartyFlags::getRepresentativeFlags, AllPartyFlags::setRepresentativeFlags,
                    REPRESENTATIVE1, INTERNAL, 0),
            representativeFlag(AllPartyFlags::getRepresentativeExternalFlags,
                    AllPartyFlags::setRepresentativeExternalFlags,
                    REPRESENTATIVE1, EXTERNAL, 0),
            representativeFlag(AllPartyFlags::getRepresentative1Flags, AllPartyFlags::setRepresentative1Flags,
                    REPRESENTATIVE2, INTERNAL, 1),
            representativeFlag(AllPartyFlags::getRepresentative1ExternalFlags,
                    AllPartyFlags::setRepresentative1ExternalFlags,
                    REPRESENTATIVE2, EXTERNAL, 1),
            representativeFlag(AllPartyFlags::getRepresentative2Flags, AllPartyFlags::setRepresentative2Flags,
                    REPRESENTATIVE3, INTERNAL, 2),
            representativeFlag(AllPartyFlags::getRepresentative2ExternalFlags,
                    AllPartyFlags::setRepresentative2ExternalFlags,
                    REPRESENTATIVE3, EXTERNAL, 2),
            representativeFlag(AllPartyFlags::getRepresentative3Flags, AllPartyFlags::setRepresentative3Flags,
                    REPRESENTATIVE4, INTERNAL, 3),
            representativeFlag(AllPartyFlags::getRepresentative3ExternalFlags,
                    AllPartyFlags::setRepresentative3ExternalFlags,
                    REPRESENTATIVE4, EXTERNAL, 3),
            representativeFlag(AllPartyFlags::getRepresentative4Flags, AllPartyFlags::setRepresentative4Flags,
                    REPRESENTATIVE5, INTERNAL, 4),
            representativeFlag(AllPartyFlags::getRepresentative4ExternalFlags,
                    AllPartyFlags::setRepresentative4ExternalFlags,
                    REPRESENTATIVE5, EXTERNAL, 4),
            representativeFlag(AllPartyFlags::getRepresentative5Flags, AllPartyFlags::setRepresentative5Flags,
                    REPRESENTATIVE6, INTERNAL, 5),
            representativeFlag(AllPartyFlags::getRepresentative5ExternalFlags,
                    AllPartyFlags::setRepresentative5ExternalFlags,
                    REPRESENTATIVE6, EXTERNAL, 5),
            representativeFlag(AllPartyFlags::getRepresentative6Flags, AllPartyFlags::setRepresentative6Flags,
                    REPRESENTATIVE7, INTERNAL, 6),
            representativeFlag(AllPartyFlags::getRepresentative6ExternalFlags,
                    AllPartyFlags::setRepresentative6ExternalFlags,
                    REPRESENTATIVE7, EXTERNAL, 6),
            representativeFlag(AllPartyFlags::getRepresentative7Flags, AllPartyFlags::setRepresentative7Flags,
                    REPRESENTATIVE8, INTERNAL, 7),
            representativeFlag(AllPartyFlags::getRepresentative7ExternalFlags,
                    AllPartyFlags::setRepresentative7ExternalFlags,
                    REPRESENTATIVE8, EXTERNAL, 7),
            representativeFlag(AllPartyFlags::getRepresentative8Flags, AllPartyFlags::setRepresentative8Flags,
                    REPRESENTATIVE9, INTERNAL, 8),
            representativeFlag(AllPartyFlags::getRepresentative8ExternalFlags,
                    AllPartyFlags::setRepresentative8ExternalFlags,
                    REPRESENTATIVE9, EXTERNAL, 8),
            representativeFlag(AllPartyFlags::getRepresentative9Flags, AllPartyFlags::setRepresentative9Flags,
                    REPRESENTATIVE10, INTERNAL, 9),
            representativeFlag(AllPartyFlags::getRepresentative9ExternalFlags,
                    AllPartyFlags::setRepresentative9ExternalFlags,
                    REPRESENTATIVE10, EXTERNAL, 9)
    );

    public boolean caseFlagsSetupRequired(CaseData caseData) {
        return getCaseFlags(caseData) == null
                || PARTY_FLAGS.stream()
                        .filter(flag -> isRequired(caseData, flag))
                        .anyMatch(flag -> partyFlagSetupRequired(caseData, flag))
                || hasStaleRespondentRepresentativeFlags(caseData);
    }

    /**
     * Setup case flags for Claimant, Respondent, Representative and Case level.
     *
     * @param caseData Data about the current case
     */
    public void setupCaseFlags(CaseData caseData) {
        if (getCaseFlags(caseData) == null) {
            setCaseFlags(caseData, CaseFlagsType.builder().build());
        }

        List<ExistingRepresentativeFlag> existingRepresentativeFlags = currentRespondentRepresentativeFlags(caseData);
        alignIndexedFlagsWithCurrentOrder(caseData);
        PARTY_FLAGS.stream()
                .filter(flag -> hasParty(caseData, flag))
                .forEach(flag -> setupPartyFlag(caseData, flag));

        alignRespondentRepresentativeFlagDetails(caseData, existingRepresentativeFlags);
        clearStaleRespondentRepresentativeFlags(caseData);
    }

    /**
     * Sets case flags for Claimant, Respondent, Representative and Case level to null.
     *
     * @param caseData Data about the current case
     */
    public void rollbackCaseFlags(CaseData caseData) {
        setCaseFlags(caseData, null);
        PARTY_FLAGS.forEach(flag -> flag.clear(caseData));
    }

    /**
     * Sets additional flags on CaseData dependent on CaseFlags raised.
     * @param caseData Data about the current case.
     */
    public void processNewlySetCaseFlags(CaseData caseData) {
        ListTypeItem<FlagDetailType> partyLevel = getPartyCaseFlags(caseData);
        caseData.setCaseInterpreterRequiredFlag(
                areAnyFlagsActive(partyLevel, SIGN_LANGUAGE_INTERPRETER, LANGUAGE_INTERPRETER) ? YES : NO
        );

        caseData.setCaseAdditionalSecurityFlag(
                areAnyFlagsActive(partyLevel, VEXATIOUS_LITIGANT, DISRUPTIVE_CUSTOMER) ? YES : NO
        );
    }

    /**
     * Sets the privateHearingFlag based various case states and events.
     * @param caseData Data about the case
     */
    public void setPrivateHearingFlag(CaseData caseData) {
        boolean shouldBePrivate = hasGrantedRestrictedPublicityDecision(caseData)
                || isFlaggedForRestrictedReporting(caseData)
                || YES.equals(caseData.getIcListingPreliminaryHearing());

        caseData.setPrivateHearingRequiredFlag(shouldBePrivate ? YES : NO);
    }

    /**
     * Inactivates all case flag details for a respondent.
     *
     * @param caseData Data about the current case
     * @param respondentName Name of the respondent
     */
    public void inactivateRespondentCaseFlags(CaseData caseData, String respondentName) {
        if (caseData == null || StringUtils.isBlank(respondentName)) {
            return;
        }

        PARTY_FLAGS.stream()
                .filter(flag -> PartyType.RESPONDENT.equals(flag.partyType()))
                .map(flag -> flag.get(caseData))
                .filter(flags -> flags != null && Objects.equals(respondentName, flags.getPartyName()))
                .forEach(this::inactivateCaseFlags);
    }

    private void inactivateCaseFlags(CaseFlagsType flags) {
        if (flags.getDetails() == null) {
            return;
        }

        flags.getDetails().stream()
                .map(GenericTypeItem::getValue)
                .filter(Objects::nonNull)
                .forEach(flag -> flag.setStatus(INACTIVE));
    }

    /**
     * Inactivates respondent representative flags when the representative no longer has active respondents.
     *
     * @param caseData Data about the current case
     * @param inactiveRespondent Respondent that has become struck out or no longer continuing
     */
    public void inactivateRespondentRepresentativeCaseFlags(
            CaseData caseData, RespondentSumTypeItem inactiveRespondent) {
        if (caseData == null || inactiveRespondent == null || caseData.getRepCollection() == null) {
            return;
        }

        List<RepresentativeIdentity> inactiveRepresentativeIdentities = caseData.getRepCollection().stream()
                .map(RepresentedTypeRItem::getValue)
                .filter(Objects::nonNull)
                .filter(representative -> representsRespondent(representative, inactiveRespondent))
                .map(CaseFlagsService::representativeIdentity)
                .filter(Objects::nonNull)
                .distinct()
                .filter(identity -> !representsAnyActiveRespondent(caseData, identity))
                .toList();

        if (inactiveRepresentativeIdentities.isEmpty()) {
            return;
        }

        PARTY_FLAGS.stream()
                .filter(flag -> PartyType.RESPONDENT_REPRESENTATIVE.equals(flag.partyType()))
                .filter(flag -> hasUniqueRepresentativeForFlagSlot(caseData, flag.index()))
                .filter(flag -> inactiveRepresentativeIdentities.contains(representativeIdentity(
                        representativeForFlagSlot(caseData, flag.index()))))
                .map(flag -> flag.get(caseData))
                .filter(Objects::nonNull)
                .forEach(this::inactivateCaseFlags);
    }

    public void clearRespondentRepresentativeFlags(CaseData caseData, List<Integer> representativeIndexes) {
        if (caseData == null || representativeIndexes == null || representativeIndexes.isEmpty()) {
            return;
        }

        List<Integer> representativeFlagSlotIndexes = representativeFlagSlotIndexes(caseData, representativeIndexes);
        PARTY_FLAGS.stream()
                .filter(flag -> PartyType.RESPONDENT_REPRESENTATIVE.equals(flag.partyType()))
                .filter(flag -> representativeFlagSlotIndexes.contains(flag.index()))
                .map(flag -> flag.get(caseData))
                .filter(Objects::nonNull)
                .forEach(flags -> flags.setDetails(null));
    }

    public void clearClaimantRepresentativeFlagsIfRepresentativeChanged(CaseData caseData, CaseData caseDataBefore) {
        if (caseData == null || caseDataBefore == null
                || !claimantRepresentativeChanged(caseData.getRepresentativeClaimantType(),
                caseDataBefore.getRepresentativeClaimantType())) {
            return;
        }

        PARTY_FLAGS.stream()
                .filter(flag -> PartyType.CLAIMANT_REPRESENTATIVE.equals(flag.partyType()))
                .map(flag -> flag.get(caseData))
                .filter(Objects::nonNull)
                .forEach(flags -> flags.setDetails(null));
    }

    private static boolean claimantRepresentativeChanged(RepresentedTypeC current, RepresentedTypeC previous) {
        if (current == null || previous == null) {
            return false;
        }

        return !Objects.equals(
                normaliseRepresentativeIdentity(current.getNameOfRepresentative()),
                normaliseRepresentativeIdentity(previous.getNameOfRepresentative()))
                || !Objects.equals(
                normaliseRepresentativeIdentity(current.getRepresentativeEmailAddress()),
                normaliseRepresentativeIdentity(previous.getRepresentativeEmailAddress()));
    }

    private static PartyFlag claimantFlag(
            Function<AllPartyFlags, CaseFlagsType> getter,
            BiConsumer<AllPartyFlags, CaseFlagsType> setter,
            String visibility) {
        return new PartyFlag(getter, setter, PartyType.CLAIMANT, CLAIMANT, visibility, NOT_INDEXED);
    }

    private static PartyFlag respondentFlag(
            Function<AllPartyFlags, CaseFlagsType> getter,
            BiConsumer<AllPartyFlags, CaseFlagsType> setter,
            String roleOnCase,
            String visibility,
            int index) {
        return new PartyFlag(getter, setter, PartyType.RESPONDENT, roleOnCase, visibility, index);
    }

    private static PartyFlag claimantRepresentativeFlag(
            Function<AllPartyFlags, CaseFlagsType> getter,
            BiConsumer<AllPartyFlags, CaseFlagsType> setter,
            String visibility) {
        return new PartyFlag(getter, setter, PartyType.CLAIMANT_REPRESENTATIVE, CLAIMANT_REPRESENTATIVE,
                visibility, NOT_INDEXED);
    }

    private static PartyFlag representativeFlag(
            Function<AllPartyFlags, CaseFlagsType> getter,
            BiConsumer<AllPartyFlags, CaseFlagsType> setter,
            String roleOnCase,
            String visibility,
            int index) {
        return new PartyFlag(getter, setter, PartyType.RESPONDENT_REPRESENTATIVE, roleOnCase, visibility, index);
    }

    private static CaseFlagsType getCaseFlags(CaseData caseData) {
        AllPartyFlags allPartyFlags = caseData.getAllPartyFlags();
        return allPartyFlags == null ? null : allPartyFlags.getCaseFlags();
    }

    private static void setCaseFlags(CaseData caseData, CaseFlagsType caseFlags) {
        if (caseFlags == null && caseData.getAllPartyFlags() == null) {
            return;
        }
        getOrCreateAllPartyFlags(caseData).setCaseFlags(caseFlags);
    }

    private static AllPartyFlags getOrCreateAllPartyFlags(CaseData caseData) {
        if (caseData.getAllPartyFlags() == null) {
            caseData.setAllPartyFlags(new AllPartyFlags());
        }
        return caseData.getAllPartyFlags();
    }

    private static boolean isRequired(CaseData caseData, PartyFlag flag) {
        return switch (flag.partyType()) {
            case CLAIMANT, CLAIMANT_REPRESENTATIVE -> true;
            case RESPONDENT -> respondentCount(caseData) > flag.index();
            case RESPONDENT_REPRESENTATIVE -> hasUniqueRepresentativeForFlagSlot(caseData, flag.index());
        };
    }

    private static boolean hasParty(CaseData caseData, PartyFlag flag) {
        return switch (flag.partyType()) {
            case CLAIMANT -> true;
            case RESPONDENT -> respondentCount(caseData) > flag.index();
            case CLAIMANT_REPRESENTATIVE -> caseData.getRepresentativeClaimantType() != null;
            case RESPONDENT_REPRESENTATIVE -> hasUniqueRepresentativeForFlagSlot(caseData, flag.index());
        };
    }

    private static boolean partyFlagSetupRequired(CaseData caseData, PartyFlag flag) {
        CaseFlagsType flags = flag.get(caseData);
        return flags == null
                || StringUtils.isEmpty(flags.getRoleOnCase())
                || partyNameChanged(caseData, flag, flags);
    }

    private static boolean partyNameChanged(CaseData caseData, PartyFlag flag, CaseFlagsType flags) {
        return hasParty(caseData, flag) && !Objects.equals(getPartyName(caseData, flag), flags.getPartyName());
    }

    private static void alignIndexedFlagsWithCurrentOrder(CaseData caseData) {
        // Discontinuance can move indexed parties, so keep flag details with the named party
        // before refreshing slot-specific roles.
        alignIndexedPartyFlags(caseData, PartyType.RESPONDENT, INTERNAL, respondentCount(caseData));
        alignIndexedPartyFlags(caseData, PartyType.RESPONDENT, EXTERNAL, respondentCount(caseData));
        alignIndexedPartyFlags(
                caseData, PartyType.RESPONDENT_REPRESENTATIVE, INTERNAL, representativeFlagSlotCount(caseData));
        alignIndexedPartyFlags(
                caseData, PartyType.RESPONDENT_REPRESENTATIVE, EXTERNAL, representativeFlagSlotCount(caseData));
    }

    private static void alignIndexedPartyFlags(
            CaseData caseData, PartyType partyType, String visibility, int partyCount) {
        List<PartyFlag> indexedFlags = PARTY_FLAGS.stream()
                .filter(flag -> partyType.equals(flag.partyType()))
                .filter(flag -> visibility.equals(flag.visibility()))
                .toList();
        List<String> currentPartyNames = currentPartyNames(caseData, indexedFlags, partyCount);
        Map<String, CaseFlagsType> existingFlagsByPartyName = hasDuplicatePartyNames(currentPartyNames)
                ? Map.of()
                : existingFlagsByPartyName(caseData, indexedFlags);
        List<CaseFlagsType> alignedFlags = new ArrayList<>();

        for (PartyFlag flag : indexedFlags) {
            if (flag.index() >= partyCount) {
                alignedFlags.add(null);
                continue;
            }

            String partyName = getPartyName(caseData, flag);
            CaseFlagsType matchedFlag = existingFlagsByPartyName.get(partyName);
            CaseFlagsType currentFlag = flag.get(caseData);
            alignedFlags.add(matchedFlag != null
                    ? matchedFlag
                    : keepCurrentFlagIfItDoesNotBelongToAnotherParty(currentFlag, currentPartyNames, partyName));
        }

        for (int i = 0; i < indexedFlags.size(); i++) {
            indexedFlags.get(i).set(caseData, alignedFlags.get(i));
        }
    }

    private static Map<String, CaseFlagsType> existingFlagsByPartyName(
            CaseData caseData, List<PartyFlag> indexedFlags) {
        Map<String, CaseFlagsType> flagsByPartyName = new HashMap<>();
        indexedFlags.stream()
                .map(flag -> flag.get(caseData))
                .filter(Objects::nonNull)
                .filter(flags -> StringUtils.isNotBlank(flags.getPartyName()))
                .forEach(flags -> flagsByPartyName.putIfAbsent(flags.getPartyName(), flags));
        return flagsByPartyName;
    }

    private static List<String> currentPartyNames(CaseData caseData, List<PartyFlag> indexedFlags, int partyCount) {
        return indexedFlags.stream()
                .filter(flag -> flag.index() < partyCount)
                .map(flag -> getPartyName(caseData, flag))
                .toList();
    }

    private static boolean hasDuplicatePartyNames(List<String> partyNames) {
        return partyNames.stream()
                .filter(StringUtils::isNotBlank)
                .distinct()
                .count() < partyNames.stream()
                .filter(StringUtils::isNotBlank)
                .count();
    }

    private static CaseFlagsType keepCurrentFlagIfItDoesNotBelongToAnotherParty(
            CaseFlagsType currentFlag, List<String> currentPartyNames, String partyName) {
        if (currentFlag == null
                || Objects.equals(currentFlag.getPartyName(), partyName)
                || !currentPartyNames.contains(currentFlag.getPartyName())) {
            return currentFlag;
        }
        return null;
    }

    private static void setupPartyFlag(CaseData caseData, PartyFlag flag) {
        String partyName = getPartyName(caseData, flag);
        CaseFlagsType existingFlag = flag.get(caseData);

        if (existingFlag == null
                || StringUtils.isEmpty(existingFlag.getRoleOnCase())
                || shouldResetRespondentRepresentativeFlags(flag, existingFlag, partyName)) {
            flag.set(caseData, createShellFlag(partyName, flag.roleOnCase(), flag.visibility()));
        } else {
            updateShellFlag(existingFlag, partyName, flag.roleOnCase(), flag.visibility());
        }
    }

    private static boolean shouldResetRespondentRepresentativeFlags(
            PartyFlag flag, CaseFlagsType existingFlag, String partyName) {
        return PartyType.RESPONDENT_REPRESENTATIVE.equals(flag.partyType())
                && !Objects.equals(partyName, existingFlag.getPartyName());
    }

    private static CaseFlagsType createShellFlag(String partyName, String roleOnCase, String visibility) {
        return CaseFlagsType.builder()
                .partyName(partyName)
                .roleOnCase(roleOnCase)
                .groupId(roleOnCase)
                .visibility(visibility)
                .build();
    }

    private static void updateShellFlag(CaseFlagsType existingFlag, String partyName, String roleOnCase,
                                        String visibility) {
        existingFlag.setPartyName(partyName);
        existingFlag.setRoleOnCase(roleOnCase);
        existingFlag.setGroupId(roleOnCase);
        existingFlag.setVisibility(visibility);
    }

    private static String getPartyName(CaseData caseData, PartyFlag flag) {
        return switch (flag.partyType()) {
            case CLAIMANT -> caseData.getClaimant();
            case RESPONDENT -> caseData.getRespondentCollection()
                    .get(flag.index())
                    .getValue()
                    .getRespondentName();
            case CLAIMANT_REPRESENTATIVE -> caseData.getRepresentativeClaimantType().getNameOfRepresentative();
            case RESPONDENT_REPRESENTATIVE -> representativePartyNameForFlagSlot(caseData, flag.index());
        };
    }

    private static int respondentCount(CaseData caseData) {
        return caseData.getRespondentCollection() == null ? 0 : caseData.getRespondentCollection().size();
    }

    private static boolean hasUniqueRepresentativeForFlagSlot(CaseData caseData, int index) {
        return representativeForFlagSlot(caseData, index) != null
                && !isDuplicateRepresentativeAtFlagSlot(caseData, index);
    }

    private static int representativeCount(CaseData caseData) {
        return caseData.getRepCollection() == null ? 0 : caseData.getRepCollection().size();
    }

    private static int representativeFlagSlotCount(CaseData caseData) {
        if (caseData.getRepCollection() == null) {
            return 0;
        }

        int slotCount = 0;
        for (int i = 0; i < caseData.getRepCollection().size(); i++) {
            slotCount = Math.max(slotCount, representativeFlagSlotIndex(caseData.getRepCollection().get(i), i) + 1);
        }
        return slotCount;
    }

    private static List<Integer> representativeFlagSlotIndexes(CaseData caseData, List<Integer> representativeIndexes) {
        if (caseData.getRepCollection() == null) {
            return representativeIndexes;
        }

        return representativeIndexes.stream()
                .map(index -> representativeIndexToFlagSlotIndex(caseData, index))
                .filter(index -> index >= 0)
                .toList();
    }

    private static int representativeIndexToFlagSlotIndex(CaseData caseData, int representativeIndex) {
        if (representativeIndex < 0 || representativeIndex >= representativeCount(caseData)) {
            return -1;
        }
        return representativeFlagSlotIndex(caseData.getRepCollection().get(representativeIndex), representativeIndex);
    }

    @Nullable
    private static RepresentedTypeR representativeForFlagSlot(CaseData caseData, int slotIndex) {
        if (caseData.getRepCollection() == null) {
            return null;
        }

        for (int i = 0; i < caseData.getRepCollection().size(); i++) {
            RepresentedTypeRItem representative = caseData.getRepCollection().get(i);
            if (representative != null
                    && representative.getValue() != null
                    && representativeFlagSlotIndex(representative, i) == slotIndex) {
                return representative.getValue();
            }
        }
        return null;
    }

    private static int representativeFlagSlotIndex(RepresentedTypeRItem representative, int fallbackIndex) {
        if (representative == null || representative.getValue() == null) {
            return fallbackIndex;
        }

        int roleIndex = RoleUtils.findRoleIndexByRoleLabel(representative.getValue().getRole());
        return roleIndex >= 0 ? roleIndex : fallbackIndex;
    }

    private static boolean representsAnyActiveRespondent(CaseData caseData, RepresentativeIdentity identity) {
        if (caseData.getRespondentCollection() == null) {
            return false;
        }

        return caseData.getRepCollection().stream()
                .map(RepresentedTypeRItem::getValue)
                .filter(Objects::nonNull)
                .filter(representative -> Objects.equals(identity, representativeIdentity(representative)))
                .anyMatch(representative -> caseData.getRespondentCollection().stream()
                        .filter(CaseFlagsService::isActiveRespondent)
                        .anyMatch(respondent -> representsRespondent(representative, respondent)));
    }

    private static boolean representsRespondent(RepresentedTypeR representative, RespondentSumTypeItem respondent) {
        if (representative == null || respondent == null || respondent.getValue() == null) {
            return false;
        }

        if (StringUtils.isNotBlank(representative.getRespondentId())) {
            return Objects.equals(representative.getRespondentId(), respondent.getId());
        }

        return StringUtils.isNotBlank(representative.getRespRepName())
                && Objects.equals(representative.getRespRepName(), respondentName(respondent));
    }

    private static boolean isActiveRespondent(RespondentSumTypeItem respondent) {
        if (respondent == null || respondent.getValue() == null) {
            return false;
        }

        RespondentSumType respondentValue = respondent.getValue();
        return !YES.equals(respondentValue.getResponseStruckOut())
                && !NO.equals(respondentValue.getResponseContinue());
    }

    private static String respondentName(RespondentSumTypeItem respondent) {
        return respondent.getValue().getRespondentName();
    }

    private static boolean isDuplicateRepresentativeAtFlagSlot(CaseData caseData, int index) {
        RepresentativeIdentity identity = representativeIdentity(representativeForFlagSlot(caseData, index));
        if (identity == null) {
            return false;
        }

        for (int i = 0; i < index; i++) {
            if (Objects.equals(identity, representativeIdentity(representativeForFlagSlot(caseData, i)))) {
                return true;
            }
        }

        return false;
    }

    @Nullable
    private static RepresentativeIdentity representativeIdentity(RepresentedTypeR representative) {
        if (representative == null
                || StringUtils.isAllBlank(
                representative.getNameOfRepresentative(), representative.getRepresentativeEmailAddress())) {
            return null;
        }

        return new RepresentativeIdentity(
                normaliseRepresentativeIdentity(representative.getNameOfRepresentative()),
                normaliseRepresentativeIdentity(representative.getRepresentativeEmailAddress())
        );
    }

    private static String normaliseRepresentativeIdentity(String value) {
        return StringUtils.trimToEmpty(value).toLowerCase(Locale.ROOT);
    }

    private static List<ExistingRepresentativeFlag> currentRespondentRepresentativeFlags(CaseData caseData) {
        return PARTY_FLAGS.stream()
                .filter(flag -> PartyType.RESPONDENT_REPRESENTATIVE.equals(flag.partyType()))
                .map(flag -> existingRepresentativeFlag(caseData, flag))
                .toList();
    }

    private static ExistingRepresentativeFlag existingRepresentativeFlag(CaseData caseData, PartyFlag flag) {
        CaseFlagsType flags = flag.get(caseData);
        return new ExistingRepresentativeFlag(
                flag.index(),
                flag.visibility(),
                flags == null ? null : flags.getPartyName(),
                flags == null ? null : flags.getDetails()
        );
    }

    private static void alignRespondentRepresentativeFlagDetails(
            CaseData caseData, List<ExistingRepresentativeFlag> existingRepresentativeFlags) {
        PARTY_FLAGS.stream()
                .filter(flag -> PartyType.RESPONDENT_REPRESENTATIVE.equals(flag.partyType()))
                .filter(flag -> hasUniqueRepresentativeForFlagSlot(caseData, flag.index()))
                .forEach(flag -> alignRepresentativeFlagDetails(caseData, flag, existingRepresentativeFlags));
    }

    private static void alignRepresentativeFlagDetails(
            CaseData caseData,
            PartyFlag targetFlag,
            List<ExistingRepresentativeFlag> existingRepresentativeFlags) {
        CaseFlagsType target = targetFlag.get(caseData);
        if (target == null) {
            return;
        }

        target.setDetails(concatFlagDetails(
                target.getDetails(),
                staleRepresentativeFlagDetails(caseData, targetFlag, target, existingRepresentativeFlags)));
    }

    private static ListTypeItem<FlagDetailType> concatFlagDetails(
            ListTypeItem<FlagDetailType> existing, ListTypeItem<FlagDetailType> additional) {
        if (additional == null) {
            return existing;
        }
        return ListTypeItem.concat(existing, additional);
    }

    private static ListTypeItem<FlagDetailType> staleRepresentativeFlagDetails(
            CaseData caseData,
            PartyFlag targetFlag,
            CaseFlagsType target,
            List<ExistingRepresentativeFlag> existingRepresentativeFlags) {
        String targetPartyName = target.getPartyName();
        if (StringUtils.isBlank(targetPartyName)) {
            return null;
        }

        ListTypeItem<FlagDetailType> details = null;
        for (ExistingRepresentativeFlag existingFlag : existingRepresentativeFlags) {
            if (existingFlag.index() == targetFlag.index()
                    || existingRepresentativeFlagStillMatchesCurrentUniqueParty(caseData, existingFlag)
                    || Objects.equals(existingFlag.details(), target.getDetails())
                    || !Objects.equals(existingFlag.visibility(), targetFlag.visibility())
                    || !Objects.equals(existingFlag.partyName(), targetPartyName)) {
                continue;
            }

            details = concatFlagDetails(details, existingFlag.details());
        }
        return details;
    }

    private static boolean existingRepresentativeFlagStillMatchesCurrentUniqueParty(
            CaseData caseData, ExistingRepresentativeFlag existingFlag) {
        String currentPartyName = representativePartyNameForFlagSlot(caseData, existingFlag.index());
        return hasUniqueRepresentativeForFlagSlot(caseData, existingFlag.index())
                && Objects.equals(existingFlag.partyName(), currentPartyName);
    }

    private static String representativePartyNameForFlagSlot(CaseData caseData, int index) {
        RepresentedTypeR representative = representativeForFlagSlot(caseData, index);
        return representative == null ? null : representative.getNameOfRepresentative();
    }

    private static void clearStaleRespondentRepresentativeFlags(CaseData caseData) {
        PARTY_FLAGS.stream()
                .filter(flag -> PartyType.RESPONDENT_REPRESENTATIVE.equals(flag.partyType()))
                .filter(flag -> !hasUniqueRepresentativeForFlagSlot(caseData, flag.index()))
                .forEach(flag -> flag.clear(caseData));
    }

    private static boolean hasStaleRespondentRepresentativeFlags(CaseData caseData) {
        return PARTY_FLAGS.stream()
                .filter(flag -> PartyType.RESPONDENT_REPRESENTATIVE.equals(flag.partyType()))
                .filter(flag -> !hasUniqueRepresentativeForFlagSlot(caseData, flag.index()))
                .anyMatch(flag -> flag.get(caseData) != null);
    }

    private boolean isFlaggedForRestrictedReporting(CaseData caseData) {
        RestrictedReportingType restricted = caseData.getRestrictedReporting();
        return restricted != null && (YES.equals(restricted.getRule503b()) || YES.equals(restricted.getImposed()));
    }

    private boolean hasGrantedRestrictedPublicityDecision(CaseData caseData) {
        if (caseData.getGenericTseApplicationCollection() == null) {
            return false;
        }

        return caseData.getGenericTseApplicationCollection().stream()
                .map(GenericTseApplicationTypeItem::getValue)
                .filter(o -> TSE_APP_RESTRICT_PUBLICITY.equals(o.getType()))
                .map(GenericTseApplicationType::getAdminDecision)
                .filter(Objects::nonNull)
                .anyMatch(this::hasGrantedDecision);
    }

    private boolean hasGrantedDecision(List<TseAdminRecordDecisionTypeItem> list) {
        return list.stream()
                .map(TseAdminRecordDecisionTypeItem::getValue)
                .filter(o -> o.getDecision() != null)
                .anyMatch(o -> o.getDecision().startsWith(GRANTED));
    }

    private ListTypeItem<FlagDetailType> getPartyCaseFlags(CaseData caseData) {
        ListTypeItem<FlagDetailType> partyLevel = new ListTypeItem<>();

        for (PartyFlag flag : PARTY_FLAGS) {
            partyLevel = appendDetailsIfPresent(partyLevel, flag.get(caseData));
        }

        return partyLevel;
    }

    private ListTypeItem<FlagDetailType> appendDetailsIfPresent(
            ListTypeItem<FlagDetailType> partyLevel, CaseFlagsType flags) {
        if (flags != null && flags.getDetails() != null) {
            return ListTypeItem.concat(partyLevel, flags.getDetails());
        }
        return partyLevel;
    }

    @Nullable
    private FlagDetailType findFlagByName(ListTypeItem<FlagDetailType> flags, String name) {
        return flags.stream()
                .map(GenericTypeItem::getValue)
                .filter(o -> name.equals(o.getName()))
                .findFirst()
                .orElse(null);
    }

    private boolean areAnyFlagsActive(ListTypeItem<FlagDetailType> flags, String...names) {
        return Arrays.stream(names)
                .map(o -> findFlagByName(flags, o))
                .filter(Objects::nonNull)
                .anyMatch(o -> ACTIVE.equals(o.getStatus()));
    }

    private enum PartyType {
        CLAIMANT,
        RESPONDENT,
        CLAIMANT_REPRESENTATIVE,
        RESPONDENT_REPRESENTATIVE
    }

    private record PartyFlag(
            Function<AllPartyFlags, CaseFlagsType> getter,
            BiConsumer<AllPartyFlags, CaseFlagsType> setter,
            PartyType partyType,
            String roleOnCase,
            String visibility,
            int index) {

        private CaseFlagsType get(CaseData caseData) {
            AllPartyFlags allPartyFlags = caseData.getAllPartyFlags();
            return allPartyFlags == null ? null : getter.apply(allPartyFlags);
        }

        private void set(CaseData caseData, CaseFlagsType flags) {
            setter.accept(getOrCreateAllPartyFlags(caseData), flags);
        }

        private void clear(CaseData caseData) {
            AllPartyFlags allPartyFlags = caseData.getAllPartyFlags();
            if (allPartyFlags != null) {
                setter.accept(allPartyFlags, null);
            }
        }
    }

    /**
     * This lets the service move flag details when representative slots reorder, and then clear stale/duplicate
     * representative flag slots.
     */
    private record ExistingRepresentativeFlag(
            int index,
            String visibility,
            String partyName,
            ListTypeItem<FlagDetailType> details) {
    }

    private record RepresentativeIdentity(String name, String email) {
    }
}

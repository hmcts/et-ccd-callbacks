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
import uk.gov.hmcts.et.common.model.ccd.items.TseAdminRecordDecisionTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.AllPartyFlags;
import uk.gov.hmcts.et.common.model.ccd.types.CaseFlagsType;
import uk.gov.hmcts.et.common.model.ccd.types.RestrictedReportingType;

import java.util.Arrays;
import java.util.List;
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
                        .anyMatch(flag -> isMissingRole(caseData, flag));
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

        PARTY_FLAGS.stream()
                .filter(flag -> hasParty(caseData, flag))
                .forEach(flag -> setupPartyFlag(caseData, flag));
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
            case RESPONDENT_REPRESENTATIVE -> representativeCount(caseData) > flag.index();
        };
    }

    private static boolean hasParty(CaseData caseData, PartyFlag flag) {
        return switch (flag.partyType()) {
            case CLAIMANT -> true;
            case RESPONDENT -> respondentCount(caseData) > flag.index();
            case CLAIMANT_REPRESENTATIVE -> caseData.getRepresentativeClaimantType() != null;
            case RESPONDENT_REPRESENTATIVE -> representativeCount(caseData) > flag.index();
        };
    }

    private static boolean isMissingRole(CaseData caseData, PartyFlag flag) {
        CaseFlagsType flags = flag.get(caseData);
        return flags == null || StringUtils.isEmpty(flags.getRoleOnCase());
    }

    private static void setupPartyFlag(CaseData caseData, PartyFlag flag) {
        String partyName = getPartyName(caseData, flag);
        CaseFlagsType existingFlag = flag.get(caseData);

        if (existingFlag == null || StringUtils.isEmpty(existingFlag.getRoleOnCase())) {
            flag.set(caseData, createShellFlag(partyName, flag.roleOnCase(), flag.visibility()));
        } else if (!Objects.equals(partyName, existingFlag.getPartyName())) {
            existingFlag.setPartyName(partyName);
        }
    }

    private static CaseFlagsType createShellFlag(String partyName, String roleOnCase, String visibility) {
        return CaseFlagsType.builder()
                .partyName(partyName)
                .roleOnCase(roleOnCase)
                .groupId(roleOnCase)
                .visibility(visibility)
                .build();
    }

    private static String getPartyName(CaseData caseData, PartyFlag flag) {
        return switch (flag.partyType()) {
            case CLAIMANT -> caseData.getClaimant();
            case RESPONDENT -> caseData.getRespondentCollection()
                    .get(flag.index())
                    .getValue()
                    .getRespondentName();
            case CLAIMANT_REPRESENTATIVE -> caseData.getRepresentativeClaimantType().getNameOfRepresentative();
            case RESPONDENT_REPRESENTATIVE -> caseData.getRepCollection()
                    .get(flag.index())
                    .getValue()
                    .getNameOfRepresentative();
        };
    }

    private static int respondentCount(CaseData caseData) {
        return caseData.getRespondentCollection() == null ? 0 : caseData.getRespondentCollection().size();
    }

    private static int representativeCount(CaseData caseData) {
        return caseData.getRepCollection() == null ? 0 : caseData.getRepCollection().size();
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
}

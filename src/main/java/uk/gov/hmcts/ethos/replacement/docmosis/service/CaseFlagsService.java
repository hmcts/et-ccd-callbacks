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
            claimantFlag(CaseData::getClaimantFlags, CaseData::setClaimantFlags, INTERNAL),
            claimantFlag(CaseData::getClaimantExternalFlags, CaseData::setClaimantExternalFlags, EXTERNAL),

            respondentFlag(CaseData::getRespondentFlags, CaseData::setRespondentFlags, RESPONDENT1, INTERNAL, 0),
            respondentFlag(CaseData::getRespondentExternalFlags, CaseData::setRespondentExternalFlags,
                    RESPONDENT1, EXTERNAL, 0),
            respondentFlag(CaseData::getRespondent1Flags, CaseData::setRespondent1Flags, RESPONDENT2, INTERNAL, 1),
            respondentFlag(CaseData::getRespondent1ExternalFlags, CaseData::setRespondent1ExternalFlags,
                    RESPONDENT2, EXTERNAL, 1),
            respondentFlag(CaseData::getRespondent2Flags, CaseData::setRespondent2Flags, RESPONDENT3, INTERNAL, 2),
            respondentFlag(CaseData::getRespondent2ExternalFlags, CaseData::setRespondent2ExternalFlags,
                    RESPONDENT3, EXTERNAL, 2),
            respondentFlag(CaseData::getRespondent3Flags, CaseData::setRespondent3Flags, RESPONDENT4, INTERNAL, 3),
            respondentFlag(CaseData::getRespondent3ExternalFlags, CaseData::setRespondent3ExternalFlags,
                    RESPONDENT4, EXTERNAL, 3),
            respondentFlag(CaseData::getRespondent4Flags, CaseData::setRespondent4Flags, RESPONDENT5, INTERNAL, 4),
            respondentFlag(CaseData::getRespondent4ExternalFlags, CaseData::setRespondent4ExternalFlags,
                    RESPONDENT5, EXTERNAL, 4),
            respondentFlag(CaseData::getRespondent5Flags, CaseData::setRespondent5Flags, RESPONDENT6, INTERNAL, 5),
            respondentFlag(CaseData::getRespondent5ExternalFlags, CaseData::setRespondent5ExternalFlags,
                    RESPONDENT6, EXTERNAL, 5),
            respondentFlag(CaseData::getRespondent6Flags, CaseData::setRespondent6Flags, RESPONDENT7, INTERNAL, 6),
            respondentFlag(CaseData::getRespondent6ExternalFlags, CaseData::setRespondent6ExternalFlags,
                    RESPONDENT7, EXTERNAL, 6),
            respondentFlag(CaseData::getRespondent7Flags, CaseData::setRespondent7Flags, RESPONDENT8, INTERNAL, 7),
            respondentFlag(CaseData::getRespondent7ExternalFlags, CaseData::setRespondent7ExternalFlags,
                    RESPONDENT8, EXTERNAL, 7),
            respondentFlag(CaseData::getRespondent8Flags, CaseData::setRespondent8Flags, RESPONDENT9, INTERNAL, 8),
            respondentFlag(CaseData::getRespondent8ExternalFlags, CaseData::setRespondent8ExternalFlags,
                    RESPONDENT9, EXTERNAL, 8),
            respondentFlag(CaseData::getRespondent9Flags, CaseData::setRespondent9Flags, RESPONDENT10,
                    INTERNAL, 9),
            respondentFlag(CaseData::getRespondent9ExternalFlags, CaseData::setRespondent9ExternalFlags,
                    RESPONDENT10, EXTERNAL, 9),

            claimantRepresentativeFlag(CaseData::getClaimantRepresentativeFlags,
                    CaseData::setClaimantRepresentativeFlags, INTERNAL),
            claimantRepresentativeFlag(CaseData::getClaimantRepresentativeExternalFlags,
                    CaseData::setClaimantRepresentativeExternalFlags, EXTERNAL),

            representativeFlag(CaseData::getRepresentativeFlags, CaseData::setRepresentativeFlags,
                    REPRESENTATIVE1, INTERNAL, 0),
            representativeFlag(CaseData::getRepresentativeExternalFlags, CaseData::setRepresentativeExternalFlags,
                    REPRESENTATIVE1, EXTERNAL, 0),
            representativeFlag(CaseData::getRepresentative1Flags, CaseData::setRepresentative1Flags,
                    REPRESENTATIVE2, INTERNAL, 1),
            representativeFlag(CaseData::getRepresentative1ExternalFlags, CaseData::setRepresentative1ExternalFlags,
                    REPRESENTATIVE2, EXTERNAL, 1),
            representativeFlag(CaseData::getRepresentative2Flags, CaseData::setRepresentative2Flags,
                    REPRESENTATIVE3, INTERNAL, 2),
            representativeFlag(CaseData::getRepresentative2ExternalFlags, CaseData::setRepresentative2ExternalFlags,
                    REPRESENTATIVE3, EXTERNAL, 2),
            representativeFlag(CaseData::getRepresentative3Flags, CaseData::setRepresentative3Flags,
                    REPRESENTATIVE4, INTERNAL, 3),
            representativeFlag(CaseData::getRepresentative3ExternalFlags, CaseData::setRepresentative3ExternalFlags,
                    REPRESENTATIVE4, EXTERNAL, 3),
            representativeFlag(CaseData::getRepresentative4Flags, CaseData::setRepresentative4Flags,
                    REPRESENTATIVE5, INTERNAL, 4),
            representativeFlag(CaseData::getRepresentative4ExternalFlags, CaseData::setRepresentative4ExternalFlags,
                    REPRESENTATIVE5, EXTERNAL, 4),
            representativeFlag(CaseData::getRepresentative5Flags, CaseData::setRepresentative5Flags,
                    REPRESENTATIVE6, INTERNAL, 5),
            representativeFlag(CaseData::getRepresentative5ExternalFlags, CaseData::setRepresentative5ExternalFlags,
                    REPRESENTATIVE6, EXTERNAL, 5),
            representativeFlag(CaseData::getRepresentative6Flags, CaseData::setRepresentative6Flags,
                    REPRESENTATIVE7, INTERNAL, 6),
            representativeFlag(CaseData::getRepresentative6ExternalFlags, CaseData::setRepresentative6ExternalFlags,
                    REPRESENTATIVE7, EXTERNAL, 6),
            representativeFlag(CaseData::getRepresentative7Flags, CaseData::setRepresentative7Flags,
                    REPRESENTATIVE8, INTERNAL, 7),
            representativeFlag(CaseData::getRepresentative7ExternalFlags, CaseData::setRepresentative7ExternalFlags,
                    REPRESENTATIVE8, EXTERNAL, 7),
            representativeFlag(CaseData::getRepresentative8Flags, CaseData::setRepresentative8Flags,
                    REPRESENTATIVE9, INTERNAL, 8),
            representativeFlag(CaseData::getRepresentative8ExternalFlags, CaseData::setRepresentative8ExternalFlags,
                    REPRESENTATIVE9, EXTERNAL, 8),
            representativeFlag(CaseData::getRepresentative9Flags, CaseData::setRepresentative9Flags,
                    REPRESENTATIVE10, INTERNAL, 9),
            representativeFlag(CaseData::getRepresentative9ExternalFlags, CaseData::setRepresentative9ExternalFlags,
                    REPRESENTATIVE10, EXTERNAL, 9)
    );

    public boolean caseFlagsSetupRequired(CaseData caseData) {
        return caseData.getCaseFlags() == null
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
        if (caseData.getCaseFlags() == null) {
            caseData.setCaseFlags(CaseFlagsType.builder().build());
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
        caseData.setCaseFlags(null);
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
            Function<CaseData, CaseFlagsType> getter,
            BiConsumer<CaseData, CaseFlagsType> setter,
            String visibility) {
        return new PartyFlag(getter, setter, PartyType.CLAIMANT, CLAIMANT, visibility, NOT_INDEXED);
    }

    private static PartyFlag respondentFlag(
            Function<CaseData, CaseFlagsType> getter,
            BiConsumer<CaseData, CaseFlagsType> setter,
            String roleOnCase,
            String visibility,
            int index) {
        return new PartyFlag(getter, setter, PartyType.RESPONDENT, roleOnCase, visibility, index);
    }

    private static PartyFlag claimantRepresentativeFlag(
            Function<CaseData, CaseFlagsType> getter,
            BiConsumer<CaseData, CaseFlagsType> setter,
            String visibility) {
        return new PartyFlag(getter, setter, PartyType.CLAIMANT_REPRESENTATIVE, CLAIMANT_REPRESENTATIVE,
                visibility, NOT_INDEXED);
    }

    private static PartyFlag representativeFlag(
            Function<CaseData, CaseFlagsType> getter,
            BiConsumer<CaseData, CaseFlagsType> setter,
            String roleOnCase,
            String visibility,
            int index) {
        return new PartyFlag(getter, setter, PartyType.RESPONDENT_REPRESENTATIVE, roleOnCase, visibility, index);
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
            Function<CaseData, CaseFlagsType> getter,
            BiConsumer<CaseData, CaseFlagsType> setter,
            PartyType partyType,
            String roleOnCase,
            String visibility,
            int index) {

        private CaseFlagsType get(CaseData caseData) {
            return getter.apply(caseData);
        }

        private void set(CaseData caseData, CaseFlagsType flags) {
            setter.accept(caseData, flags);
        }

        private void clear(CaseData caseData) {
            setter.accept(caseData, null);
        }
    }
}

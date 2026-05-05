package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.FlagDetailType;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationType;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.ListTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.TseAdminRecordDecisionTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.CaseFlagsType;
import uk.gov.hmcts.et.common.model.ccd.types.RestrictedReportingType;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static uk.gov.hmcts.ecm.common.model.helper.CaseFlagConstants.ACTIVE;
import static uk.gov.hmcts.ecm.common.model.helper.CaseFlagConstants.DISRUPTIVE_CUSTOMER;
import static uk.gov.hmcts.ecm.common.model.helper.CaseFlagConstants.LANGUAGE_INTERPRETER;
import static uk.gov.hmcts.ecm.common.model.helper.CaseFlagConstants.SIGN_LANGUAGE_INTERPRETER;
import static uk.gov.hmcts.ecm.common.model.helper.CaseFlagConstants.VEXATIOUS_LITIGANT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.TSE_APP_RESTRICT_PUBLICITY;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@Service
public class CaseFlagsService {
    private static final String GRANTED = "Granted";

    public static final String INTERNAL = "Internal";
    public static final String EXTERNAL = "External";
    public static final String CLAIMANT = "claimant";
    public static final String RESPONDENT = "respondent";
    public static final String RESPONDENT1 = "respondent1";
    public static final String RESPONDENT2 = "respondent2";
    public static final String RESPONDENT3 = "respondent3";
    public static final String RESPONDENT4 = "respondent4";
    public static final String RESPONDENT5 = "respondent5";
    public static final String RESPONDENT6 = "respondent6";
    public static final String RESPONDENT7 = "respondent7";
    public static final String RESPONDENT8 = "respondent8";
    public static final String RESPONDENT9 = "respondent9";

    private static final List<RespondentCaseFlagFields> RESPONDENT_CASE_FLAG_FIELDS = List.of(
            new RespondentCaseFlagFields(
                    CaseData::getRespondentFlags,
                    CaseData::setRespondentFlags,
                    CaseData::getRespondentExternalFlags,
                    CaseData::setRespondentExternalFlags,
                    RESPONDENT),
            new RespondentCaseFlagFields(
                    CaseData::getRespondent1Flags,
                    CaseData::setRespondent1Flags,
                    CaseData::getRespondent1ExternalFlags,
                    CaseData::setRespondent1ExternalFlags,
                    RESPONDENT1),
            new RespondentCaseFlagFields(
                    CaseData::getRespondent2Flags,
                    CaseData::setRespondent2Flags,
                    CaseData::getRespondent2ExternalFlags,
                    CaseData::setRespondent2ExternalFlags,
                    RESPONDENT2),
            new RespondentCaseFlagFields(
                    CaseData::getRespondent3Flags,
                    CaseData::setRespondent3Flags,
                    CaseData::getRespondent3ExternalFlags,
                    CaseData::setRespondent3ExternalFlags,
                    RESPONDENT3),
            new RespondentCaseFlagFields(
                    CaseData::getRespondent4Flags,
                    CaseData::setRespondent4Flags,
                    CaseData::getRespondent4ExternalFlags,
                    CaseData::setRespondent4ExternalFlags,
                    RESPONDENT4),
            new RespondentCaseFlagFields(
                    CaseData::getRespondent5Flags,
                    CaseData::setRespondent5Flags,
                    CaseData::getRespondent5ExternalFlags,
                    CaseData::setRespondent5ExternalFlags,
                    RESPONDENT5),
            new RespondentCaseFlagFields(
                    CaseData::getRespondent6Flags,
                    CaseData::setRespondent6Flags,
                    CaseData::getRespondent6ExternalFlags,
                    CaseData::setRespondent6ExternalFlags,
                    RESPONDENT6),
            new RespondentCaseFlagFields(
                    CaseData::getRespondent7Flags,
                    CaseData::setRespondent7Flags,
                    CaseData::getRespondent7ExternalFlags,
                    CaseData::setRespondent7ExternalFlags,
                    RESPONDENT7),
            new RespondentCaseFlagFields(
                    CaseData::getRespondent8Flags,
                    CaseData::setRespondent8Flags,
                    CaseData::getRespondent8ExternalFlags,
                    CaseData::setRespondent8ExternalFlags,
                    RESPONDENT8),
            new RespondentCaseFlagFields(
                    CaseData::getRespondent9Flags,
                    CaseData::setRespondent9Flags,
                    CaseData::getRespondent9ExternalFlags,
                    CaseData::setRespondent9ExternalFlags,
                    RESPONDENT9)
    );

    public boolean caseFlagsSetupRequired(CaseData caseData) {
        return caseData.getCaseFlags() == null
                || claimantCaseFlagsSetupRequired(caseData)
                || respondentCaseFlagsSetupRequired(caseData);
    }

    /**
     * Setup case flags for Claimant, Respondent and Case level.
     * @param caseData Data about the current case
     */
    public void setupCaseFlags(CaseData caseData) {
        if (caseData.getCaseFlags() == null) {
            caseData.setCaseFlags(CaseFlagsType.builder().build());
        }

        setupClaimantCaseFlags(caseData);
        setupRespondentCaseFlags(caseData);
    }

    private boolean claimantCaseFlagsSetupRequired(CaseData caseData) {
        boolean internalSetupRequired = caseFlagSetupRequired(
                caseData.getClaimantFlags(), caseData.getClaimant(), CLAIMANT, CLAIMANT, INTERNAL);
        boolean externalSetupRequired = caseFlagSetupRequired(
                caseData.getClaimantExternalFlags(), caseData.getClaimant(), CLAIMANT, CLAIMANT, EXTERNAL);

        return internalSetupRequired || externalSetupRequired;
    }

    private boolean respondentCaseFlagsSetupRequired(CaseData caseData) {
        List<RespondentSumTypeItem> respondents = respondentCollection(caseData);
        int respondentsWithCaseFlags = Math.min(respondents.size(), RESPONDENT_CASE_FLAG_FIELDS.size());
        for (int index = 0; index < respondentsWithCaseFlags; index++) {
            RespondentCaseFlagFields fields = RESPONDENT_CASE_FLAG_FIELDS.get(index);
            String respondentName = respondentName(respondents.get(index));
            if (respondentCaseFlagSetupRequired(caseData, fields, respondentName)) {
                return true;
            }
        }

        return false;
    }

    private boolean respondentCaseFlagSetupRequired(
            CaseData caseData, RespondentCaseFlagFields fields, String respondentName) {
        boolean internalSetupRequired = caseFlagSetupRequired(
                fields.internalGetter().apply(caseData), respondentName, RESPONDENT, fields.groupId(), INTERNAL);
        boolean externalSetupRequired = caseFlagSetupRequired(
                fields.externalGetter().apply(caseData), respondentName, RESPONDENT, fields.groupId(), EXTERNAL);

        return internalSetupRequired || externalSetupRequired;
    }

    private void setupClaimantCaseFlags(CaseData caseData) {
        upsertCaseFlags(caseData, CaseData::getClaimantFlags, CaseData::setClaimantFlags,
                caseData.getClaimant(), CLAIMANT, CLAIMANT, INTERNAL);
        upsertCaseFlags(caseData, CaseData::getClaimantExternalFlags, CaseData::setClaimantExternalFlags,
                caseData.getClaimant(), CLAIMANT, CLAIMANT, EXTERNAL);
    }

    private void setupRespondentCaseFlags(CaseData caseData) {
        List<RespondentSumTypeItem> respondents = respondentCollection(caseData);
        int respondentsWithCaseFlags = Math.min(respondents.size(), RESPONDENT_CASE_FLAG_FIELDS.size());
        for (int index = 0; index < respondentsWithCaseFlags; index++) {
            RespondentCaseFlagFields fields = RESPONDENT_CASE_FLAG_FIELDS.get(index);
            String respondentName = respondentName(respondents.get(index));
            upsertCaseFlags(caseData, fields.internalGetter(), fields.internalSetter(), respondentName, RESPONDENT,
                    fields.groupId(), INTERNAL);
            upsertCaseFlags(caseData, fields.externalGetter(), fields.externalSetter(), respondentName, RESPONDENT,
                    fields.groupId(), EXTERNAL);
        }
    }

    /**
     * Sets case flags for Claimant, Respondent and Case level to null.
     * @param caseData Data about the current case
     */
    public void rollbackCaseFlags(CaseData caseData) {
        caseData.setCaseFlags(null);
        caseData.setClaimantFlags(null);
        caseData.setClaimantExternalFlags(null);
        RESPONDENT_CASE_FLAG_FIELDS.forEach(fields -> {
            fields.internalSetter().accept(caseData, null);
            fields.externalSetter().accept(caseData, null);
        });
    }

    /**
     * Sets additional flags on CaseData dependent on CaseFlags raised.
     * @param caseData Data about the current case.
     */
    public void processNewlySetCaseFlags(CaseData caseData) {
        ListTypeItem<FlagDetailType> partyLevel = getPartyCaseFlags(caseData);
        boolean interpreterRequired = areAnyFlagsActive(partyLevel, SIGN_LANGUAGE_INTERPRETER, LANGUAGE_INTERPRETER);
        boolean additionalSecurityRequired = areAnyFlagsActive(partyLevel, VEXATIOUS_LITIGANT, DISRUPTIVE_CUSTOMER);

        caseData.setCaseInterpreterRequiredFlag(interpreterRequired ? YES : NO);
        caseData.setCaseAdditionalSecurityFlag(additionalSecurityRequired ? YES : NO);
    }

    /**
     * Sets the privateHearingFlag based on various case states and events.
     * @param caseData Data about the case
     */
    public void setPrivateHearingFlag(CaseData caseData) {
        boolean shouldBePrivate = hasGrantedRestrictedPublicityDecision(caseData)
                || isFlaggedForRestrictedReporting(caseData)
                || isYes(caseData.getIcListingPreliminaryHearing());

        caseData.setPrivateHearingRequiredFlag(shouldBePrivate ? YES : NO);
    }

    private boolean caseFlagSetupRequired(
            CaseFlagsType flags, String partyName, String roleOnCase, String groupId, String visibility) {
        return flags == null
                || !Objects.equals(partyName, flags.getPartyName())
                || !Objects.equals(roleOnCase, flags.getRoleOnCase())
                || !Objects.equals(groupId, flags.getGroupId())
                || !Objects.equals(visibility, flags.getVisibility());
    }

    private void upsertCaseFlags(
            CaseData caseData,
            Function<CaseData, CaseFlagsType> getter,
            BiConsumer<CaseData, CaseFlagsType> setter,
            String partyName,
            String roleOnCase,
            String groupId,
            String visibility) {
        CaseFlagsType flags = getter.apply(caseData);
        if (flags == null) {
            setter.accept(caseData, buildCaseFlags(partyName, roleOnCase, groupId, visibility));
            return;
        }

        applyCaseFlagDefaults(flags, partyName, roleOnCase, groupId, visibility);
    }

    private CaseFlagsType buildCaseFlags(String partyName, String roleOnCase, String groupId, String visibility) {
        return CaseFlagsType.builder()
                .partyName(partyName)
                .roleOnCase(roleOnCase)
                .groupId(groupId)
                .visibility(visibility)
                .build();
    }

    private void applyCaseFlagDefaults(
            CaseFlagsType flags, String partyName, String roleOnCase, String groupId, String visibility) {
        flags.setPartyName(partyName);
        flags.setRoleOnCase(roleOnCase);
        flags.setGroupId(groupId);
        flags.setVisibility(visibility);
    }

    private List<RespondentSumTypeItem> respondentCollection(CaseData caseData) {
        return caseData.getRespondentCollection() == null ? List.of() : caseData.getRespondentCollection();
    }

    private String respondentName(RespondentSumTypeItem respondent) {
        return respondent == null || respondent.getValue() == null ? null : respondent.getValue().getRespondentName();
    }

    private boolean isYes(String value) {
        return YES.equals(value);
    }

    private boolean isFlaggedForRestrictedReporting(CaseData caseData) {
        RestrictedReportingType restricted = caseData.getRestrictedReporting();
        return restricted != null && (isYes(restricted.getRule503b()) || isYes(restricted.getImposed()));
    }

    private boolean hasGrantedRestrictedPublicityDecision(CaseData caseData) {
        if (caseData.getGenericTseApplicationCollection() == null) {
            return false;
        }

        return caseData.getGenericTseApplicationCollection().stream()
                .filter(Objects::nonNull)
                .map(GenericTseApplicationTypeItem::getValue)
                .filter(Objects::nonNull)
                .filter(application -> TSE_APP_RESTRICT_PUBLICITY.equals(application.getType()))
                .map(GenericTseApplicationType::getAdminDecision)
                .filter(Objects::nonNull)
                .anyMatch(this::hasGrantedDecision);
    }

    private boolean hasGrantedDecision(List<TseAdminRecordDecisionTypeItem> decisions) {
        return decisions.stream()
                .filter(Objects::nonNull)
                .map(TseAdminRecordDecisionTypeItem::getValue)
                .filter(Objects::nonNull)
                .filter(decision -> decision.getDecision() != null)
                .anyMatch(decision -> decision.getDecision().startsWith(GRANTED));
    }

    private ListTypeItem<FlagDetailType> getPartyCaseFlags(CaseData caseData) {
        return ListTypeItem.from(partyCaseFlags(caseData)
                .map(CaseFlagsType::getDetails)
                .filter(Objects::nonNull)
                .flatMap(List::stream));
    }

    private Stream<CaseFlagsType> partyCaseFlags(CaseData caseData) {
        Stream<CaseFlagsType> claimantFlags = Stream.of(
                caseData.getClaimantFlags(),
                caseData.getClaimantExternalFlags()
        );

        Stream<CaseFlagsType> respondentFlags = RESPONDENT_CASE_FLAG_FIELDS.stream()
                .flatMap(fields -> Stream.of(
                        fields.internalGetter().apply(caseData),
                        fields.externalGetter().apply(caseData)
                ));

        return Stream.concat(claimantFlags, respondentFlags)
                .filter(Objects::nonNull);
    }

    private boolean areAnyFlagsActive(ListTypeItem<FlagDetailType> flags, String... names) {
        List<String> namesToFind = Arrays.asList(names);
        return flags.stream()
                .filter(Objects::nonNull)
                .map(GenericTypeItem::getValue)
                .filter(Objects::nonNull)
                .anyMatch(flag -> namesToFind.contains(flag.getName()) && ACTIVE.equals(flag.getStatus()));
    }

    private record RespondentCaseFlagFields(
            Function<CaseData, CaseFlagsType> internalGetter,
            BiConsumer<CaseData, CaseFlagsType> internalSetter,
            Function<CaseData, CaseFlagsType> externalGetter,
            BiConsumer<CaseData, CaseFlagsType> externalSetter,
            String groupId) {
    }
}

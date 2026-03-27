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
import javax.annotation.Nullable;

import static uk.gov.hmcts.ecm.common.model.helper.CaseFlagConstants.ACTIVE;
import static uk.gov.hmcts.ecm.common.model.helper.CaseFlagConstants.DISRUPTIVE_CUSTOMER;
import static uk.gov.hmcts.ecm.common.model.helper.CaseFlagConstants.LANGUAGE_INTERPRETER;
import static uk.gov.hmcts.ecm.common.model.helper.CaseFlagConstants.SIGN_LANGUAGE_INTERPRETER;
import static uk.gov.hmcts.ecm.common.model.helper.CaseFlagConstants.VEXATIOUS_LITIGANT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.TSE_APP_RESTRICT_PUBLICITY;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@Slf4j
@Service
public class CaseFlagsService {
    private static final String GRANTED = "Granted";

    public static final String INTERNAL    = "Internal";
    public static final String EXTERNAL    = "External";
    public static final String CLAIMANT    = "claimant";
    public static final String RESPONDENT  = "respondent";
    public static final String RESPONDENT1  = "respondent1";
    public static final String RESPONDENT2  = "respondent2";
    public static final String RESPONDENT3  = "respondent3";
    public static final String RESPONDENT4  = "respondent4";
    public static final String RESPONDENT5  = "respondent5";
    public static final String RESPONDENT6  = "respondent6";
    public static final String RESPONDENT7  = "respondent7";
    public static final String RESPONDENT8  = "respondent8";
    public static final String RESPONDENT9  = "respondent9";

    public boolean caseFlagsSetupRequired(CaseData caseData) {
        return (caseData.getCaseFlags() == null
            || caseData.getClaimantFlags() == null
            || StringUtils.isEmpty(caseData.getClaimantFlags().getRoleOnCase())
            || caseData.getClaimantExternalFlags() == null
            || StringUtils.isEmpty(caseData.getClaimantExternalFlags().getRoleOnCase())
            || (!caseData.getRespondentCollection().isEmpty()
                && (caseData.getRespondentFlags() == null
                || StringUtils.isEmpty(caseData.getRespondentFlags().getRoleOnCase())))
            || (!caseData.getRespondentCollection().isEmpty()
                && (caseData.getRespondentExternalFlags() == null
                || StringUtils.isEmpty(caseData.getRespondentExternalFlags().getRoleOnCase())))
            || (caseData.getRespondentCollection().size() > 1
                && (caseData.getRespondent1Flags() == null
                || StringUtils.isEmpty(caseData.getRespondent1Flags().getRoleOnCase())))
            || (caseData.getRespondentCollection().size() > 1
                && (caseData.getRespondent1ExternalFlags() == null
                || StringUtils.isEmpty(caseData.getRespondent1ExternalFlags().getRoleOnCase())))
            || (caseData.getRespondentCollection().size() > 2
                && (caseData.getRespondent2Flags() == null
                || StringUtils.isEmpty(caseData.getRespondent2Flags().getRoleOnCase())))
            || (caseData.getRespondentCollection().size() > 2
                && (caseData.getRespondent2ExternalFlags() == null
                || StringUtils.isEmpty(caseData.getRespondent2ExternalFlags().getRoleOnCase())))
            || (caseData.getRespondentCollection().size() > 3
                && (caseData.getRespondent3Flags() == null
                || StringUtils.isEmpty(caseData.getRespondent3Flags().getRoleOnCase())))
            || (caseData.getRespondentCollection().size() > 3
                && (caseData.getRespondent3ExternalFlags() == null
                || StringUtils.isEmpty(caseData.getRespondent3ExternalFlags().getRoleOnCase())))
            || (caseData.getRespondentCollection().size() > 4
                && (caseData.getRespondent4Flags() == null
                || StringUtils.isEmpty(caseData.getRespondent4Flags().getRoleOnCase())))
            || (caseData.getRespondentCollection().size() > 4
                && (caseData.getRespondent4ExternalFlags() == null
                || StringUtils.isEmpty(caseData.getRespondent4ExternalFlags().getRoleOnCase())))
            || (caseData.getRespondentCollection().size() > 5
                && (caseData.getRespondent5Flags() == null
                || StringUtils.isEmpty(caseData.getRespondent5Flags().getRoleOnCase())))
            || (caseData.getRespondentCollection().size() > 5
                && (caseData.getRespondent5ExternalFlags() == null
                || StringUtils.isEmpty(caseData.getRespondent5ExternalFlags().getRoleOnCase())))
            || (caseData.getRespondentCollection().size() > 6
                && (caseData.getRespondent6Flags() == null
                || StringUtils.isEmpty(caseData.getRespondent6Flags().getRoleOnCase())))
            || (caseData.getRespondentCollection().size() > 6
                && (caseData.getRespondent6ExternalFlags() == null
                || StringUtils.isEmpty(caseData.getRespondent6ExternalFlags().getRoleOnCase())))
            || (caseData.getRespondentCollection().size() > 7
                && (caseData.getRespondent7Flags() == null
                || StringUtils.isEmpty(caseData.getRespondent7Flags().getRoleOnCase())))
            || (caseData.getRespondentCollection().size() > 7
                && (caseData.getRespondent7ExternalFlags() == null
                || StringUtils.isEmpty(caseData.getRespondent7ExternalFlags().getRoleOnCase())))
            || (caseData.getRespondentCollection().size() > 8
                && (caseData.getRespondent8Flags() == null
                || StringUtils.isEmpty(caseData.getRespondent8Flags().getRoleOnCase())))
            || (caseData.getRespondentCollection().size() > 8
                && (caseData.getRespondent8ExternalFlags() == null
                || StringUtils.isEmpty(caseData.getRespondent8ExternalFlags().getRoleOnCase())))
            || (caseData.getRespondentCollection().size() > 9
                && (caseData.getRespondent9Flags() == null
                || StringUtils.isEmpty(caseData.getRespondent9Flags().getRoleOnCase())))
            || (caseData.getRespondentCollection().size() > 9
                && (caseData.getRespondent9ExternalFlags() == null
                || StringUtils.isEmpty(caseData.getRespondent9ExternalFlags().getRoleOnCase())))
            );
    }

    /**
     * Setup case flags for Claimant, Respondent and Case level.
     *
     * @param caseData Data about the current case
     */
    public void setupCaseFlags(CaseData caseData) {
        if (caseData.getCaseFlags() == null) {
            caseData.setCaseFlags(CaseFlagsType.builder().build());
        }

        if (caseData.getClaimantFlags() == null) {
            caseData.setClaimantFlags(CaseFlagsType.builder()
                    .partyName(caseData.getClaimant())
                    .roleOnCase(CLAIMANT)
                    .groupId(CLAIMANT)
                    .visibility(INTERNAL)
                    .build()
            );
        } else if (!caseData.getClaimant().equals(caseData.getClaimantFlags().getPartyName())) {
            caseData.getClaimantFlags().setPartyName(caseData.getClaimant());
        }

        if (caseData.getClaimantExternalFlags() == null) {
            caseData.setClaimantExternalFlags(CaseFlagsType.builder()
                    .partyName(caseData.getClaimant())
                    .roleOnCase(CLAIMANT)
                    .groupId(CLAIMANT)
                    .visibility(EXTERNAL)
                    .build()
            );
        } else if (!caseData.getClaimant().equals(caseData.getClaimantExternalFlags().getPartyName())) {
            caseData.getClaimantExternalFlags().setPartyName(caseData.getClaimant());
        }

        if (!caseData.getRespondentCollection().isEmpty()) {
            String respondent = caseData.getRespondentCollection().getFirst().getValue().getRespondentName();
            if (caseData.getRespondentFlags() == null) {
                caseData.setRespondentFlags(CaseFlagsType.builder()
                        .partyName(respondent)
                        .roleOnCase(RESPONDENT)
                        .groupId(RESPONDENT)
                        .visibility(INTERNAL)
                        .build()
                );
            } else if (!respondent.equals(caseData.getRespondentFlags().getPartyName())) {
                caseData.getRespondentFlags().setPartyName(respondent);
            }

            if (caseData.getRespondentExternalFlags() == null) {
                caseData.setRespondentExternalFlags(CaseFlagsType.builder()
                        .partyName(respondent)
                        .roleOnCase(RESPONDENT)
                        .groupId(RESPONDENT)
                        .visibility(EXTERNAL)
                        .build()
                );
            } else if (!respondent.equals(caseData.getRespondentExternalFlags().getPartyName())) {
                caseData.getRespondentExternalFlags().setPartyName(respondent);
            }
        }

        if (caseData.getRespondentCollection().size() > 1) {
            String respondent = caseData.getRespondentCollection().get(1).getValue().getRespondentName();
            if (caseData.getRespondent1Flags() == null) {
                caseData.setRespondent1Flags(CaseFlagsType.builder()
                        .partyName(respondent)
                        .roleOnCase(RESPONDENT)
                        .groupId(RESPONDENT1)
                        .visibility(INTERNAL)
                        .build()
                );
            } else if (!respondent.equals(caseData.getRespondent1Flags().getPartyName())) {
                caseData.getRespondent1Flags().setPartyName(respondent);
            }

            if (caseData.getRespondent1ExternalFlags() == null) {
                caseData.setRespondent1ExternalFlags(CaseFlagsType.builder()
                        .partyName(respondent)
                        .roleOnCase(RESPONDENT)
                        .groupId(RESPONDENT1)
                        .visibility(EXTERNAL)
                        .build()
                );
            } else if (!respondent.equals(caseData.getRespondent1ExternalFlags().getPartyName())) {
                caseData.getRespondent1ExternalFlags().setPartyName(respondent);
            }
        }

        if (caseData.getRespondentCollection().size() > 2) {
            String respondent = caseData.getRespondentCollection().get(2).getValue().getRespondentName();
            if (caseData.getRespondent2Flags() == null) {
                caseData.setRespondent2Flags(CaseFlagsType.builder()
                        .partyName(respondent)
                        .roleOnCase(RESPONDENT)
                        .groupId(RESPONDENT2)
                        .visibility(INTERNAL)
                        .build()
                );
            } else if (!respondent.equals(caseData.getRespondent2Flags().getPartyName())) {
                caseData.getRespondent2Flags().setPartyName(respondent);
            }

            if (caseData.getRespondent2ExternalFlags() == null) {
                caseData.setRespondent2ExternalFlags(CaseFlagsType.builder()
                        .partyName(respondent)
                        .roleOnCase(RESPONDENT)
                        .groupId(RESPONDENT2)
                        .visibility(EXTERNAL)
                        .build()
                );
            } else if (!respondent.equals(caseData.getRespondent2ExternalFlags().getPartyName())) {
                caseData.getRespondent2ExternalFlags().setPartyName(respondent);
            }
        }

        if (caseData.getRespondentCollection().size() > 3) {
            String respondent = caseData.getRespondentCollection().get(3).getValue().getRespondentName();
            if (caseData.getRespondent3Flags() == null) {
                caseData.setRespondent3Flags(CaseFlagsType.builder()
                        .partyName(respondent)
                        .roleOnCase(RESPONDENT)
                        .groupId(RESPONDENT3)
                        .visibility(INTERNAL)
                        .build()
                );
            } else if (!respondent.equals(caseData.getRespondent3Flags().getPartyName())) {
                caseData.getRespondent3Flags().setPartyName(respondent);
            }

            if (caseData.getRespondent3ExternalFlags() == null) {
                caseData.setRespondent3ExternalFlags(CaseFlagsType.builder()
                        .partyName(respondent)
                        .roleOnCase(RESPONDENT)
                        .groupId(RESPONDENT3)
                        .visibility(EXTERNAL)
                        .build()
                );
            } else if (!respondent.equals(caseData.getRespondent3ExternalFlags().getPartyName())) {
                caseData.getRespondent3ExternalFlags().setPartyName(respondent);
            }
        }

        if (caseData.getRespondentCollection().size() > 4) {
            String respondent = caseData.getRespondentCollection().get(4).getValue().getRespondentName();
            if (caseData.getRespondent4Flags() == null) {
                caseData.setRespondent4Flags(CaseFlagsType.builder()
                        .partyName(respondent)
                        .roleOnCase(RESPONDENT)
                        .groupId(RESPONDENT4)
                        .visibility(INTERNAL)
                        .build()
                );
            } else if (!respondent.equals(caseData.getRespondent4Flags().getPartyName())) {
                caseData.getRespondent4Flags().setPartyName(respondent);
            }

            if (caseData.getRespondent4ExternalFlags() == null) {
                caseData.setRespondent4ExternalFlags(CaseFlagsType.builder()
                        .partyName(respondent)
                        .roleOnCase(RESPONDENT)
                        .groupId(RESPONDENT4)
                        .visibility(EXTERNAL)
                        .build()
                );
            } else if (!respondent.equals(caseData.getRespondent4ExternalFlags().getPartyName())) {
                caseData.getRespondent4ExternalFlags().setPartyName(respondent);
            }
        }

        if (caseData.getRespondentCollection().size() > 5) {
            String respondent = caseData.getRespondentCollection().get(5).getValue().getRespondentName();
            if (caseData.getRespondent5Flags() == null) {
                caseData.setRespondent5Flags(CaseFlagsType.builder()
                        .partyName(respondent)
                        .roleOnCase(RESPONDENT)
                        .groupId(RESPONDENT5)
                        .visibility(INTERNAL)
                        .build()
                );
            } else if (!respondent.equals(caseData.getRespondent5Flags().getPartyName())) {
                caseData.getRespondent5Flags().setPartyName(respondent);
            }

            if (caseData.getRespondent5ExternalFlags() == null) {
                caseData.setRespondent5ExternalFlags(CaseFlagsType.builder()
                        .partyName(respondent)
                        .roleOnCase(RESPONDENT)
                        .groupId(RESPONDENT5)
                        .visibility(EXTERNAL)
                        .build()
                );
            } else if (!respondent.equals(caseData.getRespondent5ExternalFlags().getPartyName())) {
                caseData.getRespondent5ExternalFlags().setPartyName(respondent);
            }
        }

        if (caseData.getRespondentCollection().size() > 6) {
            String respondent = caseData.getRespondentCollection().get(6).getValue().getRespondentName();
            if (caseData.getRespondent6Flags() == null) {
                caseData.setRespondent6Flags(CaseFlagsType.builder()
                        .partyName(respondent)
                        .roleOnCase(RESPONDENT)
                        .groupId(RESPONDENT6)
                        .visibility(INTERNAL)
                        .build()
                );
            } else if (!respondent.equals(caseData.getRespondent6Flags().getPartyName())) {
                caseData.getRespondent6Flags().setPartyName(respondent);
            }

            if (caseData.getRespondent6ExternalFlags() == null) {
                caseData.setRespondent6ExternalFlags(CaseFlagsType.builder()
                        .partyName(respondent)
                        .roleOnCase(RESPONDENT)
                        .groupId(RESPONDENT6)
                        .visibility(EXTERNAL)
                        .build()
                );
            } else if (!respondent.equals(caseData.getRespondent6ExternalFlags().getPartyName())) {
                caseData.getRespondent6ExternalFlags().setPartyName(respondent);
            }
        }

        if (caseData.getRespondentCollection().size() > 7) {
            String respondent = caseData.getRespondentCollection().get(7).getValue().getRespondentName();
            if (caseData.getRespondent7Flags() == null) {
                caseData.setRespondent7Flags(CaseFlagsType.builder()
                        .partyName(respondent)
                        .roleOnCase(RESPONDENT)
                        .groupId(RESPONDENT7)
                        .visibility(INTERNAL)
                        .build()
                );
            } else if (!respondent.equals(caseData.getRespondent7Flags().getPartyName())) {
                caseData.getRespondent7Flags().setPartyName(respondent);
            }

            if (caseData.getRespondent7ExternalFlags() == null) {
                caseData.setRespondent7ExternalFlags(CaseFlagsType.builder()
                        .partyName(respondent)
                        .roleOnCase(RESPONDENT)
                        .groupId(RESPONDENT7)
                        .visibility(EXTERNAL)
                        .build()
                );
            } else if (!respondent.equals(caseData.getRespondent7ExternalFlags().getPartyName())) {
                caseData.getRespondent7ExternalFlags().setPartyName(respondent);
            }
        }

        if (caseData.getRespondentCollection().size() > 8) {
            String respondent = caseData.getRespondentCollection().get(8).getValue().getRespondentName();
            if (caseData.getRespondent8Flags() == null) {
                caseData.setRespondent8Flags(CaseFlagsType.builder()
                        .partyName(respondent)
                        .roleOnCase(RESPONDENT)
                        .groupId(RESPONDENT8)
                        .visibility(INTERNAL)
                        .build()
                );
            } else if (!respondent.equals(caseData.getRespondent8Flags().getPartyName())) {
                caseData.getRespondent8Flags().setPartyName(respondent);
            }

            if (caseData.getRespondent8ExternalFlags() == null) {
                caseData.setRespondent8ExternalFlags(CaseFlagsType.builder()
                        .partyName(respondent)
                        .roleOnCase(RESPONDENT)
                        .groupId(RESPONDENT8)
                        .visibility(EXTERNAL)
                        .build()
                );
            } else if (!respondent.equals(caseData.getRespondent8ExternalFlags().getPartyName())) {
                caseData.getRespondent8ExternalFlags().setPartyName(respondent);
            }
        }

        if (caseData.getRespondentCollection().size() > 9) {
            String respondent = caseData.getRespondentCollection().get(9).getValue().getRespondentName();
            if (caseData.getRespondent9Flags() == null) {
                caseData.setRespondent9Flags(CaseFlagsType.builder()
                        .partyName(respondent)
                        .roleOnCase(RESPONDENT)
                        .groupId(RESPONDENT9)
                        .visibility(INTERNAL)
                        .build()
                );
            } else if (!respondent.equals(caseData.getRespondent9Flags().getPartyName())) {
                caseData.getRespondent9Flags().setPartyName(respondent);
            }

            if (caseData.getRespondent9ExternalFlags() == null) {
                caseData.setRespondent9ExternalFlags(CaseFlagsType.builder()
                        .partyName(respondent)
                        .roleOnCase(RESPONDENT)
                        .groupId(RESPONDENT9)
                        .visibility(EXTERNAL)
                        .build()
                );
            } else if (!respondent.equals(caseData.getRespondent9ExternalFlags().getPartyName())) {
                caseData.getRespondent9ExternalFlags().setPartyName(respondent);
            }
        }
    }

    /**
     * Sets case flags for Claimant, Respondent and Case level to null.
     *
     * @param caseData Data about the current case
     */
    public void rollbackCaseFlags(CaseData caseData) {
        caseData.setCaseFlags(null);
        caseData.setClaimantFlags(null);
        caseData.setClaimantExternalFlags(null);
        caseData.setRespondentFlags(null);
        caseData.setRespondentExternalFlags(null);
        caseData.setRespondent1Flags(null);
        caseData.setRespondent1ExternalFlags(null);
        caseData.setRespondent2Flags(null);
        caseData.setRespondent2ExternalFlags(null);
        caseData.setRespondent3Flags(null);
        caseData.setRespondent3ExternalFlags(null);
        caseData.setRespondent4Flags(null);
        caseData.setRespondent4ExternalFlags(null);
        caseData.setRespondent5Flags(null);
        caseData.setRespondent5ExternalFlags(null);
        caseData.setRespondent6Flags(null);
        caseData.setRespondent6ExternalFlags(null);
        caseData.setRespondent7Flags(null);
        caseData.setRespondent7ExternalFlags(null);
        caseData.setRespondent8Flags(null);
        caseData.setRespondent8ExternalFlags(null);
        caseData.setRespondent9Flags(null);
        caseData.setRespondent9ExternalFlags(null);
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
        ListTypeItem<FlagDetailType> partyLevel = ListTypeItem.concat(
                caseData.getClaimantFlags().getDetails(),
                caseData.getClaimantExternalFlags().getDetails()
        );

        partyLevel = appendDetailsIfPresent(partyLevel, caseData.getRespondentFlags());
        partyLevel = appendDetailsIfPresent(partyLevel, caseData.getRespondentExternalFlags());
        partyLevel = appendDetailsIfPresent(partyLevel, caseData.getRespondent1Flags());
        partyLevel = appendDetailsIfPresent(partyLevel, caseData.getRespondent1ExternalFlags());
        partyLevel = appendDetailsIfPresent(partyLevel, caseData.getRespondent2Flags());
        partyLevel = appendDetailsIfPresent(partyLevel, caseData.getRespondent2ExternalFlags());
        partyLevel = appendDetailsIfPresent(partyLevel, caseData.getRespondent3Flags());
        partyLevel = appendDetailsIfPresent(partyLevel, caseData.getRespondent3ExternalFlags());
        partyLevel = appendDetailsIfPresent(partyLevel, caseData.getRespondent4Flags());
        partyLevel = appendDetailsIfPresent(partyLevel, caseData.getRespondent4ExternalFlags());
        partyLevel = appendDetailsIfPresent(partyLevel, caseData.getRespondent5Flags());
        partyLevel = appendDetailsIfPresent(partyLevel, caseData.getRespondent5ExternalFlags());
        partyLevel = appendDetailsIfPresent(partyLevel, caseData.getRespondent6Flags());
        partyLevel = appendDetailsIfPresent(partyLevel, caseData.getRespondent6ExternalFlags());
        partyLevel = appendDetailsIfPresent(partyLevel, caseData.getRespondent7Flags());
        partyLevel = appendDetailsIfPresent(partyLevel, caseData.getRespondent7ExternalFlags());
        partyLevel = appendDetailsIfPresent(partyLevel, caseData.getRespondent8Flags());
        partyLevel = appendDetailsIfPresent(partyLevel, caseData.getRespondent8ExternalFlags());
        partyLevel = appendDetailsIfPresent(partyLevel, caseData.getRespondent9Flags());
        partyLevel = appendDetailsIfPresent(partyLevel, caseData.getRespondent9ExternalFlags());

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
}

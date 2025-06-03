package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.DateListedTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.HearingTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.HearingType;

import java.util.List;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.FLAG_DIGITAL_FILE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.FLAG_DO_NOT_POSTPONE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.FLAG_ECC;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.FLAG_LIVE_APPEAL;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.FLAG_REPORTING;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.FLAG_RESERVED;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.FLAG_SENSITIVE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.FLAG_WITH_OUTSTATION;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.IMAGE_FILE_EXTENSION;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.IMAGE_FILE_PRECEDING;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ONE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ZERO;

@Slf4j
public final class FlagsImageHelper {

    private static final String COLOR_ORANGE = "Orange";
    private static final String COLOR_LIGHT_BLACK = "LightBlack";
    private static final String COLOR_RED = "Red";
    private static final String COLOR_PURPLE = "Purple";
    private static final String COLOR_OLIVE = "Olive";
    private static final String COLOR_GREEN = "Green";
    private static final String COLOR_DARK_RED = "DarkRed";
    private static final String COLOR_WHITE = "White";
    private static final String COLOR_DEEP_PINK = "DeepPink";
    private static final String COLOR_SLATE_GRAY = "SlateGray";
    private static final String COLOR_DARK_SLATE_BLUE = "DarkSlateBlue";
    private static final String FLAG_REASONABLE_ADJUSTMENT = "REASONABLE ADJUSTMENT";
    private static final String FLAG_WELSH_LANGUAGE = "Cymraeg";
    private static final String WELSH = "Welsh";
    private static final String FLAG_RULE_493B = "RULE 49(3)b";
    private static final String FLAG_MIGRATED_FROM_ECM = "MIGRATED FROM ECM";
    private static final String SPEAK_TO_VP = "SPEAK TO VP";
    private static final String SPEAK_TO_REJ = "SPEAK TO REJ";

    private static final List<String> FLAGS = List.of(FLAG_MIGRATED_FROM_ECM, FLAG_WITH_OUTSTATION,
            FLAG_DO_NOT_POSTPONE, FLAG_LIVE_APPEAL, FLAG_RULE_493B, FLAG_REPORTING, FLAG_SENSITIVE, FLAG_RESERVED,
            FLAG_ECC, FLAG_DIGITAL_FILE, FLAG_REASONABLE_ADJUSTMENT, FLAG_WELSH_LANGUAGE, SPEAK_TO_VP, SPEAK_TO_REJ);

    private FlagsImageHelper() {
    }

    public static void buildFlagsImageFileName(CaseDetails caseDetails) {
        buildFlagsImageFileName(caseDetails.getCaseTypeId(), caseDetails.getCaseData());
    }

    public static void buildFlagsImageFileName(String caseTypeId, CaseData caseData) {
        StringBuilder flagsImageFileName = new StringBuilder();
        StringBuilder flagsImageAltText = new StringBuilder();

        flagsImageFileName.append(IMAGE_FILE_PRECEDING);
        FLAGS.forEach(flag -> setFlagImageFor(flag, flagsImageFileName, flagsImageAltText, caseData, caseTypeId));
        flagsImageFileName.append(IMAGE_FILE_EXTENSION);

        caseData.setFlagsImageAltText(flagsImageAltText.toString());
        caseData.setFlagsImageFileName(flagsImageFileName.toString());
    }

    private static void setFlagImageFor(String flagName, StringBuilder flagsImageFileName,
                                        StringBuilder flagsImageAltText, CaseData caseData, String caseTypeId) {
        boolean flagRequired;
        String flagColor; 
        switch (flagName) {
            case FLAG_WITH_OUTSTATION -> {
                flagRequired = withOutstation(caseData, caseTypeId);
                flagColor = COLOR_DEEP_PINK;
            }
            case FLAG_DO_NOT_POSTPONE -> {
                flagRequired = doNotPostpone(caseData);
                flagColor = COLOR_DARK_RED;
            }
            case FLAG_LIVE_APPEAL -> {
                flagRequired = liveAppeal(caseData);
                flagColor = COLOR_GREEN;
            }
            case FLAG_RULE_493B -> {
                flagRequired = rule493bApplies(caseData);
                flagColor = COLOR_RED;
            }
            case FLAG_REPORTING -> {
                flagRequired = rule503dApplies(caseData);
                flagColor = COLOR_LIGHT_BLACK;
            }
            case FLAG_SENSITIVE -> {
                flagRequired = sensitiveCase(caseData);
                flagColor = COLOR_ORANGE;
            }
            case FLAG_RESERVED -> {
                flagRequired = reservedJudgement(caseData);
                flagColor = COLOR_PURPLE;
            }
            case FLAG_ECC -> {
                flagRequired = counterClaimMade(caseData);
                flagColor = COLOR_OLIVE;
            }
            case FLAG_DIGITAL_FILE -> {
                flagRequired = digitalFile(caseData);
                flagColor = COLOR_SLATE_GRAY;
            }
            case FLAG_REASONABLE_ADJUSTMENT -> {
                flagRequired = reasonableAdjustment(caseData);
                flagColor = COLOR_DARK_SLATE_BLUE;
            }
            case FLAG_WELSH_LANGUAGE -> {
                flagRequired = welshColor(caseData);
                flagColor = COLOR_RED;
            }
            case FLAG_MIGRATED_FROM_ECM -> {
                flagRequired = YES.equals(defaultIfEmpty(caseData.getMigratedFromEcm(), ""));
                flagColor = "#D6292D";
            }
            case SPEAK_TO_VP -> {
                flagRequired = speakToVp(caseTypeId, caseData);
                flagColor = "#1D70B8";
            }
            case SPEAK_TO_REJ -> {
                flagRequired = speakToRej(caseTypeId, caseData);
                flagColor = "#1D70B8";
            }
            default -> {
                flagRequired = false;
                flagColor = COLOR_WHITE;
            }
        }

        flagsImageFileName.append(flagRequired ? ONE : ZERO);
        flagsImageAltText.append(flagRequired && !flagsImageAltText.isEmpty() ? "<font size='5'> - </font>" : "")
                .append(flagRequired ? "<font color='" + flagColor + "' size='5'> " + flagName + " </font>" : "");
    }

    private static boolean speakToRej(String caseTypeId, CaseData caseData) {
        if (SCOTLAND_CASE_TYPE_ID.equals(caseTypeId)) {
            return false;
        }

        return isInterventionRequired(caseData);
    }

    private static boolean speakToVp(String caseTypeId, CaseData caseData) {
        if (ENGLANDWALES_CASE_TYPE_ID.equals(caseTypeId)) {
            return false;
        }
        return isInterventionRequired(caseData);
    }

    private static boolean isInterventionRequired(CaseData caseData) {
        if (caseData.getAdditionalCaseInfoType() != null) {
            if (isNullOrEmpty(caseData.getAdditionalCaseInfoType().getInterventionRequired())) {
                return false;
            } else {
                return YES.equals(caseData.getAdditionalCaseInfoType().getInterventionRequired());
            }
        } else {
            return false;
        }
    }

    private static boolean sensitiveCase(CaseData caseData) {
        if (caseData.getAdditionalCaseInfoType() != null) {
            if (isNullOrEmpty(caseData.getAdditionalCaseInfoType().getAdditionalSensitive())) {
                return false;
            } else {
                return YES.equals(caseData.getAdditionalCaseInfoType().getAdditionalSensitive());
            }
        } else {
            return false;
        }
    }

    private static boolean rule503dApplies(CaseData caseData) {
        if (caseData.getRestrictedReporting() != null) {
            if (isNullOrEmpty(caseData.getRestrictedReporting().getImposed())) {
                return false;
            } else {
                return YES.equals(caseData.getRestrictedReporting().getImposed());
            }
        } else {
            return false;
        }
    }

    private static boolean rule493bApplies(CaseData caseData) {
        // Note: rule494b was previously rule503b which is why the field is named as such
        if (caseData.getRestrictedReporting() != null) {
            if (isNullOrEmpty(caseData.getRestrictedReporting().getRule503b())) {
                return false;
            } else {
                return YES.equals(caseData.getRestrictedReporting().getRule503b());
            }
        } else {
            return false;
        }
    }

    private static boolean reservedJudgement(CaseData caseData) {
        if (isEmpty(caseData.getHearingCollection())) {
            return false;
        }

        return caseData.getHearingCollection().stream()
                .map(HearingTypeItem::getValue)
                .filter(FlagsImageHelper::hearingNotNullOrEmpty)
                .flatMap(hearingType -> hearingType.getHearingDateCollection().stream())
                .filter(FlagsImageHelper::dateNotNull)
                .map(dateListedTypeItem -> dateListedTypeItem.getValue().getHearingReservedJudgement())
                .anyMatch(YES::equals);
    }

    private static boolean dateNotNull(DateListedTypeItem dateListedTypeItem) {
        return dateListedTypeItem != null && dateListedTypeItem.getValue() != null;
    }

    private static boolean hearingNotNullOrEmpty(HearingType hearingType) {
        return hearingType != null && isNotEmpty(hearingType.getHearingDateCollection());
    }

    private static boolean counterClaimMade(CaseData caseData) {
        return !isNullOrEmpty(caseData.getCounterClaim())
                || caseData.getEccCases() != null
                && !caseData.getEccCases().isEmpty();
    }

    private static boolean liveAppeal(CaseData caseData) {
        if (caseData.getAdditionalCaseInfoType() != null) {
            if (isNullOrEmpty(caseData.getAdditionalCaseInfoType().getAdditionalLiveAppeal())) {
                return false;
            } else {
                return YES.equals(caseData.getAdditionalCaseInfoType().getAdditionalLiveAppeal());
            }
        } else {
            return false;
        }
    }

    private static boolean doNotPostpone(CaseData caseData) {
        if (caseData.getAdditionalCaseInfoType() != null) {
            if (isNullOrEmpty(caseData.getAdditionalCaseInfoType().getDoNotPostpone())) {
                return false;
            } else {
                return YES.equals(caseData.getAdditionalCaseInfoType().getDoNotPostpone());
            }
        } else {
            return false;
        }
    }

    private static boolean reasonableAdjustment(CaseData caseData) {
        boolean flag = false;
        if (caseData.getAdditionalCaseInfoType() != null) {
            flag = YES.equals(caseData.getAdditionalCaseInfoType().getReasonableAdjustment());
        }
        if (!flag && caseData.getClaimantHearingPreference() != null) {
            flag = YES.equals(caseData.getClaimantHearingPreference().getReasonableAdjustments());
        }
        if (!flag && isNotEmpty(caseData.getRespondentCollection())) {
            flag = caseData.getRespondentCollection()
                    .stream()
                    .anyMatch(r -> YES.equals(
                            defaultIfEmpty(r.getValue().getEt3ResponseRespondentSupportNeeded(), "")));
        }
        return flag;
    }

    private static boolean welshColor(CaseData caseData) {
        return caseData.getClaimantHearingPreference() != null
                && (WELSH.equals(caseData.getClaimantHearingPreference().getContactLanguage())
                || WELSH.equals(caseData.getClaimantHearingPreference().getHearingLanguage()));
    }

    private static boolean digitalFile(CaseData caseData) {
        if (caseData.getAdditionalCaseInfoType() != null) {
            return YES.equals(caseData.getAdditionalCaseInfoType().getDigitalFile());
        } else {
            return false;
        }
    }

    private static boolean withOutstation(CaseData caseData, String caseTypeId) {
        return SCOTLAND_CASE_TYPE_ID.equals(caseTypeId)
                && !TribunalOffice.GLASGOW.getOfficeName().equals(caseData.getManagingOffice());
    }
}
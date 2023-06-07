package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.DateListedTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.HearingTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.HearingType;
import static com.google.common.base.Strings.isNullOrEmpty;
import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.FLAG_DIGITAL_FILE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.FLAG_DO_NOT_POSTPONE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.FLAG_ECC;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.FLAG_LIVE_APPEAL;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.FLAG_REPORTING;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.FLAG_RESERVED;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.FLAG_RULE_503B;
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

    private FlagsImageHelper() {
    }

    public static void buildFlagsImageFileName(CaseDetails caseDetails) {
        buildFlagsImageFileName(caseDetails.getCaseTypeId(), caseDetails.getCaseData());
    }

    public static void buildFlagsImageFileName(String caseTypeId, CaseData caseData) {
        StringBuilder flagsImageFileName = new StringBuilder();
        StringBuilder flagsImageAltText = new StringBuilder();

        flagsImageFileName.append(IMAGE_FILE_PRECEDING);
        setFlagImageFor(FLAG_WITH_OUTSTATION, flagsImageFileName, flagsImageAltText, caseData, caseTypeId);
        setFlagImageFor(FLAG_DO_NOT_POSTPONE, flagsImageFileName, flagsImageAltText, caseData, caseTypeId);
        setFlagImageFor(FLAG_LIVE_APPEAL, flagsImageFileName, flagsImageAltText, caseData, caseTypeId);
        setFlagImageFor(FLAG_RULE_503B, flagsImageFileName, flagsImageAltText, caseData, caseTypeId);
        setFlagImageFor(FLAG_REPORTING, flagsImageFileName, flagsImageAltText, caseData, caseTypeId);
        setFlagImageFor(FLAG_SENSITIVE, flagsImageFileName, flagsImageAltText, caseData, caseTypeId);
        setFlagImageFor(FLAG_RESERVED, flagsImageFileName, flagsImageAltText, caseData, caseTypeId);
        setFlagImageFor(FLAG_ECC, flagsImageFileName, flagsImageAltText, caseData, caseTypeId);
        setFlagImageFor(FLAG_DIGITAL_FILE, flagsImageFileName, flagsImageAltText, caseData, caseTypeId);
        setFlagImageFor(FLAG_REASONABLE_ADJUSTMENT, flagsImageFileName, flagsImageAltText, caseData, caseTypeId);
        setFlagImageFor(FLAG_WELSH_LANGUAGE, flagsImageFileName, flagsImageAltText, caseData, caseTypeId);
        flagsImageFileName.append(IMAGE_FILE_EXTENSION);

        caseData.setFlagsImageAltText(flagsImageAltText.toString());
        caseData.setFlagsImageFileName(flagsImageFileName.toString());
    }

    private static void setFlagImageFor(String flagName, StringBuilder flagsImageFileName,
                                        StringBuilder flagsImageAltText, CaseData caseData, String caseTypeId) {
        boolean flagRequired;
        String flagColor;

        switch (flagName) {
            case FLAG_WITH_OUTSTATION:
                flagRequired = withOutstation(caseData, caseTypeId);
                flagColor = COLOR_DEEP_PINK;
                break;
            case FLAG_DO_NOT_POSTPONE:
                flagRequired = doNotPostpone(caseData);
                flagColor = COLOR_DARK_RED;
                break;
            case FLAG_LIVE_APPEAL:
                flagRequired = liveAppeal(caseData);
                flagColor = COLOR_GREEN;
                break;
            case FLAG_RULE_503B:
                flagRequired = rule503bApplies(caseData);
                flagColor = COLOR_RED;
                break;
            case FLAG_REPORTING:
                flagRequired = rule503dApplies(caseData);
                flagColor = COLOR_LIGHT_BLACK;
                break;
            case FLAG_SENSITIVE:
                flagRequired = sensitiveCase(caseData);
                flagColor = COLOR_ORANGE;
                break;
            case FLAG_RESERVED:
                flagRequired = reservedJudgement(caseData);
                flagColor = COLOR_PURPLE;
                break;
            case FLAG_ECC:
                flagRequired = counterClaimMade(caseData);
                flagColor = COLOR_OLIVE;
                break;
            case FLAG_DIGITAL_FILE:
                flagRequired = digitalFile(caseData);
                flagColor = COLOR_SLATE_GRAY;
                break;
            case FLAG_REASONABLE_ADJUSTMENT:
                flagRequired = reasonableAdjustment(caseData);
                flagColor = COLOR_DARK_SLATE_BLUE;
                break;
            case FLAG_WELSH_LANGUAGE:
                flagRequired = welshColor(caseData);
                flagColor = COLOR_RED;
                break;
            default:
                flagRequired = false;
                flagColor = COLOR_WHITE;
                break;
        }
        flagsImageFileName.append(flagRequired ? ONE : ZERO);
        flagsImageAltText.append(flagRequired && flagsImageAltText.length() > 0 ? "<font size='5'> - </font>" : "");
        flagsImageAltText.append(flagRequired ? "<font color='"
                + flagColor + "' size='5'> " + flagName + " </font>" : "");
    }

    private static boolean sensitiveCase(CaseData caseData) {
        if (caseData.getAdditionalCaseInfoType() != null) {
            if (!isNullOrEmpty(caseData.getAdditionalCaseInfoType().getAdditionalSensitive())) {
                return caseData.getAdditionalCaseInfoType().getAdditionalSensitive().equals(YES);
            } else {
                return  false;
            }
        } else {
            return  false;
        }
    }

    private static boolean rule503dApplies(CaseData caseData) {
        if (caseData.getRestrictedReporting() != null) {
            if (!isNullOrEmpty(caseData.getRestrictedReporting().getImposed())) {
                return caseData.getRestrictedReporting().getImposed().equals(YES);
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private static boolean rule503bApplies(CaseData caseData) {
        if (caseData.getRestrictedReporting() != null) {
            if (!isNullOrEmpty(caseData.getRestrictedReporting().getRule503b())) {
                return caseData.getRestrictedReporting().getRule503b().equals(YES);
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private static boolean reservedJudgement(CaseData caseData) {
        if (CollectionUtils.isEmpty(caseData.getHearingCollection())) {
            return false;
        }

        for (HearingTypeItem hearingTypeItem : caseData.getHearingCollection()) {
            HearingType hearingType = hearingTypeItem.getValue();
            if (hearingType != null && CollectionUtils.isNotEmpty(hearingType.getHearingDateCollection())) {
                for (DateListedTypeItem dateListedTypeItem : hearingType.getHearingDateCollection()) {
                    if (dateListedTypeItem != null && dateListedTypeItem.getValue() != null) {
                        String hearingReservedJudgement = dateListedTypeItem.getValue().getHearingReservedJudgement();
                        if (YES.equals(hearingReservedJudgement)) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    private static boolean counterClaimMade(CaseData caseData) {
        return !isNullOrEmpty(caseData.getCounterClaim())
                || caseData.getEccCases() != null
                && !caseData.getEccCases().isEmpty();
    }

    private static boolean liveAppeal(CaseData caseData) {
        if (caseData.getAdditionalCaseInfoType() != null) {
            if (!isNullOrEmpty(caseData.getAdditionalCaseInfoType().getAdditionalLiveAppeal())) {
                return caseData.getAdditionalCaseInfoType().getAdditionalLiveAppeal().equals(YES);
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private static boolean doNotPostpone(CaseData caseData) {
        if (caseData.getAdditionalCaseInfoType() != null) {
            if (!isNullOrEmpty(caseData.getAdditionalCaseInfoType().getDoNotPostpone())) {
                return caseData.getAdditionalCaseInfoType().getDoNotPostpone().equals(YES);
            } else {
                return false;
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
        if (!flag && CollectionUtils.isNotEmpty(caseData.getRespondentCollection())) {
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
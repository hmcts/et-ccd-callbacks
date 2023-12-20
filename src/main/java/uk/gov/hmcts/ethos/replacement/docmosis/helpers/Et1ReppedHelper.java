package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import org.apache.commons.collections4.CollectionUtils;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.constants.ET1ReppedConstants;

import java.util.List;

import static com.google.common.base.Strings.isNullOrEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.ET1ReppedConstants.AVAILABLE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.ET1ReppedConstants.COMPLETED;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.ET1ReppedConstants.INDIVIDUAL;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.ET1ReppedConstants.NOT_COMPLETED;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.ET1ReppedConstants.ORGANISATION;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.ET1ReppedConstants.RESPONDENT_PREAMBLE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.ET1ReppedConstants.SECTION_COMPLETE_LABEL;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.ET1ReppedConstants.TAB_PRE_LABEL;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.ET1ReppedConstants.UNAVAILABLE;

public class Et1ReppedHelper {

    private Et1ReppedHelper() {
        super();
        // Access through static methods
    }

    /**
     * Sets the create draft data for the ET1 Repped journey.
     * @param caseData the case data
     */
    public static void setCreateDraftData(CaseData caseData, String caseId) {
        caseData.setEt1ReppedSectionOne(NO);
        caseData.setEt1ReppedSectionTwo(NO);
        caseData.setEt1ReppedSectionThree(NO);
        setEt1Statuses(caseData, caseId);
    }

    /**
     * Sets the statuses for the ET1 Repped journey.
     * @param caseData the case data
     */
    public static void setEt1Statuses(CaseData caseData, String caseId) {
        caseData.setEt1ClaimStatuses(TAB_PRE_LABEL + et1ClaimStatus(caseData, caseId));
    }

    private static String et1ClaimStatus(CaseData caseData, String caseId) {
        return ET1ReppedConstants.LABEL.formatted(
                caseId,
                sectionCompleted(caseData.getEt1ReppedSectionOne()),
                caseId,
                sectionCompleted(caseData.getEt1ReppedSectionTwo()),
                caseId,
                sectionCompleted(caseData.getEt1ReppedSectionThree()),
                caseId,
                allSectionsCompleted(caseData));
    }

    public static void setEt1SectionStatuses(CCDRequest ccdRequest) {
        String eventId = ccdRequest.getEventId();
        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        switch (eventId) {
            case "et1SectionOne" -> {
                setInitialSectionOneData(caseData);
                caseData.setEt1ReppedSectionOne(YES);
            }
            case "et1SectionTwo" -> {
                setInitialSectionTwoData(caseData);
                caseData.setEt1ReppedSectionTwo(YES);
            }
            case "et1SectionThree" -> caseData.setEt1ReppedSectionThree(YES);
            default -> throw new IllegalArgumentException("Unexpected value: " + eventId);
        }
        setEt1Statuses(caseData, ccdRequest.getCaseDetails().getCaseId());
    }

    private static String sectionCompleted(String section) {
        return isNullOrEmpty(section) || NO.equalsIgnoreCase(section)
                ? NOT_COMPLETED
                : COMPLETED;
    }

    private static String allSectionsCompleted(CaseData caseData) {
        return (!isNullOrEmpty(caseData.getEt1ReppedSectionOne())
                && YES.equalsIgnoreCase(caseData.getEt1ReppedSectionOne()))
                && (!isNullOrEmpty(caseData.getEt1ReppedSectionTwo())
                && YES.equalsIgnoreCase(caseData.getEt1ReppedSectionTwo()))
                && (!isNullOrEmpty(caseData.getEt1ReppedSectionThree())
                && YES.equalsIgnoreCase(caseData.getEt1ReppedSectionThree()))
                ? AVAILABLE : UNAVAILABLE;
    }

    /**
     * Validates the options selected for the ET1 Repped journey.
     * @param options the options
     * @return a list of error messages
     */
    public static List<String> validateSingleOption(List<String> options) {
        if (CollectionUtils.isNotEmpty(options) && options.size() > 1) {
            return List.of(ET1ReppedConstants.MULTIPLE_OPTION_ERROR);
        }
        return List.of();
    }

    /**
     * Generates the preamble for the additional respondent.
     * @param caseData the case data
     */
    public static void generateRespondentPreamble(CaseData caseData) {
        caseData.setAddAdditionalRespondentPreamble(RESPONDENT_PREAMBLE.formatted(getRespondentType(caseData)));
    }

    /**
     * Generates the preamble for the claimant work address.
     * @param caseData the case data
     */
    public static void generateWorkAddressLabel(CaseData caseData) {
        if (caseData.getRespondentAddress() == null) {
            throw new NullPointerException("Respondent address is null");
        }
        caseData.setDidClaimantWorkAtSameAddressPreamble(caseData.getRespondentAddress().toString());
    }

    private static void setInitialSectionOneData(CaseData caseData) {
        if (isNullOrEmpty(caseData.getClaimantFirstName()) || isNullOrEmpty(caseData.getClaimantLastName())) {
            throw new NullPointerException("Claimant name is null or empty");
        }
        caseData.setClaimant(caseData.getClaimantFirstName() + " " + caseData.getClaimantLastName());
    }

    private static void setInitialSectionTwoData(CaseData caseData) {
        caseData.setRespondent(getRespondentType(caseData));
    }

    public static String getSectionCompleted(CaseData caseData, String caseId) {
        return SECTION_COMPLETE_LABEL + et1ClaimStatus(caseData, caseId);
    }

    private static String getRespondentType(CaseData caseData) {
        if (INDIVIDUAL.equals(caseData.getRespondentType())) {
            return caseData.getRespondentFirstName() + " " + caseData.getRespondentLastName();
        } else if (ORGANISATION.equals(caseData.getRespondentType())) {
            return caseData.getRespondentOrganisationName();
        } else {
            throw new IllegalArgumentException("Unexpected value: " + caseData.getRespondentType());
        }
    }
}

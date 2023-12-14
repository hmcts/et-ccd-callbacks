package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import org.apache.commons.collections4.CollectionUtils;
import uk.gov.hmcts.et.common.model.ccd.CaseData;

import java.util.List;

import static com.google.common.base.Strings.isNullOrEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.INDIVIDUAL_TYPE_CLAIMANT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

public class Et1ReppedHelper {

    private static final String NOT_COMPLETED = "<strong class=\"govuk-tag govuk-tag--red\">Not completed</strong><br>";
    private static final String COMPLETED = "<strong class=\"govuk-tag govuk-tag--turquoise\">Completed</strong><br>";
    private static final String UNAVAILABLE = "<strong class=\"govuk-tag govuk-tag--grey\">Unavailable</strong><br>";
    private static final String AVAILABLE = "<strong class=\"govuk-tag govuk-tag--blue\">Available</strong><br>";
    private static final String RESPONDENT_PREAMBLE = """
            <h2>Respondent added:</h2>
             %s
            <hr>
            <p>You can amend the details of each respondent before you submit the claim.<p>
            """;

    private Et1ReppedHelper() {
        super();
        // Access through static methods
    }

    /**
     * Sets the create draft data for the ET1 Repped journey.
     * @param caseData the case data
     */
    public static void setCreateDraftData(CaseData caseData) {
        caseData.setEt1ReppedSectionOne(NO);
        caseData.setEt1ReppedSectionTwo(NO);
        caseData.setEt1ReppedSectionThree(NO);
        setEt1Statuses(caseData);
    }

    /**
     * Sets the statuses for the ET1 Repped journey.
     * @param caseData the case data
     */
    public static void setEt1Statuses(CaseData caseData) {
        String sectionOne = sectionCompleted(caseData.getEt1ReppedSectionOne());
        String sectionTwo = sectionCompleted(caseData.getEt1ReppedSectionTwo());
        String sectionThree = sectionCompleted(caseData.getEt1ReppedSectionThree());
        String submitCase = allSectionsCompleted(caseData);

        String label = """
                       Complete the ET1 claim form in 3 sections.
                                       
                       You can sign out and return to the claim if you've completed at least one section.
                                   
                       You can review and edit any completed section before you submit the claim to the tribunal.
                                                        
                       You can complete the sections in any order.

                       Steps to making a claim | Status
                       -|-
                       [ET1 Section 1 - Claimant details](/cases/case-details/${[CASE_REFERENCE]}/trigger/et1SectionOne/et1SectionOne1)                 | %s
                       [ET1 Section 2 - Employment and respondent details](/cases/case-details/${[CASE_REFERENCE]}/trigger/et1SectionTwo/et1SectionTwo1) | %s
                       [ET1 Section 3 - Details of the claim](/cases/case-details/${[CASE_REFERENCE]}/trigger/et1SectionThree/et1SectionThree1)             | %s
                       [Submit claim](/cases/case-details/${[CASE_REFERENCE]}/trigger/submitEt1Draft/submitEt1Draft1)                                     |  %s
                           """;
        caseData.setEt1ClaimStatuses(label.formatted(sectionOne, sectionTwo, sectionThree, submitCase));
    }

    /**
     * Sets the statuses for the ET1 Repped journey.
     * @param caseData the case data
     * @param section the section
     */
    public static void setEt1SectionStatuses(CaseData caseData, String section) {
        switch (section) {
            case "et1SectionOne" -> {
                setInitialSectionOneData(caseData);
                caseData.setEt1ReppedSectionOne(YES);
            }
            case "et1SectionTwo" -> {
                setInitialSectionTwoData(caseData);
                caseData.setEt1ReppedSectionTwo(YES);
            }
            case "et1SectionThree" -> caseData.setEt1ReppedSectionThree(YES);
            default -> throw new IllegalArgumentException("Unexpected value: " + section);
        }
        setEt1Statuses(caseData);
    }

    private static String sectionCompleted(String section) {
        return isNullOrEmpty(section) || NO.equalsIgnoreCase(section) ? NOT_COMPLETED : COMPLETED;
    }

    private static String allSectionsCompleted(CaseData caseData) {
        return (isNullOrEmpty(caseData.getEt1ReppedSectionOne())
                || NO.equalsIgnoreCase(caseData.getEt1ReppedSectionOne()))
                && (isNullOrEmpty(caseData.getEt1ReppedSectionTwo())
                || NO.equalsIgnoreCase(caseData.getEt1ReppedSectionTwo()))
                && (isNullOrEmpty(caseData.getEt1ReppedSectionThree())
                || NO.equalsIgnoreCase(caseData.getEt1ReppedSectionThree()))
                ? UNAVAILABLE : AVAILABLE;
    }

    /**
     * Validates the options selected for the ET1 Repped journey.
     * @param options the options
     * @return a list of error messages
     */
    public static List<String> validateSingleOption(List<String> options) {
        if (CollectionUtils.isNotEmpty(options) && options.size() > 1) {
            return List.of("Please select only one option");
        }
        return List.of();
    }

    /**
     * Generates the preamble for the additional respondent.
     * @param caseData the case data
     */
    public static void generateRespondentPreamble(CaseData caseData) {
        String respondent = "";
        if (INDIVIDUAL_TYPE_CLAIMANT.equals(caseData.getRespondentType())) {
            respondent = caseData.getRespondentFirstName() + " " + caseData.getRespondentLastName();
        } else if ("Organisation".equals(caseData.getRespondentType())) {
            respondent = caseData.getRespondentOrganisationName();
        } else {
            throw new IllegalStateException("Unexpected value: " + caseData.getRespondentType());
        }
        caseData.setAddAdditionalRespondentPreamble(RESPONDENT_PREAMBLE.formatted(respondent));

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
        caseData.setRespondent(INDIVIDUAL_TYPE_CLAIMANT.equals(caseData.getRespondentType())
                ? caseData.getRespondentFirstName() + " " + caseData.getRespondentLastName()
                : caseData.getRespondentOrganisationName());
    }
}

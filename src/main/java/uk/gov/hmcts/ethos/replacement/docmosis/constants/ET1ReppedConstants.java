package uk.gov.hmcts.ethos.replacement.docmosis.constants;

import java.util.List;
import java.util.Map;

public final class ET1ReppedConstants {
    public static final String TRIAGE_ERROR_MESSAGE = """
            The postcode you entered is not included under the early adopter sites yet. Please use the ET1 claim form
            linked on this page or copy the following into your browser:
            https://www.claim-employment-tribunals.service.gov.uk/
            """;
    public static final String MULTIPLE_OPTION_ERROR = "Please select only one option";

    public static final String ORGANISATION = "Organisation";
    public static final String INDIVIDUAL = "Individual";
    public static final String TAB_PRE_LABEL = """
       Complete the ET1 claim form in 3 sections.
       
       You can sign out and return to the claim if you've completed at least one section.

       You can review and edit any completed section before you submit the claim to the tribunal.

       You can download a draft copy of the ET1 form once at least one of the sections is marked as 'completed'.
       
       You can complete the sections in any order.

        """;
    public static final String SECTION_COMPLETE_LABEL = """
           Your answers have been saved.
           
           You can amend any answers before you submit the claim
                                                                                  
            """;
    public static final String LABEL = """
   Steps to making a claim | Status | Date completed
   -|-|-
   [ET1 Section 1 - Claimant details](/cases/case-details/%s/trigger/et1SectionOne/et1SectionOne1)|%s|%s
   [ET1 Section 2 - Employment & respondent details](/cases/case-details/%s/trigger/et1SectionTwo/et1SectionTwo1)|%s|%s
   [ET1 Section 3 - Details of the claim](/cases/case-details/%s/trigger/et1SectionThree/et1SectionThree1)|%s|%s
        """;

    @SuppressWarnings("checkstyle:LineLength")
    public static final String DOWNLOAD_DRAFT_LABEL = """
     [Download draft ET1](/cases/case-details/%s/trigger/createDraftEt1/createDraftEt11)|<strong class="govuk-tag govuk-tag--blue">Available</strong>|%s
         """;

    public static final String ET1_SUBMIT_AVAILABLE =
            "[Submit claim](/cases/case-details/%s/trigger/submitEt1Draft/submitEt1Draft1) "
            + "|  <strong class=\"govuk-tag govuk-tag--blue\">Available</strong>\n";
    public static final String ET1_SUBMIT_UNAVAILABLE =
            "Submit claim |  <strong class=\"govuk-tag govuk-tag--grey\">Unavailable</strong>\n";
    public static final String NOT_COMPLETED = "<strong class=\"govuk-tag govuk-tag--red\">Not completed</strong><br>";
    public static final String COMPLETED = "<strong class=\"govuk-tag govuk-tag--turquoise\">Completed</strong><br>";
    public static final String RESPONDENT_PREAMBLE = """
                           <h2>Respondent added:</h2>
                            %s
                           <hr>
                           <p>You can amend the details of each respondent before you submit the claim.<p>
                           """;
    public static final String OTHER = "Other";
    public static final List<String> TITLES = List.of("Mr", "Mrs", "Miss", "Ms", "Mx", OTHER);
    public static final String WEEKS = "Weeks";
    public static final String MONTHS = "Months";
    public static final String WORKING = "Working";
    public static final String NOTICE = "Notice";
    public static final String NO_LONGER_WORKING = "No longer working";
    public static final Map<String, String> PAY_PERIODS = Map.of("Weekly", WEEKS,
            "Monthly", MONTHS,
            "Annual", "Annual");
    public static final String NOT_SURE = "Not sure";
    public static final String CLAIM_DETAILS_MISSING = "Provide the details of the claim by uploading a document or "
                                                       + "entering them in the text box below";
    public static final String CLAIMANT_REPRESENTATIVE_NOT_FOUND = "There is no claimant representative for the claim";
    public static final String ERROR_CASE_NOT_FOUND = "There is no claim";

    private ET1ReppedConstants() {
        // Access through static methods
    }
}

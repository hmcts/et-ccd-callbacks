package uk.gov.hmcts.ethos.replacement.docmosis.constants;

public class ET1ReppedConstants {
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
                                        
       You can complete the sections in any order.

        """;
    public static final String SECTION_COMPLETE_LABEL = """
           Your answers have been saved.
           
           You can amend any answers before you submit the claim
                                                                                  
            """;
    public static final String LABEL = """
       Steps to making a claim | Status
       -|-
       [ET1 Section 1 - Claimant details](/cases/case-details/%s/trigger/et1SectionOne/et1SectionOne1)                 | %s
       [ET1 Section 2 - Employment and respondent details](/cases/case-details/%s/trigger/et1SectionTwo/et1SectionTwo1) | %s
       [ET1 Section 3 - Details of the claim](/cases/case-details/%s/trigger/et1SectionThree/et1SectionThree1)             | %s
       [Submit claim](/cases/case-details/%s/trigger/submitEt1Draft/submitEt1Draft1)                                     |  %s
           """;
    public static final String NOT_COMPLETED = "<strong class=\"govuk-tag govuk-tag--red\">Not completed</strong><br>";
    public static final String COMPLETED = "<strong class=\"govuk-tag govuk-tag--turquoise\">Completed</strong><br>";
    public static final String UNAVAILABLE = "<strong class=\"govuk-tag govuk-tag--grey\">Unavailable</strong><br>";
    public static final String AVAILABLE = "<strong class=\"govuk-tag govuk-tag--blue\">Available</strong><br>";
    public static final String RESPONDENT_PREAMBLE = """
                           <h2>Respondent added:</h2>
                            %s
                           <hr>
                           <p>You can amend the details of each respondent before you submit the claim.<p>
                           """;

    private ET1ReppedConstants() {
        // Access through static methods
    }
}

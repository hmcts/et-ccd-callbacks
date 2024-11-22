package uk.gov.hmcts.ethos.replacement.docmosis.constants;

import java.util.Map;

import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TableMarkupConstants.DATE_MARKUP;

public class InitialConsiderationConstants {
    public static final String RESPONDENT_NAME = """
        | Respondent %s name given | |
        |-------------|:------------|
        |In ET1 by claimant | %s|
        |In ET3 by respondent | %s|
        
        """;

    public static final String RESPONDENT_HEARING_PANEL_PREFERENCE = """
        | Respondent %s hearing panel preference | |
        |-------------|:------------|
        |Preference | %s|
        |Reason | %s|
        
        """;

    public static final String HEARING_DETAILS = """
        |Hearing details | |
        |-------------|:------------|
        """ + DATE_MARKUP + """
        |Type | %s|
        |Duration | %s|
        
        """;

    public static final String CLAIMANT_HEARING_PANEL_PREFERENCE = """
            |Claimant's hearing panel preference | |
            |-------------|:------------|
            |Panel Preference | %s|
            |Reason for Panel Preference | %s|
            """;

    public static final String CLAIMANT_HEARING_PANEL_PREFERENCE_MISSING =
            String.format(CLAIMANT_HEARING_PANEL_PREFERENCE, "-", "-");

    public static final String JURISDICTION_HEADER = "<h2>Jurisdiction codes</h2><a target=\"_blank\" "
            + "href=\"%s\">View all jurisdiction codes and descriptors (opens in new tab)</a><br><br>";
    public static final String CODES_URL_ENGLAND = "https://judiciary.sharepoint"
            + ".com/:b:/s/empjudgesew/EZowDqUAYpBEl9NkTirLUdYBjXdpi3-7b18HlsDqZNV3xA?e=tR7Wof";
    public static final String CODES_URL_SCOTLAND = "https://judiciary.sharepoint"
            + ".com/:b:/r/sites/ScotlandEJs/Shared%20Documents/Jurisdictional%20Codes%20List"
            + "/ET%20jurisdiction%20list%20(2019).pdf?csf=1&web=1&e=9bCQ8P";
    public static final String HEARING_MISSING = String.format(HEARING_DETAILS, "-", "-", "-");
    public static final String RESPONDENT_MISSING = String.format(RESPONDENT_NAME, "", "", "", "", "");
    public static final String DOC_GEN_ERROR = "Failed to generate document for case id: %s";
    public static final String IC_OUTPUT_NAME = "Initial Consideration.pdf";
    public static final String LIST_FOR_PRELIMINARY_HEARING = "List for preliminary hearing";
    public static final String LIST_FOR_FINAL_HEARING = "List for final hearing";
    public static final String UDL_HEARING = "UDL hearing";
    public static final String SEEK_COMMENTS = "Seek comments on the video hearing";
    public static final String SEEK_COMMENTS_SC = "Seek comments on the video hearing";
    public static final String HEARING_NOT_LISTED = "Do not list at present (give other directions below)";
    public static final String TELEPHONE = "Telephone";
    public static final String VIDEO = "Video";
    public static final String F2F = "F2F";
    public static final String CVP = "CVP";
    public static final Map<String, String> hearingTypeMappings = Map.of(
            "Video hearing", VIDEO,
            "CVP hearing", VIDEO,
            "Final F2F hearings (not Aberdeen)", F2F
    );
    public static final String JSA = "JSA";
    public static final String WITH_MEMBERS = "With members";

    private InitialConsiderationConstants() {
        // Access through static methods
    }
}

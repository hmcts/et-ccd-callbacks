package uk.gov.hmcts.ethos.replacement.docmosis.constants;

import java.util.Map;

public final class InitialConsiderationConstants {
    public static final String APPLICATIONS_FOR_STRIKE_OUT_OR_DEPOSIT = "Applications for strike out or deposit";
    public static final String RESPONDENT_NAME = """
            <tr>
              <td>Respondent %s</td>
              <td> %s </td>
              <td> %s </td>
            </tr>
        """;

    public static final String HEARING_PANEL_PREFERENCE = """
          <tr>
            <td>%s</td>
            <td>%s</td>
            <td>%s</td>
          </tr>
        """;

    public static final String HEARING_FORMAT_PREFERENCE = """
          <tr>
            <td>%s</td>
            <td>%s</td>
          </tr>
 
        """;

    public static final String CLAIMANT_HEARING_FORMAT_NEITHER_PREFERENCE = """
          <tr>
            <td>%s</td>
            <td>
                %s
                <h3>Reason:</h3>
                %s
            </td>
          </tr>

        """;

    public static final String HEARING_DETAILS = """
            <tr>
                <td>Date</td> <td> %s </td>
            </tr>
            <tr>
                <td>Type</td> <td> %s </td>
            </tr>
            <tr>
                <td>Duration</td> <td> %s </td>
            </tr>
            <tr>
                <td>Hearing format</td> <td> %s </td>
            </tr>
            <tr>
                <td>Panel Type</td> <td> %s </td>
            </tr>
            <tr>
                <td>Venue</td> <td> %s </td>
            </tr>
        """;

    public static final String CLAIMANT_HEARING_PANEL_PREFERENCE = """
            |Claimant's hearing panel preference | |
            |-------------|:------------|
            |Panel Preference | %s|
            |Reason for Panel Preference | %s|
            """;

    public static final String PARTIES_HEARING_PANEL_PREFERENCE = """
        %s
        %s
        
        """;

    public static final String PARTIES_HEARING_FORMAT = """
        %s
        %s
        
        """;

    public static final String CLAIMANT_HEARING_PANEL_PREFERENCE_MISSING =
            String.format(CLAIMANT_HEARING_PANEL_PREFERENCE, "-", "-");
    public static final String JURISDICTIONAL_ISSUES = "Jurisdictional issues";
    public static final String JURISDICTION_HEADER = "<h2>Jurisdiction codes</h2><a target=\"_blank\" "
            + "href=\"%s\">View all jurisdiction codes and descriptors (opens in new tab)</a><br><br>";
    public static final String CODES_URL_ENGLAND = "https://judiciary.sharepoint.com/sites/empjudgesew/"
            + "Shared%20Documents/Forms/AllItems.aspx?id=%2Fsites%2Fempjudgesew%2FShared%20Documents%2FET%20"
            + "Jurisdiction%20List%2FJurisdiction%20list%20October%202024.pdf&viewid=9cee6d50-61e5-4d87-92d2-"
            + "8c9444f00c95&parent=%2Fsites%2Fempjudgesew%2FShared%20Documents%2FET%20Jurisdiction%20List";
    public static final String CODES_URL_SCOTLAND = "https://judiciary.sharepoint"
            + ".com/:w:/r/sites/ScotlandEJs/Shared%20Documents/Jurisdictional%20Codes%20List"
            + "/Jurisdiction%20list%20July%202024%20.doc?d=wfa6ba431b0b941ffa0b82504fd093af0&csf=1&web=1&e=Dm6Hda";
    public static final String DETAIL = "Detail";
    public static final String GIVE_DETAILS = "Give Details:";
    public static final String DETAILS = "Details";
    public static final String DO_WE_HAVE_THE_RESPONDENT_S_NAME = "Do we have the respondent's name?";
    public static final String DOES_THE_RESPONDENT_S_NAME_MATCH = "Does the respondent's name match?";
    public static final String REFERRAL_ISSUE = "Referral Issue";
    public static final String HEARING_MISSING = String.format(HEARING_DETAILS, "-", "-", "-", "-", "-", "-");
    public static final String RESPONDENT_MISSING = String.format(RESPONDENT_NAME, "", "", "", "", "");
    public static final String DOC_GEN_ERROR = "Failed to generate document for case id: %s";
    public static final String IC_OUTPUT_NAME = "Initial Consideration.pdf";
    public static final String LIST_FOR_PRELIMINARY_HEARING = "List for preliminary hearing";
    public static final String LIST_FOR_FINAL_HEARING = "List for final hearing";
    public static final String NEWLINE = "\n";
    public static final String NONE_PROVIDED = "None provided.";
    public static final String UDL_HEARING = "UDL hearing";
    public static final String SEEK_COMMENTS = "Seek comments on the video hearing";
    public static final String SEEK_COMMENTS_SC = "Seek comments on the video hearing";
    public static final String HEARING_NOT_LISTED = "Do not list at present (give other directions below)";
    public static final String TELEPHONE = "Telephone";
    public static final String VIDEO = "Video";
    public static final String F2F = "F2F";
    public static final String CVP = "CVP";
    public static final String CVP_HEARING = "CVP hearing";
    public static final Map<String, String> HEARING_TYPE_MAPPINGS = Map.of(
            "Video hearing", VIDEO,
            CVP_HEARING, VIDEO,
            "Final F2F hearings (not Aberdeen)", F2F
    );
    public static final String JSA = "JSA";
    public static final String WITH_MEMBERS = "With members";
    public static final String INTERPRETERS = "Interpreters";
    public static final String ISSUE_RULE_27_NOTICE_AND_ORDER = "Issue Rule 27 Notice and order";
    public static final String ISSUE_RULE_28_NOTICE_AND_ORDER = "Issue Rule 28 Notice and order";
    public static final String ISSUE_RULE_29_NOTICE_AND_ORDER = "Issue Rule 29 Notice and order";
    public static final String ISSUE_RULE_27_NOTICE_AND_ORDER_SC = "issueRule27";
    public static final String ISSUE_RULE_28_NOTICE_AND_ORDER_SC = "issueRule28";
    public static final String RULE_29_NOTICE = "Rule 29 Notice";
    public static final String RULE_49 = "Rule 49";
    public static final String RULE_50 = "Rule 50";
    public static final String REQUEST_FOR_ADJUSTMENTS = "Request for adjustments";
    public static final String TIME_POINTS = "Time points";
    public static final String TABLE_END = """
            </tbody>
            </table>
            """;

    private InitialConsiderationConstants() {
        // Access through static methods
    }
}

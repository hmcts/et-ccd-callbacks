package uk.gov.hmcts.ethos.replacement.docmosis.utils;

import java.util.List;

public final class JurisdictionCodeTrackConstants {
    public static final List<String> JUR_CODE_CONCILIATION_TRACK_OP = List.of(
        "DAG", "DDA", "DRB", "DSO", "EQP", "GRA", "PID", "RRD", "SXD", "MAT", "VIC");
    public static final List<String> JUR_CODE_CONCILIATION_TRACK_ST = List.of(
        "ADG", "APA", "AWR", "DOD", "FCT", "FPI", "FLW", "FTE", "FT1", "FWP", "FWS",
        "HSD", "HSR", "IRF", "MWD", "PAC", "PLD", "PTE", "RTR(ST)", "SUN",
        "TPE", "TT", "TUE", "TUI", "TUM", "TUR", "TUS", "TXC(ST)", "UDC", "UDL", "UIA", "WTR");
    public static final List<String> JUR_CODE_CONCILIATION_TRACK_SH = List.of(
        "BOC", "FML", "FPA", "FTC", "FTO", "FTP", "FTR", "FTS", "FTU", "PAY", "RPT", "TIP", "WA", "WTR(AL)");
    public static final List<String> JUR_CODE_CONCILIATION_TRACK_NO = List.of(
        "ADT", "ADT(ST)", "CCP", "COM", "EAP", "HAS", "ISV", "LEV ", "LSO", "MWA",
        "NNA", "PEN",  "RPT(S)", "RTR", "TXC", "WTA");

    public static final String TRACK_OPEN = "Open";
    public static final String TRACK_STANDARD = "Standard";
    public static final String TRACK_SHORT = "Short";
    public static final String TRACK_NO = "No track";

    private JurisdictionCodeTrackConstants() {
    }
}

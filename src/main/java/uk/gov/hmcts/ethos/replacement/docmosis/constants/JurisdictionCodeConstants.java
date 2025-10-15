package uk.gov.hmcts.ethos.replacement.docmosis.constants;

import uk.gov.hmcts.et.common.model.ccd.items.JurCodesTypeItem;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;

public final class JurisdictionCodeConstants {
    public static final String JURISDICTION_EMPLOYMENT = "EMPLOYMENT";
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

    private static final String JURISDICTION_OUTCOME_ACAS_CONCILIATED_SETTLEMENT = "Acas conciliated settlement";
    private static final String JURISDICTION_OUTCOME_WITHDRAWN_OR_PRIVATE_SETTLEMENT =
            "Withdrawn or private settlement";
    private static final String JURISDICTION_OUTCOME_INPUT_IN_ERROR = "Input in error";
    private static final String JURISDICTION_OUTCOME_DISMISSED_ON_WITHDRAWAL = "Dismissed on withdrawal";
    static final List<String> HIDE_JURISDICTION_OUTCOME = Arrays.asList(
            JURISDICTION_OUTCOME_ACAS_CONCILIATED_SETTLEMENT,
            JURISDICTION_OUTCOME_WITHDRAWN_OR_PRIVATE_SETTLEMENT,
            JURISDICTION_OUTCOME_INPUT_IN_ERROR,
            JURISDICTION_OUTCOME_DISMISSED_ON_WITHDRAWAL);

    private JurisdictionCodeConstants() {
    }

    public static String getJurCodesCollectionWithHide(List<JurCodesTypeItem> jurCodesTypeItems) {
        if (isNotEmpty(jurCodesTypeItems)) {
            return defaultIfEmpty(
                jurCodesTypeItems.stream()
                    .filter(jurCodesTypeItem ->
                        !HIDE_JURISDICTION_OUTCOME.contains(jurCodesTypeItem.getValue().getJudgmentOutcome()))
                    .map(jurCodesTypeItem -> jurCodesTypeItem.getValue().getJuridictionCodesList())
                    .distinct()
                    .collect(Collectors.joining(", ")),
                " ");
        } else {
            return " ";
        }
    }
}

package uk.gov.hmcts.ethos.replacement.docmosis.service.admin.excelimport.fixedlistsheetreader;

import java.io.Serial;

public class FixedListSheetReaderException extends Exception {
    @Serial
    private static final long serialVersionUID = Long.MIN_VALUE;

    public FixedListSheetReaderException(String message) {
        super(message);
    }
}

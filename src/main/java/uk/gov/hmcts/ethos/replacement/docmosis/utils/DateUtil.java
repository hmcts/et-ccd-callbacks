package uk.gov.hmcts.ethos.replacement.docmosis.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public final class DateUtil {

    private DateUtil() {
        // Utility classes should not have a public or default constructor.
    }

    public static String getCurrentDateFormatted_yyyyMMddHHmm() {
        return new SimpleDateFormat("yyyyMMddHHmm").format(new Date());
    }

}

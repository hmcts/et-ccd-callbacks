package uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.util;

import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;

import static java.util.Optional.of;

public class PdfMapperUtil {

    private PdfMapperUtil() {
        // Add a private constructor to hide the implicit public one.
    }

    /**
     * Formats date from YYYY/MM/DD to DD/MM/YYYY.
     * @param dateToFormat String value of date to be formatted
     * @return Formatted date
     */
    public static String formatDate(String dateToFormat) {
        SimpleDateFormat parsingFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.UK);
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy", Locale.UK);
        String formattedDateStringValue;
        try {
            formattedDateStringValue = dateToFormat == null ? "" :
                    formatter.format(parsingFormatter.parse(dateToFormat));
        } catch (ParseException e) {
            return dateToFormat;
        }
        return formattedDateStringValue;
    }

    /**
     * Adds a new field to pdf form fields.
     * @param pdfFields map for mapping pdf fields with case data
     * @param fieldName name of the field in the pdf file
     * @param value value of the field in the case data
     */
    public static void addPdfField(ConcurrentMap<String, Optional<String>> pdfFields,
                                   String fieldName,
                                   String value) {
        if (StringUtils.isBlank(fieldName)) {
            return;
        }
        if (StringUtils.isBlank(value)) {
            pdfFields.put(fieldName, of(StringUtils.EMPTY));
        } else {
            pdfFields.put(fieldName, of(value));
        }
    }

    /**
     * Clones (creates a new instance) of any instance of any class.
     * @param object object to be cloned
     * @param classType class of the object to be cloned
     * @param <T> generics type of the object
     * @return  cloned object
     */
    public static <T> T cloneObject(T object, Class<T> classType) {
        Gson gson = new Gson();
        return classType.cast(gson.fromJson(gson.toJson(object), object.getClass()));
    }

}

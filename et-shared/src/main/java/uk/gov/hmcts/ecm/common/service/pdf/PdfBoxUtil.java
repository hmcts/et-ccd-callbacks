package uk.gov.hmcts.ecm.common.service.pdf;

import org.apache.pdfbox.pdmodel.interactive.form.PDTextField;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

/**
 * Utility methods shared across PDF generation services (ET1 and ET3).
 */
public final class PdfBoxUtil {

    /**
     * Values longer than this threshold are considered candidates for multiline wrapping
     * and auto-sizing. Short-value fields (dates, numbers, codes, etc.) are left untouched
     * so that their single-line vertical centering and original font size are preserved.
     */
    public static final int MULTILINE_VALUE_THRESHOLD = 20;

    private PdfBoxUtil() {
        // utility class
    }

    /**
     * Enables multiline text wrapping and auto-sizing for a PDF text field, but only
     * when the value is long enough to potentially overflow the field.
     * Setting the font size to 0 in the default appearance string instructs PDFBox to
     * auto-calculate an appropriate font size during field flattening.
     */
    public static void enableTextWrapAndAutoSize(PDTextField textField, String value) {
        if (value == null || value.length() <= MULTILINE_VALUE_THRESHOLD) {
            return;
        }
        textField.setMultiline(true);
        String da = textField.getDefaultAppearance();
        if (da != null && !da.isEmpty()) {
            // Replace fixed font size (e.g. "/Helv 12 Tf") with 0 to enable auto-sizing
            da = da.replaceAll("(/[A-Za-z0-9]+)\\s+\\d+(?:\\.\\d+)?\\s+Tf", "$1 0 Tf");
            textField.setDefaultAppearance(da);
        }
    }

    /**
     * Strips invisible Unicode format characters that are not encodable in WinAnsiEncoding
     * (e.g. zero-width spaces inserted by browsers or rich-text editors) to prevent
     * PDFBox from failing when generating appearance streams during flattening.
     */
    public static String sanitiseForPdf(String value) {
        if (value == null) {
            return null;
        }
        // Strip invisible Unicode format characters (zero-width spaces, directional marks, BOM, etc.)
        String sanitised = value.replaceAll("\\p{Cf}", "");
        // Replace typographic Unicode space variants not in WinAnsiEncoding with a regular space
        // e.g. figure space (U+2007), thin space (U+2009), em space (U+2003), narrow no-break space (U+202F)
        sanitised = sanitised.replaceAll("[\\u2000-\\u200A\\u202F\\u205F\\u3000]", " ");
        // Replace unsupported hyphen/dash variants with the nearest WinAnsiEncoding equivalent
        // U+2010 hyphen, U+2011 non-breaking hyphen, U+2012 figure dash → regular hyphen-minus
        // U+2015 horizontal bar, U+2E3A two-em dash, U+2E3B three-em dash → regular hyphen-minus
        sanitised = sanitised.replaceAll("[\\u2010-\\u2012\\uFE58\\uFE63\\uFF0D]", "-");
        sanitised = sanitised.replaceAll("[\\u2015\\u2E3A\\u2E3B]", "-");
        // Replace mathematical minus sign (U+2212) with hyphen-minus
        sanitised = sanitised.replace("\u2212", "-");
        // Final catch-all: strip any character still not encodable in Windows-1252 (WinAnsiEncoding).
        // This covers symbols, emoji, CJK, Arabic, Cyrillic, and any future edge-case characters.
        CharsetEncoder winAnsi = Charset.forName("windows-1252").newEncoder();
        StringBuilder result = new StringBuilder(sanitised.length());
        sanitised.codePoints()
                .filter(cp -> cp < 0x10000 && winAnsi.canEncode((char) cp))
                .forEach(result::appendCodePoint);
        return result.toString();
    }
}

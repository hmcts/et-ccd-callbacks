package uk.gov.hmcts.ecm.common.service.pdf;

import lombok.SneakyThrows;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import uk.gov.hmcts.ecm.common.model.CaseTestData;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ecm.common.constants.PdfMapperConstants.PDF_TYPE_ET1;
import static uk.gov.hmcts.ecm.common.constants.PdfMapperConstants.SUBMIT_ET1;
import static uk.gov.hmcts.ecm.common.service.pdf.et3.ET3FormConstants.ET3_FORM_CLIENT_TYPE_REPRESENTATIVE;

/**
 * Verifies that sanitiseForPdf correctly handles characters that are not encodable
 * in WinAnsiEncoding (Windows-1252), which PDFBox uses when flattening AcroForm fields.
 *
 * Tests are driven through the full PDF pipeline (createPdf → flatten → PDFTextStripper)
 * to validate that the generated PDF contains the expected sanitised text and no errors occur.
 */
class PdfSanitisationTest {

    private static final String TEMPLATE = "ET1_0224.pdf";

    private PdfService pdfService;
    private CaseTestData caseTestData;

    @BeforeEach
    void setUp() {
        pdfService = new PdfService(new ET1PdfMapperService());
        caseTestData = new CaseTestData();
    }

    // ── Invisible Unicode format characters (category Cf) ──────────────────────

    @SneakyThrows
    @Test
    void stripsZeroWidthSpace() {
        // U+200B between "self" and "employed" should be silently removed
        setClaimDescription("I was self\u200Bemployed.");
        String text = generateAndExtract();
        assertThat(text).contains("selfemployed");
        assertThat(text).doesNotContain("\u200B");
    }

    @SneakyThrows
    @Test
    void stripsZeroWidthNoBreakSpaceAndBom() {
        // U+FEFF (BOM / zero-width no-break space) should be stripped
        setClaimDescription("Amount\uFEFF: \u00a3500.");
        String text = generateAndExtract();
        assertThat(text).contains("\u00a3500");
        assertThat(text).doesNotContain("\uFEFF");
    }

    @SneakyThrows
    @Test
    void stripsWordJoinerAndDirectionalMarks() {
        // U+2060 word joiner, U+202C pop directional formatting — all invisible
        setClaimDescription("Notice\u2060period\u202C.");
        String text = generateAndExtract();
        assertThat(text).contains("Noticeperiod");
    }

    // ── Typographic space variants (category Zs) ───────────────────────────────

    @SneakyThrows
    @ParameterizedTest(name = "space variant U+{0} replaced with regular space")
    @CsvSource({
        "2007, figure space",
        "2009, thin space",
        "2003, em space",
        "2002, en space",
        "202F, narrow no-break space"
    })
    void replacesUnicodeSpaceVariantWithRegularSpace(String hex, String description) {
        char spaceVariant = (char) Integer.parseInt(hex, 16);
        setClaimDescription("28" + spaceVariant + "November 2024.");
        String text = generateAndExtract();
        // The space variant should be replaced; "28" and "November" should both appear
        assertThat(text).contains("28");
        assertThat(text).contains("November");
        assertThat(text).doesNotContain(String.valueOf(spaceVariant));
    }

    // ── Hyphen / dash variants ─────────────────────────────────────────────────

    @SneakyThrows
    @ParameterizedTest(name = "hyphen variant U+{0} replaced with hyphen-minus")
    @CsvSource({
        "2011, non-breaking hyphen",
        "2010, hyphen",
        "2012, figure dash"
    })
    void replacesHyphenVariantWithHyphenMinus(String hex, String description) {
        char hyphenVariant = (char) Integer.parseInt(hex, 16);
        setClaimDescription("self" + hyphenVariant + "employed.");
        String text = generateAndExtract();
        assertThat(text).contains("self-employed");
        assertThat(text).doesNotContain(String.valueOf(hyphenVariant));
    }

    @SneakyThrows
    @Test
    void replacesThreeEmDashWithEmDash() {
        // U+2E3B three-em dash → U+2014 em dash (which IS in WinAnsiEncoding)
        setClaimDescription("Note\u2E3Bsee above.");
        String text = generateAndExtract();
        assertThat(text).contains("\u2014");
        assertThat(text).doesNotContain("\u2E3B");
    }

    @SneakyThrows
    @Test
    void replacesMathematicalMinusWithHyphenMinus() {
        // U+2212 minus sign → regular hyphen-minus
        setClaimDescription("Balance: \u2212\u00a3200.");
        String text = generateAndExtract();
        assertThat(text).contains("-\u00a3200");
        assertThat(text).doesNotContain("\u2212");
    }

    // ── Catch-all: unencodable characters stripped ─────────────────────────────

    @SneakyThrows
    @ParameterizedTest(name = "unencodable character U+{0} ({1}) is stripped")
    @CsvSource({
        "2192, rightwards arrow",
        "2248, almost equal to",
        "25CF, black circle bullet",
        "2212, minus sign",    // also covered by explicit replacement above
        "0420, Cyrillic R",
        "0627, Arabic alef",
        "064A, Arabic yeh",
        "8FD8, CJK ideograph"
    })
    void stripsUnencodableCharacterFromCatchAll(String hex, String description) {
        char unencodable = (char) Integer.parseInt(hex, 16);
        // Wrap in known text so we can verify the surrounding text is preserved
        setClaimDescription("before" + unencodable + "after.");
        // Should generate without error — no exception means the catch-all handled it
        byte[] pdfBytes = generatePdf();
        assertThat(pdfBytes).isNotEmpty();
        String text = extractText(pdfBytes);
        assertThat(text).doesNotContain(String.valueOf(unencodable));
        assertThat(text).contains("before");
        assertThat(text).contains("after");
    }

    // ── WinAnsiEncoding characters preserved unchanged ─────────────────────────

    @SneakyThrows
    @Test
    void preservesWinAnsiEncodableCharacters() {
        // £ (U+00A3), é (U+00E9), — (U+2014), – (U+2013), € (U+20AC)
        // are all in Windows-1252 and should render unchanged
        setClaimDescription("Deducted \u00a3976.80 \u2014 see note\u2013A. Caf\u00e9.");
        String text = generateAndExtract();
        assertThat(text).contains("\u00a3976.80");
        assertThat(text).contains("\u2014");
        assertThat(text).contains("Caf\u00e9");
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private void setClaimDescription(String description) {
        caseTestData.getCaseData().getClaimantRequests().setClaimDescription(description);
    }

    @SneakyThrows
    private byte[] generatePdf() {
        return pdfService.createPdf(
            caseTestData.getCaseData(),
            TEMPLATE,
            PDF_TYPE_ET1,
            ET3_FORM_CLIENT_TYPE_REPRESENTATIVE,
            SUBMIT_ET1
        );
    }

    @SneakyThrows
    private String generateAndExtract() {
        return extractText(generatePdf());
    }

    @SneakyThrows
    private String extractText(byte[] pdfBytes) {
        try (PDDocument doc = Loader.loadPDF(pdfBytes)) {
            return new PDFTextStripper().getText(doc);
        }
    }
}

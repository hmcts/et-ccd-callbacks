package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import uk.gov.hmcts.et.common.model.ccd.Address;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantIndType;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantType;
import uk.gov.hmcts.et.common.model.ccd.types.multiples.AdditionalClaimant;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class that builds a PDF listing contact details for all claimants in a multiple.
 *
 * <p>Uses Apache PDFBox 3 with standard Helvetica Type 1 fonts.  Text is sanitised to the
 * WinAnsi encoding range (code points 0x20–0xFF) so characters outside that range are
 * replaced with {@code '?'} rather than causing an {@code IllegalArgumentException}. Note that
 * this range covers accented Latin-1 characters (e.g. â, ê, ô, û) but NOT Welsh circumflex
 * characters such as ŵ/ŵ or ŷ/Ŷ (Latin Extended-A, above 0xFF) - if Welsh claimant data or
 * headings ever require those, a Unicode-capable embedded font (e.g. via {@code PDType0Font})
 * would be needed instead of the standard 14 fonts used here.
 */
public final class ClaimantContactDetailsPdfHelper {

    private static final float MARGIN = 50f;
    private static final float TITLE_SIZE = 16f;
    private static final float HEADING_SIZE = 12f;
    private static final float BODY_SIZE = 10f;
    private static final float HEADING_LINE_HEIGHT = 18f;
    private static final float BODY_LINE_HEIGHT = 14f;
    private static final float SECTION_SPACING = 18f;

    private static final String TITLE_EN = "Claimant Contact Details";
    private static final String TITLE_CY = "Manylion Cyswllt Hawlwyr";
    private static final String LEAD_CLAIMANT_HEADING_EN = "Lead Claimant";
    private static final String LEAD_CLAIMANT_HEADING_CY = "Prif Hawlydd";
    private static final String ADDITIONAL_CLAIMANTS_HEADING_EN = "Additional Claimants";
    private static final String ADDITIONAL_CLAIMANTS_HEADING_CY = "Hawlwyr Ychwanegol";

    private ClaimantContactDetailsPdfHelper() {
    }

    /**
     * Generates the English-language PDF containing contact details for the lead claimant and
     * every additional claimant.
     *
     * @param leadCaseData        CCD case data for the lead case (used for the lead claimant's details)
     * @param additionalClaimants ordered list of additional claimants
     * @return the raw PDF bytes
     * @throws IOException if PDFBox fails to create or serialise the document
     */
    public static byte[] buildPdf(CaseData leadCaseData, List<AdditionalClaimant> additionalClaimants)
            throws IOException {
        return buildPdf(leadCaseData, additionalClaimants,
                TITLE_EN, LEAD_CLAIMANT_HEADING_EN, ADDITIONAL_CLAIMANTS_HEADING_EN);
    }

    /**
     * Generates the Welsh-language PDF containing contact details for the lead claimant and
     * every additional claimant.
     *
     * @param leadCaseData        CCD case data for the lead case (used for the lead claimant's details)
     * @param additionalClaimants ordered list of additional claimants
     * @return the raw PDF bytes
     * @throws IOException if PDFBox fails to create or serialise the document
     */
    public static byte[] buildPdfWelsh(CaseData leadCaseData, List<AdditionalClaimant> additionalClaimants)
            throws IOException {
        return buildPdf(leadCaseData, additionalClaimants,
                TITLE_CY, LEAD_CLAIMANT_HEADING_CY, ADDITIONAL_CLAIMANTS_HEADING_CY);
    }

    /**
     * Shared PDF-building logic used by both language variants.
     *
     * @param leadCaseData               CCD case data for the lead case
     * @param additionalClaimants        ordered list of additional claimants
     * @param title                      document title heading
     * @param leadClaimantHeading        lead claimant section heading
     * @param additionalClaimantsHeading additional claimants section heading
     * @return the raw PDF bytes
     * @throws IOException if PDFBox fails to create or serialise the document
     */
    private static byte[] buildPdf(CaseData leadCaseData, List<AdditionalClaimant> additionalClaimants,
                                   String title, String leadClaimantHeading, String additionalClaimantsHeading)
            throws IOException {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            PDType1Font boldFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font regularFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            PageWriter writer = new PageWriter(document, boldFont, regularFont);

            writer.writeLine(title, boldFont, TITLE_SIZE, HEADING_LINE_HEIGHT);
            writer.addSpacing(SECTION_SPACING);

            writer.writeLine(leadClaimantHeading, boldFont, HEADING_SIZE, HEADING_LINE_HEIGHT);
            writeLeadClaimantSection(writer, leadCaseData, regularFont);
            writer.addSpacing(SECTION_SPACING);

            if (additionalClaimants != null) {
                writer.writeLine(additionalClaimantsHeading, boldFont, HEADING_SIZE, HEADING_LINE_HEIGHT);
                for (AdditionalClaimant claimant : additionalClaimants) {
                    if (claimant == null) {
                        continue;
                    }
                    writeAdditionalClaimantSection(writer, claimant, regularFont);
                    writer.addSpacing(SECTION_SPACING);
                }
            }

            writer.close();
            document.save(baos);
            return baos.toByteArray();
        }
    }

    private static void writeLeadClaimantSection(PageWriter writer, CaseData caseData,
                                                 PDType1Font font) throws IOException {
        if (caseData == null) {
            return;
        }
        ClaimantIndType indType = caseData.getClaimantIndType();
        ClaimantType claimantType = caseData.getClaimantType();

        if (indType != null) {
            String name = formatName(indType.getClaimantTitle(),
                    indType.getClaimantFirstNames(), indType.getClaimantLastName());
            if (StringUtils.isNotBlank(name)) {
                writer.writeLine("Name: " + name, font, BODY_SIZE, BODY_LINE_HEIGHT);
            }
        }
        if (claimantType != null) {
            String email = claimantType.getClaimantEmailAddress();
            if (StringUtils.isNotBlank(email)) {
                writer.writeLine("Email: " + email, font, BODY_SIZE, BODY_LINE_HEIGHT);
            }
            writeAddress(writer, claimantType.getClaimantAddressUK(), font);
        }
    }

    private static void writeAdditionalClaimantSection(PageWriter writer, AdditionalClaimant claimant,
                                                       PDType1Font font) throws IOException {
        if (StringUtils.isNotBlank(claimant.getFirstName())
                && StringUtils.isNotBlank(claimant.getLastName())) {
            writer.writeLine("Name: "
                            + claimant.getFirstName() + " "
                            + claimant.getLastName(),
                    font, BODY_SIZE, BODY_LINE_HEIGHT);
        }
        if (StringUtils.isNotBlank(claimant.getEmail())) {
            writer.writeLine("Email: " + claimant.getEmail(), font, BODY_SIZE, BODY_LINE_HEIGHT);
        }
        writeAddress(writer, claimant.getAddress(), font);
    }

    private static void writeAddress(PageWriter writer, Address address, PDType1Font font) throws IOException {
        if (address == null) {
            return;
        }
        String formatted = address.toString();
        if (StringUtils.isNotBlank(formatted)) {
            writer.writeLine("Address: " + formatted, font, BODY_SIZE, BODY_LINE_HEIGHT);
        }
    }

    private static String formatName(String title, String firstName, String lastName) {
        List<String> parts = new ArrayList<>();
        if (StringUtils.isNotBlank(title)) {
            parts.add(title.trim());
        }
        if (StringUtils.isNotBlank(firstName)) {
            parts.add(firstName.trim());
        }
        if (StringUtils.isNotBlank(lastName)) {
            parts.add(lastName.trim());
        }
        return String.join(" ", parts);
    }

    /**
     * Replaces characters outside the WinAnsi encoding range (code points 0x20–0xFF) with {@code '?'}.
     * This prevents {@code IllegalArgumentException} from PDFBox when trying to encode unsupported glyphs.
     */
    static String sanitize(String text) {
        if (text == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(text.length());
        for (char c : text.toCharArray()) {
            sb.append((c >= 0x20 && c <= 0xFF) ? c : '?');
        }
        return sb.toString();
    }

    /**
     * Stateful helper that tracks the current page and Y position, adding new pages automatically
     * when the content would overflow the bottom margin.
     */
    private static final class PageWriter {
        private final PDDocument document;
        private final PDType1Font boldFont;
        private final PDType1Font regularFont;
        private PDPageContentStream contentStream;
        private float currentY;

        PageWriter(PDDocument document, PDType1Font boldFont, PDType1Font regularFont) throws IOException {
            this.document = document;
            this.boldFont = boldFont;
            this.regularFont = regularFont;
            addPage();
        }

        void writeLine(String text, PDType1Font font, float fontSize, float lineHeight) throws IOException {
            if (currentY - lineHeight < MARGIN) {
                contentStream.close();
                addPage();
            }
            contentStream.beginText();
            contentStream.setFont(font, fontSize);
            contentStream.newLineAtOffset(MARGIN, currentY);
            contentStream.showText(sanitize(text));
            contentStream.endText();
            currentY -= lineHeight;
        }

        void addSpacing(float spacing) {
            currentY -= spacing;
        }

        void close() throws IOException {
            if (contentStream != null) {
                contentStream.close();
                contentStream = null;
            }
        }

        private void addPage() throws IOException {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            contentStream = new PDPageContentStream(document, page);
            currentY = PDRectangle.A4.getHeight() - MARGIN;
        }
    }
}
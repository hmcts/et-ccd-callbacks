package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.JurisdictionCode;

import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.CASE_NAME_AND_DESCRIPTION_HTML;

@Slf4j
public class JurisdictionCodeHelper {

    /**
     * Appends the HTML to list out jurisdiction codes.
     * @param sb the StringBuilder to append to
     * @param codeName code used to get description and to append
     */
    public static void populateCodeNameAndDescriptionHtml(StringBuilder sb, String codeName) {
        if (codeName != null) {
            try {
                String enumCode = codeName.replaceAll("[^a-zA-Z]+", "");
                String description = JurisdictionCode.valueOf(enumCode).getDescription();
                sb.append(String.format(CASE_NAME_AND_DESCRIPTION_HTML, codeName, description));
            } catch (IllegalArgumentException e) {
                log.warn("Invalid jurisdiction code: {}", codeName, e);
            }
        }
    }
}

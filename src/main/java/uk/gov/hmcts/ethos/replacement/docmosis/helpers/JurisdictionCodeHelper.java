package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.JurisdictionCode;

import java.util.Objects;
import java.util.UUID;

import static com.google.common.base.Strings.isNullOrEmpty;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.CASE_NAME_AND_DESCRIPTION_HTML;

@Slf4j
public final class JurisdictionCodeHelper {

    private JurisdictionCodeHelper() {
    }

    /**
     * Populates missing IDs on items in jurCodesCollection.
     * @param caseData the case data
     */
    public static void populateJurCodesCollectionIds(CaseData caseData) {
        if (caseData == null || CollectionUtils.isEmpty(caseData.getJurCodesCollection())) {
            return;
        }
        caseData.getJurCodesCollection().stream()
                .filter(Objects::nonNull)
                .filter(item -> isNullOrEmpty(item.getId()))
                .forEach(item -> item.setId(UUID.randomUUID().toString()));
    }

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
                log.error("Invalid jurisdiction code: {}", codeName, e);
            }
        }
    }
}

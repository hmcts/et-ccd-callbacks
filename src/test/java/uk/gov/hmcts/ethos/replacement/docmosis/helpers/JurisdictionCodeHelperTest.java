package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.JurCodesTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.JurCodesType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class JurisdictionCodeHelperTest {

    @Test
    void populateJurCodesCollectionIds_populatesMissingIdsAndPreservesExistingIds() {
        JurCodesTypeItem missingIdItem = new JurCodesTypeItem();
        JurCodesType jurCodesType1 = new JurCodesType();
        jurCodesType1.setJuridictionCodesList("DAG");
        missingIdItem.setValue(jurCodesType1);

        JurCodesTypeItem existingIdItem = new JurCodesTypeItem();
        existingIdItem.setId("existing-id-123");
        JurCodesType jurCodesType2 = new JurCodesType();
        jurCodesType2.setJuridictionCodesList("DDA");
        existingIdItem.setValue(jurCodesType2);

        CaseData caseData = new CaseData();
        caseData.setJurCodesCollection(new ArrayList<>(Arrays.asList(missingIdItem, existingIdItem, null)));

        JurisdictionCodeHelper.populateJurCodesCollectionIds(caseData);

        assertThat(missingIdItem.getId()).isNotBlank();
        assertThat(existingIdItem.getId()).isEqualTo("existing-id-123");
    }

    @Test
    void populateJurCodesCollectionIds_handlesNullAndEmptyGracefully() {
        assertDoesNotThrow(() -> JurisdictionCodeHelper.populateJurCodesCollectionIds(null));

        CaseData caseDataWithNullCollection = new CaseData();
        assertDoesNotThrow(() -> JurisdictionCodeHelper.populateJurCodesCollectionIds(caseDataWithNullCollection));

        CaseData caseDataWithEmptyCollection = new CaseData();
        caseDataWithEmptyCollection.setJurCodesCollection(Collections.emptyList());
        assertDoesNotThrow(() -> JurisdictionCodeHelper.populateJurCodesCollectionIds(caseDataWithEmptyCollection));
    }

    @Test
    void populateCodeNameAndDescriptionHtml_populatesValidCodeAndIgnoresInvalidOrNull() {
        StringBuilder sb = new StringBuilder();
        JurisdictionCodeHelper.populateCodeNameAndDescriptionHtml(sb, "DAG");
        JurisdictionCodeHelper.populateCodeNameAndDescriptionHtml(sb, "INVALID_CODE");
        JurisdictionCodeHelper.populateCodeNameAndDescriptionHtml(sb, null);

        assertThat(sb.toString()).contains("DAG");
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
        "SXH|Sexual harassment by unwanted conduct of a sexual nature or because of rejection of such conduct",
        "TUA|Suffered a detriment as a result of union membership or activities, or taking part in protected "
            + "industrial action",
        "TUD|Unfair dismissal for having taken part in protected industrial action"
    })
    void populateCodeNameAndDescriptionHtml_newJurisdictionCodes(String code, String description) {
        StringBuilder sb = new StringBuilder();
        JurisdictionCodeHelper.populateCodeNameAndDescriptionHtml(sb, code);

        assertThat(sb.toString()).contains(code).contains(description);
    }
}

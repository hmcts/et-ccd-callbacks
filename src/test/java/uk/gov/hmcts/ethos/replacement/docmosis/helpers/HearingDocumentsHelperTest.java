package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_STATUS_HEARD;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_STATUS_LISTED;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_TYPE_JUDICIAL_HEARING;

class HearingDocumentsHelperTest {

    private CaseData caseData;

    @BeforeEach
    void setUp() {
        // Set up a case with two hearings, one past and one future
        caseData = CaseDataBuilder.builder()
            .withHearing("1", HEARING_TYPE_JUDICIAL_HEARING, "Judge", "Venue", null, null, null, null)
            .withHearingSession(0, LocalDateTime.now().minusDays(1).toString(), HEARING_STATUS_HEARD, false)
            .withHearing("2", HEARING_TYPE_JUDICIAL_HEARING, "Judge", "Venue", null, null, null, null)
            .withHearingSession(1, LocalDateTime.now().plusDays(1).toString(), HEARING_STATUS_LISTED, false)
            .build();
    }

    @Test
    void populateHearingDetails() {
        List<String> errors = HearingDocumentsHelper.populateHearingDetails(caseData);
        assertTrue(errors.isEmpty());
        assertNotNull(caseData.getUploadHearingDocumentsSelectFutureHearing());
        assertNotNull(caseData.getUploadHearingDocumentsSelectPastHearing());

        // Check future hearing
        assertEquals(1, caseData.getUploadHearingDocumentsSelectFutureHearing().getListItems().size());

        // Check past hearing
        assertEquals(1, caseData.getUploadHearingDocumentsSelectPastHearing().getListItems().size());
    }

    @Test
    void populateHearingDetails_noHearings() {
        caseData.setHearingCollection(null);
        List<String> errors = HearingDocumentsHelper.populateHearingDetails(caseData);
        assertEquals(1, errors.size());
        assertEquals(HearingDocumentsHelper.HEARING_DOCUMENT_NO_HEARING_ERROR, errors.getFirst());
        assertNull(caseData.getUploadHearingDocumentsSelectFutureHearing());
        assertNull(caseData.getUploadHearingDocumentsSelectPastHearing());
    }

    @ParameterizedTest
    @CsvSource(
        value = {
            "Past, 1",
            "Future, 2"
        }
    )
    void getSelectedHearing(String selectedHearingType, String hearingNumber) {
        HearingDocumentsHelper.populateHearingDetails(caseData);
        caseData.getUploadHearingDocumentsSelectFutureHearing().setFirstItemAsSelected();
        caseData.getUploadHearingDocumentsSelectPastHearing().setFirstItemAsSelected();
        caseData.setUploadHearingDocumentsSelectPastOrFutureHearing(selectedHearingType);
        DynamicValueType hearing = HearingDocumentsHelper.getSelectedHearing(caseData);
        assertEquals(hearingNumber, hearing.getCode());
    }

    @Test
    void getSelectedHearing_unknownOption() {
        caseData.setUploadHearingDocumentsSelectPastOrFutureHearing("Unknown");
        assertThrows(IllegalArgumentException.class, () -> HearingDocumentsHelper.getSelectedHearing(caseData));
    }
}
package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.UploadHearingDocumentType;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLAIMANT_TITLE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.RESPONDENT_TITLE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.HelperTest.getUserDetails;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.DocumentFixtures.getUploadedDocumentType;

class HearingDocumentsServiceTest {

    private CaseData caseData;
    private UserIdamService userIdamService;
    @InjectMocks
    private HearingDocumentsService hearingDocumentsService;

    @BeforeEach
    void setUp() {
        caseData = new CaseData();
        UploadHearingDocumentType uploadHearingDocumentType = new UploadHearingDocumentType();
        uploadHearingDocumentType.setDocument(getUploadedDocumentType());
        caseData.setUploadHearingDocumentType(List.of(GenericTypeItem.from(uploadHearingDocumentType)));
        caseData.setUploadHearingDocumentsSelectPastOrFutureHearing("Past");
        caseData.setUploadHearingDocumentsSelectPastHearing(DynamicFixedListType.from("1", "Past Hearing", true));
        caseData.setUploadHearingDocumentsDateSubmitted(LocalDate.now().toString());

        userIdamService = mock(UserIdamService.class);
        hearingDocumentsService = new HearingDocumentsService(userIdamService);
        when(userIdamService.getUserDetails(anyString())).thenReturn(getUserDetails());
    }

    @Test
    void addDocumentToHearingDocuments_Claimant() {
        caseData.setUploadHearingDocumentsWhoseDocuments(CLAIMANT_TITLE);
        hearingDocumentsService.addDocumentToHearingDocuments(caseData, "token");
        assertEquals(1, caseData.getBundlesClaimantCollection().size());
        checkIfFieldsAreCleared(caseData);
    }

    @Test
    void addDocumentToHearingDocuments_Respondent() {
        caseData.setUploadHearingDocumentsWhoseDocuments(RESPONDENT_TITLE);
        hearingDocumentsService.addDocumentToHearingDocuments(caseData, "token");
        assertEquals(1, caseData.getBundlesRespondentCollection().size());
        checkIfFieldsAreCleared(caseData);
    }

    @Test
    void addDocumentToHearingDocuments_Other() {
        caseData.setUploadHearingDocumentsWhoseDocuments("Unknown");
        assertThrows(IllegalArgumentException.class, ()
            -> hearingDocumentsService.addDocumentToHearingDocuments(caseData, "token"));
        assertEquals(0, caseData.getBundlesClaimantCollection().size());
        assertEquals(0, caseData.getBundlesRespondentCollection().size());
    }

    @Test
    void addDocumentToHearingDocuments_NoFileUploaded() {
        caseData.setUploadHearingDocumentType(List.of());
        assertThrows(IllegalArgumentException.class,
            () -> hearingDocumentsService.addDocumentToHearingDocuments(caseData, "token"));
    }

    @Test
    void userIdamServiceThrowsException() {
        when(userIdamService.getUserDetails(anyString())).thenThrow(new RuntimeException("User service error"));
        caseData.setUploadHearingDocumentsWhoseDocuments(CLAIMANT_TITLE);
        assertDoesNotThrow(() -> hearingDocumentsService.addDocumentToHearingDocuments(caseData, "token"));
        assertEquals(1, caseData.getBundlesClaimantCollection().size());
        assertEquals("Admin", caseData.getBundlesClaimantCollection().getFirst().getValue().getUploadedBy());
        checkIfFieldsAreCleared(caseData);
    }

    private void checkIfFieldsAreCleared(CaseData caseData) {
        assertNull(caseData.getUploadHearingDocumentsSelectFutureHearing());
        assertNull(caseData.getUploadHearingDocumentsSelectPastHearing());
        assertNull(caseData.getUploadHearingDocumentsSelectPastOrFutureHearing());
        assertNull(caseData.getUploadHearingDocumentsDateSubmitted());
        assertNull(caseData.getUploadHearingDocumentType());
        assertNull(caseData.getUploadHearingDocumentsWhoseDocuments());
    }
}
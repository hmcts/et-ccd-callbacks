package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.et.common.model.listing.ListingCallbackResponse;
import uk.gov.hmcts.et.common.model.listing.ListingData;
import uk.gov.hmcts.et.common.model.listing.ListingDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.ListingService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.ReportDataService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.callback.ListingGenerationCallbackService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GenerateReportCallbackHandlerTest {

    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private ListingService listingService;
    @Mock
    private ReportDataService reportDataService;
    @Mock
    private VerifyTokenService verifyTokenService;
    @Mock
    private ListingGenerationCallbackService listingGenerationCallbackService;

    private GenerateReportCallbackHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GenerateReportCallbackHandler(
            caseDetailsConverter,
            listingService,
            reportDataService,
            verifyTokenService,
            listingGenerationCallbackService
        );
    }

    @Test
    void aboutToSubmitShouldNotGenerateReportWhenTokenInvalid() {
        ListingDetails listingDetails = listingDetails(new ListingData());
        stubListingConverter(listingDetails);
        when(verifyTokenService.verifyTokenSignature(null)).thenReturn(false);

        handler.aboutToSubmit(callbackCaseDetails());

        verify(reportDataService, never()).generateReportData(any(), any());
    }

    @Test
    void submittedShouldDelegateToGenerateHearingDocumentConfirmation() {
        ListingDetails listingDetails = listingDetails(new ListingData());
        stubListingConverter(listingDetails);
        when(caseDetailsConverter.getObjectMapper()).thenReturn(new ObjectMapper());
        when(listingGenerationCallbackService.generateHearingDocumentConfirmation(any(), eq(null)))
            .thenReturn(ResponseEntity.ok(ListingCallbackResponse.builder().build()));

        handler.submitted(callbackCaseDetails());

        verify(listingGenerationCallbackService).generateHearingDocumentConfirmation(any(), eq(null));
    }

    private void stubListingConverter(ListingDetails listingDetails) {
        when(caseDetailsConverter.convert(any(CaseDetails.class), eq(ListingDetails.class)))
            .thenReturn(listingDetails);
    }

    private ListingDetails listingDetails(ListingData listingData) {
        ListingDetails listingDetails = new ListingDetails();
        listingDetails.setCaseData(listingData);
        listingDetails.setCaseTypeId("ET_EnglandWales_Listings");
        listingDetails.setCaseId("123");
        listingDetails.setState("Open");
        return listingDetails;
    }

    private CaseDetails callbackCaseDetails() {
        return CaseDetails.builder()
            .id(123L)
            .caseTypeId("ET_EnglandWales_Listings")
            .state("Open")
            .build();
    }
}

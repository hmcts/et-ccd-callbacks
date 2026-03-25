package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;
import uk.gov.hmcts.et.common.model.listing.ListingCallbackResponse;
import uk.gov.hmcts.et.common.model.listing.ListingData;
import uk.gov.hmcts.et.common.model.listing.ListingDetails;
import uk.gov.hmcts.et.common.model.listing.items.ListingTypeItem;
import uk.gov.hmcts.et.common.model.listing.types.ListingType;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.ListingService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.callback.ListingGenerationCallbackService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PrintCauseListCallbackHandlerTest {

    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private ListingService listingService;
    @Mock
    private VerifyTokenService verifyTokenService;
    @Mock
    private ListingGenerationCallbackService listingGenerationCallbackService;

    private PrintCauseListCallbackHandler handler;

    @BeforeEach
    void setUp() {
        handler = new PrintCauseListCallbackHandler(
            caseDetailsConverter,
            listingService,
            verifyTokenService,
            listingGenerationCallbackService
        );
    }

    @Test
    void aboutToSubmitShouldNotGenerateDocumentWhenTokenInvalid() {
        stubListingConverter(listingDetails(new ListingData()));
        when(verifyTokenService.verifyTokenSignature(null)).thenReturn(false);

        handler.aboutToSubmit(callbackCaseDetails());

        verify(listingService, never()).processHearingDocument(any(), any(), any());
    }

    @Test
    void aboutToSubmitShouldGenerateDocumentWhenTokenValid() {
        ListingData listingData = new ListingData();
        ListingTypeItem item = new ListingTypeItem();
        item.setValue(new ListingType());
        listingData.setListingCollection(List.of(item));
        ListingDetails listingDetails = listingDetails(listingData);
        stubListingConverter(listingDetails);
        when(caseDetailsConverter.getObjectMapper()).thenReturn(new ObjectMapper());
        when(verifyTokenService.verifyTokenSignature(null)).thenReturn(true);
        when(listingService.processHearingDocument(listingData, "ET_EnglandWales_Listings", null))
            .thenReturn(DocumentInfo.builder().markUp("hearing-doc").build());

        handler.aboutToSubmit(callbackCaseDetails());

        verify(listingService).processHearingDocument(listingData, "ET_EnglandWales_Listings", null);
    }

    @Test
    void submittedShouldDelegateToConfirmationService() {
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

package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.et.common.model.listing.ListingData;
import uk.gov.hmcts.et.common.model.listing.ListingDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.ListingService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListingCaseCreationCallbackHandlerTest {

    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private ListingService listingService;
    @Mock
    private VerifyTokenService verifyTokenService;

    private ListingCaseCreationCallbackHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ListingCaseCreationCallbackHandler(caseDetailsConverter, listingService, verifyTokenService);
    }

    @Test
    void aboutToSubmitShouldNotCreateListingsWhenTokenInvalid() {
        stubListingConverter(listingDetails(new ListingData()));
        when(verifyTokenService.verifyTokenSignature(null)).thenReturn(false);

        handler.aboutToSubmit(callbackCaseDetails());

        verify(listingService, never()).listingCaseCreation(any());
    }

    @Test
    void aboutToSubmitShouldCreateListingsWhenTokenValid() {
        ListingDetails listingDetails = listingDetails(new ListingData());
        stubListingConverter(listingDetails);
        when(caseDetailsConverter.getObjectMapper()).thenReturn(new ObjectMapper());
        when(verifyTokenService.verifyTokenSignature(null)).thenReturn(true);
        when(listingService.listingCaseCreation(listingDetails)).thenReturn(new ListingData());

        handler.aboutToSubmit(callbackCaseDetails());

        verify(listingService).listingCaseCreation(listingDetails);
    }

    @Test
    void submittedShouldThrowUnsupportedOperation() {
        assertThatThrownBy(() -> handler.submitted(callbackCaseDetails()))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("does not support submitted callbacks");
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

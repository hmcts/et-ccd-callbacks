package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.items.ReferralTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.ReferralType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.et.common.model.multiples.MultipleDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CloseReferralMultiplesCloseReferralCallbackHandlerTest {

    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    private CloseReferralMultiplesCloseReferralCallbackHandler handler;

    @BeforeEach
    void setUp() {
        handler = new CloseReferralMultiplesCloseReferralCallbackHandler(caseDetailsConverter);
    }

    @Test
    void aboutToSubmitShouldCloseReferralAndClearCloseFields() {
        MultipleData caseData = multipleData();
        MultipleDetails multipleDetails = multipleDetails(caseData, "ET_EnglandWales_Multiple", "123");
        stubMultipleConverter(multipleDetails);
        when(caseDetailsConverter.getObjectMapper()).thenReturn(new ObjectMapper());

        handler.aboutToSubmit(callbackCaseDetails());

        ReferralType updatedReferral = caseData.getReferralCollection().get(0).getValue();
        assertThat(updatedReferral.getReferralStatus()).isEqualTo("Closed");
        assertThat(updatedReferral.getCloseReferralGeneralNotes()).isEqualTo("close-notes");
        assertThat(caseData.getSelectReferral()).isNull();
        assertThat(caseData.getCloseReferralGeneralNotes()).isNull();
        assertThat(caseData.getDocumentCollection()).hasSize(1);
    }

    @Test
    void submittedShouldReturnConfirmationBody() {
        MultipleData caseData = new MultipleData();
        MultipleDetails multipleDetails = multipleDetails(caseData, "ET_EnglandWales_Multiple", "123");
        stubMultipleConverter(multipleDetails);
        when(caseDetailsConverter.getObjectMapper()).thenReturn(new ObjectMapper());

        CCDCallbackResponse response = (CCDCallbackResponse) handler.submitted(callbackCaseDetails());

        assertThat(response.getConfirmationBody()).contains("/cases/case-details/123#Referrals");
    }

    private void stubMultipleConverter(MultipleDetails multipleDetails) {
        when(caseDetailsConverter.convert(any(CaseDetails.class), eq(MultipleDetails.class)))
            .thenReturn(multipleDetails);
    }

    private MultipleData multipleData() {
        MultipleData caseData = new MultipleData();
        caseData.setCloseReferralGeneralNotes("close-notes");

        ReferralType referralType = new ReferralType();
        referralType.setReferralNumber("1");
        referralType.setReferralSubject("Other");
        referralType.setReferralStatus("Awaiting instructions");
        UploadedDocumentType referralSummaryPdf = new UploadedDocumentType();
        referralSummaryPdf.setDocumentFilename("Referral Summary.pdf");
        referralSummaryPdf.setDocumentBinaryUrl("http://dm-store/documents/abc/binary");
        referralSummaryPdf.setDocumentUrl("http://dm-store/documents/abc");
        referralType.setReferralSummaryPdf(referralSummaryPdf);

        ReferralTypeItem referralTypeItem = new ReferralTypeItem();
        referralTypeItem.setId("ref1");
        referralTypeItem.setValue(referralType);
        caseData.setReferralCollection(List.of(referralTypeItem));

        DynamicFixedListType selectReferral = new DynamicFixedListType();
        DynamicValueType dynamicValueType = new DynamicValueType();
        dynamicValueType.setCode("1");
        dynamicValueType.setLabel("1 - Other");
        selectReferral.setValue(dynamicValueType);
        caseData.setSelectReferral(selectReferral);
        return caseData;
    }

    private MultipleDetails multipleDetails(MultipleData caseData, String caseTypeId, String caseId) {
        MultipleDetails multipleDetails = new MultipleDetails();
        multipleDetails.setCaseData(caseData);
        multipleDetails.setCaseTypeId(caseTypeId);
        multipleDetails.setCaseId(caseId);
        multipleDetails.setState("Open");
        return multipleDetails;
    }

    private CaseDetails callbackCaseDetails() {
        return CaseDetails.builder()
            .id(123L)
            .caseTypeId("ET_EnglandWales_Multiple")
            .state("Open")
            .build();
    }
}

package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.ReferralTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.ReferralType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CloseReferralCloseReferralCallbackHandlerTest {

    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    private CloseReferralCloseReferralCallbackHandler handler;

    @BeforeEach
    void setUp() {
        handler = new CloseReferralCloseReferralCallbackHandler(caseDetailsConverter);
    }

    @Test
    void aboutToSubmitShouldCloseReferralAndClearCloseFields() {
        CaseData caseData = caseData();
        stubConverter(caseData, "123");

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
        stubConverter(caseData(), "123");

        CCDCallbackResponse response = (CCDCallbackResponse) handler.submitted(callbackCaseDetails());

        assertThat(response.getConfirmationBody()).contains("/cases/case-details/123#Referrals");
    }

    private void stubConverter(CaseData caseData, String caseId) {
        uk.gov.hmcts.et.common.model.ccd.CaseDetails ccdCaseDetails =
            new uk.gov.hmcts.et.common.model.ccd.CaseDetails();
        ccdCaseDetails.setCaseData(caseData);
        ccdCaseDetails.setCaseTypeId("ET_EnglandWales");
        ccdCaseDetails.setState("Open");
        ccdCaseDetails.setCaseId(caseId);
        when(caseDetailsConverter.convert(any(CaseDetails.class))).thenReturn(ccdCaseDetails);
    }

    private CaseData caseData() {
        CaseData caseData = new CaseData();
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

    private CaseDetails callbackCaseDetails() {
        return CaseDetails.builder()
            .id(123L)
            .caseTypeId("ET_EnglandWales")
            .state("Open")
            .build();
    }
}

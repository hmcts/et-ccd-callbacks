package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;
import uk.gov.hmcts.et.common.model.ccd.items.ReferralTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.ReferralType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.DocumentManagementService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.ReferralService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.UserIdamService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateReferralUpdateReferralCallbackHandlerTest {

    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private UserIdamService userIdamService;
    @Mock
    private ReferralService referralService;
    @Mock
    private DocumentManagementService documentManagementService;

    private UpdateReferralUpdateReferralCallbackHandler handler;

    @BeforeEach
    void setUp() {
        handler = new UpdateReferralUpdateReferralCallbackHandler(
            caseDetailsConverter,
            userIdamService,
            referralService,
            documentManagementService
        );
    }

    @Test
    void aboutToSubmitShouldUpdateReferralAndClearTemporaryFields() {
        CaseData caseData = caseData();
        stubConverter(caseData, "ET_EnglandWales", "123");

        UserDetails userDetails = new UserDetails();
        userDetails.setFirstName("Judge");
        userDetails.setLastName("Person");
        when(userIdamService.getUserDetails(null)).thenReturn(userDetails);

        DocumentInfo documentInfo = DocumentInfo.builder()
            .description("Referral Summary")
            .url("http://dm-store/documents/123/binary")
            .build();
        when(referralService.generateCRDocument(caseData, null, "ET_EnglandWales")).thenReturn(documentInfo);

        UploadedDocumentType uploadedDocumentType = new UploadedDocumentType();
        uploadedDocumentType.setDocumentFilename("Referral Summary");
        when(documentManagementService.addDocumentToDocumentField(documentInfo)).thenReturn(uploadedDocumentType);

        handler.aboutToSubmit(callbackCaseDetails());

        ReferralType updatedReferral = caseData.getReferralCollection().get(0).getValue();
        assertThat(updatedReferral.getUpdateReferralCollection()).hasSize(1);
        assertThat(updatedReferral.getUpdateReferralCollection().get(0).getValue().getUpdateReferralSubject())
            .isEqualTo("Rule 22");
        assertThat(updatedReferral.getReferralSummaryPdf()).isEqualTo(uploadedDocumentType);
        assertThat(caseData.getSelectReferral()).isNull();
        assertThat(caseData.getUpdateReferralSubject()).isNull();

        verify(userIdamService).getUserDetails(null);
        verify(referralService).generateCRDocument(caseData, null, "ET_EnglandWales");
        verify(documentManagementService).addDocumentToDocumentField(documentInfo);
    }

    @Test
    void submittedShouldThrowWhenCallbackTypeUnsupported() {
        assertThatThrownBy(() -> handler.submitted(callbackCaseDetails()))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("does not support submitted callbacks");
    }

    private void stubConverter(CaseData caseData, String caseTypeId, String caseId) {
        uk.gov.hmcts.et.common.model.ccd.CaseDetails ccdCaseDetails =
            new uk.gov.hmcts.et.common.model.ccd.CaseDetails();
        ccdCaseDetails.setCaseData(caseData);
        ccdCaseDetails.setCaseTypeId(caseTypeId);
        ccdCaseDetails.setCaseId(caseId);
        ccdCaseDetails.setState("Open");
        when(caseDetailsConverter.convert(any(CaseDetails.class))).thenReturn(ccdCaseDetails);
    }

    private CaseData caseData() {
        CaseData caseData = new CaseData();
        caseData.setUpdateReferCaseTo("Judge");
        caseData.setUpdateIsUrgent("No");
        caseData.setUpdateReferralSubject("Rule 21");
        caseData.setUpdateReferralDetails("Referral details");
        caseData.setUpdateReferralInstruction("Instruction");
        caseData.setUpdateReferentEmail("judge@email.com");

        ReferralType referralType = new ReferralType();
        referralType.setReferralNumber("1");
        referralType.setReferralStatus("Awaiting instructions");
        referralType.setReferralSubject("Other");
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

package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.et.common.model.multiples.MultipleDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseLookupService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.DocumentManagementService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.ReferralService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.UserIdamService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateReferralMultiplesUpdateReferralCallbackHandlerTest {

    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private UserIdamService userIdamService;
    @Mock
    private ReferralService referralService;
    @Mock
    private DocumentManagementService documentManagementService;
    @Mock
    private CaseLookupService caseLookupService;

    private UpdateReferralMultiplesUpdateReferralCallbackHandler handler;

    @BeforeEach
    void setUp() {
        handler = new UpdateReferralMultiplesUpdateReferralCallbackHandler(
            caseDetailsConverter,
            userIdamService,
            referralService,
            documentManagementService,
            caseLookupService
        );
    }

    @Test
    void aboutToSubmitShouldUpdateMultipleReferralAndClearTemporaryFields() throws IOException {
        MultipleData caseData = multipleData();
        MultipleDetails multipleDetails = multipleDetails(caseData, "ET_EnglandWales_Multiple", "123");
        stubMultipleConverter(multipleDetails);
        when(caseDetailsConverter.getObjectMapper()).thenReturn(new ObjectMapper());

        UserDetails userDetails = new UserDetails();
        userDetails.setFirstName("Judge");
        userDetails.setLastName("Person");
        when(userIdamService.getUserDetails(null)).thenReturn(userDetails);

        CaseData leadCase = new CaseData();
        when(caseLookupService.getLeadCaseFromMultipleAsAdmin(multipleDetails)).thenReturn(leadCase);

        DocumentInfo documentInfo = DocumentInfo.builder()
            .description("Referral Summary")
            .url("http://dm-store/documents/123/binary")
            .build();
        when(referralService.generateDocument(caseData, leadCase, null, "ET_EnglandWales_Multiple"))
            .thenReturn(documentInfo);

        UploadedDocumentType uploadedDocumentType = new UploadedDocumentType();
        uploadedDocumentType.setDocumentFilename("Referral Summary");
        when(documentManagementService.addDocumentToDocumentField(documentInfo)).thenReturn(uploadedDocumentType);

        handler.aboutToSubmit(callbackCaseDetails());

        ReferralType updatedReferral = caseData.getReferralCollection().get(0).getValue();
        assertThat(updatedReferral.getUpdateReferralCollection()).hasSize(1);
        assertThat(updatedReferral.getReferralSummaryPdf()).isEqualTo(uploadedDocumentType);
        assertThat(caseData.getSelectReferral()).isNull();
        assertThat(caseData.getUpdateReferralSubject()).isNull();

        verify(userIdamService).getUserDetails(null);
        verify(caseLookupService).getLeadCaseFromMultipleAsAdmin(multipleDetails);
        verify(referralService).generateDocument(caseData, leadCase, null, "ET_EnglandWales_Multiple");
        verify(documentManagementService).addDocumentToDocumentField(documentInfo);
    }

    @Test
    void submittedShouldThrowWhenCallbackTypeUnsupported() {
        assertThatThrownBy(() -> handler.submitted(callbackCaseDetails()))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("does not support submitted callbacks");
    }

    private void stubMultipleConverter(MultipleDetails multipleDetails) {
        when(caseDetailsConverter.convert(any(CaseDetails.class), eq(MultipleDetails.class)))
            .thenReturn(multipleDetails);
    }

    private MultipleData multipleData() {
        MultipleData caseData = new MultipleData();
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

package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseManagementForCaseWorkerService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.DocumentManagementService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.EmailService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.ReferralService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.UserIdamService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateReferralCreateReferralCallbackHandlerTest {

    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private CaseManagementForCaseWorkerService caseManagementForCaseWorkerService;
    @Mock
    private ReferralService referralService;
    @Mock
    private UserIdamService userIdamService;
    @Mock
    private DocumentManagementService documentManagementService;
    @Mock
    private EmailService emailService;

    private CreateReferralCreateReferralCallbackHandler handler;

    @BeforeEach
    void setUp() {
        handler = new CreateReferralCreateReferralCallbackHandler(
            caseDetailsConverter,
            caseManagementForCaseWorkerService,
            referralService,
            userIdamService,
            documentManagementService,
            emailService,
            "template-id"
        );
    }

    @Test
    void aboutToSubmitShouldCreateReferralAndSetNextListedDate() {
        CaseData caseData = new CaseData();
        caseData.setReferralSubject("Case management");
        caseData.setReferentEmail("");
        stubConverter(caseData);

        UserDetails userDetails = new UserDetails();
        userDetails.setFirstName("Judge");
        userDetails.setLastName("Smith");
        userDetails.setName("Judge Smith");
        when(userIdamService.getUserDetails(null)).thenReturn(userDetails);

        when(referralService.generateCRDocument(caseData, null, "ET_EnglandWales"))
            .thenReturn(DocumentInfo.builder().build());
        when(documentManagementService.addDocumentToDocumentField(any()))
            .thenReturn(new UploadedDocumentType());

        handler.aboutToSubmit(callbackCaseDetails());

        assertThat(caseData.getReferralCollection()).hasSize(1);
        assertThat(caseData.getReferralCollection().getFirst().getValue().getReferredBy()).isEqualTo("Judge Smith");
        verify(caseManagementForCaseWorkerService).setNextListedDate(caseData);
    }

    @Test
    void submittedShouldReturnReferralConfirmationBody() {
        CaseData caseData = new CaseData();
        stubConverter(caseData);

        CCDCallbackResponse response = (CCDCallbackResponse) handler.submitted(callbackCaseDetails());

        assertThat(response.getConfirmationBody()).contains("/cases/case-details/123#Referrals");
    }

    private void stubConverter(CaseData caseData) {
        uk.gov.hmcts.et.common.model.ccd.CaseDetails ccdCaseDetails =
            new uk.gov.hmcts.et.common.model.ccd.CaseDetails();
        ccdCaseDetails.setCaseData(caseData);
        ccdCaseDetails.setCaseTypeId("ET_EnglandWales");
        ccdCaseDetails.setCaseId("123");
        when(caseDetailsConverter.convert(any(CaseDetails.class))).thenReturn(ccdCaseDetails);
    }

    private CaseDetails callbackCaseDetails() {
        return CaseDetails.builder()
            .id(123L)
            .caseTypeId("ET_EnglandWales")
            .state("Open")
            .build();
    }
}

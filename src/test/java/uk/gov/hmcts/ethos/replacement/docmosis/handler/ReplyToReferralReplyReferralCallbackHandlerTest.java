package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;
import uk.gov.hmcts.et.common.model.ccd.items.ReferralTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.ReferralType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseManagementForCaseWorkerService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.DocumentManagementService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.EmailService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.FeatureToggleService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.ReferralService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.UserIdamService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReplyToReferralReplyReferralCallbackHandlerTest {

    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private CaseManagementForCaseWorkerService caseManagementForCaseWorkerService;
    @Mock
    private UserIdamService userIdamService;
    @Mock
    private ReferralService referralService;
    @Mock
    private DocumentManagementService documentManagementService;
    @Mock
    private EmailService emailService;
    @Mock
    private FeatureToggleService featureToggleService;

    private ReplyToReferralReplyReferralCallbackHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ReplyToReferralReplyReferralCallbackHandler(
            caseDetailsConverter,
            caseManagementForCaseWorkerService,
            userIdamService,
            referralService,
            documentManagementService,
            emailService,
            featureToggleService,
            "template-id"
        );
    }

    @Test
    void aboutToSubmitShouldCreateReferralReplyAndSetNextListedDate() {
        CaseData caseData = caseDataWithSelectedReferral();
        caseData.setReplyToEmailAddress("");
        stubConverter(caseData);

        UserDetails userDetails = new UserDetails();
        userDetails.setFirstName("Admin");
        userDetails.setLastName("User");
        userDetails.setName("Admin User");
        when(userIdamService.getUserDetails(null)).thenReturn(userDetails);
        when(featureToggleService.isWorkAllocationEnabled()).thenReturn(false);
        when(referralService.generateCRDocument(caseData, null, "ET_EnglandWales"))
            .thenReturn(DocumentInfo.builder().build());
        when(documentManagementService.addDocumentToDocumentField(any()))
            .thenReturn(new UploadedDocumentType());

        handler.aboutToSubmit(callbackCaseDetails());

        verify(caseManagementForCaseWorkerService).setNextListedDate(caseData);
        assertThat(caseData.getReferralCollection().getFirst().getValue().getReferralSummaryPdf()).isNotNull();
    }

    @Test
    void submittedShouldReturnReplyConfirmationBody() {
        final CaseData caseData = caseDataWithSelectedReferral();
        stubConverter(caseData);

        CCDCallbackResponse response = (CCDCallbackResponse) handler.submitted(callbackCaseDetails());

        assertThat(response.getConfirmationBody()).contains("/cases/case-details/123#Referrals");
    }

    private CaseData caseDataWithSelectedReferral() {
        ReferralType referralType = new ReferralType();
        referralType.setReferralNumber("1");
        ReferralTypeItem referralTypeItem = new ReferralTypeItem();
        referralTypeItem.setId("ref-1");
        referralTypeItem.setValue(referralType);
        List<ReferralTypeItem> referrals = List.of(referralTypeItem);

        CaseData caseData = new CaseData();
        caseData.setReferralCollection(referrals);

        DynamicValueType selectedReferral = new DynamicValueType();
        selectedReferral.setCode("1");
        selectedReferral.setLabel("Referral 1");
        DynamicFixedListType selectedReferralList = new DynamicFixedListType();
        selectedReferralList.setValue(selectedReferral);
        caseData.setSelectReferral(selectedReferralList);

        return caseData;
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

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
import uk.gov.hmcts.ethos.replacement.docmosis.service.FeatureToggleService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.ReferralService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.UserIdamService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReplyToReferralMultiplesReplyReferralCallbackHandlerTest {

    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private UserIdamService userIdamService;
    @Mock
    private ReferralService referralService;
    @Mock
    private DocumentManagementService documentManagementService;
    @Mock
    private FeatureToggleService featureToggleService;
    @Mock
    private CaseLookupService caseLookupService;

    private ReplyToReferralMultiplesReplyReferralCallbackHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ReplyToReferralMultiplesReplyReferralCallbackHandler(
            caseDetailsConverter,
            userIdamService,
            referralService,
            documentManagementService,
            featureToggleService,
            caseLookupService
        );
        lenient().when(caseDetailsConverter.getObjectMapper()).thenReturn(new ObjectMapper());
    }

    @Test
    void aboutToSubmitShouldCreateReferralReplyForMultipleAndSendEmail() throws IOException {
        MultipleData multipleData = multipleDataWithSelectedReferral();
        final MultipleDetails multipleDetails = stubMultipleConverter(multipleData, "999");

        UserDetails userDetails = new UserDetails();
        userDetails.setFirstName("Admin");
        userDetails.setLastName("User");
        when(userIdamService.getUserDetails(null)).thenReturn(userDetails);
        when(featureToggleService.isWorkAllocationEnabled()).thenReturn(false);

        CaseData leadCase = new CaseData();
        leadCase.setEthosCaseReference("11111/2026");
        when(caseLookupService.getLeadCaseFromMultipleAsAdmin(multipleDetails)).thenReturn(leadCase);
        when(referralService.generateDocument(multipleData, leadCase, null, "ET_EnglandWales_Multiple"))
            .thenReturn(DocumentInfo.builder().build());
        when(documentManagementService.addDocumentToDocumentField(any()))
            .thenReturn(new UploadedDocumentType());

        handler.aboutToSubmit(callbackCaseDetails());

        verify(referralService).sendEmail(multipleDetails, leadCase, "1", false, "Admin User");
        assertThat(multipleData.getReferralCollection().getFirst().getValue().getReferralSummaryPdf()).isNotNull();
    }

    @Test
    void aboutToSubmitShouldWrapIOException() throws IOException {
        MultipleData multipleData = multipleDataWithSelectedReferral();
        final MultipleDetails multipleDetails = stubMultipleConverter(multipleData, "999");

        UserDetails userDetails = new UserDetails();
        userDetails.setFirstName("Admin");
        userDetails.setLastName("User");
        when(userIdamService.getUserDetails(null)).thenReturn(userDetails);
        when(featureToggleService.isWorkAllocationEnabled()).thenReturn(true);

        when(caseLookupService.getLeadCaseFromMultipleAsAdmin(multipleDetails))
            .thenThrow(new IOException("lookup failed"));

        assertThatThrownBy(() -> handler.aboutToSubmit(callbackCaseDetails()))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Failed to reply to referral for multiple")
            .hasCauseInstanceOf(IOException.class);
    }

    @Test
    void submittedShouldReturnReplyConfirmationBody() {
        final MultipleData multipleData = multipleDataWithSelectedReferral();
        stubMultipleConverter(multipleData, "999");

        uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse response =
            (uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse) handler.submitted(callbackCaseDetails());

        assertThat(response.getConfirmationBody()).contains("/cases/case-details/999#Referrals");
    }

    private MultipleData multipleDataWithSelectedReferral() {
        ReferralType referralType = new ReferralType();
        referralType.setReferralNumber("1");
        ReferralTypeItem referralTypeItem = new ReferralTypeItem();
        referralTypeItem.setId("ref-1");
        referralTypeItem.setValue(referralType);
        List<ReferralTypeItem> referrals = List.of(referralTypeItem);

        MultipleData multipleData = new MultipleData();
        multipleData.setReferralCollection(referrals);

        DynamicValueType selectedReferral = new DynamicValueType();
        selectedReferral.setCode("1");
        selectedReferral.setLabel("Referral 1");
        DynamicFixedListType selectedReferralList = new DynamicFixedListType();
        selectedReferralList.setValue(selectedReferral);
        multipleData.setSelectReferral(selectedReferralList);

        return multipleData;
    }

    private MultipleDetails stubMultipleConverter(MultipleData multipleData, String caseId) {
        MultipleDetails multipleDetails = new MultipleDetails();
        multipleDetails.setCaseData(multipleData);
        multipleDetails.setCaseTypeId("ET_EnglandWales_Multiple");
        multipleDetails.setCaseId(caseId);
        when(caseDetailsConverter.convert(any(CaseDetails.class), eq(MultipleDetails.class))).thenReturn(
            multipleDetails);
        return multipleDetails;
    }

    private CaseDetails callbackCaseDetails() {
        return CaseDetails.builder()
            .id(999L)
            .caseTypeId("ET_EnglandWales_Multiple")
            .state("Open")
            .build();
    }
}

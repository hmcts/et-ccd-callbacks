package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.Et3NotificationService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class Et3NotificationCallbackHandlerTest {

    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private Et3NotificationService et3NotificationService;

    private Et3NotificationCallbackHandler handler;

    @BeforeEach
    void setUp() {
        handler = new Et3NotificationCallbackHandler(caseDetailsConverter, et3NotificationService);
    }

    @Test
    void aboutToSubmitShouldProcessNotificationDocuments() {
        CaseData caseData = new CaseData();
        caseData.setRespondentCollection(new ArrayList<>());
        stubConverter(caseData);

        var response = handler.aboutToSubmit(callbackCaseDetails());

        assertThat(response.getErrors()).isNotNull();
    }

    @Test
    void submittedShouldSendNotificationsAndReturnConfirmationHeader() {
        CaseData caseData = new CaseData();
        caseData.setRespondentCollection(new ArrayList<>());
        uk.gov.hmcts.et.common.model.ccd.CaseDetails ccdCaseDetails = stubConverter(caseData);

        var response = handler.submitted(callbackCaseDetails());

        verify(et3NotificationService).sendNotifications(ccdCaseDetails);
        assertThat(response.getConfirmationHeader()).contains("Documents submitted");
    }

    private uk.gov.hmcts.et.common.model.ccd.CaseDetails stubConverter(CaseData caseData) {
        uk.gov.hmcts.et.common.model.ccd.CaseDetails ccdCaseDetails =
            new uk.gov.hmcts.et.common.model.ccd.CaseDetails();
        ccdCaseDetails.setCaseData(caseData);
        ccdCaseDetails.setCaseTypeId("ET_EnglandWales");
        ccdCaseDetails.setCaseId("123");
        when(caseDetailsConverter.convert(any(CaseDetails.class))).thenReturn(ccdCaseDetails);
        return ccdCaseDetails;
    }

    private CaseDetails callbackCaseDetails() {
        return CaseDetails.builder()
            .id(123L)
            .caseTypeId("ET_EnglandWales")
            .state("Open")
            .build();
    }
}

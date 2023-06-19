package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.config.NotificationProperties;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.HelperTest;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.TseHelper;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import java.io.IOException;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@ExtendWith(SpringExtension.class)
class TseRespondentReplyServiceTest {
    private TseRespondentReplyService tseRespondentReplyService;
    @MockBean
    private TornadoService tornadoService;
    @MockBean
    private EmailService emailService;
    @MockBean
    private UserService userService;
    @MockBean
    private NotificationProperties notificationProperties;
    @MockBean
    private TseService tseService;
    private UserDetails userDetails;
    private CaseData caseData;
    private MockedStatic mockStatic;

    @BeforeEach
    void setUp() throws Exception {
        tseRespondentReplyService = new TseRespondentReplyService(tornadoService, emailService, userService,
                notificationProperties, tseService);
        userDetails = HelperTest.getUserDetails();
        when(userService.getUserDetails(anyString())).thenReturn(userDetails);
        when(tornadoService.generateEventDocumentBytes(any(), any(), any())).thenReturn(new byte[]{});
        doNothing().when(emailService).sendEmail(any(), any(), any());

        mockStatic = mockStatic(TseHelper.class);
        mockStatic.when(() -> TseHelper.getPersonalisationForResponse(any(), any(), any()))
                .thenReturn(Collections.emptyMap());
        mockStatic.when(() -> TseHelper.getPersonalisationForAcknowledgement(any(), any()))
                .thenReturn(Collections.emptyMap());

        caseData = CaseDataBuilder.builder()
            .withEthosCaseReference("9876")
            .withClaimantType("person@email.com")
            .withRespondent("respondent", YES, "01-Jan-2003", false)
            .build();

        caseData.setClaimant("Claimant LastName");
    }

    @AfterEach
    void afterEach() {
        mockStatic.close();
    }

    @Test
    void sendAcknowledgementAndClaimantEmail_rule92Yes() throws IOException {
        caseData.setTseResponseCopyToOtherParty(YES);

        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseId("caseId");
        caseDetails.setCaseData(caseData);

        tseRespondentReplyService.sendAcknowledgementAndClaimantEmail(caseDetails, "userToken");

        verify(emailService, times(2)).sendEmail(any(), any(), any());
        verify(emailService).sendEmail(any(), eq(caseData.getClaimantType().getClaimantEmailAddress()), any());
        verify(emailService).sendEmail(any(), eq(userDetails.getEmail()), any());
    }

    @Test
    void sendAcknowledgementAndClaimantEmail_rule92No() throws IOException {
        caseData.setTseResponseCopyToOtherParty(NO);

        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseId("caseId");
        caseDetails.setCaseData(caseData);

        tseRespondentReplyService.sendAcknowledgementAndClaimantEmail(caseDetails, "userToken");

        verify(emailService, times(1)).sendEmail(any(), any(), any());
        verify(emailService).sendEmail(any(), eq(userDetails.getEmail()), any());
    }
}

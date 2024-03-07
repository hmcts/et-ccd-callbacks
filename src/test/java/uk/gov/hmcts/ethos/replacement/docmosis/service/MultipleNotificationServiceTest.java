package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.et.common.model.multiples.MultipleDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.MultipleUtil;
import uk.gov.hmcts.ethos.replacement.docmosis.servicebus.CreateUpdatesBusSender;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class MultipleNotificationServiceTest {

    private static final String EMAIL = "email@email.com";

    @MockBean
    CreateUpdatesBusSender createUpdatesBusSender;

    @MockBean
    UserIdamService userIdamService;

    private MultiplesSendNotificationService multiplesSendNotificationService;
    private MultipleDetails multipleDetails;
    private String userToken;

    private List<String> errors;

    @BeforeEach
    public void setUp() {
        multiplesSendNotificationService =
                new MultiplesSendNotificationService(createUpdatesBusSender, userIdamService);
        multipleDetails = new MultipleDetails();
        multipleDetails.setCaseData(MultipleUtil.getMultipleDataForNotification());
        userToken = "authString";
        errors = new ArrayList<>();
        UserDetails user = new UserDetails();
        user.setEmail(EMAIL);
        when(userIdamService.getUserDetails(userToken)).thenReturn(user);
    }

    @Test
    void verifyNotificationIsSentOnceForLeadCase() {
        multiplesSendNotificationService.sendNotificationToSingles(
                multipleDetails.getCaseData(),
                multipleDetails,
                userToken,
                errors
        );

        verify(createUpdatesBusSender, times(1))
                .sendUpdatesToQueue(any(), any(), any(), eq("1"));

    }

    @Test
    void verifyNotificationIsNotSent() {
        MultipleData multipleData = multipleDetails.getCaseData();
        multipleData.setSendNotificationNotify("Both Parties");
        multiplesSendNotificationService.sendNotificationToSingles(
                multipleDetails.getCaseData(),
                multipleDetails,
                userToken,
                errors
        );

        verify(createUpdatesBusSender, times(0))
                .sendUpdatesToQueue(any(), any(), any(), any());

    }

    @Test
    void verifySendNotificationFieldsAreCleared() {
        MultipleData multipleData = multipleDetails.getCaseData();
        multiplesSendNotificationService.clearSendNotificationFields(multipleData);

        assertNull(multipleData.getSendNotificationTitle());
        assertNull(multipleData.getSendNotificationLetter());
        assertNull(multipleData.getSendNotificationUploadDocument());
        assertNull(multipleData.getSendNotificationSubject());
        assertNull(multipleData.getSendNotificationAdditionalInfo());
        assertNull(multipleData.getSendNotificationNotify());
        assertNull(multipleData.getSendNotificationSelectHearing());
        assertNull(multipleData.getSendNotificationCaseManagement());
        assertNull(multipleData.getSendNotificationResponseTribunal());
        assertNull(multipleData.getSendNotificationWhoCaseOrder());
        assertNull(multipleData.getSendNotificationSelectParties());
        assertNull(multipleData.getSendNotificationFullName());
        assertNull(multipleData.getSendNotificationFullName2());
        assertNull(multipleData.getSendNotificationDecision());
        assertNull(multipleData.getSendNotificationDetails());
        assertNull(multipleData.getSendNotificationRequestMadeBy());
        assertNull(multipleData.getSendNotificationEccQuestion());
        assertNull(multipleData.getSendNotificationWhoCaseOrder());
        assertNull(multipleData.getSendNotificationNotifyLeadCase());
    }
}

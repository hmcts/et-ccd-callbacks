package uk.gov.hmcts.ethos.replacement.docmosis.service.multiples;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.et.common.model.multiples.MultipleDetails;
import uk.gov.hmcts.et.common.model.multiples.MultipleObject;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.FilterExcelType;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.MultipleUtil;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseLookupService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.FileLocationSelectionService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.ScotlandFileLocationSelectionService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.UserIdamService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.excel.ExcelReadingService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.excel.MultipleDynamicListFlagsService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.hearings.HearingSelectionService;
import uk.gov.hmcts.ethos.replacement.docmosis.servicebus.CreateUpdatesBusSender;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.CaseDetailsGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_BULK_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SEND_NOTIFICATION_ALL;

@ExtendWith(SpringExtension.class)
class MultipleNotificationServiceTest {

    private static final String EMAIL = "email@email.com";

    @MockBean
    CreateUpdatesBusSender createUpdatesBusSender;

    @MockBean
    UserIdamService userIdamService;

    @MockBean
    ExcelReadingService excelReadingService;

    @MockBean
    CaseLookupService caseLookupService;

    @MockBean
    MultipleDynamicListFlagsService multipleDynamicListFlagsService;
    @MockBean
    FileLocationSelectionService fileLocationSelectionService;
    @MockBean
    ScotlandFileLocationSelectionService scotlandFileLocationSelectionService;

    private HearingSelectionService hearingSelectionService;

    private MultiplesSendNotificationService multiplesSendNotificationService;
    private MultipleDetails multipleDetails;
    private String userToken;

    private List<String> errors;

    @BeforeEach
    public void setUp() {
        hearingSelectionService = new HearingSelectionService();
        multiplesSendNotificationService =
                new MultiplesSendNotificationService(createUpdatesBusSender,
                        userIdamService,
                        excelReadingService,
                        caseLookupService,
                        hearingSelectionService,
                        multipleDynamicListFlagsService,
                        fileLocationSelectionService,
                        scotlandFileLocationSelectionService
                );
        multipleDetails = new MultipleDetails();
        multipleDetails.setCaseTypeId(ENGLANDWALES_BULK_CASE_TYPE_ID);
        multipleDetails.setCaseData(MultipleUtil.getMultipleDataForNotification());
        userToken = "authString";
        errors = new ArrayList<>();
        UserDetails user = new UserDetails();
        user.setEmail(EMAIL);
        when(userIdamService.getUserDetails(userToken)).thenReturn(user);
    }

    @Test
    void verifyMultipleDataIsSetWithHearingsFromLead() throws Exception {
        var caseDetails = CaseDetailsGenerator.generateCaseDetails("caseDetailsTest21.json");
        hearingSelectionService = new HearingSelectionService();
        List<String> errors = new ArrayList<>();
        when(caseLookupService.getCaseDataAsAdmin(any(), any())).thenReturn(caseDetails.getCaseData());

        multiplesSendNotificationService.setHearingDetailsFromLeadCase(multipleDetails, errors);

        int hearingsSize = multipleDetails.getCaseData().getSendNotificationSelectHearing().getListItems().size();
        assertEquals(9, hearingsSize);
        assertEquals(0, errors.size());
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
    void verifyNotificationIsSentOnceForAllCases() {
        SortedMap<String, Object> sortedMap = new TreeMap<>();
        MultipleObject multipleObject1 = MultipleObject.builder().ethosCaseRef("6000001/2024").build();
        MultipleObject multipleObject2 = MultipleObject.builder().ethosCaseRef("6000001/2023").build();
        sortedMap.put("A", multipleObject1);
        sortedMap.put("B", multipleObject2);
        var caseData = multipleDetails.getCaseData();
        caseData.setSendNotificationNotify(SEND_NOTIFICATION_ALL);
        when(excelReadingService.readExcel(any(), any(), any(), any(), any())).thenReturn(sortedMap);

        multiplesSendNotificationService.sendNotificationToSingles(
                multipleDetails.getCaseData(),
                multipleDetails,
                userToken,
                errors
        );

        verify(createUpdatesBusSender, times(1))
                .sendUpdatesToQueue(any(), any(), any(), eq("2"));

    }

    @Test
    void verifyNotificationIsSentUsingBulk() {
        SortedMap<String, Object> sortedMap = new TreeMap<>();
        MultipleObject multipleObject1 = MultipleObject.builder().ethosCaseRef("6000001/2024").build();
        sortedMap.put("A", multipleObject1);
        var caseData = multipleDetails.getCaseData();
        caseData.setSendNotificationNotify("Selected cases");
        when(excelReadingService.readExcel(any(), any(), any(), any(), eq(FilterExcelType.FLAGS)))
                .thenReturn(sortedMap);

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

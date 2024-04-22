package uk.gov.hmcts.ethos.replacement.docmosis.service.multiples;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.model.servicebus.CreateUpdatesDto;
import uk.gov.hmcts.ecm.common.model.servicebus.datamodel.DataModelParent;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationTypeMultiple;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.et.common.model.multiples.MultipleDetails;
import uk.gov.hmcts.et.common.model.multiples.MultipleObject;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.FilterExcelType;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.MultiplesHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.PersistentQHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseLookupService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.FileLocationSelectionService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.ScotlandFileLocationSelectionService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.UserIdamService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.excel.ExcelReadingService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.excel.MultipleDynamicListFlagsService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.hearings.HearingSelectionService;
import uk.gov.hmcts.ethos.replacement.docmosis.servicebus.CreateUpdatesBusSender;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_BULK_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.MULTIPLE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_BULK_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SEND_NOTIFICATION_ALL;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SEND_NOTIFICATION_LEAD;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SEND_NOTIFICATION_SELECTED;

@Slf4j
@RequiredArgsConstructor
@Service("multiplesSendNotificationService")
public class MultiplesSendNotificationService {
    private static final String NO_CASES_FILTERED = "No cases filtered";
    private static final String INVALID_CASE_TYPE = "Invalid case type";
    private final CreateUpdatesBusSender createUpdatesBusSender;
    private final UserIdamService userIdamService;
    private final ExcelReadingService excelReadingService;
    private final CaseLookupService caseLookupService;
    private final HearingSelectionService hearingSelectionService;
    private final MultipleDynamicListFlagsService multipleDynamicListFlagsService;
    private final FileLocationSelectionService fileLocationSelectionService;
    private final ScotlandFileLocationSelectionService scotlandFileLocationSelectionService;

    public void setHearingDetailsFromLeadCase(MultipleDetails multipleDetails, List<String> errors) {
        try {
            MultipleData multipleData = multipleDetails.getCaseData();
            CaseData leadCaseData = caseLookupService.getCaseDataAsAdmin(
                    multipleDetails.getCaseTypeId().replace(MULTIPLE, ""),
                    multipleData.getLeadCaseId());
            DynamicFixedListType dynamicFixedListType = new DynamicFixedListType();
            List<DynamicValueType> hearings = hearingSelectionService.getHearingSelectionSortedByDateTime(leadCaseData);
            dynamicFixedListType.setListItems(hearings);
            multipleData.setSendNotificationSelectHearing(dynamicFixedListType);
        } catch (Exception e) {
            log.error(e.toString());
            errors.add("Failed to retrieve hearing details from lead case");
        }
    }

    public void setMultipleWithExcelFileData(MultipleDetails multipleDetails, String userToken, List<String> errors) {
        multipleDynamicListFlagsService.populateDynamicListFlagsLogic(userToken, multipleDetails, errors);
        if (CollectionUtils.isNotEmpty(errors)) {
            return;
        }
        String caseTypeId = multipleDetails.getCaseTypeId();
        if (ENGLANDWALES_BULK_CASE_TYPE_ID.equals(caseTypeId)) {
            fileLocationSelectionService.initialiseFileLocation(multipleDetails.getCaseData());
        } else if (SCOTLAND_BULK_CASE_TYPE_ID.equals(caseTypeId)) {
            scotlandFileLocationSelectionService.initialiseFileLocation(multipleDetails.getCaseData());
        } else {
            log.error(INVALID_CASE_TYPE);
            errors.add(INVALID_CASE_TYPE);
        }
    }

    public void sendNotificationToSingles(MultipleData multipleData,
                                          MultipleDetails caseDetails,
                                          String userToken,
                                          List<String> errors) {

        List<String> ethosCaseRefCollection = new ArrayList<>();
        if (SEND_NOTIFICATION_LEAD.equals(multipleData.getSendNotificationNotify())) {
            ethosCaseRefCollection.add(multipleData.getLeadEthosCaseRef());
        } else if (SEND_NOTIFICATION_ALL.equals(multipleData.getSendNotificationNotify())) {
            SortedMap<String, Object> multipleObjects = excelReadingService.readExcel(
                    userToken, MultiplesHelper.getExcelBinaryUrl(caseDetails.getCaseData()),
                    errors, caseDetails.getCaseData(), FilterExcelType.ALL
            );
            setCaseRefCollection(multipleObjects, ethosCaseRefCollection);
        } else if (SEND_NOTIFICATION_SELECTED.equals(multipleData.getSendNotificationNotify())) {
            SortedMap<String, Object> multipleObjects =
                    excelReadingService.readExcel(
                            userToken,
                            MultiplesHelper.getExcelBinaryUrl(multipleData),
                            errors,
                            multipleData,
                            FilterExcelType.FLAGS
                    );
            ethosCaseRefCollection = new ArrayList<>(multipleObjects.keySet());
        }

        if (ethosCaseRefCollection.isEmpty()) {
            log.error(NO_CASES_FILTERED);
            errors.add(NO_CASES_FILTERED);
            return;
        }
        sendNotificationToQueue(multipleData, caseDetails, userToken, errors, ethosCaseRefCollection);
    }

    private void sendNotificationToQueue(MultipleData multipleData,
                                         MultipleDetails caseDetails,
                                         String userToken,
                                         List<String> errors,
                                         List<String> ethosCaseRefCollection) {

        CreateUpdatesDto sendNotificationsDto = getCreateUpdatesDto(
                multipleData,
                caseDetails,
                userToken,
                ethosCaseRefCollection
        );

        SendNotificationTypeMultiple sendNotificationType = createSendNotificationType(multipleData);
        DataModelParent dataModelParent = PersistentQHelper.getSendNotificationDataModel(sendNotificationType);
        createUpdatesBusSender.sendUpdatesToQueue(
                sendNotificationsDto,
                dataModelParent,
                errors,
                String.valueOf(ethosCaseRefCollection.size()));
    }

    private static void setCaseRefCollection(SortedMap<String, Object> multipleObjects,
                                             List<String> ethosCaseRefCollection) {

        multipleObjects.forEach((key, value) -> {
            MultipleObject excelRow = (MultipleObject) value;
            ethosCaseRefCollection.add(excelRow.getEthosCaseRef());
        });
    }

    private CreateUpdatesDto getCreateUpdatesDto(MultipleData multipleData,
                                                 MultipleDetails caseDetails,
                                                 String userToken,
                                                 List<String> ethosCaseRefCollection) {

        String username = userIdamService.getUserDetails(userToken).getEmail();
        return CreateUpdatesDto.builder()
                .caseTypeId(caseDetails.getCaseTypeId())
                .jurisdiction(caseDetails.getJurisdiction())
                .multipleRef(multipleData.getMultipleReference())
                .ethosCaseRefCollection(ethosCaseRefCollection)
                .username(username)
                .build();
    }

    private SendNotificationTypeMultiple createSendNotificationType(MultipleData multipleData) {
        SendNotificationTypeMultiple sendNotificationType = new SendNotificationTypeMultiple();

        // Values from form
        sendNotificationType.setSendNotificationTitle(multipleData.getSendNotificationTitle());
        sendNotificationType.setSendNotificationLetter(multipleData.getSendNotificationLetter());
        sendNotificationType.setSendNotificationUploadDocument(multipleData.getSendNotificationUploadDocument());
        sendNotificationType.setSendNotificationSubject(multipleData.getSendNotificationSubject());
        sendNotificationType.setSendNotificationAdditionalInfo(multipleData.getSendNotificationAdditionalInfo());
        sendNotificationType.setSendNotificationNotify(multipleData.getSendNotificationNotify());
        sendNotificationType.setSendNotificationNotifyLeadCase(multipleData.getSendNotificationNotifyLeadCase());
        sendNotificationType.setSendNotificationNotifyAll(multipleData.getSendNotificationNotifyAll());
        sendNotificationType.setSendNotificationNotifySelected(multipleData.getSendNotificationNotifySelected());
        sendNotificationType.setSendNotificationSelectHearing(multipleData.getSendNotificationSelectHearing());
        sendNotificationType.setSendNotificationCaseManagement(multipleData.getSendNotificationCaseManagement());
        sendNotificationType.setSendNotificationResponseTribunal(multipleData.getSendNotificationResponseTribunal());
        sendNotificationType.setSendNotificationWhoCaseOrder(multipleData.getSendNotificationWhoCaseOrder());
        sendNotificationType.setSendNotificationSelectParties(multipleData.getSendNotificationSelectParties());
        sendNotificationType.setSendNotificationFullName(multipleData.getSendNotificationFullName());
        sendNotificationType.setSendNotificationFullName2(multipleData.getSendNotificationFullName2());
        sendNotificationType.setSendNotificationDecision(multipleData.getSendNotificationDecision());
        sendNotificationType.setSendNotificationDetails(multipleData.getSendNotificationDetails());
        sendNotificationType.setSendNotificationRequestMadeBy(multipleData.getSendNotificationRequestMadeBy());
        sendNotificationType.setSendNotificationEccQuestion(multipleData.getSendNotificationEccQuestion());
        sendNotificationType.setSendNotificationWhoMadeJudgement(multipleData.getSendNotificationWhoMadeJudgement());

        // Multiple sender indicator
        sendNotificationType.setNotificationSentFrom(multipleData.getMultipleReference());

        return sendNotificationType;
    }

    public void clearSendNotificationFields(MultipleData multipleData) {
        multipleData.setSendNotificationTitle(null);
        multipleData.setSendNotificationLetter(null);
        multipleData.setSendNotificationUploadDocument(null);
        multipleData.setSendNotificationSubject(null);
        multipleData.setSendNotificationAdditionalInfo(null);
        multipleData.setSendNotificationNotify(null);
        multipleData.setSendNotificationSelectHearing(null);
        multipleData.setSendNotificationCaseManagement(null);
        multipleData.setSendNotificationResponseTribunal(null);
        multipleData.setSendNotificationWhoCaseOrder(null);
        multipleData.setSendNotificationSelectParties(null);
        multipleData.setSendNotificationFullName(null);
        multipleData.setSendNotificationFullName2(null);
        multipleData.setSendNotificationDecision(null);
        multipleData.setSendNotificationDetails(null);
        multipleData.setSendNotificationRequestMadeBy(null);
        multipleData.setSendNotificationEccQuestion(null);
        multipleData.setSendNotificationWhoCaseOrder(null);
        multipleData.setSendNotificationNotifyLeadCase(null);
        multipleData.setSendNotificationNotifyAll(null);
        multipleData.setSendNotificationNotifySelected(null);
    }
}

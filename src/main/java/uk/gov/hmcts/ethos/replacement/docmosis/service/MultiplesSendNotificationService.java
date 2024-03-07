package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.model.servicebus.CreateUpdatesDto;
import uk.gov.hmcts.ecm.common.model.servicebus.datamodel.DataModelParent;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationType;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.et.common.model.multiples.MultipleDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.PersistentQHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.servicebus.CreateUpdatesBusSender;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service("multiplesSendNotificationService")
public class MultiplesSendNotificationService {
    private final CreateUpdatesBusSender createUpdatesBusSender;
    private final UserIdamService userIdamService;

    public void sendNotificationToSingles(MultipleData multipleData,
                                          MultipleDetails caseDetails,
                                          String userToken,
                                          List<String> errors) {

        List<String> ethosCaseRefCollection = new ArrayList<>();
        if ("Lead case".equals(multipleData.getSendNotificationNotify())) {
            ethosCaseRefCollection.add(multipleData.getLeadEthosCaseRef());
        } else {
            // TODO Read excel file to send to all cases on multiple
            log.info("TODO");
        }

        if (ethosCaseRefCollection.isEmpty()) {
            return;
        }
        CreateUpdatesDto sendNotificationsDto = getCreateUpdatesDto(
                multipleData,
                caseDetails,
                userToken,
                ethosCaseRefCollection
        );

        SendNotificationType sendNotificationType = createSendNotificationType(multipleData);
        DataModelParent dataModelParent = PersistentQHelper.getSendNotificationDataModel(sendNotificationType);
        createUpdatesBusSender.sendUpdatesToQueue(
                sendNotificationsDto,
                dataModelParent,
                errors,
                String.valueOf(ethosCaseRefCollection.size()));
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

    private SendNotificationType createSendNotificationType(MultipleData multipleData) {
        SendNotificationType sendNotificationType = new SendNotificationType();

        // Values from form
        sendNotificationType.setSendNotificationTitle(multipleData.getSendNotificationTitle());
        sendNotificationType.setSendNotificationLetter(multipleData.getSendNotificationLetter());
        sendNotificationType.setSendNotificationUploadDocument(multipleData.getSendNotificationUploadDocument());
        sendNotificationType.setSendNotificationSubject(multipleData.getSendNotificationSubject());
        sendNotificationType.setSendNotificationAdditionalInfo(multipleData.getSendNotificationAdditionalInfo());
        sendNotificationType.setSendNotificationNotify(multipleData.getSendNotificationNotify());
        sendNotificationType.setSendNotificationNotifyLeadCase(multipleData.getSendNotificationNotifyLeadCase());
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
    }
}

package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.PseResponseTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.documents.TornadoDocument;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.documents.notifications.NotificationData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.documents.notifications.ResponseAdminData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.documents.notifications.ResponsePartyData;

import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.JUDGMENT;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.SendNotificationService.CASE_MANAGEMENT_ORDERS_REQUESTS;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.SendNotificationService.EMPLOYER_CONTRACT_CLAIM;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.DocumentUtils.generateDocumentListFromDocumentList;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.DocumentUtils.generateUploadedDocumentListFromDocumentList;

@Slf4j
public final class NotificationDocumentHelper {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());
    private static final String NOTIFICATION_TEMPLATE_DOCX = "EM-TRB-EGW-ENG-00070.docx";

    private NotificationDocumentHelper() {
        // Access through static methods
    }

    public static String buildNotificationDocumentData(CaseData caseData, String accessKey)
            throws JsonProcessingException {
        SendNotificationType notification = getSendNotificationType(caseData);
        NotificationData notificationData = getNotificationData(caseData, notification);

        return OBJECT_MAPPER.writeValueAsString(TornadoDocument.<NotificationData>builder()
                .accessKey(accessKey)
                .outputName(getDocumentName(caseData))
                .templateName(NOTIFICATION_TEMPLATE_DOCX)
                .data(notificationData)
                .build());
    }

    private static NotificationData getNotificationData(CaseData caseData, SendNotificationType notification) {
        NotificationData.NotificationDataBuilder notificationData = NotificationData.builder()
                .ethosCaseReference(caseData.getEthosCaseReference())
                .notificationNumber(notification.getNumber())
                .notificationTitle(notification.getSendNotificationTitle())
                .notificationSubject(notification.getSendNotificationSubjectString())
                .dateSent(notification.getDate())
                .partyToNotify(notification.getSendNotificationNotify())
                .additionalInformation(defaultIfEmpty(notification.getSendNotificationAdditionalInfo(), "-"));
        if (isNotEmpty(notification.getSendNotificationUploadDocument())) {
            notificationData.areThereLetters(YES)
                    .documents(generateDocumentListFromDocumentList(notification.getSendNotificationUploadDocument()));
        }
        List<String> notificationSubject = notification.getSendNotificationSubject();
        if (notificationSubject.contains("Hearing")) {
            notificationData.isHearingSubject(YES)
                    .hearing(notification.getSendNotificationSelectHearing().getSelectedLabel());
        }
        if (notificationSubject.contains(CASE_MANAGEMENT_ORDERS_REQUESTS)) {
            notificationData.isCmoSubject(YES)
                    .cmoOrRequest(notification.getSendNotificationCaseManagement())
                    .cmoRequestMadeBy(notification.getSendNotificationFullName())
                    .cmoRequestResponseRequired(notification.getSendNotificationResponseTribunal());
        }
        if (notificationSubject.contains(JUDGMENT)) {
            notificationData.isJudgmentSubject(YES)
                    .judgmentName(notification.getSendNotificationFullName2())
                    .judgmentDecision(notification.getSendNotificationDecision());
        }
        if (notificationSubject.contains(EMPLOYER_CONTRACT_CLAIM)) {
            notificationData.isEccSubject(YES);
            notificationData.eccType(notification.getSendNotificationEccQuestion());
            notificationData.eccResponseRequired(NO.equals(notification.getSendNotificationResponseTribunal()
            ) ? NO : YES + " - " + notification.getSendNotificationSelectParties());

        }
        if (isNotEmpty(notification.getRespondCollection())) {
            notificationData.areThereResponses(YES)
                    .responses(createResponsesListForDocument(notification));
        }
        if (isNotEmpty(notification.getRespondNotificationTypeCollection())) {
            notificationData.areThereTribunalResponses(YES)
                    .tribunalResponses(createTribunalResponsesListForDocument(notification));
        }

        return notificationData.build();
    }

    private static List<ResponseAdminData> createTribunalResponsesListForDocument(SendNotificationType notification) {
        return emptyIfNull(notification.getRespondNotificationTypeCollection()).stream()
                .map(GenericTypeItem::getValue)
                .filter(ObjectUtils::isNotEmpty)
                .map(value -> ResponseAdminData.builder()
                    .title(value.getRespondNotificationTitle())
                    .date(value.getRespondNotificationDate())
                    .additionalInformation(defaultIfEmpty(value.getRespondNotificationAdditionalInfo(), "-"))
                    .partiesToNotify(value.getRespondNotificationPartyToNotify())
                    .cmoOrRequest(value.getRespondNotificationCmoOrRequest())
                    .responseRequired(
                        NO.equals(value.getRespondNotificationResponseRequired()) ? NO :
                            YES + " - " + value.getRespondNotificationWhoRespond())
                    .madeBy(value.getRespondNotificationFullName())
                    .areThereDocuments(isNotEmpty(value.getRespondNotificationUploadDocument()) ? YES : NO)
                    .documents(generateDocumentListFromDocumentList(value.getRespondNotificationUploadDocument()))
                    .build()
                )
                .collect(Collectors.toList());
    }

    private static List<ResponsePartyData> createResponsesListForDocument(SendNotificationType notification) {
        return emptyIfNull(notification.getRespondCollection()).stream()
                .map(PseResponseTypeItem::getValue)
                .filter(ObjectUtils::isNotEmpty)
                .map(value -> ResponsePartyData.builder()
                    .party(value.getFrom())
                    .responseDate(value.getDate())
                    .responseDetail(value.getResponse())
                    .areThereDocuments(isNotEmpty(value.getSupportingMaterial()) ? YES : NO)
                    .responseDocuments(generateUploadedDocumentListFromDocumentList(value.getSupportingMaterial()))
                    .copyToOtherParty(YES.equals(value.getCopyToOtherParty())
                            ? YES : NO + " - " + value.getCopyNoGiveDetails())
                        .build())
                .collect(Collectors.toList());
    }

    public static String getDocumentName(CaseData caseData) {
        SendNotificationType notification = getSendNotificationType(caseData);
        return "Notification %s Summary.pdf".formatted(notification.getNumber());
    }

    private static SendNotificationType getSendNotificationType(CaseData caseData) {
        return caseData.getSendNotificationCollection()
                .stream()
                .filter(s -> s.getId().equals(caseData.getSelectNotificationDropdown().getSelectedCode()))
                .findFirst()
                .orElseThrow(() ->
                    new IllegalArgumentException("Cannot find notification with id: "
                                                 + caseData.getSelectNotificationDropdown().getSelectedCode()
                                                 + " in case "
                                                 + caseData.getEthosCaseReference()))
                .getValue();
    }
}

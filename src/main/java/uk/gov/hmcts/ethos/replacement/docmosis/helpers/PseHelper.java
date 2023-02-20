package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.PseResponseType;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationType;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationTypeItem;

import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.defaultString;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CASE_MANAGEMENT_ORDER;

@Slf4j
public final class PseHelper {

    private static final String ORDER_APP_MARKUP = "|Hearing, case management order or request | |\r\n"
        + "|--|--|\r\n"
        + "|Notification | %s|\r\n"
        + "|Hearing | %s|\r\n"
        + "|Date sent | %s|\r\n"
        + "|Sent by | Tribunal|\r\n"
        + "|Case management order or request? | %s|\r\n"
        + "|Response due | %s|\r\n"
        + "|Party or parties to respond | %s|\r\n"
        + "|Additional information | %s|\r\n"
        + "%s" // APP_DETAILS_DOC
        + "|%s made by | %s|\r\n" // Case management order / Request
        + "|Name | %s|\r\n"
        + "|Sent to | %s|\r\n"
        + "\r\n"
        + "\r\n";

    private static final String ORDER_APP_DOC_MARKUP = "|Description | %s|\r\n"
        + "|Document | <a href=\"/documents/%s\" target=\"_blank\">%s</a>|\r\n";

    private static final String LEGAL_REP_REPLY_MARKUP = "|Response %s | |\r\n"
        + "|--|--|\r\n"
        + "|Response from | %s|\r\n"
        + "|Response date | %s|\r\n"
        + "|What's your response to the tribunal? | %s|\r\n"
        + "|Supporting material | %s|\r\n"
        + "|Do you want to copy this correspondence to the other party to satisfy the Rules of Procedure? | %s|\r\n"
        + "\r\n";

    private static final String DOC_MARKUP = "<a href=\"/documents/%s\" target=\"_blank\">%s</a>\r\n";

    private PseHelper() {
        // Access through static methods
    }

    /**
     * Gets the selected SendNotificationTypeItem.
     * @param caseData contains all the case data
     * @return the select application in GenericTseApplicationTypeItem
     */
    public static SendNotificationTypeItem getSelectedSendNotificationTypeItem(CaseData caseData) {
        String selectedAppId = caseData.getPseRespondentSelectOrderOrRequest().getSelectedCode();
        return caseData.getSendNotificationCollection().stream()
            .filter(s -> s.getValue().getNumber().equals(selectedAppId))
            .findFirst()
            .orElse(null);
    }

    /**
     * Format Markup for displaying Hearing, case management order or request.
     * @param sendNotificationType Target SendNotification Subject
     * @return Hearing, case management order or request Markup
     */
    public static String formatOrdReqDetails(SendNotificationType sendNotificationType) {
        return String.format(
            ORDER_APP_MARKUP,
            sendNotificationType.getSendNotificationTitle(),
            getSendNotificationSelectHearing(sendNotificationType),
            sendNotificationType.getDate(),
            defaultString(sendNotificationType.getSendNotificationCaseManagement()),
            defaultString(sendNotificationType.getSendNotificationResponseTribunal()),
            defaultString(sendNotificationType.getSendNotificationSelectParties()),
            defaultString(sendNotificationType.getSendNotificationAdditionalInfo()),
            getSendNotificationUploadDocument(sendNotificationType),
            defaultString(sendNotificationType.getSendNotificationCaseManagement()),
            getSendNotificationWhoMadeBy(sendNotificationType),
            defaultString(sendNotificationType.getSendNotificationFullName()),
            sendNotificationType.getSendNotificationNotify()
        );
    }

    private static String getSendNotificationSelectHearing(SendNotificationType sendNotificationType) {
        return sendNotificationType.getSendNotificationSelectHearing() == null
            ? ""
            : sendNotificationType.getSendNotificationSelectHearing().getSelectedLabel();
    }

    private static String getSendNotificationUploadDocument(SendNotificationType sendNotificationType) {
        return sendNotificationType.getSendNotificationUploadDocument() == null
            ? ""
            : sendNotificationType.getSendNotificationUploadDocument().stream()
            .map(d -> String.format(
                ORDER_APP_DOC_MARKUP,
                d.getValue().getShortDescription(),
                Helper.getDocumentMatcher(d.getValue().getUploadedDocument().getDocumentBinaryUrl())
                    .replaceFirst(""),
                d.getValue().getUploadedDocument().getDocumentFilename()
            ))
            .collect(Collectors.joining());
    }

    private static String getSendNotificationWhoMadeBy(SendNotificationType sendNotificationType) {
        return CASE_MANAGEMENT_ORDER.equals(sendNotificationType.getSendNotificationCaseManagement())
            ? defaultString(sendNotificationType.getSendNotificationWhoCaseOrder())
            : defaultString(sendNotificationType.getSendNotificationRequestMadeBy());
    }

    /**
     * Markup for displaying Response(s).
     * @param pseResponseType Legal Rep Respond
     * @return Response(s) Markup
     */
    public static String formatLegalRepReply(PseResponseType pseResponseType, int respondCount) {
        return String.format(
            LEGAL_REP_REPLY_MARKUP,
            respondCount,
            pseResponseType.getFrom(),
            pseResponseType.getDate(),
            pseResponseType.getResponse(),
            pseResponseType.getSupportingMaterial().stream()
                .map(d -> String.format(
                    DOC_MARKUP,
                    Helper.getDocumentMatcher(d.getValue().getUploadedDocument().getDocumentBinaryUrl())
                        .replaceFirst(""),
                    d.getValue().getUploadedDocument().getDocumentFilename()
                ))
                .collect(Collectors.joining()),
            pseResponseType.getCopyToOtherParty()
        );
    }

}

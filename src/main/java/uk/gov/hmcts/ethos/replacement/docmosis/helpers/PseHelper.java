package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.PseResponseType;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationType;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationTypeItem;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.IntWrapper;

import java.util.stream.Collectors;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CASE_MANAGEMENT_ORDER;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TableMarkupConstants.ADDITIONAL_INFORMATION;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TableMarkupConstants.DECISION_NOTIFICATION_TITLE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TableMarkupConstants.NAME_MARKUP;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TableMarkupConstants.ORDER_APP_DOC_MARKUP;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TableMarkupConstants.ORDER_APP_HEARING_MARKUP;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TableMarkupConstants.PARTY_OR_PARTIES_TO_RESPOND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TableMarkupConstants.RESPONSE_DATE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TableMarkupConstants.RESPONSE_DUE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TableMarkupConstants.RESPONSE_FROM;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TableMarkupConstants.RESPONSE_TABLE_HEADER;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TableMarkupConstants.SUPPORTING_MATERIAL_TABLE_HEADER;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TableMarkupConstants.TABLE_START_WITHOUT_HEADER;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TableMarkupConstants.TABLE_STRING;

@Slf4j
public final class PseHelper {

    private static final String ORDER_APP_MARKUP = "### Hearing, case management order or request \r\n "
            + TABLE_START_WITHOUT_HEADER
            + TABLE_STRING
            + DECISION_NOTIFICATION_TITLE
            + "%s" // Hearing
            + "|Date sent | %s|\r\n"
            + "|Sent by | Tribunal|\r\n"
            + "|Case management order or request? | %s|\r\n"
            + RESPONSE_DUE
            + PARTY_OR_PARTIES_TO_RESPOND
            + "%s" // Additional information
            + "%s" // APP_DETAILS_DOC
            + "%s" // Case management order / Request
            + "|Sent to | %s|\r\n"
            + "\r\n";

    private static final String ORDER_APP_CMO_MARKUP = "|%s made by | %s|\r\n"
            + NAME_MARKUP;

    private static final String CLAIMANT_REPLY_MARKUP = RESPONSE_TABLE_HEADER
            + TABLE_STRING
            + RESPONSE_FROM
            + RESPONSE_DATE
            + "|What's your response to the tribunal? | %s|\r\n"
            + SUPPORTING_MATERIAL_TABLE_HEADER
            + "|Do you want to copy this correspondence to the other party to satisfy the Rules of Procedure? | %s|\r\n"
            + "%s" // Rule92 No Details
            + "\r\n";

    private static final String RULE92_DETAILS_MARKUP =
            "|Details of why you do not want to inform the other party | %s|\r\n";

    private static final String DOC_MARKUP = "<a href=\"/documents/%s\" target=\"_blank\">%s</a>\r\n";

    private PseHelper() {
        // Access through static methods
    }

    /**
     * Gets the selected SendNotificationTypeItem.
     *
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
     *
     * @param sendNotificationType Target SendNotification Subject
     * @return Hearing, case management order or request Markup
     */
    // TODO: RET-2879: Add Judgment and ECC
    public static String formatOrdReqDetails(SendNotificationType sendNotificationType) {
        return String.format(
                ORDER_APP_MARKUP,
                sendNotificationType.getSendNotificationTitle(),
                getSendNotificationSelectHearing(sendNotificationType),
                sendNotificationType.getDate(),
                defaultString(sendNotificationType.getSendNotificationCaseManagement()),
                defaultString(sendNotificationType.getSendNotificationResponseTribunal()),
                defaultString(sendNotificationType.getSendNotificationSelectParties()),
                isBlank(sendNotificationType.getSendNotificationAdditionalInfo())
                    ? ""
                    : String.format(ADDITIONAL_INFORMATION, sendNotificationType.getSendNotificationAdditionalInfo()),
                getSendNotificationUploadDocument(sendNotificationType),
                getSendNotificationCmoRequestWhoMadeBy(sendNotificationType),
                sendNotificationType.getSendNotificationNotify()
        );
    }

    private static String getSendNotificationSelectHearing(SendNotificationType sendNotificationType) {
        return sendNotificationType.getSendNotificationSelectHearing() == null
                ? ""
                : String.format(
                ORDER_APP_HEARING_MARKUP,
                sendNotificationType.getSendNotificationSelectHearing().getSelectedLabel());
    }

    public static String getSendNotificationUploadDocument(SendNotificationType sendNotificationType) {
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

    private static String getSendNotificationCmoRequestWhoMadeBy(SendNotificationType sendNotificationType) {
        if (isNullOrEmpty(sendNotificationType.getSendNotificationCaseManagement())) {
            return "";
        }
        return String.format(
                ORDER_APP_CMO_MARKUP,
                defaultString(sendNotificationType.getSendNotificationCaseManagement()),
                CASE_MANAGEMENT_ORDER.equals(sendNotificationType.getSendNotificationCaseManagement())
                        ? defaultString(sendNotificationType.getSendNotificationWhoCaseOrder())
                        : defaultString(sendNotificationType.getSendNotificationRequestMadeBy()),
                defaultString(sendNotificationType.getSendNotificationFullName())
        );
    }

    /**
     * Markup for displaying Response(s).
     * @param sendNotificationType Send Notification Type with Response(s)
     * @return Response(s) Markup
     */
    // TODO: RET-2879: Update Claimant response after RET-2928 ready
    public static String formatRespondDetails(SendNotificationType sendNotificationType) {
        if (CollectionUtils.isEmpty(sendNotificationType.getRespondCollection())) {
            return "";
        }
        IntWrapper respondCount = new IntWrapper(0);
        return sendNotificationType.getRespondCollection().stream()
            .map(r -> formatClaimantReply(r.getValue(), respondCount.incrementAndReturnValue()))
            .collect(Collectors.joining(""));
    }

    private static String formatClaimantReply(PseResponseType pseResponseType, int respondCount) {
        return String.format(
                CLAIMANT_REPLY_MARKUP,
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
                pseResponseType.getCopyToOtherParty(),
                NO.equals(pseResponseType.getCopyToOtherParty())
                        ? String.format(
                        RULE92_DETAILS_MARKUP,
                        pseResponseType.getCopyNoGiveDetails())
                        : ""
        );
    }

}

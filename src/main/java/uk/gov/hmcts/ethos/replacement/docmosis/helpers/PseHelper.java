package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.PseResponseType;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationType;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationTypeItem;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.IntWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.CASE_MANAGEMENT_ORDER;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.TRIBUNAL;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TableMarkupConstants.DOC_MARKUP_DOCUMENT;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TableMarkupConstants.RESPONSE_DATE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TableMarkupConstants.RESPONSE_FROM;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TableMarkupConstants.RESPONSE_TABLE_HEADER;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TableMarkupConstants.SUPPORTING_MATERIAL_TABLE_HEADER;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TableMarkupConstants.TABLE_STRING;

@Slf4j
public final class PseHelper {

    private static final String CLAIMANT_REPLY_MARKUP = RESPONSE_TABLE_HEADER
            + TABLE_STRING
            + RESPONSE_FROM
            + RESPONSE_DATE
            + "|What's your response to the tribunal? | %s|\r\n"
            + "%s"
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
        return getSelectedNotificationWithCode(caseData, selectedAppId);
    }

    /**
     * Gets the selected SendNotificationTypeItem with Selected Code.
     * @param caseData contains all the case data
     * @param selectedAppId Selected Code from the list
     * @return the select application in GenericTseApplicationTypeItem
     */
    public static SendNotificationTypeItem getSelectedNotificationWithCode(CaseData caseData, String selectedAppId) {
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
    public static String formatOrdReqDetails(SendNotificationType sendNotificationType) {
        List<String[]> rows = new ArrayList<>(List.of(
            new String[]{"Notification", sendNotificationType.getSendNotificationTitle()},
            new String[]{"Hearing", getSendNotificationSelectHearing(sendNotificationType)},
            new String[]{"Date sent", sendNotificationType.getDate()},
            new String[]{"Sent by", TRIBUNAL},
            new String[]{"Case management order or request?", sendNotificationType.getSendNotificationCaseManagement()},
            new String[]{"Is a response required?", sendNotificationType.getSendNotificationResponseTribunal()},
            new String[]{"Party or parties to respond", sendNotificationType.getSendNotificationSelectParties()},
            new String[]{"Additional information", sendNotificationType.getSendNotificationAdditionalInfo()}
        ));
        rows.addAll(getSendNotificationUploadDocumentList(sendNotificationType));
        String requestMadeBy = CASE_MANAGEMENT_ORDER.equals(sendNotificationType.getSendNotificationCaseManagement())
            ? sendNotificationType.getSendNotificationWhoCaseOrder()
            : sendNotificationType.getSendNotificationRequestMadeBy();
        rows.addAll(List.of(
                new String[] {"Request made by", requestMadeBy},
                new String[] {"Name", sendNotificationType.getSendNotificationFullName()},
                new String[] {"Who made the judgment?", sendNotificationType.getSendNotificationWhoMadeJudgement()},
                new String[] {"Full name", sendNotificationType.getSendNotificationFullName2()},
                new String[] {"Decision", sendNotificationType.getSendNotificationDecision()},
                new String[] {"Details", sendNotificationType.getSendNotificationDetails()},
                new String[] {"What is the ECC notification?", sendNotificationType.getSendNotificationEccQuestion()},
                new String[] {"Sent to", sendNotificationType.getSendNotificationNotify()}
            )
        );
        return MarkdownHelper.createTwoColumnTable(new String[]{"View Application", ""}, rows);
    }

    public static List<String[]> getSendNotificationUploadDocumentList(SendNotificationType sendNotificationType) {
        List<String []> documents = new ArrayList<>();
        if (sendNotificationType.getSendNotificationUploadDocument() == null) {
            return documents;
        }
        for (DocumentTypeItem documentTypeItem : sendNotificationType.getSendNotificationUploadDocument()) {
            documents.add(new String[]{"Description", documentTypeItem.getValue().getShortDescription()});
            documents.add(new String[]{"Document", String.format(DOC_MARKUP_DOCUMENT,
                Helper.getDocumentMatcher(documentTypeItem.getValue().getUploadedDocument().getDocumentBinaryUrl())
                    .replaceFirst(""),
                documentTypeItem.getValue().getUploadedDocument().getDocumentFilename())});
        }
        return documents;
    }

    private static String getSendNotificationSelectHearing(SendNotificationType sendNotificationType) {
        return Optional.ofNullable(sendNotificationType.getSendNotificationSelectHearing())
            .map(hearing -> hearing.getSelectedLabel())
            .orElse("");
    }

    /**
     * Markup for displaying Response(s).
     * @param sendNotificationType Send Notification Type with Response(s)
     * @return Response(s) Markup
     */
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
        var supportingMaterial = pseResponseType.getSupportingMaterial();
        String supportingMaterialString = "";
        if (supportingMaterial != null) {
            supportingMaterialString = supportingMaterial.stream()
                .map(d -> String.format(
                    DOC_MARKUP,
                    Helper.getDocumentMatcher(d.getValue().getUploadedDocument().getDocumentBinaryUrl())
                        .replaceFirst(""),
                    d.getValue().getUploadedDocument().getDocumentFilename()
                ))
                .collect(Collectors.joining());
            supportingMaterialString = String.format(SUPPORTING_MATERIAL_TABLE_HEADER, supportingMaterialString);
        }

        return String.format(
                CLAIMANT_REPLY_MARKUP,
                respondCount,
                pseResponseType.getFrom(),
                pseResponseType.getDate(),
                pseResponseType.getResponse(),
                supportingMaterialString,
                pseResponseType.getCopyToOtherParty(),
                NO.equals(pseResponseType.getCopyToOtherParty())
                        ? String.format(
                        RULE92_DETAILS_MARKUP,
                        pseResponseType.getCopyNoGiveDetails())
                        : ""
        );
    }

}

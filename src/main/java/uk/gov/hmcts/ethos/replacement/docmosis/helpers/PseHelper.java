package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.PseResponseTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.PseResponseType;
import uk.gov.hmcts.et.common.model.ccd.types.RespondNotificationType;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationType;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationTypeItem;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.IntWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.BOTH_PARTIES;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CASE_MANAGEMENT_ORDER;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLAIMANT_ONLY;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLAIMANT_TITLE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.RESPONDENT_ONLY;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.RESPONDENT_TITLE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.TRIBUNAL;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.MarkdownHelper.addDocumentTypeRows;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.MarkdownHelper.addGenericTypeDocumentRows;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.MarkdownHelper.asRow;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.MarkdownHelper.detailsWrapper;

@Slf4j
public final class PseHelper {

    private static final String ACCEPTANCE_OF_ECC_RESPONSE = "Acceptance of ECC response";
    public static final String CLAIMANT_REPRESENTATIVE = "Claimant Representative";
    public static final String INVALID_PARTY_SELECTION = "Invalid party selection";

    private PseHelper() {
        // Access through static methods
    }

    /**
     * Gets the selected SendNotificationTypeItem.
     *
     * @param caseData contains all the case data
     * @return the select application in GenericTseApplicationTypeItem
     */
    public static SendNotificationTypeItem getSelectedRespondentNotification(CaseData caseData) {
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
            new String[]{"Date sent", sendNotificationType.getDate()},
            new String[]{"Sent by", TRIBUNAL},
            new String[]{"Case management order or request?", sendNotificationType.getSendNotificationCaseManagement()},
            new String[]{"Is a response required?", sendNotificationType.getSendNotificationResponseTribunal()},
            new String[]{"Party or parties to respond", sendNotificationType.getSendNotificationSelectParties()},
            new String[]{"Additional information", sendNotificationType.getSendNotificationAdditionalInfo()}
        ));

        if (!ACCEPTANCE_OF_ECC_RESPONSE.equals(sendNotificationType.getSendNotificationEccQuestion())) {
            rows.add(1, new String[]{"Hearing", getSendNotificationSelectHearing(sendNotificationType)});
        }

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
        return MarkdownHelper.createTwoColumnTable(new String[]{"View Notification", ""}, rows);
    }

    public static List<String[]> getSendNotificationUploadDocumentList(SendNotificationType sendNotificationType) {

        if (sendNotificationType.getSendNotificationUploadDocument() == null) {
            return new ArrayList<>();
        }
        return sendNotificationType.getSendNotificationUploadDocument()
            .stream()
            .flatMap(documentTypeItem -> MarkdownHelper.addDocumentRow(documentTypeItem.getValue()).stream())
            .toList();
    }

    private static String getSendNotificationSelectHearing(SendNotificationType sendNotificationType) {
        return Optional.ofNullable(sendNotificationType.getSendNotificationSelectHearing())
            .map(hearing -> hearing.getSelectedLabel())
            .orElse(null);
    }

    /**
     * Markup for displaying Response(s).
     *
     * @param sendNotificationType Send Notification Type with Response(s)
     * @return Response(s) Markup
     */
    public static String formatResponseDetails(SendNotificationType sendNotificationType, String party) {
        if (CollectionUtils.isEmpty(sendNotificationType.getRespondCollection())) {
            return "";
        }
        IntWrapper respondCount = new IntWrapper(0);
        String notificationToDisplay = sendNotificationType.getRespondCollection().stream()
                .filter(sn -> shouldDisplayResponse(sn, party))
                .map(r -> formatPartyReply(r.getValue(),
                        respondCount,
                        sendNotificationType.getSendNotificationSubject()))
                .collect(Collectors.joining(""));
        return notificationToDisplay.isEmpty()
                ? ""
                : detailsWrapper("Responses", notificationToDisplay);
    }

    private static boolean shouldDisplayResponse(PseResponseTypeItem sn, String party) {
        final PseResponseType value = sn.getValue();
        return switch (party) {
            case RESPONDENT_TITLE ->
                YES.equals(value.getCopyToOtherParty());
            case CLAIMANT_TITLE ->
                (RESPONDENT_TITLE.equals(value.getFrom()) && YES.equals(value.getCopyToOtherParty()))
                    || CLAIMANT_TITLE.equals(value.getFrom())
                    || CLAIMANT_REPRESENTATIVE.equals(value.getFrom());
            default -> throw new IllegalArgumentException(INVALID_PARTY_SELECTION);
        };
    }

    private static String formatPartyReply(PseResponseType pseResponseType,
                                           IntWrapper respondCount,
                                           List<String> sendNotificationSubject) {
        if (isClaimantEccResponse(sendNotificationSubject, pseResponseType.getFrom())) {
            return "";
        }

        String rule92 = "Do you want to copy this correspondence to the other party to satisfy the Rules of Procedure?";
        String rule92Why = "Details of why you do not want to inform the other party";
        return "\r\n" + MarkdownHelper.createTwoColumnTable(
                new String[]{"Response " + respondCount.incrementAndReturnValue(), " "}, Stream.of(
                        asRow("Response from", pseResponseType.getFrom()),
                        asRow("Response date", pseResponseType.getDate()),
                        asRow("What's your response to the tribunal?", pseResponseType.getResponse()),
                        addGenericTypeDocumentRows(pseResponseType.getSupportingMaterial(), "Supporting material"),
                        asRow(rule92, pseResponseType.getCopyToOtherParty()),
                        asRow(rule92Why, pseResponseType.getCopyNoGiveDetails())
        ));
    }

    private static boolean isClaimantEccResponse(List<String> sendNotificationSubject, String from) {
        return sendNotificationSubject.contains("Employer Contract Claim") && from.equals(CLAIMANT_TITLE);
    }

    public static String formatTribunalResponse(SendNotificationType sendNotificationType, String party) {
        if (CollectionUtils.isEmpty(sendNotificationType.getRespondNotificationTypeCollection())) {
            return "";
        }

        IntWrapper respondCount = new IntWrapper(0);
        return detailsWrapper("Tribunal Responses", sendNotificationType.getRespondNotificationTypeCollection().stream()
                .filter(r -> filterTribunalResponse(r.getValue(), party))
                .map(r -> formatTribunalReply(r.getValue(), respondCount))
                .collect(Collectors.joining("")));
    }

    private static boolean filterTribunalResponse(RespondNotificationType value, String party) {
        return switch (party) {
            case CLAIMANT_TITLE ->
                value.getRespondNotificationPartyToNotify().equals(CLAIMANT_ONLY)
                    || value.getRespondNotificationPartyToNotify().equals(BOTH_PARTIES)
                    || value.getRespondNotificationWhoRespond().equals(CLAIMANT_ONLY)
                    || value.getRespondNotificationWhoRespond().equals(BOTH_PARTIES);
            case RESPONDENT_TITLE ->
                value.getRespondNotificationPartyToNotify().equals(RESPONDENT_ONLY)
                    || value.getRespondNotificationPartyToNotify().equals(BOTH_PARTIES)
                    || value.getRespondNotificationWhoRespond().equals(RESPONDENT_ONLY)
                    || value.getRespondNotificationWhoRespond().equals(BOTH_PARTIES);
            default -> throw new IllegalArgumentException(INVALID_PARTY_SELECTION);
        };
    }

    private static String formatTribunalReply(RespondNotificationType value, IntWrapper respondCount) {
        return "\r\n" + MarkdownHelper.createTwoColumnTable(
                new String[]{"Tribunal Response " + respondCount.incrementAndReturnValue(), " "},
                Stream.of(
                        asRow("Notification", defaultIfEmpty(value.getRespondNotificationTitle(), " - ")),
                        asRow("Response from", defaultIfEmpty(value.getRespondNotificationFullName(), " - ")),
                        asRow("Response date", defaultIfEmpty(value.getRespondNotificationDate(), " - ")),
                        addDocumentTypeRows(value.getRespondNotificationUploadDocument(), "Supporting material"),
                        asRow("Additional information",
                                defaultIfEmpty(value.getRespondNotificationAdditionalInfo(), " - ")),
                        asRow("Response Type", defaultIfEmpty(value.getRespondNotificationCmoOrRequest(), " - ")),
                        asRow("Party to notify", defaultIfEmpty(value.getRespondNotificationPartyToNotify(), " - ")),
                        asRow("Is a response required?",
                                defaultIfEmpty(value.getRespondNotificationResponseRequired(), " - ")),
                        asRow("Parties to respond", defaultIfEmpty(value.getRespondNotificationWhoRespond(), " - "))
                ));
    }

    public static boolean canPartyViewNotification(SendNotificationTypeItem sendNotificationTypeItem, String party) {
        if (CLAIMANT_TITLE.equals(party)) {
            return CLAIMANT_ONLY.equals(sendNotificationTypeItem.getValue().getSendNotificationNotify())
                   || BOTH_PARTIES.equalsIgnoreCase(sendNotificationTypeItem.getValue().getSendNotificationNotify());
        } else if (RESPONDENT_TITLE.equals(party)) {
            return RESPONDENT_ONLY.equals(sendNotificationTypeItem.getValue().getSendNotificationNotify())
                   || BOTH_PARTIES.equalsIgnoreCase(sendNotificationTypeItem.getValue().getSendNotificationNotify());
        } else {
            throw new IllegalArgumentException(INVALID_PARTY_SELECTION);
        }

    }

    public static SendNotificationTypeItem getSelectedClaimantNotification(CaseData caseData) {
        String selectedAppId = caseData.getClaimantSelectNotification().getSelectedCode();
        return getSelectedNotificationWithCode(caseData, selectedAppId);
    }

    /**
     * Checks if party to notify selection mismatches with selected parties.
     * @param partyToNotify selected party to notify
     * @param partyToRespond selected party to respond
     * @return true if there is a mismatch, false otherwise
     */
    public static boolean isPartyToNotifyMismatch(String partyToRespond, String partyToNotify) {
        if (partyToRespond == null || partyToNotify == null) {
            return false;
        }

        if (BOTH_PARTIES.equals(partyToNotify)) {
            return false;
        }

        String partyToRespondMap = switch (partyToRespond) {
            case BOTH_PARTIES -> BOTH_PARTIES;
            case CLAIMANT_TITLE, CLAIMANT_ONLY -> CLAIMANT_ONLY;
            case RESPONDENT_TITLE, RESPONDENT_ONLY -> RESPONDENT_ONLY;
            default -> null;
        };
        if (partyToRespondMap == null) {
            return false;
        }

        return !partyToNotify.equals(partyToRespondMap);
    }
}

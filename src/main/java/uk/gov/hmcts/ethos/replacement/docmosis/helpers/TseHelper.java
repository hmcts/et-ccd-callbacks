package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.json.JSONObject;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationType;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.TseRespondTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.TseRespondType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.documents.TseReplyData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.documents.TseReplyDocument;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.isNullOrEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NEW_DATE_PATTERN;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@Slf4j
public final class TseHelper {
    public static final String INTRO = "The respondent has applied to <b>%s</b>.</br>%s</br> If you have any "
        + "objections or responses to their application you must send them to the tribunal as soon as possible and by "
        + "%s at the latest.</br></br>If you need more time to respond, you may request more time from the tribunal. If"
        + " you do not respond or request more time to respond, the tribunal will consider the application without your"
        + " response.";
    public static final String TABLE = "| | |\r\n"
        + "|--|--|\r\n"
        + "|Application date | %s\r\n"
        + "|Details of the application | %s\r\n"
        + "Application file upload | %s";
    public static final String GROUP_B = "You do not need to respond to this application.<br>";
    public static final List<String> GROUP_B_TYPES = List.of("Change my personal details", "Consider a decision "
        + "afresh", "Reconsider a judgment", "Withdraw my claim");
    public static final String OPEN = "Open";
    public static final String CLOSED = "Closed";

    private static final String REPLY_OUTPUT_NAME = "%s Reply.pdf";
    private static final String REPLY_TEMPLATE_NAME = "EM-TRB-EGW-ENG-01212.docx";

    private static final String ADMIN_REPLY_MARKUP_FOR_REPLY = "|Response %s | |\r\n"
        + "|--|--|\r\n"
        + "|Response from | %s|\r\n"
        + "|Response date | %s|\r\n"
        + "|What’s your response to the %s’s application? | %s|\r\n"
        + "|Supporting material | %s|\r\n"
        + "|Do you want to copy this correspondence to the other party to satisfy the Rules of Procedure? | %s|\r\n"
        + "\r\n";
    private static final String RESPONDENT_REPLY_MARKUP_FOR_DECISION = "|Response %s | |\r\n"
        + "|--|--|\r\n"
        + "|Response from | %s|\r\n"
        + "|Response date | %s|\r\n"
        + "|Details | %s|\r\n"
        + "|Supporting material | %s|\r\n"
        + "\r\n";
    private static final String ADMIN_REPLY_MARKUP_FOR_DECISION = "|Response %s | |\r\n"
        + "|--|--|\r\n"
        + "|Response | %s|\r\n"
        + "|Date | %s|\r\n"
        + "|Sent by | %s|\r\n"
        + "|Case management order or request? | %s|\r\n"
        + "|Response due | %s|\r\n"
        + "|Party or parties to respond | %s|\r\n"
        + "|Additional information | %s|\r\n"
        + "|Description | %s|\r\n"
        + "|Document | %s|\r\n"
        + "|Request made by | %s|\r\n"
        + "|Name | %s|\r\n"
        + "|Sent to | %s|\r\n"
        + "\r\n";
    private static final String COPY_TO_OTHER_PARTY_YES = "I confirm I want to copy";
    private static final String COPY_TO_OTHER_PARTY_NO = "I do not want to copy";

    private TseHelper() {
        // Access through static methods
    }

    /**
     * Create fields for application dropdown selector.
     * @param caseData contains all the case data
     */
    public static DynamicFixedListType populateSelectApplicationDropdown(CaseData caseData) {
        if (CollectionUtils.isEmpty(caseData.getGenericTseApplicationCollection())) {
            return null;
        }

        return DynamicFixedListType.from(caseData.getGenericTseApplicationCollection().stream()
            .filter(r -> r.getValue().getRespondCollection() == null
                && r.getValue().getStatus() != null
                && !CLOSED.equals(r.getValue().getStatus())
            ).map(r -> DynamicValueType.create(
                r.getValue().getNumber(),
                r.getValue().getNumber() + " " + r.getValue().getType())
            ).collect(Collectors.toList()));
    }

    /**
     * Sets the data for the second page of the response journey.
     * @param caseData contains all the case data
     */
    public static void setDataForRespondingToApplication(CaseData caseData) {
        List<GenericTseApplicationTypeItem> applications = caseData.getGenericTseApplicationCollection();
        if (CollectionUtils.isEmpty(applications)) {
            return;
        }

        GenericTseApplicationType genericTseApplicationType = getSelectedApplication(caseData);

        LocalDate date = LocalDate.parse(genericTseApplicationType.getDate(), NEW_DATE_PATTERN);

        caseData.setTseResponseIntro(
            String.format(
                INTRO,
                genericTseApplicationType.getType(),
                GROUP_B_TYPES.contains(genericTseApplicationType.getType()) ? GROUP_B : "",
                UtilHelper.formatCurrentDatePlusDays(date, 7)
            )
        );

        String document = "N/A";

        if (genericTseApplicationType.getDocumentUpload() != null) {
            Pattern pattern = Pattern.compile("^.+?/documents/");
            Matcher matcher = pattern.matcher(genericTseApplicationType.getDocumentUpload().getDocumentBinaryUrl());
            String documentLink = matcher.replaceFirst("");
            String documentName = genericTseApplicationType.getDocumentUpload().getDocumentFilename();
            document = String.format("<a href=\"/documents/%s\" target=\"_blank\">%s</a>", documentLink, documentName);
        }

        caseData.setTseResponseTable(
            String.format(
                TABLE,
                genericTseApplicationType.getDate(),
                isNullOrEmpty(genericTseApplicationType.getDetails()) ? "N/A" : genericTseApplicationType.getDetails(),
                document
            )
        );
    }

    /**
     * Saves the data on the reply page onto the application object.
     * @param caseData contains all the case data
     */
    public static void saveReplyToApplication(CaseData caseData) {
        List<GenericTseApplicationTypeItem> applications = caseData.getGenericTseApplicationCollection();
        if (CollectionUtils.isEmpty(applications)) {
            return;
        }

        GenericTseApplicationType genericTseApplicationType = getSelectedApplication(caseData);
        genericTseApplicationType.setRespondCollection(List.of(TseRespondTypeItem.builder()
            .id(UUID.randomUUID().toString())
            .value(
                TseRespondType.builder()
                    .response(caseData.getTseResponseText())
                    .supportingMaterial(caseData.getTseResponseSupportingMaterial())
                    .hasSupportingMaterial(caseData.getTseResponseHasSupportingMaterial())
                    .from("Respondent")
                    .date(UtilHelper.formatCurrentDate(LocalDate.now()))
                    .copyToOtherParty(caseData.getTseResponseCopyToOtherParty())
                    .copyNoGiveDetails(caseData.getTseResponseCopyNoGiveDetails())
                    .build()
            ).build()));
        // TODO: This will need changing when we support admin decisions
        genericTseApplicationType.setResponsesCount("1");
    }

    /**
     * Clears fields that are used when responding to an application.
     * @param caseData contains all the case data
     */
    public static void resetReplyToApplicationPage(CaseData caseData) {
        caseData.setTseResponseText(null);
        caseData.setTseResponseIntro(null);
        caseData.setTseResponseTable(null);
        caseData.setTseResponseHasSupportingMaterial(null);
        caseData.setTseResponseSupportingMaterial(null);
        caseData.setTseResponseCopyToOtherParty(null);
        caseData.setTseResponseCopyNoGiveDetails(null);
    }

    /**
     * Gets the select application.
     * @param caseData contains all the case data
     * @return the select application
     */
    public static GenericTseApplicationType getSelectedApplication(CaseData caseData) {
        return caseData.getGenericTseApplicationCollection()
            .get(Integer.parseInt(caseData.getTseRespondSelectApplication().getValue().getCode()) - 1).getValue();
    }

    /**
     * Gets the select application in GenericTseApplicationTypeItem.
     * @param caseData contains all the case data
     * @return the select application in GenericTseApplicationTypeItem
     */
    public static GenericTseApplicationTypeItem getSelectedApplicationTypeItem(CaseData caseData) {
        String selectedAppId = caseData.getTseAdminSelectApplication().getSelectedCode();
        return caseData.getGenericTseApplicationCollection().stream()
                .filter(genericTseApplicationTypeItem ->
                        genericTseApplicationTypeItem.getValue().getNumber().equals(selectedAppId))
                .findFirst()
                .orElse(null);
    }

    /**
     * Builds a document request for generating the pdf of the CYA page for responding to a claimant application.
     * @param caseData contains all the case data
     * @param accessKey access key required for docmosis
     * @return a string representing the api request to docmosis
     */
    public static String getReplyDocumentRequest(CaseData caseData, String accessKey) throws JsonProcessingException {
        GenericTseApplicationType selectedApplication = getSelectedApplication(caseData);
        TseReplyData data = createDataForTseReply(caseData.getEthosCaseReference(), selectedApplication);
        TseReplyDocument document = TseReplyDocument.builder()
            .accessKey(accessKey)
            .outputName(String.format(REPLY_OUTPUT_NAME, selectedApplication.getType()))
            .templateName(REPLY_TEMPLATE_NAME)
            .data(data).build();
        return new ObjectMapper().writeValueAsString(document);
    }

    public static Map<String, Object> getPersonalisationForResponse(CaseDetails caseDetails, byte[] document)
        throws NotificationClientException {
        CaseData caseData = caseDetails.getCaseData();
        GenericTseApplicationType selectedApplication = getSelectedApplication(caseData);
        TseRespondType replyType = selectedApplication.getRespondCollection().get(0).getValue();
        JSONObject documentJson = NotificationClient.prepareUpload(document, false, true, "52 weeks");

        return Map.of(
            "ccdId", caseDetails.getCaseId(),
            "caseNumber", caseData.getEthosCaseReference(),
            "applicationType", selectedApplication.getType(),
            "response", isNullOrEmpty(replyType.getResponse()) ? "" : replyType.getResponse(),
            "claimant", caseData.getClaimant(),
            "respondents", Helper.getRespondentNames(caseData),
            "linkToDocument", documentJson
        );
    }

    public static Map<String, Object> getPersonalisationForAcknowledgement(CaseDetails caseDetails) {
        CaseData caseData = caseDetails.getCaseData();
        GenericTseApplicationType selectedApplication = getSelectedApplication(caseData);

        return Map.of(
            "caseNumber", caseData.getEthosCaseReference(),
            "claimant", caseData.getClaimant(),
            "respondents", Helper.getRespondentNames(caseData),
            "shortText", selectedApplication.getType(),
            "caseId", caseDetails.getCaseId()
        );
    }

    private static TseReplyData createDataForTseReply(String caseId, GenericTseApplicationType application) {
        TseRespondType replyType = application.getRespondCollection().get(0).getValue();
        return TseReplyData.builder()
            .copy(replyType.getCopyToOtherParty())
            .caseNumber(caseId)
            .supportingYesNo(replyType.getHasSupportingMaterial())
            .type(application.getType())
            .documentCollection(replyType.getSupportingMaterial())
            .response(replyType.getResponse())
            .build();
    }

    /**
     * Format Admin response markup.
     * @param reply Respond as TseRespondType
     * @param respondCount Respond count as incrementAndReturnValue()
     * @param docInfo Supporting material info as documentManagementService.displayDocNameTypeSizeLink()
     * @return Markup String
     */
    public static String formatAdminReply(TseRespondType reply, int respondCount, String docInfo) {
        return String.format(
            ADMIN_REPLY_MARKUP_FOR_DECISION,
            respondCount,
            reply.getEnterResponseTitle(),
            reply.getDate(),
            "Tribunal",
            reply.getIsCmoOrRequest(),
            reply.getIsResponseRequired(),
            reply.getSelectPartyRespond(),
            reply.getAdditionalInformation(),
            "description of document entered",
            docInfo,
            reply.getRequestMadeBy(),
            reply.getMadeByFullName(),
            reply.getSelectPartyNotify());
    }

    /**
     * Format Respondent or Claimant response markup for Respond to an application.
     * @param reply Respond as TseRespondType
     * @param respondCount Respond count as incrementAndReturnValue()
     * @param docInfo Supporting material info as documentManagementService.displayDocNameTypeSizeLink()
     * @return Markup String
     */
    public static String formatRespondentReplyForReply(TseRespondType reply, int respondCount, String docInfo) {
        return String.format(
            ADMIN_REPLY_MARKUP_FOR_REPLY,
            respondCount,
            reply.getFrom(),
            reply.getDate(),
            reply.getFrom().toLowerCase(),
            reply.getResponse(),
            docInfo,
            displayCopyToOtherPartyYesOrNo(reply.getCopyToOtherParty())
        );
    }

    /**
     * Return getCopyToOtherPartyYesOrNo as Yes or No.
     * @param copyToOtherPartyYesOrNo getCopyToOtherPartyYesOrNo()
     * @return Yes or No
     */
    public static String displayCopyToOtherPartyYesOrNo(String copyToOtherPartyYesOrNo) {
        if (COPY_TO_OTHER_PARTY_YES.equals(copyToOtherPartyYesOrNo)) {
            return YES;
        } else if (COPY_TO_OTHER_PARTY_NO.equals(copyToOtherPartyYesOrNo)) {
            return NO;
        } else {
            return copyToOtherPartyYesOrNo;
        }
    }

    /**
     * Format Respondent or Claimant response markup for Record a Decision.
     * @param reply Respond as TseRespondType
     * @param respondCount Respond count as incrementAndReturnValue()
     * @param docInfo Supporting material info as documentManagementService.displayDocNameTypeSizeLink()
     * @return Markup String
     */
    public static String formatRespondentReplyForDecision(TseRespondType reply, int respondCount, String docInfo) {
        return String.format(
            RESPONDENT_REPLY_MARKUP_FOR_DECISION,
            respondCount,
            reply.getFrom(),
            reply.getDate(),
            reply.getResponse(),
            docInfo
        );
    }

}

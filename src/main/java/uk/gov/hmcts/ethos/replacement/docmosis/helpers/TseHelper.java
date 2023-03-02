package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationType;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.TseRespondTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.TseRespondType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.documents.TseReplyData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.documents.TseReplyDocument;
import uk.gov.hmcts.ethos.replacement.docmosis.service.DocumentManagementService;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CASE_MANAGEMENT_ORDER;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLOSED_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NEW_DATE_PATTERN;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.REQUEST;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.RESPONDENT_TITLE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ADMIN;

@Slf4j
@SuppressWarnings({"PMD.ExcessiveImports"})
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

    private static final String REPLY_OUTPUT_NAME = "%s Reply.pdf";
    private static final String REPLY_TEMPLATE_NAME = "EM-TRB-EGW-ENG-01212.docx";

    private static final String STRING_BR = "<br>";

    private static final String RESPONDENT_REPLY_MARKUP_FOR_REPLY = "|Response %s | |\r\n"
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
    private static final String ADMIN_REPLY_MARKUP = "|Response %s | |\r\n"
        + "|--|--|\r\n"
        + "|Response | %s|\r\n"
        + "|Date | %s|\r\n"
        + "|Sent by | Tribunal|\r\n"
        + "|Case management order or request? | %s|\r\n"
        + "|Response due | %s|\r\n"
        + "|Party or parties to respond | %s|\r\n"
        + "|Additional information | %s|\r\n"
        + "|Supporting material | %s|\r\n"
        + "%s"
        + "|Full name | %s|\r\n"
        + "|Sent to | %s|\r\n"
        + "\r\n";

    private static final String VIEW_APPLICATION_ADMIN_REPLY_MARKUP = "|Response %s | |\r\n"
            + "|--|--|\r\n"
            + "|Response | %s|\r\n"
            + "|Date | %s|\r\n"
            + "|Sent by | Tribunal|\r\n"
            + "|Case management order or request? | %s|\r\n"
            + "|Response due | %s|\r\n"
            + "|Party or parties to respond | %s|\r\n"
            + "|Additional information | %s|\r\n"
            + "|Supporting material | %s|\r\n"
            + "%s"
            + "|Name | %s|\r\n"
            + "|Sent to | %s|\r\n"
            + "\r\n";

    private static final String RESPONSE_LIST_TITLE =  "|Responses | |\r\n"
            + "|--|--|\r\n"
            + "\r\n";

    private static final String APPLICATION_DETAILS = "|Application | |\r\n"
            + "|--|--|\r\n"
            + "|Applicant | %s|\r\n"
            + "|Type of application | %s|\r\n"
            + "|Application date | %s|\r\n"
            + "|What do you want to tell or ask the tribunal? | %s|\r\n"
            + "|Supporting material | %s|\r\n"
            + "|Do you want to copy this correspondence to the other party to satisfy the Rules of Procedure? | %s |\r\n"
            + "\r\n";
    private static final String ADMIN_REPLY_MARKUP_MADE_BY = "|%s made by | %s|\r\n";

    private TseHelper() {
        // Access through static methods
    }

    /**
     * Create fields for application dropdown selector.
     * @param caseData contains all the case data
     */
    public static DynamicFixedListType populateRespondentSelectApplication(CaseData caseData) {
        if (CollectionUtils.isEmpty(caseData.getGenericTseApplicationCollection())) {
            return null;
        }

        return DynamicFixedListType.from(caseData.getGenericTseApplicationCollection().stream()
            .filter(o -> !CLOSED_STATE.equals(o.getValue().getStatus())
                && isNoRespondentReply(o.getValue().getRespondCollection()))
            .map(TseHelper::formatDropdownOption)
            .collect(Collectors.toList()));
    }

    private static boolean isNoRespondentReply(List<TseRespondTypeItem> tseRespondTypeItems) {
        return CollectionUtils.isEmpty(tseRespondTypeItems)
            || tseRespondTypeItems.stream().noneMatch(r -> RESPONDENT_TITLE.equals(r.getValue().getFrom()));
    }

    public static DynamicFixedListType populateOpenOrClosedApplications(CaseData caseData) {

        if (CollectionUtils.isEmpty(caseData.getGenericTseApplicationCollection())) {
            return null;
        }

        boolean selectedClosed = CLOSED_STATE.equals(caseData.getTseViewApplicationOpenOrClosed());

        return DynamicFixedListType.from(caseData.getGenericTseApplicationCollection().stream()
                .filter(o -> selectedClosed ? o.getValue().getStatus().equals(CLOSED_STATE)
                        : !o.getValue().getStatus().equals(CLOSED_STATE))
                .map(TseHelper::formatDropdownOption)
                .collect(Collectors.toList()));
    }

    private static DynamicValueType formatDropdownOption(GenericTseApplicationTypeItem genericTseApplicationTypeItem) {
        GenericTseApplicationType value = genericTseApplicationTypeItem.getValue();
        return DynamicValueType.create(value.getNumber(), String.format("%s %s", value.getNumber(), value.getType()));
    }

    /**
     * Sets the data for the second page of the response journey.
     * @param caseData contains all the case data
     */
    public static void setDataForRespondingToApplication(CaseData caseData) {
        List<GenericTseApplicationTypeItem> applications = caseData.getGenericTseApplicationCollection();
        if (CollectionUtils.isEmpty(applications) || getSelectedApplication(caseData) == null) {
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
            Matcher matcher = Helper.getDocumentMatcher(
                genericTseApplicationType.getDocumentUpload().getDocumentBinaryUrl());
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

    public static GenericTseApplicationType getChosenApplication(CaseData caseData) {
        return caseData.getGenericTseApplicationCollection()
                .get(Integer.parseInt(caseData.getTseViewApplicationSelect().getValue().getCode()) - 1).getValue();
    }

    public static String getDocumentUrls(TseRespondTypeItem tseRespondType){
        if (tseRespondType.getValue().getSupportingMaterial() != null) {
            Pattern pattern = Pattern.compile("^.+?/documents/");
            return tseRespondType.getValue().getSupportingMaterial().stream()
                    .map(doc -> {
                        Matcher matcher = pattern.matcher(doc.getValue().getUploadedDocument().getDocumentBinaryUrl());
                        String documentLink = matcher.replaceFirst("");
                        String documentName = doc.getValue().getUploadedDocument().getDocumentFilename();
                        return String.format("<a href=\"/documents/%s\" target=\"_blank\">%s</a>", documentLink, documentName) + STRING_BR;
                    }).collect(Collectors.joining(""));
        }
        return null;
    }

    private static String createResponseTable(List<TseRespondTypeItem> respondList, String applicant){
        AtomicInteger i = new AtomicInteger(0);
        return  RESPONSE_LIST_TITLE + respondList.stream().map((TseRespondTypeItem response)-> {
            i.getAndIncrement();
            String doc = "N/A";
            if( ADMIN.equals(response.getValue().getFrom()) ){
                Pattern pattern = Pattern.compile("^.+?/documents/");
                Matcher matcher = pattern.matcher(response.getValue().getAddDocument().getDocumentBinaryUrl());
                String documentLink = matcher.replaceFirst("");
                String documentName = response.getValue().getAddDocument().getDocumentFilename();
                doc = String.format("<a href=\"/documents/%s\" target=\"_blank\">%s</a>", documentLink, documentName);

                return String.format(
                        VIEW_APPLICATION_ADMIN_REPLY_MARKUP,
                        i.get(),
                        defaultString(response.getValue().getEnterResponseTitle()),
                        response.getValue().getDate(),
                        defaultString(response.getValue().getIsCmoOrRequest()),
                        defaultString(response.getValue().getIsResponseRequired()),
                        defaultString(response.getValue().getSelectPartyRespond()),
                        defaultString(response.getValue().getAdditionalInformation()),
                        doc,
                        formatAdminReplyMadeBy(response.getValue()),
                        defaultString(response.getValue().getMadeByFullName()),
                        defaultString(response.getValue().getSelectPartyNotify())
                );
            } else {
                String links = getDocumentUrls(response);
                if (links != null) {
                    doc = links;
                }
                return String.format(
                        RESPONDENT_REPLY_MARKUP_FOR_REPLY,
                        i.get(),
                        response.getValue().getFrom(),
                        response.getValue().getDate(),
                        applicant.toLowerCase(Locale.ENGLISH),
                        defaultString(response.getValue().getResponse()),
                        doc,
                        response.getValue().getCopyToOtherParty()
                );
            }
        }).collect(Collectors.joining(""));
    }

    public static void getDataSetViewForSelectedApplication(CaseData caseData) {

        List<GenericTseApplicationTypeItem> applications = caseData.getGenericTseApplicationCollection();
        if (CollectionUtils.isEmpty(applications) || getChosenApplication(caseData) == null) {
            return;
        }
        // get values for the application summary
        GenericTseApplicationType genericTseApplicationType = getChosenApplication(caseData);
        String document = "N/A";
        if (genericTseApplicationType.getDocumentUpload() != null) {
            Pattern pattern = Pattern.compile("^.+?/documents/");
            Matcher matcher = pattern.matcher(genericTseApplicationType.getDocumentUpload().getDocumentBinaryUrl());
            String documentLink = matcher.replaceFirst("");
            String documentName = genericTseApplicationType.getDocumentUpload().getDocumentFilename();
            document = String.format("<a href=\"/documents/%s\" target=\"_blank\">%s</a>", documentLink, documentName);
        }
        // get values for the response list
        String respondTablesCollection = "";
        if (!CollectionUtils.isEmpty(genericTseApplicationType.getRespondCollection())) {
            List<TseRespondTypeItem> respondList = genericTseApplicationType.getRespondCollection();
            respondTablesCollection = createResponseTable(respondList, genericTseApplicationType.getApplicant());
        }
        // set the application summary and response list to setTse......
        caseData.setTseApplicationResponsesTable(
                String.format(
                APPLICATION_DETAILS, genericTseApplicationType.getApplicant(),
                genericTseApplicationType.getType(),
                genericTseApplicationType.getDate(),
                isNullOrEmpty(genericTseApplicationType.getDetails()) ? "N/A" : genericTseApplicationType.getDetails(),
                document,
                genericTseApplicationType.getCopyToOtherPartyYesOrNo()
        ) + respondTablesCollection );
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

        if (CollectionUtils.isEmpty(genericTseApplicationType.getRespondCollection())) {
            genericTseApplicationType.setRespondCollection(new ArrayList<>());
        }

        genericTseApplicationType.getRespondCollection().add(TseRespondTypeItem.builder()
            .id(UUID.randomUUID().toString())
            .value(
                TseRespondType.builder()
                    .response(caseData.getTseResponseText())
                    .supportingMaterial(caseData.getTseResponseSupportingMaterial())
                    .hasSupportingMaterial(caseData.getTseResponseHasSupportingMaterial())
                    .from(RESPONDENT_TITLE)
                    .date(UtilHelper.formatCurrentDate(LocalDate.now()))
                    .copyToOtherParty(caseData.getTseResponseCopyToOtherParty())
                    .copyNoGiveDetails(caseData.getTseResponseCopyNoGiveDetails())
                    .build()
            ).build());

        genericTseApplicationType.setResponsesCount(
            String.valueOf(genericTseApplicationType.getRespondCollection().size())
        );
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

    /**
     * Personalisation for sending Acknowledgement for Response.
     * @param caseDetails contains all the case data
     * @param document TSE Reply.pdf
     * @return Personalisation For Response
     * @throws NotificationClientException Throw Exception
     */
    public static Map<String, Object> getPersonalisationForResponse(CaseDetails caseDetails, byte[] document)
        throws NotificationClientException {
        CaseData caseData = caseDetails.getCaseData();
        GenericTseApplicationType selectedApplication = getSelectedApplication(caseData);
        JSONObject documentJson = NotificationClient.prepareUpload(document, false, true, "52 weeks");

        return Map.of(
            "ccdId", caseDetails.getCaseId(),
            "caseNumber", caseData.getEthosCaseReference(),
            "applicationType", selectedApplication.getType(),
            "response", isNullOrEmpty(caseData.getTseResponseText()) ? "" : caseData.getTseResponseText(),
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
            ADMIN_REPLY_MARKUP,
            respondCount,
            defaultString(reply.getEnterResponseTitle()),
            reply.getDate(),
            defaultString(reply.getIsCmoOrRequest()),
            defaultString(reply.getIsResponseRequired()),
            defaultString(reply.getSelectPartyRespond()),
            defaultString(reply.getAdditionalInformation()),
            docInfo,
            formatAdminReplyMadeBy(reply),
            defaultString(reply.getMadeByFullName()),
            defaultString(reply.getSelectPartyNotify())
        );
    }

    private static String formatAdminReplyMadeBy(TseRespondType reply) {
        if (CASE_MANAGEMENT_ORDER.equals(reply.getIsCmoOrRequest())) {
            return String.format(
                ADMIN_REPLY_MARKUP_MADE_BY,
                reply.getIsCmoOrRequest(),
                reply.getCmoMadeBy());
        } else if (REQUEST.equals(reply.getIsCmoOrRequest())) {
            return String.format(
                ADMIN_REPLY_MARKUP_MADE_BY,
                reply.getIsCmoOrRequest(),
                reply.getRequestMadeBy());
        }
        return "";
    }

    /**
     * Format Respondent or Claimant response markup for Respond to an application.
     * @param reply Respond as TseRespondType
     * @param respondCount Respond count as incrementAndReturnValue()
     * @param applicant GenericTseApplicationType getApplicant()
     * @param docInfo Supporting material info as documentManagementService.displayDocNameTypeSizeLink()
     * @return Markup String
     */
    public static String formatLegalRepReplyOrClaimantForReply(TseRespondType reply, int respondCount, String applicant,
                                                               String docInfo) {
        return String.format(
            RESPONDENT_REPLY_MARKUP_FOR_REPLY,
            respondCount,
            reply.getFrom(),
            reply.getDate(),
            applicant.toLowerCase(Locale.ENGLISH),
            defaultString(reply.getResponse()),
            docInfo,
            reply.getCopyToOtherParty()
        );
    }

    /**
     * Format Respondent or Claimant response markup for Record a Decision.
     * @param reply Respond as TseRespondType
     * @param respondCount Respond count as incrementAndReturnValue()
     * @param docInfo Supporting material info as documentManagementService.displayDocNameTypeSizeLink()
     * @return Markup String
     */
    public static String formatLegalRepReplyOrClaimantForDecision(TseRespondType reply, int respondCount,
                                                                  String docInfo) {
        return String.format(
            RESPONDENT_REPLY_MARKUP_FOR_DECISION,
            respondCount,
            reply.getFrom(),
            reply.getDate(),
            defaultString(reply.getResponse()),
            docInfo
        );
    }

}

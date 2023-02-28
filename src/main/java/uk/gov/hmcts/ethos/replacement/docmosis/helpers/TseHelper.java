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

    public static ArrayList<String> getDocumentUrls(TseRespondTypeItem tseRespondType){
        if (tseRespondType.getValue().getSupportingMaterial() != null) {
            Pattern pattern = Pattern.compile("^.+?/documents/");

            ArrayList<String> links = tseRespondType.getValue().getSupportingMaterial().stream()
                    .map(doc -> {
                        Matcher matcher = pattern.matcher(doc.getValue().getUploadedDocument().getDocumentBinaryUrl());
                        String documentLink = matcher.replaceFirst("");
                        String documentName = doc.getValue().getUploadedDocument().getDocumentFilename();
                        return String.format("<a href=\"/documents/%s\" target=\"_blank\">%s</a>", documentLink, documentName);
                    }).collect(Collectors.toCollection(ArrayList::new));
            return links;
        }
        return null;
    }

    public static String createResponseTable(List<TseRespondTypeItem> respondList){
        AtomicInteger i = new AtomicInteger(1);

        String RESPONSE_TABLE_BODY = respondList.stream().map((TseRespondTypeItem response)-> {

            // check if value is admin or not
            if( ADMIN.equals(response.getValue().getFrom()) ){
                String RESPONSES_TABLE = "|Response " + i.get() + " | |\r\n" +  "|--|--|\r\n";

                // AN ADDITIONAL TITLE FIELD
                if(response.getValue().getEnterResponseTitle() != null ){
                    RESPONSES_TABLE +=("|Response |" + String.valueOf(response.getValue().getEnterResponseTitle()))+ "\r\n";
                }
                //  WHAT ABOUT THE ACTUAL RESPONSE
                if(response.getValue().getResponse() != null ){
                    RESPONSES_TABLE +=("|DO I EXIST |" + String.valueOf(response.getValue().getResponse())) + "\r\n";
                }
                if (response.getValue().getDate() != null) {
                    RESPONSES_TABLE += ("|Date |" + String.valueOf(response.getValue().getDate())) + "\r\n";
                }
                if(response.getValue().getFrom() != null ){
                    RESPONSES_TABLE +="|Sent by | Tribunal\r\n";
                }
                if (response.getValue().getIsCmoOrRequest() != null) {
                    RESPONSES_TABLE += ("|Case management or order request? |" + String.valueOf(response.getValue().getIsCmoOrRequest())) + "\r\n";
                }

                // response due
                if (response.getValue().getIsResponseRequired() != null) {
                    RESPONSES_TABLE += ("|Response due |" + String.valueOf(response.getValue().getIsResponseRequired())) + "\r\n";
                }

                if (response.getValue().getSelectPartyRespond() != null) {
                    RESPONSES_TABLE += ("|Parties or parties to respond |" + String.valueOf(response.getValue().getSelectPartyRespond())) + "\r\n";
                }
                if (response.getValue().getAdditionalInformation() != null) {
                    RESPONSES_TABLE += ("|Additional information |" + String.valueOf(response.getValue().getAdditionalInformation())) + "\r\n";
                }
                // CONFIRM access to document and DESCRIPTION

                // REQUEST MADE BY
                if (StringUtils.isNotEmpty(formatAdminReplyMadeBy(response.getValue() ))) {
                    RESPONSES_TABLE += formatAdminReplyMadeBy(response.getValue());
                }
                // check the ending of the above
                if (StringUtils.isNotEmpty(defaultString(response.getValue().getMadeByFullName()))) {
                    RESPONSES_TABLE += ("|Name |" + String.valueOf(response.getValue().getMadeByFullName())) + "\r\n";
                }

                // SENT TO
                if (response.getValue().getSelectPartyNotify() != null) {
                    RESPONSES_TABLE += ("|Sent to |" + response.getValue().getSelectPartyNotify() + "\r\n");
                }

                RESPONSES_TABLE += "\r\n";
                i.getAndIncrement();
                return RESPONSES_TABLE;
            } else {

                String RESPONSES_TABLE = "|Response " + i.get() + " | |\r\n" +  "|--|--|\r\n";

                if (response.getValue().getResponse() != null) {
                    RESPONSES_TABLE += ("|Response |" + String.valueOf(response.getValue().getResponse())) + "\r\n";
                }
                if (response.getValue().getEnterResponseTitle() != null) {
                    RESPONSES_TABLE += ("|Response title |" + String.valueOf(response.getValue().getEnterResponseTitle())) + "\r\n";
                }

                if (response.getValue().getFrom() != null) {
                    RESPONSES_TABLE += ("|From |" + String.valueOf(response.getValue().getFrom())) + "\r\n";
                }

                ArrayList<String> links = getDocumentUrls(response);
                AtomicInteger j = new AtomicInteger(1);

                if (links != null) {
                    String linky = links.stream().map(link -> {
                        if (j.get() == 1) {
                            j.getAndIncrement();
                            return "|Supporting Material |" + link + "\r\n";
                        }
                        return "| |" + link + "\r\n";
                    }).collect(Collectors.joining(""));
                    RESPONSES_TABLE += linky;

                }

                if (response.getValue().getDate() != null) {
                    RESPONSES_TABLE += ("|Date |" + String.valueOf(response.getValue().getDate())) + "\r\n";
                }

                if (response.getValue().getCopyToOtherParty() != null) {
                    RESPONSES_TABLE += ("|Copy to other party |" + String.valueOf(response.getValue().getCopyToOtherParty())) + "\r\n";
                }
                if (response.getValue().getCopyNoGiveDetails() != null) {
                    RESPONSES_TABLE += ("|Copy no give details |" + String.valueOf(response.getValue().getCopyNoGiveDetails())) + "\r\n";
                }
                if (response.getValue().getIsCmoOrRequest() != null) {
                    RESPONSES_TABLE += ("|Case management or order request? |" + String.valueOf(response.getValue().getIsCmoOrRequest())) + "\r\n";
                }
                if (response.getValue().getRequestMadeBy() != null) {
                    RESPONSES_TABLE += ("|Request made by |" + String.valueOf(response.getValue().getRequestMadeBy())) + "\r\n";
                }
                if (response.getValue().getSelectPartyRespond() != null) {
                    RESPONSES_TABLE += ("|Parties or parties to respond |" + String.valueOf(response.getValue().getSelectPartyRespond())) + "\r\n";
                }
                if (response.getValue().getSelectPartyNotify() != null) {
                    RESPONSES_TABLE += ("|Sent to |" + String.valueOf(response.getValue().getSelectPartyNotify())) + "\r\n";
                }
                if (response.getValue().getAdditionalInformation() != null) {
                    RESPONSES_TABLE += ("|Additional information |" + String.valueOf(response.getValue().getAdditionalInformation())) + "\r\n";
                }

                RESPONSES_TABLE += "\r\n";

                i.getAndIncrement();
                return RESPONSES_TABLE;
            }


        }).collect(Collectors.joining(""));

        return RESPONSE_TABLE_BODY; //+ "<br><br><br><br>";
       // return RESPONSES_TABLE_START + "<br><br><br><br>";

    //    return  String.format(TABLE, "documentLink", "documentName","THREE");
    }

    private static final String APPLICATION_DETAILS = "<hr><h3>Application</h3>"
            + "<pre>Applicant               &#09&#09&#09&#09&#09&#09&#09&#09&#09&#09&#09&#09&nbsp; %s"
            + "<br><br>Type of application  &#09&#09&#09&#09&#09&#09&#09&#09&#09&#09&#09&nbsp;&nbsp;&nbsp; %s"
            + "<br><br>Application date     &#09&#09&#09&#09&#09&#09&#09&#09&#09&#09&#09&#09 %s"
            + "<br><br>What do you want to tell or ask the tribunal? &#09&nbsp;&nbsp;&nbsp; %s"
            + "<br><br>Supporting material                          &#09&#09&#09&#09&#09&nbsp; %s"
            + "<br><br>Do you want to copy this correspondence to the other party to satisfy the Rules of Procedure? &#09&#09 %s</pre> ";
          //  + "<br><br>";


    public static void getDataSetViewForSelectedApplication(CaseData caseData) {

        // get every application on the case
        List<GenericTseApplicationTypeItem> applications = caseData.getGenericTseApplicationCollection();
        // return null if no applications
        if (CollectionUtils.isEmpty(applications) || getChosenApplication(caseData) == null) {
            return;
        }
        // get the selected application picked from dropdown
        GenericTseApplicationType genericTseApplicationType = getChosenApplication(caseData);

        // change
         // if the chosen application has a response collection create a table and set


        String document = "N/A";

        if (genericTseApplicationType.getDocumentUpload() != null) {
            Pattern pattern = Pattern.compile("^.+?/documents/");
            Matcher matcher = pattern.matcher(genericTseApplicationType.getDocumentUpload().getDocumentBinaryUrl());
            String documentLink = matcher.replaceFirst("");
            String documentName = genericTseApplicationType.getDocumentUpload().getDocumentFilename();
            document = String.format("<a href=\"/documents/%s\" target=\"_blank\">%s</a>", documentLink, documentName);
        }

        // change
        String respondTablesCollection = "";
        if (!CollectionUtils.isEmpty(genericTseApplicationType.getRespondCollection())) {
            List<TseRespondTypeItem> respondList = genericTseApplicationType.getRespondCollection();
            respondTablesCollection = createResponseTable(respondList);
          //  caseData.setTseApplicationResponsesTable(respondTablesCollection);
        }
        caseData.setTseApplicationResponsesTable(
//                String.format(
//                APPLICATION_DETAILS,
//                "Applicant",
//                genericTseApplicationType.getType(),
//                genericTseApplicationType.getDate(),
//                isNullOrEmpty(genericTseApplicationType.getDetails()) ? "N/A" : genericTseApplicationType.getDetails(),
//                document,
//                genericTseApplicationType.getCopyToOtherPartyYesOrNo()
//        ) +
                 respondTablesCollection );

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

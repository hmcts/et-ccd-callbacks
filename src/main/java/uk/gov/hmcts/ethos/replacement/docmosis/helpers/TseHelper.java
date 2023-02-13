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
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationType;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.TseRespondTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.TseRespondType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.documents.TseReplyData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.documents.TseReplyDocument;
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
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NEW_DATE_PATTERN;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

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
    public static final String OPEN = "Open";
    public static final String CLOSED = "Closed";

    private static final String REPLY_OUTPUT_NAME = "%s Reply.pdf";
    private static final String REPLY_TEMPLATE_NAME = "EM-TRB-EGW-ENG-01212.docx";

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
        + "|Sent by | %s|\r\n"
        + "|Case management order or request? | %s|\r\n"
        + "|Response due | %s|\r\n"
        + "|Party or parties to respond | %s|\r\n"
        + "|Additional information | %s|\r\n"
        + "|Description | %s|\r\n"
        + "|Document | %s|\r\n"
        + "%s"
        + "|Name | %s|\r\n"
        + "|Sent to | %s|\r\n"
        + "\r\n";
    private static final String ADMIN_REPLY_MARKUP_MADE_BY = "|%s made by | %s|\r\n";
    private static final String IS_CMO_OR_REQUEST_CMO = "Case management order";
    private static final String IS_CMO_OR_REQUEST_REQUEST = "Request";
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
            .filter(o -> !CLOSED.equals(o.getValue().getStatus()))
            .map(TseHelper::formatDropdownOption)
            .collect(Collectors.toList()));
    }

    public static DynamicFixedListType populateOpenOrClosedApplications(CaseData caseData) {

        if (CollectionUtils.isEmpty(caseData.getGenericTseApplicationCollection())) {
            return null;
        }

        String selectedOpenOrClosed = caseData.getViewRespondentTSEApplicationsOpenClosed();

        return DynamicFixedListType.from(caseData.getGenericTseApplicationCollection().stream()
                .filter(o -> o.getValue().getStatus().equals(selectedOpenOrClosed))
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


    public static GenericTseApplicationType getChosenApplication(CaseData caseData) {
        return caseData.getGenericTseApplicationCollection()
                .get(Integer.parseInt(caseData.getTseSelectOpenOrClosedApplications().getValue().getCode()) - 1).getValue();
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


    private static final String APPLICATION_DETAILS = "<hr><h3>Application</h3>"
            + "<pre>Applicant               &#09&#09&#09&#09&#09&#09&#09&#09&#09&#09&#09&#09&nbsp; %s"
            + "<br><br>Type of application  &#09&#09&#09&#09&#09&#09&#09&#09&#09&#09&#09&nbsp;&nbsp;&nbsp; %s"
            + "<br><br>Application date     &#09&#09&#09&#09&#09&#09&#09&#09&#09&#09&#09&#09 %s"
            + "<br><br>What do you want to tell or ask the tribunal? &#09&nbsp;&nbsp;&nbsp; %s"
            + "<br><br>Supporting material                          &#09&#09&#09&#09&#09&nbsp; %s"
            + "<br><br>Do you want to copy this correspondence to the other party to satisfy the Rules of Procedure? &#09&#09 %s</pre> "
            + "<br><br>";


    public static void getDataSetViewForSelectedApplication(CaseData caseData) {

        // get the applications
        List<GenericTseApplicationTypeItem> applications = caseData.getGenericTseApplicationCollection();
        // return null if no applications
        if (CollectionUtils.isEmpty(applications) || getChosenApplication(caseData) == null) {
            return;
        }
        // get the selected application
        GenericTseApplicationType genericTseApplicationType = getChosenApplication(caseData);

         // Check if the chosen application has a response collection
        if (!CollectionUtils.isEmpty(genericTseApplicationType.getRespondCollection())) {
            String RESPONSES_TABLE_BEGIN = "| | |\r\n"
                    + "|--|--|\r\n"+
                    "|Responses |\r\n";

            AtomicInteger i = new AtomicInteger(1);
            List<TseRespondTypeItem> respondList = genericTseApplicationType.getRespondCollection();

            if (CollectionUtils.isEmpty(respondList)) {
                // handle empty list
            }

            String respondTablesCollection = respondList.stream().map((TseRespondTypeItem response)-> {
                String RESPONSES_TABLE = "";

                RESPONSES_TABLE+="|Response " + String.valueOf(i.get()) + "\r\n";

                if(response.getValue().getResponse() != null ){
                    RESPONSES_TABLE +=("|Response |" + String.valueOf(response.getValue().getResponse())) + "\r\n";
                }
                if(response.getValue().getEnterResponseTitle() != null ){
                    RESPONSES_TABLE +=("|Response title |" + String.valueOf(response.getValue().getEnterResponseTitle()))+ "\r\n";
                }

                if(response.getValue().getFrom() != null ){
                    RESPONSES_TABLE +=("|From |" + String.valueOf(response.getValue().getFrom()))+ "\r\n";
                }

                ArrayList<String> links = getDocumentUrls(response);
                            AtomicInteger j = new AtomicInteger(1);

                if (links != null){
                            String linky = links.stream().map(link->{
                                    if(j.get() ==1) {
                                        j.getAndIncrement();
                                        return "|Supporting Material |" + link + "\r\n";
                                    }
                                    return "| |"+link+ "\r\n";
                            }).collect(Collectors.joining(""));
                            RESPONSES_TABLE += linky;

                }

                if(response.getValue().getDate() != null ){
                    RESPONSES_TABLE +=("|Date |" +String.valueOf(response.getValue().getDate()))+ "\r\n";
                }

                if(response.getValue().getCopyToOtherParty() != null ){
                    RESPONSES_TABLE +=("|Copy to other party |" +String.valueOf(response.getValue().getCopyToOtherParty()))+ "\r\n";
                }
                if(response.getValue().getCopyNoGiveDetails() != null ){
                    RESPONSES_TABLE +=("|Copy no give details |" +String.valueOf(response.getValue().getCopyNoGiveDetails()))+ "\r\n";
                }
                if(response.getValue().getIsCmoOrRequest() != null ){
                    RESPONSES_TABLE +=("|Case management or order request? |" +String.valueOf(response.getValue().getIsCmoOrRequest())) + "\r\n";
                }
                if(response.getValue().getRequestMadeBy() != null ){
                    RESPONSES_TABLE +=("|Request made by |" +String.valueOf(response.getValue().getRequestMadeBy())) + "\r\n";
                }
                if(response.getValue().getSelectPartyRespond() != null ){
                    RESPONSES_TABLE +=("|Parties or parties to respond |" +String.valueOf(response.getValue().getSelectPartyRespond())) + "\r\n";
                }
                if(response.getValue().getSelectPartyNotify() != null ){
                    RESPONSES_TABLE +=("|Sent to |" +String.valueOf(response.getValue().getSelectPartyNotify())) + "\r\n";
                }
                if(response.getValue().getAdditionalInformation() != null ){
                    RESPONSES_TABLE +=("|Additional information |" +String.valueOf(response.getValue().getAdditionalInformation())) + "\r\n";
                }

                RESPONSES_TABLE +="|  &nbsp;  |   | \r\n";
                i.getAndIncrement();
                return RESPONSES_TABLE;

            }).collect(Collectors.joining(""));
            caseData.setTseApplicationResponsesTable(RESPONSES_TABLE_BEGIN + respondTablesCollection);

        }

        String document = "N/A";

        if (genericTseApplicationType.getDocumentUpload() != null) {
            Pattern pattern = Pattern.compile("^.+?/documents/");
            Matcher matcher = pattern.matcher(genericTseApplicationType.getDocumentUpload().getDocumentBinaryUrl());
            String documentLink = matcher.replaceFirst("");
            String documentName = genericTseApplicationType.getDocumentUpload().getDocumentFilename();
            document = String.format("<a href=\"/documents/%s\" target=\"_blank\">%s</a>", documentLink, documentName);
        }
        caseData.setTseApplicationSummary(String.format(
                APPLICATION_DETAILS,
                "Applicant",
                genericTseApplicationType.getType(),
                genericTseApplicationType.getDate(),
                isNullOrEmpty(genericTseApplicationType.getDetails()) ? "N/A" : genericTseApplicationType.getDetails(),
                document,
                genericTseApplicationType.getCopyToOtherPartyYesOrNo()
        ));

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
                    .from("Respondent")
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
            ADMIN_REPLY_MARKUP,
            respondCount,
            defaultString(reply.getEnterResponseTitle()),
            reply.getDate(),
            "Tribunal",
            defaultString(reply.getIsCmoOrRequest()),
            defaultString(reply.getIsResponseRequired()),
            defaultString(reply.getSelectPartyRespond()),
            defaultString(reply.getAdditionalInformation()),
            "description of document entered",
            docInfo,
            formatAdminReplyMadeBy(reply),
            defaultString(reply.getMadeByFullName()),
            defaultString(reply.getSelectPartyNotify())
        );
    }

    private static String formatAdminReplyMadeBy(TseRespondType reply) {
        if (IS_CMO_OR_REQUEST_CMO.equals(reply.getIsCmoOrRequest())) {
            return String.format(
                ADMIN_REPLY_MARKUP_MADE_BY,
                reply.getIsCmoOrRequest(),
                reply.getCmoMadeBy());
        } else if (IS_CMO_OR_REQUEST_REQUEST.equals(reply.getIsCmoOrRequest())) {
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
    public static String formatRespondentReplyForReply(TseRespondType reply, int respondCount, String applicant,
                                                       String docInfo) {
        return String.format(
            RESPONDENT_REPLY_MARKUP_FOR_REPLY,
            respondCount,
            reply.getFrom(),
            reply.getDate(),
            applicant.toLowerCase(Locale.ENGLISH),
            defaultString(reply.getResponse()),
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
            return defaultString(copyToOtherPartyYesOrNo);
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
            defaultString(reply.getResponse()),
            docInfo
        );
    }

}

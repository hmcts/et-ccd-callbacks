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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;

import static com.google.common.base.Strings.isNullOrEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLOSED_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NEW_DATE_PATTERN;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.RESPONDENT_TITLE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.CASE_NUMBER;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.CLAIMANT;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.LINK_TO_CITIZEN_HUB;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.LINK_TO_EXUI;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.RESPONDENTS;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TableMarkupConstants.TABLE_STRING;

@Slf4j
public final class TseHelper {
    public static final String INTRO = "The respondent has applied to <b>%s</b>.</br>%s</br> If you have any "
        + "objections or responses to their application you must send them to the tribunal as soon as possible and by "
        + "%s at the latest.</br></br>If you need more time to respond, you may request more time from the tribunal. If"
        + " you do not respond or request more time to respond, the tribunal will consider the application without your"
        + " response.";
    public static final String TSE_RESPONSE_TABLE = "| | |\r\n"
            + TABLE_STRING
            + "|Application date | %s\r\n"
            + "|Details of the application | %s\r\n"
            + "Application file upload | %s";
    public static final String GROUP_B = "You do not need to respond to this application.<br>";
    public static final List<String> GROUP_B_TYPES = List.of("Change my personal details", "Consider a decision "
            + "afresh", "Reconsider a judgment", "Withdraw my claim");

    private static final String REPLY_OUTPUT_NAME = "%s Reply.pdf";
    private static final String REPLY_TEMPLATE_NAME = "EM-TRB-EGW-ENG-01212.docx";
    private static final String RULE92_YES_OR_NO_MARKUP =
            "|Do you want to copy this correspondence to the other party to satisfy the Rules of Procedure? | %s|\r\n"
                    + "%s";
    private static final String RULE92_DETAILS_MARKUP =
            "|Details of why you do not want to inform the other party | %s|\r\n";

    private TseHelper() {
        // Access through static methods
    }

    /**
     * Create fields for application dropdown selector.
     *
     * @param caseData contains all the case data
     */
    public static DynamicFixedListType populateRespondentSelectApplication(CaseData caseData) {
        if (CollectionUtils.isEmpty(caseData.getGenericTseApplicationCollection())) {
            return null;
        }

        return DynamicFixedListType.from(caseData.getGenericTseApplicationCollection().stream()
                .filter(o -> !CLOSED_STATE.equals(o.getValue().getStatus())
                        && isNoRespondentReply(o.getValue().getRespondCollection())
                        && YES.equals(o.getValue().getCopyToOtherPartyYesOrNo()))
                .map(TseHelper::formatDropdownOption)
                .toList());
    }

    private static boolean isNoRespondentReply(List<TseRespondTypeItem> tseRespondTypeItems) {
        return CollectionUtils.isEmpty(tseRespondTypeItems)
                || tseRespondTypeItems.stream().noneMatch(r -> RESPONDENT_TITLE.equals(r.getValue().getFrom()));
    }

    private static DynamicValueType formatDropdownOption(GenericTseApplicationTypeItem genericTseApplicationTypeItem) {
        GenericTseApplicationType value = genericTseApplicationTypeItem.getValue();
        return DynamicValueType.create(value.getNumber(), String.format("%s %s", value.getNumber(), value.getType()));
    }

    /**
     * Sets the data for the second page of the response journey.
     *
     * @param caseData contains all the case data
     */
    public static void setDataForRespondingToApplication(CaseData caseData) {
        List<GenericTseApplicationTypeItem> applications = caseData.getGenericTseApplicationCollection();
        if (CollectionUtils.isEmpty(applications)) {
            return;
        }

        GenericTseApplicationType genericTseApplicationType = getSelectedApplication(caseData);

        if (genericTseApplicationType == null) {
            return;
        }

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
                TSE_RESPONSE_TABLE,
                genericTseApplicationType.getDate(),
                isNullOrEmpty(genericTseApplicationType.getDetails()) ? "N/A" : genericTseApplicationType.getDetails(),
                document
            )
        );
    }

    /**
     * Saves the data on the reply page onto the application object.
     *
     * @param caseData contains all the case data
     */
    public static void saveReplyToApplication(CaseData caseData) {
        List<GenericTseApplicationTypeItem> applications = caseData.getGenericTseApplicationCollection();
        if (CollectionUtils.isEmpty(applications)) {
            return;
        }

        GenericTseApplicationType genericTseApplicationType = getSelectedApplication(caseData);
        ensureSelectedApplicationExists(genericTseApplicationType);

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
     *
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
        caseData.setTseRespondSelectApplication(null);
    }

    /**
     * Gets the select application.
     *
     * @param caseData contains all the case data
     * @return the select application
     */
    public static GenericTseApplicationType getSelectedApplication(CaseData caseData) {
        List<GenericTseApplicationTypeItem> applications = caseData.getGenericTseApplicationCollection();

        if (CollectionUtils.isEmpty(applications)) {
            return null;
        }

        DynamicFixedListType respondSelectApplication = caseData.getTseRespondSelectApplication();

        if (caseData.getTseRespondSelectApplication() != null) {
            return applications.get(Integer.parseInt(respondSelectApplication.getValue().getCode()) - 1).getValue();
        }

        DynamicFixedListType tseAdminSelectApplication = caseData.getTseAdminSelectApplication();

        if (tseAdminSelectApplication != null) {
            return applications.get(Integer.parseInt(tseAdminSelectApplication.getValue().getCode()) - 1).getValue();
        }

        DynamicFixedListType tseViewSelectApplication = caseData.getTseViewApplicationSelect();

        if (tseViewSelectApplication != null) {
            return applications.get(Integer.parseInt(tseViewSelectApplication.getValue().getCode()) - 1).getValue();
        }

        return null;
    }

    /**
     * Gets the select application in GenericTseApplicationTypeItem.
     *
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
     *
     * @param caseData  contains all the case data
     * @param accessKey access key required for docmosis
     * @return a string representing the api request to docmosis
     */
    public static String getReplyDocumentRequest(CaseData caseData, String accessKey) throws JsonProcessingException {
        GenericTseApplicationType selectedApplication = getSelectedApplication(caseData);
        ensureSelectedApplicationExists(selectedApplication);

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
     *
     * @param caseDetails contains all the case data
     * @param document    TSE Reply.pdf
     * @return Personalisation For Response
     * @throws NotificationClientException Throw Exception
     */
    public static Map<String, Object> getPersonalisationForResponse(CaseDetails caseDetails, byte[] document,
                                                                    String citizenUrl)
            throws NotificationClientException {
        CaseData caseData = caseDetails.getCaseData();
        GenericTseApplicationType selectedApplication = getSelectedApplication(caseData);
        ensureSelectedApplicationExists(selectedApplication);

        JSONObject documentJson = NotificationClient.prepareUpload(document, false, true, "52 weeks");

        return Map.of(
                LINK_TO_CITIZEN_HUB, citizenUrl + caseDetails.getCaseId(),
                CASE_NUMBER, caseData.getEthosCaseReference(),
                "applicationType", selectedApplication.getType(),
                "response", isNullOrEmpty(caseData.getTseResponseText()) ? "" : caseData.getTseResponseText(),
                CLAIMANT, caseData.getClaimant(),
                RESPONDENTS, Helper.getRespondentNames(caseData),
                "linkToDocument", documentJson
        );
    }

    public static Map<String, Object> getPersonalisationForAcknowledgement(CaseDetails caseDetails, String exuiUrl) {
        CaseData caseData = caseDetails.getCaseData();
        GenericTseApplicationType selectedApplication = getSelectedApplication(caseData);
        ensureSelectedApplicationExists(selectedApplication);

        return Map.of(
                CASE_NUMBER, caseData.getEthosCaseReference(),
                CLAIMANT, caseData.getClaimant(),
                RESPONDENTS, Helper.getRespondentNames(caseData),
                "shortText", selectedApplication.getType(),
                LINK_TO_EXUI, exuiUrl + caseDetails.getCaseId()
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
     * Format Rule92 No Given details markup.
     *
     * @param copyToOtherPartyYesOrNo Rule 92 Yes or No
     * @param copyToOtherPartyText    Give details
     * @return Markup String
     */
    public static String formatRule92(String copyToOtherPartyYesOrNo, String copyToOtherPartyText) {
        return String.format(
                RULE92_YES_OR_NO_MARKUP,
                copyToOtherPartyYesOrNo,
                NO.equals(copyToOtherPartyYesOrNo)
                        ? String.format(
                        RULE92_DETAILS_MARKUP,
                        copyToOtherPartyText
                )
                        : ""
        );
    }

    private static void ensureSelectedApplicationExists(GenericTseApplicationType genericTseApplicationType) {
        if (genericTseApplicationType == null) {
            throw new IllegalStateException("Selected application is null");
        }
    }
}
package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationType;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.TseRespondTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.documents.TseReplyData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.documents.TseReplyDocument;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.DocumentUtil;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLOSED_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NEW_DATE_PATTERN;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.RESPONDENT_TITLE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.et.common.model.ccd.types.citizenhub.ClaimantTse.CY_RESPONDING_TO_APP_TYPE_MAP;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.APPLICATION_TYPE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.CASE_NUMBER;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.CLAIMANT;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.LINK_TO_CITIZEN_HUB;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.LINK_TO_EXUI;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.RESPONDENTS;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.SHORT_TEXT;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.WELSH_LANGUAGE_PARAM;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TableMarkupConstants.TABLE_STRING;

@Slf4j
public final class TseHelper {

    private static final String INTRO = """
            <p>The %s has applied to <strong>%s</strong>.</p>
            %s
            <p>If you have any objections or responses to their application you must send them to the tribunal as soon
            as possible and by <strong>%s</strong> at the latest.
            
            If you need more time to respond, you may request more time from the tribunal. If you do not respond or
            request more time to respond, the tribunal will consider the application without your response.</p>
            """;
    private static final String TSE_RESPONSE_TABLE = "| | |\r\n"
            + TABLE_STRING
            + "|Application date | %s\r\n"
            + "|Details of the application | %s\r\n"
            + "Application file upload | %s";
    private static final String GROUP_B = "<p>You do not need to respond to this application.</p>";
    private static final List<String> GROUP_B_TYPES = List.of("Change my personal details", "Consider a decision "
            + "afresh", "Reconsider a judgment", "Withdraw my claim");

    private static final String REPLY_OUTPUT_NAME = "%s Reply.pdf";
    private static final String REPLY_TEMPLATE_NAME = "EM-TRB-EGW-ENG-01212.docx";

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
                        && (isNoRespondentReply(o.getValue().getRespondCollection())
                        && YES.equals(o.getValue().getCopyToOtherPartyYesOrNo())
                        || hasTribunalResponse(o.getValue()))
                )
                .map(TseHelper::formatDropdownOption)
                .toList());
    }

    private static boolean isNoRespondentReply(List<TseRespondTypeItem> tseRespondTypeItems) {
        return CollectionUtils.isEmpty(tseRespondTypeItems)
                || tseRespondTypeItems.stream().noneMatch(r -> RESPONDENT_TITLE.equals(r.getValue().getFrom()));
    }

    /**
     * Check if there is any request/order from Tribunal that requires Respondent to respond to.
     */
    private static boolean hasTribunalResponse(GenericTseApplicationType application) {
        return YES.equals(application.getRespondentResponseRequired());
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
        if (CollectionUtils.isEmpty(caseData.getGenericTseApplicationCollection())) {
            return;
        }

        GenericTseApplicationType genericTseApplicationType = getRespondentSelectedApplicationType(caseData);
        assert genericTseApplicationType != null;

        LocalDate date = LocalDate.parse(genericTseApplicationType.getDate(), NEW_DATE_PATTERN);

        caseData.setTseResponseIntro(
                String.format(
                        INTRO,
                        StringUtils.lowerCase(genericTseApplicationType.getApplicant()),
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
     * Gets the admin select application in GenericTseApplicationType.
     *
     * @param caseData contains all the case data
     * @return the select application in GenericTseApplicationType
     */
    public static GenericTseApplicationType getAdminSelectedApplicationType(CaseData caseData) {
        return getTseApplication(caseData, caseData.getTseAdminSelectApplication().getSelectedCode());
    }

    /**
     * Gets the view select application in GenericTseApplicationType.
     *
     * @param caseData contains all the case data
     * @return the select application in GenericTseApplicationType
     */
    public static GenericTseApplicationType getViewSelectedApplicationType(CaseData caseData) {
        return getTseApplication(caseData, caseData.getTseViewApplicationSelect().getSelectedCode());
    }

    /**
     * Gets the respondent select application in GenericTseApplicationType.
     *
     * @param caseData contains all the case data
     * @return the select application in GenericTseApplicationType
     */
    public static GenericTseApplicationType getRespondentSelectedApplicationType(CaseData caseData) {
        return getTseApplication(caseData, caseData.getTseRespondSelectApplication().getSelectedCode());
    }

    @Nullable
    private static GenericTseApplicationType getTseApplication(CaseData caseData, String selectedAppId) {

        return caseData.getGenericTseApplicationCollection().stream()
            .filter(item -> item.getValue().getNumber().equals(selectedAppId))
            .findFirst()
            .map(GenericTseApplicationTypeItem::getValue)
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
        GenericTseApplicationType selectedApplication = getRespondentSelectedApplicationType(caseData);
        assert selectedApplication != null;

        TseReplyData data = createDataForTseReply(caseData, selectedApplication);
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

    public static Map<String, Object> getPersonalisationForResponse(CaseDetails caseDetails,
                                                                    byte[] document,
                                                                    String citizenUrl,
                                                                    boolean isWelsh)
            throws NotificationClientException {
        CaseData caseData = caseDetails.getCaseData();
        GenericTseApplicationType selectedApplication = getRespondentSelectedApplicationType(caseData);
        assert selectedApplication != null;
        String linkToCitizenHub = isWelsh
                ? citizenUrl + WELSH_LANGUAGE_PARAM
                : citizenUrl;
        String applicationType = isWelsh
                ? CY_RESPONDING_TO_APP_TYPE_MAP.get(selectedApplication.getType())
                : selectedApplication.getType();

        JSONObject documentJson = NotificationClient.prepareUpload(document, false, true, "52 weeks");

        return Map.of(
                LINK_TO_CITIZEN_HUB, linkToCitizenHub,
                CASE_NUMBER, caseData.getEthosCaseReference(),
                APPLICATION_TYPE, applicationType,
                "response", isNullOrEmpty(caseData.getTseResponseText()) ? "" : caseData.getTseResponseText(),
                CLAIMANT, caseData.getClaimant(),
                RESPONDENTS, Helper.getRespondentNames(caseData),
                "linkToDocument", documentJson
        );
    }

    public static Map<String, Object> getPersonalisationForAcknowledgement(CaseDetails caseDetails, String exuiUrl) {
        CaseData caseData = caseDetails.getCaseData();
        GenericTseApplicationType selectedApplication = getRespondentSelectedApplicationType(caseData);
        assert selectedApplication != null;

        return Map.of(
                CASE_NUMBER, caseData.getEthosCaseReference(),
                CLAIMANT, caseData.getClaimant(),
                RESPONDENTS, Helper.getRespondentNames(caseData),
                SHORT_TEXT, selectedApplication.getType(),
                LINK_TO_EXUI, exuiUrl
        );
    }

    private static TseReplyData createDataForTseReply(CaseData caseData, GenericTseApplicationType application) {

        return TseReplyData.builder()
            .caseNumber(defaultIfEmpty(caseData.getEthosCaseReference(), null))
            .respondentParty(RESPONDENT_TITLE)
            .type(defaultIfEmpty(application.getType(), null))
            .responseDate(UtilHelper.formatCurrentDate(LocalDate.now()))
            .response(defaultIfEmpty(application.getDetails(), null))
            .supportingYesNo(replyHasSupportingDocs(caseData.getTseResponseSupportingMaterial()))
            .documentCollection(getUploadedDocList(caseData))
            .copy(defaultIfEmpty(application.getCopyToOtherPartyYesOrNo(), null))
            .build();
    }

    private static List<GenericTypeItem<DocumentType>> getUploadedDocList(CaseData caseData) {
        if (caseData.getTseResponseSupportingMaterial() == null) {
            return null;
        }

        return DocumentUtil.generateUploadedDocumentListFromDocumentList(caseData.getTseResponseSupportingMaterial());
    }

    private static String replyHasSupportingDocs(List<GenericTypeItem<DocumentType>> supportDocList) {
        return supportDocList != null && !supportDocList.isEmpty() ? "Yes" : "No";
    }
}

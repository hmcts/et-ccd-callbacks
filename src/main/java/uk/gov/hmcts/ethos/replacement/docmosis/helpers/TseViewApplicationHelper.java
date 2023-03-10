package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationType;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.TseRespondTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.TseRespondType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ADMIN;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CASE_MANAGEMENT_ORDER;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLOSED_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.REQUEST;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.TseHelper.formatRule92;

@Slf4j
@SuppressWarnings({"PMD.ExcessiveImports", "PMD.TooManyMethods"})
public final class TseViewApplicationHelper {

    private static final String STRING_BR = "<br>";

    private static final String RESPONDENT_REPLY_MARKUP_FOR_REPLY = "|Response %s | |\r\n"
            + "|--|--|\r\n"
            + "|Response from | %s|\r\n"
            + "|Response date | %s|\r\n"
            + "|What’s your response to the %s’s application? | %s|\r\n"
            + "|Supporting material | %s|\r\n"
            + "%s" // Rule92
            + "\r\n";
    private static final String VIEW_APPLICATION_ADMIN_REPLY_MARKUP = "|Response %s | |\r\n"
            + "|--|--|\r\n"
            + "%s" // response title
            + "|Date | %s|\r\n"
            + "|Sent by | Tribunal|\r\n"
            + "|Case management order or request? | %s|\r\n"
            + "%s" // response due
            + "%s" // party or parties to respond
            + "%s" // Additional information
            + "|Supporting material | %s|\r\n"
            + "%s" // made by
            + "%s" // name / author
            + "|Sent to | %s|\r\n"
            + "\r\n";

    private static final String PARTY_OR_PARTIES_TO_RESPOND = "|Party or parties to respond | %s|\r\n";
    private static final String RESPONSE_DUE = "|Response due | %s|\r\n";
    private static final String NAME_OF_ADMIN_RESPONSE_AUTHOR = "|Name | %s|\r\n";
    private static final String ADMIN_RESPONSE_TITLE = "|Response | %s|\r\n";
    private static final String ADMIN_RESPONSE_ADDITIONAL_INFORMATION = "|Additional information | %s|\r\n";
    private static final String RESPONSE_LIST_TITLE = "|Responses | |\r\n"
            + "|--|--|\r\n"
            + "\r\n";

    private static final String APPLICATION_DETAILS = "|Application | |\r\n"
            + "|--|--|\r\n"
            + "|Applicant | %s|\r\n"
            + "|Type of application | %s|\r\n"
            + "|Application date | %s|\r\n"
            + "|What do you want to tell or ask the tribunal? | %s|\r\n"
            + "|Supporting material | %s|\r\n"
            + "|Do you want to copy this correspondence to the other party "
            + "to satisfy the Rules of Procedure? | %s |\r\n"
            + "\r\n";
    private static final String ADMIN_REPLY_MARKUP_MADE_BY = "|%s made by | %s|\r\n";
    private static final String DOCUMENT_LINK = "<a href=\"/documents/%s\" target=\"_blank\">%s</a>";

    private TseViewApplicationHelper() {
        // Access through static methods
    }

    /**
     * Populates a dynamic list with either open or closed applications
     * for the tell something else 'view an application' dropdown selector.
     * @param caseData - the caseData contains the values for the case
     * @return DynamicFixedListType
     */
    public static DynamicFixedListType populateOpenOrClosedApplications(CaseData caseData) {

        if (CollectionUtils.isEmpty(caseData.getGenericTseApplicationCollection())) {
            return null;
        }

        boolean selectedClosed = CLOSED_STATE.equals(caseData.getTseViewApplicationOpenOrClosed());

        return DynamicFixedListType.from(caseData.getGenericTseApplicationCollection().stream()
                .filter(o -> selectedClosed ? o.getValue().getStatus().equals(CLOSED_STATE)
                        : !o.getValue().getStatus().equals(CLOSED_STATE))
                .map(TseViewApplicationHelper::formatDropdownOption)
                .collect(Collectors.toList()));
    }

    /**
     * Set the markup for an application summary and a table of responses
     * for the 'view an application' event.
     * @param caseData - all case data for the case
     */
    public static void setDataForTseApplicationSummaryAndResponses(CaseData caseData) {
        List<GenericTseApplicationTypeItem> applications = caseData.getGenericTseApplicationCollection();
        if (CollectionUtils.isEmpty(applications) || getChosenApplication(caseData) == null) {
            return;
        }
        GenericTseApplicationType genericTseApplicationType = getChosenApplication(caseData);
        String document = "N/A";
        if (genericTseApplicationType.getDocumentUpload() != null) {
            document = createLinkForUploadedDocument(genericTseApplicationType.getDocumentUpload());
        }
        String respondTablesCollection = "";
        if (!CollectionUtils.isEmpty(genericTseApplicationType.getRespondCollection())) {
            List<TseRespondTypeItem> respondList = genericTseApplicationType.getRespondCollection();
            respondTablesCollection = createResponseTable(respondList, genericTseApplicationType.getApplicant());
        }

        String applicationSummary = String.format(
                APPLICATION_DETAILS, genericTseApplicationType.getApplicant(),
                genericTseApplicationType.getType(),
                genericTseApplicationType.getDate(),
                isNullOrEmpty(genericTseApplicationType.getDetails()) ? "N/A"
                        : genericTseApplicationType.getDetails(),
                document,
                isNullOrEmpty(genericTseApplicationType.getCopyToOtherPartyYesOrNo()) ? "N/A"
                        : genericTseApplicationType.getCopyToOtherPartyYesOrNo()
        );
        
        caseData.setTseApplicationSummaryAndResponsesMarkup(applicationSummary + respondTablesCollection);
    }

    private static String formatAminResponseTitle(TseRespondType reply) {
        if (!isNullOrEmpty(reply.getEnterResponseTitle())) {
            return String.format(
                    ADMIN_RESPONSE_TITLE,
                    reply.getEnterResponseTitle());
        }
        return "";
    }

    private static String formatAdminResponseAdditionalInfo(TseRespondType reply) {
        if (!isNullOrEmpty(reply.getAdditionalInformation())) { //
            return String.format(
                    ADMIN_RESPONSE_ADDITIONAL_INFORMATION,
                    reply.getAdditionalInformation());
        }
        return "";
    }

    private static String formatMadeByFullName(TseRespondType reply) {
        if (!isNullOrEmpty(reply.getMadeByFullName())) { //
            return String.format(
                    NAME_OF_ADMIN_RESPONSE_AUTHOR,
                    reply.getMadeByFullName());
        }
        return "";
    }

    private static String formatResponseDue(TseRespondType reply) {
        if (!isNullOrEmpty(reply.getIsResponseRequired())) { //
            return String.format(
                    RESPONSE_DUE,
                    reply.getIsResponseRequired());
        }
        return "";
    }

    private static String formatPartyOrPartiesToRespond(TseRespondType reply) {
        if (!isNullOrEmpty(reply.getSelectPartyRespond())) { //
            return String.format(
                    PARTY_OR_PARTIES_TO_RESPOND,
                    reply.getSelectPartyRespond());
        }
        return "";
    }

    private static String formatRespondentResponse(TseRespondType reply) {
        return isNullOrEmpty(reply.getResponse()) ? "N/A"
                : defaultString(reply.getResponse());
    }

    private static String formatReasonForNotSharing(TseRespondType reply) {
        return isNullOrEmpty(reply.getCopyNoGiveDetails()) ? "N/A"
                : defaultString(reply.getCopyNoGiveDetails());
    }

    private static DynamicValueType formatDropdownOption(GenericTseApplicationTypeItem genericTseApplicationTypeItem) {
        GenericTseApplicationType value = genericTseApplicationTypeItem.getValue();
        return DynamicValueType.create(value.getNumber(), String.format("%s %s", value.getNumber(), value.getType()));
    }

    private static GenericTseApplicationType getChosenApplication(CaseData caseData) {
        return caseData.getGenericTseApplicationCollection()
                .get(Integer.parseInt(caseData.getTseViewApplicationSelect().getValue().getCode()) - 1).getValue();
    }

    private static String getDocumentUrls(TseRespondType tseRespondType) {
        if (tseRespondType.getSupportingMaterial() != null) {
            return tseRespondType.getSupportingMaterial().stream()
                    .map(doc -> createLinkForUploadedDocument(doc.getValue().getUploadedDocument())
                            + STRING_BR)
                    .collect(Collectors.joining(""));
        }
        return null;
    }

    private static String createLinkForUploadedDocument(UploadedDocumentType document) {
        Pattern pattern = Pattern.compile("^.+?/documents/");
        Matcher matcher = pattern.matcher(document.getDocumentBinaryUrl());
        String documentLink = matcher.replaceFirst("");
        String documentName = document.getDocumentFilename();
        return String.format(DOCUMENT_LINK, documentLink, documentName);
    }

    private static String createAdminResponse(TseRespondType response, AtomicInteger count) {
        String doc = "N/A";
        if (response.getAddDocument() != null) {
            doc = createLinkForUploadedDocument(response.getAddDocument());
        }
        return String.format(
                VIEW_APPLICATION_ADMIN_REPLY_MARKUP,
                count.get(),
                formatAminResponseTitle(response),
                response.getDate(),
                defaultString(response.getIsCmoOrRequest()),
                formatResponseDue(response),
                formatPartyOrPartiesToRespond(response),
                formatAdminResponseAdditionalInfo(response),
                doc,
                formatAdminReplyMadeBy(response),
                formatMadeByFullName(response),
                defaultString(response.getSelectPartyNotify()));
    }

    private static String createRespondentOrClaimantResponse(TseRespondType response,
                                                             AtomicInteger count, String applicant) {
        String doc = "N/A";

        String links = getDocumentUrls(response);
        if (links != null) {
            doc = links;
        }
        return String.format(
                RESPONDENT_REPLY_MARKUP_FOR_REPLY,
                count.get(),
                response.getFrom(),
                response.getDate(),
                applicant.toLowerCase(Locale.ENGLISH),
                formatRespondentResponse(response),
                doc,
                formatRule92(response.getCopyToOtherParty(), formatReasonForNotSharing(response)));
    }

    private static String createResponseTable(List<TseRespondTypeItem> respondList, String applicant) {
        AtomicInteger count = new AtomicInteger(0);
        return RESPONSE_LIST_TITLE + respondList.stream().map((TseRespondTypeItem response) -> {
            count.getAndIncrement();
            if (ADMIN.equals(response.getValue().getFrom())) {
                return createAdminResponse(response.getValue(), count);
            }
            return createRespondentOrClaimantResponse(response.getValue(), count, applicant);
        }).collect(Collectors.joining(""));
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

}

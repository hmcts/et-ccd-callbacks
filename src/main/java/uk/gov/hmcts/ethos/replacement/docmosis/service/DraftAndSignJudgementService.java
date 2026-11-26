package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.UploadedDocument;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DraftAndSignJudgement;
import uk.gov.service.notify.NotificationClientException;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.CASE_NUMBER;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.CLAIMANT;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.LINK_TO_DOCUMENT;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.RESPONDENT_NAMES;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.SUBMITTED_DATE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.USER_NAME;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper.getRespondentNames;
import static uk.gov.service.notify.NotificationClient.prepareUpload;

@Slf4j
@Service
@RequiredArgsConstructor
public class DraftAndSignJudgementService {

    public static final String NOT_FOR_PUBLICATION = "notForPublication";
    public static final String ERROR_MSG_MISSING_NON_PUBLICATION_REASON = "Reason for non-publication is required";
    private static final String ERROR_MSG_MISSING_CASE_DETAILS = "Case details is missing";
    private static final String ERROR_MSG_MISSING_USER_TOKEN = "User token is missing";
    private static final String ERROR_MSG_MISSING_CASE_DATA = "Case data is missing or incomplete";
    private static final String ERROR_MSG_MISSING_DRAFT_AND_SIGN = "Draft and sign judgement is missing";
    private static final String ERROR_MSG_MISSING_DOC = "Draft and sign judgement document is missing";
    private static final String ERROR_MSG_MORE_THAN_ONE_DOC =
        "Expected exactly one draft and sign judgement document, but found multiple";
    private static final String ERROR_MSG_DOWNLOAD_FILE = "Downloaded document is null or has no content";
    private static final String ERROR_MSG_GET_INPUT_STREAM = "Error getting document input stream binary content";
    private static final String ERROR_MSG_EMPTY_DOC_CONTENT = "Document content is empty";

    private final EmailService emailService;
    private final ReportDataService reportDataService;
    private final DocumentManagementService documentManagementService;

    @Value("${template.draftAndSignJudgement.forFindCaseLaw}")
    private String forFindCaseLawEmailTemplateId;
    @Value("${notifications.draftAndSignFindCaseLawEmail}")
    private String emailAddressToSend;

    /**
     * Validates that a non-publication reason is provided when the judgment is marked
     * as not to be published on Find Case Law.
     * @param caseData Case data containing draft and sign judgement details
     * @return List of validation error messages, or empty list if valid
     */
    public List<String> validateNonPublicationReason(CaseData caseData) {
        if (caseData == null || ObjectUtils.isEmpty(caseData.getDraftAndSignJudgement())) {
            return List.of();
        }
        DraftAndSignJudgement judgement = caseData.getDraftAndSignJudgement();
        if (YES.equals(judgement.getIsJudgement())
            && isNotForPublication(judgement)
            && StringUtils.isBlank(judgement.getNonPublicationReason())) {
            return List.of(ERROR_MSG_MISSING_NON_PUBLICATION_REASON);
        }
        return List.of();
    }

    /**
     * Checks if the Draft and Sign Judgement is a judgment and is suitable for Find Case Law publication
     * (i.e. not marked as excluded from publication).
     * If so, sends an email notification to the Find Case Law email address.
     * @param caseDetails Details on the case
     * @param userToken User token for the request
     * @throws NotificationClientException if there is an error in prepareUpload
     */
    public void checkAndSendEmails(CaseDetails caseDetails, String userToken) throws NotificationClientException {
        if (caseDetails == null) {
            throw new IllegalStateException(ERROR_MSG_MISSING_CASE_DETAILS);
        }

        if (StringUtils.isBlank(userToken)) {
            throw new IllegalStateException(ERROR_MSG_MISSING_USER_TOKEN);
        }

        CaseData caseData = caseDetails.getCaseData();
        if (caseData == null
            || StringUtils.isBlank(caseData.getEthosCaseReference())
            || StringUtils.isBlank(caseData.getClaimant())) {
            throw new IllegalStateException(ERROR_MSG_MISSING_CASE_DATA);
        }

        DraftAndSignJudgement judgement = caseData.getDraftAndSignJudgement();
        if (ObjectUtils.isEmpty(judgement)) {
            throw new IllegalStateException(ERROR_MSG_MISSING_DRAFT_AND_SIGN);
        }
        if (YES.equals(judgement.getIsJudgement()) && !isNotForPublication(judgement)) {
            emailService.sendEmail(
                forFindCaseLawEmailTemplateId,
                emailAddressToSend,
                prepareEmailContent(caseDetails, judgement, userToken)
            );
        }
    }

    private boolean isNotForPublication(DraftAndSignJudgement judgement) {
        return CollectionUtils.isNotEmpty(judgement.getNotForPublication())
            && judgement.getNotForPublication().contains(NOT_FOR_PUBLICATION);
    }

    private Map<String, Object> prepareEmailContent(
        CaseDetails caseDetails,
        DraftAndSignJudgement judgement,
        String userToken
    ) throws NotificationClientException {
        CaseData caseData = caseDetails.getCaseData();
        return Map.of(
            CASE_NUMBER, caseData.getEthosCaseReference(),
            CLAIMANT, caseData.getClaimant(),
            RESPONDENT_NAMES, getRespondentNames(caseData),
            SUBMITTED_DATE, UtilHelper.formatCurrentDate(LocalDate.now()),
            USER_NAME, reportDataService.getUserFullName(userToken),
            LINK_TO_DOCUMENT, getLinkToDocument(judgement, userToken)
        );
    }

    private JSONObject getLinkToDocument(
        DraftAndSignJudgement judgement,
        String userToken
    ) throws NotificationClientException {
        if (CollectionUtils.isEmpty(judgement.getDraftAndSignJudgementDocuments())) {
            throw new IllegalStateException(ERROR_MSG_MISSING_DOC);
        }

        if (judgement.getDraftAndSignJudgementDocuments().size() > 1) {
            throw new IllegalStateException(ERROR_MSG_MORE_THAN_ONE_DOC);
        }

        byte[] docContent =  getDocumentContent(
            judgement.getDraftAndSignJudgementDocuments().getLast(),
            userToken
        );
        return prepareUpload(docContent);
    }

    private byte[] getDocumentContent(DocumentTypeItem docItem, String userToken) {
        if (docItem == null
            || docItem.getValue() == null
            || docItem.getValue().getUploadedDocument() == null
            || StringUtils.isBlank(docItem.getValue().getUploadedDocument().getDocumentBinaryUrl())) {
            throw new IllegalStateException(ERROR_MSG_DOWNLOAD_FILE);
        }

        UploadedDocument uploadedDoc = documentManagementService.downloadFile(
            userToken,
            docItem.getValue().getUploadedDocument().getDocumentBinaryUrl()
        );
        if (uploadedDoc == null || uploadedDoc.getContent() == null) {
            throw new IllegalStateException(ERROR_MSG_DOWNLOAD_FILE);
        }

        byte[] content;
        try {
            content = uploadedDoc.getContent().getInputStream().readAllBytes();
        } catch (IOException ioException) {
            throw new IllegalStateException(ERROR_MSG_GET_INPUT_STREAM, ioException);
        }

        if (content.length == 0) {
            throw new IllegalStateException(ERROR_MSG_EMPTY_DOC_CONTENT);
        }
        return content;
    }
}

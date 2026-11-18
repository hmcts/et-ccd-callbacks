package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.util.Map;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.CASE_NUMBER;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.CLAIMANT;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.LINK_TO_DOCUMENT;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.REASON_FOR_PUBLICATION;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.RESPONDENT_NAMES;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.SUBMITTED_DATE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.USER_NAME;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper.getRespondentNames;
import static uk.gov.service.notify.NotificationClient.prepareUpload;

@Slf4j
@Service
@RequiredArgsConstructor
public class DraftAndSignJudgementService {

    private static final String ERROR_MSG_DOWNLOAD_DOC = "Source: DraftAndSignJudgementService, "
        + "method: getUploadedDocumentContent, "
        + "Error downloading uploaded document binary content: {}";

    private final EmailService emailService;
    private final ReportDataService reportDataService;
    private final DocumentManagementService documentManagementService;

    @Value("${template.draftAndSignJudgement.forFindCaseLaw}")
    private String forFindCaseLawEmailTemplateId;
    @Value("${email.forFindCaseLaw}")
    private String emailAddressToSend;

    public void checkAndSendEmails(CaseDetails caseDetails, String userToken) throws NotificationClientException {
        CaseData caseData = caseDetails.getCaseData();
        DraftAndSignJudgement judgement = caseData.getDraftAndSignJudgement();

        if (isNotEmpty(judgement)
            && YES.equals(judgement.getIsJudgement())
            && YES.equals(judgement.getShouldForPublication())) {
            emailService.sendEmail(
                forFindCaseLawEmailTemplateId,
                emailAddressToSend,
                prepareEmailContent(caseDetails, judgement, userToken)
            );
        }
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
            REASON_FOR_PUBLICATION, judgement.getShouldForPublicationReason(),
            SUBMITTED_DATE, UtilHelper.formatCurrentDate(LocalDate.now()),
            USER_NAME, reportDataService.getUserFullName(userToken),
            LINK_TO_DOCUMENT, getLinkToDocument(judgement, userToken)
        );
    }

    private JSONObject getLinkToDocument(
        DraftAndSignJudgement judgement,
        String userToken
    ) throws NotificationClientException {
        byte[] docContent =  getUploadedDocumentContent(
            judgement.getDraftAndSignJudgementDocuments().getLast(),
            userToken
        );
        return prepareUpload(docContent);
    }

    private byte[] getUploadedDocumentContent(DocumentTypeItem docItem, String userToken) {
        byte[] uploadedDocContent = null;
        try {
            UploadedDocument uploadedDoc = documentManagementService.downloadFile(
                userToken,
                docItem.getValue().getUploadedDocument().getDocumentBinaryUrl()
            );
            if (uploadedDoc != null) {
                uploadedDocContent = uploadedDoc.getContent().getInputStream().readAllBytes();
            }
        } catch (IOException ioException) {
            log.error(ERROR_MSG_DOWNLOAD_DOC, ioException.getMessage());
        }
        return uploadedDocContent;
    }
}

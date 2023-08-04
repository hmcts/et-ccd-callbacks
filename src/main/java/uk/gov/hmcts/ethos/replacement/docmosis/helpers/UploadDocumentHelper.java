package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantIndType;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static com.google.common.base.Strings.isNullOrEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.REJECTED_STATE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.CASE_NUMBER;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.CCD_ID;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.DocumentConstants.ACAS_CERTIFICATE;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.DocumentConstants.CLAIM_ACCEPTED;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.DocumentConstants.CLAIM_REJECTED;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.DocumentConstants.ET1;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.DocumentConstants.ET1_ATTACHMENT;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.DocumentConstants.ET3;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.DocumentConstants.ET3_ATTACHMENT;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.DocumentConstants.HEARINGS;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.DocumentConstants.LEGACY_DOCUMENT_NAMES;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.DocumentConstants.NOTICE_OF_A_CLAIM;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.DocumentConstants.NOTICE_OF_CLAIM;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.DocumentConstants.NOTICE_OF_HEARING;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.DocumentConstants.REJECTION_OF_CLAIM;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.DocumentConstants.RESPONSE_TO_A_CLAIM;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.DocumentConstants.STARTING_A_CLAIM;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.DocumentConstants.TRIBUNAL_CORRESPONDENCE;

/**
 * Helper methods for the Upload Documents event.
 */
@Slf4j
public final class UploadDocumentHelper {

    private UploadDocumentHelper() {
        // Access through static methods
    }

    /**
     * Returns true if the case is rejected, has a rejection of claim document and has not got an email sent flag set.
     * @param caseDetails details of the case
     */
    public static boolean shouldSendRejectionEmail(CaseDetails caseDetails) {
        CaseData caseData = caseDetails.getCaseData();
        List<DocumentTypeItem> documentCollection = caseData.getDocumentCollection();

        if (CollectionUtils.isEmpty(documentCollection)) {
            return false;
        }

        Optional<DocumentTypeItem> rejectionDocument = documentCollection.stream().filter(o ->
            "Rejection of claim".equals(o.getValue().getTypeOfDocument())).findFirst();

        return REJECTED_STATE.equals(caseDetails.getState())
            && rejectionDocument.isPresent()
            && isNullOrEmpty(caseData.getCaseRejectedEmailSent());
    }

    /**
     * Generates a map of personalised information that will be used for the
     * placeholder fields in the Case Rejected email template.
     * @param caseDetails Contains details about the case.
     */
    public static Map<String, String> buildPersonalisationForCaseRejection(CaseDetails caseDetails) {
        CaseData caseData = caseDetails.getCaseData();
        Map<String, String> personalisation = new ConcurrentHashMap<>();
        personalisation.put(CASE_NUMBER, caseData.getEthosCaseReference());
        personalisation.put("initialTitle", getClaimantTitleOrInitial(caseData));
        personalisation.put("lastName", getLastName(caseData.getClaimant()));
        personalisation.put(CCD_ID, caseDetails.getCaseId());
        return personalisation;
    }

    private static String getClaimantTitleOrInitial(CaseData caseData) {
        ClaimantIndType claimantIndType = caseData.getClaimantIndType();

        if (!isNullOrEmpty(claimantIndType.getClaimantPreferredTitle())) {
            return claimantIndType.getClaimantPreferredTitle();
        }

        if (!isNullOrEmpty(claimantIndType.getClaimantTitle())) {
            return claimantIndType.getClaimantTitle();
        }

        return caseData.getClaimant().substring(0, 1).toUpperCase(Locale.ROOT);
    }

    private static String getLastName(String name) {
        return name.substring(name.lastIndexOf(' ') + 1);
    }

    /**
     * Changes the old documents into the new doc naming convention by checking what the old is and converting it to the
     * new where possible. If it can't find a new doc type version, defaults to a new section called Legacy Document
     * Names where all the preexisting data will sit
     * @param caseData where the data is stored
     */
    public static void convertLegacyDocsToNewDocNaming(CaseData caseData) {
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(caseData.getDocumentCollection())) {
            for (DocumentTypeItem documentTypeItem : caseData.getDocumentCollection()) {
                DocumentType documentType = documentTypeItem.getValue();
                if (isNullOrEmpty(documentType.getTopLevelDocuments())
                        && (!isNullOrEmpty(documentType.getTypeOfDocument()))) {
                    mapLegacyDocTypeToNewDocType(documentType);
                }
            }
        } else {
            caseData.setDocumentCollection(new ArrayList<>());
        }
    }

    private static void mapLegacyDocTypeToNewDocType(DocumentType documentType) {
        switch (documentType.getTypeOfDocument()) {
            case ET1 -> {
                documentType.setTopLevelDocuments(STARTING_A_CLAIM);
                documentType.setStartingClaimDocuments(ET1);
            }
            case ET1_ATTACHMENT -> {
                documentType.setTopLevelDocuments(STARTING_A_CLAIM);
                documentType.setStartingClaimDocuments(ET1_ATTACHMENT);
            }
            case ACAS_CERTIFICATE -> {
                documentType.setTopLevelDocuments(STARTING_A_CLAIM);
                documentType.setStartingClaimDocuments(ACAS_CERTIFICATE);
            }
            case NOTICE_OF_A_CLAIM -> {
                documentType.setTopLevelDocuments(STARTING_A_CLAIM);
                documentType.setStartingClaimDocuments(NOTICE_OF_CLAIM);
            }
            case TRIBUNAL_CORRESPONDENCE -> {
                documentType.setTopLevelDocuments(STARTING_A_CLAIM);
                documentType.setStartingClaimDocuments(CLAIM_ACCEPTED);
            }
            case REJECTION_OF_CLAIM -> {
                documentType.setTopLevelDocuments(STARTING_A_CLAIM);
                documentType.setStartingClaimDocuments(CLAIM_REJECTED);
            }
            case ET3 -> {
                documentType.setTopLevelDocuments(RESPONSE_TO_A_CLAIM);
                documentType.setResponseClaimDocuments(ET3);
            }
            case ET3_ATTACHMENT -> {
                documentType.setTopLevelDocuments(RESPONSE_TO_A_CLAIM);
                documentType.setResponseClaimDocuments(ET3_ATTACHMENT);
            }
            case NOTICE_OF_HEARING -> {
                documentType.setTopLevelDocuments(HEARINGS);
                documentType.setHearingsDocuments(NOTICE_OF_HEARING);
            }
            default -> documentType.setTopLevelDocuments(LEGACY_DOCUMENT_NAMES);
        }
    }

    public static void setDocumentTypeForDocumentCollection(CaseData caseData) {
        if (CollectionUtils.isNotEmpty(caseData.getDocumentCollection())) {
            for (DocumentTypeItem documentTypeItem : caseData.getDocumentCollection()) {
                setDocumentTypeForDocument(documentTypeItem.getValue());
            }
        }
    }

    private static void setDocumentTypeForDocument(DocumentType documentType) {
        if (!isNullOrEmpty(documentType.getTopLevelDocuments()) || !isNullOrEmpty(documentType.getTypeOfDocument())) {
            if (!isNullOrEmpty(documentType.getStartingClaimDocuments())) {
                documentType.setDocumentType(documentType.getStartingClaimDocuments());
            } else if (!isNullOrEmpty(documentType.getResponseClaimDocuments())) {
                documentType.setDocumentType(documentType.getResponseClaimDocuments());
            } else if (!isNullOrEmpty(documentType.getInitialConsiderationDocuments())) {
                documentType.setDocumentType(documentType.getInitialConsiderationDocuments());
            } else if (!isNullOrEmpty(documentType.getCaseManagementDocuments())) {
                documentType.setDocumentType(documentType.getCaseManagementDocuments());
            } else if (!isNullOrEmpty(documentType.getWithdrawalSettledDocuments())) {
                documentType.setDocumentType(documentType.getWithdrawalSettledDocuments());
            } else if (!isNullOrEmpty(documentType.getHearingsDocuments())) {
                documentType.setDocumentType(documentType.getHearingsDocuments());
            } else if (!isNullOrEmpty(documentType.getJudgmentAndReasonsDocuments())) {
                documentType.setDocumentType(documentType.getJudgmentAndReasonsDocuments());
            } else if (!isNullOrEmpty(documentType.getReconsiderationDocuments())) {
                documentType.setDocumentType(documentType.getReconsiderationDocuments());
            } else if (!isNullOrEmpty(documentType.getMiscDocuments())) {
                documentType.setDocumentType(documentType.getMiscDocuments());
            } else {
                documentType.setDocumentType(documentType.getTypeOfDocument());
            }
        }
    }
}

package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import uk.gov.hmcts.ecm.common.helpers.DocumentHelper;
import uk.gov.hmcts.ecm.common.model.helper.DocumentCategory;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantIndType;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.et.common.model.generic.BaseCaseData;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.REJECTED_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.ACAS_CERTIFICATE;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.CLAIM_ACCEPTED;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.CLAIM_REJECTED;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.ET1;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.ET1_ATTACHMENT;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.ET3;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.ET3_ATTACHMENT;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.HEARINGS;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.INITIAL_CONSIDERATION;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.LEGACY_DOCUMENT_NAMES;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.MISC;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.NOTICE_OF_A_CLAIM;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.NOTICE_OF_CLAIM;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.NOTICE_OF_HEARING;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.REJECTION_OF_CLAIM;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.RESPONSE_TO_A_CLAIM;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.RULE_27_NOTICE;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.RULE_28_NOTICE;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.STARTING_A_CLAIM;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.TRIBUNAL_CASE_FILE;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.TRIBUNAL_CORRESPONDENCE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.RULE_29_NOTICE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.CASE_NUMBER;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.LINK_TO_CITIZEN_HUB;

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
     * @param caseData Contains all the case's data.
     * @param citizenHubLink link to the citizen hub with case id.
     */
    public static Map<String, String> buildPersonalisationForCaseRejection(CaseData caseData, String citizenHubLink) {
        Map<String, String> personalisation = new ConcurrentHashMap<>();
        personalisation.put(CASE_NUMBER, caseData.getEthosCaseReference());
        personalisation.put("initialTitle", getClaimantTitleOrInitial(caseData));
        personalisation.put("lastName", getLastName(caseData.getClaimant()));
        personalisation.put(LINK_TO_CITIZEN_HUB, citizenHubLink);
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
    public static void convertLegacyDocsToNewDocNaming(BaseCaseData caseData) {
        if (CollectionUtils.isNotEmpty(caseData.getDocumentCollection())) {
            for (DocumentTypeItem documentTypeItem : caseData.getDocumentCollection()) {
                DocumentType documentType = documentTypeItem.getValue();
                if (isNullOrEmpty(documentType.getTopLevelDocuments())
                    && !isNullOrEmpty(documentType.getTypeOfDocument())) {
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
            case TRIBUNAL_CASE_FILE -> {
                documentType.setTopLevelDocuments(MISC);
                documentType.setMiscDocuments(TRIBUNAL_CASE_FILE);
            }
            default -> documentType.setTopLevelDocuments(LEGACY_DOCUMENT_NAMES);
        }
    }

    public static void setDocumentTypeForDocumentCollection(BaseCaseData caseData) {
        if (CollectionUtils.isEmpty(caseData.getDocumentCollection())) {
            return;
        }
        caseData.getDocumentCollection().forEach(documentTypeItem -> {
            DocumentType documentType = documentTypeItem.getValue();
            DocumentHelper.setDocumentTypeForDocument(documentType);
            setDocumentTypeForInitialConsiderationRuleChanges(documentType);

            if (!ObjectUtils.isEmpty(documentType.getUploadedDocument())) {
                UploadedDocumentType uploadedDocumentType = documentType.getUploadedDocument();
                uploadedDocumentType.setCategoryId(
                        DocumentCategory.getIdFromCategory(documentType.getDocumentType()));
                documentType.setUploadedDocument(uploadedDocumentType);
            }

            documentType.setDocNumber(
                    String.valueOf(caseData.getDocumentCollection().indexOf(documentTypeItem) + 1));
        });

    }

    private static void setDocumentTypeForInitialConsiderationRuleChanges(DocumentType documentType) {
        if (INITIAL_CONSIDERATION.equals(defaultIfEmpty(documentType.getTopLevelDocuments(), ""))) {
            if (RULE_27_NOTICE.equals(defaultIfEmpty(documentType.getInitialConsiderationDocuments(), ""))) {
                documentType.setDocumentType(RULE_28_NOTICE);
            } else if (RULE_28_NOTICE.equals(defaultIfEmpty(documentType.getInitialConsiderationDocuments(), ""))) {
                documentType.setDocumentType(RULE_29_NOTICE);
            }
        }

    }
}

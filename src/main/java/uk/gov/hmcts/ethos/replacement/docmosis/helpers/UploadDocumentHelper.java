package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantIndType;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static com.google.common.base.Strings.isNullOrEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.REJECTED_STATE;
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
}

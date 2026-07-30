package uk.gov.hmcts.ethos.replacement.docmosis.service.multiples;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.ecm.common.model.servicebus.CreateUpdatesMsg;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.multiples.AdditionalClaimant;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.et.common.model.multiples.MultipleRequest;
import uk.gov.hmcts.et.common.model.multiples.SubmitMultipleEvent;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.ClaimantContactDetailsPdfHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.DocumentManagementService;
import uk.gov.hmcts.reform.et.syaapi.service.multiples.MultiplesCaseService;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import static org.springframework.http.MediaType.APPLICATION_PDF_VALUE;

@Slf4j
@RequiredArgsConstructor
@Service
public class ClaimantContactDetailsDocumentService {

    private static final String CLAIMANT_CONTACT_DETAILS_PDF = "Claimant Contact Details.pdf";
    private static final String CLAIMANT_CONTACT_DETAILS_PDF_CY = "[W]Claimant Contact Details.pdf";
    private static final int ADDITIONAL_CLAIMANTS_DISPLAY_LIMIT = 5;
    private final DocumentManagementService documentManagementService;
    private final CcdClient ccdClient;
    private final MultiplesCaseService multiplesCaseService;

    @Autowired
    public ClaimantContactDetailsDocumentService(CcdClient ccdClient,
                                                 DocumentManagementService documentManagementService,
                                                 MultiplesCaseService multiplesCaseService) {
        this.ccdClient = ccdClient;
        this.documentManagementService = documentManagementService;
        this.multiplesCaseService = multiplesCaseService;
    }

    public void generateAndUploadClaimantContactDetails(String accessToken,
                                                        CreateUpdatesMsg createUpdatesMsg,
                                                        SubmitEvent leadCase,
                                                        SubmitMultipleEvent createdMultiple) {
        if (ObjectUtils.isEmpty(createdMultiple)) {
            log.error("Skipping claimant contact details PDF - multiple shell is null");
            return;
        }

        CaseData leadCaseData = Optional.ofNullable(leadCase)
                .map(SubmitEvent::getCaseData)
                .orElse(null);

        if (leadCaseData == null) {
            log.error("Skipping claimant contact details PDF - lead case or lead case data is null");
            return;
        }

        List<AdditionalClaimant> additionalClaimants = multiplesCaseService
                .getAdditionalClaimantsByMultipleReference(
                        createUpdatesMsg.getCaseTypeId(),
                        createdMultiple.getCaseData().getMultipleReference());

        int claimantCount = additionalClaimants == null ? 0 : additionalClaimants.size();
        if (claimantCount <= ADDITIONAL_CLAIMANTS_DISPLAY_LIMIT) {
            log.error("Skipping claimant contact details PDF - {} additional claimant(s) is within display limit",
                    claimantCount);
            return;
        }

        try {
            byte[] englishPdfBytes = ClaimantContactDetailsPdfHelper.buildPdf(leadCaseData, additionalClaimants);
            byte[] welshPdfBytes = ClaimantContactDetailsPdfHelper.buildPdfWelsh(leadCaseData, additionalClaimants);

            UploadedDocumentType englishDoc = uploadClaimantContactDetailsPdf(
                    accessToken, createUpdatesMsg, englishPdfBytes, CLAIMANT_CONTACT_DETAILS_PDF);
            UploadedDocumentType welshDoc = uploadClaimantContactDetailsPdf(
                    accessToken, createUpdatesMsg, welshPdfBytes, CLAIMANT_CONTACT_DETAILS_PDF_CY);

            String multipleCaseId = String.valueOf(createdMultiple.getCaseId());
            String multipleCaseTypeId = UtilHelper.getBulkCaseTypeId(createUpdatesMsg.getCaseTypeId());
            MultipleRequest amendRequest = ccdClient.startBulkAmendEventForMultiple(
                    accessToken, multipleCaseTypeId, createUpdatesMsg.getJurisdiction(), multipleCaseId);
            MultipleData multipleData = amendRequest.getCaseDetails().getCaseData();
            if (multipleData != null) {
                multipleData.setClaimantContactDetailsDocument(englishDoc);
                multipleData.setClaimantContactDetailsDocumentWelsh(welshDoc);

                ccdClient.submitMultipleEventForCase(
                        accessToken, multipleData, multipleCaseTypeId,
                        createUpdatesMsg.getJurisdiction(), amendRequest, multipleCaseId);

                log.info("Claimant contact details PDFs (En/Cy) stored on multiple case {}", multipleCaseId);
            } else {
                log.error("Failed to update multiple case {} - MultipleData is null", multipleCaseId);
            }
        } catch (Exception e) {
            log.error("Failed to generate claimant contact details PDFs for multiple {}: {}",
                    createdMultiple.getCaseId(), e.getMessage(), e);
        }
    }

    /**
     * Uploads a claimant contact details PDF to Document Management and builds the reference to it.
     *
     * @param accessToken       admin access token
     * @param createUpdatesMsg  provides case type id for the document store upload
     * @param pdfBytes          the PDF content to upload
     * @param fileName          filename to store the document under
     * @return the uploaded document reference, ready to attach to {@link MultipleData}
     */
    private UploadedDocumentType uploadClaimantContactDetailsPdf(String accessToken,
                                                                 CreateUpdatesMsg createUpdatesMsg,
                                                                 byte[] pdfBytes,
                                                                 String fileName) {
        URI docUri = documentManagementService.uploadDocument(
                accessToken, pdfBytes, fileName,
                APPLICATION_PDF_VALUE, createUpdatesMsg.getCaseTypeId());

        String binaryUrl = documentManagementService.generateDownloadableURL(docUri);
        String docUrl = binaryUrl.substring(0, binaryUrl.length() - "/binary".length());

        return UploadedDocumentType.builder()
                .documentBinaryUrl(binaryUrl)
                .documentFilename(fileName)
                .documentUrl(docUrl)
                .build();
    }
}

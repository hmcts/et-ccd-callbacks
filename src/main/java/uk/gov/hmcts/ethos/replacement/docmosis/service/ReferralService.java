package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.exceptions.DocumentManagementException;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.et.common.model.multiples.MultipleDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.ReferralHelper;

import java.io.IOException;

import static com.google.common.base.Strings.isNullOrEmpty;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReferralService {
    private static final String DOCGEN_ERROR = "Failed to generate document for case id: %s";
    
    private final TornadoService tornadoService;
    private final CaseLookupService caseLookupService;
    private final EmailService emailService;

    @Value("${template.referral}")
    private String referralTemplateId;

    /**
     * Uses {@link TornadoService} to generate a pdf to display a summary of data for the created referral.
     * @param caseData in which the referral type is extracted from
     * @param userToken jwt used for authorization
     * @param caseTypeId e.g. ET_EnglandWales
     * @return {@link DocumentInfo} object populated with pdf data
     */
    public DocumentInfo generateCRDocument(CaseData caseData, String userToken, String caseTypeId) {
        try {
            return tornadoService.generateEventDocument(caseData, userToken,
                caseTypeId, "Referral Summary.pdf");
        } catch (Exception e) {
            throw new DocumentManagementException(String.format(DOCGEN_ERROR, caseData.getEthosCaseReference()), e);
        }
    }

    /**
     * Generates referral summary pdf.
     * @param caseData information about the cases for which a referral summary will be generated.
     * @param leadCase the lead case that will serve as a reference for the document generation.
     * @param userJwt The JWT token of the user who is requesting to generate the document.
     * @param caseTypeId The ID of the type of case for which the referral summary should be generated.
     * @return A DocumentInfo object representing the generated document.
     */
    public DocumentInfo generateDocument(MultipleData caseData, CaseData leadCase, String userJwt, String caseTypeId) {
        try {
            var doc = ReferralHelper.getDocumentRequest(caseData, leadCase, caseTypeId);
            return tornadoService.generateDocument(userJwt, doc, "Referral Summary.pdf", caseTypeId);
        } catch (Exception e) {
            throw new DocumentManagementException(String.format(DOCGEN_ERROR, caseData.getMultipleReference()), e);
        }
    }

    /**
     * Looks up the lead case and uses {@link ReferralHelper} to populate the hearing details.
     * @param data Multiple case type case data
     * @param singleCaseTypeId e.g. ET_EnglandWales
     * @return HTML formatted string of hearing details
     */
    public String populateHearingDetailsFromLeadCase(MultipleData data, String singleCaseTypeId) throws IOException {
        String leadId = data.getLeadCaseId();

        if (isNullOrEmpty(leadId)) {
            throw new IllegalArgumentException("Lead case id is null");
        }

        CaseData caseData = caseLookupService.getCaseDataAsAdmin(singleCaseTypeId, leadId);
        return ReferralHelper.populateHearingDetails(caseData);
    }

    public void sendEmail(MultipleDetails details, CaseData leadCase, String refNumber, boolean isNew, String name) {
        MultipleData caseData = details.getCaseData();
        if (StringUtils.isEmpty(caseData.getReferentEmail())) {
            log.error("No email given");
            return;
        }

        String caseLink = emailService.getExuiCaseLink(details.getCaseId());
        log.error("Sending email to " + caseData.getReferentEmail());
        emailService.sendEmail(
                referralTemplateId,
                caseData.getReferentEmail(),
                ReferralHelper.buildPersonalisation(caseData, leadCase, refNumber, isNew, name, caseLink)
        );
    }
}

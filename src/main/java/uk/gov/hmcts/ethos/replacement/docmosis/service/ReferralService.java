package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.exceptions.DocumentManagementException;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.ReferralHelper;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReferralService {
    private static final String DOCGEN_ERROR = "Failed to generate document for case id: %s";

    private final EmailService emailService;
    private final UserService userService;
    private final TornadoService tornadoService;

    @Value("${referral.template.id}")
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

    public void sendEmail(CaseDetails caseDetails, String referralNumber, boolean isNew, String userToken) {
        CaseData caseData = caseDetails.getCaseData();
        emailService.sendEmail(
            referralTemplateId,
            caseData.getReferentEmail(),
            ReferralHelper.buildPersonalisation(
                caseData,
                referralNumber,
                isNew,
                userService.getUserDetails(userToken).getName(),
                emailService.getExuiCaseLink(caseDetails.getCaseId())
            )
        );
    }
}

package uk.gov.hmcts.ethos.replacement.docmosis.service;

import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.exceptions.DocumentManagementException;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateReferralService {
    private final TornadoService tornadoService;
    private static final String DOCGEN_ERROR = "Failed to generate document for case id: %s";

    /**
     * Uses {@link TornadoService} to generate a pdf to display a summary of data for the created referral.
     * @param caseData
     * @param userToken
     * @param caseTypeId
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


}

package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.elasticsearch.common.Strings;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.exceptions.DocumentManagementException;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateReferralService {
    private final TornadoService tornadoService;
    private static final String DOCGEN_ERROR = "Failed to generate document for case id: %s";

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
     * Gets errors in document upload.
     * @param caseData in which the referral type is extracted from
     * @param errors list
     */
    public void addDocumentUploadErrors(CaseData caseData, List<String> errors) {
        if (CollectionUtils.isNotEmpty(caseData.getReferralDocument())) {
            for (DocumentTypeItem documentTypeItem : caseData.getReferralDocument()) {
                if (!Strings.isNullOrEmpty(documentTypeItem.getValue().getShortDescription())
                        && documentTypeItem.getValue().getUploadedDocument() == null) {
                    errors.add("Short description is added but document is not uploaded.");
                }
            }
        }
    }
}

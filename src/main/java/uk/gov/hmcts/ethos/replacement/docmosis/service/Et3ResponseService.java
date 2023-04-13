package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.ecm.common.exceptions.DocumentManagementException;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.DynamicListTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service to support ET3 Response journey. Contains methods for generating and saving ET3 Response documents.
 */
@Slf4j
@Service("et3ResponseService")
@RequiredArgsConstructor
public class Et3ResponseService {

    private final DocumentManagementService documentManagementService;
    private final TornadoService tornadoService;

    private static final String DOCGEN_ERROR = "Failed to generate document for case id: %s";

    /**
     * This method calls the tornado service to generate the PDF for the ET3 Response journey.
     * @param caseData where the data is stored
     * @param userToken user authentication token
     * @param caseTypeId reference which caseType the document will be uploaded to
     * @return DocumentInfo which contains the URL and description of the document uploaded to DM Store
     */
    public DocumentInfo generateEt3ResponseDocument(CaseData caseData, String userToken, String caseTypeId) {
        try {
            return tornadoService.generateEventDocument(caseData, userToken,
                caseTypeId, "ET3 Response.pdf");
        } catch (Exception e) {
            throw new DocumentManagementException(String.format(DOCGEN_ERROR, caseData.getEthosCaseReference()), e);
        }
    }

    /**
     * Saves the generated ET3 Response form document in the document collection and the respondent.
     * @param caseData where the data is stored
     */
    public void saveEt3ResponseDocument(CaseData caseData, DocumentInfo documentInfo) {
        UploadedDocumentType uploadedDocument = documentManagementService.addDocumentToDocumentField(documentInfo);
        DocumentType documentType = new DocumentType();
        documentType.setUploadedDocument(uploadedDocument);
        documentType.setTypeOfDocument("ET3");

        DocumentTypeItem documentTypeItem = new DocumentTypeItem();
        documentTypeItem.setValue(documentType);
        documentTypeItem.setId(UUID.randomUUID().toString());

        addDocumentToDocCollection(caseData, documentTypeItem);
        addDocumentToRespondent(caseData, uploadedDocument);
    }

    private void addDocumentToRespondent(CaseData caseData, UploadedDocumentType uploadedDocument) {
        if (CollectionUtils.isEmpty(caseData.getEt3RepresentingRespondent())) {
            return;
        }

        Set<String> respondentSet = new HashSet<>();
        for (DynamicListTypeItem dynamicListTypeItem : caseData.getEt3RepresentingRespondent()) {
            respondentSet.add(dynamicListTypeItem.getValue().getDynamicList().getSelectedLabel());
        }

        for (String respondentSelected : respondentSet) {
            Optional<RespondentSumTypeItem> respondent = caseData.getRespondentCollection().stream()
                    .filter(r -> respondentSelected.equals(r.getValue().getRespondentName()))
                    .findFirst();
            if (respondent.isPresent()) {
                respondent.get().getValue().setEt3Form(uploadedDocument);
                for (RespondentSumTypeItem respondentSumTypeItem : caseData.getRespondentCollection()) {
                    if (respondentSelected.equals(respondentSumTypeItem.getValue().getRespondentName())) {
                        respondentSumTypeItem.setValue(respondent.get().getValue());
                    }
                }
            }
        }

    }

    private static void addDocumentToDocCollection(CaseData caseData, DocumentTypeItem documentTypeItem) {
        if (CollectionUtils.isEmpty(caseData.getDocumentCollection())) {
            caseData.setDocumentCollection(new ArrayList<>());
        }

        caseData.getDocumentCollection().add(documentTypeItem);
    }

    public void saveRelatedDocumentsToDocumentCollection(CaseData caseData) {
        if (CollectionUtils.isEmpty(caseData.getDocumentCollection())) {
            caseData.setDocumentCollection(new ArrayList<>());
        }

        List<DocumentTypeItem> documents = caseData.getDocumentCollection();

        Set<String> documentSet = documents.stream()
                .map(GenericTypeItem::getId)
                .collect(Collectors.toCollection(HashSet::new));

        documents.addAll(
                Optional.ofNullable(caseData.getEt3ResponseContestClaimDocument())
                .orElse(List.of())
                .stream()
                .filter(o -> !documentSet.contains(o.getId()))
                .collect(Collectors.toList())
        );

        if (caseData.getEt3ResponseEmployerClaimDocument() != null) {
            documents.add(DocumentTypeItem.fromUploadedDocument(caseData.getEt3ResponseEmployerClaimDocument()));
        }

        if (caseData.getEt3ResponseRespondentSupportDocument() != null) {
            documents.add(DocumentTypeItem.fromUploadedDocument(caseData.getEt3ResponseRespondentSupportDocument()));
        }
    }
}

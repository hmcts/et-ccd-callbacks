package uk.gov.hmcts.ethos.replacement.docmosis.service.multiples;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicMultiSelectListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.DynamicListHelper;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service("multiplesDocumentAccessService")
public final class MultiplesDocumentAccessService {

    public void setMultipleDocumentCollection(MultipleData multipleData) {
        if (CollectionUtils.isEmpty(multipleData.getDocumentCollection())) {
            log.warn("Empty document collection");
            return;
        }

        log.info("Getting docs");
        List<DynamicValueType> docs = multipleData.getDocumentCollection().stream()
                .map(documentTypeItem -> DynamicListHelper.getDynamicCodeLabel(documentTypeItem.getId(),
                        documentTypeItem.getValue().getUploadedDocument().getDocumentFilename()))
                .toList();

        log.info("Retrieved docs: " + docs.size());

        DynamicMultiSelectListType dynamicMultiSelectList = new DynamicMultiSelectListType();
        dynamicMultiSelectList.setListItems(docs);

        multipleData.setDocumentSelect(dynamicMultiSelectList);
    }


    public void setMultipleDocumentsToCorrectTab(MultipleData multipleData) {
        List<DocumentTypeItem> docs = multipleData.getDocumentCollection();
        String documentAccess = multipleData.getDocumentAccess();

        List<DocumentTypeItem> selectedDocs = docs.stream()
                .filter(doc -> multipleData.getDocumentSelect().getValue().stream()
                        .anyMatch(code -> code.getCode().equals(doc.getId())))
                .toList();

        if (documentAccess != null) {
            switch (documentAccess) {
                case "Citizens":
                    addSelectedDocsToCollection(selectedDocs, multipleData.getClaimantDocumentCollection());
                    break;
                case "Legal rep/respondents":
                    addSelectedDocsToCollection(selectedDocs, multipleData.getLegalrepDocumentCollection());
                    break;
                case "Both Citizens and Legal rep/respondents":
                    addSelectedDocsToCollection(selectedDocs, multipleData.getClaimantDocumentCollection());
                    addSelectedDocsToCollection(selectedDocs, multipleData.getLegalrepDocumentCollection());
                    break;
                default:
                    addSelectedDocsToCollection(selectedDocs, multipleData.getDocumentCollection());
                    break;
            }
        }
    }

    private void addSelectedDocsToCollection(List<DocumentTypeItem> selectedDocs,
                                             List<DocumentTypeItem> documentCollection) {
        for (DocumentTypeItem selectedDoc : selectedDocs) {
            if (!documentCollection.stream().anyMatch(doc -> doc.getId().equals(selectedDoc.getId()))) {
                documentCollection.add(selectedDoc);
            }
        }
    }
}

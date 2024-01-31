package uk.gov.hmcts.ethos.replacement.docmosis.utils;

import org.apache.commons.lang3.ObjectUtils;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class DocumentUtil {

    private DocumentUtil() {
        // Utility classes should not have a public or default constructor.
    }

    public static List<GenericTypeItem<DocumentType>> generateUploadedDocumentListFromDocumentList(
            List<GenericTypeItem<DocumentType>> documentList, String ccdGatewayBaseUrl) {

        List<GenericTypeItem<DocumentType>> uploadedDocumentList = new ArrayList<>();
        documentList.forEach(doc -> {
            if (ObjectUtils.isNotEmpty(doc.getValue().getUploadedDocument())) {
                doc.getValue().setTornadoEmbeddedPdfUrl(doc.getValue().getUploadedDocument().getDocumentFilename()
                        + "|" + getDownloadableDocumentURL(doc.getValue().getUploadedDocument().getDocumentUrl(),
                        ccdGatewayBaseUrl));
            }
            GenericTypeItem<DocumentType> genTypeItems = new GenericTypeItem<>();
            genTypeItems.setId(doc.getId() != null ? doc.getId() : UUID.randomUUID().toString());
            genTypeItems.setValue(doc.getValue());
            uploadedDocumentList.add(genTypeItems);
        });
        return uploadedDocumentList;
    }

    public static String getDownloadableDocumentURL(String documentURL, String ccdGatewayBaseUrl) {
        return ccdGatewayBaseUrl + "/documents/" + getDocumentUUIDByDocumentURL(documentURL) + "/binary";
    }

    private static String getDocumentUUIDByDocumentURL(String documentURL) {
        return documentURL.substring(documentURL.lastIndexOf('/') + 1);
    }
}

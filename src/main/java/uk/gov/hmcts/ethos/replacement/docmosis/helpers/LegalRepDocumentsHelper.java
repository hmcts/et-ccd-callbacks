package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationType;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.TseRespondTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.TseRespondType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLAIMANT_TITLE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NEW_DATE_PATTERN;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.LegalRepDocumentConstants.DOCUMENT_HEADING;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.LegalRepDocumentConstants.LEGAL_REP_HIDDEN_DOCS;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.MarkdownHelper.getDocumentLink;

public final class LegalRepDocumentsHelper {

    private LegalRepDocumentsHelper() {
        // Access through static methods
    }

    /**
     *  Filter documents that only the legal rep should be able to see and display it in a custom Markdown table.
     */
    public static void setLegalRepVisibleDocuments(CaseData caseData) {
        if (caseData.getDocumentCollection() == null) {
            return;
        }

        List<DocumentTypeItem> legalRepDocs = caseData.getDocumentCollection().stream()
                .filter(d -> ObjectUtils.isNotEmpty(d.getValue().getUploadedDocument()))
                .filter(d -> !containsTypeOfDocument(d.getValue()))
                .filter(d -> !getClaimantRule92NoDocumentBinaryUrls(caseData)
                        .contains(d.getValue().getUploadedDocument().getDocumentBinaryUrl())).toList();

        if (legalRepDocs.isEmpty()) {
            return;
        }

        List<String[]> rows = new ArrayList<>();
        IntStream.range(0, legalRepDocs.size()).forEach(
                i -> {
                    DocumentTypeItem legalRepDoc = legalRepDocs.get(i);
                    rows.add(new String[]{String.valueOf(i + 1),
                        getDocumentLink(legalRepDoc),
                        defaultIfEmpty(getTypeOfDocument(legalRepDoc.getValue()), "-"),
                        defaultIfEmpty(getDocFormattedDate(legalRepDoc.getValue().getDateOfCorrespondence()), "-")});
            }
        );

        String[] header = {EMPTY, DOCUMENT_HEADING, "Type", "Date"};
        caseData.setLegalRepDocumentsMarkdown(MarkdownHelper.createFourColumnTable(header, rows));
    }

    private static String getDocFormattedDate(String dateOfCorrespondence) {
        return isNullOrEmpty(dateOfCorrespondence) ? "" :
                LocalDate.parse(dateOfCorrespondence).format(NEW_DATE_PATTERN);
    }

    private static boolean containsTypeOfDocument(DocumentType documentType) {
        String typeOfDocument = getTypeOfDocument(documentType);
        if (ObjectUtils.isEmpty(typeOfDocument)) {
            return false;
        }
        return LEGAL_REP_HIDDEN_DOCS.stream()
                .anyMatch(doc -> doc.equalsIgnoreCase(typeOfDocument));
    }

    private static String getTypeOfDocument(DocumentType documentType) {
        return ObjectUtils.isNotEmpty(documentType.getDocumentType())
                ? documentType.getDocumentType()
                : documentType.getTypeOfDocument();
    }

    @NotNull
    private static List<String> getClaimantRule92NoDocumentBinaryUrls(CaseData caseData) {
        List<Optional<UploadedDocumentType>> claimantRule92NoDocuments = new ArrayList<>();

        // Get all documents with claimant rule 92 no - whether on application creation or in any subsequent response
        // These will only be supporting material as pdfs for rule 92 'no' aren't meant to be generated.
        for (GenericTseApplicationTypeItem app : ListUtils.emptyIfNull(caseData.getGenericTseApplicationCollection())) {
            GenericTseApplicationType appType = app.getValue();
            if (CLAIMANT_TITLE.equals(appType.getApplicant()) && NO.equals(appType.getCopyToOtherPartyYesOrNo())) {
                claimantRule92NoDocuments.add(Optional.ofNullable(appType.getDocumentUpload()));
            }

            for (TseRespondTypeItem response : ListUtils.emptyIfNull(appType.getRespondCollection())) {
                TseRespondType respondType = response.getValue();
                if (CLAIMANT_TITLE.equals(respondType.getFrom()) && NO.equals(respondType.getCopyToOtherParty())) {
                    claimantRule92NoDocuments.addAll(
                            emptyIfNull(respondType.getSupportingMaterial()).stream()
                                .map(documentType -> Optional.ofNullable(documentType.getValue().getUploadedDocument()))
                                .toList()
                    );
                }
            }
        }

        // Get document binary urls of non-null documents
        return claimantRule92NoDocuments.stream()
                .filter(Optional::isPresent)
                .map(optional -> optional.get().getDocumentBinaryUrl())
                .toList();
    }
}

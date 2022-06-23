package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.ET1VettingJurisdictionCodesTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.JurCodesTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.ET1VettingJurisdictionCodesType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.JurisdictionCode;

import java.util.ArrayList;
import java.util.List;

@Service
public class Et1VettingService {

    static final String ET1_DOC_TYPE = "ET1";
    static final String ACAS_DOC_TYPE = "ACAS Certificate";
    static final String DOC_LINK_DEFAULT = "/cases/case-details/%s#Documents";
    static final String BEFORE_LINK_LABEL = "Open these documents to help you complete this form: "
            + "<br/><a target=\"_blank\" href=\"%s\">ET1 form (opens in new tab)</a>"
            + "<br/><a target=\"_blank\" href=\"%s\">Acas certificate (opens in new tab)</a>"
            + "<br/>Check the Documents tab for additional ET1 documents the claimant may have uploaded.";

    public void initialBeforeYouStart(CaseDetails caseDetails) {

        var et1BinaryUrl = createDocLinkDefault(caseDetails.getCaseId());
        var acasBinaryUrl = createDocLinkDefault(caseDetails.getCaseId());

        var documentCollection = caseDetails.getCaseData().getDocumentCollection();
        if (documentCollection != null && !documentCollection.isEmpty()) {
            for (DocumentTypeItem d : documentCollection) {
                if (ET1_DOC_TYPE.equals(d.getValue().getTypeOfDocument())) {
                    et1BinaryUrl = createDocLinkBinary(d);
                }
                if (ACAS_DOC_TYPE.equals(d.getValue().getTypeOfDocument())) {
                    acasBinaryUrl = createDocLinkBinary(d);
                }
            }
        }

        caseDetails.getCaseData().setEt1VettingBeforeYouStart(
                String.format(BEFORE_LINK_LABEL, et1BinaryUrl, acasBinaryUrl));
    }

    public String generateJurisdictionCodesHtml(List<JurCodesTypeItem> jurisdictionCodes) {
        StringBuilder sb = new StringBuilder()
            .append("<hr><h3>Jurisdiction Codes</h3>")
            .append("<a href=\"https://intranet.justice.gov.uk/documents/2017/11/jurisdiction-list.pdf\">")
            .append("View all jurisdiction codes and descriptors (opens in new tab)</a><hr>")
            .append("<h3>Codes already added</h3>");

        for (JurCodesTypeItem codeItem : jurisdictionCodes) {
            String codeName = codeItem.getValue().getJuridictionCodesList();
            sb.append("<h4>")
                .append(codeName)
                .append("</h4>")
                .append(JurisdictionCode.valueOf(codeName).getDescription());

        }
        
        return sb.append("<hr>").toString();
    }

    public List<ET1VettingJurisdictionCodesTypeItem> populateJurisdictionCodesCollection() {
        List<DynamicValueType> listItems = new ArrayList<>();
        for (JurisdictionCode code : JurisdictionCode.values()) {
            String title = code.getDescription();
            String displayed = code.name() + " - "
                    + title.substring(0, Math.min(title.length(), 100))
                    + (title.length() > 100 ? "..." : "");
            listItems.add(DynamicValueType.create(code.name(), displayed));
        }

        var jurisdictionCodesType = new ET1VettingJurisdictionCodesType();
        jurisdictionCodesType.setDynamicJurisdictionCode(new DynamicFixedListType());
        jurisdictionCodesType.getDynamicJurisdictionCode().setListItems(listItems);

        var jurisdictionCodesTypeItem = new ET1VettingJurisdictionCodesTypeItem();
        jurisdictionCodesTypeItem.setValue(jurisdictionCodesType);

        List<ET1VettingJurisdictionCodesTypeItem> jurisdictionCodesTypeItemList = new ArrayList<>();
        jurisdictionCodesTypeItemList.add(jurisdictionCodesTypeItem);

        return jurisdictionCodesTypeItemList;
    }

    private String createDocLinkDefault(String caseId) {
        return String.format(DOC_LINK_DEFAULT, caseId);
    }

    private String createDocLinkBinary(DocumentTypeItem documentTypeItem) {
        var documentBinaryUrl = documentTypeItem.getValue().getUploadedDocument().getDocumentBinaryUrl();
        return documentBinaryUrl.substring(documentBinaryUrl.indexOf("/documents/"));
    }

}

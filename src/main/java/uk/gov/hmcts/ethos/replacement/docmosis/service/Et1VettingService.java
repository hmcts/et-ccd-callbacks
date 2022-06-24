package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.JurCodesTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.VettingJurCodesTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.JurCodesType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.JurisdictionCode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static uk.gov.hmcts.ethos.replacement.docmosis.service.ConciliationTrackService.*;

@Slf4j
@Service
public class Et1VettingService {

    static final String ET1_DOC_TYPE = "ET1";
    static final String ACAS_DOC_TYPE = "ACAS Certificate";
    static final String DOC_LINK_DEFAULT = "/cases/case-details/%s#Documents";
    static final String BEFORE_LINK_LABEL = "Open these documents to help you complete this form: "
            + "<br/><a target=\"_blank\" href=\"%s\">ET1 form (opens in new tab)</a>"
            + "<br/><a target=\"_blank\" href=\"%s\">Acas certificate (opens in new tab)</a>"
            + "<br/>Check the Documents tab for additional ET1 documents the claimant may have uploaded.";
    static final String TRACk_ALLOCATION_HTML = "|||\r\n|--|--|\r\n|Tack allocation|%s|\r\n";
    static final String JUR_CODE_HTML = "<hr><h3>Jurisdiction Codes</h3>"
            + "<a href=\"https://intranet.justice.gov.uk/documents/2017/11/jurisdiction-list.pdf\">"
            + "View all jurisdiction codes and descriptors (opens in new tab)</a><hr>"
            + "<h3>Codes already added</h3>%s<hr>";
    static final String CASE_NAME_AND_DESCRIPTION_HTML = "<h4>%s</h4>%s";
    static final String ERROR_EXISTING_JUR_CODE = "Jurisdiction code %s already exists.";
    static final String ERROR_SELECTED_JUR_CODE = "Jurisdiction code %s is selected more than once.";

    static final String TRACK_OPEN = "Open";
    static final String TRACK_STANDARD = "Standard";
    static final String TRACK_SHORT = "Short";
    static final String TRACK_NO = "No track";

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

    /**
     * Generate the Existing Jurisdiction Code list in HTML.
     */
    public String generateJurisdictionCodesHtml(List<JurCodesTypeItem> jurisdictionCodes) {
        StringBuilder sb = new StringBuilder();
        for (JurCodesTypeItem codeItem : jurisdictionCodes) {
            String codeName = codeItem.getValue().getJuridictionCodesList();
            if (codeName != null) {
                try {
                    sb.append(String.format(CASE_NAME_AND_DESCRIPTION_HTML, codeName,
                        JurisdictionCode.valueOf(codeName.replaceAll("[^a-zA-Z]+", ""))
                            .getDescription()));
                } catch (IllegalArgumentException e) {
                    log.warn("The jurisdiction code " + codeName + " is invalid.");
                }
            }
        }

        return String.format(JUR_CODE_HTML, sb);
    }

    /**
     * Validates jurisdiction codes that the caseworker has added in the vettingJurisdictionCodeCollection
     * to ensure that existing code can't be added and the codes that's been added can't have duplicate entries.
     *
     * @return a list of errors
     */
    public List<String> validateJurisdictionCodes(CaseData caseData) {
        List<String> errors = new ArrayList<>();
        List<VettingJurCodesTypeItem> codeList = caseData.getVettingJurisdictionCodeCollection();
        if(codeList != null) {
            codeList.stream().filter(codesTypeItem -> caseData.getJurCodesCollection().stream()
                            .map(existingCode -> existingCode.getValue().getJuridictionCodesList())
                            .collect(Collectors.toList()).stream()
                            .anyMatch(code -> code.equals(codesTypeItem.getValue().getEt1VettingJurCodeList())))
                    .forEach(c -> errors
                        .add(String.format(ERROR_EXISTING_JUR_CODE, c.getValue().getEt1VettingJurCodeList())));

            codeList.stream()
                    .filter(code -> Collections.frequency(codeList, code) > 1).collect(Collectors.toSet())
                    .forEach(c -> errors
                        .add(String.format(ERROR_SELECTED_JUR_CODE, c.getValue().getEt1VettingJurCodeList())));
        }

        return errors;
    }

    /**
     * Add the jurisdiction codes that's been added by the caseworker to jurCodesCollection.
     * Set the Track Allocation field which default the longest track for a claim based on the jurisdiction codes
     */
    public void populateEt1TrackAllocationHtml(CaseData caseData) {
        if (caseData.getVettingJurisdictionCodeCollection() != null) {
            for (VettingJurCodesTypeItem codeItem : caseData.getVettingJurisdictionCodeCollection()) {
                JurCodesType newCode = new JurCodesType();
                newCode.setJuridictionCodesList(codeItem.getValue().getEt1VettingJurCodeList());
                JurCodesTypeItem codesTypeItem = new JurCodesTypeItem();
                codesTypeItem.setValue(newCode);
                codesTypeItem.setId(UUID.randomUUID().toString());
                caseData.getJurCodesCollection().add(codesTypeItem);
            }
        }

        if (caseData.getJurCodesCollection().stream()
                .anyMatch(c -> JUR_CODE_CONCILIATION_TRACK_OP.contains(c.getValue().getJuridictionCodesList()))) {
            caseData.setTrackAllocation(String.format(TRACk_ALLOCATION_HTML, TRACK_OPEN));
        } else if(caseData.getJurCodesCollection().stream()
                .anyMatch(c -> JUR_CODE_CONCILIATION_TRACK_ST.contains(c.getValue().getJuridictionCodesList()))) {
            caseData.setTrackAllocation(String.format(TRACk_ALLOCATION_HTML, TRACK_STANDARD));
        } else if(caseData.getJurCodesCollection().stream()
                .anyMatch(c -> JUR_CODE_CONCILIATION_TRACK_SH.contains(c.getValue().getJuridictionCodesList()))) {
            caseData.setTrackAllocation(String.format(TRACk_ALLOCATION_HTML, TRACK_SHORT));
        } else {
            caseData.setTrackAllocation(String.format(TRACk_ALLOCATION_HTML, TRACK_NO));
        }
    }


    private String createDocLinkDefault(String caseId) {
        return String.format(DOC_LINK_DEFAULT, caseId);
    }

    private String createDocLinkBinary(DocumentTypeItem documentTypeItem) {
        var documentBinaryUrl = documentTypeItem.getValue().getUploadedDocument().getDocumentBinaryUrl();
        return documentBinaryUrl.substring(documentBinaryUrl.indexOf("/documents/"));
    }

}

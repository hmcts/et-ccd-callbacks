package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.IntWrapper;
import uk.gov.hmcts.et.common.model.ccd.items.JurCodesTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.VettingJurCodesTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.JurCodesType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.JurisdictionCode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static uk.gov.hmcts.ethos.replacement.docmosis.service.ConciliationTrackService.JUR_CODE_CONCILIATION_TRACK_OP;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.ConciliationTrackService.JUR_CODE_CONCILIATION_TRACK_SH;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.ConciliationTrackService.JUR_CODE_CONCILIATION_TRACK_ST;

@Slf4j
@Service
public class Et1VettingService {

    private static final String ET1_DOC_TYPE = "ET1";
    private static final String ACAS_DOC_TYPE = "ACAS Certificate";
    private static final String BEFORE_LABEL_TEMPLATE = "Open these documents to help you complete this form: %s%s"
        + "<br/>Check the Documents tab for additional ET1 documents the claimant may have uploaded.";
    private static final String BEFORE_LABEL_ET1 =
        "<br/><a target=\"_blank\" href=\"%s\">ET1 form (opens in new tab)</a>";
    private static final String BEFORE_LABEL_ACAS =
        "<br/><a target=\"_blank\" href=\"%s\">Acas certificate %s (opens in new tab)</a>";
    private static final String BEFORE_LABEL_ACAS_OPEN_TAB =
        "<br/><a target=\"_blank\" href=\"/cases/case-details/%s#Documents\">"
            + "Open the Documents tab to view/open Acas certificates (opens in new tab)</a>";
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

    /**
     * Update et1VettingBeforeYouStart.
     *
     * @param caseDetails Get caseId and Update caseData
     */
    public void initialiseEt1Vetting(CaseDetails caseDetails) {
        caseDetails.getCaseData().setEt1VettingBeforeYouStart(initialBeforeYouStart(caseDetails));
    }

    /**
     * Prepare wordings to be displayed in et1VettingBeforeYouStart.
     * Check uploaded document in documentCollection
     * For ET1 form
     * - get and display ET1 form
     * For Acas cert
     * - get and count number of Acas cert
     * - if 0 Acas cert, hide the Acas link
     * - if 1-5 Acas cert(s), display one or multi Acas link(s)
     * - if 6 or more Acas certs, display a link to case doc tab
     *
     * @param caseDetails Get caseId and documentCollection
     * @return et1VettingBeforeYouStart
     */
    private String initialBeforeYouStart(CaseDetails caseDetails) {

        String et1Display = "";
        String acasDisplay = "";
        IntWrapper acasCount = new IntWrapper(0);

        List<DocumentTypeItem> documentCollection = caseDetails.getCaseData().getDocumentCollection();
        if (documentCollection != null) {
            et1Display = documentCollection
                .stream()
                .filter(d -> d.getValue().getTypeOfDocument().equals(ET1_DOC_TYPE))
                .map(d -> String.format(BEFORE_LABEL_ET1, createDocLinkBinary(d)))
                .collect(Collectors.joining());
            acasDisplay = documentCollection
                .stream()
                .filter(d -> d.getValue().getTypeOfDocument().equals(ACAS_DOC_TYPE))
                .map(d -> String.format(
                    BEFORE_LABEL_ACAS, createDocLinkBinary(d), acasCount.incrementAndReturnValue()))
                .collect(Collectors.joining());
        }

        if (acasCount.getValue() > 5) {
            acasDisplay = String.format(BEFORE_LABEL_ACAS_OPEN_TAB, caseDetails.getCaseId());
        }

        return String.format(BEFORE_LABEL_TEMPLATE, et1Display, acasDisplay);
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
        if (codeList != null) {
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
    public String populateEt1TrackAllocationHtml(CaseData caseData) {
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
            return String.format(TRACk_ALLOCATION_HTML, TRACK_OPEN);
        } else if (caseData.getJurCodesCollection().stream()
            .anyMatch(c -> JUR_CODE_CONCILIATION_TRACK_ST.contains(c.getValue().getJuridictionCodesList()))) {
            return String.format(TRACk_ALLOCATION_HTML, TRACK_STANDARD);
        } else if (caseData.getJurCodesCollection().stream()
            .anyMatch(c -> JUR_CODE_CONCILIATION_TRACK_SH.contains(c.getValue().getJuridictionCodesList()))) {
            return String.format(TRACk_ALLOCATION_HTML, TRACK_SHORT);
        } else {
            return String.format(TRACk_ALLOCATION_HTML, TRACK_NO);
        }
    }

    private String createDocLinkBinary(DocumentTypeItem documentTypeItem) {
        String documentBinaryUrl = documentTypeItem.getValue().getUploadedDocument().getDocumentBinaryUrl();
        return documentBinaryUrl.substring(documentBinaryUrl.indexOf("/documents/"));
    }

}

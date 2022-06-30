package uk.gov.hmcts.ethos.replacement.docmosis.service;

import com.google.common.base.Strings;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.Address;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.IntWrapper;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class Et1VettingService {

    private static final String ET1_DOC_TYPE = "ET1";
    private static final String ACAS_DOC_TYPE = "ACAS Certificate";
    private static final String BEFORE_LABEL_TEMPLATE = "Open these documents to help you complete this form: %s%s"
            + "<br>Check the Documents tab for additional ET1 documents the claimant may have uploaded.";
    private static final String BEFORE_LABEL_ET1 =
            "<br><a target=\"_blank\" href=\"%s\">ET1 form (opens in new tab)</a>";
    private static final String BEFORE_LABEL_ACAS =
            "<br><a target=\"_blank\" href=\"%s\">Acas certificate %s (opens in new tab)</a>";
    private static final String BEFORE_LABEL_ACAS_OPEN_TAB =
            "<br><a target=\"_blank\" href=\"/cases/case-details/%s#Documents\">"
                    + "Open the Documents tab to view/open Acas certificates (opens in new tab)</a>";
    private static final String CLAIMANT_DETAILS = "<hr><h3>Claimant</h3>"
            + "<pre>First name &#09&#09&#09&#09&nbsp; %s"
            + "<br><br>Last name &#09&#09&#09&#09&nbsp; %s"
            + "<br><br>Contact address &#09&#09 %s</pre><hr>";
    private static final String RESPONDENT_DETAILS = "<h3>Respondent %s</h3>"
            + "<pre>Name &#09&#09&#09&#09&#09&#09&nbsp; %s"
            + "<br><br>Contact address &#09&#09 %s</pre><hr>";
    private static final String BR_WITH_TAB = "<br>&#09&#09&#09&#09&#09&#09&#09&#09&#09 ";
    private static final String ACAS_CERT_LIST_DISPLAY = "Certificate number %s has been provided.<br>";

    /**
     * Update et1VettingBeforeYouStart.
     * @param caseDetails Get caseId and Update caseData
     */
    public void initialiseEt1Vetting(CaseDetails caseDetails) {
        caseDetails.getCaseData().setEt1VettingBeforeYouStart(initialBeforeYouStart(caseDetails));
        caseDetails.getCaseData().setEt1VettingClaimantDetailsMarkUp(
                initialClaimantDetailsMarkUp(caseDetails.getCaseData()));
        caseDetails.getCaseData().setEt1VettingRespondentDetailsMarkUp(
                initialRespondentDetailsMarkUp(caseDetails.getCaseData()));
        caseDetails.getCaseData().setEt1VettingAcasCertListMarkUp(initialAcasCertList(caseDetails.getCaseData()));
    }

    /**
     * Prepare wordings to be displayed in et1VettingBeforeYouStart.
     * Check uploaded document in documentCollection
     *  For ET1 form
     *  - get and display ET1 form
     *  For Acas cert
     *  - get and count number of Acas cert
     *  - if 0 Acas cert, hide the Acas link
     *  - if 1-5 Acas cert(s), display one or multi Acas link(s)
     *  - if 6 or more Acas certs, display a link to case doc tab
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

    private String createDocLinkBinary(DocumentTypeItem documentTypeItem) {
        String documentBinaryUrl = documentTypeItem.getValue().getUploadedDocument().getDocumentBinaryUrl();
        return documentBinaryUrl.substring(documentBinaryUrl.indexOf("/documents/"));
    }

    /**
     * Prepare wordings to be displayed in et1VettingClaimantDetailsMarkUp.
     * @param caseData Get ClaimantIndType and ClaimantType
     * @return et1VettingClaimantDetailsMarkUp
     */
    private String initialClaimantDetailsMarkUp(CaseData caseData) {
        return String.format(CLAIMANT_DETAILS,
                caseData.getClaimantIndType().getClaimantFirstNames(),
                caseData.getClaimantIndType().getClaimantLastName(),
                toAddressWithTab(caseData.getClaimantType().getClaimantAddressUK()));
    }

    /**
     * Prepare wordings to be displayed in et1VettingRespondentDetailsMarkUp.
     * @param caseData Get RespondentCollection
     * @return et1VettingRespondentDetailsMarkUp
     */
    private String initialRespondentDetailsMarkUp(CaseData caseData) {
        if (caseData.getRespondentCollection().size() == 1) {
            RespondentSumType respondentSumType = caseData.getRespondentCollection().get(0).getValue();
            return String.format(RESPONDENT_DETAILS, "",
                    respondentSumType.getRespondentName(),
                    toAddressWithTab(respondentSumType.getRespondentAddress()));
        } else {
            IntWrapper count = new IntWrapper(0);
            return caseData.getRespondentCollection()
                    .stream()
                    .map(r -> String.format(RESPONDENT_DETAILS,
                            count.incrementAndReturnValue(),
                            r.getValue().getRespondentName(),
                            toAddressWithTab(r.getValue().getRespondentAddress())))
                    .collect(Collectors.joining());
        }
    }

    public String toAddressWithTab(Address address) {
        StringBuilder claimantAddressStr = new StringBuilder();
        claimantAddressStr.append(address.getAddressLine1());
        if (!Strings.isNullOrEmpty(address.getAddressLine2())) {
            claimantAddressStr.append(BR_WITH_TAB).append(address.getAddressLine2());
        }
        if (!Strings.isNullOrEmpty(address.getAddressLine3())) {
            claimantAddressStr.append(BR_WITH_TAB).append(address.getAddressLine3());
        }
        claimantAddressStr.append(BR_WITH_TAB).append(address.getPostTown())
                .append(BR_WITH_TAB).append(address.getPostCode());
        return claimantAddressStr.toString();
    }

    /**
     * Prepare wordings to be displayed in et1VettingAcasCertListMarkUp.
     * @param caseData Get RespondentCollection
     * @return et1VettingAcasCertListMarkUp
     */
    private String initialAcasCertList(CaseData caseData) {
        return caseData.getRespondentCollection()
                .stream()
                .filter(r -> r.getValue().getRespondentACAS() != null)
                .map(r -> String.format(ACAS_CERT_LIST_DISPLAY,
                        r.getValue().getRespondentACAS()))
                .findFirst()
                .orElse("");
    }

}

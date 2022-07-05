package uk.gov.hmcts.ethos.replacement.docmosis.service;

import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.Address;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.JurCodesTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.VettingJurCodesTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.JurCodesType;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.et.common.model.ccd.types.VettingJurisdictionCodesType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.JurisdictionCode;
import uk.gov.hmcts.ethos.replacement.docmosis.service.referencedata.jpaservice.JpaVenueService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.IntWrapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static uk.gov.hmcts.ethos.replacement.docmosis.utils.JurisdictionCodeTrackConstants.JUR_CODE_CONCILIATION_TRACK_OP;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.JurisdictionCodeTrackConstants.JUR_CODE_CONCILIATION_TRACK_SH;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.JurisdictionCodeTrackConstants.JUR_CODE_CONCILIATION_TRACK_ST;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.JurisdictionCodeTrackConstants.TRACK_NO;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.JurisdictionCodeTrackConstants.TRACK_OPEN;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.JurisdictionCodeTrackConstants.TRACK_SHORT;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.JurisdictionCodeTrackConstants.TRACK_STANDARD;

@Slf4j
@Service
@RequiredArgsConstructor
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

    private static final String HEARING_VENUES_TITLE = "<hr><h2>Listing details<hr>";
    private static final String CLAIMANT_DETAILS = "<hr><h3>Claimant</h3>"
            + "<pre>First name &#09&#09&#09&#09&nbsp; %s"
            + "<br><br>Last name &#09&#09&#09&#09&nbsp; %s"
            + "<br><br>Contact address &#09&#09 %s</pre><hr>";
    private static final String CLAIMANT_CONTACT_ADDRESS = "<h3>Claimant</h3>"
        + "<pre>Contact address &#09&#09 %s</pre>";
    private static final String CLAIMANT_WORK_ADDRESS = "<br><pre>Work address &#09&#09&#09 %s</pre><hr>";
    private static final String RESPONDENT_DETAILS = "<h3>Respondent %s</h3>"
            + "<pre>Name &#09&#09&#09&#09&#09&#09&nbsp; %s"
            + "<br><br>Contact address &#09&#09 %s</pre><hr>";
    private static final String RESPONDENT_ADDRESS = "<h3>Respondent</h3>"
            + "<pre>Contact address &#09&#09 %s</pre><hr>";
    private static final String BR_WITH_TAB = "<br>&#09&#09&#09&#09&#09&#09&#09&#09&#09 ";
    private static final String TRIBUNAL_OFFICE_LOCATION = "<hr><h3>Tribunal location</h3>"
        + "<pre>Tribunal &#09&#09&#09&#09&nbsp; %s"
        + "<br><br>Office &#09&#09&#09&#09&#09 %s</pre><hr>";
    private static final String TRIBUNAL_LOCATION_LABEL = "**<big>%s regional office</big>**";

    private static final String TRACK_ALLOCATION_HTML = "|||\r\n|--|--|\r\n|Track allocation|%s|\r\n";
    private static final String JUR_CODE_HTML = "<hr><h3>Jurisdiction Codes</h3>"
        + "<a href=\"https://intranet.justice.gov.uk/documents/2017/11/jurisdiction-list.pdf\">"
        + "View all jurisdiction codes and descriptors (opens in new tab)</a><hr>"
        + "<h3>Codes already added</h3>%s<hr>";
    private static final String CASE_NAME_AND_DESCRIPTION_HTML = "<h4>%s</h4>%s";
    private static final String ERROR_EXISTING_JUR_CODE = "Jurisdiction code %s already exists.";
    private static final String ERROR_SELECTED_JUR_CODE = "Jurisdiction code %s is selected more than once.";

    private static final String TRIBUNAL_ENGLAND = "England & Wales";
    private static final String TRIBUNAL_SCOTLAND = "Scotland";
    private static final String ACAS_CERT_LIST_DISPLAY = "Certificate number %s has been provided.<br>";

    private final JpaVenueService jpaVenueService;

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

    /**
     * Generate the Existing Jurisdiction Code list in HTML.
     */
    public String generateJurisdictionCodesHtml(List<JurCodesTypeItem> jurisdictionCodes) {
        StringBuilder sb = new StringBuilder();
        for (JurCodesTypeItem codeItem : jurisdictionCodes) {
            populateCodeNameAndDescriptionHtml(sb, codeItem.getValue().getJuridictionCodesList());
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
        if (!codeList.isEmpty()) {
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
        if (!caseData.getVettingJurisdictionCodeCollection().isEmpty()) {
            for (VettingJurCodesTypeItem codeItem : caseData.getVettingJurisdictionCodeCollection()) {
                addJurCodeToExistingCollection(caseData, codeItem.getValue());
            }
        }

        if (caseData.getJurCodesCollection().stream()
            .anyMatch(c -> JUR_CODE_CONCILIATION_TRACK_OP.contains(c.getValue().getJuridictionCodesList()))) {
            return String.format(TRACK_ALLOCATION_HTML, TRACK_OPEN);
        } else if (caseData.getJurCodesCollection().stream()
            .anyMatch(c -> JUR_CODE_CONCILIATION_TRACK_ST.contains(c.getValue().getJuridictionCodesList()))) {
            return String.format(TRACK_ALLOCATION_HTML, TRACK_STANDARD);
        } else if (caseData.getJurCodesCollection().stream()
            .anyMatch(c -> JUR_CODE_CONCILIATION_TRACK_SH.contains(c.getValue().getJuridictionCodesList()))) {
            return String.format(TRACK_ALLOCATION_HTML, TRACK_SHORT);
        } else {
            return String.format(TRACK_ALLOCATION_HTML, TRACK_NO);
        }
    }

    /**
     * Populates tribunal office location and regional office label/list based on managing office location.
     */
    public void populateTribunalOfficeFields(CaseData caseData) {
        String managingOffice = caseData.getManagingOffice();
        String tribunalLocation = TribunalOffice.isScotlandOffice(managingOffice)
            ? TRIBUNAL_SCOTLAND : TRIBUNAL_ENGLAND;
        caseData.setTribunalAndOfficeLocation(String
            .format(TRIBUNAL_OFFICE_LOCATION, tribunalLocation, managingOffice));
        caseData.setRegionalOffice(String.format(TRIBUNAL_LOCATION_LABEL, tribunalLocation));
        caseData.setRegionalOfficeList(populateRegionalOfficeList(tribunalLocation, managingOffice));
    }

    private DynamicFixedListType populateRegionalOfficeList(String tribunalLocation, String managingOffice) {
        List<TribunalOffice> tribunalOffices = tribunalLocation.equals(TRIBUNAL_ENGLAND)
            ? TribunalOffice.ENGLANDWALES_OFFICES : TribunalOffice.SCOTLAND_OFFICES;

        return DynamicFixedListType.from(tribunalOffices.stream()
            .filter(tribunalOffice -> !tribunalOffice.getOfficeName().equals(managingOffice))
            .map(tribunalOffice ->
                DynamicValueType.create(tribunalOffice.getOfficeName(), tribunalOffice.getOfficeName()))
            .collect(Collectors.toList()));
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

    private void populateCodeNameAndDescriptionHtml(StringBuilder sb, String codeName) {
        if (codeName != null) {
            try {
                sb.append(String.format(CASE_NAME_AND_DESCRIPTION_HTML, codeName,
                    JurisdictionCode.valueOf(codeName.replaceAll("[^a-zA-Z]+", ""))
                        .getDescription()));
            } catch (IllegalArgumentException e) {
                log.warn("The jurisdiction code " + codeName + " is invalid.", e);
            }
        }
    }

    private void addJurCodeToExistingCollection(CaseData caseData, VettingJurisdictionCodesType code) {
        JurCodesType newCode = new JurCodesType();
        newCode.setJuridictionCodesList(code.getEt1VettingJurCodeList());
        JurCodesTypeItem codesTypeItem = new JurCodesTypeItem();
        codesTypeItem.setValue(newCode);
        codesTypeItem.setId(UUID.randomUUID().toString());
        caseData.getJurCodesCollection().add(codesTypeItem);
    }

    public DynamicFixedListType getHearingVenuesList(CaseData caseData) {
        var dynamicListingVenues = new DynamicFixedListType();

        dynamicListingVenues.setListItems(new ArrayList<>(
            jpaVenueService.getVenues(TribunalOffice.valueOfOfficeName(caseData.getManagingOffice()))
        ));

        return dynamicListingVenues;
    }

    public String getAddressesHtml(CaseData caseData) {
        Address claimantAddressUK = caseData.getClaimantType().getClaimantAddressUK();
        Address claimantWorkAddress = caseData.getClaimantWorkAddress().getClaimantWorkAddress();
        Address respondentAddress = caseData.getRespondentCollection().get(0).getValue().getRespondentAddress();

        return HEARING_VENUES_TITLE
            + String.format(CLAIMANT_CONTACT_ADDRESS, toAddressWithTab(claimantAddressUK))
            + String.format(CLAIMANT_WORK_ADDRESS, toAddressWithTab(claimantWorkAddress))
            + String.format(RESPONDENT_ADDRESS, toAddressWithTab(respondentAddress));
    }
}

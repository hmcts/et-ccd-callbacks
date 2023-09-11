package uk.gov.hmcts.ethos.replacement.docmosis.service;

import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.exceptions.DocumentManagementException;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.Address;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.JurCodesTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
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
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.ACAS_CERT_LIST_DISPLAY;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.ACAS_DOC_TYPE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.BEFORE_LABEL_ACAS;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.BEFORE_LABEL_ACAS_OPEN_TAB;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.BEFORE_LABEL_ET1;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.BEFORE_LABEL_ET1_ATTACHMENT;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.BEFORE_LABEL_TEMPLATE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.BR_WITH_TAB;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.CASE_NAME_AND_DESCRIPTION_HTML;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.CLAIMANT_AND_RESPONDENT_ADDRESSES;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.CLAIMANT_AND_RESPONDENT_ADDRESSES_WITHOUT_WORK_ADDRESS;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.CLAIMANT_DETAILS_COMPANY;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.CLAIMANT_DETAILS_PERSON;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.COMPANY;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.DOCGEN_ERROR;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.ERROR_EXISTING_JUR_CODE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.ERROR_SELECTED_JUR_CODE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.ET1_ATTACHMENT_DOC_TYPE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.ET1_DOC_TYPE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.FIVE_ACAS_DOC_TYPE_ITEMS_COUNT;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.JUR_CODE_HTML;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.NO_ACAS_CERT_DISPLAY;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.ONE_RESPONDENT_COUNT;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.RESPONDENT_ACAS_DETAILS;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.RESPONDENT_DETAILS;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.TRACK_ALLOCATION_HTML;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.TRIBUNAL_ENGLAND;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.TRIBUNAL_LOCATION_LABEL;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.TRIBUNAL_OFFICE_LOCATION;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.TRIBUNAL_SCOTLAND;
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

    private final TornadoService tornadoService;
    private final JpaVenueService jpaVenueService;

    /**
     * Update et1VettingBeforeYouStart.
     * @param caseDetails Get caseId and Update caseData
     */
    public void initialiseEt1Vetting(CaseDetails caseDetails) {
        caseDetails.getCaseData().setEt1VettingBeforeYouStart(initialBeforeYouStart(caseDetails));
        caseDetails.getCaseData().setEt1VettingClaimantDetailsMarkUp(
            getInitialClaimantDetailsMarkUp(caseDetails.getCaseData()));
        caseDetails.getCaseData().setEt1VettingRespondentDetailsMarkUp(
            initialRespondentDetailsMarkUp(caseDetails.getCaseData()));
        populateRespondentAcasDetailsMarkUp(caseDetails.getCaseData());
    }

    /**
     * This method populates the hearing venue. The office is determined from the previous screen where the user
     * picks the tribunal office the case should be listed in. If they choose a different office to the current one,
     * they should see the venues for that office
     * @param caseData holds all the casedata
     */
    public void populateHearingVenue(CaseData caseData) {
        if (DynamicFixedListType.getSelectedLabel(caseData.getRegionalOfficeList()).isPresent()) {
            caseData.setEt1TribunalRegion(caseData.getRegionalOfficeList().getSelectedLabel());
        } else {
            caseData.setEt1TribunalRegion(caseData.getManagingOffice());
        }
        caseData.setEt1HearingVenues(getHearingVenuesList(caseData.getEt1TribunalRegion()));
        if (caseData.getSuggestedHearingVenues() != null
                && caseData.getSuggestedHearingVenues().getValue() != null
                && caseData.getEt1HearingVenues()
                .isValidCodeForList(caseData.getSuggestedHearingVenues().getValue().getCode())) {
            caseData.getEt1HearingVenues().setValue(caseData.getSuggestedHearingVenues().getValue());
        }
    }

    /**
     * Populates hearing venues for suggestedHearingVenues from the managing office.
     * @param caseData data on the case.
     */
    public void populateSuggestedHearingVenues(CaseData caseData) {
        DynamicFixedListType hearingVenuesList = getHearingVenuesList(caseData.getManagingOffice());
        DynamicFixedListType suggestedHearingVenues = caseData.getSuggestedHearingVenues();
        if (suggestedHearingVenues != null) {
            hearingVenuesList.setValue(suggestedHearingVenues.getValue());
        }
        caseData.setSuggestedHearingVenues(hearingVenuesList);
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
        String et1Attachment = "";
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
            et1Attachment = documentCollection
                    .stream()
                    .filter(d -> d.getValue().getTypeOfDocument().equals(ET1_ATTACHMENT_DOC_TYPE))
                    .map(d -> String.format(BEFORE_LABEL_ET1_ATTACHMENT,
                            createDocLinkBinary(d), d.getValue().getUploadedDocument().getDocumentFilename()))
                    .collect(Collectors.joining());
        }

        if (acasCount.getValue() > FIVE_ACAS_DOC_TYPE_ITEMS_COUNT) {
            acasDisplay = String.format(BEFORE_LABEL_ACAS_OPEN_TAB, caseDetails.getCaseId());
        }

        return String.format(BEFORE_LABEL_TEMPLATE, et1Display, acasDisplay, et1Attachment);
    }

    private String createDocLinkBinary(DocumentTypeItem documentTypeItem) {
        String documentBinaryUrl = documentTypeItem.getValue().getUploadedDocument().getDocumentBinaryUrl();
        return documentBinaryUrl.substring(documentBinaryUrl.indexOf("/documents/"));
    }

    /**
     * Prepare wordings to be displayed in et1VettingClaimantDetailsMarkUp
     * for the type of current claimant, i.e. Person or Company.
     * @param caseData Get ClaimantIndType and ClaimantType
     * @return et1VettingClaimantDetailsMarkUp
     */
    private String getInitialClaimantDetailsMarkUp(CaseData caseData) {
        if (COMPANY.equals(caseData.getClaimantTypeOfClaimant())) {
            return String.format(CLAIMANT_DETAILS_COMPANY,
                caseData.getClaimantCompany() != null
                    ? caseData.getClaimantCompany()
                    : "Company name not specified.",
                toAddressWithTab(caseData.getClaimantType().getClaimantAddressUK()));
        }

        if (caseData.getClaimantIndType() == null) {
            return String.format(CLAIMANT_DETAILS_PERSON, "Name not specified.", "Name not specified.",
                toAddressWithTab(caseData.getClaimantType().getClaimantAddressUK()));
        }

        return String.format(CLAIMANT_DETAILS_PERSON,
            caseData.getClaimantIndType().getClaimantFirstNames() != null
                ? caseData.getClaimantIndType().getClaimantFirstNames()
                : "First name not specified.",
            caseData.getClaimantIndType().getClaimantLastName() != null
                ? caseData.getClaimantIndType().getClaimantLastName()
                : "Last name not specified.",
            toAddressWithTab(caseData.getClaimantType().getClaimantAddressUK()));
    }

    /**
     * Prepare wordings to be displayed in et1VettingRespondentDetailsMarkUp.
     * @param caseData Get RespondentCollection
     * @return et1VettingRespondentDetailsMarkUp
     */
    private String initialRespondentDetailsMarkUp(CaseData caseData) {
        if (caseData.getRespondentCollection().size() == ONE_RESPONDENT_COUNT) {
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

    @SuppressWarnings({"checkstyle:FallThrough"}) // This is intentional as it acts like a loop
    private void populateRespondentAcasDetailsMarkUp(CaseData caseData) {
        List<RespondentSumTypeItem> respondentList = caseData.getRespondentCollection();
        switch (respondentList.size()) {
            case 6:
                caseData.setEt1VettingRespondentAcasDetails6(
                        generateRespondentAndAcasDetails(respondentList.get(5).getValue(), 6));
            case 5:
                caseData.setEt1VettingRespondentAcasDetails5(
                        generateRespondentAndAcasDetails(respondentList.get(4).getValue(), 5));
            case 4:
                caseData.setEt1VettingRespondentAcasDetails4(
                        generateRespondentAndAcasDetails(respondentList.get(3).getValue(), 4));
            case 3:
                caseData.setEt1VettingRespondentAcasDetails3(
                        generateRespondentAndAcasDetails(respondentList.get(2).getValue(), 3));
            case 2:
                caseData.setEt1VettingRespondentAcasDetails2(
                        generateRespondentAndAcasDetails(respondentList.get(1).getValue(), 2));
            case 1:
                caseData.setEt1VettingRespondentAcasDetails1(
                        generateRespondentAndAcasDetails(respondentList.get(0).getValue(), 1));
                break;
            default:
        }
    }

    private String generateRespondentAndAcasDetails(RespondentSumType respondent, int respondentNumber) {
        return String.format(RESPONDENT_ACAS_DETAILS, respondentNumber, respondent.getRespondentName(),
            toAddressWithTab(respondent.getRespondentAddress()))
            + (respondent.getRespondentAcas() == null
            ? NO_ACAS_CERT_DISPLAY : String.format(ACAS_CERT_LIST_DISPLAY, respondent.getRespondentAcas()));
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
            if (CollectionUtils.isNotEmpty(caseData.getJurCodesCollection())) {
                codeList.stream().filter(codesTypeItem -> caseData.getJurCodesCollection().stream()
                        .map(existingCode -> existingCode.getValue().getJuridictionCodesList())
                        .collect(Collectors.toList()).stream()
                        .anyMatch(code -> code.equals(codesTypeItem.getValue().getEt1VettingJurCodeList())))
                    .forEach(c -> errors
                        .add(String.format(ERROR_EXISTING_JUR_CODE, c.getValue().getEt1VettingJurCodeList())));
            }
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
            caseData.setTrackType(TRACK_OPEN);
            return String.format(TRACK_ALLOCATION_HTML, TRACK_OPEN);
        } else if (caseData.getJurCodesCollection().stream()
            .anyMatch(c -> JUR_CODE_CONCILIATION_TRACK_ST.contains(c.getValue().getJuridictionCodesList()))) {
            caseData.setTrackType(TRACK_STANDARD);
            return String.format(TRACK_ALLOCATION_HTML, TRACK_STANDARD);
        } else if (caseData.getJurCodesCollection().stream()
            .anyMatch(c -> JUR_CODE_CONCILIATION_TRACK_SH.contains(c.getValue().getJuridictionCodesList()))) {
            caseData.setTrackType(TRACK_SHORT);
            return String.format(TRACK_ALLOCATION_HTML, TRACK_SHORT);
        } else {
            caseData.setTrackType(TRACK_NO);
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

    public DynamicFixedListType getHearingVenuesList(String office) {
        List<DynamicValueType> venueList = jpaVenueService.getVenues(TribunalOffice.valueOfOfficeName(office));
        return DynamicFixedListType.from(venueList);
    }

    public String getAddressesHtml(CaseData caseData) {
        if (caseData.getClaimantWorkAddressQuestion() != null
                && NO.equals(caseData.getClaimantWorkAddressQuestion())) {
            return String.format(CLAIMANT_AND_RESPONDENT_ADDRESSES,
                    toAddressWithTab(caseData.getClaimantType().getClaimantAddressUK()),
                    toAddressWithTab(caseData.getClaimantWorkAddress().getClaimantWorkAddress()),
                    toAddressWithTab(caseData.getRespondentCollection().get(0).getValue().getRespondentAddress()));
        } else {
            return String.format(CLAIMANT_AND_RESPONDENT_ADDRESSES_WITHOUT_WORK_ADDRESS,
                    toAddressWithTab(caseData.getClaimantType().getClaimantAddressUK()),
                    toAddressWithTab(caseData.getRespondentCollection().get(0).getValue().getRespondentAddress())
            );
        }

    }

    /**
     * This calls the Tornado service to generate the pdf for the ET1 Vetting journey.
     * @param caseData gets the casedata
     * @param userToken user authentication token
     * @param caseTypeId reference which casetype the document will be uploaded to
     * @return DocumentInfo which contains the url and markup for the uploaded document
     */
    public DocumentInfo generateEt1VettingDocument(CaseData caseData, String userToken, String caseTypeId) {
        try {
            return tornadoService.generateEventDocument(caseData, userToken,
                    caseTypeId, "ET1 Vetting.pdf");
        } catch (Exception e) {
            throw new DocumentManagementException(String.format(DOCGEN_ERROR, caseData.getEthosCaseReference()), e);
        }
    }
}

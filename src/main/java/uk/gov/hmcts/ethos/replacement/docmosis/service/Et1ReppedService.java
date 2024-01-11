package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.gov.dwp.regex.InvalidPostcodeException;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.ecm.common.service.JurisdictionCodesMapperService;
import uk.gov.hmcts.ecm.common.service.PostcodeToOfficeService;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.google.common.base.Strings.isNullOrEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.DocumentHelper.createDocumentTypeItem;

@Service
@Slf4j
@RequiredArgsConstructor
public class Et1ReppedService {
    private final AuthTokenGenerator authTokenGenerator;

    private final AcasService acasService;
    private final DocumentManagementService documentManagementService;
    private final JurisdictionCodesMapperService jurisdictionCodesMapperService;
    private final PostcodeToOfficeService postcodeToOfficeService;
    private final TribunalOfficesService tribunalOfficesService;
    private final UserIdamService userIdamService;
    private final List<TribunalOffice> liveTribunalOffices = List.of(TribunalOffice.LEEDS,
            TribunalOffice.MIDLANDS_EAST, TribunalOffice.BRISTOL, TribunalOffice.GLASGOW);

    /**
     * Validates the postcode.
     * @param caseData the case data
     * @return YES if the postcode is valid, NO otherwise
     * @throws InvalidPostcodeException if the postcode is invalid
     */
    public String validatePostcode(CaseData caseData) throws InvalidPostcodeException {
        if (ObjectUtils.isEmpty(caseData.getEt1ReppedTriageAddress())
                || isNullOrEmpty(caseData.getEt1ReppedTriageAddress().getPostCode())) {
            return NO;
        }

        Optional<TribunalOffice> office = postcodeToOfficeService.getTribunalOfficeFromPostcode(
                caseData.getEt1ReppedTriageAddress().getPostCode());
        if (office.isEmpty() || !liveTribunalOffices.contains(office.get())) {
            return NO;
        }

        return YES;
    }

    private void getS2SAuthToken() {
        System.out.println(authTokenGenerator.generate());
    }

    /**
     * Adds some base data to the case.
     * @param caseDetails the case details
     */
    public void addDefaultData(CaseDetails caseDetails) {
        CaseData caseData = caseDetails.getCaseData();
        tribunalOfficesService.addManagingOffice(caseData, caseDetails.getCaseTypeId());
        caseData.setJurCodesCollection(jurisdictionCodesMapperService.mapToJurCodes(caseData));
        caseData.setReceiptDate(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        caseData.setPositionType("ET1 Online submission");
        caseData.setCaseSource("ET1 Online");
    }

    /**
     * Creates the ET1 PDF and calls of to ACAS to retrieve the certificates.
     * @param caseDetails the case details
     * @param userToken the user token
     */
    public void createAndUploadEt1Docs(CaseDetails caseDetails, String userToken) {
        DocumentTypeItem et1 = createEt1(caseDetails, userToken);
        List<DocumentTypeItem> acasCertificates = retrieveAndAddAcasCertificates(caseDetails.getCaseData(), userToken);
        addDocsToClaim(caseDetails.getCaseData(), et1, acasCertificates);
    }

    private void addDocsToClaim(CaseData caseData, DocumentTypeItem et1,
                                List<DocumentTypeItem> acasCertificates) {
        ArrayList<DocumentTypeItem> documentList = new ArrayList<>();
        documentList.add(et1);
        if (caseData.getEt1SectionThreeDocumentUpload() != null) {
            UploadedDocumentType et1Attachment = caseData.getEt1SectionThreeDocumentUpload();
            et1Attachment.setCategoryId("C12");
            documentList.add(createDocumentTypeItem(caseData.getEt1SectionThreeDocumentUpload(), "ET1 Attachment"));
        }
        documentList.addAll(acasCertificates);
        caseData.setClaimantDocumentCollection(documentList);
        caseData.setDocumentCollection(documentList);
    }

    private DocumentTypeItem createEt1(CaseDetails caseDetails, String userToken) {
        DocumentTypeItem et1 = new DocumentTypeItem();
        return et1;
    }

    private List<DocumentTypeItem> retrieveAndAddAcasCertificates(CaseData caseData, String userToken) {
        if (CollectionUtils.isEmpty(caseData.getRespondentCollection())) {
            return new ArrayList<>();
        }
        List<String> acasNumbers = caseData.getRespondentCollection().stream()
                .filter(respondent -> !isNullOrEmpty(respondent.getValue().getRespondentAcas()))
                .map(respondent -> respondent.getValue().getRespondentAcas())
                .toList();

        if (CollectionUtils.isEmpty(acasNumbers)) {
            return new ArrayList<>();
        }

        List<DocumentInfo> documentInfoList = acasNumbers.stream()
                .map(acasNumber -> acasService.getAcasCertificates(caseData, acasNumber, userToken))
                .filter(Objects::nonNull)
                .toList();

        if (CollectionUtils.isEmpty(documentInfoList)) {
            return new ArrayList<>();
        }

        List<DocumentTypeItem> documentTypeItems = new ArrayList<>();
        documentInfoList.stream()
                .map(documentManagementService::addDocumentToDocumentField)
                .forEach(uploadedDocumentType -> {
                    uploadedDocumentType.setCategoryId("C13");
                    documentTypeItems.add(createDocumentTypeItem(uploadedDocumentType, "ACAS Certificate"));
                });

        return documentTypeItems;
    }

    /**
     * Adds the claimant representative details to the case data.
     * @param caseData the case data
     * @param userToken the user token
     */
    public void addClaimantRepresentativeDetails(CaseData caseData, String userToken) {
        RepresentedTypeC claimantRepresentative;
        if (ObjectUtils.isEmpty(caseData.getRepresentativeClaimantType())) {
            claimantRepresentative = new RepresentedTypeC();
        } else {
            claimantRepresentative = caseData.getRepresentativeClaimantType();
        }
        UserDetails userDetails = userIdamService.getUserDetails(userToken);
        claimantRepresentative.setNameOfRepresentative(userDetails.getFirstName() + " " + userDetails.getLastName());
        claimantRepresentative.setRepresentativeEmailAddress(userDetails.getEmail());
        // TODO figure out if we can get claimant organisation name
        caseData.setRepresentativeClaimantType(claimantRepresentative);
    }
}

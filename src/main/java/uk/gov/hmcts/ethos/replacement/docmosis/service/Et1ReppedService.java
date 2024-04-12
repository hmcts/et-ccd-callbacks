package uk.gov.hmcts.ethos.replacement.docmosis.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.gov.dwp.regex.InvalidPostcodeException;
import uk.gov.hmcts.ecm.common.exceptions.PdfServiceException;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.ecm.common.model.ccd.CaseAssignmentUserRolesRequest;
import uk.gov.hmcts.ecm.common.model.helper.DocumentCategory;
import uk.gov.hmcts.ecm.common.model.helper.DocumentConstants;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.ecm.common.service.JurisdictionCodesMapperService;
import uk.gov.hmcts.ecm.common.service.PostcodeToOfficeService;
import uk.gov.hmcts.ecm.common.service.pdf.PdfService;
import uk.gov.hmcts.et.common.model.ccd.Address;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.Organisation;
import uk.gov.hmcts.et.common.model.ccd.types.OrganisationPolicy;
import uk.gov.hmcts.et.common.model.ccd.types.OrganisationsResponse;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.ClaimantSolicitorRole;
import uk.gov.hmcts.ethos.replacement.docmosis.rdprofessional.OrganisationClient;
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
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.ET1_ATTACHMENT;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.DocumentHelper.createDocumentTypeItem;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper.getFirstListItem;

@Service
@Slf4j
@RequiredArgsConstructor
public class Et1ReppedService {
    private static final String ET1_EN_PDF = "ET1_2222.pdf";
    private static final String ET1_CY_PDF = "CY_ET1_2222.pdf";

    private final AcasService acasService;
    private final AuthTokenGenerator authTokenGenerator;
    private final CcdCaseAssignment ccdCaseAssignment;
    private final DocumentManagementService documentManagementService;
    private final JurisdictionCodesMapperService jurisdictionCodesMapperService;
    private final OrganisationClient organisationClient;
    private final PdfService pdfService;
    private final PostcodeToOfficeService postcodeToOfficeService;
    private final TornadoService tornadoService;
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

    /**
     * Adds some base data to the case.
     * @param caseTypeId the case type ID
     * @param caseData the case data
     */
    public void addDefaultData(String caseTypeId, CaseData caseData) {
        tribunalOfficesService.addManagingOffice(caseData, caseTypeId);
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
        try {
            DocumentTypeItem englishEt1 = createEt1DocumentType(caseDetails, userToken, ET1_EN_PDF);
            // TODO add logic to descide if we need a welsh version
            // DocumentTypeItem welshEt1 = createEt1DocumentType(caseDetails, userToken, ET1_CY_PDF);

            List<DocumentTypeItem> acasCertificates = retrieveAndAddAcasCertificates(caseDetails.getCaseData(),
                    userToken);
            addDocsToClaim(caseDetails.getCaseData(), englishEt1, acasCertificates);
        } catch (Exception e) {
            log.error("Failed to create and upload ET1 documents", e);
        }
    }

    private void addDocsToClaim(CaseData caseData, DocumentTypeItem et1,
                                List<DocumentTypeItem> acasCertificates) {
        List<DocumentTypeItem> documentList = new ArrayList<>();
        if (!ObjectUtils.isEmpty(et1)) {
            documentList.add(et1);
        }
        if (caseData.getEt1SectionThreeDocumentUpload() != null) {
            UploadedDocumentType et1Attachment = caseData.getEt1SectionThreeDocumentUpload();
            et1Attachment.setCategoryId(DocumentCategory.ET1_ATTACHMENT.getCategory());
            documentList.add(createDocumentTypeItem(caseData.getEt1SectionThreeDocumentUpload(), ET1_ATTACHMENT));
        }
        documentList.addAll(acasCertificates);
        caseData.setClaimantDocumentCollection(documentList);
        caseData.setDocumentCollection(documentList);
    }

    /**
     * Creates a draft ET1 PDF.
     * @param caseDetails the case details
     * @param userToken the user token
     */
    public void createDraftEt1(CaseDetails caseDetails, String userToken) {
        try {
            caseDetails.getCaseData().setManagingOffice(null);
            DocumentInfo documentInfo = createEt1(caseDetails, userToken, ET1_EN_PDF);
            documentInfo.setMarkUp(documentInfo.getMarkUp().replace("Document",
                    "Draft ET1 - " + caseDetails.getCaseId()));
            caseDetails.getCaseData().setDocMarkUp(documentInfo.getMarkUp());
        } catch (Exception e) {
            log.error("Failed to create and upload draft ET1 documents", e);
        }
    }

    private DocumentTypeItem createEt1DocumentType(CaseDetails caseDetails, String userToken, String pdfSource)
            throws PdfServiceException {
        DocumentInfo documentInfo = createEt1(caseDetails, userToken, pdfSource);

        UploadedDocumentType uploadedDocumentType = documentManagementService.addDocumentToDocumentField(documentInfo);
        uploadedDocumentType.setCategoryId(DocumentCategory.ET1.getCategory());
        return createDocumentTypeItem(uploadedDocumentType, DocumentConstants.ET1);
    }

    private DocumentInfo createEt1(CaseDetails caseDetails, String userToken, String pdfSource)
            throws PdfServiceException {
        byte[] pdf = pdfService.convertCaseToPdf(caseDetails.getCaseData(), pdfSource);
        if (ObjectUtils.isEmpty(pdf)) {
            throw new PdfServiceException("Failed to create ET1 PDF", new NullPointerException());
        }
        return tornadoService.createDocumentInfoFromBytes(userToken,
                pdf,
                getEt1DocumentName(caseDetails.getCaseData()),
                caseDetails.getCaseTypeId());
    }

    private String getEt1DocumentName(CaseData caseData) {
        return "ET1 - " + caseData.getClaimant() + ".pdf";
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
                .map(acasNumber -> {
                    try {
                        return acasService.getAcasCertificates(caseData, acasNumber, userToken);
                    } catch (JsonProcessingException e) {
                        log.error("Failed to retrieve ACAS Certificate", e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();

        if (CollectionUtils.isEmpty(documentInfoList)) {
            return new ArrayList<>();
        }

        List<DocumentTypeItem> documentTypeItems = new ArrayList<>();
        documentInfoList.stream()
                .map(documentManagementService::addDocumentToDocumentField)
                .forEach(uploadedDocumentType -> {
                    uploadedDocumentType.setCategoryId(DocumentCategory.ACAS_CERTIFICATE.getCategory());
                    documentTypeItems.add(createDocumentTypeItem(uploadedDocumentType,
                            DocumentConstants.ACAS_CERTIFICATE));
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
        claimantRepresentative.setRepresentativeReference(caseData.getRepresentativeReferenceNumber());
        claimantRepresentative.setRepresentativePreference(
                getFirstListItem(caseData.getRepresentativeContactPreference()));
        claimantRepresentative.setRepresentativePhoneNumber(caseData.getRepresentativePhoneNumber());
        OrganisationsResponse organisationDetails = getOrganisationDetailsFromUserId(userToken, userDetails.getUid());
        if (!ObjectUtils.isEmpty(organisationDetails)) {
            log.info("Adding ref data organisation details to case {}", caseData.getEthosCaseReference());
            claimantRepresentative.setMyHmctsOrganisation(Organisation.builder()
                    .organisationID(organisationDetails.getOrganisationIdentifier())
                    .organisationName(organisationDetails.getName())
                    .build());
            claimantRepresentative.setNameOfOrganisation(organisationDetails.getName());
            claimantRepresentative.setRepresentativeAddress(getOrganisationAddress(organisationDetails));
            setClaimantRepOrgPolicy(caseData, organisationDetails);
        }
        caseData.setClaimantRepresentedQuestion(YES);
        caseData.setRepresentativeClaimantType(claimantRepresentative);
    }

    @NotNull
    private static Address getOrganisationAddress(OrganisationsResponse organisationDetails) {
        Address organisationAddress = new Address();
        if (CollectionUtils.isEmpty(organisationDetails.getContactInformation())) {
            return organisationAddress;
        }
        organisationAddress.setAddressLine1(organisationDetails.getContactInformation().get(0).getAddressLine1());
        organisationAddress.setAddressLine2(organisationDetails.getContactInformation().get(0).getAddressLine2());
        organisationAddress.setAddressLine3(organisationDetails.getContactInformation().get(0).getAddressLine3());
        organisationAddress.setPostCode(organisationDetails.getContactInformation().get(0).getPostCode());
        organisationAddress.setPostTown(organisationDetails.getContactInformation().get(0).getTownCity());
        organisationAddress.setCounty(organisationDetails.getContactInformation().get(0).getCounty());
        organisationAddress.setCountry(organisationDetails.getContactInformation().get(0).getCountry());
        return organisationAddress;
    }

    /**
     * Retrieves the organisation details from the user ID.
     * @param userToken the user token
     * @param userId the user ID
     * @return the organisation details
     */
    public OrganisationsResponse getOrganisationDetailsFromUserId(String userToken, String userId) {
        try {
            ResponseEntity<OrganisationsResponse> response =
                    organisationClient.retrieveOrganisationDetailsByUserId(userToken,
                            authTokenGenerator.generate(),
                            userId);
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            }
        } catch (Exception e) {
            log.error("Failed to retrieve organisation details", e);
        }
        return null;
    }

    /**
     * Assigns the case access to the claimant representative.
     * @param caseDetails the case details
     * @param userToken the user token
     */
    public void assignCaseAccess(CaseDetails caseDetails, String userToken) {
        UserDetails claimantRepUser = userIdamService.getUserDetails(userToken);
        OrganisationsResponse organisation = getOrganisationDetailsFromUserId(userToken, claimantRepUser.getUid());

        log.info("Adding claimant solicitor role to case {}", caseDetails.getCaseId());

        CaseAssignmentUserRolesRequest addCaseUserRole = ccdCaseAssignment.getCaseAssignmentRequest(
                Long.valueOf(caseDetails.getCaseId()),
                claimantRepUser.getUid(),
                organisation.getOrganisationIdentifier(),
                ClaimantSolicitorRole.CLAIMANTSOLICITOR.getCaseRoleLabel());
        ccdCaseAssignment.addCaseUserRoles(addCaseUserRole);

        log.info("Removing creator role from case {}", caseDetails.getCaseId());

        CaseAssignmentUserRolesRequest removeCaseUserRole = ccdCaseAssignment.getCaseAssignmentRequest(
                Long.valueOf(caseDetails.getCaseId()),
                claimantRepUser.getUid(),
                organisation.getOrganisationIdentifier(),
                "[CREATOR]");

        ccdCaseAssignment.removeCaseUserRoles(removeCaseUserRole);

        log.info("Successfully modified roles for case {} ", caseDetails.getCaseId());
    }

    private void setClaimantRepOrgPolicy(CaseData caseData, OrganisationsResponse organisation) {
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder()
                .organisation(Organisation.builder()
                        .organisationID(organisation.getOrganisationIdentifier())
                        .organisationName(organisation.getName())
                        .build())
                .orgPolicyCaseAssignedRole(ClaimantSolicitorRole.CLAIMANTSOLICITOR.getCaseRoleLabel())
                .build();
        caseData.setClaimantRepresentativeOrganisationPolicy(organisationPolicy);
    }

}

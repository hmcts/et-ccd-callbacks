package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.dwp.regex.InvalidPostcodeException;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.ecm.common.model.ccd.CaseAssignmentUserWithOrganisationRolesRequest;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.ecm.common.service.JurisdictionCodesMapperService;
import uk.gov.hmcts.ecm.common.service.PostcodeToOfficeService;
import uk.gov.hmcts.et.common.model.ccd.Address;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;
import uk.gov.hmcts.et.common.model.ccd.types.Organisation;
import uk.gov.hmcts.et.common.model.ccd.types.OrganisationPolicy;
import uk.gov.hmcts.et.common.model.ccd.types.OrganisationsResponse;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.ClaimantSolicitorRole;
import uk.gov.hmcts.ethos.replacement.docmosis.rdprofessional.OrganisationClient;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Strings.isNullOrEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Et1ReppedHelper.setEt1Statuses;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper.getFirstListItem;

@Service
@Slf4j
@RequiredArgsConstructor
public class Et1ReppedService {

    private final AuthTokenGenerator authTokenGenerator;
    private final CcdCaseAssignment ccdCaseAssignment;
    private final JurisdictionCodesMapperService jurisdictionCodesMapperService;
    private final OrganisationClient organisationClient;
    private final PostcodeToOfficeService postcodeToOfficeService;
    private final TribunalOfficesService tribunalOfficesService;
    private final UserIdamService userIdamService;
    private final AdminUserService adminUserService;
    private final Et1SubmissionService et1SubmissionService;

    private static final String ET1_EN_PDF = "ET1_0125.pdf";
    private final List<TribunalOffice> liveTribunalOffices = List.of(TribunalOffice.LEEDS,
            TribunalOffice.MIDLANDS_EAST, TribunalOffice.BRISTOL, TribunalOffice.LONDON_CENTRAL,
            TribunalOffice.LONDON_SOUTH, TribunalOffice.LONDON_EAST, TribunalOffice.MANCHESTER,
            TribunalOffice.NEWCASTLE, TribunalOffice.WATFORD, TribunalOffice.WALES, TribunalOffice.MIDLANDS_WEST);

    /**
     * Validates the postcode and region.
     * @param caseData the case data
     * @param caseTypeId the case type ID
     * @return a list of validation messages
     * @throws InvalidPostcodeException if the postcode is invalid
     */
    public List<String> validatePostcode(CaseData caseData, String caseTypeId)
            throws InvalidPostcodeException {
        if (ObjectUtils.isEmpty(caseData.getEt1ReppedTriageAddress())
            || isNullOrEmpty(caseData.getEt1ReppedTriageAddress().getPostCode())) {
            return Collections.singletonList("Please enter a valid postcode");
        }

        Optional<TribunalOffice> office = postcodeToOfficeService.getTribunalOfficeFromPostcode(
                caseData.getEt1ReppedTriageAddress().getPostCode());
        if (office.isEmpty()) {
            return Collections.singletonList("Could not match postcode to a tribunal office. "
                                             + "Please check the postcode.");
        }

        caseData.setEt1ReppedTriageYesNo(NO);
        switch (caseTypeId) {
            case ENGLANDWALES_CASE_TYPE_ID -> {
                if (TribunalOffice.isScotlandOffice(office.get().getOfficeName())) {
                    return Collections.singletonList(
                            "Please use the link below to submit your claim to the correct office");
                }
                if (!liveTribunalOffices.contains(office.get())) {
                    return Collections.emptyList();
                }
            }
            case SCOTLAND_CASE_TYPE_ID -> {
                if (TribunalOffice.isEnglandWalesOffice(office.get().getOfficeName())) {
                    return Collections.singletonList(
                            "Please use the link below to submit your claim to the correct office");
                }
            }
            default -> {
                // Do nothing for unmatched casetypes
            }
        }

        caseData.setEt1ReppedTriageYesNo(YES);
        return Collections.emptyList();
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
        caseData.setCaseSource("MyHMCTS");
    }

    /**
     * Creates a draft ET1 PDF.
     * @param caseDetails the case details
     * @param userToken the user token
     */
    public void createDraftEt1(CaseDetails caseDetails, String userToken) {
        try {
            CaseData caseData = caseDetails.getCaseData();
            caseData.setManagingOffice(null);
            caseData.setReceiptDate(null);
            DocumentInfo documentInfo = et1SubmissionService.createEt1(caseDetails, userToken, ET1_EN_PDF);
            documentInfo.setMarkUp(documentInfo.getMarkUp().replace("Document",
                    "Draft ET1 - " + caseDetails.getCaseId()));
            caseData.setDocMarkUp(documentInfo.getMarkUp());
            caseData.setDownloadDraftEt1Date(LocalDate.now().toString());
            setEt1Statuses(caseData, caseDetails.getCaseId());

        } catch (Exception e) {
            log.error("Failed to create and upload draft ET1 documents", e);
        }
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
        claimantRepresentative.setHearingContactLanguage(caseData.getHearingContactLanguage());
        claimantRepresentative.setContactLanguageQuestion(caseData.getContactLanguageQuestion());
        claimantRepresentative.setRepresentativeAttendHearing(caseData.getRepresentativeAttendHearing());
        OrganisationsResponse organisationDetails = getOrganisationDetailsFromUserId(userDetails.getUid());
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
     * @param userId the user ID
     * @return the organisation details
     */
    public OrganisationsResponse getOrganisationDetailsFromUserId(String userId) {
        try {
            String userToken = adminUserService.getAdminUserToken();
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
    public void assignCaseAccess(CaseDetails caseDetails, String userToken) throws IOException {
        UserDetails claimantRepUser = userIdamService.getUserDetails(userToken);
        OrganisationsResponse organisation = getOrganisationDetailsFromUserId(claimantRepUser.getUid());

        log.info("Adding claimant solicitor role to case {}", caseDetails.getCaseId());

        CaseAssignmentUserWithOrganisationRolesRequest addCaseUserRole = ccdCaseAssignment.getCaseAssignmentRequest(
                Long.valueOf(caseDetails.getCaseId()),
                claimantRepUser.getUid(),
                organisation.getOrganisationIdentifier(),
                ClaimantSolicitorRole.CLAIMANTSOLICITOR.getCaseRoleLabel());
        ccdCaseAssignment.addCaseUserRoles(addCaseUserRole);

        log.info("Removing creator role from case {}", caseDetails.getCaseId());

        CaseAssignmentUserWithOrganisationRolesRequest removeCaseUserRole = ccdCaseAssignment.getCaseAssignmentRequest(
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

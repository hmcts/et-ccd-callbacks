package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.et.common.model.ccd.AuditEvent;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CallbackRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.types.ChangeOrganisationRequest;
import uk.gov.hmcts.et.common.model.ccd.types.Organisation;
import uk.gov.hmcts.et.common.model.ccd.types.OrganisationsResponse;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC;
import uk.gov.hmcts.et.common.model.ccd.types.TriageQuestions;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.CcdInputOutputException;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.NocClaimantHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.rdprofessional.OrganisationClient;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.Et1ReppedService.getOrganisationAddress;

@Service
@RequiredArgsConstructor
@Slf4j
public class NocClaimantRepresentativeService {
    public static final String NOC_REQUEST = "nocRequest";

    private final AuthTokenGenerator authTokenGenerator;
    private final OrganisationClient organisationClient;
    private final AdminUserService adminUserService;
    private final NocCcdService nocCcdService;
    private final NocNotificationService nocNotificationService;
    private final CcdCaseAssignment ccdCaseAssignment;
    private final CcdClient ccdClient;
    private final NocService nocService;
    private final NocClaimantHelper nocClaimantHelper;

    /**
     * Update claimant representation based on NoC request.
     * @param caseDetails containing case data with change organisation request field
     * @return updated case data
     * @throws IOException if CCD operation fails
     */
    public CaseData updateClaimantRepresentation(CaseDetails caseDetails, String userToken) throws IOException {
        CaseData caseData = caseDetails.getCaseData();
        String caseId = caseDetails.getCaseId();
        updateClaimantRepMap(caseData, caseId);
        prepopulateOrgAddress(caseData, userToken);

        return caseData;
    }

    private void updateClaimantRepMap(CaseData caseData, String caseId) throws IOException {
        if (ObjectUtils.isEmpty(caseData.getTriageQuestions())) {
            caseData.setTriageQuestions(new TriageQuestions());
        }
        caseData.setClaimantRepresentedQuestion(YES);
        caseData.setClaimantRepresentativeRemoved(NO);
        caseData.getTriageQuestions().setClaimantRepresentedQuestion(YES);
        RepresentedTypeC claimantRep = createRepresentedTypeC(caseId, caseData.getChangeOrganisationRequestField());
        caseData.setRepresentativeClaimantType(claimantRep);
    }

    private RepresentedTypeC createRepresentedTypeC(String caseId, ChangeOrganisationRequest change)
            throws IOException {
        String accessToken = adminUserService.getAdminUserToken();

        Optional<AuditEvent> auditEvent =
                nocCcdService.getLatestAuditEventByName(accessToken, caseId, NOC_REQUEST);
        Optional<UserDetails> userDetailsOptional = auditEvent
                .map(event -> adminUserService.getUserDetails(adminUserService.getAdminUserToken(),
                        event.getUserId()));

        UserDetails userDetails = userDetailsOptional.orElseThrow();

        RepresentedTypeC claimantRep = new RepresentedTypeC();
        claimantRep.setNameOfRepresentative(userDetails.getFirstName() + " " + userDetails.getLastName());
        claimantRep.setRepresentativeEmailAddress(userDetails.getEmail());
        claimantRep.setMyHmctsOrganisation(change.getOrganisationToAdd());

        return claimantRep;
    }

    private void prepopulateOrgAddress(CaseData caseData, String userToken) {
        List<OrganisationsResponse> organisationList = organisationClient.getOrganisations(
                userToken, authTokenGenerator.generate());

        RepresentedTypeC claimantRep = caseData.getRepresentativeClaimantType();
        if (claimantRep != null && claimantRep.getMyHmctsOrganisation() != null) {
            String orgId = claimantRep.getMyHmctsOrganisation().getOrganisationID();
            Optional<OrganisationsResponse> organisationResponse = organisationList.stream()
                    .filter(org -> org.getOrganisationIdentifier().equals(orgId))
                    .findFirst();

            if (organisationResponse.isPresent()) {
                OrganisationsResponse organisation = organisationResponse.get();
                claimantRep.setRepresentativeAddress(getOrganisationAddress(organisation));
                claimantRep.setNameOfOrganisation(organisation.getName());
                caseData.setRepresentativeClaimantType(claimantRep);
            }
        }
    }

    public void updateClaimantRepAccess(CallbackRequest callbackRequest)
            throws IOException {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseDetails caseDetailsBefore = callbackRequest.getCaseDetailsBefore();
        CaseData caseDataBefore = caseDetailsBefore.getCaseData();
        CaseData caseData = caseDetails.getCaseData();

        ChangeOrganisationRequest changeRequest = identifyRepresentationChanges(caseData,
                caseDataBefore);

        try {
            nocNotificationService.sendNotificationOfChangeEmails(caseDetailsBefore, caseDetails, changeRequest);
        } catch (Exception exception) {
            log.error(exception.getMessage(), exception);
        }

        if (changeRequest != null
                && changeRequest.getOrganisationToRemove() != null) {
            try {
                nocService.removeOrganisationRepresentativeAccess(caseDetails.getCaseId(), changeRequest);
            } catch (IOException e) {
                throw new CcdInputOutputException("Failed to remove organisation representative access", e);
            }
        }

        String accessToken = adminUserService.getAdminUserToken();
        CCDRequest ccdRequest = nocCcdService.startEventForUpdateRepresentation(accessToken,
                caseDetails.getJurisdiction(), caseDetails.getCaseTypeId(), caseDetails.getCaseId());
        callbackRequest.getCaseDetails().getCaseData().setChangeOrganisationRequestField(changeRequest);
        ccdRequest.getCaseDetails().setCaseData(ccdCaseAssignment.applyNocAsAdmin(callbackRequest).getData());

        if (YES.equals(caseData.getClaimantRepresentedQuestion())) {
            RepresentedTypeC claimantRep = caseData.getRepresentativeClaimantType();
            if (claimantRep != null && claimantRep.getRepresentativeEmailAddress() != null) {
                nocService.grantClaimantRepAccess(accessToken,
                        caseData.getRepresentativeClaimantType().getRepresentativeEmailAddress(),
                        caseDetails.getCaseId(),
                        changeRequest.getOrganisationToAdd());
            }
        }

        ccdClient.submitUpdateRepEvent(
                accessToken,
                    Map.of("changeOrganisationRequestField",
                            callbackRequest.getCaseDetails().getCaseData().getChangeOrganisationRequestField()),
                    caseDetails.getCaseTypeId(),
                    caseDetails.getJurisdiction(),
                    ccdRequest,
                    caseDetails.getCaseId());
    }

    public ChangeOrganisationRequest identifyRepresentationChanges(CaseData  after, CaseData before) {
        Organisation newRepOrg = after.getRepresentativeClaimantType() != null
                ? after.getRepresentativeClaimantType().getMyHmctsOrganisation() : null;
        Organisation oldRepOrg = before.getRepresentativeClaimantType() != null
                ? before.getRepresentativeClaimantType().getMyHmctsOrganisation() : null;
        ChangeOrganisationRequest changeRequests;

        if (!Objects.equals(newRepOrg, oldRepOrg)) {
            changeRequests = nocClaimantHelper.createChangeRequest(newRepOrg, oldRepOrg);
        } else {
            changeRequests = nocClaimantHelper.createChangeRequest(newRepOrg, null);
        }

        return changeRequests;
    }
}

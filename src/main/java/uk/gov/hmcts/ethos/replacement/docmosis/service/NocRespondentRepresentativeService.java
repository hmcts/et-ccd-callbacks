package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.et.common.model.ccd.Address;
import uk.gov.hmcts.et.common.model.ccd.AuditEvent;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CallbackRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.ChangeOrganisationRequest;
import uk.gov.hmcts.et.common.model.ccd.types.Organisation;
import uk.gov.hmcts.et.common.model.ccd.types.OrganisationAddress;
import uk.gov.hmcts.et.common.model.ccd.types.OrganisationsResponse;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.SolicitorRole;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.CcdInputOutputException;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.NocHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.NocRespondentHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.NoticeOfChangeFieldPopulator;
import uk.gov.hmcts.ethos.replacement.docmosis.rdprofessional.OrganisationClient;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.AddressUtils;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import static com.google.common.base.Strings.isNullOrEmpty;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@Service
@RequiredArgsConstructor
@Slf4j
public class NocRespondentRepresentativeService {
    public static final String NOC_REQUEST = "nocRequest";
    private final NoticeOfChangeFieldPopulator noticeOfChangeFieldPopulator;

    private final CaseConverter caseConverter;

    private final NocCcdService nocCcdService;

    private final AdminUserService adminUserService;

    private final NocRespondentHelper nocRespondentHelper;

    private final NocNotificationService nocNotificationService;

    private final CcdClient ccdClient;

    private final CcdCaseAssignment ccdCaseAssignment;

    private final OrganisationClient organisationClient;

    private final AuthTokenGenerator authTokenGenerator;

    private final NocHelper noCHelper;

    /**
     * Add respondent organisation policy and notice of change answer fields to the case data.
     * @param caseData case data
     * @return modified case data
     */
    public CaseData prepopulateOrgPolicyAndNoc(CaseData caseData) {
        Map<String, Object> caseDataAsMap = caseConverter.toMap(caseData);
        Map<String, Object> generatedContent =
            noticeOfChangeFieldPopulator.generate(caseData);
        caseDataAsMap.putAll(generatedContent);
        return  caseConverter.convert(caseDataAsMap, CaseData.class);
    }

    /**
     * Replace the organisation policy and relevant respondent representative mapping with
     * new respondent representative details.
     * @param caseDetails containing case data with change organisation request field
     * @return updated case
     */
    public CaseData updateRespondentRepresentation(CaseDetails caseDetails) throws IOException {
        CaseData caseData = caseDetails.getCaseData();
        resetRespondentRepresentativeRemovedField(caseData);
        Map<String, Object> caseDataAsMap = caseConverter.toMap(caseData);
        Map<String, Object> repCollection = updateRepresentationMap(caseData, caseDetails.getCaseId());
        caseDataAsMap.putAll(repCollection);
        return  caseConverter.convert(caseDataAsMap, CaseData.class);
    }

    private static void resetRespondentRepresentativeRemovedField(CaseData caseData) {
        ChangeOrganisationRequest change = findChangeOrganisationRequest(caseData);
        SolicitorRole role = SolicitorRole.from(change.getCaseRoleId().getSelectedCode()).orElseThrow();
        RespondentSumTypeItem respondent = caseData.getRespondentCollection().get(role.getIndex());
        respondent.getValue().setRepresentativeRemoved(NO);
    }

    private static ChangeOrganisationRequest findChangeOrganisationRequest(CaseData caseData) {
        ChangeOrganisationRequest change = caseData.getChangeOrganisationRequestField();
        if (isEmpty(change) || isEmpty(change.getCaseRoleId()) || isEmpty(change.getOrganisationToAdd())) {
            throw new IllegalStateException("Invalid or missing ChangeOrganisationRequest: " + change);
        }
        return change;
    }

    private Map<String, Object> updateRepresentationMap(CaseData caseData, String caseId) throws IOException {

        final ChangeOrganisationRequest change = findChangeOrganisationRequest(caseData);

        String accessToken = adminUserService.getAdminUserToken();

        Optional<AuditEvent> auditEvent =
            nocCcdService.getLatestAuditEventByName(accessToken, caseId, NOC_REQUEST);

        Optional<UserDetails> userDetails = auditEvent
            .map(event -> adminUserService.getUserDetails(event.getUserId()));

        final SolicitorRole role = SolicitorRole.from(change.getCaseRoleId().getSelectedCode()).orElseThrow();

        RespondentSumTypeItem respondent = caseData.getRespondentCollection().get(role.getIndex());

        RepresentedTypeR addedSolicitor = nocRespondentHelper.generateNewRepDetails(change, userDetails, respondent);

        List<RepresentedTypeRItem> repCollection = defaultIfNull(caseData.getRepCollection(), new ArrayList<>());

        int repIndex = nocRespondentHelper.getIndexOfRep(respondent, repCollection);

        if (repIndex >= 0) {
            repCollection.get(repIndex).setValue(addedSolicitor);
        } else {
            //assumption is NOC will take care of replacing value in org policy
            RepresentedTypeRItem representedTypeRItem = new RepresentedTypeRItem();
            representedTypeRItem.setValue(addedSolicitor);
            repCollection.add(representedTypeRItem);
        }

        return Map.of(SolicitorRole.CASE_FIELD, repCollection);
    }

    /**
     * Gets the case data before and after and checks respondent org policies for differences.
     * For each difference creates a change organisation request to remove old organisation and add new.
     * For each change request trigger the updateRepresentation event against CCD.
     * Notifications are sent to Tribunal, Claimant, Respondent, New Rep, Old Rep (if there is existing org).
     * Previous Representative's access is revoked.
     * @param callbackRequest - containing case details before event and after the event
     * @throws IOException - exception thrown by ccd
     */
    @SuppressWarnings({"PMD.PrematureDeclaration", "checkstyle:VariableDeclarationUsageDistance"})
    public void updateRepresentativesAccess(CallbackRequest callbackRequest, String currentUserEmail)
            throws IOException {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseDetails caseDetailsBefore = callbackRequest.getCaseDetailsBefore();
        CaseData caseDataBefore = caseDetailsBefore.getCaseData();
        CaseData caseData = caseDetails.getCaseData();

        List<ChangeOrganisationRequest> changeRequests = identifyRepresentationChanges(caseData,
            caseDataBefore);

        String accessToken = adminUserService.getAdminUserToken();

        for (ChangeOrganisationRequest changeRequest : changeRequests) {
            log.info("About to apply representation change {}", changeRequest);

            CCDRequest ccdRequest = nocCcdService.updateCaseRepresentation(accessToken,
                    caseDetails.getJurisdiction(), caseDetails.getCaseTypeId(), caseDetails.getCaseId());

            log.info("Representation change applied {}", changeRequest);

            try {
                nocNotificationService.sendNotificationOfChangeEmails(caseDetailsBefore, caseDetails, changeRequest,
                        currentUserEmail);
            } catch (Exception exception) {
                log.error(exception.getMessage(), exception);
            }

            if (changeRequest != null
                    && changeRequest.getOrganisationToRemove() != null) {
                try {
                    noCHelper.removeOrganisationRepresentativeAccess(caseDetails.getCaseId(), changeRequest);
                } catch (IOException e) {
                    throw new CcdInputOutputException("Failed to remove organisation representative access", e);
                }
            }

            callbackRequest.getCaseDetails().getCaseData().setChangeOrganisationRequestField(changeRequest);
            ccdRequest.getCaseDetails().setCaseData(ccdCaseAssignment.applyNocAsAdmin(callbackRequest).getData());

            ccdClient.submitUpdateRepEvent(
                    accessToken,
                    Map.of("changeOrganisationRequestField",
                            ccdRequest.getCaseDetails().getCaseData().getChangeOrganisationRequestField()),
                    caseDetails.getCaseTypeId(),
                    caseDetails.getJurisdiction(),
                    ccdRequest,
                    caseDetails.getCaseId());
        }

    }

    /**
     * Identifies differences in representation.
     * @param after - case data after event
     * @param before - case data before event was triggered
     * @return list of change organisation requests for any changes detected
     */
    public List<ChangeOrganisationRequest> identifyRepresentationChanges(CaseData  after,
                                                                         CaseData before) {
        final List<RespondentSumTypeItem> newRespondents =
            defaultIfNull(after.getRespondentCollection(), new ArrayList<>());
        final Map<String, Organisation> newRespondentsOrganisations =
            nocRespondentHelper.getRespondentOrganisations(after);
        final Map<String, Organisation> oldRespondentsOrganisations =
            nocRespondentHelper.getRespondentOrganisations(before);
        final List<ChangeOrganisationRequest> changeRequests = new ArrayList<>();

        for (int i = 0; i < newRespondents.size(); i++) {
            SolicitorRole solicitorRole = Arrays.asList(SolicitorRole.values()).get(i);
            String respondentId = newRespondents.get(i).getId();

            Organisation newOrganisation = newRespondentsOrganisations.get(respondentId);
            Organisation oldOrganisation = oldRespondentsOrganisations.get(respondentId);

            if (!Objects.equals(newOrganisation, oldOrganisation)) {
                changeRequests.add(nocRespondentHelper
                    .createChangeRequest(newOrganisation, oldOrganisation, solicitorRole));
            }
        }

        return changeRequests;
    }

    /**
     * Add respondent representative organisation address to the case data.
     * @param caseData case data
     * @return modified case data
     */
    public CaseData prepopulateOrgAddress(CaseData caseData, String userToken) {
        List<RepresentedTypeRItem> repCollection = caseData.getRepCollection();

        if (CollectionUtils.isEmpty(repCollection)
                || repCollection.stream()
                        .noneMatch(r -> r.getValue() != null && YES.equals(r.getValue().getMyHmctsYesNo()))) {
            return caseData;
        }

        // get all Organisation Details
        List<OrganisationsResponse> organisationList = organisationClient.getOrganisations(
                userToken, authTokenGenerator.generate());

        for (RepresentedTypeRItem representative : repCollection) {
            RepresentedTypeR representativeDetails = representative.getValue();

            if (representativeDetails != null && YES.equals(representativeDetails.getMyHmctsYesNo())) {
                Organisation repOrg = representativeDetails.getRespondentOrganisation();

                if (repOrg != null && repOrg.getOrganisationID() != null) {
                    // get organisation details
                    Optional<OrganisationsResponse> organisation =
                            organisationList
                                    .stream()
                                    .filter(o -> o.getOrganisationIdentifier().equals(repOrg.getOrganisationID()))
                                    .findFirst();

                    organisation.ifPresent(orgResponse -> updateRepDetails(orgResponse, representativeDetails));
                }
            }
        }

        return caseData;
    }

    private void updateRepDetails(OrganisationsResponse orgRes, RepresentedTypeR repDetails) {
        repDetails.setNameOfOrganisation(orgRes.getName());

        if (!CollectionUtils.isEmpty(orgRes.getContactInformation())) {
            Address repAddress = repDetails.getRepresentativeAddress();
            if (AddressUtils.isNullOrEmpty(repAddress)) {
                repAddress = AddressUtils.createIfNull(repDetails.getRepresentativeAddress());
                OrganisationAddress orgAddress = orgRes.getContactInformation().getFirst();
                // update Representative Address with Org Address
                repAddress.setAddressLine1(orgAddress.getAddressLine1());
                repAddress.setAddressLine2(orgAddress.getAddressLine2());
                repAddress.setAddressLine3(orgAddress.getAddressLine3());
                repAddress.setPostTown(orgAddress.getTownCity());
                repAddress.setCounty(orgAddress.getCounty());
                repAddress.setCountry(orgAddress.getCountry());
                repAddress.setPostCode(orgAddress.getPostCode());
                repDetails.setRepresentativeAddress(repAddress);
            }
        }
    }

    /**
     * Assigns an ID to each non myHMCTS legal rep representing their legal firm.
     * @param repCollection Collection of representatives on the case
     */
    public void updateNonMyHmctsOrgIds(List<RepresentedTypeRItem> repCollection) {
        repCollection.stream()
                .map(RepresentedTypeRItem::getValue)
                .forEach(this::updateNonMyHmctsOrgId);
    }

    private void updateNonMyHmctsOrgId(RepresentedTypeR rep) {
        if (YES.equals(rep.getMyHmctsYesNo())) {
            return;
        }

        if (isNullOrEmpty(rep.getNonMyHmctsOrganisationId())) {
            rep.setNonMyHmctsOrganisationId(UUID.randomUUID().toString());
        }
    }
}

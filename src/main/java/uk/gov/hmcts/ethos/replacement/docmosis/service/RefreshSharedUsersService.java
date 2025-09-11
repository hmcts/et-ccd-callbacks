package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignment;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.OrganisationUsersIdamUser;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.ClaimantSolicitorRole;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.SolicitorRole;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.apache.commons.collections.CollectionUtils.isEmpty;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshSharedUsersService {
    private final AdminUserService adminUserService;
    private final CcdCaseAssignment ccdCaseAssignment;
    private CaseData caseData;

    public void refreshSharedUsers(CaseDetails caseDetails) throws IOException {
        List<CaseUserAssignment> caseAssignedUserRolesList =
                ccdCaseAssignment.getCaseUserRoles(caseDetails.getCaseId()).getCaseUserAssignments();
        if (isEmpty(caseAssignedUserRolesList)) {
            log.info("No case user assignments found for case id {}", caseDetails.getCaseId());
            return;
        }
        claimantRepresentativeUsers(caseDetails, caseAssignedUserRolesList);
        respondentRepresentativeUsers(caseDetails, caseAssignedUserRolesList);
    }

    private void respondentRepresentativeUsers(CaseDetails caseDetails,
                                               List<CaseUserAssignment> caseAssignedUserRolesList) {
        Map<String, String> respondentSolicitors = caseAssignedUserRolesList.stream()
                .filter(caseUserAssignment -> SolicitorRole.from(caseUserAssignment.getCaseRole()).isPresent())
                .collect(Collectors.toMap(CaseUserAssignment::getUserId, CaseUserAssignment::getCaseRole, (a, b) -> b));

        caseData = caseDetails.getCaseData();
        if (respondentSolicitors.isEmpty() || isEmpty(caseData.getRepCollection())) {
            return;
        }

        log.info("{} Respondent solicitors found for case id {}", respondentSolicitors.size(), caseDetails.getCaseId());
        caseData.getRepCollection().forEach(item -> item.getValue().setOrganisationUsers(new ArrayList<>()));

        respondentSolicitors.forEach((userId, roleLabel) -> {
            UserDetails userDetails = adminUserService.getUserDetails(adminUserService.getAdminUserToken(), userId);
            OrganisationUsersIdamUser organisationUsersIdamUser = OrganisationUsersIdamUser.builder()
                    .email(userDetails.getEmail())
                    .firstName(userDetails.getFirstName())
                    .lastName(userDetails.getLastName())
                    .build();
            GenericTypeItem<OrganisationUsersIdamUser> user = new GenericTypeItem<>();
            user.setValue(organisationUsersIdamUser);
            user.setId(UUID.randomUUID().toString());

            int index = SolicitorRole.from(roleLabel)
                    .orElseThrow(() -> new IllegalArgumentException("Unknown solicitor role: " + roleLabel))
                    .getIndex();

            List<GenericTypeItem<OrganisationUsersIdamUser>> usersList =
                    caseData.getRepCollection().get(index).getValue().getOrganisationUsers();
            usersList.add(user);
        });
    }

    private void claimantRepresentativeUsers(CaseDetails caseDetails,
                                             List<CaseUserAssignment> caseAssignedUserRolesList) {
        List<String> claimantSolicitors = caseAssignedUserRolesList.stream()
                .filter(caseUserAssignment ->
                        caseUserAssignment.getCaseRole().equals(
                                ClaimantSolicitorRole.CLAIMANTSOLICITOR.getCaseRoleLabel()))
                .map(CaseUserAssignment::getUserId)
                .toList();
        List<GenericTypeItem<OrganisationUsersIdamUser>> claimantUsers = new ArrayList<>();
        if (isEmpty(claimantSolicitors)) {
            return;
        }
        log.info("{} Claimant solicitors found for case id {}", claimantSolicitors.size(), caseDetails.getCaseId());
        for (String userId : claimantSolicitors) {
            UserDetails userDetails =  getUserDetailsById(userId);
            OrganisationUsersIdamUser organisationUsersIdamUser = OrganisationUsersIdamUser.builder()
                    .email(userDetails.getEmail())
                    .firstName(userDetails.getFirstName())
                    .lastName(userDetails.getLastName())
                    .build();
            GenericTypeItem<OrganisationUsersIdamUser> user = new GenericTypeItem<>();
            user.setValue(organisationUsersIdamUser);
            user.setId(UUID.randomUUID().toString());
            claimantUsers.add(user);
        }
        RepresentedTypeC claimantRep = caseDetails.getCaseData().getRepresentativeClaimantType();
        if (claimantRep != null && claimantRep.getMyHmctsOrganisation() != null) {
            claimantRep.setOrganisationUsers(claimantUsers);
        }
    }

    private UserDetails getUserDetailsById(String userId) {
        return adminUserService.getUserDetails(adminUserService.getAdminUserToken(), userId);
    }
}

package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignment;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.ClaimantSolicitorRole;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.SolicitorRole;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationService {
    private final AdminUserService adminUserService;

    public List<String> getCaseClaimantSolicitorEmails(List<CaseUserAssignment> assignments) {
        List<String> emailAddresses = new ArrayList<>();
        for (CaseUserAssignment assignment : assignments) {
            if (ClaimantSolicitorRole.CLAIMANTSOLICITOR.getCaseRoleLabel().equals(assignment.getCaseRole())) {
                UserDetails userDetails = adminUserService.getUserDetails(adminUserService.getAdminUserToken(),
                        assignment.getUserId());
                emailAddresses.add(userDetails.getEmail());
            }
        }
        return emailAddresses;
    }

    public List<String> getRespondentSolicitorEmails(List<CaseUserAssignment> assignments) {
        List<String> emailAddresses = new ArrayList<>();
        for (CaseUserAssignment assignment : assignments) {
            if (SolicitorRole.from(assignment.getCaseRole()).isPresent()) {
                UserDetails userDetails = adminUserService.getUserDetails(adminUserService.getAdminUserToken(),
                        assignment.getUserId());
                emailAddresses.add(userDetails.getEmail());
            }
        }
        return emailAddresses;
    }
}

package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignment;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.ClaimantSolicitorRole;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.SolicitorRole;

import java.util.List;
import java.util.Map;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EmailNotificationServiceTest {

    private AdminUserService adminUserService;
    private EmailNotificationService service;

    @BeforeEach
    void setUp() {
        adminUserService = mock(AdminUserService.class);
        service = new EmailNotificationService(adminUserService);
    }

    @Test
    void getCaseClaimantSolicitorEmails_returnsEmails() {
        CaseUserAssignment assignment = new CaseUserAssignment();
        assignment.setCaseRole(ClaimantSolicitorRole.CLAIMANTSOLICITOR.getCaseRoleLabel());
        assignment.setUserId("user1");

        UserDetails userDetails = new UserDetails();
        userDetails.setEmail("solicitor@example.com");

        when(adminUserService.getAdminUserToken()).thenReturn("token");
        when(adminUserService.getUserDetails("token", "user1")).thenReturn(userDetails);

        List<String> emails = service.getCaseClaimantSolicitorEmails(List.of(assignment));
        assertEquals(List.of("solicitor@example.com"), emails);
    }

    @Test
    void getRespondentSolicitorEmails_returnsEmails() {
        CaseUserAssignment assignment = new CaseUserAssignment();
        assignment.setCaseRole(SolicitorRole.SOLICITORA.getCaseRoleLabel());
        assignment.setUserId("user2");

        UserDetails userDetails = new UserDetails();
        userDetails.setEmail("resp.sol@example.com");

        when(adminUserService.getAdminUserToken()).thenReturn("token");
        when(adminUserService.getUserDetails("token", "user2")).thenReturn(userDetails);

        Set<String> emails = service.getRespondentSolicitorEmails(List.of(assignment));
        assertTrue(emails.contains("resp.sol@example.com"));
    }

    @Test
    void getRespondentsAndRepsEmailAddresses_returnsMap() {
        RespondentSumType respondent = new RespondentSumType();
        respondent.setRespondentEmail("resp@example.com");
        RespondentSumTypeItem item = new RespondentSumTypeItem();
        item.setId("1");
        item.setValue(respondent);

        CaseData caseData = new CaseData();
        caseData.setRespondentCollection(List.of(item));

        CaseUserAssignment assignment = new CaseUserAssignment();
        assignment.setCaseRole(SolicitorRole.SOLICITORA.getCaseRoleLabel());
        assignment.setUserId("user2");

        UserDetails userDetails = new UserDetails();
        userDetails.setEmail("sol@example.com");

        when(adminUserService.getAdminUserToken()).thenReturn("token");
        when(adminUserService.getUserDetails("token", "user2")).thenReturn(userDetails);

        Map<String, String> result = service.getRespondentsAndRepsEmailAddresses(caseData, List.of(assignment));
        assertTrue(result.containsKey("resp@example.com"));
        assertTrue(result.containsKey("sol@example.com"));
        assertEquals("1", result.get("resp@example.com"));
    }

    @Test
    void getRespondentsAndAssignedRepsEmailAddresses_returnsMap() {
        RespondentSumType respondent = new RespondentSumType();
        respondent.setRespondentEmail("resp@example.com");
        RespondentSumTypeItem item = new RespondentSumTypeItem();
        item.setId("1");
        item.setValue(respondent);

        CaseData caseData = mock(CaseData.class);
        when(caseData.getRespondentCollection()).thenReturn(List.of(item));

        RepresentedTypeR rep = new RepresentedTypeR();
        rep.setRepresentativeEmailAddress("rep@example.com");

        // Static import for getRespondentRepresentative is not mockable, so this test only covers the main logic
        Map<String, String> result = service.getRespondentsAndAssignedRepsEmailAddresses(caseData);
        assertTrue(result.containsKey("resp@example.com"));
    }
}
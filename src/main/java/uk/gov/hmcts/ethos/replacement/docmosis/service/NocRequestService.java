package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;

@Slf4j
@Service
@RequiredArgsConstructor
public class NocRequestService {

    @Value("${template.nocNotification.org-admin-not-representing}")
    private String nocOrgAdminNotRepresentingTemplateId;
    @Value("${template.nocNotification.noc-legal-rep-no-longer-assigned}")
    private String nocLegalRepNoLongerAssignedTemplateId;
    @Value("${template.nocNotification.noc-citizen-no-longer-represented}")
    private String nocCitizenNoLongerRepresentedTemplateId;
    @Value("${template.nocNotification.noc-other-party-not-represented}")
    private String nocOtherPartyNotRepresentedTemplateId;

    public void revokeClaimantLegalRep(String userToken, CaseDetails caseDetails) {
    }

    public void sendEmailToOrgAdmin() {
        // legalRepName
    }

    public void sendEmailToThisLegalRep() {
    }

    public void sendEmailToRemovedParty() {
        // legalRepOrg
        // linkToCitUI
    }

    public void sendEmailToOtherParty() {
        // party_name
        // linkToCitUI
    }
}

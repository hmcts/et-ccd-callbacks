package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignment;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.ClaimantSolicitorRole;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.SolicitorRole;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.EMPTY_STRING;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.NotificationHelper.getRespondentRepresentative;

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

    public Set<String> getRespondentSolicitorEmails(List<CaseUserAssignment> assignments) {
        Set<String> emailAddresses = new HashSet<>();
        for (CaseUserAssignment assignment : assignments) {
            if (SolicitorRole.from(assignment.getCaseRole()).isPresent()) {
                UserDetails userDetails = adminUserService.getUserDetails(adminUserService.getAdminUserToken(),
                        assignment.getUserId());
                emailAddresses.add(userDetails.getEmail());
            }
        }
        log.info("Respondent solicitor emails: {}", emailAddresses);
        return emailAddresses;
    }

    /**
     * Retrieves a list of email addresses for respondents and their representatives from the given case data.
     * this also includes respondent solicitors from the shared list.
     *
     * @param caseData the case data containing respondent and representative information
     * @return a mapping of email addresses and respondent ids for respondents and their representatives
     */
    public Map<String, String> getRespondentsAndRepsEmailAddresses(CaseData caseData,
                                                                   List<CaseUserAssignment> caseUserAssignments) {
        List<RespondentSumTypeItem> respondentCollection = caseData.getRespondentCollection();
        Map<String, String> emailAddressesMap = new ConcurrentHashMap<>();

        respondentCollection.forEach(respondentSumTypeItem ->
               getRespondentEmailAddress(respondentSumTypeItem, emailAddressesMap)
        );

        // Add respondent solicitors' emails from case assignments
        getRespondentSolicitorEmails(caseUserAssignments).forEach(email ->
                emailAddressesMap.put(email, EMPTY_STRING)
        );

        return emailAddressesMap;
    }

    /**
     * Retrieves a list of email addresses for respondents and their lead representatives from the given case data.
     * This only includes the main legal representative for each respondent,
     * not respondent solicitors from the shared list.
     *
     * @param caseData the case data containing respondent and representative information
     * @return a mapping of email addresses and respondent ids for respondents and their representatives
     */
    public Map<String, String> getRespondentsAndAssignedRepsEmailAddresses(CaseData caseData) {
        List<RespondentSumTypeItem> respondentCollection = caseData.getRespondentCollection();
        Map<String, String> emailAddressesMap = new ConcurrentHashMap<>();

        respondentCollection.forEach(respondentSumTypeItem -> {
            getRespondentEmailAddress(respondentSumTypeItem, emailAddressesMap);
            RepresentedTypeR representative = getRespondentRepresentative(caseData, respondentSumTypeItem.getValue());
            if (representative != null && StringUtils.isNotBlank(representative.getRepresentativeEmailAddress())) {
                emailAddressesMap.put(representative.getRepresentativeEmailAddress(), "");
            }
        });

        return emailAddressesMap;
    }

    /**
     * Retrieves a list of email addresses for respondents and the respondent id.
     *
     * @param respondentSumTypeItem respondentSumTypeItem
     * @param emailAddressesMap map of email addresses and respondent ids
     */
    private void getRespondentEmailAddress(RespondentSumTypeItem respondentSumTypeItem,
                                                 Map<String, String> emailAddressesMap) {
        RespondentSumType respondent = respondentSumTypeItem.getValue();
        String responseEmail = respondent.getResponseRespondentEmail();
        String respondentEmail = respondent.getRespondentEmail();

        if (StringUtils.isNotBlank(responseEmail)) {
            emailAddressesMap.put(responseEmail, respondentSumTypeItem.getId());
        } else if (StringUtils.isNotBlank(respondentEmail)) {
            emailAddressesMap.put(respondentEmail, respondentSumTypeItem.getId());
        }
    }
}

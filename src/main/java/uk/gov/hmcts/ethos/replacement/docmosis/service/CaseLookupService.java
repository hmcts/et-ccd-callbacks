package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.et.common.model.multiples.MultipleDetails;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.EMPLOYMENT;

@Slf4j
@Service
@RequiredArgsConstructor
public class CaseLookupService {
    private final AdminUserService adminUserService;
    private final CcdClient ccdClient;

    public CaseData getLeadCaseFromMultipleAsAdmin(MultipleDetails caseDetails) throws IOException {
        String adminToken = adminUserService.getAdminUserToken();
        return getLeadCaseFromMultiple(caseDetails, adminToken);
    }

    public CaseData getLeadCaseFromMultiple(MultipleDetails caseDetails, String auth) throws IOException {
        MultipleData caseData = caseDetails.getCaseData();
        Pattern pattern = Pattern.compile("(\\d{16})");
        Matcher matcher = pattern.matcher(caseData.getLeadCase());

        if (!matcher.find()) {
            throw new IllegalArgumentException("Could not find 16 digit case id for lead case");
        }

        String caseId = matcher.group(1);
        String caseTypeId = caseDetails.getCaseTypeId().replace("_Multiple", "");

        return getCaseData(caseTypeId, caseId, auth);
    }

    public CaseData getCaseDataAsAdmin(String caseTypeId, String caseId) throws IOException {
        String adminToken = adminUserService.getAdminUserToken();
        return getCaseData(caseTypeId, caseId, adminToken);
    }

    public CaseData getCaseData(String caseTypeId, String caseId, String auth) throws IOException {
        SubmitEvent submitEvent = ccdClient.retrieveCase(auth, caseTypeId, EMPLOYMENT, caseId);
        return submitEvent.getCaseData();
    }
}

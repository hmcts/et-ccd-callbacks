package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.types.TTL;

import java.io.IOException;
import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Strings.isNullOrEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.EMPLOYMENT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SUBMITTED_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.VETTED_STATE;

@RequiredArgsConstructor
@Service
@Slf4j
public class EcmMigrationService {

    private final AdminUserService adminUserService;
    private final CcdClient ccdClient;

    public void rollbackEcmMigration(CaseDetails caseDetails) throws IOException {
        log.info("Rolling back migration for case {}", caseDetails.getCaseId());
        CaseData caseData = caseDetails.getCaseData();
        String ecmCaseId = getEcmCaseId(caseData.getEcmCaseLink());
        String ecmCaseType = SCOTLAND_CASE_TYPE_ID.equals(caseDetails.getCaseTypeId())
                ? "Scotland"
                : getEcmCaseType(caseData.getManagingOffice());
        String adminUserToken = adminUserService.getAdminUserToken();
        var submitEvent = ccdClient.retrieveCase(adminUserToken, caseDetails.getCaseTypeId(),
                EMPLOYMENT, caseDetails.getCaseId());
        var ecmCcdRequest = ccdClient.startEventForEcmCase(adminUserToken, ecmCaseType, EMPLOYMENT,
                ecmCaseId, "rollbackMigrateCase");
        log.info("Rolling back migration for case {} with ECM case {}", caseDetails.getCaseId(), ecmCaseId);
        var ecmCaseDetails = ecmCcdRequest.getCaseDetails();
        ecmCaseDetails.getCaseData().setReformCaseLink(null);
        ecmCaseDetails.getCaseData().setStateAPI(
                VETTED_STATE.equals(submitEvent.getState()) ? SUBMITTED_STATE : submitEvent.getState());
        ccdClient.submitEventForEcmCase(adminUserToken, ecmCaseDetails, ecmCcdRequest);
        log.info("Migration rolled back for case {} and ECM case {}", caseDetails.getCaseId(),
                ecmCaseDetails.getCaseId());

        caseData.setEcmFeeGroupReference(null);
        caseData.setEcmCaseLink(null);
        caseData.setMigratedFromEcm(null);
        TTL ttl = new TTL();
        ttl.setSystemTTL(LocalDate.now().minusDays(1));
        caseData.setTtl(ttl);
    }

    private String getEcmCaseType(String managingOffice) {
        if (isNullOrEmpty(managingOffice)) {
            throw new IllegalArgumentException("Managing office is null or empty");
        }
        return switch (managingOffice) {
            case "London Central" -> "LondonCentral";
            case "London East" -> "LondonEast";
            case "London South" -> "LondonSouth";
            case "Midlands East" -> "MidlandsEast";
            case "Midlands West" -> "MidlandsWest";
            default -> managingOffice;
        };
    }

    private String getEcmCaseId(String ecmCaseLink) {
        if (isNullOrEmpty(ecmCaseLink)) {
            throw new IllegalArgumentException("ECM case link is null or empty");
        }

        Pattern pattern = Pattern.compile("(\\d{16})");
        Matcher matcher = pattern.matcher(ecmCaseLink);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Could not find 16 digit case id for ECM case");
        } else {
            return matcher.group(1);
        }
    }
}

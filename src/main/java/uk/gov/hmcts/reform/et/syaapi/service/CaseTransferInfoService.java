package uk.gov.hmcts.reform.et.syaapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.model.ccd.CaseAssignedUserRolesResponse;
import uk.gov.hmcts.ecm.common.model.ccd.CaseAssignmentUserRole;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.et.syaapi.config.interceptors.ResourceNotFoundException;
import uk.gov.hmcts.reform.et.syaapi.exception.CaseUserRoleNotFoundException;
import uk.gov.hmcts.reform.et.syaapi.exception.ManageCaseRoleException;
import uk.gov.hmcts.reform.et.syaapi.models.CaseTransferInfoResponse;
import uk.gov.hmcts.reform.et.syaapi.models.CaseTransferType;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.EXCEPTION_CASE_USER_ROLE_NOT_FOUND;

@Slf4j
@Service
@RequiredArgsConstructor
public class CaseTransferInfoService {

    static final String TRANSFERRED_STATE = "Transferred";
    static final String TRANSFERRED_TO_ECM = "Transferred to ECM";
    static final String TRANSFERRED_TO_PREFIX = "Transferred to ";
    private static final Pattern TRANSFERRED_CASE_ID_PATTERN =
        Pattern.compile("/cases/case-details/(\\d{16})");
    private static final Pattern TRANSFERRED_ETHOS_REF_PATTERN =
        Pattern.compile(">([^<]+)</a>");

    private final AdminUserService adminUserService;
    private final AuthTokenGenerator authTokenGenerator;
    private final CoreCaseDataApi ccdApi;
    private final ManageCaseRoleService manageCaseRoleService;
    private final IdamClient idamClient;

    public CaseTransferInfoResponse getCaseTransferInfo(String authorization, String caseId, String caseUserRole) {
        verifyUserHasCaseRole(authorization, caseId, caseUserRole);

        CaseDetails caseDetails = ccdApi.getCase(
            adminUserService.getAdminUserToken(),
            authTokenGenerator.generate(),
            caseId
        );

        return buildTransferInfo(caseDetails);
    }

    private void verifyUserHasCaseRole(String authorization, String caseId, String caseUserRole) {
        try {
            UserInfo userInfo = idamClient.getUserInfo(authorization);
            CaseDetails caseReference = CaseDetails.builder().id(Long.valueOf(caseId)).build();
            CaseAssignedUserRolesResponse rolesResponse = manageCaseRoleService.getCaseUserRolesByCaseAndUserIdsCcd(
                authorization,
                List.of(caseReference)
            );

            if (rolesResponse == null || CollectionUtils.isEmpty(rolesResponse.getCaseAssignedUserRoles())) {
                throw new CaseUserRoleNotFoundException(String.format(EXCEPTION_CASE_USER_ROLE_NOT_FOUND, caseId));
            }

            boolean hasRole = rolesResponse.getCaseAssignedUserRoles().stream()
                .anyMatch(role -> userHasRequestedRole(userInfo.getUid(), caseUserRole, role));

            if (!hasRole) {
                throw new CaseUserRoleNotFoundException(String.format(EXCEPTION_CASE_USER_ROLE_NOT_FOUND, caseId));
            }
        } catch (IOException e) {
            throw new ManageCaseRoleException(e);
        }
    }

    private static boolean userHasRequestedRole(String userId, String caseUserRole, CaseAssignmentUserRole role) {
        return userId.equals(role.getUserId()) && caseUserRole.equals(role.getCaseRole());
    }

    private CaseTransferInfoResponse buildTransferInfo(CaseDetails caseDetails) {
        Map<String, Object> data = caseDetails.getData() == null
            ? Collections.emptyMap()
            : caseDetails.getData();
        String linkedCaseCT = stringValue(data.get("linkedCaseCT"));
        String transferredCaseLink = stringValue(data.get("transferredCaseLink"));
        String caseState = caseDetails.getState();

        if (!isTransferredCase(caseState, linkedCaseCT)) {
            throw new ResourceNotFoundException(
                String.format("Case %s has not been transferred", caseDetails.getId()),
                null
            );
        }

        ParsedTransferredCaseLink parsedLink = parseTransferredCaseLink(transferredCaseLink);

        return CaseTransferInfoResponse.builder()
            .transferred(true)
            .transferType(resolveTransferType(linkedCaseCT))
            .caseState(caseState)
            .originalCaseId(caseDetails.getId().toString())
            .originalEthosCaseReference(stringValue(data.get("ethosCaseReference")))
            .newEthosCaseReference(parsedLink.ethosCaseReference())
            .newCaseId(parsedLink.caseId())
            .destinationOffice(extractDestinationOffice(linkedCaseCT))
            .reasonForCT(stringValue(data.get("reasonForCT")))
            .transferComplete(StringUtils.isNotBlank(transferredCaseLink))
            .build();
    }

    private static String stringValue(Object value) {
        return value == null ? null : value.toString();
    }

    static boolean isTransferredCase(String caseState, String linkedCaseCT) {
        return TRANSFERRED_STATE.equals(caseState) || isTransferLinkedCaseCT(linkedCaseCT);
    }

    static boolean isTransferLinkedCaseCT(String linkedCaseCT) {
        return StringUtils.isNotBlank(linkedCaseCT)
            && linkedCaseCT.startsWith(TRANSFERRED_TO_PREFIX);
    }

    static CaseTransferType resolveTransferType(String linkedCaseCT) {
        if (TRANSFERRED_TO_ECM.equals(linkedCaseCT)) {
            return CaseTransferType.ECM;
        }
        return CaseTransferType.CROSS_COUNTRY;
    }

    static String extractDestinationOffice(String linkedCaseCT) {
        if (StringUtils.isBlank(linkedCaseCT) || !linkedCaseCT.startsWith(TRANSFERRED_TO_PREFIX)) {
            return null;
        }
        return linkedCaseCT.substring(TRANSFERRED_TO_PREFIX.length());
    }

    static ParsedTransferredCaseLink parseTransferredCaseLink(String transferredCaseLink) {
        if (StringUtils.isBlank(transferredCaseLink)) {
            return ParsedTransferredCaseLink.empty();
        }

        String caseId = null;
        Matcher caseIdMatcher = TRANSFERRED_CASE_ID_PATTERN.matcher(transferredCaseLink);
        if (caseIdMatcher.find()) {
            caseId = caseIdMatcher.group(1);
        }

        String ethosCaseReference = null;
        Matcher ethosRefMatcher = TRANSFERRED_ETHOS_REF_PATTERN.matcher(transferredCaseLink);
        if (ethosRefMatcher.find()) {
            ethosCaseReference = ethosRefMatcher.group(1);
        }

        return new ParsedTransferredCaseLink(caseId, ethosCaseReference);
    }

    record ParsedTransferredCaseLink(String caseId, String ethosCaseReference) {
        static ParsedTransferredCaseLink empty() {
            return new ParsedTransferredCaseLink(null, null);
        }
    }
}

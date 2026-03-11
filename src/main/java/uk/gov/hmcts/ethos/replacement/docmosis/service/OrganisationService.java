package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.AccountIdByEmailResponse;
import uk.gov.hmcts.ethos.replacement.docmosis.rdprofessional.OrganisationClient;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.OrganisationUtils;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_REPRESENTATIVE_ACCOUNT_NOT_FOUND_BY_EMAIL;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrganisationService {

    private final AdminUserService adminUserService;
    private final AuthTokenGenerator authTokenGenerator;
    private final OrganisationClient organisationClient;

    public String checkRepresentativeAccountByEmail(String representativeName, String email) {
        StringBuilder nocWarnings = new StringBuilder(StringUtils.EMPTY);
        try {
            ResponseEntity<AccountIdByEmailResponse> userResponse =
                    organisationClient.getAccountIdByEmail(adminUserService.getAdminUserToken(),
                            authTokenGenerator.generate(), email);
            // checking if representative email address exists in organisation users
            if (!OrganisationUtils.hasUserIdentifier(userResponse)) {
                String warningMessage = String.format(WARNING_REPRESENTATIVE_ACCOUNT_NOT_FOUND_BY_EMAIL,
                        representativeName, email);
                nocWarnings.append(warningMessage).append('\n');
            }
        } catch (Exception e) {
            String warningMessage = String.format(WARNING_REPRESENTATIVE_ACCOUNT_NOT_FOUND_BY_EMAIL,
                    representativeName, email);
            nocWarnings.append(warningMessage).append('\n');
        }
        return nocWarnings.toString();
    }
}

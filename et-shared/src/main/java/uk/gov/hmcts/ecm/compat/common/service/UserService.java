package uk.gov.hmcts.ecm.compat.common.service;

import uk.gov.hmcts.ecm.compat.common.idam.models.UserDetails;

public interface UserService {

    UserDetails getUserDetails(String authorisation);
    UserDetails getUserDetailsById(String var1, String var2);
}

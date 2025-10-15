package uk.gov.hmcts.ethos.replacement.docmosis.utils;

import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.CallbacksRuntimeException;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericServiceException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.HttpConstants.HTTP_HEADER_PARAM_NAME_AUTHORIZATION;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.HttpConstants.HTTP_HEADER_PARAM_NAME_SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.RoleConstants.ROLE_MODIFICATION_TYPE_ASSIGNMENT;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.RoleConstants.ROLE_MODIFICATION_TYPE_REVOKE;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.HttpUtils.buildCaseAccessUrl;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.HttpUtils.buildHeaders;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.HttpUtils.getHttpMethodByCaseUserRoleModificationType;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.UtilsTestConstants.DUMMY_AUTHORISATION_TOKEN;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.UtilsTestConstants.DUMMY_SERVICE_AUTHORISATION_TOKEN;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.UtilsTestConstants.TEST_CASE_ID_STRING;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.UtilsTestConstants.TEST_CCD_DATA_STORE_BASE_URL;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.UtilsTestConstants.TEST_EXCEPTION_INVALID_MODIFICATION_TYPE;

public class HttpUtilsTest {

    @Test
    @SneakyThrows
    void theBuildHeaders() {
        HttpHeaders httpHeaders = buildHeaders(DUMMY_AUTHORISATION_TOKEN, DUMMY_SERVICE_AUTHORISATION_TOKEN);
        assertThat(httpHeaders.get(HTTP_HEADER_PARAM_NAME_AUTHORIZATION)).contains(DUMMY_AUTHORISATION_TOKEN);
        assertThat(httpHeaders.get(HTTP_HEADER_PARAM_NAME_SERVICE_AUTHORIZATION))
                .contains(DUMMY_SERVICE_AUTHORISATION_TOKEN);
        assertThrows(
                GenericServiceException.class,
                () -> buildHeaders(StringUtils.EMPTY, DUMMY_SERVICE_AUTHORISATION_TOKEN));
    }

    @Test
    void theBuildCaseAccessUrl() {
        String expectedCaseAccessUrl = "http://localhost:8080/ccd/data-store/case-users?case_ids=123";
        assertThat(buildCaseAccessUrl(TEST_CCD_DATA_STORE_BASE_URL, TEST_CASE_ID_STRING))
                .isEqualTo(expectedCaseAccessUrl);
    }

    @Test
    @SneakyThrows
    void theGetHttpMethodByCaseUserRoleModificationType() {
        assertThat(getHttpMethodByCaseUserRoleModificationType(ROLE_MODIFICATION_TYPE_ASSIGNMENT))
                .isEqualTo(HttpMethod.POST);
        assertThat(getHttpMethodByCaseUserRoleModificationType(ROLE_MODIFICATION_TYPE_REVOKE))
                .isEqualTo(HttpMethod.DELETE);
        Exception exception =
                assertThrows(CallbacksRuntimeException.class, () -> getHttpMethodByCaseUserRoleModificationType(null));
        assertThat(exception.getMessage()).contains(TEST_EXCEPTION_INVALID_MODIFICATION_TYPE);
    }

}

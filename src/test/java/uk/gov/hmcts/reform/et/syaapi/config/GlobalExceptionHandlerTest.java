package uk.gov.hmcts.reform.et.syaapi.config;

import feign.FeignException;
import feign.Request;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.authorisation.exceptions.InvalidTokenException;
import uk.gov.hmcts.reform.et.syaapi.config.interceptors.UnAuthorisedServiceException;
import uk.gov.hmcts.reform.et.syaapi.exception.CaseUserRoleConflictException;
import uk.gov.hmcts.reform.et.syaapi.exception.CaseUserRoleNotFoundException;
import uk.gov.hmcts.reform.et.syaapi.exception.CaseUserRoleValidationException;
import uk.gov.hmcts.reform.et.syaapi.exception.ManageCaseRoleException;
import uk.gov.hmcts.reform.et.syaapi.models.ErrorResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Test
    void shouldHandleInvalidTokenException() {
        final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();
        final InvalidTokenException invalidTokenException = new InvalidTokenException("Unauthorized");
        final ErrorResponse errorResponse = ErrorResponse.builder().message("Unauthorized").code(401).build();

        final ResponseEntity<ErrorResponse> actualResponse =
            exceptionHandler.handleInvalidTokenException(invalidTokenException);

        assertThat(actualResponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(errorResponse).isEqualTo(actualResponse.getBody());
    }

    @Test
    void shouldHandleUnAuthorisedServiceException() {
        final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();
        final UnAuthorisedServiceException unAuthorisedServiceException = new UnAuthorisedServiceException("Forbidden");
        final ErrorResponse errorResponse = ErrorResponse.builder().message("Forbidden").code(403).build();

        final ResponseEntity<ErrorResponse> actualResponse =
            exceptionHandler.handleUnAuthorisedServiceException(unAuthorisedServiceException);

        assertThat(actualResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(errorResponse).isEqualTo(actualResponse.getBody());
    }

    @Test
    void shouldHandleFeignException() {
        final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();
        final FeignException feignException = new FeignException.InternalServerError(
            "Call failed",
            mock(Request.class),
            "service is down.".getBytes(),
            null
        );
        final ErrorResponse errorResponse = ErrorResponse.builder()
            .message("Call failed - service is down.")
            .code(500)
            .build();

        final ResponseEntity<ErrorResponse> actualResponse =
            exceptionHandler.handleFeignException(feignException);

        assertThat(actualResponse.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(errorResponse).isEqualTo(actualResponse.getBody());
    }

    @Test
    void shouldHandleCaseUserRoleConflictException() {
        final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();
        final CaseUserRoleConflictException exception = new CaseUserRoleConflictException("Conflict");

        final ResponseEntity<ErrorResponse> actualResponse =
            exceptionHandler.handleCaseUserRoleConflictException(exception);

        assertThat(actualResponse.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(actualResponse.getBody()).isEqualTo(ErrorResponse.builder().message("Conflict").code(409).build());
    }

    @Test
    void shouldHandleCaseUserRoleNotFoundException() {
        final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();
        final CaseUserRoleNotFoundException exception = new CaseUserRoleNotFoundException("Not Found");

        final ResponseEntity<ErrorResponse> actualResponse =
            exceptionHandler.handleCaseUserRoleNotFoundException(exception);

        assertThat(actualResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(actualResponse.getBody()).isEqualTo(ErrorResponse.builder().message("Not Found").code(404).build());
    }

    @Test
    void shouldHandleCaseUserRoleValidationException() {
        final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();
        final CaseUserRoleValidationException exception = new CaseUserRoleValidationException("Bad Request");

        final ResponseEntity<ErrorResponse> actualResponse =
            exceptionHandler.handleCaseUserRoleValidationException(exception);

        assertThat(actualResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(actualResponse.getBody()).isEqualTo(ErrorResponse.builder().message("Bad Request").code(400)
                                                           .build());
    }

    @Test
    void shouldHandleManageCaseRoleException() {
        final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();
        final ManageCaseRoleException exception = new ManageCaseRoleException(new Exception("Internal Error"));

        final ResponseEntity<ErrorResponse> actualResponse =
            exceptionHandler.handleManageCaseRoleException(exception);

        assertThat(actualResponse.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(actualResponse.getBody()).isEqualTo(ErrorResponse.builder().message(exception.getMessage())
                                                           .code(500).build());
    }
}

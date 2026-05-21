package uk.gov.hmcts.reform.et.syaapi.config;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import uk.gov.hmcts.reform.et.syaapi.service.RoleValidationService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@ExtendWith(MockitoExtension.class)
class AcasRoleAuthorizationAspectTest {

    private static final String AUTH_TOKEN = "Bearer some-token";
    private static final String REQUEST_URI = "/getLastModifiedCaseList";

    @Mock
    private RoleValidationService roleValidationService;
    @Mock
    private ProceedingJoinPoint joinPoint;
    @Mock
    private HttpServletRequest httpServletRequest;
    @Mock
    private ServletRequestAttributes servletRequestAttributes;
    @Captor
    private ArgumentCaptor<List<String>> rolesCaptor;

    @InjectMocks
    private AcasRoleAuthorizationAspect acasRoleAuthorizationAspect;

    @Test
    void checkAcasRoleWhenNoHttpRequestThenReturnInternalServerError() throws Throwable {
        try (MockedStatic<RequestContextHolder> mockedStatic = mockStatic(RequestContextHolder.class)) {
            mockedStatic.when(RequestContextHolder::getRequestAttributes).thenReturn(null);

            Object result = acasRoleAuthorizationAspect.checkAcasRole(joinPoint);

            assertThat(result).isInstanceOf(ResponseEntity.class);
            ResponseEntity<?> response = (ResponseEntity<?>) result;
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(response.getBody()).isEqualTo("Internal server error");
        }
    }

    @Test
    void checkAcasRoleWhenNullAuthTokenThenReturnUnauthorized() throws Throwable {
        try (MockedStatic<RequestContextHolder> mockedStatic = mockStatic(RequestContextHolder.class)) {
            mockedStatic.when(RequestContextHolder::getRequestAttributes).thenReturn(servletRequestAttributes);
            when(servletRequestAttributes.getRequest()).thenReturn(httpServletRequest);
            when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(null);

            Object result = acasRoleAuthorizationAspect.checkAcasRole(joinPoint);

            assertThat(result).isInstanceOf(ResponseEntity.class);
            ResponseEntity<?> response = (ResponseEntity<?>) result;
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(response.getBody()).isEqualTo("No authorization token provided");
        }
    }

    @Test
    void checkAcasRoleWhenEmptyAuthTokenThenReturnUnauthorized() throws Throwable {
        try (MockedStatic<RequestContextHolder> mockedStatic = mockStatic(RequestContextHolder.class)) {
            mockedStatic.when(RequestContextHolder::getRequestAttributes).thenReturn(servletRequestAttributes);
            when(servletRequestAttributes.getRequest()).thenReturn(httpServletRequest);
            when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn("");

            Object result = acasRoleAuthorizationAspect.checkAcasRole(joinPoint);

            assertThat(result).isInstanceOf(ResponseEntity.class);
            ResponseEntity<?> response = (ResponseEntity<?>) result;
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(response.getBody()).isEqualTo("No authorization token provided");
        }
    }

    @Test
    void checkAcasRoleWhenUserDoesNotHaveRequiredRoleThenReturnForbidden() throws Throwable {
        try (MockedStatic<RequestContextHolder> mockedStatic = mockStatic(RequestContextHolder.class)) {
            mockedStatic.when(RequestContextHolder::getRequestAttributes).thenReturn(servletRequestAttributes);
            when(servletRequestAttributes.getRequest()).thenReturn(httpServletRequest);
            when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(AUTH_TOKEN);
            when(httpServletRequest.getRequestURI()).thenReturn(REQUEST_URI);
            when(roleValidationService.hasAnyRole(anyString(), rolesCaptor.capture())).thenReturn(false);

            Object result = acasRoleAuthorizationAspect.checkAcasRole(joinPoint);

            assertThat(result).isInstanceOf(ResponseEntity.class);
            ResponseEntity<?> response = (ResponseEntity<?>) result;
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            assertThat(response.getBody()).isEqualTo("User does not have required role to access this endpoint");
        }
    }

    @Test
    void checkAcasRoleWhenUserHasRequiredRoleThenProceedWithExecution() throws Throwable {
        String expectedResponse = "expected-response";
        try (MockedStatic<RequestContextHolder> mockedStatic = mockStatic(RequestContextHolder.class)) {
            mockedStatic.when(RequestContextHolder::getRequestAttributes).thenReturn(servletRequestAttributes);
            when(servletRequestAttributes.getRequest()).thenReturn(httpServletRequest);
            when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(AUTH_TOKEN);
            when(roleValidationService.hasAnyRole(anyString(), rolesCaptor.capture())).thenReturn(true);
            when(joinPoint.proceed()).thenReturn(expectedResponse);

            Object result = acasRoleAuthorizationAspect.checkAcasRole(joinPoint);

            assertThat(result).isEqualTo(expectedResponse);
            verify(joinPoint).proceed();
        }
    }

    @Test
    void checkAcasRoleVerifiesCorrectRolesAreChecked() throws Throwable {
        try (MockedStatic<RequestContextHolder> mockedStatic = mockStatic(RequestContextHolder.class)) {
            mockedStatic.when(RequestContextHolder::getRequestAttributes).thenReturn(servletRequestAttributes);
            when(servletRequestAttributes.getRequest()).thenReturn(httpServletRequest);
            when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(AUTH_TOKEN);
            when(roleValidationService.hasAnyRole(anyString(), rolesCaptor.capture())).thenReturn(true);
            when(joinPoint.proceed()).thenReturn(null);

            acasRoleAuthorizationAspect.checkAcasRole(joinPoint);

            assertThat(rolesCaptor.getValue())
                .containsExactlyInAnyOrder("caseworker-employment-api", "et-acas-api");
        }
    }

    @Test
    void checkAcasRolePassesAuthTokenToRoleValidationService() throws Throwable {
        ArgumentCaptor<String> tokenCaptor = ArgumentCaptor.forClass(String.class);
        try (MockedStatic<RequestContextHolder> mockedStatic = mockStatic(RequestContextHolder.class)) {
            mockedStatic.when(RequestContextHolder::getRequestAttributes).thenReturn(servletRequestAttributes);
            when(servletRequestAttributes.getRequest()).thenReturn(httpServletRequest);
            when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(AUTH_TOKEN);
            when(roleValidationService.hasAnyRole(tokenCaptor.capture(), rolesCaptor.capture())).thenReturn(true);
            when(joinPoint.proceed()).thenReturn(null);

            acasRoleAuthorizationAspect.checkAcasRole(joinPoint);

            assertThat(tokenCaptor.getValue()).isEqualTo(AUTH_TOKEN);
        }
    }
}

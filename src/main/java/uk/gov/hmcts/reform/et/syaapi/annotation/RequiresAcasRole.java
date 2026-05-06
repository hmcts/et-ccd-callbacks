package uk.gov.hmcts.reform.et.syaapi.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Documents that a controller method requires one of the ACAS-specific IDAM roles
 * ({@code caseworker-employment-api} or {@code et-acas-api}).
 *
 * <p>The actual enforcement is applied directly on each annotated method via
 * {@code @PreAuthorize("@roleValidationService.hasAnyRole(#authToken, ...)")}.
 * This annotation serves as a readable semantic label.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresAcasRole {
}

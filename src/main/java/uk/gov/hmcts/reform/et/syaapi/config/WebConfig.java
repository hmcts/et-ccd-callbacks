package uk.gov.hmcts.reform.et.syaapi.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import uk.gov.hmcts.reform.et.syaapi.config.interceptors.RequestInterceptor;

/**
 * Configures the API to add intercepters for calls from external sources.
 */
@Component("syaWebConfig")
public class WebConfig implements WebMvcConfigurer {
    private final RequestInterceptor requestInterceptor;

    @Autowired
    public WebConfig(RequestInterceptor requestInterceptor) {
        this.requestInterceptor = requestInterceptor;
    }

    /**
     * This config excludes certain urls to the inteceptor.
     * these are endpoints that do not require authentication
     * @param registry {@link InterceptorRegistry} in which the exclusions for the inteceptor are added
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(requestInterceptor)
            .addPathPatterns(
                "/bundles/**",
                "/cases/**",
                "/document/**",
                "/documents/**",
                "/et3/**",
                "/generate-pdf",
                "/getAcasDocuments",
                "/getCaseData",
                "/getLastModifiedCaseList",
                "/downloadAcasDocuments",
                "/manageCaseRole/**",
                "/respondentTSE/submit-respondent-application",
                "/respondentTSE/store-respondent-application",
                "/respondentTSE/respond-to-claimant-application",
                "/respondentTSE/change-respondent-application-status",
                "/sendNotification/update-notification-state",
                "/sendNotification/add-response-send-notification",
                "/sendNotification/change-respondent-notification-status",
                "/sendNotification/add-respondent-respond-to-notification",
                "/sendNotification/store-respondent-respond-to-notification",
                "/sendNotification/submit-respondent-respond-to-notification",
                "/store/**",
                "/tseAdmin/update-admin-decision-state",
                "/vetAndAcceptCase"
            )
            .excludePathPatterns(
                "/health",
                "/webjars/springfox-swagger-ui/**",
                "/swagger-ui/**",
                "/swagger-resources/**",
                "/v2/**",
                "/favicon.ico",
                "/health",
                "/mappings",
                "/info",
                "/metrics",
                "/metrics/**",
                "/v3/**",
                "/"
            );
    }
}

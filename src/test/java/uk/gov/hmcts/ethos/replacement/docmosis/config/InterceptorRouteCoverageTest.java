package uk.gov.hmcts.ethos.replacement.docmosis.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.handler.MappedInterceptor;
import org.springframework.web.util.ServletRequestPathUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Controllers rely on the two request interceptors to verify the bearer token rather than checking it themselves.
 * This fails if a controller route is not covered by either interceptor's path patterns.
 */
class InterceptorRouteCoverageTest {

    private static final List<String> CONTROLLER_PACKAGES = List.of(
        "uk.gov.hmcts.ethos.replacement.docmosis",
        "uk.gov.hmcts.reform.et.syaapi"
    );

    /**
     * Routes deliberately left without a token check.
     */
    private static final Set<String> PUBLIC_ROUTES = Set.of("/");

    @Test
    void everyControllerRouteIsCoveredByARequestInterceptor() throws Exception {
        List<Object> interceptors = new ArrayList<>();
        interceptors.addAll(registeredInterceptors(new WebConfig(
            new uk.gov.hmcts.ethos.replacement.docmosis.config.interceptors.RequestInterceptor(
                mock(VerifyTokenService.class), null))));
        interceptors.addAll(registeredInterceptors(new uk.gov.hmcts.reform.et.syaapi.config.WebConfig(
            new uk.gov.hmcts.reform.et.syaapi.config.interceptors.RequestInterceptor(
                mock(VerifyTokenService.class)))));

        Set<String> uncovered = new TreeSet<>();
        Set<String> routes = controllerRoutes();
        for (String route : routes) {
            if (!PUBLIC_ROUTES.contains(route) && interceptors.stream().noneMatch(i -> matches(i, route))) {
                uncovered.add(route);
            }
        }

        assertThat(routes).isNotEmpty();
        assertThat(uncovered).as("routes not covered by any RequestInterceptor").isEmpty();
    }

    private static List<Object> registeredInterceptors(
        org.springframework.web.servlet.config.annotation.WebMvcConfigurer config) {
        ExposedInterceptorRegistry registry = new ExposedInterceptorRegistry();
        config.addInterceptors(registry);
        return registry.interceptors();
    }

    private static boolean matches(Object interceptor, String route) {
        if (!(interceptor instanceof MappedInterceptor mappedInterceptor)) {
            return true;
        }
        MockHttpServletRequest request = new MockHttpServletRequest("POST", route);
        ServletRequestPathUtils.parseAndCache(request);
        return mappedInterceptor.matches(request);
    }

    private static Set<String> controllerRoutes() throws ClassNotFoundException {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(Controller.class));
        Set<String> routes = new TreeSet<>();
        for (String basePackage : CONTROLLER_PACKAGES) {
            for (BeanDefinition candidate : scanner.findCandidateComponents(basePackage)) {
                Class<?> type = ClassUtils.forName(candidate.getBeanClassName(), null);
                RequestMapping typeMapping = AnnotatedElementUtils.findMergedAnnotation(type, RequestMapping.class);
                for (Method method : type.getMethods()) {
                    RequestMapping methodMapping =
                        AnnotatedElementUtils.findMergedAnnotation(method, RequestMapping.class);
                    if (methodMapping != null) {
                        for (String prefix : paths(typeMapping)) {
                            for (String path : paths(methodMapping)) {
                                routes.add(normalise(prefix + path));
                            }
                        }
                    }
                }
            }
        }
        return routes;
    }

    private static List<String> paths(RequestMapping mapping) {
        if (mapping == null || mapping.path().length == 0) {
            return List.of("");
        }
        return List.of(mapping.path());
    }

    private static String normalise(String path) {
        String withSlash = path.startsWith("/") ? path : "/" + path;
        return withSlash.replaceAll("\\{[^}]+}", "x");
    }

    private static final class ExposedInterceptorRegistry extends InterceptorRegistry {
        List<Object> interceptors() {
            return getInterceptors();
        }
    }
}

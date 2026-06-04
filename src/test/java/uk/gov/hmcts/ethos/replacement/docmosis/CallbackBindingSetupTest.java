package uk.gov.hmcts.ethos.replacement.docmosis;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("PMD.AvoidAccessibilityAlteration")
class CallbackBindingSetupTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    private static final Path DEFINITION_SNAPSHOTS_ROOT = Path.of("build", "cftlib", "definition-snapshots");
    private static final String CONTROLLERS_PACKAGE = "uk.gov.hmcts.ethos.replacement.docmosis.controllers";
    private static final String PRE_HEARING_DEPOSIT = "Pre_Hearing_Deposit";
    private static final String LOCAL_CALLBACK_BASE_URL = "http://localhost:8081";
    private static final List<String> CALLBACK_URL_FIELDS = List.of(
        "callback_url_about_to_start_event",
        "callback_url_about_to_submit_event",
        "callback_url_submitted_event"
    );

    @Test
    void callbackBindingsFromCcdDefinitionsResolveAgainstEtControllers()
        throws Exception {
        List<String> callbackUrls = loadCallbackUrls();
        List<Class<?>> controllerClasses = discoverControllerClasses();

        assertThat(callbackUrls).isNotEmpty();
        assertThat(controllerClasses).isNotEmpty();

        try (GenericApplicationContext applicationContext = applicationContext(controllerClasses)) {
            Object callbackRouteRegistry = createCallbackRouteRegistry(applicationContext);
            Method validate = callbackRouteRegistry.getClass().getDeclaredMethod("validate", String.class);
            validate.setAccessible(true);

            for (String callbackUrl : callbackUrls) {
                validateCallback(validate, callbackRouteRegistry, callbackUrl);
            }
        }
    }

    private static List<String> loadCallbackUrls()
        throws IOException {
        Set<String> callbackUrls = new LinkedHashSet<>();

        try (Stream<Path> paths = Files.walk(DEFINITION_SNAPSHOTS_ROOT)) {
            for (Path path : paths
                .filter(Files::isRegularFile)
                .filter(snapshot -> snapshot.getFileName().toString().endsWith(".json"))
                .sorted(Comparator.naturalOrder())
                .toList()) {
                String caseTypeId = path.getFileName().toString().replaceFirst("\\.json$", "");
                if (PRE_HEARING_DEPOSIT.equals(caseTypeId)) {
                    continue;
                }
                JsonNode caseTypeDefinition = OBJECT_MAPPER.readTree(path.toFile());
                for (JsonNode event : caseTypeDefinition.path("events")) {
                    for (String field : CALLBACK_URL_FIELDS) {
                        String callbackUrl = event.path(field).asText();
                        if (!callbackUrl.isBlank() && !"null".equals(callbackUrl)) {
                            callbackUrls.add(callbackUrl);
                        }
                    }
                }
            }
        }

        return List.copyOf(callbackUrls);
    }

    private static List<Class<?>> discoverControllerClasses() throws ClassNotFoundException {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(RestController.class));
        scanner.addIncludeFilter(new AnnotationTypeFilter(Controller.class));

        List<Class<?>> controllerClasses = new ArrayList<>();
        for (var beanDefinition : scanner.findCandidateComponents(CONTROLLERS_PACKAGE)) {
            controllerClasses.add(Class.forName(beanDefinition.getBeanClassName()));
        }

        controllerClasses.sort(Comparator.comparing(Class::getName));
        return controllerClasses;
    }

    private static GenericApplicationContext applicationContext(List<Class<?>> controllerClasses) {
        GenericApplicationContext applicationContext = new GenericApplicationContext();
        for (Class<?> controllerClass : controllerClasses) {
            RootBeanDefinition beanDefinition = new RootBeanDefinition(controllerClass);
            beanDefinition.setInstanceSupplier(() -> Mockito.mock(controllerClass));
            applicationContext.registerBeanDefinition(controllerClass.getName(), beanDefinition);
        }
        applicationContext.refresh();
        return applicationContext;
    }

    private static Object createCallbackRouteRegistry(GenericApplicationContext applicationContext)
        throws ReflectiveOperationException {
        RequestMappingHandlerMapping handlerMapping = new RequestMappingHandlerMapping();
        handlerMapping.setApplicationContext(applicationContext);
        handlerMapping.afterPropertiesSet();

        Class<?> callbackRouteRegistryClass = Class.forName("uk.gov.hmcts.ccd.sdk.impl.json.JsonCallbackRouteRegistry");
        Constructor<?> constructor = callbackRouteRegistryClass.getDeclaredConstructor(
            ApplicationContext.class,
            ObjectMapper.class,
            RequestMappingHandlerMapping.class,
            Environment.class
        );
        constructor.setAccessible(true);
        return constructor.newInstance(
            applicationContext,
            OBJECT_MAPPER,
            handlerMapping,
            new MockEnvironment().withProperty("decentralisation.local-callback-base-url", LOCAL_CALLBACK_BASE_URL)
        );
    }

    private static void validateCallback(Method validate, Object callbackRouteRegistry, String callbackUrl)
        throws ReflectiveOperationException {
        try {
            validate.invoke(callbackRouteRegistry, callbackUrl);
        } catch (InvocationTargetException e) {
            throw new AssertionError("Unresolved local callback " + callbackUrl, e.getTargetException());
        }
    }
}

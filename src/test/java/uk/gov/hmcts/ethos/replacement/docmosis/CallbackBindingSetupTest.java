package uk.gov.hmcts.ethos.replacement.docmosis;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.Mockito;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Controller;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class CallbackBindingSetupTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    private static final Path DEFINITION_SNAPSHOTS_ROOT = Path.of("build", "cftlib", "definition-snapshots");
    private static final String CONTROLLERS_PACKAGE = "uk.gov.hmcts.ethos.replacement.docmosis.controllers";
    private static final String PRE_HEARING_DEPOSIT = "Pre_Hearing_Deposit";
    private static final String LOCAL_CALLBACK_BASE_URL = "http://localhost:8081";

    @Test
    void callbackBindingsFromCcdDefinitionsResolveAgainstEtControllers()
        throws IOException, ReflectiveOperationException {
        Map<String, Object> definitions = loadCaseTypeDefinitions();
        List<Class<?>> controllerClasses = discoverControllerClasses();

        assertThat(definitions).isNotEmpty();
        assertThat(controllerClasses).isNotEmpty();

        Object callbackDispatchService = createCallbackDispatchService(definitions, controllerClasses);
        ReflectionTestUtils.invokeMethod(callbackDispatchService, "initialiseHandlerMaps");
    }

    private static Map<String, Object> loadCaseTypeDefinitions()
        throws IOException, ReflectiveOperationException {
        Map<String, Object> definitions = new LinkedHashMap<>();
        Class<?> caseTypeDefinitionClass = Class.forName("uk.gov.hmcts.ccd.domain.model.definition.CaseTypeDefinition");

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
                Object caseTypeDefinition = OBJECT_MAPPER.readValue(path.toFile(), caseTypeDefinitionClass);
                definitions.put(caseTypeId, caseTypeDefinition);
            }
        }

        return definitions;
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

    @SuppressWarnings("unchecked")
    private static Object createCallbackDispatchService(
        Map<String, Object> definitions,
        List<Class<?>> controllerClasses
    ) throws ReflectiveOperationException {
        ListableBeanFactory beanFactory = Mockito.mock(ListableBeanFactory.class);
        Map<String, Object> restControllers = new LinkedHashMap<>();
        Map<String, Object> controllers = new LinkedHashMap<>();
        for (Class<?> controllerClass : controllerClasses) {
            Object controllerInstance = Mockito.mock(controllerClass);
            String beanName = controllerClass.getSimpleName();
            if (AnnotatedElementUtils.hasAnnotation(controllerClass, RestController.class)) {
                restControllers.put(beanName, controllerInstance);
            }
            if (AnnotatedElementUtils.hasAnnotation(controllerClass, Controller.class)) {
                controllers.put(beanName, controllerInstance);
            }
        }

        Mockito.when(beanFactory.getBeansWithAnnotation(RestController.class)).thenReturn(restControllers);
        Mockito.when(beanFactory.getBeansWithAnnotation(Controller.class)).thenReturn(controllers);

        Class<?> callbackDispatchServiceClass = Class.forName("uk.gov.hmcts.ccd.sdk.impl.CallbackDispatchService");
        Class<?> definitionRegistryClass = Class.forName("uk.gov.hmcts.ccd.sdk.impl.DefinitionRegistry");
        Constructor<?> constructor = callbackDispatchServiceClass.getConstructor(
            definitionRegistryClass,
            ListableBeanFactory.class,
            ObjectMapper.class
        );
        Object definitionRegistry = Mockito.mock((Class<Object>) definitionRegistryClass, invocation -> {
            if ("loadDefinitions".equals(invocation.getMethod().getName())) {
                return definitions;
            }
            return Answers.RETURNS_DEFAULTS.answer(invocation);
        });
        Object service = constructor.newInstance(definitionRegistry, beanFactory, OBJECT_MAPPER);
        ReflectionTestUtils.setField(service, "localCallbackBaseUrls", LOCAL_CALLBACK_BASE_URL);
        return service;
    }
}

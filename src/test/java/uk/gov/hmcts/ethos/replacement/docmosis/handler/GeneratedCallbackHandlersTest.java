package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.CallbackHandler;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockingDetails;

class GeneratedCallbackHandlersTest {

    private static final String HANDLER_PACKAGE = "uk.gov.hmcts.ethos.replacement.docmosis.handler.";
    private static final String MATRIX_RESOURCE = "generated-callback-handlers-phase2.tsv";

    @Test
    void shouldMatchGeneratedHandlerMatrixAndDelegateCallbacksToMappedControllers() throws Exception {
        List<HandlerRow> rows = readRows();
        assertThat(rows).hasSize(113);

        CaseDetails ccdCaseDetails =
            CaseDetails.builder()
                .id(1L)
                .caseTypeId("ET_EnglandWales")
                .state("Open")
                .data(new HashMap<>())
                .build();

        for (HandlerRow row : rows) {
            Class<?> handlerClass = Class.forName(HANDLER_PACKAGE + row.className());
            Constructor<?> constructor = handlerClass.getConstructors()[0];

            Map<Class<?>, Object> dependencies = new HashMap<>();
            Object[] args = new Object[constructor.getParameterCount()];
            Class<?>[] parameterTypes = constructor.getParameterTypes();

            for (int i = 0; i < parameterTypes.length; i++) {
                Object dependency = instantiateDependency(parameterTypes[i]);
                args[i] = dependency;
                dependencies.put(parameterTypes[i], dependency);
            }

            CallbackHandler<?> handler = (CallbackHandler<?>) constructor.newInstance(args);

            assertThat(handler.getHandledCaseTypeIds()).containsExactlyElementsOf(splitCsv(row.caseTypeIdsCsv()));
            assertThat(handler.getHandledEventIds()).containsExactlyElementsOf(splitCsv(row.eventIdsCsv()));
            assertThat(handler.acceptsAboutToSubmit()).isEqualTo(!row.aboutPath().isEmpty());
            assertThat(handler.acceptsSubmitted()).isEqualTo(!row.submittedPath().isEmpty());

            CallbackRequest request = CallbackRequest.builder().caseDetails(ccdCaseDetails).build();

            if (row.aboutPath().isEmpty()) {
                assertThatThrownBy(() -> handler.aboutToSubmit(request))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("does not support about-to-submit callbacks");
            } else {
                handler.aboutToSubmit(request);
                assertControllerMethodInvoked(dependencies, row.aboutController(), row.aboutMethod());
            }

            if (row.submittedPath().isEmpty()) {
                assertThatThrownBy(() -> handler.submitted(request))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("does not support submitted callbacks");
            } else {
                handler.submitted(request);
                assertControllerMethodInvoked(dependencies, row.submittedController(), row.submittedMethod());
            }
        }
    }

    private void assertControllerMethodInvoked(Map<Class<?>, Object> dependencies, String controllerClassName,
                                               String methodName) throws ClassNotFoundException {
        Class<?> controllerClass = Class.forName(controllerClassName);
        Object controllerMock = dependencies.get(controllerClass);

        assertThat(controllerMock)
            .as("mock for %s", controllerClassName)
            .isNotNull();

        boolean invoked = mockingDetails(controllerMock).getInvocations().stream()
            .anyMatch(invocation -> invocation.getMethod().getName().equals(methodName));

        assertThat(invoked)
            .as("expected method %s to be invoked on %s", methodName, controllerClassName)
            .isTrue();
    }

    private List<HandlerRow> readRows() throws IOException {
        List<HandlerRow> rows = new ArrayList<>();
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream(MATRIX_RESOURCE)) {
            assertThat(stream).as("resource %s", MATRIX_RESOURCE).isNotNull();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                String line = reader.readLine();
                while (line != null) {
                    if (line.isBlank()) {
                        line = reader.readLine();
                        continue;
                    }

                    String[] fields = line.split("\\t", -1);
                    rows.add(new HandlerRow(
                        fields[1],
                        fields[2],
                        fields[3],
                        fields[4],
                        fields[5],
                        fields[6],
                        fields[7],
                        fields[11],
                        fields[12]
                    ));
                    line = reader.readLine();
                }
            }
        }
        return rows;
    }

    private List<String> splitCsv(String csv) {
        return Arrays.asList(csv.split(","));
    }

    private Object instantiateDependency(Class<?> dependencyType) {
        if (dependencyType.equals(CaseDetailsConverter.class)) {
            return new CaseDetailsConverter(new ObjectMapper());
        }
        if (dependencyType.equals(ObjectMapper.class)) {
            return new ObjectMapper();
        }
        if (dependencyType.equals(String.class)) {
            return "test-value";
        }
        return mock(dependencyType);
    }

    private record HandlerRow(
        String className,
        String aboutPath,
        String submittedPath,
        String caseTypeIdsCsv,
        String eventIdsCsv,
        String aboutController,
        String aboutMethod,
        String submittedController,
        String submittedMethod
    ) {
    }
}

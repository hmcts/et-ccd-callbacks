package uk.gov.hmcts.ethos.replacement.docmosis.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import uk.gov.hmcts.et.common.model.ccd.CaseData;

import java.util.LinkedHashMap;
import java.util.Map;

public final class MapperUtils {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private MapperUtils() {
        // restrict instantiation
    }

    private static CaseData getCaseData(Map<String, Object> caseData) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper.convertValue(caseData, CaseData.class);
    }

    /**
     * Converts caseData from Map to {@link CaseData} object.
     * @param caseData to be converted
     * @return case data wrapped in {@link CaseData} format
     */
    public static CaseData convertCaseDataMapToCaseDataObject(Map<String, Object> caseData) {
        return getCaseData(caseData);
    }

    /**
     * Converts any object to target class.
     * @param clazz Target class
     * @param object source object
     * @param <T> Type of return object
     * @return object of given class type
     * @throws JsonProcessingException exception may occur while converting object to json string
     */
    public static <T> T mapJavaObjectToClass(Class<? extends T> clazz, Object object)
            throws JsonProcessingException {
        return OBJECT_MAPPER.readValue(OBJECT_MAPPER.writeValueAsString(object),
                OBJECT_MAPPER.getTypeFactory().constructType(clazz));
    }

    /**
     * Converts a {@link CaseData} object into a {@link LinkedHashMap} representation.
     * <p>
     * This method uses Jackson’s {@link ObjectMapper} to serialize the {@code caseData}
     * object into a map structure while preserving insertion order. The {@link JavaTimeModule}
     * is registered with the mapper to ensure proper handling of Java 8 date and time types.
     * <p>
     * This mapping is useful when dynamic or generic manipulation of case data is required,
     * such as for logging, JSON serialization, or sending data to external systems.
     *
     * @param caseData the {@link CaseData} object to be converted; must not be null
     * @return a {@link LinkedHashMap} containing the serialized key-value pairs of the {@code caseData}
     *
     * @see ObjectMapper
     * @see JavaTimeModule
     */
    public static Map<String, Object> mapCaseDataToLinkedHashMap(CaseData caseData) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper.convertValue(caseData, new TypeReference<>() {});
    }

}

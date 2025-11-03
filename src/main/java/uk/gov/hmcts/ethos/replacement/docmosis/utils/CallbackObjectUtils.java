package uk.gov.hmcts.ethos.replacement.docmosis.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class CallbackObjectUtils {

    private CallbackObjectUtils() {
        // Utility classes should not have a public or default constructor.
    }

    /**
     * Creates a deep clone of the given object using JSON serialization and deserialization.
     * <p>
     * This method serializes the input object to a JSON string and then deserializes it back
     * into a new instance of the specified class. It is useful for deep cloning objects that
     * are fully compatible with Jackson's serialization mechanisms.
     *
     * @param object the object to clone; may be {@code null}
     * @param clazz  the target class type to deserialize into; must match the type of {@code object}
     * @param <T>    the type of the object being cloned
     * @return a deep copy of the original object, or {@code null} if the input object or class is {@code null}
     * @throws JsonProcessingException if serialization or deserialization fails
     */
    public static <T> T cloneObject(T object, Class<T> clazz) throws JsonProcessingException {
        if (object == null || clazz == null) {
            return null;
        }
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper
                .readValue(objectMapper.writeValueAsString(object), clazz);
    }
}

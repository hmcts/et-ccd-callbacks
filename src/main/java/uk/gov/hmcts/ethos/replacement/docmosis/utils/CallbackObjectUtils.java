package uk.gov.hmcts.ethos.replacement.docmosis.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collection;

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

    /**
     * Determines whether any of the provided objects is considered empty.
     *
     * <p>An object is treated as empty according to the {@link #isEmpty(Object)} method.
     * This includes special handling for {@link String} (blank values),
     * {@link java.util.Collection} (empty collections), and other object types.</p>
     *
     * <p>If the provided array is {@code null} or contains no elements,
     * this method returns {@code false}.</p>
     *
     * @param <T>     the type of the objects to evaluate
     * @param objects the objects to check for emptiness (maybe {@code null})
     * @return {@code true} if at least one object is considered empty;
     *         {@code false} if all objects are non-empty or if the array is {@code null} or empty
     */
    @SafeVarargs
    public static <T> boolean isAnyEmpty(T... objects) {
        if (objects == null || CollectionUtils.isEmpty(Arrays.asList(objects))) {
            return false;
        }
        for (T object : objects) {
            if (isEmpty(object)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines whether either of the provided objects is considered empty.
     *
     * <p>Emptiness is evaluated using {@link #isEmpty(Object)}, which provides
     * special handling for {@link String} (blank values), {@link java.util.Collection}
     * (empty collections), and other object types.</p>
     *
     * @param <T>     the type of the first object
     * @param <Z>     the type of the second object
     * @param object1 the first object to evaluate (maybe {@code null})
     * @param object2 the second object to evaluate (maybe {@code null})
     * @return {@code true} if either object is considered empty;
     *         {@code false} if both objects are non-empty
     */
    public static <T, Z> boolean isAnyEmpty(T object1, Z object2) {
        return isEmpty(object1) || isEmpty(object2);
    }

    /**
     * Determines whether the given object is considered empty.
     *
     * <p>Emptiness is evaluated as follows:
     * <ul>
     *     <li>If the object is a {@link String}, it is considered empty if it is
     *         {@code null}, empty, or contains only whitespace
     *         (evaluated using {@link StringUtils#isBlank(CharSequence)}).</li>
     *     <li>If the object is a {@link Collection}, it is considered empty if it is
     *         {@code null} or contains no elements
     *         (evaluated using {@link CollectionUtils#isEmpty(Collection)}).</li>
     *     <li>For all other object types, emptiness is determined using
     *         {@link ObjectUtils#isEmpty(Object)}.</li>
     * </ul>
     *
     * @param <T>    the type of the object to evaluate
     * @param object the object to check (maybe {@code null})
     * @return {@code true} if the object is considered empty; {@code false} otherwise
     */
    public static <T> boolean isEmpty(T object) {
        boolean isEmpty;
        if (object instanceof String str) {
            isEmpty = StringUtils.isBlank(str);
        } else if (object instanceof Collection<?> collection) {
            isEmpty = CollectionUtils.isEmpty(collection);
        } else {
            isEmpty = ObjectUtils.isEmpty(object);
        }
        return isEmpty;
    }
}

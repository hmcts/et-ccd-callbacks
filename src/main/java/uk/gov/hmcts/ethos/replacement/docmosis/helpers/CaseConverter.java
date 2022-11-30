package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import static java.util.Objects.isNull;

@Service
@RequiredArgsConstructor
public class CaseConverter {

    public static TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

    private final ObjectMapper objectMapper;

    public <T> T convert(Object o, Class<T> clazz) {
        if (isNull(o)) {
            return null;
        }
        return objectMapper.convertValue(o, clazz);
    }

    public <T> Map<String, Object> toMap(T object) {
        if (isNull(object)) {
            return null;
        }
        return objectMapper.convertValue(object, MAP_TYPE);
    }
}


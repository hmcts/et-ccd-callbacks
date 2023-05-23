package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import static java.util.Objects.isNull;

@Service
@RequiredArgsConstructor
public class CaseConverter {

    private static TypeReference<Map<String, Object>> typeReference = new TypeReference<>() {};

    private final ObjectMapper objectMapper;

    public <T> T convert(Object object, Class<T> clazz) {
        if (isNull(object)) {
            return null;
        }
        return objectMapper.convertValue(object, clazz);
    }

    public <T> Map<String, Object> toMap(T object) {
        if (isNull(object)) {
            return Collections.emptyMap();
        }
        return objectMapper.convertValue(object, typeReference);
    }
}


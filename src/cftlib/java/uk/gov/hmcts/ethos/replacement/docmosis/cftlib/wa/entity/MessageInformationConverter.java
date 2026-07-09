package uk.gov.hmcts.ethos.replacement.docmosis.cftlib.wa.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@Converter
@RequiredArgsConstructor
public class MessageInformationConverter implements AttributeConverter<MessageInformation, String> {
    private final ObjectMapper objectMapper;

    @Override
    public String convertToDatabaseColumn(MessageInformation attribute) {
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error converting MessageInformation to JSON", e);
        }
    }

    @Override
    public MessageInformation convertToEntityAttribute(String dbData) {
        try {
            return objectMapper.readValue(dbData, MessageInformation.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error reading JSON to MessageInformation", e);
        }
    }
}

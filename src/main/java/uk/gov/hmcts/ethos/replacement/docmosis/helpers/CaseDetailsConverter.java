package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;

@Component
@RequiredArgsConstructor
public class CaseDetailsConverter {

    private static final String DATA = "data";
    private static final String CASE_DATA = "case_data";

    private final ObjectMapper objectMapper;

    public CaseDetails convert(uk.gov.hmcts.reform.ccd.client.model.CaseDetails source) {
        return convert(source, CaseDetails.class);
    }

    public <T> T convert(uk.gov.hmcts.reform.ccd.client.model.CaseDetails source, Class<T> targetClass) {
        if (source == null) {
            return null;
        }

        ObjectNode caseDetailsNode = objectMapper.valueToTree(source);
        if (caseDetailsNode.has(DATA) && !caseDetailsNode.has(CASE_DATA)) {
            caseDetailsNode.set(CASE_DATA, caseDetailsNode.get(DATA));
        }

        return objectMapper.convertValue(caseDetailsNode, targetClass);
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }
}

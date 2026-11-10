package uk.gov.hmcts.ethos.replacement.docmosis.domain.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiDocumentAttachment {
    private String filename;
    private String mimeType;
    private byte[] data;
}

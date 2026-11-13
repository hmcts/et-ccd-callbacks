package uk.gov.hmcts.ethos.replacement.docmosis.domain.caseflags;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CaseFlagReferenceDataDetail {
    private String name;
    private Boolean externallyAvailable;
    private String flagCode;
    private String nativeFlagCode;
    private List<CaseFlagReferenceDataDetail> childFlags;
}

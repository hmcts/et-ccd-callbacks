package uk.gov.hmcts.et.common.model.ccd.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CaseNote {
    @CCD(label = "Title")
    @JsonProperty("title")
    private String title;
    @CCD(label = "Note", typeOverride = FieldType.TextArea)
    @JsonProperty("note")
    private String note;
    @CCD(label = "Author")
    @JsonProperty("author")
    private String author;
    @CCD(label = "Date")
    @JsonProperty("date")
    private String date;

}

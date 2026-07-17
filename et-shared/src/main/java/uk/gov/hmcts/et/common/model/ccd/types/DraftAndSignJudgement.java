package uk.gov.hmcts.et.common.model.ccd.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@JsonIgnoreProperties(ignoreUnknown = true)
@SuperBuilder
@Data
@NoArgsConstructor
public class DraftAndSignJudgement {
    @CCD(label = "Is this a Judgment", searchable = false, typeOverride = FieldType.YesOrNo)
    @JsonProperty("isJudgement")
    private String isJudgement;
    @CCD(
            label = "Documents",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "DocumentUpload"
    )
    @JsonProperty("draftAndSignJudgementDocuments")
    private List<DocumentTypeItem> draftAndSignJudgementDocuments;
    @CCD(label = "Any further directions", searchable = false, typeOverride = FieldType.TextArea)
    @JsonProperty("furtherDirections")
    private String furtherDirections;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "Document upload", showCondition = "isJudgement=\"dummy\"", categoryID = "C60", searchable = false)
  private uk.gov.hmcts.ccd.sdk.type.Document draftAndSignJudgementDocument;
  // ==== end synthesised definition-only fields ====
}

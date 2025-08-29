package uk.gov.hmcts.ethos.replacement.docmosis.domain.documents.notifications;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;

import java.util.List;

@SuperBuilder
@Data
public class ResponseAdminData {
    @JsonProperty("title")
    private String title;
    @JsonProperty("date")
    private String date;
    @JsonProperty("additionalInformation")
    private String additionalInformation;
    @JsonProperty("cmoOrRequest")
    private String cmoOrRequest;
    @JsonProperty("partiesToNotify")
    private String partiesToNotify;
    @JsonProperty("responseRequired")
    private String responseRequired;
    @JsonProperty("madeBy")
    private String madeBy;
    @JsonProperty("areThereDocuments")
    private String areThereDocuments;
    @JsonProperty("documents")
    private List<GenericTypeItem<DocumentType>> documents;
}

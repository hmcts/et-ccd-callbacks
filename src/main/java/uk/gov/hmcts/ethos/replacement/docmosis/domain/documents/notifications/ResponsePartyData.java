package uk.gov.hmcts.ethos.replacement.docmosis.domain.documents.notifications;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;

import java.util.List;

@SuperBuilder
@Data
public class ResponsePartyData {
    @JsonProperty("party")
    private String party;
    @JsonProperty("responseDate")
    private String responseDate;
    @JsonProperty("responseDetail")
    private String responseDetail;
    @JsonProperty("areThereDocuments")
    private String areThereDocuments;
    @JsonProperty("responseDocuments")
    private List<GenericTypeItem<DocumentType>> responseDocuments;
    @JsonProperty("copyToOtherParty")
    private String copyToOtherParty;
}

package uk.gov.hmcts.ethos.replacement.docmosis.domain.documents;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;

@SuperBuilder
@Data
public class ReferralTypeData {
    @JsonProperty("referCaseTo")
    private String referCaseTo;
    @JsonProperty("referentEmail")
    private String referentEmail;
    @JsonProperty("isUrgent")
    private String isUrgent;
    @JsonProperty("referralSubject")
    private String referralSubject;
    @JsonProperty("referralDetails")
    private String referralDetails;
    @JsonProperty("referralDocument")
    private List<DocumentTypeItem> referralDocument;
    @JsonProperty("referralInstruction")
    private String referralInstruction;
}

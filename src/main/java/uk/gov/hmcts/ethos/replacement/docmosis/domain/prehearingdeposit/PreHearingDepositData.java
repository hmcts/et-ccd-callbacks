package uk.gov.hmcts.ethos.replacement.docmosis.domain.prehearingdeposit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.types.ImportFile;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class PreHearingDepositData {
    @JsonProperty("preHearingDepositImportFile")
    private ImportFile preHearingDepositImportFile;
    @JsonProperty("preHearingDepositDataCollection")
    private List<PreHearingDepositTypeItem> preHearingDepositDataCollection;
}

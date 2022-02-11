package uk.gov.hmcts.ethos.replacement.docmosis.reports.hearingstojudgments;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HearingsToJudgmentsSearchResult {
    private Long total;
    private List<HearingsToJudgmentsSubmitEvent> cases;
}

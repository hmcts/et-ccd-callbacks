package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.model.reports.casesawaitingjudgment.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.service.hearings.HearingSelectionService;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service("bundlesRespondentService")
public class BundlesRespondentService {
    private final HearingSelectionService hearingSelectionService;
    public void populateSelectHearings(CaseData caseData){
        // caseData.getHearingCollection().stream()
        //         .filter(o -> o.getValue().getHearingDateCollection().stream().filter(x -> x.getValue().getListedDate()))
    }
}

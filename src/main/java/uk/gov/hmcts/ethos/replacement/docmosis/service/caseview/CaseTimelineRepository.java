package uk.gov.hmcts.ethos.replacement.docmosis.service.caseview;

import java.util.List;

public interface CaseTimelineRepository {

    List<CaseTimelineEvent> findRecent(long caseReference);
}

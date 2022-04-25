package uk.gov.hmcts.ethos.replacement.docmosis.reports.nochangeincurrentposition;

import uk.gov.hmcts.et.common.model.multiples.SubmitMultipleEvent;

import java.util.List;

public interface NoPositionChangeDataSource {
    List<NoPositionChangeSubmitEvent> getData(String caseTypeId, String reportDate, String managingOffice);

    List<SubmitMultipleEvent> getMultiplesData(String caseTypeId, List<String> multipleRefsList);
}

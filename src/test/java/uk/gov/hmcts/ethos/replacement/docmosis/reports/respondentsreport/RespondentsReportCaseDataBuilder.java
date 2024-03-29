package uk.gov.hmcts.ethos.replacement.docmosis.reports.respondentsreport;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ecm.common.model.reports.respondentsreport.RespondentsReportCaseData;
import uk.gov.hmcts.ecm.common.model.reports.respondentsreport.RespondentsReportSubmitEvent;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

@ExtendWith(SpringExtension.class)
class RespondentsReportCaseDataBuilder {
    private final RespondentsReportCaseData caseData = new RespondentsReportCaseData();

    public void withNoRespondents() {
        caseData.setRespondentCollection(null);
    }

    public RespondentSumTypeItem getRespondent(String respName) {
        RespondentSumTypeItem item = new RespondentSumTypeItem();
        RespondentSumType type = new RespondentSumType();
        type.setRespondentName(respName);
        item.setId(UUID.randomUUID().toString());
        item.setValue(type);
        return item;
    }

    public RepresentedTypeRItem getRepresentative(String respName, String repName) {
        RepresentedTypeRItem item = new RepresentedTypeRItem();
        RepresentedTypeR type = RepresentedTypeR.builder()
            .respRepName(respName)
            .nameOfRepresentative(repName).build();
        item.setId(UUID.randomUUID().toString());
        item.setValue(type);
        return item;
    }

    public void withOneRespondent() {
        caseData.setRespondentCollection(Collections.singletonList(getRespondent("Resp")));
    }

    public void withMoreThanOneRespondents() {
        RespondentSumTypeItem item1 = getRespondent("Resp1");
        RespondentSumTypeItem item2 = getRespondent("Resp2");
        caseData.setRespondentCollection(Arrays.asList(item1, item2));
    }

    public void withMoreThan1RespondentsRepresented() {
        RespondentSumTypeItem item1 = getRespondent("Resp1");
        RespondentSumTypeItem item2 = getRespondent("Resp2");
        RepresentedTypeRItem repItem1 = getRepresentative("Resp1", "Rep1");
        RepresentedTypeRItem repItem2 = getRepresentative("Resp2", "Rep1");

        caseData.setRepCollection(Arrays.asList(repItem1, repItem2));
        caseData.setRespondentCollection(Arrays.asList(item1, item2));
    }

    public RespondentsReportSubmitEvent buildAsSubmitEvent() {
        RespondentsReportSubmitEvent submitEvent = new RespondentsReportSubmitEvent();
        caseData.setEthosCaseReference("111");
        submitEvent.setCaseData(caseData);
        return submitEvent;
    }

}

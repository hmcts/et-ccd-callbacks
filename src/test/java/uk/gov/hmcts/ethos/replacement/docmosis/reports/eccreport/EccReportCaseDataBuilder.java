package uk.gov.hmcts.ethos.replacement.docmosis.reports.eccreport;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ecm.common.model.reports.eccreport.EccReportCaseData;
import uk.gov.hmcts.ecm.common.model.reports.eccreport.EccReportSubmitEvent;
import uk.gov.hmcts.et.common.model.ccd.items.EccCounterClaimTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.EccCounterClaimType;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;

import java.util.Arrays;
import java.util.UUID;

@ExtendWith(SpringExtension.class)
class EccReportCaseDataBuilder {
    private final EccReportCaseData caseData = new EccReportCaseData();

    public static EccReportCaseDataBuilder builder() {
        return new EccReportCaseDataBuilder();
    }

    public EccReportCaseDataBuilder withNoEcc() {
        caseData.setEccCases(null);
        return this;
    }

    public RespondentSumTypeItem getRespondent(String respName) {
        RespondentSumTypeItem item = new RespondentSumTypeItem();
        RespondentSumType type = new RespondentSumType();
        type.setRespondentName(respName);
        item.setId(UUID.randomUUID().toString());
        item.setValue(type);
        return item;
    }

    public EccCounterClaimTypeItem getEcc(String counterClaim) {
        EccCounterClaimTypeItem item = new EccCounterClaimTypeItem();
        EccCounterClaimType type = new EccCounterClaimType();
        type.setCounterClaim(counterClaim);
        item.setId(UUID.randomUUID().toString());
        item.setValue(type);
        return item;
    }

    public EccReportCaseDataBuilder withRespondents() {
        RespondentSumTypeItem item1 = getRespondent("Resp1");
        RespondentSumTypeItem item2 = getRespondent("Resp2");
        caseData.setRespondentCollection(Arrays.asList(item1, item2));

        return this;
    }

    public EccReportCaseDataBuilder withEccs() {
        EccCounterClaimTypeItem item1 = getEcc("ecc1");
        EccCounterClaimTypeItem item2 = getEcc("ecc2");
        caseData.setEccCases(Arrays.asList(item1, item2));

        return this;
    }

    public EccReportSubmitEvent buildAsSubmitEvent() {
        EccReportSubmitEvent submitEvent = new EccReportSubmitEvent();
        caseData.setEthosCaseReference("111");
        submitEvent.setState("Accepted");
        submitEvent.setCaseData(caseData);
        return submitEvent;
    }
}

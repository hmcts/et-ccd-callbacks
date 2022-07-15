package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import java.util.HashMap;
import java.util.Map;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper.nullCheck;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.InitialConsiderationData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.InitialConsiderationDocument;

public class InitialConsiderationHelper {

    public String getDocumentRequest(CaseData caseData) throws JsonProcessingException {
        InitialConsiderationData data = InitialConsiderationData.builder()
            .caseNumber(nullCheck(caseData.getEthosCaseReference()))
            .issuesJurisdiction(nullCheck(caseData.getIcJurisdictionCodeIssues()))
            .hearingAlreadyListed(nullCheck(caseData.getEtICHearingAlreadyListed()))
            .hearingListed(caseData.getEtICHearingListed())
            .hearingPostpone(caseData.getEtICPostponeGiveDetails())
            .hearingConvertF2f(caseData.getEtICConvertF2fGiveDetails())
            //.hearingListed(caseData.getEtICHearingAlreadyListed())
            .build();

        InitialConsiderationDocument document = InitialConsiderationDocument.builder()
            .accessKey("")
            .outputName("")
            .templateName("")
            .data(data).build();

        ObjectMapper mapper = new ObjectMapper();

       return  mapper.writeValueAsString(document);
    }
}





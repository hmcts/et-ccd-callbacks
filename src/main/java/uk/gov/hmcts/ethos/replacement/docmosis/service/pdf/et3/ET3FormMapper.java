package uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.webjars.NotFoundException;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Service to support ET3 Response journey. Contains methods for generating and saving ET3 Response documents.
 */
@Slf4j
public class ET3FormMapper {

    private ET3FormMapper() {
        // Add a private constructor to hide the implicit public one.
    }

    public static Map<String, Optional<String>> mapCaseData(CaseData caseData) {
        ConcurrentMap<String, Optional<String>> pdfFields = new ConcurrentHashMap<>();
        if (ObjectUtils.isEmpty(caseData)) {
            return pdfFields;
        }
        if (caseData.getSubmitEt3Respondent() == null || CollectionUtils.isEmpty(caseData.getRespondentCollection())) {
            throw new NotFoundException("submitEt3Respondent or respondentCollection is null");
        }
        String submitRespondent = caseData.getSubmitEt3Respondent().getSelectedLabel();
        RespondentSumType respondentSumType = caseData.getRespondentCollection().stream()
                .filter(r -> submitRespondent.equals(r.getValue().getRespondentName()))
                .toList().get(0).getValue();
        ET3FormHeaderMapper.mapHeader(caseData, respondentSumType, pdfFields);
        ET3FormClaimantMapper.mapClaimant(caseData, respondentSumType, pdfFields);
        return pdfFields;
    }

}

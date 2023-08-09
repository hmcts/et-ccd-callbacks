package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.JudgementTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.JudgementType;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.dynamiclists.DynamicJudgements.NO_HEARINGS;

@Service("judgmentValidationService")
public class JudgmentValidationService {

    public void validateJudgments(CaseData caseData) throws ParseException {
        if (CollectionUtils.isNotEmpty(caseData.getJudgementCollection())) {
            for (JudgementTypeItem judgementTypeItem : caseData.getJudgementCollection()) {
                populateJudgmentDateOfHearing(judgementTypeItem.getValue());
            }
        }
    }

    private void populateJudgmentDateOfHearing(JudgementType judgementType) throws ParseException {
        if (NO.equals(judgementType.getNonHearingJudgment())
                && !NO_HEARINGS.equals(judgementType.getDynamicJudgementHearing().getValue().getLabel())) {
            String hearingDate = judgementType.getDynamicJudgementHearing().getValue().getLabel();
            hearingDate = hearingDate.substring(hearingDate.length() - 11);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM yyyy");
            Date date = simpleDateFormat.parse(hearingDate);
            simpleDateFormat.applyPattern("yyyy-MM-dd");
            judgementType.setJudgmentHearingDate(simpleDateFormat.format(date));
        } else {
            judgementType.setDynamicJudgementHearing(null);
            judgementType.setJudgmentHearingDate(null);
        }
    }

}

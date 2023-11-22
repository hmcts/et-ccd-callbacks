package uk.gov.hmcts.ethos.replacement.docmosis.helpers.dynamiclists;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.JudgementTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.JudgementType;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.DynamicListHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.HearingsHelper;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Strings.isNullOrEmpty;

public final class DynamicJudgements {
    public static final String NO_HEARINGS = "No Hearings";

    private DynamicJudgements() {
    }

    public static void dynamicJudgements(CaseData caseData) {
        List<DynamicValueType> listHearings = DynamicListHelper.createDynamicHearingList(caseData);
        List<DynamicValueType> caseParties = DynamicListHelper.createDynamicRespondentName(
                caseData.getRespondentCollection());
        caseParties.add(DynamicListHelper.getDynamicCodeLabel("C: " + caseData.getClaimant(), caseData.getClaimant()));
        populateDynamicJudgements(caseData, listHearings, caseParties);
    }

    private static void populateDynamicJudgements(CaseData caseData, List<DynamicValueType> listHearings,
                                                  List<DynamicValueType> caseParties) {
        if (!caseParties.isEmpty()) {
            DynamicFixedListType hearingDynamicList = new DynamicFixedListType();
            hearingDynamicList.setListItems(listHearings);
            DynamicFixedListType parties = new DynamicFixedListType();
            parties.setListItems(caseParties);

            if (CollectionUtils.isNotEmpty(caseData.getJudgementCollection())) {
                List<JudgementTypeItem> judgementCollection = caseData.getJudgementCollection();
                for (JudgementTypeItem judgementTypeItem : judgementCollection) {
                    dynamicHearingDate(caseData, hearingDynamicList, judgementTypeItem.getValue());
                }
            } else {
                createDynamicJudgment(caseData, hearingDynamicList);
            }

        }
    }

    private static void createDynamicJudgment(CaseData caseData, DynamicFixedListType hearingDynamicList) {
        JudgementType judgmentType = new JudgementType();
        judgmentType.setDynamicJudgementHearing(hearingDynamicList);
        JudgementTypeItem judgmentTypeItem = new JudgementTypeItem();
        judgmentTypeItem.setValue(judgmentType);
        List<JudgementTypeItem> judgementTypeList = new ArrayList<>();
        judgementTypeList.add(judgmentTypeItem);
        caseData.setJudgementCollection(judgementTypeList);
    }

    private static void dynamicHearingDate(CaseData caseData, DynamicFixedListType hearingDynamicList,
                                           JudgementType judgementType) {
        DynamicValueType dynamicValueType;
        if (judgementType.getDynamicJudgementHearing() == null) {
            judgementType.setDynamicJudgementHearing(hearingDynamicList);
            if (StringUtils.isNotEmpty(judgementType.getJudgmentHearingDate())) {
                String judgementHearingDate = judgementType.getJudgmentHearingDate();
                String hearingNumber = HearingsHelper.findHearingNumber(caseData, judgementHearingDate);
                if (isNullOrEmpty(hearingNumber)) { // Check needed if hearing number cannot be found
                    judgementType.setJudgmentHearingDate(null);
                    return;
                } else {
                    dynamicValueType = DynamicListHelper.findDynamicValue(hearingDynamicList.getListItems(),
                            hearingNumber);
                }
            } else {
                dynamicValueType = hearingDynamicList.getListItems().get(0);
            }
        } else {
            dynamicValueType = judgementType.getDynamicJudgementHearing().getValue();
            judgementType.getDynamicJudgementHearing().setListItems(hearingDynamicList.getListItems());
        }
        judgementType.getDynamicJudgementHearing().setValue(dynamicValueType);
    }

}

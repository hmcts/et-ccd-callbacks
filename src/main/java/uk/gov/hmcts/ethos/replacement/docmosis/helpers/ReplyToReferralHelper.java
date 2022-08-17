package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;

import java.util.stream.Collectors;

@Slf4j
@Service
public final class ReplyToReferralHelper {
    private ReplyToReferralHelper() {
        //Constructor
    }

    /**
     * Create fields for referral dropdown selector.
     * @param caseData contains all the case data
     */
    public static void populateSelectReferralDropdown(CaseData caseData) {
        if (CollectionUtils.isEmpty(caseData.getReferralCollection())) {
            return;
        }

        caseData.setSelectReferralToReply(DynamicFixedListType.from(caseData.getReferralCollection().stream()
            .map(r -> DynamicValueType.create(
                r.getId(),
                r.getValue().getReferralNumber() + " " + r.getValue().getReferralSubject()))
            .collect(Collectors.toList())));
    }
}
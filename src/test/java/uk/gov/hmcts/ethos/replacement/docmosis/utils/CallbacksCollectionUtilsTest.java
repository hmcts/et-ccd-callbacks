package uk.gov.hmcts.ethos.replacement.docmosis.utils;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CallbacksCollectionUtilsTest {

    public static final String DUMMY_RESPONDENT_ID_1 = "dummy_respondent_id_1";
    public static final String DUMMY_RESPONDENT_ID_2 = "dummy_respondent_id_2";

    @Test
    void theSameByKey() {
        // ---- sameByField ----
        RespondentSumTypeItem respondentSumTypeItem1 = new RespondentSumTypeItem();
        respondentSumTypeItem1.setId(DUMMY_RESPONDENT_ID_1);
        respondentSumTypeItem1.setValue(RespondentSumType.builder().build());
        RespondentSumTypeItem respondentSumTypeItem2 = new RespondentSumTypeItem();
        respondentSumTypeItem2.setId(DUMMY_RESPONDENT_ID_2);
        respondentSumTypeItem2.setValue(RespondentSumType.builder().build());
        RespondentSumTypeItem respondentSumTypeItem3 = new RespondentSumTypeItem();
        respondentSumTypeItem3.setId(DUMMY_RESPONDENT_ID_1);
        respondentSumTypeItem3.setValue(RespondentSumType.builder().build());
        // when both are null should return true
        assertThat(CallbacksCollectionUtils.sameByKey(null, null, RespondentSumTypeItem::getId)).isTrue();
        // when one is null should return false
        assertThat(CallbacksCollectionUtils.sameByKey(null, List.of(respondentSumTypeItem1),
                RespondentSumTypeItem::getId)).isFalse();
        assertThat(CallbacksCollectionUtils.sameByKey(List.of(respondentSumTypeItem1), null,
                RespondentSumTypeItem::getId)).isFalse();
        // when null key extractor but both are same should return true
        assertThat(CallbacksCollectionUtils.sameByKey(List.of(respondentSumTypeItem1),
                List.of(respondentSumTypeItem3), null)).isTrue();
        // when null key extractor but different collections should return false
        assertThat(CallbacksCollectionUtils.sameByKey(List.of(respondentSumTypeItem1),
                List.of(respondentSumTypeItem2), null)).isFalse();
        // same keys, different order and values should return true
        assertThat(CallbacksCollectionUtils.sameByKey(List.of(respondentSumTypeItem1, respondentSumTypeItem2),
                List.of(respondentSumTypeItem2, respondentSumTypeItem3), RespondentSumTypeItem::getId)).isTrue();
        // different keys, should return false
        assertThat(CallbacksCollectionUtils.sameByKey(List.of(respondentSumTypeItem1),
                List.of(respondentSumTypeItem2), RespondentSumTypeItem::getId)).isFalse();
        // duplicates ignored, should return true
        assertThat(CallbacksCollectionUtils.sameByKey(List.of(respondentSumTypeItem1, respondentSumTypeItem3),
                List.of(respondentSumTypeItem1), RespondentSumTypeItem::getId)).isTrue();
    }

}

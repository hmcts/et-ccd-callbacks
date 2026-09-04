package uk.gov.hmcts.ethos.replacement.docmosis.utils;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

final class CallbacksCollectionUtilsTest {

    private static final String DUMMY_RESPONDENT_ID_1 = "dummy_respondent_id_1";
    private static final String DUMMY_RESPONDENT_ID_2 = "dummy_respondent_id_2";
    private static final String ROLE_SOLICITOR_A = "[SOLICITORA]";
    private static final String ROLE_SOLICITOR_B = "[SOLICITORB]";
    private static final String ROLE_SOLICITOR_C = "[SOLICITORC]";
    private static final String ROLE_SOLICITOR_D = "[SOLICITORD]";
    private static final String ROLE_SOLICITOR_E = "[SOLICITORE]";
    private static final String ROLE_SOLICITOR_F = "[SOLICITORF]";
    private static final String ROLE_SOLICITOR_G = "[SOLICITORG]";
    private static final String ROLE_SOLICITOR_H = "[SOLICITORH]";
    private static final String ROLE_SOLICITOR_I = "[SOLICITORI]";
    private static final String ROLE_SOLICITOR_J = "[SOLICITORJ]";

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

    @Test
    void theMergeListsWithoutDuplicates() {
        // when both first and second array lists are empty should return empty list
        List<String> rolesList1 = new ArrayList<>();
        List<String> rolesList2 = new ArrayList<>();
        assertThat(CallbacksCollectionUtils.mergeListsWithoutDuplicates(rolesList1, rolesList2)).isEmpty();
        // when one of the lists is empty should return the other list without duplicates
        rolesList1 = List.of(ROLE_SOLICITOR_A, ROLE_SOLICITOR_B, ROLE_SOLICITOR_C, ROLE_SOLICITOR_D, ROLE_SOLICITOR_E);
        rolesList2 = new ArrayList<>();
        assertThat(CallbacksCollectionUtils.mergeListsWithoutDuplicates(rolesList1, rolesList2))
                .containsExactlyInAnyOrder(ROLE_SOLICITOR_E, ROLE_SOLICITOR_A, ROLE_SOLICITOR_B,
                        ROLE_SOLICITOR_C, ROLE_SOLICITOR_D);
        // when both lists have same values should return the same list without duplicates
        rolesList2 = List.of(ROLE_SOLICITOR_A, ROLE_SOLICITOR_B, ROLE_SOLICITOR_C, ROLE_SOLICITOR_D, ROLE_SOLICITOR_E);
        assertThat(CallbacksCollectionUtils.mergeListsWithoutDuplicates(rolesList1, rolesList2))
                .containsExactlyInAnyOrder(ROLE_SOLICITOR_E, ROLE_SOLICITOR_A, ROLE_SOLICITOR_B,
                        ROLE_SOLICITOR_C, ROLE_SOLICITOR_D);
        // when both lists are different should return all values of both lists without duplicates
        rolesList2 = List.of(ROLE_SOLICITOR_F, ROLE_SOLICITOR_G, ROLE_SOLICITOR_H, ROLE_SOLICITOR_I, ROLE_SOLICITOR_J);
        assertThat(CallbacksCollectionUtils.mergeListsWithoutDuplicates(rolesList1, rolesList2))
                .containsExactlyInAnyOrder(ROLE_SOLICITOR_E, ROLE_SOLICITOR_A, ROLE_SOLICITOR_B, ROLE_SOLICITOR_C,
                        ROLE_SOLICITOR_D, ROLE_SOLICITOR_F, ROLE_SOLICITOR_G, ROLE_SOLICITOR_H, ROLE_SOLICITOR_I,
                        ROLE_SOLICITOR_J);
    }

    @Test
    void shouldFindDifferentObjectsFromFirstListComparedToSecondList() {
        assertThat(CallbacksCollectionUtils.findDifferentObjects(null, List.of("A", "B")))
                .isEmpty();

        assertThat(CallbacksCollectionUtils.findDifferentObjects(Collections.emptyList(), List.of("A", "B")))
                .isEmpty();

        assertThat(CallbacksCollectionUtils.findDifferentObjects(List.of("A", "B"), null))
                .containsExactly("A", "B");

        assertThat(CallbacksCollectionUtils.findDifferentObjects(List.of("A", "B"), Collections.emptyList()))
                .containsExactly("A", "B");

        assertThat(CallbacksCollectionUtils.findDifferentObjects(
                List.of("A", "B", "C"),
                List.of("B", "D")
        )).containsExactly("A", "C");

        assertThat(CallbacksCollectionUtils.findDifferentObjects(
                List.of("A", "B"),
                List.of("A", "B", "C")
        )).isEmpty();

        assertThat(CallbacksCollectionUtils.findDifferentObjects(
                List.of("A", "A", "B", "C"),
                List.of("B")
        )).containsExactly("A", "A", "C");
    }
}

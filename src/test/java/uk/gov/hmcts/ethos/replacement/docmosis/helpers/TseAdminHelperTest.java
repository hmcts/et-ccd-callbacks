package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationType;
import uk.gov.hmcts.et.common.model.ccd.items.ListTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.TypeItem;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;
import uk.gov.hmcts.ethos.utils.TseApplicationBuilder;

import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertNull;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLAIMANT_TITLE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.OPEN_STATE;

@ExtendWith(SpringExtension.class)
class TseAdminHelperTest {
    private CaseData caseData;

    @BeforeEach
    public void setUp() {
        caseData = CaseDataBuilder.builder()
            .withClaimantIndType("First", "Last")
            .withEthosCaseReference("1234")
            .withClaimant("First Last")
            .build();

        GenericTseApplicationType build = TseApplicationBuilder.builder()
            .withApplicant(CLAIMANT_TITLE)
            .withDate("13 December 2022")
            .withDue("20 December 2022")
            .withType("Withdraw my claim")
            .withDetails("Text")
            .withNumber("1")
            .withResponsesCount("0")
            .withStatus(OPEN_STATE)
            .build();

        TypeItem<GenericTseApplicationType> genericTseApplicationTypeItem = new TypeItem<>();
        genericTseApplicationTypeItem.setId(UUID.randomUUID().toString());
        genericTseApplicationTypeItem.setValue(build);
        caseData.setGenericTseApplicationCollection(ListTypeItem.from(genericTseApplicationTypeItem));
    }

    @Test
    void populateSelectApplicationAdminDropdown_withEmptyList_doesNothing() {
        caseData.setGenericTseApplicationCollection(null);
        DynamicFixedListType actual = TseAdminHelper.populateSelectApplicationAdminDropdown(caseData);
        assertNull(actual);
    }

    @Test
    void populateSelectApplicationAdminDropdown_withAnApplication_returnsDynamicList() {
        DynamicFixedListType actual = TseAdminHelper.populateSelectApplicationAdminDropdown(caseData);
        assertThat(actual.getListItems().size(), is(1));
    }
}

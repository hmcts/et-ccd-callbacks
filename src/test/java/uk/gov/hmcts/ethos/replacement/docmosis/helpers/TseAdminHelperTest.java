package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationType;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationTypeItem;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.CaseDataBuilder;
import uk.gov.hmcts.ethos.utils.TseApplicationBuilder;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNull;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLAIMANT_TITLE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.OPEN_STATE;

public class TseAdminHelperTest {
    private CaseData caseData;

    @Before
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

        GenericTseApplicationTypeItem genericTseApplicationTypeItem = new GenericTseApplicationTypeItem();
        genericTseApplicationTypeItem.setId(UUID.randomUUID().toString());
        genericTseApplicationTypeItem.setValue(build);
        caseData.setGenericTseApplicationCollection(List.of(genericTseApplicationTypeItem));
    }

    @Test
    public void populateSelectApplicationAdminDropdown_withEmptyList_doesNothing() {
        caseData.setGenericTseApplicationCollection(null);
        DynamicFixedListType actual = TseAdminHelper.populateSelectApplicationAdminDropdown(caseData);
        assertNull(actual);
    }

    @Test
    public void populateSelectApplicationAdminDropdown_withAnApplication_returnsDynamicList() {
        DynamicFixedListType actual = TseAdminHelper.populateSelectApplicationAdminDropdown(caseData);
        assertThat(actual.getListItems().size(), is(1));
    }
}

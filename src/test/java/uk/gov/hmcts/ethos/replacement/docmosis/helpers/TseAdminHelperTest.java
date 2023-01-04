package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationType;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationTypeItem;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.CCDRequestBuilder;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.CaseDataBuilder;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.TseApplicationBuilder;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNull;

public class TseAdminHelperTest {
    CCDRequest ccdRequest;
    CaseData caseData;

    @Before
    public void setUp() {
        caseData = CaseDataBuilder.builder()
            .withClaimantIndType("First", "Last")
            .withEthosCaseReference("1234")
            .build();

        caseData.setClaimant("First Last");

        ccdRequest = CCDRequestBuilder.builder()
            .withState("Accepted")
            .withCaseId("1234")
            .withCaseData(caseData)
            .build();

        GenericTseApplicationType build = TseApplicationBuilder.builder()
            .withApplicant("Claimant")
            .withDate("13 December 2022")
            .withDue("20 December 2022")
            .withType("Withdraw my claim")
            .withDetails("Text")
            .withNumber("1")
            .withResponsesCount("0")
            .withStatus("Open")
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

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

import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLAIMANT_TITLE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.OPEN_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.RESPONDENT_TITLE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@ExtendWith(SpringExtension.class)
class TseViewApplicationHelperTest {
    private CaseData caseData;

    @BeforeEach
    public void setUp() {
        caseData = CaseDataBuilder.builder()
            .withClaimantIndType("First", "Last")
            .withEthosCaseReference("1234")
            .withClaimant("First Last")
            .withRespondent("Respondent Name", YES, "13 December 2022", false)
            .build();

        GenericTseApplicationType build = TseApplicationBuilder.builder().withApplicant(CLAIMANT_TITLE)
            .withDate("13 December 2022").withDue("20 December 2022").withType("Withdraw my claim")
            .withCopyToOtherPartyYesOrNo(YES).withDetails("Text").withNumber("1").withResponsesCount("0")
            .withStatus(OPEN_STATE).build();

        TypeItem<GenericTseApplicationType> genericTseApplicationTypeItem = new TypeItem<>();
        genericTseApplicationTypeItem.setId(UUID.randomUUID().toString());
        genericTseApplicationTypeItem.setValue(build);
        caseData.setGenericTseApplicationCollection(ListTypeItem.from(genericTseApplicationTypeItem));
    }

    @Test
    void populateOpenOrClosedApplications_withEmptyList_doesNothing() {
        caseData.setGenericTseApplicationCollection(null);
        caseData.setTseViewApplicationOpenOrClosed("Open");
        DynamicFixedListType actual = TseViewApplicationHelper.populateOpenOrClosedApplications(caseData);
        assertNull(actual);
    }

    @Test
    void populateOpenApplications_withAnOpenApplication_returnsDynamicList() {
        caseData.setTseViewApplicationOpenOrClosed("Open");

        DynamicFixedListType actual = TseViewApplicationHelper.populateOpenOrClosedApplications(caseData);

        assert actual != null;
        assertThat(actual.getListItems().size(), is(1));
    }

    @Test
    void populateClosedApplications_withNoClosedApplications_returnEmptyList() {
        caseData.setTseViewApplicationOpenOrClosed("Closed");

        DynamicFixedListType actual = TseViewApplicationHelper.populateOpenOrClosedApplications(caseData);

        assert actual != null;
        assertThat(actual.getListItems().size(), is(0));
    }

    @Test
    void shouldNotShareApplicationWithRespondent() {
        GenericTseApplicationType build = TseApplicationBuilder.builder().withApplicant(CLAIMANT_TITLE)
                .withDate("13 December 2022").withDue("20 December 2022").withType("Withdraw my claim")
                .withCopyToOtherPartyYesOrNo(NO).withDetails("Text").withNumber("1").withResponsesCount("0")
                .withStatus(OPEN_STATE).build();

        TypeItem<GenericTseApplicationType> genericTseApplicationTypeItem = new TypeItem<>();
        genericTseApplicationTypeItem.setId(UUID.randomUUID().toString());
        genericTseApplicationTypeItem.setValue(build);
        assertFalse(TseViewApplicationHelper.applicationsSharedWithRespondent(genericTseApplicationTypeItem));
    }

    @Test
    void shouldShareApplicationWithRespondentForRespondentApplication() {
        GenericTseApplicationType build = TseApplicationBuilder.builder().withApplicant(RESPONDENT_TITLE)
                .withDate("13 December 2022").withDue("20 December 2022").withType("Withdraw my claim")
                .withCopyToOtherPartyYesOrNo(NO).withDetails("Text").withNumber("1").withResponsesCount("0")
                .withStatus(OPEN_STATE).build();

        TypeItem<GenericTseApplicationType> genericTseApplicationTypeItem = new TypeItem<>();
        genericTseApplicationTypeItem.setId(UUID.randomUUID().toString());
        genericTseApplicationTypeItem.setValue(build);
        assertTrue(TseViewApplicationHelper.applicationsSharedWithRespondent(genericTseApplicationTypeItem));
    }
}

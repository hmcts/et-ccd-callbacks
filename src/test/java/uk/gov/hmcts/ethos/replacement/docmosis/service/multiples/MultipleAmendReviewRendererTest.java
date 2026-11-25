package uk.gov.hmcts.ethos.replacement.docmosis.service.multiples;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantIndType;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MultipleAmendReviewRendererTest {

    private final MultipleAmendReviewRenderer renderer = new MultipleAmendReviewRenderer();

    @Test
    void rendersClaimantAndAllRespondentsOnSeparateLines() {
        CaseData data = new CaseData();
        data.setEthosCaseReference("6000187/2026");
        ClaimantIndType claimant = new ClaimantIndType();
        claimant.setClaimantFirstNames("Jane");
        claimant.setClaimantLastName("Smith");
        data.setClaimantIndType(claimant);
        data.setRespondentCollection(List.of(respondent("First Ltd"), respondent("Second Ltd")));

        String html = renderer.render(List.of(data), false);

        assertThat(html).contains("You are adding 1 case to this multiple.",
                ">Case number</th>", ">Claimant</th>", ">Respondents</th>",
                "6000187/2026", "Jane Smith", "<div>First Ltd</div><div>Second Ltd</div>",
                "Use Previous to edit the cases you are adding. Submit will add the above cases.")
                .doesNotContain("<strong>");
    }

    @Test
    void rendersRemovalCountAndKeepsCaseOrder() {
        CaseData first = new CaseData();
        first.setEthosCaseReference("6000189/2026");
        first.setClaimantCompany("Claimant Company");
        CaseData second = new CaseData();
        second.setEthosCaseReference("6000187/2026");
        CaseData third = new CaseData();
        third.setEthosCaseReference("6000188/2026");

        String html = renderer.render(List.of(first, second, third), true);

        assertThat(html).contains("You are removing 3 cases from this multiple.",
                "Claimant Company",
                "Use Previous to edit the cases you are removing. Submit will remove the above cases.")
                .doesNotContain("<strong>");
        assertThat(html.indexOf("6000189/2026")).isLessThan(html.indexOf("6000187/2026"));
        assertThat(html.indexOf("6000187/2026")).isLessThan(html.indexOf("6000188/2026"));
    }

    @Test
    void escapesCaseReferencesAndPartyNames() {
        CaseData data = new CaseData();
        data.setEthosCaseReference("<script>case</script>");
        data.setClaimantCompany("<img src=x onerror=alert(1)>");
        data.setRespondentCollection(List.of(respondent("<script>alert(1)</script> & Sons")));

        String html = renderer.render(List.of(data), true);

        assertThat(html).contains("&lt;script&gt;", "&lt;img", "&amp; Sons")
                .doesNotContain("<script>", "<img");
    }

    @Test
    void handlesMissingPartyDetailsAndPartiallyPopulatedNames() {
        CaseData data = new CaseData();
        ClaimantIndType claimant = new ClaimantIndType();
        claimant.setClaimantLastName("Smith");
        data.setClaimantIndType(claimant);
        data.setRespondentCollection(Arrays.asList(null, new RespondentSumTypeItem(), respondent("")));

        String html = renderer.render(List.of(data), false);

        assertThat(html).contains("Smith", "<div>Not provided</div>").doesNotContain(">null<");
    }

    @Test
    void usesLegacyPartyNamesWhenStructuredDetailsAreAbsent() {
        CaseData data = new CaseData();
        data.setClaimant("Legacy claimant");
        data.setRespondent("Legacy respondent");

        assertThat(renderer.render(List.of(data), false)).contains("Legacy claimant", "Legacy respondent");
    }

    @Test
    void fallsBackToRespondentOrganisationAndIndividualNames() {
        RespondentSumTypeItem company = respondent(null);
        company.getValue().setRespondentOrganisation("Respondent Company");
        RespondentSumTypeItem individual = respondent(null);
        individual.getValue().setRespondentFirstName("Alex");
        individual.getValue().setRespondentLastName("Jones");
        CaseData data = new CaseData();
        data.setRespondentCollection(List.of(company, individual));

        assertThat(renderer.render(List.of(data), false))
                .contains("<div>Respondent Company</div><div>Alex Jones</div>");
    }

    private RespondentSumTypeItem respondent(String name) {
        RespondentSumTypeItem item = new RespondentSumTypeItem();
        RespondentSumType respondent = new RespondentSumType();
        respondent.setRespondentName(name);
        item.setValue(respondent);
        return item;
    }
}

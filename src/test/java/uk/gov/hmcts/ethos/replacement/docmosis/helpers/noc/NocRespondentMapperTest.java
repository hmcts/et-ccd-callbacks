package uk.gov.hmcts.ethos.replacement.docmosis.helpers.noc;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.Organisation;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NocRespondentMapperTest {
    @Test
    void getFirstRepOrganisationId_returnsFirstOrganisationId() {
        Organisation org = Organisation.builder()
            .organisationID("org1")
            .build();
        RepresentedTypeRItem item = new RepresentedTypeRItem();
        item.setValue(RepresentedTypeR.builder()
            .respondentOrganisation(org)
            .build());
        List<RepresentedTypeRItem> reps = List.of(item);

        String actual = NocRespondentMapper.getFirstRepOrganisationId(reps);

        assertThat(actual).isEqualTo("org1");
    }

    @Test
    void getFirstRepOrganisationId_returnsNullIfNoOrganisation() {
        RepresentedTypeRItem item = new RepresentedTypeRItem();
        item.setValue(RepresentedTypeR.builder()
            .respondentOrganisation(null)
            .build());
        List<RepresentedTypeRItem> reps = List.of(item);

        String actual = NocRespondentMapper.getFirstRepOrganisationId(reps);

        assertThat(actual).isNull();
    }

    @Test
    void getFirstRepOrganisationId_returnsNullIfEmpty() {
        String actual = NocRespondentMapper.getFirstRepOrganisationId(Collections.emptyList());

        assertThat(actual).isNull();
    }

    @Test
    void getRespondentCollectionToEmail_filtersCorrectly() {
        // Respondent 1: has rep, Respondent 2: no rep, Respondent 3: in revoke list
        RespondentSumTypeItem r1 = new RespondentSumTypeItem();
        r1.setId("r1");
        RespondentSumTypeItem r2 = new RespondentSumTypeItem();
        r2.setId("r2");
        RespondentSumTypeItem r3 = new RespondentSumTypeItem();
        r3.setId("r3");
        List<RespondentSumTypeItem> respondents = List.of(r1, r2, r3);
        CaseData caseData = new CaseData();
        caseData.setRespondentCollection(new ArrayList<>(respondents));

        RepresentedTypeRItem repItem = new RepresentedTypeRItem();
        repItem.setValue(RepresentedTypeR.builder()
            .respondentId("r1")
            .build());
        caseData.setRepCollection(List.of(repItem));

        List<String> revoke = List.of("r3");

        List<RespondentSumTypeItem> actual = NocRespondentMapper.getRespondentCollectionToEmail(caseData, revoke);

        assertThat(actual).containsExactly(r2);
    }

    @Test
    void getRespondentCollectionToEmail_emptyCollections() {
        CaseData caseData = new CaseData();
        caseData.setRespondentCollection(new ArrayList<>());
        caseData.setRepCollection(new ArrayList<>());

        List<RespondentSumTypeItem> actual = NocRespondentMapper.getRespondentCollectionToEmail(caseData, List.of());

        assertThat(actual).isEmpty();
    }

    @Test
    void getOrganisationName_returnsFirstOrganisationName() {
        RepresentedTypeRItem item = new RepresentedTypeRItem();
        item.setValue(RepresentedTypeR.builder()
            .nameOfOrganisation("OrgName")
            .build());
        List<RepresentedTypeRItem> reps = List.of(item);

        String actual = NocRespondentMapper.getOrganisationName(reps);

        assertThat(actual).isEqualTo("OrgName");
    }

    @Test
    void getOrganisationName_returnsEmptyIfNoName() {
        RepresentedTypeRItem item = new RepresentedTypeRItem();
        item.setValue(RepresentedTypeR.builder()
            .nameOfOrganisation("")
            .build());
        List<RepresentedTypeRItem> reps = List.of(item);

        String actual = NocRespondentMapper.getOrganisationName(reps);

        assertThat(actual).isEmpty();
    }

    @Test
    void getOrganisationName_returnsEmptyIfEmptyList() {
        String actual = NocRespondentMapper.getOrganisationName(Collections.emptyList());

        assertThat(actual).isEmpty();
    }

    @Test
    void getRepresentativeNames_joinsDistinctNames() {
        RepresentedTypeRItem item1 = new RepresentedTypeRItem();
        item1.setValue(RepresentedTypeR.builder()
            .nameOfRepresentative("Rep1")
            .build());
        RepresentedTypeRItem item2 = new RepresentedTypeRItem();
        item2.setValue(RepresentedTypeR.builder()
            .nameOfRepresentative("Rep2")
            .build());
        RepresentedTypeRItem item3 = new RepresentedTypeRItem();
        item3.setValue(RepresentedTypeR.builder()
            .nameOfRepresentative("Rep1")
            .build());
        List<RepresentedTypeRItem> reps = List.of(item1, item2, item3);

        String actual = NocRespondentMapper.getRepresentativeNames(reps);

        assertThat(actual).isEqualTo("Rep1, Rep2");
    }

    @Test
    void getRepresentativeNames_emptyList() {
        String actual = NocRespondentMapper.getRepresentativeNames(Collections.emptyList());

        assertThat(actual).isEmpty();
    }

    @Test
    void getRepresentativeEmails_returnsDistinctEmails() {
        RepresentedTypeRItem item1 = new RepresentedTypeRItem();
        item1.setValue(RepresentedTypeR.builder()
            .representativeEmailAddress("a@b.com")
            .build());
        RepresentedTypeRItem item2 = new RepresentedTypeRItem();
        item2.setValue(RepresentedTypeR.builder()
            .representativeEmailAddress("b@b.com")
            .build());
        RepresentedTypeRItem item3 = new RepresentedTypeRItem();
        item3.setValue(RepresentedTypeR.builder()
            .representativeEmailAddress("a@b.com")
            .build());
        List<RepresentedTypeRItem> reps = List.of(item1, item2, item3);

        List<String> actual = NocRespondentMapper.getRepresentativeEmails(reps);

        assertThat(actual).containsExactlyInAnyOrder("a@b.com", "b@b.com");
    }

    @Test
    void getRepresentativeEmails_emptyList() {
        List<String> actual = NocRespondentMapper.getRepresentativeEmails(Collections.emptyList());

        assertThat(actual).isEmpty();
    }

    @Test
    void getRespondentPartyNames_joinsNames() {
        RepresentedTypeRItem item1 = new RepresentedTypeRItem();
        item1.setValue(RepresentedTypeR.builder()
            .respRepName("Party1")
            .build());
        RepresentedTypeRItem item2 = new RepresentedTypeRItem();
        item2.setValue(RepresentedTypeR.builder()
            .respRepName("Party2")
            .build());
        List<RepresentedTypeRItem> reps = List.of(item1, item2);

        String actual = NocRespondentMapper.getRespondentPartyNames(reps);

        assertThat(actual).isEqualTo("Party1, Party2");
    }

    @Test
    void getRespondentPartyNames_emptyList() {
        String actual = NocRespondentMapper.getRespondentPartyNames(Collections.emptyList());

        assertThat(actual).isEmpty();
    }

    @Test
    void getRespondentIds_returnsIds() {
        RepresentedTypeRItem item1 = new RepresentedTypeRItem();
        item1.setValue(RepresentedTypeR.builder()
            .respondentId("id1")
            .build());
        RepresentedTypeRItem item2 = new RepresentedTypeRItem();
        item2.setValue(RepresentedTypeR.builder()
            .respondentId("id2")
            .build());
        List<RepresentedTypeRItem> reps = List.of(item1, item2);

        List<String> actual = NocRespondentMapper.getRespondentIds(reps);

        assertThat(actual).containsExactly("id1", "id2");
    }

    @Test
    void getRespondentIds_nullList() {
        List<String> actual = NocRespondentMapper.getRespondentIds(null);

        assertThat(actual).isEmpty();
    }

    @Test
    void getRespondentEmail_prefersResponseRespondentEmail() {
        RespondentSumType respondent = RespondentSumType.builder()
                .responseRespondentEmail("main@email.com")
                .respondentEmail("fallback@email.com")
                .build();

        String actual = NocRespondentMapper.getRespondentEmail(respondent);

        assertThat(actual).isEqualTo("main@email.com");
    }

    @Test
    void getRespondentEmail_fallbackToRespondentEmail() {
        RespondentSumType respondent = RespondentSumType.builder()
                .respondentEmail("fallback@email.com")
                .build();

        String actual = NocRespondentMapper.getRespondentEmail(respondent);

        assertThat(actual).isEqualTo("fallback@email.com");
    }

    @Test
    void getRespondentEmail_returnsNullIfBothBlank() {
        RespondentSumType respondent = RespondentSumType.builder()
                .build();

        String actual = NocRespondentMapper.getRespondentEmail(respondent);

        assertThat(actual).isNull();
    }
}

package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.webjars.NotFoundException;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.CaseDataBuilder;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

class Et3VettingServiceTest {
    private Et3VettingService et3VettingService;
    private CaseData caseData;

    @BeforeEach
    void setUp() {
        et3VettingService = new Et3VettingService(documentManagementService);
        caseData = CaseDataBuilder.builder()
            .withClaimantIndType("Doris", "Johnson")
            .withClaimantType("232 Petticoat Square", "3 House", null,
                "London", "W10 4AG", "United Kingdom")
            .withRespondentWithAddress("Antonio Vazquez",
                "11 Small Street", "22 House", null,
                "Manchester", "M12 42R", "United Kingdom",
                "1234/5678/90")
            .withRespondentWithAddress("Juan Garcia",
                "32 Sweet Street", "14 House", null,
                "Manchester", "M11 4ED", "United Kingdom",
                "2987/6543/01")
            .withChooseEt3Respondent("Antonio Vazquez")
            .build();
        caseData.setEt3NoEt3Response("Test data");
    }

    @Test
    void whenGivenASubmittedEt3Response_shouldSetEt3VettingCompleted() {
        et3VettingService.saveEt3VettingToRespondent(caseData);
        RespondentSumType result = caseData.getRespondentCollection().get(0).getValue();

        assertThat(result.getEt3VettingCompleted(),
            equalTo(YES));
    }

    @Test
    void whenNoRespondentMatches_ShouldThrowFailedLookup() {
        caseData = CaseDataBuilder.builder()
            .withRespondentWithAddress("Juan Garcia",
                "32 Sweet Street", "14 House", null,
                "Manchester", "M11 4ED", "United Kingdom",
                "2987/6543/01")
            .withChooseEt3Respondent("Antonio Vazquez")
            .build();

        assertThrows(NotFoundException.class, () -> et3VettingService.saveEt3VettingToRespondent(caseData));
    }

    @Test
    void whenRestoreVettingFromRespondent_RecoverData() {
        et3VettingService.saveEt3VettingToRespondent(caseData);
        assertNull(caseData.getEt3NoEt3Response());

        DynamicValueType respondent = DynamicValueType.create("Antonio Vazquez", "Antonio Vazquez");
        DynamicFixedListType respondentList = new DynamicFixedListType();
        respondentList.setListItems(List.of(respondent));
        respondentList.setValue(respondent);
        caseData.setEt3ChooseRespondent(respondentList);

        et3VettingService.restoreEt3VettingFromRespondentOntoCaseData(caseData);
        assertThat(caseData.getEt3NoEt3Response(),
            equalTo("Test data"));
    }
}

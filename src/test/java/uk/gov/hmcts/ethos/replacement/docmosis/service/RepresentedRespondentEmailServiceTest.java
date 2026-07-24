package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;

import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

class RepresentedRespondentEmailServiceTest {

    private static final String RESPONDENT_ID_ONE = "respondent-one";
    private static final String RESPONDENT_ID_TWO = "respondent-two";
    private static final String OLD_EMAIL = "old@example.com";
    private static final String NEW_EMAIL = "new@example.com";
    private static final String OLD_USER_ID = "old-user-id";

    private RepresentedRespondentEmailService service;
    private CaseData caseData;
    private RespondentSumTypeItem firstRespondent;
    private RespondentSumTypeItem secondRespondent;

    @BeforeEach
    void setUp() {
        service = new RepresentedRespondentEmailService();
        firstRespondent = respondent(RESPONDENT_ID_ONE, "First respondent", OLD_EMAIL, OLD_USER_ID, YES);
        secondRespondent = respondent(RESPONDENT_ID_TWO, "Second respondent",
                "second@example.com", "second-user-id", NO);

        caseData = new CaseData();
        caseData.setRespondentCollection(List.of(firstRespondent, secondRespondent));
    }

    @Test
    void initialiseListsOnlyRepresentedRespondents() {
        assertThat(service.initialise(caseData)).isEmpty();
        assertThat(caseData.getRespondentEmailUpdateSelection().getListItems())
                .extracting("code", "label")
                .containsExactly(tuple(RESPONDENT_ID_ONE, "First respondent"));
        assertThat(caseData.getCurrentRespondentEmail()).isNull();
        assertThat(caseData.getNewRespondentEmail()).isNull();
    }

    @Test
    void initialiseReturnsErrorWhenNoRepresentedRespondentExists() {
        firstRespondent.getValue().setRepresented(NO);

        assertThat(service.initialise(caseData))
                .containsExactly(RepresentedRespondentEmailService.NO_REPRESENTED_RESPONDENTS_ERROR);
    }

    @Test
    void initialiseIncludesRespondentLinkedToRepresentative() {
        firstRespondent.getValue().setRepresented(null);
        caseData.setRepCollection(List.of(representativeFor(RESPONDENT_ID_ONE, "First respondent")));

        assertThat(service.initialise(caseData)).isEmpty();
        assertThat(caseData.getRespondentEmailUpdateSelection().getListItems())
                .extracting("code")
                .containsExactly(RESPONDENT_ID_ONE);
    }

    @Test
    void initialiseExcludesRespondentWhoseRepresentativeWasRemoved() {
        firstRespondent.getValue().setRepresented(YES);
        firstRespondent.getValue().setRepresentativeRemoved(YES);

        assertThat(service.initialise(caseData))
                .containsExactly(RepresentedRespondentEmailService.NO_REPRESENTED_RESPONDENTS_ERROR);
    }

    @Test
    void populateCurrentEmailUsesSelectedRespondentsResponseEmail() {
        firstRespondent.getValue().setResponseRespondentEmail("portal@example.com");
        selectRespondent(RESPONDENT_ID_ONE);

        assertThat(service.populateCurrentEmail(caseData)).isEmpty();
        assertThat(caseData.getCurrentRespondentEmail()).isEqualTo("portal@example.com");
    }

    @Test
    void populateCurrentEmailRejectsMissingSelection() {
        caseData.setCurrentRespondentEmail(OLD_EMAIL);

        assertThat(service.populateCurrentEmail(caseData))
                .containsExactly(RepresentedRespondentEmailService.RESPONDENT_REQUIRED_ERROR);
        assertThat(caseData.getCurrentRespondentEmail()).isNull();
    }

    @Test
    void validateRejectsInvalidEmail() {
        selectRespondent(RESPONDENT_ID_ONE);
        caseData.setCurrentRespondentEmail(OLD_EMAIL);
        caseData.setNewRespondentEmail("not-an-email");

        assertThat(service.validateNewEmail(caseData))
                .containsExactly("The email address entered is invalid.");
    }

    @Test
    void validateRejectsUnchangedEmailIgnoringCase() {
        selectRespondent(RESPONDENT_ID_ONE);
        caseData.setCurrentRespondentEmail(OLD_EMAIL);
        caseData.setNewRespondentEmail(OLD_EMAIL.toUpperCase(Locale.ROOT));

        assertThat(service.validateNewEmail(caseData))
                .containsExactly(RepresentedRespondentEmailService.EMAIL_UNCHANGED_ERROR);
    }

    @Test
    void prepareUpdateAppliesContactEmailWithoutChangingIdamId() {
        selectRespondent(RESPONDENT_ID_ONE);
        caseData.setCurrentRespondentEmail(OLD_EMAIL);
        caseData.setNewRespondentEmail(NEW_EMAIL);

        assertThat(service.prepareUpdate(caseData)).isEmpty();

        assertThat(firstRespondent.getValue().getRespondentEmail()).isEqualTo(NEW_EMAIL);
        assertThat(firstRespondent.getValue().getResponseRespondentEmail()).isEqualTo(NEW_EMAIL);
        assertThat(firstRespondent.getValue().getIdamId()).isEqualTo(OLD_USER_ID);
        assertThat(caseData.getCurrentRespondentEmail()).isNull();
        assertThat(caseData.getNewRespondentEmail()).isNull();
    }

    @Test
    void prepareUpdateReturnsErrorsWithoutChangingCaseData() {
        selectRespondent(RESPONDENT_ID_ONE);
        caseData.setCurrentRespondentEmail(OLD_EMAIL);
        caseData.setNewRespondentEmail(OLD_EMAIL);

        assertThat(service.prepareUpdate(caseData))
                .containsExactly(RepresentedRespondentEmailService.EMAIL_UNCHANGED_ERROR);
        assertThat(firstRespondent.getValue().getRespondentEmail()).isEqualTo(OLD_EMAIL);
        assertThat(firstRespondent.getValue().getIdamId()).isEqualTo(OLD_USER_ID);
    }

    private void selectRespondent(String respondentId) {
        DynamicFixedListType selection = DynamicFixedListType.from(
                respondentId,
                RESPONDENT_ID_ONE.equals(respondentId) ? "First respondent" : "Second respondent",
                true);
        caseData.setRespondentEmailUpdateSelection(selection);
    }

    private static RespondentSumTypeItem respondent(String id,
                                                    String name,
                                                    String email,
                                                    String idamId,
                                                    String represented) {
        RespondentSumType value = new RespondentSumType();
        value.setRespondentName(name);
        value.setRespondentEmail(email);
        value.setIdamId(idamId);
        value.setRepresented(represented);
        RespondentSumTypeItem item = new RespondentSumTypeItem();
        item.setId(id);
        item.setValue(value);
        return item;
    }

    private static RepresentedTypeRItem representativeFor(String respondentId, String respondentName) {
        RepresentedTypeR value = new RepresentedTypeR();
        value.setRespondentId(respondentId);
        value.setRespRepName(respondentName);
        RepresentedTypeRItem item = new RepresentedTypeRItem();
        item.setId("rep-" + respondentId);
        item.setValue(value);
        return item;
    }
}

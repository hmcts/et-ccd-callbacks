package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.constants.ET1ReppedConstants;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.ET1ReppedConstants.INDIVIDUAL;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.ET1ReppedConstants.ORGANISATION;
import static uk.gov.hmcts.ethos.utils.CaseDataBuilder.createGenericAddress;

class Et1ReppedHelperTest {

    private CaseData caseData;
    private CCDRequest ccdRequest;
    private CaseDetails caseDetails;

    @BeforeEach
    void setUp() {
        caseData = new CaseData();

        caseDetails = new CaseDetails();
        caseDetails.setCaseData(caseData);
        caseDetails.setCaseTypeId(ENGLANDWALES_CASE_TYPE_ID);
        caseDetails.setCaseId("1234567890123456");

        ccdRequest = new CCDRequest();
        ccdRequest.setCaseDetails(caseDetails);
    }

    @Test
    void setCreateDraftData() {
        Et1ReppedHelper.setCreateDraftData(caseData, caseDetails.getCaseId());
        assertEquals(NO, caseData.getEt1ReppedSectionOne());
        assertEquals(NO, caseData.getEt1ReppedSectionTwo());
        assertEquals(NO, caseData.getEt1ReppedSectionThree());
        assertNotNull(caseData.getEt1ClaimStatuses());
    }

    @ParameterizedTest
    @MethodSource
    void setEt1SectionStatuses(String eventId, String sectionOne, String sectionTwo, String sectionThree) {
        caseData.setClaimantFirstName("First");
        caseData.setClaimantLastName("Last");
        caseData.setRespondentType(INDIVIDUAL);
        generateRespondentTypeInfo(INDIVIDUAL);
        ccdRequest.setEventId(eventId);
        Et1ReppedHelper.setCreateDraftData(caseData, caseDetails.getCaseId());
        Et1ReppedHelper.setEt1SectionStatuses(ccdRequest);
        assertEquals(sectionOne, caseData.getEt1ReppedSectionOne());
        assertEquals(sectionTwo, caseData.getEt1ReppedSectionTwo());
        assertEquals(sectionThree, caseData.getEt1ReppedSectionThree());
    }

    private static Stream<Arguments> setEt1SectionStatuses() {
        return Stream.of(
                Arguments.of("et1SectionOne", YES, NO, NO),
                Arguments.of("et1SectionTwo", NO, YES, NO),
                Arguments.of("et1SectionThree", NO, NO, YES)
        );
    }

    @Test
    void setEt1SectionStatusesInvalidEventId() {
        Et1ReppedHelper.setCreateDraftData(caseData, caseDetails.getCaseId());
        ccdRequest.setEventId("invalid");
        assertThrows(IllegalArgumentException.class, () -> Et1ReppedHelper.setEt1SectionStatuses(ccdRequest));
    }

    @ParameterizedTest
    @MethodSource
    void validateSingleOption(List<String> options, int expected) {
        assertEquals(expected, Et1ReppedHelper.validateSingleOption(options).size());
    }

    private static Stream<Arguments> validateSingleOption() {
        return Stream.of(
                Arguments.of(null, 0),
                Arguments.of(List.of(), 0),
                Arguments.of(List.of("test"), 0),
                Arguments.of(List.of("test", "test"), 1)
        );
    }

    @ParameterizedTest
    @MethodSource
    void generateRespondentPreamble(String type, String respondentName) {
        caseData.setRespondentType(type);
        generateRespondentTypeInfo(type);
        Et1ReppedHelper.generateRespondentPreamble(caseData);
        assertNotNull(caseData.getAddAdditionalRespondentPreamble());
        assertEquals(String.format(ET1ReppedConstants.RESPONDENT_PREAMBLE, respondentName),
                caseData.getAddAdditionalRespondentPreamble());
    }

    private static Stream<Arguments> generateRespondentPreamble() {
        return Stream.of(
                Arguments.of(INDIVIDUAL, "First Last"),
                Arguments.of(ORGANISATION, "Org")
        );
    }

    @Test
    void generateRespondentPreamble_invalidValue() {
        caseData.setRespondentType("invalid");
        assertThrows(IllegalArgumentException.class, () -> Et1ReppedHelper.generateRespondentPreamble(caseData));
    }

    @Test
    void generateWorkAddressLabel() {
        caseData.setRespondentAddress(createGenericAddress());
        Et1ReppedHelper.generateWorkAddressLabel(caseData);
        assertEquals(caseData.getRespondentAddress().toString(), createGenericAddress().toString());
    }

    @Test
    void generateWorkAddressLabel_nullAddress() {
        assertThrows(NullPointerException.class, () -> Et1ReppedHelper.generateWorkAddressLabel(caseData));
    }

    private void generateRespondentTypeInfo(String type) {
        if (INDIVIDUAL.equals(type)) {
            caseData.setRespondentFirstName("First");
            caseData.setRespondentLastName("Last");
        } else {
            caseData.setRespondentOrganisationName("Org");
        }
    }
}
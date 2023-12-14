package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.et.common.model.ccd.CaseData;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.utils.CaseDataBuilder.createGenericAddress;

class Et1ReppedHelperTest {

    private CaseData caseData;

    @BeforeEach
    void setUp() {
        caseData = new CaseData();
    }

    @Test
    void setCreateDraftData() {
        Et1ReppedHelper.setCreateDraftData(caseData);
        assertEquals(NO, caseData.getEt1ReppedSectionOne());
        assertEquals(NO, caseData.getEt1ReppedSectionTwo());
        assertEquals(NO, caseData.getEt1ReppedSectionThree());
        assertNotNull(caseData.getEt1ClaimStatuses());
    }

    @Test
    void setEt1Statuses() {

    }

    @ParameterizedTest
    @MethodSource
    void setEt1SectionStatuses(String eventId, String sectionOne, String sectionTwo, String sectionThree) {
        caseData.setClaimantFirstName("First");
        caseData.setClaimantLastName("Last");
        caseData.setRespondentOrganisationName("Org");
        Et1ReppedHelper.setCreateDraftData(caseData);
        Et1ReppedHelper.setEt1SectionStatuses(caseData, eventId);
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
        Et1ReppedHelper.setCreateDraftData(caseData);
        assertThrows(IllegalArgumentException.class,
                () -> Et1ReppedHelper.setEt1SectionStatuses(caseData, "invalid"));
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

    @Test
    void generateRespondentPreamble() {
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
}
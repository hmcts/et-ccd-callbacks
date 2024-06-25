package uk.gov.hmcts.ethos.replacement.docmosis.helpers.letters;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.letters.InvalidCharacterCheck.DOUBLE_SPACE_ERROR;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.letters.InvalidCharacterCheck.NEW_LINE_ERROR;

@ExtendWith(SpringExtension.class)
class InvalidCharacterCheckTest {

    private CaseDetails caseDetails1;

    @BeforeEach
    public void setUp() throws Exception {
        caseDetails1 = generateCaseDetails("caseDetailsTest1.json");
    }

    private CaseDetails generateCaseDetails(String jsonFileName) throws Exception {
        String json = new String(Files.readAllBytes(Paths.get(Objects.requireNonNull(Thread.currentThread()
            .getContextClassLoader().getResource(jsonFileName)).toURI())));
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, CaseDetails.class);
    }

    @Test
    void checkInvalidCharactersInNames() {
        CaseData casedata = caseDetails1.getCaseData();
        casedata.setClaimant("Double  Space");
        casedata.getRepresentativeClaimantType().setNameOfRepresentative("New\nLine");
        casedata.getRespondentCollection().get(0).getValue().setRespondentName("Double  Space and New\nLine");

        RepresentedTypeR representedTypeR = RepresentedTypeR.builder()
            .nameOfRepresentative("No Errors In Name").build();
        RepresentedTypeRItem representedTypeRItem = new RepresentedTypeRItem();
        representedTypeRItem.setValue(representedTypeR);
        casedata.setRepCollection(List.of(representedTypeRItem));

        List<String> errors = InvalidCharacterCheck.checkNamesForInvalidCharacters(casedata, "letter");
        assertEquals(4, errors.size());
        assertEquals(String.format(DOUBLE_SPACE_ERROR, "Claimant Double  Space",
                casedata.getEthosCaseReference(), "letter"), errors.get(0));
        assertEquals(String.format(NEW_LINE_ERROR, "Claimant Rep New\nLine",
                casedata.getEthosCaseReference(), "letter"), errors.get(1));
        assertEquals(String.format(DOUBLE_SPACE_ERROR, "Respondent Double  Space and New\nLine",
                casedata.getEthosCaseReference(), "letter"), errors.get(2));
        assertEquals(String.format(NEW_LINE_ERROR, "Respondent Double  Space and New\nLine",
                casedata.getEthosCaseReference(), "letter"), errors.get(3));
    }

    @Test
    void checkInvalidCharactersInNamesNoClaimantResp() {
        CaseData casedata = caseDetails1.getCaseData();
        casedata.setClaimant("Single Space");
        casedata.setClaimantRepresentedQuestion("No");
        casedata.getRepresentativeClaimantType().setNameOfRepresentative("New\nLine");
        casedata.setRespondentCollection(null);
        casedata.setRepCollection(null);
        List<String> errors = InvalidCharacterCheck.checkNamesForInvalidCharacters(casedata, "letter");
        assertEquals(0, errors.size());
    }

    @ParameterizedTest
    @MethodSource("provideStringsForSanitizePartyName")
    void sanitizePartyName(String input, String expected) {
        assertEquals(expected, InvalidCharacterCheck.sanitizePartyName(input));
    }

    private static Stream<Arguments> provideStringsForSanitizePartyName() {
        return Stream.of(
            Arguments.of("John Doe", "John Doe"),
            Arguments.of("John O'Doe", "John O Doe"),
            Arguments.of("John Doe/Jr", "John Doe Jr"),
            Arguments.of("", ""),
            Arguments.of(null, "")
        );
    }
}

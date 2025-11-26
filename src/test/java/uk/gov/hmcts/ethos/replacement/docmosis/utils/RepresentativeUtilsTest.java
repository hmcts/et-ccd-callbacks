package uk.gov.hmcts.ethos.replacement.docmosis.utils;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericServiceException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class RepresentativeUtilsTest {

    private static final String DUMMY_CASE_REFERENCE = "1234567890123456";
    private static final String DUMMY_REPRESENTATIVE_ID = "dummy12_representative34_id56";
    private static final String EXCEPTION_REPRESENTATIVE_NOT_FOUND =
            "Representative not found for case ID 1234567890123456.";
    private static final String EXCEPTION_REPRESENTATIVE_ID_NOT_FOUND =
            "Representative ID not found for case ID 1234567890123456.";
    private static final String EXCEPTION_REPRESENTATIVE_DETAILS_NOT_EXISTS =
            "Representative details not found for representative ID dummy12_representative34_id56 "
                    + "in case 1234567890123456.";

    @Test
    void theValidateRespondentRepresentative() {
        // when representative is null
        GenericServiceException gse = assertThrows(GenericServiceException.class,
                () -> RepresentativeUtils.validateRespondentRepresentative(null, DUMMY_CASE_REFERENCE));
        assertThat(gse.getMessage()).isEqualTo(EXCEPTION_REPRESENTATIVE_NOT_FOUND);
        // when representative does not have id
        RepresentedTypeRItem representativeWithoutId = RepresentedTypeRItem.builder().build();
        gse = assertThrows(GenericServiceException.class,
                () -> RepresentativeUtils.validateRespondentRepresentative(representativeWithoutId,
                        DUMMY_CASE_REFERENCE));
        assertThat(gse.getMessage()).isEqualTo(EXCEPTION_REPRESENTATIVE_ID_NOT_FOUND);
        // when respondent details not found
        RepresentedTypeRItem representativeWithoutDetails = RepresentedTypeRItem.builder().build();
        representativeWithoutDetails.setId(DUMMY_REPRESENTATIVE_ID);
        gse = assertThrows(GenericServiceException.class,
                () -> RepresentativeUtils.validateRespondentRepresentative(representativeWithoutDetails,
                        DUMMY_CASE_REFERENCE));
        assertThat(gse.getMessage()).isEqualTo(EXCEPTION_REPRESENTATIVE_DETAILS_NOT_EXISTS);
    }

    @Test
    void theIsValidRespondentRepresentative() {
        // when representative is empty should return false
        assertThat(RepresentativeUtils.isValidRespondentRepresentative(null)).isFalse();
        // when representative id is empty should return false
        RepresentedTypeRItem respondentRepresentative = RepresentedTypeRItem.builder().build();
        assertThat(RepresentativeUtils.isValidRespondentRepresentative(respondentRepresentative)).isFalse();
        // when representative value is empty should return false
        respondentRepresentative.setId(DUMMY_REPRESENTATIVE_ID);
        assertThat(RepresentativeUtils.isValidRespondentRepresentative(respondentRepresentative)).isFalse();
        // when representative has both id and value should return true
        respondentRepresentative.setValue(RepresentedTypeR.builder().build());
        assertThat(RepresentativeUtils.isValidRespondentRepresentative(respondentRepresentative)).isTrue();
    }
}

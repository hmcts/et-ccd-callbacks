package uk.gov.hmcts.ethos.replacement.docmosis.utils;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
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
    void theValidateRepresentative() {
        // when representative is null
        GenericServiceException gse = assertThrows(GenericServiceException.class,
                () -> RepresentativeUtils.validateRepresentative(null, DUMMY_CASE_REFERENCE));
        assertThat(gse.getMessage()).isEqualTo(EXCEPTION_REPRESENTATIVE_NOT_FOUND);
        // when representative does not have id
        RepresentedTypeRItem representativeWithoutId = RepresentedTypeRItem.builder().build();
        gse = assertThrows(GenericServiceException.class,
                () -> RepresentativeUtils.validateRepresentative(representativeWithoutId, DUMMY_CASE_REFERENCE));
        assertThat(gse.getMessage()).isEqualTo(EXCEPTION_REPRESENTATIVE_ID_NOT_FOUND);
        // when respondent details not found
        RepresentedTypeRItem representativeWithoutDetails = RepresentedTypeRItem.builder().build();
        representativeWithoutDetails.setId(DUMMY_REPRESENTATIVE_ID);
        gse = assertThrows(GenericServiceException.class,
                () -> RepresentativeUtils.validateRepresentative(representativeWithoutDetails,
                        DUMMY_CASE_REFERENCE));
        assertThat(gse.getMessage()).isEqualTo(EXCEPTION_REPRESENTATIVE_DETAILS_NOT_EXISTS);
    }
}

package uk.gov.hmcts.ethos.replacement.docmosis.service.excel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.et.common.model.multiples.MultipleDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.MultipleUtil;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.AMEND_ACTION;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.DELETE_ACTION;

@ExtendWith(SpringExtension.class)
class SubMultipleMidEventValidationServiceTest {

    @InjectMocks
    private SubMultipleMidEventValidationService subMultipleMidEventValidationService;

    private MultipleDetails multipleDetails;
    private List<String> errors;

    @BeforeEach
    void setUp() {
        multipleDetails = new MultipleDetails();
        multipleDetails.setCaseData(MultipleUtil.getMultipleData());
        errors = new ArrayList<>();
    }

    @Test
    void subMultipleMidEventValidationServiceCreate() {

        multipleDetails.getCaseData().getSubMultipleAction().setCreateSubMultipleName("SubMultiple");

        subMultipleMidEventValidationService.subMultipleValidationLogic(
                multipleDetails,
                errors);

        assertEquals(1, errors.size());
        assertEquals("Sub Multiple SubMultiple already exists", errors.get(0));

    }

    @Test
    void subMultipleMidEventValidationServiceAmend() {

        multipleDetails.getCaseData().getSubMultipleAction().setActionType(AMEND_ACTION);
        multipleDetails.getCaseData().getSubMultipleAction().setAmendSubMultipleNameExisting("SubMultipleDoesNotExist");
        multipleDetails.getCaseData().getSubMultipleAction().setAmendSubMultipleNameNew("SubMultiple");

        subMultipleMidEventValidationService.subMultipleValidationLogic(
                multipleDetails,
                errors);

        assertEquals(2, errors.size());
        assertEquals("Sub Multiple SubMultipleDoesNotExist does not exist", errors.get(0));
        assertEquals("Sub Multiple SubMultiple already exists", errors.get(1));

    }

    @Test
    void subMultipleMidEventValidationServiceAmendEmptySubCollection() {

        multipleDetails.getCaseData().getSubMultipleAction().setActionType(AMEND_ACTION);
        multipleDetails.getCaseData().getSubMultipleAction().setAmendSubMultipleNameExisting("SubMultipleDoesNotExist");
        multipleDetails.getCaseData().getSubMultipleAction().setAmendSubMultipleNameNew("SubMultiple");
        multipleDetails.getCaseData().setSubMultipleCollection(null);

        subMultipleMidEventValidationService.subMultipleValidationLogic(
                multipleDetails,
                errors);

        assertEquals(1, errors.size());
        assertEquals("Sub Multiple SubMultipleDoesNotExist does not exist", errors.get(0));

    }

    @Test
    void subMultipleMidEventValidationServiceDelete() {

        multipleDetails.getCaseData().getSubMultipleAction().setActionType(DELETE_ACTION);
        multipleDetails.getCaseData().getSubMultipleAction().setDeleteSubMultipleName("SubMultipleDoesNotExist");

        subMultipleMidEventValidationService.subMultipleValidationLogic(
                multipleDetails,
                errors);

        assertEquals(1, errors.size());
        assertEquals("Sub Multiple SubMultipleDoesNotExist does not exist", errors.get(0));

    }

}
package uk.gov.hmcts.ethos.replacement.docmosis.service.excel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.et.common.model.multiples.MultipleDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.MultipleUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CASE_IS_NOT_IN_MULTIPLE_ERROR;

@ExtendWith(SpringExtension.class)
class MultipleAmendLeadCaseServiceTest {

    @Mock
    private MultipleHelperService multipleHelperService;
    @InjectMocks
    private MultipleAmendLeadCaseService multipleAmendLeadCaseService;

    private SortedMap<String, Object> multipleObjects;
    private MultipleDetails multipleDetails;
    private String userToken;

    @BeforeEach
    public void setUp() {
        multipleObjects = MultipleUtil.getMultipleObjectsAll();
        multipleDetails = new MultipleDetails();
        multipleDetails.setCaseData(MultipleUtil.getMultipleData());
        userToken = "authString";
    }

    @Test
    void bulkAmendLeadCaseLogicDoesNotExist() {
        List<String> errors = new ArrayList<>();
        multipleDetails.getCaseData().setAmendLeadCase("245020/2020");
        multipleAmendLeadCaseService.bulkAmendLeadCaseLogic(userToken,
                multipleDetails,
                errors,
                multipleObjects);
        assertEquals(1, errors.size());
        assertEquals(CASE_IS_NOT_IN_MULTIPLE_ERROR, errors.get(0));
    }

    @Test
    void bulkAmendLeadCaseLogicDifferentLead() {
        String amendLeadCase = "245000/2020";
        multipleDetails.getCaseData().setAmendLeadCase(amendLeadCase);
        multipleAmendLeadCaseService.bulkAmendLeadCaseLogic(userToken,
                multipleDetails,
                new ArrayList<>(),
                multipleObjects);
        verify(multipleHelperService, times(1)).sendUpdatesToSinglesLogic(
                userToken,
                multipleDetails,
                new ArrayList<>(),
                amendLeadCase,
                multipleObjects,
                new ArrayList<>(Collections.singletonList(amendLeadCase)));
        verifyNoMoreInteractions(multipleHelperService);
    }

}
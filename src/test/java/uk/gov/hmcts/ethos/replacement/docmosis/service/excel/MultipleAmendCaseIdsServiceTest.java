package uk.gov.hmcts.ethos.replacement.docmosis.service.excel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.et.common.model.multiples.MultipleDetails;
import uk.gov.hmcts.et.common.model.multiples.MultipleObject;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.MultipleUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
class MultipleAmendCaseIdsServiceTest {

    @Mock
    private MultipleHelperService multipleHelperService;
    @InjectMocks
    private MultipleAmendCaseIdsService multipleAmendCaseIdsService;

    private TreeMap<String, Object> multipleObjects;
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
    void bulkAmendCaseIdsLogic() {
        List<MultipleObject> multipleObjectList = multipleAmendCaseIdsService.bulkAmendCaseIdsLogic(userToken,
                multipleDetails,
                new ArrayList<>(),
                multipleObjects);

        assertEquals(multipleObjectList, getMultipleObjectsList());
    }

    @Test
    void bulkAmendCaseIdsLogicEmptyLead() {
        multipleDetails.getCaseData().setLeadCase(null);
        List<MultipleObject>  multipleObjectList = multipleAmendCaseIdsService.bulkAmendCaseIdsLogic(userToken,
                multipleDetails,
                new ArrayList<>(),
                multipleObjects);

        assertEquals(multipleObjectList, getMultipleObjectsList());
    }

    private List<MultipleObject> getMultipleObjectsList() {
        return new ArrayList<>(Arrays.asList(
                MultipleObject.builder()
                        .subMultiple("245000")
                        .ethosCaseRef("245000/2020")
                        .flag1("AA")
                        .flag2("BB")
                        .flag3("")
                        .flag4("")
                        .build(),
                MultipleObject.builder()
                        .subMultiple("")
                        .ethosCaseRef("245001/2020")
                        .flag1("")
                        .flag2("")
                        .flag3("")
                        .flag4("")
                        .build(),
                MultipleObject.builder()
                        .subMultiple("245003")
                        .ethosCaseRef("245003/2020")
                        .flag1("AA")
                        .flag2("EE")
                        .flag3("")
                        .flag4("")
                        .build(),
                MultipleObject.builder()
                        .subMultiple("245002")
                        .ethosCaseRef("245004/2020")
                        .flag1("AA")
                        .flag2("BB")
                        .flag3("")
                        .flag4("")
                        .build(),
                MultipleObject.builder()
                        .subMultiple("SubMultiple")
                        .ethosCaseRef("245005/2020")
                        .flag1("AA")
                        .flag2("BB")
                        .flag3("")
                        .flag4("")
                        .build()));
    }

}
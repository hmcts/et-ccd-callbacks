package uk.gov.hmcts.ethos.replacement.docmosis.service.excel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.et.common.model.ccd.items.ListTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.types.DynamicListType;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;
import uk.gov.hmcts.et.common.model.multiples.MultipleDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.MultipleUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.OPEN_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SELECT_NONE_VALUE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@ExtendWith(SpringExtension.class)
class MultipleBatchUpdate3ServiceTest {

    @Mock
    private SingleCasesReadingService singleCasesReadingService;
    @Mock
    private MultipleHelperService multipleHelperService;
    @Mock
    private CcdClient ccdClient;

    @InjectMocks
    private MultipleBatchUpdate3Service multipleBatchUpdate3Service;

    private SortedMap<String, Object> multipleObjectsFlags;
    private MultipleDetails multipleDetails;
    private List<SubmitEvent> submitEvents;
    private String userToken;

    @BeforeEach
    public void setUp() {
        multipleObjectsFlags = MultipleUtil.getMultipleObjectsFlags();
        multipleDetails = new MultipleDetails();
        multipleDetails.setCaseData(MultipleUtil.getMultipleData());
        submitEvents = MultipleUtil.getSubmitEvents();
        userToken = "authString";
    }

    @Test
    void batchUpdate3Logic() {

        multipleDetails.getCaseData().setBatchUpdateClaimantRep(MultipleUtil.generateDynamicList(SELECT_NONE_VALUE));
        multipleDetails.getCaseData().setBatchUpdateJurisdiction(MultipleUtil.generateDynamicList("AA"));
        multipleDetails.getCaseData().setBatchUpdateRespondent(MultipleUtil.generateDynamicList("Andrew Smith"));
        multipleDetails.getCaseData().setBatchUpdateJudgment(MultipleUtil.generateDynamicList("JD"));
        multipleDetails.getCaseData().setBatchUpdateRespondentRep(MultipleUtil
                .generateDynamicList("Respondent Rep"));

        multipleDetails.getCaseData().setBatchUpdateCase("245000/2020");

        assertEquals(3, multipleObjectsFlags.size());

        when(singleCasesReadingService.retrieveSingleCase(userToken,
                multipleDetails.getCaseTypeId(),
                multipleDetails.getCaseData().getBatchUpdateCase(),
                multipleDetails.getCaseData().getMultipleSource()))
                .thenReturn(submitEvents.get(0));

        multipleBatchUpdate3Service.batchUpdate3Logic(userToken,
                multipleDetails,
                new ArrayList<>(),
                multipleObjectsFlags);

        assertEquals(2, multipleObjectsFlags.size());

        verify(multipleHelperService, times(1))
                .sendUpdatesToSinglesWithConfirmation(userToken, multipleDetails, new ArrayList<>(),
                        multipleObjectsFlags, submitEvents.get(0).getCaseData());
        verifyNoMoreInteractions(multipleHelperService);

    }

    @Test
    void batchUpdate3LogicClaimantRepRemoval() throws IOException {

        multipleDetails.getCaseData().setBatchUpdateClaimantRep(MultipleUtil.generateDynamicList(SELECT_NONE_VALUE));
        multipleDetails.getCaseData().setBatchUpdateJurisdiction(MultipleUtil.generateDynamicList("AA"));
        multipleDetails.getCaseData().setBatchUpdateRespondent(MultipleUtil.generateDynamicList("Andrew Smith"));
        multipleDetails.getCaseData().setBatchUpdateJudgment(MultipleUtil.generateDynamicList("JD"));
        multipleDetails.getCaseData().setBatchUpdateRespondentRep(MultipleUtil
                .generateDynamicList("Respondent Rep"));
        multipleDetails.getCaseData().setBatchRemoveClaimantRep(YES);
        multipleDetails.getCaseData().setBatchUpdateCase("245000/2020");
        RepresentedTypeC representedTypeC = new RepresentedTypeC();
        representedTypeC.setNameOfRepresentative("ABC");
        representedTypeC.setNameOfOrganisation("FDS");
        submitEvents.get(0).getCaseData().setRepresentativeClaimantType(representedTypeC);
        assertEquals(3, multipleObjectsFlags.size());

        when(singleCasesReadingService.retrieveSingleCase(userToken,
                multipleDetails.getCaseTypeId(),
                multipleDetails.getCaseData().getBatchUpdateCase(),
                multipleDetails.getCaseData().getMultipleSource()))
                .thenReturn(submitEvents.get(0));
        CCDRequest returnedRequest = new CCDRequest();
        when(ccdClient.startEventForCase(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(returnedRequest);
        multipleBatchUpdate3Service.batchUpdate3Logic(userToken,
                multipleDetails,
                new ArrayList<>(),
                multipleObjectsFlags);

        assertEquals(2, multipleObjectsFlags.size());
        assertEquals(submitEvents.get(0).getCaseData().getRepresentativeClaimantType(), new RepresentedTypeC());
        verify(multipleHelperService, times(1))
                .sendUpdatesToSinglesWithConfirmation(userToken, multipleDetails, new ArrayList<>(),
                        multipleObjectsFlags, submitEvents.get(0).getCaseData());
        verifyNoMoreInteractions(multipleHelperService);

    }

    @Test
    void batchUpdate3LogicRespondentRepRemoval() throws IOException {

        multipleDetails.getCaseData().setBatchUpdateClaimantRep(MultipleUtil.generateDynamicList(SELECT_NONE_VALUE));
        multipleDetails.getCaseData().setBatchUpdateJurisdiction(MultipleUtil.generateDynamicList("AA"));
        multipleDetails.getCaseData().setBatchUpdateRespondent(MultipleUtil.generateDynamicList("Andrew Smith"));
        multipleDetails.getCaseData().setBatchUpdateJudgment(MultipleUtil.generateDynamicList("JD"));
        multipleDetails.getCaseData().setBatchUpdateRespondentRep(MultipleUtil
                .generateDynamicList("Respondent Rep"));
        multipleDetails.getCaseData().setBatchRemoveRespondentRep(YES);
        multipleDetails.getCaseData().setBatchUpdateCase("245000/2020");
        RepresentedTypeR representedTypeR = RepresentedTypeR.builder()
            .respRepName("Andrew Smith")
            .nameOfRepresentative("Respondent Rep").build();
        RepresentedTypeRItem representedTypeRItem = new RepresentedTypeRItem();
        representedTypeRItem.setId("Respondent Rep");
        representedTypeRItem.setValue(representedTypeR);
        submitEvents.get(0).getCaseData().setRepCollection(
                new ArrayList<>(Collections.singletonList(representedTypeRItem)));
        assertEquals(3, multipleObjectsFlags.size());

        when(singleCasesReadingService.retrieveSingleCase(userToken,
                multipleDetails.getCaseTypeId(),
                multipleDetails.getCaseData().getBatchUpdateCase(),
                multipleDetails.getCaseData().getMultipleSource()))
                .thenReturn(submitEvents.get(0));
        CCDRequest returnedRequest = new CCDRequest();
        when(ccdClient.startEventForCase(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(returnedRequest);
        multipleBatchUpdate3Service.batchUpdate3Logic(userToken,
                multipleDetails,
                new ArrayList<>(),
                multipleObjectsFlags);

        assertEquals(2, multipleObjectsFlags.size());
        assertNull(submitEvents.get(0).getCaseData().getRepCollection().get(0).getValue());
    }

    @Test
    void batchUpdate3LogicNoChanges() {
        DynamicListType dynamicListType = new DynamicListType();
        dynamicListType.setDynamicList(MultipleUtil.generateDynamicList(SELECT_NONE_VALUE));
        multipleDetails.getCaseData().setBatchUpdateClaimantRep(MultipleUtil.generateDynamicList(SELECT_NONE_VALUE));
        multipleDetails.getCaseData().setBatchUpdateJurisdictionList(ListTypeItem.from(dynamicListType));
        multipleDetails.getCaseData().setBatchUpdateRespondent(MultipleUtil.generateDynamicList(SELECT_NONE_VALUE));
        multipleDetails.getCaseData().setBatchUpdateJudgment(MultipleUtil.generateDynamicList(SELECT_NONE_VALUE));
        multipleDetails.getCaseData().setBatchUpdateRespondentRep(MultipleUtil.generateDynamicList(SELECT_NONE_VALUE));

        multipleDetails.getCaseData().setBatchUpdateCase("245000/2020");

        when(singleCasesReadingService.retrieveSingleCase(userToken,
                multipleDetails.getCaseTypeId(),
                multipleDetails.getCaseData().getBatchUpdateCase(),
                multipleDetails.getCaseData().getMultipleSource()))
                .thenReturn(submitEvents.get(0));

        multipleBatchUpdate3Service.batchUpdate3Logic(userToken,
                multipleDetails,
                new ArrayList<>(),
                multipleObjectsFlags);

        verifyNoMoreInteractions(multipleHelperService);

        assertEquals(OPEN_STATE, multipleDetails.getCaseData().getState());

    }

    @Test
    void batchUpdate3LogicJurisdictionNull() {
        DynamicListType dynamicListType = new DynamicListType();
        dynamicListType.setDynamicList(MultipleUtil.generateDynamicList(SELECT_NONE_VALUE));
        multipleDetails.getCaseData().setBatchUpdateClaimantRep(MultipleUtil.generateDynamicList(SELECT_NONE_VALUE));
        multipleDetails.getCaseData().setBatchUpdateJurisdictionList(null);
        multipleDetails.getCaseData().setBatchUpdateRespondent(MultipleUtil.generateDynamicList(SELECT_NONE_VALUE));
        multipleDetails.getCaseData().setBatchUpdateJudgment(MultipleUtil.generateDynamicList(SELECT_NONE_VALUE));
        multipleDetails.getCaseData().setBatchUpdateRespondentRep(MultipleUtil.generateDynamicList(SELECT_NONE_VALUE));

        multipleDetails.getCaseData().setBatchUpdateCase("245000/2020");

        when(singleCasesReadingService.retrieveSingleCase(userToken,
                multipleDetails.getCaseTypeId(),
                multipleDetails.getCaseData().getBatchUpdateCase(),
                multipleDetails.getCaseData().getMultipleSource()))
                .thenReturn(submitEvents.get(0));

        multipleBatchUpdate3Service.batchUpdate3Logic(userToken,
                multipleDetails,
                new ArrayList<>(),
                multipleObjectsFlags);

        verifyNoMoreInteractions(multipleHelperService);

        assertEquals(OPEN_STATE, multipleDetails.getCaseData().getState());
    }

    @Test
    void batchUpdate3LogicJurisdictions() {
        DynamicListType noneList = new DynamicListType();
        noneList.setDynamicList(MultipleUtil.generateDynamicList(SELECT_NONE_VALUE));
        DynamicListType dagList = new DynamicListType();
        dagList.setDynamicList(MultipleUtil.generateDynamicList("DAG"));

        multipleDetails.getCaseData().setBatchUpdateClaimantRep(MultipleUtil.generateDynamicList(SELECT_NONE_VALUE));
        multipleDetails.getCaseData().setBatchUpdateJurisdictionList(ListTypeItem.from(noneList, dagList));
        multipleDetails.getCaseData().setBatchUpdateRespondent(MultipleUtil.generateDynamicList(SELECT_NONE_VALUE));
        multipleDetails.getCaseData().setBatchUpdateJudgment(MultipleUtil.generateDynamicList(SELECT_NONE_VALUE));
        multipleDetails.getCaseData().setBatchUpdateRespondentRep(MultipleUtil.generateDynamicList(SELECT_NONE_VALUE));

        multipleDetails.getCaseData().setBatchUpdateCase("245000/2020");

        when(singleCasesReadingService.retrieveSingleCase(userToken,
                multipleDetails.getCaseTypeId(),
                multipleDetails.getCaseData().getBatchUpdateCase(),
                multipleDetails.getCaseData().getMultipleSource()))
                .thenReturn(submitEvents.get(0));

        multipleBatchUpdate3Service.batchUpdate3Logic(userToken,
                multipleDetails,
                new ArrayList<>(),
                multipleObjectsFlags);

        assertEquals(OPEN_STATE, multipleDetails.getCaseData().getState());
    }
}

package uk.gov.hmcts.ethos.replacement.docmosis.service.excel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.et.common.model.ccd.items.JurCodesTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.JurCodesType;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC;
import uk.gov.hmcts.et.common.model.multiples.MultipleDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.MultipleUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SELECT_NONE_VALUE;

@ExtendWith(SpringExtension.class)
class MultipleSingleMidEventValidationServiceTest {

    @Mock
    private SingleCasesReadingService singleCasesReadingService;
    @Mock
    private MultipleHelperService multipleHelperService;

    @InjectMocks
    private MultipleSingleMidEventValidationService multipleSingleMidEventValidationService;

    private MultipleDetails multipleDetails;
    private List<String> errors;
    private String userToken;
    private List<SubmitEvent> submitEventList;
    private List<String> caseIdCollection;

    @BeforeEach
    public void setUp() {
        multipleDetails = new MultipleDetails();
        multipleDetails.setCaseData(MultipleUtil.getMultipleData());
        submitEventList = MultipleUtil.getSubmitEvents();
        errors = new ArrayList<>();
        userToken = "authString";
        caseIdCollection = new ArrayList<>(Arrays.asList("21006/2020", "245000/2020", "245001/2020"));
    }

    @Test
    void multipleSingleValidationLogic() {

        multipleDetails.getCaseData().setBatchUpdateCase("245000/2020");

        when(singleCasesReadingService.retrieveSingleCase(userToken,
                multipleDetails.getCaseTypeId(),
                multipleDetails.getCaseData().getBatchUpdateCase(),
                multipleDetails.getCaseData().getMultipleSource()))
                .thenReturn(submitEventList.get(0));

        when(multipleHelperService.getEthosCaseRefCollection(userToken,
                multipleDetails.getCaseData(),
                errors))
                .thenReturn(caseIdCollection);

        multipleSingleMidEventValidationService.multipleSingleValidationLogic(
                userToken,
                multipleDetails,
                errors);

        assertEquals(0, errors.size());
        assertEquals(SELECT_NONE_VALUE, multipleDetails.getCaseData().getBatchUpdateClaimantRep().getValue().getCode());
        assertEquals(1, multipleDetails.getCaseData().getBatchUpdateJurisdictionList().get(0).getValue()
                .getDynamicList().getListItems().size());
        assertEquals(2, multipleDetails.getCaseData().getBatchUpdateRespondent().getListItems().size());
        assertEquals(SELECT_NONE_VALUE, multipleDetails
                .getCaseData().getBatchUpdateRespondentRep().getValue().getLabel());

    }

    @Test
    void multipleSingleValidationLogicDoesNotExist() {

        multipleDetails.getCaseData().setBatchUpdateCase("245010/2020");

        when(multipleHelperService.getEthosCaseRefCollection(userToken,
                multipleDetails.getCaseData(),
                errors))
                .thenReturn(caseIdCollection);

        multipleSingleMidEventValidationService.multipleSingleValidationLogic(
                userToken,
                multipleDetails,
                errors);

        assertEquals(1, errors.size());
        assertEquals("Multiple does not have the case: 245010/2020", errors.get(0));

    }

    @Test
    void multipleSingleValidationLogicEmptyCaseIdCollection() {

        multipleDetails.getCaseData().setCaseIdCollection(null);
        multipleDetails.getCaseData().setBatchUpdateCase("245000/2020");

        multipleSingleMidEventValidationService.multipleSingleValidationLogic(
                userToken,
                multipleDetails,
                errors);

        assertEquals(1, errors.size());
        assertEquals("Multiple does not have cases", errors.get(0));

    }

    @Test
    void multipleSingleValidationLogicEmptyCaseSearch() {

        multipleDetails.getCaseData().setBatchUpdateCase(null);

        multipleSingleMidEventValidationService.multipleSingleValidationLogic(
                userToken,
                multipleDetails,
                errors);

        assertEquals(0, errors.size());

    }

    @Test
    void multipleSingleValidationLogicWithDynamicLists() {

        multipleDetails.getCaseData().setBatchUpdateCase("245000/2020");

        RepresentedTypeC representedTypeC = new RepresentedTypeC();
        representedTypeC.setNameOfRepresentative("Rep");
        submitEventList.get(0).getCaseData().setRepresentativeClaimantType(representedTypeC);

        JurCodesTypeItem jurCodesTypeItem = new JurCodesTypeItem();
        JurCodesType jurCodesType = new JurCodesType();
        jurCodesType.setJuridictionCodesList("AA");
        jurCodesTypeItem.setValue(jurCodesType);
        submitEventList.get(0).getCaseData().setJurCodesCollection(
                new ArrayList<>(Collections.singletonList(jurCodesTypeItem)));

        when(singleCasesReadingService.retrieveSingleCase(userToken,
                multipleDetails.getCaseTypeId(),
                multipleDetails.getCaseData().getBatchUpdateCase(),
                multipleDetails.getCaseData().getMultipleSource()))
                .thenReturn(submitEventList.get(0));

        when(multipleHelperService.getEthosCaseRefCollection(userToken,
                multipleDetails.getCaseData(),
                errors))
                .thenReturn(caseIdCollection);

        multipleSingleMidEventValidationService.multipleSingleValidationLogic(
                userToken,
                multipleDetails,
                errors);

        assertEquals(0, errors.size());
        assertEquals(2, multipleDetails.getCaseData().getBatchUpdateClaimantRep().getListItems().size());
        assertEquals(2, multipleDetails.getCaseData().getBatchUpdateJurisdictionList().get(0).getValue()
                .getDynamicList().getListItems().size());
        assertEquals(2, multipleDetails.getCaseData().getBatchUpdateRespondent().getListItems().size());
        assertEquals(2, multipleDetails.getCaseData().getBatchUpdateRespondentRep().getListItems().size());

    }

    /**
     * This test is for the scenario where CCD returns the case data with a
     * representativeClaimantType as a RepresentedTypeC object with no values set.
     */
    @Test
    void shouldHandleRepresentativeClaimantWithNoValues() {
        multipleDetails.getCaseData().setBatchUpdateCase("245000/2020");

        RepresentedTypeC representedTypeC = new RepresentedTypeC();
        submitEventList.get(0).getCaseData().setRepresentativeClaimantType(representedTypeC);

        JurCodesTypeItem jurCodesTypeItem = new JurCodesTypeItem();
        JurCodesType jurCodesType = new JurCodesType();
        jurCodesType.setJuridictionCodesList("AA");
        jurCodesTypeItem.setValue(jurCodesType);
        submitEventList.get(0).getCaseData().setJurCodesCollection(
                new ArrayList<>(Collections.singletonList(jurCodesTypeItem)));

        when(singleCasesReadingService.retrieveSingleCase(userToken,
                multipleDetails.getCaseTypeId(),
                multipleDetails.getCaseData().getBatchUpdateCase(),
                multipleDetails.getCaseData().getMultipleSource()))
                .thenReturn(submitEventList.get(0));

        when(multipleHelperService.getEthosCaseRefCollection(userToken,
                multipleDetails.getCaseData(),
                errors))
                .thenReturn(caseIdCollection);

        multipleSingleMidEventValidationService.multipleSingleValidationLogic(
                userToken,
                multipleDetails,
                errors);

        assertEquals(0, errors.size());
        assertEquals(1, multipleDetails.getCaseData().getBatchUpdateClaimantRep().getListItems().size());
        assertEquals(2, multipleDetails.getCaseData().getBatchUpdateJurisdictionList().get(0).getValue()
                .getDynamicList().getListItems().size());
        assertEquals(2, multipleDetails.getCaseData().getBatchUpdateRespondent().getListItems().size());
        assertEquals(2, multipleDetails.getCaseData().getBatchUpdateRespondentRep().getListItems().size());
    }

}
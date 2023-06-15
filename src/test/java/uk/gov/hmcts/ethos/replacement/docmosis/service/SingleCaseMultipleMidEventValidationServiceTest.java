package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.MultipleUtil;
import uk.gov.hmcts.ethos.replacement.docmosis.service.excel.MultipleHelperService;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ACCEPTED_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SINGLE_CASE_TYPE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@ExtendWith(SpringExtension.class)
class SingleCaseMultipleMidEventValidationServiceTest {

    @Mock
    private MultipleHelperService multipleHelperService;
    @InjectMocks
    private SingleCaseMultipleMidEventValidationService singleCaseMultipleMidEventValidationService;

    private CaseDetails caseDetails;
    private String userToken;
    private String multipleCaseTypeId;

    @BeforeEach
    public void setUp() {
        caseDetails = new CaseDetails();
        caseDetails.setState(ACCEPTED_STATE);
        caseDetails.setCaseTypeId(ENGLANDWALES_CASE_TYPE_ID);
        multipleCaseTypeId = UtilHelper.getBulkCaseTypeId(caseDetails.getCaseTypeId());
        caseDetails.setCaseData(MultipleUtil.getCaseDataForSinglesToBeMoved());
        userToken = "authString";
    }

    @Test
    void singleCaseMultipleValidationLogic() {

        List<String> errors = new ArrayList<>();

        singleCaseMultipleMidEventValidationService.singleCaseMultipleValidationLogic(userToken,
                caseDetails,
                errors);

        verify(multipleHelperService, times(1)).validateExternalMultipleAndSubMultiple(
                userToken,
                multipleCaseTypeId,
                caseDetails.getCaseData().getMultipleReference(),
                caseDetails.getCaseData().getSubMultipleName(),
                errors);
        verifyNoMoreInteractions(multipleHelperService);

    }

    @Test
    void singleCaseMultipleValidationLogicMultipleToSingleError() {

        List<String> errors = new ArrayList<>();

        caseDetails.getCaseData().setEcmCaseType(SINGLE_CASE_TYPE);
        caseDetails.getCaseData().setMultipleFlag(YES);

        singleCaseMultipleMidEventValidationService.singleCaseMultipleValidationLogic(userToken,
                caseDetails,
                errors);

        assertEquals(1, errors.size());
        assertEquals("Case belongs to a multiple. It cannot be moved to single", errors.get(0));

    }

    @Test
    void singleCaseMultipleValidationLogicSingleToSingle() {

        List<String> errors = new ArrayList<>();

        caseDetails.getCaseData().setEcmCaseType(SINGLE_CASE_TYPE);
        caseDetails.getCaseData().setMultipleFlag(NO);

        singleCaseMultipleMidEventValidationService.singleCaseMultipleValidationLogic(userToken,
                caseDetails,
                errors);

        assertEquals(0, errors.size());

    }

}
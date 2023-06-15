package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.multiples.SubmitMultipleEvent;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.MultipleUtil;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.MultiplesHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.excel.MultipleCasesReadingService;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_BULK_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SINGLE_CASE_TYPE;

@ExtendWith(SpringExtension.class)
class FixCaseApiServiceTest {

    @Mock
    private MultipleCasesReadingService multipleCasesReadingService;
    @InjectMocks
    private FixCaseApiService fixCaseApiService;

    private CaseDetails caseDetails;
    private String userToken;
    private String urlLinkMarkUp;
    private long ccdReference;

    @BeforeEach
    public void setUp() {
        userToken = "authToken";
        ccdReference = 1_643_639_063_185_009L;
        String multipleReference = "246001";

        caseDetails = new CaseDetails();
        caseDetails.setCaseData(MultipleUtil.getCaseDataForSinglesToBeMoved());
        caseDetails.getCaseData().setMultipleReference(multipleReference);
        caseDetails.setCaseTypeId(ENGLANDWALES_CASE_TYPE_ID);
        caseDetails.setCaseId(String.valueOf(ccdReference));

        List<SubmitMultipleEvent> submitMultipleEvents = MultipleUtil.getSubmitMultipleEvents();
        submitMultipleEvents.get(0).getCaseData().setMultipleReference(multipleReference);
        submitMultipleEvents.get(0).setCaseId(ccdReference);

        when(multipleCasesReadingService.retrieveMultipleCases(userToken,
                ENGLANDWALES_BULK_CASE_TYPE_ID,
                caseDetails.getCaseData().getMultipleReference())
        ).thenReturn(submitMultipleEvents);

        urlLinkMarkUp = MultiplesHelper.generateMarkUp(null,
                String.valueOf(ccdReference), caseDetails.getCaseData().getMultipleReference());
    }

    @Test
    void checkUpdateMultipleReference_LinkMarkUp_Normal() {
        caseDetails.getCaseData().setMultipleReferenceLinkMarkUp(urlLinkMarkUp);
        fixCaseApiService.checkUpdateMultipleReference(caseDetails, userToken);
        assertEquals(urlLinkMarkUp, caseDetails.getCaseData().getMultipleReferenceLinkMarkUp());
    }

    @Test
    void checkUpdateMultipleReference_LinkMarkUp_Null() {
        caseDetails.getCaseData().setMultipleReferenceLinkMarkUp(null);
        fixCaseApiService.checkUpdateMultipleReference(caseDetails, userToken);
        assertEquals(urlLinkMarkUp, caseDetails.getCaseData().getMultipleReferenceLinkMarkUp());
    }

    @Test
    void checkUpdateMultipleReference_LinkMarkUp_CcdReference() {
        caseDetails.getCaseData().setMultipleReferenceLinkMarkUp(String.valueOf(ccdReference));
        fixCaseApiService.checkUpdateMultipleReference(caseDetails, userToken);
        assertEquals(urlLinkMarkUp, caseDetails.getCaseData().getMultipleReferenceLinkMarkUp());
    }

    @Test
    void checkUpdateMultipleReference_EcmCaseType_getCaseData() {
        CaseData caseData = MultipleUtil.getCaseData("245000/2021");
        caseDetails.setCaseData(caseData);
        fixCaseApiService.checkUpdateMultipleReference(caseDetails, userToken);
        assertNull(caseDetails.getCaseData().getMultipleReferenceLinkMarkUp());
    }

    @Test
    void checkUpdateMultipleReference_EcmCaseType_Null() {
        caseDetails.getCaseData().setEcmCaseType(null);
        fixCaseApiService.checkUpdateMultipleReference(caseDetails, userToken);
        assertNull(caseDetails.getCaseData().getMultipleReferenceLinkMarkUp());
    }

    @Test
    void checkUpdateMultipleReference_EcmCaseType_Single() {
        caseDetails.getCaseData().setEcmCaseType(SINGLE_CASE_TYPE);
        fixCaseApiService.checkUpdateMultipleReference(caseDetails, userToken);
        assertNull(caseDetails.getCaseData().getMultipleReferenceLinkMarkUp());
    }

    @Test
    void checkUpdateMultipleReference_submitMultipleEvents_Null() {
        List<SubmitMultipleEvent> newSubmitMultipleEvents = new ArrayList<>();
        when(multipleCasesReadingService.retrieveMultipleCases(userToken,
                ENGLANDWALES_BULK_CASE_TYPE_ID,
                caseDetails.getCaseData().getMultipleReference())
        ).thenReturn(newSubmitMultipleEvents);
        fixCaseApiService.checkUpdateMultipleReference(caseDetails, userToken);
        assertNull(caseDetails.getCaseData().getMultipleReferenceLinkMarkUp());
    }

}
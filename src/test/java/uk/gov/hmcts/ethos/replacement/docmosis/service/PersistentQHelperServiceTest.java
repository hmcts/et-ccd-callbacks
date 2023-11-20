package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.HelperTest;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.MultipleUtil;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.MultiplesHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.servicebus.CreateUpdatesBusSender;

import java.util.ArrayList;
import java.util.Collections;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ACCEPTED_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SINGLE_CASE_TYPE;

@ExtendWith(SpringExtension.class)
class PersistentQHelperServiceTest {

    @InjectMocks
    private PersistentQHelperService persistentQHelperService;
    @Mock
    private CreateUpdatesBusSender createUpdatesBusSender;
    @Mock
    private UserIdamService userIdamService;

    private CCDRequest ccdRequest;
    private String userToken;

    @BeforeEach
    public void setUp() {
        ccdRequest = new CCDRequest();
        CaseData caseData = MultipleUtil.getCaseData("2123456/2020");
        caseData.setCaseRefNumberCount("2");
        caseData.setPositionTypeCT("PositionTypeCT");
        DynamicFixedListType officeCT = new DynamicFixedListType();
        DynamicValueType valueType = new DynamicValueType();
        valueType.setCode(ENGLANDWALES_CASE_TYPE_ID);
        officeCT.setValue(valueType);
        caseData.setOfficeCT(officeCT);
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseData(caseData);
        caseDetails.setCaseTypeId("ENGLANDWALES");
        caseDetails.setJurisdiction("Employment");
        caseDetails.setState(ACCEPTED_STATE);
        ccdRequest.setCaseDetails(caseDetails);
        userToken = "authToken";
    }

    @Test
    void sendCreationEventToSinglesWithoutConfirmation() {

        when(userIdamService.getUserDetails("authToken")).thenReturn(HelperTest.getUserDetails());
        persistentQHelperService.sendCreationEventToSingles(userToken,
                ccdRequest.getCaseDetails().getCaseTypeId(), ccdRequest.getCaseDetails().getJurisdiction(),
                new ArrayList<>(), new ArrayList<>(
                        Collections.singletonList("ethosCaseReference")), ENGLANDWALES_CASE_TYPE_ID,
                "positionTypeCT", "ccdGatewayBaseUrl", "",
                SINGLE_CASE_TYPE, NO,
                MultiplesHelper.generateMarkUp("ccdGatewayBaseUrl",
                        ccdRequest.getCaseDetails().getCaseId(),
                        ccdRequest.getCaseDetails().getCaseData().getMultipleRefNumber()),
                true, null
        );

        verify(userIdamService).getUserDetails(userToken);
        verifyNoMoreInteractions(userIdamService);

    }

    @Test
    void sendTransferToEcmEvent() {

        when(userIdamService.getUserDetails("authToken")).thenReturn(HelperTest.getUserDetails());
        persistentQHelperService.sendTransferToEcmEvent(userToken,
                ccdRequest.getCaseDetails().getCaseTypeId(), ccdRequest.getCaseDetails().getJurisdiction(),
                new ArrayList<>(), new ArrayList<>(
                        Collections.singletonList("ethosCaseReference")), ENGLANDWALES_CASE_TYPE_ID,
                "positionTypeCT", "ccdGatewayBaseUrl", "",
                NO, null
        );

        verify(userIdamService).getUserDetails(userToken);
        verifyNoMoreInteractions(userIdamService);

    }

}
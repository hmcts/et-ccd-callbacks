package uk.gov.hmcts.ethos.replacement.docmosis.service;

import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.exceptions.CaseCreationException;
import uk.gov.hmcts.ecm.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.ecm.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.ecm.common.model.ccd.CaseData;
import uk.gov.hmcts.ecm.common.model.ccd.CaseDetails;
import uk.gov.hmcts.ecm.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.ecm.common.model.ccd.items.BFActionTypeItem;
import uk.gov.hmcts.ecm.common.model.ccd.items.DateListedTypeItem;
import uk.gov.hmcts.ecm.common.model.ccd.items.EccCounterClaimTypeItem;
import uk.gov.hmcts.ecm.common.model.ccd.items.HearingTypeItem;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.isNullOrEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_STATUS_LISTED;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SINGLE_CASE_TYPE;

@Slf4j
@RequiredArgsConstructor
@Service("caseTransferService")
public class CaseTransferService {

    static final String BF_ACTIONS_ERROR_MSG = "There are one or more open Brought Forward actions that must be "
            + "cleared before the case %s can be transferred";

    static final String HEARINGS_ERROR_MSG = "There are one or more hearings that have the status Listed. "
            + "These must be updated before the case %s can be transferred";

    private final PersistentQHelperService persistentQHelperService;
    private final CcdClient ccdClient;

    @Value("${ccd_gateway_base_url}")
    private String ccdGatewayBaseUrl;

    public void populateCaseTransferOffices(CaseData caseData) {
        var managingOffice = caseData.getManagingOffice();
        if (StringUtils.isBlank(managingOffice)) {
            return;
        }

        var officeCT = new DynamicFixedListType();
        if (TribunalOffice.isEnglandWalesOffice(managingOffice)) {
            var tribunalOffices = TribunalOffice.ENGLANDWALES_OFFICES.stream()
                    .filter(tribunalOffice -> !tribunalOffice.getOfficeName().equals(managingOffice))
                    .map(tribunalOffice ->
                            DynamicValueType.create(tribunalOffice.getOfficeName(), tribunalOffice.getOfficeName()))
                    .collect(Collectors.toList());
            officeCT.setListItems(tribunalOffices);
        } else if (TribunalOffice.isScotlandOffice(managingOffice)) {
            officeCT.setListItems(Collections.emptyList());
        }

        caseData.setOfficeCT(officeCT);
    }

    public List<String> createCaseTransfer(CaseDetails caseDetails, String userToken) {
        var errors = new ArrayList<String>();
        var caseDataList = getAllCasesToBeTransferred(caseDetails, userToken);

        caseDataList.forEach(caseData -> validateCase(caseData, errors));

        if (!errors.isEmpty()) {
            return errors;
        }

        return transferCases(caseDetails, caseDataList, userToken);
    }

    public List<String> caseTransferSameCountry(CaseDetails caseDetails, String userToken) {
        var errors = new ArrayList<String>();
        var caseDataList = getAllCasesToBeTransferred(caseDetails, userToken);

        caseDataList.forEach(caseData -> validateCase(caseData, errors));

        if (!errors.isEmpty()) {
            return errors;
        }

        var newManagingOffice = caseDetails.getCaseData().getOfficeCT().getSelectedCode();
        caseDetails.getCaseData().setManagingOffice(newManagingOffice);
        caseDetails.getCaseData().setOfficeCT(null);

        for (var caseData : caseDataList) {
            if (caseData.getEthosCaseReference().equals(caseDetails.getCaseData().getEthosCaseReference())) {
                continue;
            }

            caseData.setManagingOffice(newManagingOffice);
        }
        return errors;
        //return transferCases(caseDetails, caseDataList, userToken);
    }

    public List<String> caseTransferDifferentCountry(CaseDetails caseDetails, String userToken) {
        throw new UnsupportedOperationException();
    }

    private List<CaseData> getAllCasesToBeTransferred(CaseDetails caseDetails, String userToken) {
        try {
            var originalCaseData = getOriginalCase(caseDetails, userToken);
            List<CaseData> cases = new ArrayList<>();
            cases.add(originalCaseData);

            var eccCases = getEccCases(originalCaseData, caseDetails.getCaseTypeId(), userToken);
            if (!eccCases.isEmpty()) {
                cases.addAll(eccCases);
            }

            return cases;
        } catch (Exception ex) {
            throw new CaseCreationException("Error getting all cases to be transferred for case number: "
                    + caseDetails.getCaseData().getEthosCaseReference() + " " + ex.getMessage());
        }
    }

    private CaseData getOriginalCase(CaseDetails caseDetails, String userToken) {
        try {
            var caseData = caseDetails.getCaseData();
            if (!Strings.isNullOrEmpty(caseData.getCounterClaim())) {
                List<SubmitEvent> submitEvents = ccdClient.retrieveCasesElasticSearch(userToken,
                        caseDetails.getCaseTypeId(), List.of(caseData.getCounterClaim()));
                return submitEvents.get(0).getCaseData();
            } else {
                return caseDetails.getCaseData();
            }

        } catch (Exception ex) {
            throw new CaseCreationException("Error getting original case number: "
                    + caseDetails.getCaseData().getEthosCaseReference() + " " + ex.getMessage());
        }
    }

    private List<CaseData> getEccCases(CaseData caseData, String caseTypeId, String userToken) throws IOException {
        var eccCases = new ArrayList<CaseData>();

        if (CollectionUtils.isNotEmpty(caseData.getEccCases())) {
            for (EccCounterClaimTypeItem counterClaimItem : caseData.getEccCases()) {
                var counterClaim = counterClaimItem.getValue().getCounterClaim();
                var submitEvents = ccdClient.retrieveCasesElasticSearch(userToken, caseTypeId,
                        new ArrayList<>(Collections.singleton(counterClaim)));
                if (submitEvents != null && !submitEvents.isEmpty()) {
                    eccCases.add(submitEvents.get(0).getCaseData());
                }
            }
        }

        return eccCases;
    }

    private boolean checkBfActionsCleared(CaseData caseData) {
        if (caseData.getBfActions() != null) {
            for (BFActionTypeItem bfActionTypeItem : caseData.getBfActions()) {
                if (isNullOrEmpty(bfActionTypeItem.getValue().getCleared())) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean checkHearingsNotListed(CaseData caseData) {
        if (caseData.getHearingCollection() == null) {
            return true;
        }
        for (HearingTypeItem hearingTypeItem : caseData.getHearingCollection()) {
            if (hearingTypeItem.getValue().getHearingDateCollection() != null) {
                for (DateListedTypeItem dateListedTypeItem : hearingTypeItem.getValue().getHearingDateCollection()) {
                    if (dateListedTypeItem.getValue().getHearingStatus() != null
                            && dateListedTypeItem.getValue().getHearingStatus().equals(HEARING_STATUS_LISTED)) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private void validateCase(CaseData caseData, List<String> errors) {
        if (!checkBfActionsCleared(caseData)) {
            errors.add(String.format(BF_ACTIONS_ERROR_MSG, caseData.getEthosCaseReference()));
        }

        if (!checkHearingsNotListed(caseData)) {
            errors.add(String.format(HEARINGS_ERROR_MSG, caseData.getEthosCaseReference()));
        }
    }

    private List<String> transferCases(CaseDetails caseDetails, List<CaseData> caseDataList, String userToken) {
        var errors = new ArrayList<String>();
        for (CaseData caseData : caseDataList) {
            createCaseTransferEvent(caseDetails, caseData, userToken, errors);
        }

        return errors;
    }

    private void createCaseTransferEvent(CaseDetails caseDetails, CaseData caseData, String userToken,
                                         List<String> errors) {
        persistentQHelperService.sendCreationEventToSingles(
                userToken,
                caseDetails.getCaseTypeId(),
                caseDetails.getJurisdiction(),
                errors,
                List.of(caseData.getEthosCaseReference()),
                caseDetails.getCaseData().getOfficeCT().getValue().getCode(),
                caseDetails.getCaseData().getPositionTypeCT(),
                ccdGatewayBaseUrl,
                caseDetails.getCaseData().getReasonForCT(),
                SINGLE_CASE_TYPE,
                NO,
                null,
                null
        );

        caseData.setLinkedCaseCT("Transferred to " + caseDetails.getCaseData().getOfficeCT().getValue().getCode());
        caseData.setPositionType(caseDetails.getCaseData().getPositionTypeCT());
        log.info("Clearing the CT payload for case: " + caseData.getEthosCaseReference());
        caseData.setOfficeCT(null);
        caseData.setPositionTypeCT(null);
        caseData.setStateAPI(null);
    }
}

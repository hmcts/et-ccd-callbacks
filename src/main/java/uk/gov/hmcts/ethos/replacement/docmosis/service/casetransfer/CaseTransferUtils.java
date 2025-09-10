package uk.gov.hmcts.ethos.replacement.docmosis.service.casetransfer;

import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.exceptions.CaseCreationException;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.et.common.model.ccd.items.BFActionTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.DateListedTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.EccCounterClaimTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.HearingTypeItem;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseManagementLocationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.exceptions.OfficeToAssignCaseToNotFoundException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.google.common.base.Strings.isNullOrEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_STATUS_LISTED;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;

@Service
@RequiredArgsConstructor
public class CaseTransferUtils {

    public static final String BF_ACTIONS_ERROR_MSG = "There are one or more open Brought Forward actions that must be "
            + "cleared before the case %s can be transferred";

    public static final String HEARINGS_ERROR_MSG = "There are one or more hearings that have the status Listed. "
            + "These must be updated before the case %s can be transferred";

    private final CcdClient ccdClient;
    private final CaseManagementLocationService caseManagementLocationService;

    public static void setCaseManagingOffice(CaseData caseData, String caseTypeId) {
        if (ENGLANDWALES_CASE_TYPE_ID.equals(caseTypeId)) {
            if (caseData.getAssignOffice() != null) {
                caseData.setManagingOffice(caseData.getAssignOffice().getSelectedCode());
            } else {
                throw new OfficeToAssignCaseToNotFoundException(
                        String.format("AssignOffice or SelectedCode not found for case type %s "
                                + "with EthosCaseReference %s", caseTypeId, caseData.getEthosCaseReference()));
            }
        } else if (SCOTLAND_CASE_TYPE_ID.equals(caseTypeId)) {
            caseData.setManagingOffice(TribunalOffice.GLASGOW.getOfficeName());
            caseData.setAllocatedOffice(TribunalOffice.GLASGOW.getOfficeName());
        }
    }

    List<CaseData> getAllCasesToBeTransferred(CaseDetails caseDetails, String userToken) {
        try {
            CaseData claimantCaseData = getClaimantCase(caseDetails, userToken);
            List<CaseData> cases = new ArrayList<>();
            cases.add(claimantCaseData);

            List<CaseData> eccCases = getEccCases(claimantCaseData, caseDetails.getCaseTypeId(), userToken);
            if (!eccCases.isEmpty()) {
                cases.addAll(eccCases);
            }
            return cases;
        } catch (Exception ex) {
            throw (CaseCreationException) new CaseCreationException(
                "Error getting all cases to be transferred for case number: "
                        + caseDetails.getCaseData().getEthosCaseReference()).initCause(ex);
        }
    }

    public List<String> validateCase(CaseData caseData) {
        List<String> errors = new ArrayList<>();
        if (!checkBfActionsCleared(caseData)) {
            errors.add(String.format(BF_ACTIONS_ERROR_MSG, caseData.getEthosCaseReference()));
        }

        if (!checkHearingsNotListed(caseData)) {
            errors.add(String.format(HEARINGS_ERROR_MSG, caseData.getEthosCaseReference()));
        }
        return errors;
    }

    private CaseData getClaimantCase(CaseDetails caseDetails, String userToken) {
        CaseData caseData = caseDetails.getCaseData();
        if (isNullOrEmpty(caseData.getCounterClaim())) {
            return caseData;
        }

        try {
            List<SubmitEvent> submitEvents = ccdClient.retrieveCasesElasticSearch(userToken,
                    caseDetails.getCaseTypeId(), List.of(caseData.getCounterClaim()));
            return submitEvents.get(0).getCaseData();

        } catch (Exception ex) {
            throw (CaseCreationException) new CaseCreationException("Error getting counter claim case for : "
                    + caseData.getEthosCaseReference()).initCause(ex);
        }
    }

    private List<CaseData> getEccCases(CaseData caseData, String caseTypeId, String userToken) throws IOException {
        List<CaseData> eccCases = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(caseData.getEccCases())) {
            for (EccCounterClaimTypeItem counterClaimItem : caseData.getEccCases()) {
                String counterClaim = counterClaimItem.getValue().getCounterClaim();
                List<SubmitEvent> submitEvents = ccdClient.retrieveCasesElasticSearch(userToken, caseTypeId,
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
}

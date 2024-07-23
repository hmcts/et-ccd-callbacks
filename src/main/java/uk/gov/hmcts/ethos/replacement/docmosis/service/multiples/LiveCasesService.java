package uk.gov.hmcts.ethos.replacement.docmosis.service.multiples;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.ethos.replacement.docmosis.service.FeatureToggleService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.excel.SingleCasesReadingService;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLOSED_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@Slf4j
@RequiredArgsConstructor
@Service("liveCasesService")
public class LiveCasesService {
    private final SingleCasesReadingService singleCasesReadingService;
    private final FeatureToggleService featureToggleService;

    /**
     * Modifies the list of cases to remove any cases in a Closed state.
     *
     * @param userToken       Authorisation
     * @param caseTypeId      EnglandWales or Scotland case type
     * @param multipleObjects List of single cases retrieved from Excel sheet
     * @param multipleData    Case data
     */
    public void filterLiveCases(String userToken,
                                String caseTypeId,
                                SortedMap<String, Object> multipleObjects,
                                MultipleData multipleData) {
        if (!featureToggleService.isMultiplesEnabled()) {
            return;
        }
        if (YES.equals(multipleData.getLiveCases())) {
            log.info("Filtering live cases");
            List<String> ethosCaseRefCollection = new ArrayList<>(multipleObjects.keySet());

            log.info("Retrieving data from single cases");
            List<SubmitEvent> submitEvents = singleCasesReadingService.retrieveSingleCases(
                    userToken,
                    caseTypeId,
                    ethosCaseRefCollection,
                    multipleData.getMultipleSource());

            List<String> closedCases = submitEvents.stream()
                    .filter(submitEvent -> CLOSED_STATE.equals(submitEvent.getState()))
                    .map(submitEvent -> submitEvent.getCaseData().getEthosCaseReference())
                    .toList();

            if (closedCases.isEmpty()) {
                log.warn("No closed cases found");
            } else {
                log.info("Removing closed cases");
                closedCases.forEach(multipleObjects::remove);
            }
        }
        multipleData.setLiveCases(null);
    }
}

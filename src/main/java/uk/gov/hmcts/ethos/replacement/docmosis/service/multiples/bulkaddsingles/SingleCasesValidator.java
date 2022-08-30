package uk.gov.hmcts.ethos.replacement.docmosis.service.multiples.bulkaddsingles;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.client.CcdClient;
import uk.gov.hmcts.et.common.helpers.UtilHelper;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.et.common.model.multiples.MultipleDetails;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Strings.isNullOrEmpty;
import static uk.gov.hmcts.et.common.model.helper.Constants.ACCEPTED_STATE;
import static uk.gov.hmcts.et.common.model.helper.Constants.MANUALLY_CREATED_POSITION;
import static uk.gov.hmcts.et.common.model.helper.Constants.SCOTLAND_BULK_CASE_TYPE_ID;

@Service
@Slf4j
class SingleCasesValidator {

    static final int ELASTICSEARCH_TERMS_SIZE = 1024;

    private final CcdClient ccdClient;

    SingleCasesValidator(CcdClient ccdClient) {
        this.ccdClient = ccdClient;
    }

    List<ValidatedSingleCase> getValidatedCases(List<String> ethosCaseReferences, MultipleDetails multipleDetails,
                                                String authToken) throws IOException {
        var partitionedCaseReferences = Lists.partition(ethosCaseReferences, ELASTICSEARCH_TERMS_SIZE);
        var isScotland = SCOTLAND_BULK_CASE_TYPE_ID.equals(multipleDetails.getCaseTypeId());
        var validatedSingleCases = new ArrayList<ValidatedSingleCase>();
        for (var caseReferences : partitionedCaseReferences) {
            var submitEvents = ccdClient.retrieveCasesElasticSearchForCreation(authToken,
                    UtilHelper.getCaseTypeId(multipleDetails.getCaseTypeId()),
                    caseReferences,
                    MANUALLY_CREATED_POSITION);
            log.info("Search returned {} results", submitEvents.size());

            for (var ethosCaseReference : caseReferences) {
                log.info(ethosCaseReference);
                var searchResult = submitEvents.stream()
                        .filter(se -> se.getCaseData().getEthosCaseReference().equals(ethosCaseReference))
                        .findFirst();

                validatedSingleCases.add(create(ethosCaseReference, searchResult, isScotland,
                        multipleDetails.getCaseData().getManagingOffice()));
            }
        }
        return validatedSingleCases;
    }

    private ValidatedSingleCase create(String ethosCaseReference, Optional<SubmitEvent> submitEventOptional,
                                       boolean isScotland, String multipleManagingOffice) {
        if (submitEventOptional.isPresent()) {
            var submitEvent = submitEventOptional.get();

            if (!isAccepted(submitEvent)) {
                return ValidatedSingleCase.createInvalidCase(ethosCaseReference,
                        "Case is in state " + submitEvent.getState());
            }

            if (!isScotland && managingOfficeCheck(submitEvent, multipleManagingOffice)) {
                return ValidatedSingleCase.createInvalidCase(ethosCaseReference,
                        "Case is managed by " + submitEvent.getCaseData().getManagingOffice());
            }
        } else {
            return ValidatedSingleCase.createInvalidCase(ethosCaseReference, "Case not found");
        }

        return ValidatedSingleCase.createValidCase(ethosCaseReference);
    }

    private boolean managingOfficeCheck(SubmitEvent submitEvent, String managingOffice) {
        return !isNullOrEmpty(submitEvent.getCaseData().getManagingOffice())
                && !managingOffice.equals(submitEvent.getCaseData().getManagingOffice());
    }

    private boolean isAccepted(SubmitEvent submitEvent) {
        return ACCEPTED_STATE.equals(submitEvent.getState());
    }
}

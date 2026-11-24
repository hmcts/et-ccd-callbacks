package uk.gov.hmcts.ethos.replacement.docmosis.service.excel;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.et.common.model.multiples.MultipleDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.MultiplesHelper;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.MANUALLY_CREATED_POSITION;

@Service
@RequiredArgsConstructor
public class MultipleAmendReviewService {

    private final MultipleCreationMidEventValidationService validationService;
    private final SingleCasesReadingService singleCasesReadingService;
    private final MultipleAmendReviewRenderer renderer;

    public void prepareReview(String userToken, MultipleDetails details, boolean removing,
                              List<String> errors, List<String> warnings) {
        MultipleData data = details.getCaseData();
        data.setMultipleAmendReview(null);
        if (removing) {
            validationService.multipleRemoveCasesValidationLogic(userToken, details, errors);
        } else {
            validationService.multipleCreationValidationLogic(userToken, details, errors, warnings, true);
        }
        if (!errors.isEmpty()) {
            return;
        }

        List<String> references = MultiplesHelper.getCaseIdsForMidEvent(
                removing ? data.getAltCaseIdCollection() : data.getCaseIdCollection());
        if (references.isEmpty()) {
            errors.add("Enter at least one case number.");
            return;
        }

        Map<String, CaseData> cases = singleCasesReadingService.retrieveSingleCases(
                userToken, details.getCaseTypeId(), references, MANUALLY_CREATED_POSITION).stream()
                .map(SubmitEvent::getCaseData)
                .collect(Collectors.toMap(CaseData::getEthosCaseReference, Function.identity(),
                        (first, second) -> first));
        List<String> missing = references.stream().filter(reference -> !cases.containsKey(reference)).toList();
        if (!missing.isEmpty()) {
            errors.add("Unable to load case details for " + missing + ". Please check the case numbers and try again.");
            return;
        }

        data.setMultipleAmendReview(renderer.render(references.stream().map(cases::get).toList(), removing));
    }
}

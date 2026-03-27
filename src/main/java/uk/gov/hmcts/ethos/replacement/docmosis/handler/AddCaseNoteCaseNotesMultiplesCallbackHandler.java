package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.CallbackRequestContext;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.et.common.model.multiples.MultipleRequest;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseNotesService;

import java.util.List;

import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.multipleResponse;

@Component
public class AddCaseNoteCaseNotesMultiplesCallbackHandler extends MultipleCallbackHandlerBase {

    private final CaseNotesService caseNotesService;

    @Autowired
    public AddCaseNoteCaseNotesMultiplesCallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        CaseNotesService caseNotesService
    ) {
        super(caseDetailsConverter);
        this.caseNotesService = caseNotesService;
    }

    @Override
    public List<String> getHandledCaseTypeIds() {
        return List.of("ET_Scotland_Multiple", "ET_EnglandWales_Multiple");
    }

    @Override
    public List<String> getHandledEventIds() {
        return List.of("addCaseNote");
    }

    @Override
    public boolean acceptsAboutToSubmit() {
        return true;
    }

    @Override
    public boolean acceptsSubmitted() {
        return false;
    }

    @Override
    Object aboutToSubmit(MultipleRequest multipleRequest) {
        String authorizationToken = CallbackRequestContext.getAuthorizationToken().orElse(null);
        MultipleData multipleData = multipleRequest.getCaseDetails().getCaseData();
        caseNotesService.addCaseNote(multipleData, authorizationToken);
        return multipleResponse(multipleData, null);
    }
}

package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.CallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.JudgmentValidationService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.text.ParseException;
import java.util.List;

import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityNoErrors;

@Slf4j
@Component
public class AddAmendJudgmentCallbackHandler extends CallbackHandlerBase {

    private static final String LOG_MESSAGE = "received notification request for case reference :    ";

    private final JudgmentValidationService judgmentValidationService;

    @Autowired
    public AddAmendJudgmentCallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        JudgmentValidationService judgmentValidationService
    ) {
        super(caseDetailsConverter);
        this.judgmentValidationService = judgmentValidationService;
    }

    @Override
    public List<String> getHandledCaseTypeIds() {
        return List.of("ET_Scotland", "ET_EnglandWales");
    }

    @Override
    public List<String> getHandledEventIds() {
        return List.of("addAmendJudgment");
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
    CallbackResponse<CaseData> aboutToSubmit(CaseDetails caseDetails) {
        var request = toCcdRequest(caseDetails);
        log.info("JUDGEMENT SUBMITTED ---> " + LOG_MESSAGE + "{}", request.getCaseDetails().getCaseId());

        CaseData caseData = request.getCaseDetails().getCaseData();
        try {
            judgmentValidationService.validateJudgments(caseData);
        } catch (ParseException exception) {
            throw new IllegalStateException("Failed to validate judgments", exception);
        }
        caseData.setDraftAndSignJudgement(null);
        return toCallbackResponse(getCallbackRespEntityNoErrors(caseData));
    }
}

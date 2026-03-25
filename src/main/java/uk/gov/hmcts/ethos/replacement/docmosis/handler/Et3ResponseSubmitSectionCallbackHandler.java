package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.CallbackRequestContext;
import uk.gov.hmcts.ccd.sdk.CallbackResponse;
import uk.gov.hmcts.ccd.sdk.SubmittedCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericServiceException;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.Et3ResponseHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.Et3ResponseService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityErrors;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Et3ResponseHelper.ET3_RESPONSE;

@Component
public class Et3ResponseSubmitSectionCallbackHandler extends CallbackHandlerBase {

    private static final String SECTION_COMPLETE_BODY = """
        You may want to complete the rest of the ET3 Form using the links below\
        <br><a href="/cases/case-details/%s/trigger/et3Response/et3Response1">ET3 - Respondent Details</a>\
        <br><a href="/cases/case-details/%s/trigger/et3ResponseEmploymentDetails/et3ResponseEmploymentDetails1\
        ">ET3 - Employment Details</a>\
        <br><a href="/cases/case-details/%s/trigger/et3ResponseDetails/et3ResponseDetails1">ET3 - \
        Response Details</a>
        <br><a href="/cases/case-details/%s/trigger/downloadDraftEt3/downloadDraftEt31">Download draft ET3 Form</a>""";

    private final Et3ResponseService et3ResponseService;

    @Autowired
    public Et3ResponseSubmitSectionCallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        Et3ResponseService et3ResponseService
    ) {
        super(caseDetailsConverter);
        this.et3ResponseService = et3ResponseService;
    }

    @Override
    public List<String> getHandledCaseTypeIds() {
        return List.of("ET_Scotland", "ET_EnglandWales");
    }

    @Override
    public List<String> getHandledEventIds() {
        return List.of("et3Response", "et3ResponseEmploymentDetails", "et3ResponseDetails");
    }

    @Override
    public boolean acceptsAboutToSubmit() {
        return true;
    }

    @Override
    public boolean acceptsSubmitted() {
        return true;
    }

    @Override
    CallbackResponse<CaseData> aboutToSubmit(CaseDetails caseDetails) {
        String authorizationToken = CallbackRequestContext.getAuthorizationToken().orElse(null);
        var request = toCcdRequest(caseDetails);
        CaseData caseData = request.getCaseDetails().getCaseData();
        Et3ResponseHelper.addEt3DataToRespondent(caseData, request.getEventId());
        List<String> errors = new ArrayList<>();
        if (ET3_RESPONSE.equals(request.getEventId())) {
            try {
                et3ResponseService.setRespondentRepresentsContactDetails(
                    authorizationToken, caseData, request.getCaseDetails().getCaseId());
            } catch (GenericServiceException genericServiceException) {
                errors.add(genericServiceException.getMessage());
            }
        }
        Et3ResponseHelper.resetEt3FormFields(caseData);
        return toCallbackResponse(getCallbackRespEntityErrors(errors, caseData));
    }

    @Override
    SubmittedCallbackResponse submitted(CaseDetails caseDetails) {
        String ccdId = toCcdRequest(caseDetails).getCaseDetails().getCaseId();
        String body = String.format(SECTION_COMPLETE_BODY, ccdId, ccdId, ccdId, ccdId);
        return toSubmittedCallbackResponse(ResponseEntity.ok(CCDCallbackResponse.builder()
            .data(toCcdRequest(caseDetails).getCaseDetails().getCaseData())
            .confirmation_body(body)
            .build()));
    }
}

package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.et.common.model.multiples.MultipleRequest;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.multiples.MultiplesDocumentAccessService;

import java.util.List;

import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.multipleResponse;

@Component
public class DocumentSelectCallbackHandler extends MultipleCallbackHandlerBase {

    private final MultiplesDocumentAccessService multiplesDocumentAccessService;

    @Autowired
    public DocumentSelectCallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        MultiplesDocumentAccessService multiplesDocumentAccessService
    ) {
        super(caseDetailsConverter);
        this.multiplesDocumentAccessService = multiplesDocumentAccessService;
    }

    @Override
    public List<String> getHandledCaseTypeIds() {
        return List.of("ET_Scotland_Multiple", "ET_EnglandWales_Multiple");
    }

    @Override
    public List<String> getHandledEventIds() {
        return List.of("documentSelect");
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
        var ccdRequest = multipleRequest;
        var multipleData = ccdRequest.getCaseDetails().getCaseData();
        multiplesDocumentAccessService.setMultipleDocumentsToCorrectTab(multipleData);
        return multipleResponse(multipleData, null);
    }
}

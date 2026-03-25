package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.CallbackRequestContext;
import uk.gov.hmcts.ccd.sdk.CallbackResponse;
import uk.gov.hmcts.ccd.sdk.SubmittedCallbackResponse;
import uk.gov.hmcts.ecm.common.model.helper.DefaultValues;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;
import uk.gov.hmcts.et.common.model.ccd.SignificantItem;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.UploadDocumentHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.DefaultValuesReaderService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.DocumentGenerationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.EventValidationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityErrors;

@Component
public class GenerateCorrespondenceGenerateDocumentCallbackHandler extends CallbackHandlerBase {

    private static final String GENERATED_DOCUMENT_URL = "Please download the document from : ";

    private final DocumentGenerationService documentGenerationService;
    private final DefaultValuesReaderService defaultValuesReaderService;
    private final VerifyTokenService verifyTokenService;
    private final EventValidationService eventValidationService;

    @Autowired
    public GenerateCorrespondenceGenerateDocumentCallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        DocumentGenerationService documentGenerationService,
        DefaultValuesReaderService defaultValuesReaderService,
        VerifyTokenService verifyTokenService,
        EventValidationService eventValidationService
    ) {
        super(caseDetailsConverter);
        this.documentGenerationService = documentGenerationService;
        this.defaultValuesReaderService = defaultValuesReaderService;
        this.verifyTokenService = verifyTokenService;
        this.eventValidationService = eventValidationService;
    }

    @Override
    public List<String> getHandledCaseTypeIds() {
        return List.of("ET_Scotland", "ET_EnglandWales");
    }

    @Override
    public List<String> getHandledEventIds() {
        return List.of("generateCorrespondence");
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
        return toCallbackResponse(generateDocument(
                toCcdRequest(caseDetails),
                authorizationToken
            ));
    }

    @Override
    SubmittedCallbackResponse submitted(CaseDetails caseDetails) {
        String authorizationToken = CallbackRequestContext.getAuthorizationToken().orElse(null);
        return toSubmittedCallbackResponse(generateDocumentConfirmation(
                toCcdRequest(caseDetails),
                authorizationToken
            ));
    }

    private ResponseEntity<CCDCallbackResponse> generateDocument(CCDRequest ccdRequest, String userToken) {
        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        var details = ccdRequest.getCaseDetails();
        List<String> errors = eventValidationService.validateHearingNumber(
            details.getCaseData(),
            details.getCaseData().getCorrespondenceType(),
            details.getCaseData().getCorrespondenceScotType()
        );

        if (!errors.isEmpty()) {
            return getCallbackRespEntityErrors(errors, details.getCaseData());
        }

        DefaultValues defaultValues = getPostDefaultValues(details);
        defaultValuesReaderService.setCaseData(details.getCaseData(), defaultValues);

        DocumentInfo documentInfo = documentGenerationService.processDocumentRequest(ccdRequest, userToken);
        documentGenerationService.updateBfActions(documentInfo, details.getCaseData());
        details.getCaseData().setDocMarkUp(documentInfo.getMarkUp());
        documentGenerationService.clearUserChoices(details);
        UploadDocumentHelper.convertLegacyDocsToNewDocNaming(details.getCaseData());
        UploadDocumentHelper.setDocumentTypeForDocumentCollection(details.getCaseData());
        SignificantItem significantItem = Helper.generateSignificantItem(documentInfo, errors);

        return ResponseEntity.ok(
            CCDCallbackResponse.builder()
                .data(details.getCaseData())
                .errors(errors)
                .significant_item(significantItem)
                .build()
        );
    }

    private ResponseEntity<CCDCallbackResponse> generateDocumentConfirmation(CCDRequest ccdRequest, String userToken) {
        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        return ResponseEntity.ok(
            CCDCallbackResponse.builder()
                .data(ccdRequest.getCaseDetails().getCaseData())
                .confirmation_body(GENERATED_DOCUMENT_URL + ccdRequest.getCaseDetails().getCaseData().getDocMarkUp())
                .build()
        );
    }

    private DefaultValues getPostDefaultValues(uk.gov.hmcts.et.common.model.ccd.CaseDetails caseDetails) {
        return defaultValuesReaderService.getDefaultValues(caseDetails.getCaseData().getManagingOffice());
    }
}

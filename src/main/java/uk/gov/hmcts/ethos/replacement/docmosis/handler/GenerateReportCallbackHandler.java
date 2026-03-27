package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.CallbackRequestContext;
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;
import uk.gov.hmcts.et.common.model.listing.ListingCallbackResponse;
import uk.gov.hmcts.et.common.model.listing.ListingData;
import uk.gov.hmcts.et.common.model.listing.ListingRequest;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.ListingHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.casesawaitingjudgment.CasesAwaitingJudgmentReportData;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.hearingstojudgments.HearingsToJudgmentsReportData;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.nochangeincurrentposition.NoPositionChangeReportData;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.respondentsreport.RespondentsReportData;
import uk.gov.hmcts.ethos.replacement.docmosis.service.ListingService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.ReportDataService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.callback.ListingGenerationCallbackService;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SERVING_CLAIMS_REPORT;

@Slf4j
@Component
public class GenerateReportCallbackHandler extends ListingCallbackHandlerBase {

    private static final String LOG_MESSAGE = "received notification request for case reference : ";
    private static final String INVALID_TOKEN = "Invalid Token {}";

    private final ListingService listingService;
    private final ReportDataService reportDataService;
    private final VerifyTokenService verifyTokenService;
    private final ListingGenerationCallbackService listingGenerationCallbackService;

    @Autowired
    public GenerateReportCallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        ListingService listingService,
        ReportDataService reportDataService,
        VerifyTokenService verifyTokenService,
        ListingGenerationCallbackService listingGenerationCallbackService
    ) {
        super(caseDetailsConverter);
        this.listingService = listingService;
        this.reportDataService = reportDataService;
        this.verifyTokenService = verifyTokenService;
        this.listingGenerationCallbackService = listingGenerationCallbackService;
    }

    @Override
    public List<String> getHandledCaseTypeIds() {
        return List.of("ET_Scotland_Listings", "ET_EnglandWales_Listings");
    }

    @Override
    public List<String> getHandledEventIds() {
        return List.of("generateReport");
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
    Object aboutToSubmit(ListingRequest listingRequest) {
        String authorizationToken = CallbackRequestContext.getAuthorizationToken().orElse(null);
        var request = listingRequest;
        log.info("GENERATE REPORT ---> " + LOG_MESSAGE + request.getCaseDetails().getCaseId());

        if (!verifyTokenService.verifyTokenSignature(authorizationToken)) {
            log.error(INVALID_TOKEN, authorizationToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        ListingData listingData = reportDataService.generateReportData(request.getCaseDetails(), authorizationToken);
        return getResponseEntity(listingData, request.getCaseDetails().getCaseTypeId(), authorizationToken);
    }

    @Override
    Object submitted(ListingRequest listingRequest) {
        String authorizationToken = CallbackRequestContext.getAuthorizationToken().orElse(null);
        return listingGenerationCallbackService.generateHearingDocumentConfirmation(
            listingRequest,
            authorizationToken
        );
    }

    private ResponseEntity<ListingCallbackResponse> getResponseEntity(
        ListingData listingData,
        String caseTypeId,
        String userToken
    ) {
        List<String> errorsList = new ArrayList<>();

        if (hasListings(listingData)
            || isAllowedReportType(listingData) && hasServedClaims(listingData)
            || hasSummaryAndDetails(listingData)) {
            DocumentInfo documentInfo = listingService.processHearingDocument(listingData, caseTypeId, userToken);
            listingData.setDocMarkUp(documentInfo.getMarkUp());
            return ResponseEntity.ok(ListingCallbackResponse.builder()
                .data(listingData)
                .significant_item(Helper.generateSignificantItem(documentInfo, errorsList))
                .build());
        }

        errorsList.add("No cases (with hearings / claims served) have been found for your search criteria");
        return ResponseEntity.ok(ListingCallbackResponse.builder()
            .errors(errorsList)
            .data(listingData)
            .build());
    }

    private boolean hasServedClaims(ListingData listingData) {
        if (SERVING_CLAIMS_REPORT.equals(listingData.getReportType())) {
            return CollectionUtils.isNotEmpty(listingData.getLocalReportsDetail());
        }
        return true;
    }

    private boolean isAllowedReportType(ListingData listingData) {
        return ListingHelper.isReportType(listingData.getReportType());
    }

    private boolean hasListings(ListingData listingData) {
        return CollectionUtils.isNotEmpty(listingData.getListingCollection());
    }

    private boolean hasSummaryAndDetails(ListingData listingData) {
        return listingData.getClass() == HearingsToJudgmentsReportData.class
            || listingData.getClass() == CasesAwaitingJudgmentReportData.class
            || listingData.getClass() == RespondentsReportData.class
            || listingData.getClass() == NoPositionChangeReportData.class;
    }
}

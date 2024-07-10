package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import org.jetbrains.annotations.NotNull;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;
import uk.gov.hmcts.et.common.model.listing.ListingCallbackResponse;
import uk.gov.hmcts.et.common.model.listing.ListingData;
import uk.gov.hmcts.et.common.model.multiples.MultipleCallbackResponse;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.et.common.model.multiples.MultipleDetails;

import java.util.List;

public final class CallbackRespHelper {

    private CallbackRespHelper() {
    }

    @NotNull
    public static ResponseEntity<CCDCallbackResponse> getCallbackRespEntity(
            List<String> errors, CaseDetails caseDetails) {
        return ResponseEntity.ok(CCDCallbackResponse.builder()
                .errors(errors)
                .data(caseDetails.getCaseData())
                .build());
    }

    @NotNull
    public static ResponseEntity<CCDCallbackResponse> getCallbackRespEntityNoErrors(
            CaseData caseData) {

        return ResponseEntity.ok(CCDCallbackResponse.builder()
                .data(caseData)
                .build());
    }

    @NotNull
    public static ResponseEntity<CCDCallbackResponse> getCallbackRespEntityErrors(
            List<String> errors, CaseData caseData) {

        return ResponseEntity.ok(CCDCallbackResponse.builder()
                .data(caseData)
                .errors(errors)
                .build());
    }

    @NotNull
    public static ResponseEntity<ListingCallbackResponse> getListingCallbackRespEntityErrors(
            List<String> errors, ListingData listingData) {

        return ResponseEntity.ok(ListingCallbackResponse.builder()
                .data(listingData)
                .errors(errors)
                .build());
    }

    @NotNull
    public static ResponseEntity<MultipleCallbackResponse> getMultipleCallbackRespEntity(
            List<String> errors, MultipleDetails multipleDetails) {

        return ResponseEntity.ok(MultipleCallbackResponse.builder()
                .errors(errors)
                .data(multipleDetails.getCaseData())
                .build());
    }

    @NotNull
    public static ResponseEntity<MultipleCallbackResponse> getMultipleCallbackRespEntityDocInfo(
            List<String> errors, MultipleDetails multipleDetails, DocumentInfo documentInfo) {
        if (errors.isEmpty()) {

            multipleDetails.getCaseData().setDocMarkUp(documentInfo.getMarkUp());

            return ResponseEntity.ok(MultipleCallbackResponse.builder()
                    .data(multipleDetails.getCaseData())
                    .significant_item(Helper.generateSignificantItem(documentInfo, errors))
                    .build());

        } else {

            return ResponseEntity.ok(MultipleCallbackResponse.builder()
                    .errors(errors)
                    .data(multipleDetails.getCaseData())
                    .build());

        }
    }

    public static ResponseEntity<MultipleCallbackResponse> multipleResponse(MultipleData data, List<String> errors) {
        return ResponseEntity.ok(MultipleCallbackResponse.builder()
                .errors(errors)
                .data(data)
                .build());
    }

    @NotNull
    public static ResponseEntity<MultipleCallbackResponse> getMultipleCallbackRespEntityErrorsAndWarnings(
            List<String> warnings, List<String> errors, MultipleData caseData) {

        return ResponseEntity.ok(MultipleCallbackResponse.builder()
                .data(caseData)
                .errors(errors)
                .warnings(warnings)
                .build());
    }

}

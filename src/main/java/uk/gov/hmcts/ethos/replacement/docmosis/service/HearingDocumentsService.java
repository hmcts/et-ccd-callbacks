package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.HearingBundleType;

import java.util.ArrayList;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static uk.gov.hmcts.ecm.common.helpers.UtilHelper.listingFormatLocalDate;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.ET1ReppedConstants.OTHER;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.HearingDocumentsHelper.getSelectedHearing;

@RequiredArgsConstructor
@Service
@Slf4j
public class HearingDocumentsService {
    private final UserIdamService userIdamService;
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("d MMMM yyyy HH:mm");

    /**
     * Adds a document to the hearing document collection in the case data.
     *
     * @param caseData   the case data to which the document will be added
     * @param userToken  the token of the user uploading the document
     */
    public void addDocumentToHearingDocuments(CaseData caseData, String userToken) {
        String uploadedBy = "Admin";
        try {
            UserDetails userDetails = userIdamService.getUserDetails(userToken);
            if (userDetails != null) {
                uploadedBy = userDetails.getFirstName() + " " + userDetails.getLastName();
            }
        } catch (Exception e) {
            log.error("Error getting user details", e);
        }

        GenericTypeItem<HearingBundleType> documentToAdd =
            GenericTypeItem.from(
                HearingBundleType.builder()
                    .hearing(getSelectedHearing(caseData).getCode())
                    .formattedSelectedHearing(getSelectedHearing(caseData).getLabel())
                    .uploadFile(caseData.getUploadHearingDocumentsUploadFile())
                    .whatDocuments(caseData.getUploadHearingDocumentsWhatAreDocuments())
                    .whatDocumentsOther(OTHER.equals(caseData.getUploadHearingDocumentsWhatAreDocuments())
                        ? caseData.getUploadHearingDocumentsWhatAreDocumentsOther() : null)
                    .whoseDocuments(isNullOrEmpty(caseData.getUploadHearingDocumentsWhatAreDocuments())
                        ? null : caseData.getUploadHearingDocumentsWhoseDocuments())
                    .submittedDate(listingFormatLocalDate(caseData.getUploadHearingDocumentsDateSubmitted()))
                    .uploadDateTime(dateTimeFormatter.print(DateTime.now()))
                    .uploadedBy(uploadedBy)
                    .build()

            );
        if (isEmpty(caseData.getHearingDocumentCollection())) {
            caseData.setHearingDocumentCollection(new ArrayList<>());
        }
        caseData.getHearingDocumentCollection().add(documentToAdd);

        clearUploadHearingDocumentFields(caseData);
    }

    private static void clearUploadHearingDocumentFields(CaseData caseData) {
        caseData.setUploadHearingDocumentsSelectFutureHearing(null);
        caseData.setUploadHearingDocumentsSelectPastHearing(null);
        caseData.setUploadHearingDocumentsSelectPastOrFutureHearing(null);
        caseData.setUploadHearingDocumentsUploadFile(null);
        caseData.setUploadHearingDocumentsWhatAreDocuments(null);
        caseData.setUploadHearingDocumentsWhatAreDocumentsOther(null);
        caseData.setUploadHearingDocumentsWhoseDocuments(null);
        caseData.setUploadHearingDocumentsDateSubmitted(null);
    }
}

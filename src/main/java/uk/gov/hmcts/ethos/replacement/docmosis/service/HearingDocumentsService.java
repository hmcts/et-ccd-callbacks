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
import uk.gov.hmcts.et.common.model.ccd.types.UploadHearingDocumentType;

import java.util.ArrayList;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static uk.gov.hmcts.ecm.common.helpers.UtilHelper.listingFormatLocalDate;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLAIMANT_TITLE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.RESPONDENT_TITLE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.ET1ReppedConstants.OTHER;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.HearingDocumentsHelper.getSelectedHearing;

@RequiredArgsConstructor
@Service
@Slf4j
public class HearingDocumentsService {
    private final UserIdamService userIdamService;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormat.forPattern("d MMMM yyyy HH:mm");

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
                uploadedBy = userDetails.getName();
            }
        } catch (Exception e) {
            log.warn("Error getting user details", e);
        }

        if (isEmpty(caseData.getUploadHearingDocumentType())) {
            throw new IllegalArgumentException("UploadHearingDocumentType cannot be empty");
        }

        if (isEmpty(caseData.getBundlesRespondentCollection())) {
            caseData.setBundlesRespondentCollection(new ArrayList<>());
        }
        if (isEmpty(caseData.getBundlesClaimantCollection())) {
            caseData.setBundlesClaimantCollection(new ArrayList<>());
        }

        for (GenericTypeItem<UploadHearingDocumentType> hearingDoc : caseData.getUploadHearingDocumentType()) {
            UploadHearingDocumentType hearingDocumentType = hearingDoc.getValue();
            GenericTypeItem<HearingBundleType> documentToAdd =
                GenericTypeItem.from(
                    HearingBundleType.builder()
                        .hearing(getSelectedHearing(caseData).getCode())
                        .formattedSelectedHearing(getSelectedHearing(caseData).getLabel())
                        .uploadFile(hearingDocumentType.getDocument())
                        .whatDocuments(hearingDocumentType.getType())
                        .whatDocumentsOther(OTHER.equals(hearingDocumentType.getType())
                            ? hearingDocumentType.getTypeOther() : null)
                        .submittedDate(listingFormatLocalDate(caseData.getUploadHearingDocumentsDateSubmitted()))
                        .uploadDateTime(DATE_TIME_FORMATTER.print(DateTime.now()))
                        .uploadedBy(uploadedBy)
                        .build()
                );

            switch (caseData.getUploadHearingDocumentsWhoseDocuments()) {
                case CLAIMANT_TITLE -> caseData.getBundlesClaimantCollection().add(documentToAdd);
                case RESPONDENT_TITLE -> caseData.getBundlesRespondentCollection().add(documentToAdd);
                default -> throw new IllegalArgumentException(
                    "Invalid value for UploadHearingDocumentsWhoseDocuments: "
                        + caseData.getUploadHearingDocumentsWhoseDocuments());
            }
        }

        clearUploadHearingDocumentFields(caseData);
    }

    private static void clearUploadHearingDocumentFields(CaseData caseData) {
        caseData.setUploadHearingDocumentsSelectFutureHearing(null);
        caseData.setUploadHearingDocumentsSelectPastHearing(null);
        caseData.setUploadHearingDocumentsSelectPastOrFutureHearing(null);
        caseData.setUploadHearingDocumentType(null);
        caseData.setUploadHearingDocumentsWhoseDocuments(null);
        caseData.setUploadHearingDocumentsDateSubmitted(null);
    }
}

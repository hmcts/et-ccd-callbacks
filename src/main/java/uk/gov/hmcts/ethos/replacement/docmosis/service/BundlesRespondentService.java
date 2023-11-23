package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.HearingTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.HearingBundleType;
import uk.gov.hmcts.et.common.model.ccd.types.HearingType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.HearingsHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static uk.gov.hmcts.ecm.common.helpers.UtilHelper.formatLocalDate;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;

@Slf4j
@Service
@RequiredArgsConstructor
public class BundlesRespondentService {

    /**
     * Clear interface data from caseData.
     * @param caseData contains all the case data
     */
    private static final String AGREED_DOCS_WITH_BUT = "No, we have not agreed and I want to provide my own documents";
    private static final String AGREED_DOCS_WITH_NO = "No, we have not agreed and I want to provide my own documents";
    private static final String BUT = "But";

    public void clearInputData(CaseData caseData) {
        caseData.setBundlesRespondentPrepareDocNotesShow(null);
        caseData.setBundlesRespondentAgreedDocWith(null);
        caseData.setBundlesRespondentAgreedDocWithBut(null);
        caseData.setBundlesRespondentAgreedDocWithNo(null);
        caseData.setBundlesRespondentSelectHearing(null);
        caseData.setBundlesRespondentUploadFile(null);
        caseData.setBundlesRespondentWhatDocuments(null);
        caseData.setBundlesRespondentWhoseDocuments(null);
    }

    /**
     * Populates select hearing field with available hearings.
     */
    public void populateSelectHearings(CaseData caseData) {
        if (CollectionUtils.isEmpty(caseData.getHearingCollection())) {
            return;
        }
        DynamicFixedListType listType = DynamicFixedListType.from(caseData.getHearingCollection().stream()
                .map(this::createValueType)
                .toList()
        );

        caseData.setBundlesRespondentSelectHearing(listType);
    }

    private DynamicValueType createValueType(HearingTypeItem hearingTypeItem) {
        var earliestHearing = HearingsHelper.mapEarliest(hearingTypeItem);
        if (earliestHearing == null) {
            return null;
        }

        HearingType value = hearingTypeItem.getValue();
        String label = String.format("%s %s - %s - %s",
                value.getHearingNumber(),
                value.getHearingType(),
                HearingsHelper.getHearingVenue(value),
                formatLocalDate(earliestHearing.getValue().getListedDate())
        );

        return DynamicValueType.create(value.getHearingNumber(), label);
    }

    /**
     * Validates that the file uploaded for bundles is a PDF file.
     */
    public List<String> validateFileUpload(CaseData caseData) {
        UploadedDocumentType document = caseData.getBundlesRespondentUploadFile();
        if (document == null) {
            return List.of("You must upload a PDF file");
        }

        if (document.getDocumentFilename().toLowerCase(Locale.ENGLISH).endsWith(".pdf")) {
            return List.of();
        }

        return List.of("Your upload contains a disallowed file type");
    }

    /**
     * Creates a HearingBundleType and adds to the Bundles collection on CaseData.
     */
    public void addToBundlesCollection(CaseData caseData) {
        if (caseData.getBundlesRespondentCollection() == null) {
            caseData.setBundlesRespondentCollection(new ArrayList<>());
        }
        String agreedDocsWith = caseData.getBundlesRespondentAgreedDocWith();
        if (agreedDocsWith.equals(BUT)) {
            agreedDocsWith = AGREED_DOCS_WITH_BUT;
        }
        if (agreedDocsWith.equals(NO)) {
            agreedDocsWith = AGREED_DOCS_WITH_NO;
        }

        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("d MMMM yyyy HH:mm");
        caseData.getBundlesRespondentCollection().add(
            GenericTypeItem.from(HearingBundleType.builder()
                .agreedDocWith(agreedDocsWith)
                .agreedDocWithBut(caseData.getBundlesRespondentAgreedDocWithBut())
                .agreedDocWithNo(caseData.getBundlesRespondentAgreedDocWithNo())
                .hearing(caseData.getBundlesRespondentSelectHearing().getSelectedCode())
                .uploadFile(caseData.getBundlesRespondentUploadFile())
                .whatDocuments(caseData.getBundlesRespondentWhatDocuments())
                .whoseDocuments(caseData.getBundlesRespondentWhoseDocuments())
                    .formattedSelectedHearing(caseData.getBundlesRespondentSelectHearing().getSelectedLabel())
                    .uploadDateTime(dateTimeFormatter.print(DateTime.now()))
                .build()
            )
        );
    }
}

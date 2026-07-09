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
import uk.gov.hmcts.et.common.model.ccd.items.DateListedTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.HearingTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.HearingBundleType;
import uk.gov.hmcts.et.common.model.ccd.types.HearingType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.HearingsHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static uk.gov.hmcts.ecm.common.helpers.UtilHelper.formatLocalDate;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;

@Slf4j
@Service
@RequiredArgsConstructor
public class BundlesClaimantService {

    private static final String AGREED_DOCS_WITH_BUT = "We have agreed but there are some disputed documents";
    private static final String AGREED_DOCS_WITH_NO = "No, we have not agreed and I want to provide my own documents";
    private static final String BUT = "But";

    public void clearInputData(CaseData caseData) {
        caseData.setBundlesClaimantPrepareDocNotesShow(null);
        caseData.setBundlesClaimantAgreedDocWith(null);
        caseData.setBundlesClaimantAgreedDocWithBut(null);
        caseData.setBundlesClaimantAgreedDocWithNo(null);
        caseData.setBundlesClaimantSelectHearing(null);
        caseData.setBundlesClaimantUploadFile(null);
        caseData.setBundlesClaimantWhatDocuments(null);
        caseData.setBundlesClaimantWhoseDocuments(null);
    }

    public void populateSelectHearings(CaseData caseData) {
        if (CollectionUtils.isEmpty(caseData.getHearingCollection())) {
            return;
        }

        DynamicFixedListType listType = DynamicFixedListType.from(caseData.getHearingCollection().stream()
                .map(this::createValueType)
                .filter(Objects::nonNull)
                .toList()
        );

        caseData.setBundlesClaimantSelectHearing(listType);
    }

    private DynamicValueType createValueType(HearingTypeItem hearingTypeItem) {
        DateListedTypeItem earliestHearing = HearingsHelper.mapEarliest(hearingTypeItem);
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

    public List<String> validateFileUpload(CaseData caseData) {
        UploadedDocumentType document = caseData.getBundlesClaimantUploadFile();
        if (document == null) {
            return List.of("You must upload a PDF file");
        }

        if (document.getDocumentFilename().toLowerCase(Locale.ENGLISH).endsWith(".pdf")) {
            return List.of();
        }

        return List.of("Your upload contains a disallowed file type");
    }

    public void addToBundlesCollection(CaseData caseData) {
        if (caseData.getBundlesClaimantCollection() == null) {
            caseData.setBundlesClaimantCollection(new ArrayList<>());
        }
        String agreedDocsWith = caseData.getBundlesClaimantAgreedDocWith();
        if (BUT.equals(agreedDocsWith)) {
            agreedDocsWith = AGREED_DOCS_WITH_BUT;
        }
        if (NO.equals(agreedDocsWith)) {
            agreedDocsWith = AGREED_DOCS_WITH_NO;
        }

        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("d MMMM yyyy HH:mm");
        caseData.getBundlesClaimantCollection().add(
            GenericTypeItem.from(HearingBundleType.builder()
                .agreedDocWith(agreedDocsWith)
                .agreedDocWithBut(caseData.getBundlesClaimantAgreedDocWithBut())
                .agreedDocWithNo(caseData.getBundlesClaimantAgreedDocWithNo())
                .hearing(caseData.getBundlesClaimantSelectHearing().getSelectedCode())
                .uploadFile(caseData.getBundlesClaimantUploadFile())
                .whatDocuments(caseData.getBundlesClaimantWhatDocuments())
                .whoseDocuments(caseData.getBundlesClaimantWhoseDocuments())
                .formattedSelectedHearing(caseData.getBundlesClaimantSelectHearing().getSelectedLabel())
                .uploadDateTime(dateTimeFormatter.print(DateTime.now()))
                .build()
            )
        );
    }
}

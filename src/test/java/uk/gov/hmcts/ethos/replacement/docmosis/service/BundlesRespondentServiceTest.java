package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.HearingBundleType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertNull;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@ExtendWith(SpringExtension.class)
class BundlesRespondentServiceTest {

    private BundlesRespondentService bundlesRespondentService;
    private CaseData scotlandCaseData;
    private CaseData englandCaseData;

    @BeforeEach
    void setUp() {
        bundlesRespondentService = new BundlesRespondentService();
        englandCaseData = CaseDataBuilder.builder()
                .withHearing("1", "Hearing", "Judge", "Bodmin", List.of("In person"), "60", "Days", "Sit Alone")
                .withHearingSession(0, "1", "2069-05-16T01:00:00.000", "Listed", false)
                .withHearingSession(0, "2", "2022-05-16T01:00:00.000", "Listed", false)
                .withHearing("2", "Costs Hearing", "Judge", "ROIT", List.of("Video"), "60", "Days", "Sit Alone")
                .withHearingSession(1, "1", "2069-05-16T01:00:00.000", "Listed", false)
                .withHearingSession(1, "2", "2070-05-16T01:00:00.000", "Listed", false)
                .build();

        scotlandCaseData = CaseDataBuilder.builder()
                .withHearingScotland("1", "Hearing", "Judge", TribunalOffice.EDINBURGH, "Venue")
                .withHearingSession(0, "1", "2069-05-16T01:00:00.000", "Listed", false)
                .withHearingSession(0, "2", "2022-05-16T01:00:00.000", "Listed", false)
                .withHearingScotland("2", "Costs Hearing", "Judge", TribunalOffice.ABERDEEN, "Venue")
                .withHearingSession(1, "1", "2069-05-16T01:00:00.000", "Listed", false)
                .withHearingSession(1, "2", "2070-05-16T01:00:00.000", "Listed", false)
                .build();
    }

    @Test
    void clearInputData() {
        englandCaseData.setBundlesRespondentPrepareDocNotesShow(YES);
        englandCaseData.setBundlesRespondentAgreedDocWith(NO);
        englandCaseData.setBundlesRespondentAgreedDocWithBut("Some input");
        englandCaseData.setBundlesRespondentAgreedDocWithNo("Some input");

        bundlesRespondentService.clearInputData(englandCaseData);

        assertNull(englandCaseData.getBundlesRespondentPrepareDocNotesShow());
        assertNull(englandCaseData.getBundlesRespondentAgreedDocWith());
        assertNull(englandCaseData.getBundlesRespondentAgreedDocWithBut());
        assertNull(englandCaseData.getBundlesRespondentAgreedDocWithNo());
    }

    @Test
    void populateSelectHearings_englandWales_twoOptionsWithCorrectDates() {
        bundlesRespondentService.populateSelectHearings(englandCaseData);
        var actual = englandCaseData.getBundlesRespondentSelectHearing().getListItems();

        assertThat(actual.size(), is(2));
        assertThat(actual.get(0).getLabel(), is("1 Hearing - Bodmin - 16 May 2069"));
        assertThat(actual.get(1).getLabel(), is("2 Costs Hearing - ROIT - 16 May 2069"));
    }

    @Test
    void populateSelectHearings_scotland_twoOptionsWithCorrectDates() {
        bundlesRespondentService.populateSelectHearings(scotlandCaseData);
        var actual = scotlandCaseData.getBundlesRespondentSelectHearing().getListItems();

        assertThat(actual.size(), is(2));
        assertThat(actual.get(0).getLabel(), is("1 Hearing - Edinburgh - 16 May 2069"));
        assertThat(actual.get(1).getLabel(), is("2 Costs Hearing - Aberdeen - 16 May 2069"));
    }

    @Test
    void populateSelectHearings_doesNothingWhenNoHearings() {
        scotlandCaseData.setHearingCollection(null);
        bundlesRespondentService.populateSelectHearings(scotlandCaseData);

        assertNull(scotlandCaseData.getBundlesRespondentSelectHearing());
    }

    @Test
    void validateFileUpload_noFile_returnsError() {
        englandCaseData.setBundlesRespondentUploadFile(null);
        List<String> errors = bundlesRespondentService.validateFileUpload(englandCaseData);
        assertThat(errors.size(), is(1));
        assertThat(errors.get(0), is("You must upload a PDF file"));
    }

    @Test
    void validateFileUpload_wrongFileType_returnsError() {
        UploadedDocumentType uploadedDocumentType = UploadedDocumentType.builder().documentFilename("file.txt").build();
        englandCaseData.setBundlesRespondentUploadFile(uploadedDocumentType);
        List<String> errors = bundlesRespondentService.validateFileUpload(englandCaseData);
        assertThat(errors.size(), is(1));
        assertThat(errors.get(0), is("Your upload contains a disallowed file type"));
    }

    @Test
    void validateFileUpload_correctFileType_noErrors() {
        UploadedDocumentType uploadedDocumentType = UploadedDocumentType.builder().documentFilename("file.pdf").build();
        englandCaseData.setBundlesRespondentUploadFile(uploadedDocumentType);
        List<String> errors = bundlesRespondentService.validateFileUpload(englandCaseData);
        assertThat(errors.size(), is(0));
    }

    @Test
    void addToBundlesCollection_whenEmptyCollection_createsNewCollection() {
        englandCaseData.setBundlesRespondentCollection(null);
        englandCaseData.setBundlesRespondentSelectHearing(getTestSelectHearing());

        bundlesRespondentService.addToBundlesCollection(englandCaseData);

        assertThat(englandCaseData.getBundlesRespondentCollection().size(), is(1));
    }

    @NotNull
    private static DynamicFixedListType getTestSelectHearing() {
        DynamicFixedListType selectHearing = DynamicFixedListType.from(List.of(
                DynamicValueType.create("1", "1 Hearing - Bodmin - 16 May 2069")));

        selectHearing.setValue(selectHearing.getListItems().get(0));
        return selectHearing;
    }

    @Test
    void addToBundlesCollection_addsBundlesObject() {
        List<GenericTypeItem<HearingBundleType>> collection = new ArrayList<>();
        englandCaseData.setBundlesRespondentCollection(collection);

        String respondentsDocumentsOnly = "Respondent's documents only";
        String witnessStatementsOnly = "Witness statements only";
        String butReason = "ButReason";
        String disagree = "Disagree";
        UploadedDocumentType file = UploadedDocumentType.builder().documentFilename("file.txt").build();

        englandCaseData.setBundlesRespondentWhoseDocuments(respondentsDocumentsOnly);
        englandCaseData.setBundlesRespondentWhatDocuments(witnessStatementsOnly);
        englandCaseData.setBundlesRespondentSelectHearing(getTestSelectHearing());
        englandCaseData.setBundlesRespondentUploadFile(file);
        englandCaseData.setBundlesRespondentAgreedDocWith(YES);
        englandCaseData.setBundlesRespondentAgreedDocWithBut(butReason);
        englandCaseData.setBundlesRespondentAgreedDocWithNo(disagree);

        bundlesRespondentService.addToBundlesCollection(englandCaseData);
        HearingBundleType actual = collection.get(0).getValue();

        assertThat(englandCaseData.getBundlesRespondentCollection(), is(collection));
        assertThat(actual.getAgreedDocWith(), is(YES));
        assertThat(actual.getAgreedDocWithBut(), is(butReason));
        assertThat(actual.getAgreedDocWithNo(), is(disagree));
        assertThat(actual.getHearing(), is("1"));
        assertThat(actual.getUploadFile(), is(file));
        assertThat(actual.getWhatDocuments(), is(witnessStatementsOnly));
        assertThat(actual.getWhoseDocuments(), is(respondentsDocumentsOnly));
    }
}

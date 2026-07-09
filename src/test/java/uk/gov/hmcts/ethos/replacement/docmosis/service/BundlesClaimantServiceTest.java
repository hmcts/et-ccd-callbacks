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
class BundlesClaimantServiceTest {

    private BundlesClaimantService bundlesClaimantService;
    private CaseData scotlandCaseData;
    private CaseData englandCaseData;

    @BeforeEach
    void setUp() {
        bundlesClaimantService = new BundlesClaimantService();
        englandCaseData = CaseDataBuilder.builder()
                .withHearing("1", "Hearing", "Judge", "Bodmin", List.of("In person"), "60", "Days", "Sit Alone")
                .withHearingSession(0, "2069-05-16T01:00:00.000", "Listed", false)
                .withHearingSession(0, "2022-05-16T01:00:00.000", "Listed", false)
                .withHearing("2", "Costs Hearing", "Judge", "ROIT", List.of("Video"), "60", "Days", "Sit Alone")
                .withHearingSession(1, "2069-05-16T01:00:00.000", "Listed", false)
                .withHearingSession(1, "2070-05-16T01:00:00.000", "Listed", false)
                .build();

        scotlandCaseData = CaseDataBuilder.builder()
                .withHearingScotland("1", "Hearing", "Judge", TribunalOffice.EDINBURGH, "Venue")
                .withHearingSession(0, "2069-05-16T01:00:00.000", "Listed", false)
                .withHearingSession(0, "2022-05-16T01:00:00.000", "Listed", false)
                .withHearingScotland("2", "Costs Hearing", "Judge", TribunalOffice.ABERDEEN, "Venue")
                .withHearingSession(1, "2069-05-16T01:00:00.000", "Listed", false)
                .withHearingSession(1, "2070-05-16T01:00:00.000", "Listed", false)
                .build();
    }

    @Test
    void clearInputData() {
        englandCaseData.setBundlesClaimantPrepareDocNotesShow(YES);
        englandCaseData.setBundlesClaimantAgreedDocWith(NO);
        englandCaseData.setBundlesClaimantAgreedDocWithBut("Some input");
        englandCaseData.setBundlesClaimantAgreedDocWithNo("Some input");

        bundlesClaimantService.clearInputData(englandCaseData);

        assertNull(englandCaseData.getBundlesClaimantPrepareDocNotesShow());
        assertNull(englandCaseData.getBundlesClaimantAgreedDocWith());
        assertNull(englandCaseData.getBundlesClaimantAgreedDocWithBut());
        assertNull(englandCaseData.getBundlesClaimantAgreedDocWithNo());
    }

    @Test
    void populateSelectHearings_englandWales_twoOptionsWithCorrectDates() {
        bundlesClaimantService.populateSelectHearings(englandCaseData);
        var actual = englandCaseData.getBundlesClaimantSelectHearing().getListItems();

        assertThat(actual.size(), is(2));
        assertThat(actual.get(0).getLabel(), is("1 Hearing - Bodmin - 16 May 2069"));
        assertThat(actual.get(1).getLabel(), is("2 Costs Hearing - ROIT - 16 May 2069"));
    }

    @Test
    void populateSelectHearings_filtersOutPastDates() {
        englandCaseData.getHearingCollection().get(0).getValue().getHearingDateCollection().get(0).getValue()
                .setListedDate("2022-05-16T01:00:00.000");

        bundlesClaimantService.populateSelectHearings(englandCaseData);
        var actual = englandCaseData.getBundlesClaimantSelectHearing().getListItems();

        assertThat(actual.size(), is(1));
        assertThat(actual.get(0).getLabel(), is("2 Costs Hearing - ROIT - 16 May 2069"));
    }

    @Test
    void populateSelectHearings_scotland_twoOptionsWithCorrectDates() {
        bundlesClaimantService.populateSelectHearings(scotlandCaseData);
        var actual = scotlandCaseData.getBundlesClaimantSelectHearing().getListItems();

        assertThat(actual.size(), is(2));
        assertThat(actual.get(0).getLabel(), is("1 Hearing - Edinburgh - 16 May 2069"));
        assertThat(actual.get(1).getLabel(), is("2 Costs Hearing - Aberdeen - 16 May 2069"));
    }

    @Test
    void populateSelectHearings_doesNothingWhenNoHearings() {
        scotlandCaseData.setHearingCollection(null);
        bundlesClaimantService.populateSelectHearings(scotlandCaseData);

        assertNull(scotlandCaseData.getBundlesClaimantSelectHearing());
    }

    @Test
    void validateFileUpload_noFile_returnsError() {
        englandCaseData.setBundlesClaimantUploadFile(null);
        List<String> errors = bundlesClaimantService.validateFileUpload(englandCaseData);
        assertThat(errors.size(), is(1));
        assertThat(errors.get(0), is("You must upload a PDF file"));
    }

    @Test
    void validateFileUpload_wrongFileType_returnsError() {
        UploadedDocumentType uploadedDocumentType = UploadedDocumentType.builder().documentFilename("file.txt").build();
        englandCaseData.setBundlesClaimantUploadFile(uploadedDocumentType);
        List<String> errors = bundlesClaimantService.validateFileUpload(englandCaseData);
        assertThat(errors.size(), is(1));
        assertThat(errors.get(0), is("Your upload contains a disallowed file type"));
    }

    @Test
    void validateFileUpload_correctFileType_noErrors() {
        UploadedDocumentType uploadedDocumentType = UploadedDocumentType.builder().documentFilename("file.pdf").build();
        englandCaseData.setBundlesClaimantUploadFile(uploadedDocumentType);
        List<String> errors = bundlesClaimantService.validateFileUpload(englandCaseData);
        assertThat(errors.size(), is(0));
    }

    @Test
    void addToBundlesCollection_whenEmptyCollection_createsNewCollection() {
        englandCaseData.setBundlesClaimantCollection(null);
        englandCaseData.setBundlesClaimantSelectHearing(getTestSelectHearing());
        englandCaseData.setBundlesClaimantAgreedDocWith("Yes");
        bundlesClaimantService.addToBundlesCollection(englandCaseData);

        assertThat(englandCaseData.getBundlesClaimantCollection().size(), is(1));
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
        englandCaseData.setBundlesClaimantCollection(collection);

        String claimantsDocumentsOnly = "Claimant's documents only";
        String witnessStatementsOnly = "Witness statements only";
        String butReason = "ButReason";
        String disagree = "Disagree";
        UploadedDocumentType file = UploadedDocumentType.builder().documentFilename("file.txt").build();
        setupEnglandCaseData(YES, butReason, disagree, claimantsDocumentsOnly, witnessStatementsOnly, file);

        bundlesClaimantService.addToBundlesCollection(englandCaseData);
        HearingBundleType actual = collection.get(0).getValue();

        assertThat(englandCaseData.getBundlesClaimantCollection(), is(collection));
        assertThat(actual.getAgreedDocWith(), is(YES));
        assertThat(actual.getAgreedDocWithBut(), is(butReason));
        assertThat(actual.getAgreedDocWithNo(), is(disagree));
        assertThat(actual.getHearing(), is("1"));
        assertThat(actual.getUploadFile(), is(file));
        assertThat(actual.getWhatDocuments(), is(witnessStatementsOnly));
        assertThat(actual.getWhoseDocuments(), is(claimantsDocumentsOnly));
        assertThat(actual.getFormattedSelectedHearing(),
                is(englandCaseData.getBundlesClaimantSelectHearing().getSelectedLabel()));
    }

    @Test
    void addToBundlesCollection_addsCorrectReason() {
        List<GenericTypeItem<HearingBundleType>> collection = new ArrayList<>();
        englandCaseData.setBundlesClaimantCollection(collection);

        String claimantsDocumentsOnly = "Claimant's documents only";
        String witnessStatementsOnly = "Witness statements only";
        String butReason = "ButReason";
        String disagree = "Disagree";
        UploadedDocumentType file = UploadedDocumentType.builder().documentFilename("file.txt").build();
        setupEnglandCaseData(NO, butReason, disagree, claimantsDocumentsOnly, witnessStatementsOnly, file);

        bundlesClaimantService.addToBundlesCollection(englandCaseData);
        HearingBundleType actual = collection.get(0).getValue();
        String expectedAgreedDocsWith = "No, we have not agreed and I want to provide my own documents";

        assertThat(englandCaseData.getBundlesClaimantCollection(), is(collection));
        assertThat(actual.getAgreedDocWith(), is(expectedAgreedDocsWith));
        assertThat(actual.getWhatDocuments(), is(witnessStatementsOnly));
        assertThat(actual.getWhoseDocuments(), is(claimantsDocumentsOnly));
        assertThat(actual.getUploadFile(), is(file));
    }

    private void setupEnglandCaseData(String agreedDocWith, String butReason, String disagree,
                                      String claimantsDocumentsOnly, String witnessStatementsOnly,
                                      UploadedDocumentType file) {

        englandCaseData.setBundlesClaimantWhoseDocuments(claimantsDocumentsOnly);
        englandCaseData.setBundlesClaimantWhatDocuments(witnessStatementsOnly);
        englandCaseData.setBundlesClaimantSelectHearing(getTestSelectHearing());
        englandCaseData.setBundlesClaimantUploadFile(file);
        englandCaseData.setBundlesClaimantAgreedDocWith(agreedDocWith);
        englandCaseData.setBundlesClaimantAgreedDocWithBut(butReason);
        englandCaseData.setBundlesClaimantAgreedDocWithNo(disagree);
    }
}

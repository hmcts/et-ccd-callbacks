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
import uk.gov.hmcts.et.common.model.ccd.items.TypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.HearingBundleType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@ExtendWith(SpringExtension.class)
class BundlesRespondentServiceTest {

    private BundlesRespondentService bundlesRespondentService;
    private CaseData scotlandCaseData;
    private CaseData englandCaseData;
    private static final String VALID_TEXT = "valid text";
    private static final String EXCEED_CHAR_LIMIT_TEXT = "Life is a journey filled with "
            + "twists and turns, highs and lows. It's "
            + "a mosaic of experiences that shape us into who we are. Embrace the challenges, for "
            + "they are opportunities in disguise. Cherish the "
            + "moments of joy, for they are the fuel that keeps us going."
            + "In the tapestry of life, relationships are the threads that bind us together. "
            + "They come in various forms - family, "
            + "friends, mentors, and even chance encounters. Each connection adds a unique hue "
            + "to the canvas of our existence. Nurture these bonds,"
            + " for they hold the power to lift us up and carry us through the toughest of times."
            + "Remember to take a moment to appreciate the beauty around you. Nature, with its"
            + " breathtaking landscapes and intricate "
            + "ecosystems, reminds us of the wonders of the world. The laughter of children, "
            + "the warmth of a hug, the taste of a delicious meal "
            + "- these simple pleasures are the jewels that adorn the fabric of our days."
            + "Don't be afraid to dream, to set ambitious goals, and to chase after them with "
            + "unwavering determination. "
            + "The path to success may be fraught with obstacles, but with persistence and a "
            + "resilient spirit, you can overcome "
            + "anything that stands in your way."
            + "Self-care is not a luxury, but a necessity. Take the time to rest, to recharge,"
            + " and to nourish your body "
            + "and mind. Prioritize your well-being, for it is the cornerstone of a fulfilling life."
            + "In moments of doubt, remember that you are stronger than you think. You have the power to rise"
            + " above adversity and to emerge from challenges with newfound wisdom and strength. "
            + "Trust in yourself and your abilities."
            + "Finally, be kind. Kindness is a beacon of light in a sometimes dark world. It has the power to "
            + "heal wounds, to bridge divides, and to inspire hope. Practice it freely, and watch "
            + "as it ripples out, touching the lives"
            + " of those around you."
            + "So, as you navigate the intricate tapestry of life, remember to cherish t"
            + "he threads of connection,"
            + " to savor the colors of joy, and to weave your own story with purpose and intention. "
            + "You are the artist of your own masterpiece, "
            + "and the world is your canvas. Lorem ipsum dolor sit amet, consectetur adipiscing elit, "
            + "sed do eiusmod tempor incididunt ut labore et dolore magna aliqua."
            + " Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex "
            + "sed do eiusmod tempor incididunt ut labore et dolore magna aliqua."
            + " Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex "
            + "ea commodo consequat. Duis aute irure dolor in "
            + "reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur "
            + "sint occaecat cupidatat non proident, "
            + "sunt in culpa qui officia deserunt mollit anim id est laborum.";

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
    void validateTextAreaLength_noTooLong_returnsError() {
        englandCaseData.setBundlesRespondentAgreedDocWith("No");
        englandCaseData.setBundlesRespondentAgreedDocWithNo(EXCEED_CHAR_LIMIT_TEXT);
        List<String> errors = bundlesRespondentService.validateTextAreaLength(englandCaseData);
        assertThat(errors.size(), is(1));
        assertThat(errors.get(0), is("This field must be 2500 characters or less"));
    }

    @Test
    void validateTextAreaLength_butTooLong_returnsError() {
        englandCaseData.setBundlesRespondentAgreedDocWith("But");
        englandCaseData.setBundlesRespondentAgreedDocWithBut(EXCEED_CHAR_LIMIT_TEXT);
        List<String> errors = bundlesRespondentService.validateTextAreaLength(englandCaseData);
        assertThat(errors.size(), is(1));
        assertThat(errors.get(0), is("This field must be 2500 characters or less"));
    }

    @Test
    void validateTextAreaLength_validTextForNo_returnsNoError() {
        englandCaseData.setBundlesRespondentAgreedDocWith(NO);
        englandCaseData.setBundlesRespondentAgreedDocWithNo(VALID_TEXT);
        List<String> errors = bundlesRespondentService.validateTextAreaLength(englandCaseData);
        assertTrue(errors.isEmpty());
    }

    @Test
    void validateTextAreaLength_validTextForBut_returnsNoError() {
        englandCaseData.setBundlesRespondentAgreedDocWith("But");
        englandCaseData.setBundlesRespondentAgreedDocWithBut(VALID_TEXT);
        List<String> errors = bundlesRespondentService.validateTextAreaLength(englandCaseData);
        assertTrue(errors.isEmpty());
    }

    @Test
    void addToBundlesCollection_whenEmptyCollection_createsNewCollection() {
        englandCaseData.setBundlesRespondentCollection(null);
        englandCaseData.setBundlesRespondentSelectHearing(getTestSelectHearing());
        englandCaseData.setBundlesRespondentAgreedDocWith("Yes");
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
        List<TypeItem<HearingBundleType>> collection = new ArrayList<>();
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
        assertThat(actual.getFormattedSelectedHearing(),
                is(englandCaseData.getBundlesRespondentSelectHearing().getSelectedLabel()));
    }

    @Test
    void addToBundlesCollection_addsCorrectReason() {
        List<TypeItem<HearingBundleType>> collection = new ArrayList<>();
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
        englandCaseData.setBundlesRespondentAgreedDocWith(NO);
        englandCaseData.setBundlesRespondentAgreedDocWithBut(butReason);
        englandCaseData.setBundlesRespondentAgreedDocWithNo(disagree);

        bundlesRespondentService.addToBundlesCollection(englandCaseData);
        HearingBundleType actual = collection.get(0).getValue();
        String expectedAgreedDocsWith = "No, we have not agreed and I want to provide my own documents";

        assertThat(englandCaseData.getBundlesRespondentCollection(), is(collection));
        assertThat(actual.getAgreedDocWith(), is(expectedAgreedDocsWith));
    }
}

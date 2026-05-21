package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.CaseNote;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.MultipleUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.EUROPE_LONDON;

@ExtendWith(SpringExtension.class)
class CaseNotesServiceTest {
    private  static final String EMAIL = "email@email.com";
    private static final String NAME = "Mr Magoo";

    @MockitoBean
    private UserIdamService userIdamService;

    private CaseNotesService caseNotesService;
    private CaseNote caseNote;
    private String userToken;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        caseNotesService = new CaseNotesService(userIdamService);
        caseNote = CaseNote.builder()
                .title("Test note")
                .note("Will multiples ever get released?")
                .build();
        userToken = "authString";
        userDetails = new UserDetails();
        userDetails.setEmail(EMAIL);
        when(userIdamService.getUserDetails(userToken)).thenReturn(userDetails);
    }

    @Test
    void verifyCaseIsSetWithCaseNotes_emptyCollection_withName() {
        MultipleData multipleData = MultipleUtil.getMultipleData();
        multipleData.setCaseNote(caseNote);
        caseNotesService.addCaseNote(multipleData, userToken);
        userDetails.setName(NAME);

        List<GenericTypeItem<CaseNote>> caseNoteCollection = multipleData.getMultipleCaseNotesCollection();
        CaseNote createdNote = caseNoteCollection.getFirst().getValue();
        assertEquals(1, caseNoteCollection.size());
        assertEquals(createdNote, caseNote);
        assertEquals(EMAIL, createdNote.getAuthor());
        assertNotNull(createdNote.getDate());
        assertNull(multipleData.getCaseNote());
    }

    @Test
    void verifyCaseIsSetWithCaseNotes_emptyCollection_noName() {
        MultipleData multipleData = MultipleUtil.getMultipleData();
        multipleData.setCaseNote(caseNote);
        caseNotesService.addCaseNote(multipleData, userToken);

        List<GenericTypeItem<CaseNote>> caseNoteCollection = multipleData.getMultipleCaseNotesCollection();
        CaseNote createdNote = caseNoteCollection.getFirst().getValue();
        assertEquals(1, caseNoteCollection.size());
        assertEquals(createdNote, caseNote);
        assertEquals(EMAIL, createdNote.getAuthor());
        assertNotNull(createdNote.getDate());
        assertNull(multipleData.getCaseNote());
    }

    @Test
    void verifyCaseIsSetWithCaseNotes_nonEmptyCollection() {
        MultipleData multipleData = MultipleUtil.getMultipleData();
        multipleData.setCaseNote(caseNote);
        multipleData.setMultipleCaseNotesCollection(new ArrayList<>());
        multipleData.getMultipleCaseNotesCollection().add(GenericTypeItem.from(caseNote));

        caseNotesService.addCaseNote(multipleData, userToken);

        List<GenericTypeItem<CaseNote>> caseNoteCollection = multipleData.getMultipleCaseNotesCollection();
        assertEquals(2, caseNoteCollection.size());
        assertNotNull(caseNoteCollection.get(1).getValue().getDate());
        assertNull(multipleData.getCaseNote());
    }

    @Test
    void verifySingleCaseIsSetWithCaseNotes() {
        userDetails.setName(NAME);
        CaseData caseData = new CaseData();
        caseData.setAddCaseNote(caseNote);
        caseNotesService.addCaseNote(caseData, userToken);

        List<GenericTypeItem<CaseNote>> caseNoteCollection = caseData.getCaseNotesCollection();
        CaseNote createdNote = caseNoteCollection.getFirst().getValue();
        assertEquals(1, caseNoteCollection.size());
        assertEquals(createdNote, caseNote);
        assertEquals(NAME, createdNote.getAuthor());
        assertNotNull(createdNote.getDate());
        assertNull(caseData.getAddCaseNote());
    }

    @Test
    void verifyDateIsFormattedInEuropeLondonTimezone() throws ParseException {
        CaseData caseData = new CaseData();
        caseData.setAddCaseNote(caseNote);
        caseNotesService.addCaseNote(caseData, userToken);

        String dateStr = caseData.getCaseNotesCollection().getFirst().getValue().getDate();
        assertNotNull(dateStr);

        SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.ENGLISH);
        formatter.setTimeZone(TimeZone.getTimeZone(EUROPE_LONDON));
        Date parsedDate = formatter.parse(dateStr);

        ZonedDateTime londonNow = ZonedDateTime.now(ZoneId.of(EUROPE_LONDON));
        LocalDateTime parsedLocal = parsedDate.toInstant()
                .atZone(ZoneId.of(EUROPE_LONDON)).toLocalDateTime();

        // Parsed date should be within 1 minute of now in Europe/London time
        assertEquals(londonNow.getHour(), parsedLocal.getHour());
        assertEquals(londonNow.getMinute(), parsedLocal.getMinute());
    }

    @Test
    void verifySingleCaseIsSetWithCaseNotes_nullUserDetails() {
        when(userIdamService.getUserDetails(userToken)).thenReturn(null);
        CaseData caseData = new CaseData();
        caseData.setAddCaseNote(caseNote);
        assertDoesNotThrow(() -> caseNotesService.addCaseNote(caseData, userToken));

        List<GenericTypeItem<CaseNote>> caseNoteCollection = caseData.getCaseNotesCollection();
        assertEquals(1, caseNoteCollection.size());
        CaseNote createdNote = caseNoteCollection.getFirst().getValue();
        assertEquals(createdNote, caseNote);
        assertEquals("Unknown", createdNote.getAuthor());
        assertNotNull(createdNote.getDate());
        assertNull(caseData.getAddCaseNote());
    }

    @Test
    void populateCaseNoteList_returnsErrorMessage_whenNoCaseNotes() {
        CaseData caseData = new CaseData();
        List<String> result = caseNotesService.populateCaseNoteList(caseData);
        assertEquals(1, result.size());
        assertEquals("There are no telephone notes to edit or delete", result.getFirst());
    }

    @Test
    void populateCaseNoteList_populatesList_whenCaseNotesExist() {
        CaseData caseData = new CaseData();
        GenericTypeItem<CaseNote> note = GenericTypeItem.from(
            CaseNote.builder()
                .title("Title")
                .date(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .author("Random User").build());
        List<GenericTypeItem<CaseNote>> notes = new ArrayList<>();
        notes.add(note);
        caseData.setCaseNotesCollection(notes);
        List<String> result = caseNotesService.populateCaseNoteList(caseData);
        assertTrue(result.isEmpty());
        assertNotNull(caseData.getCaseNoteList());
        assertEquals(1, caseData.getCaseNoteList().getListItems().size());
    }

    @Test
    void populateEditCaseNote_throwsException_whenEditSelectedAndNoCaseNoteList() {
        CaseData caseData = new CaseData();
        caseData.setEditOrDeleteCaseNote("Edit");
        IllegalArgumentException ex =
            assertThrows(IllegalArgumentException.class, () -> caseNotesService.populateEditCaseNote(caseData));
        assertEquals("Selected note cannot be null or empty", ex.getMessage());
    }

    @Test
    void populateEditCaseNote_setsAddCaseNote_whenEditSelectedAndNoteExists() {
        CaseData caseData = new CaseData();
        GenericTypeItem<CaseNote> note = GenericTypeItem.from(
            CaseNote.builder()
                .title("Title")
                .date(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .author("Random User").build());
        note.setId("id1");
        List<GenericTypeItem<CaseNote>> notes = new ArrayList<>();
        notes.add(note);
        caseData.setCaseNotesCollection(notes);
        caseData.setEditOrDeleteCaseNote("Edit");
        DynamicValueType value = DynamicValueType.create("id1", "Title");
        caseData.setCaseNoteList(DynamicFixedListType.from(List.of(value)));
        caseData.getCaseNoteList().setValue(value);
        caseNotesService.populateEditCaseNote(caseData);
        assertNotNull(caseData.getAddCaseNote());
        assertEquals("Title", caseData.getAddCaseNote().getTitle());
    }

    @Test
    void populateEditCaseNote_throwsException_whenInvalidOption() {
        CaseData caseData = new CaseData();
        caseData.setEditOrDeleteCaseNote("Invalid");
        IllegalArgumentException ex =
            assertThrows(IllegalArgumentException.class, () -> caseNotesService.populateEditCaseNote(caseData));
        assertEquals("Invalid edit or delete case note option: Invalid", ex.getMessage());
    }

    @Test
    void submitCaseNoteUpdate_deletesNote_whenDeleteSelected() {
        CaseData caseData = new CaseData();
        GenericTypeItem<CaseNote> note1 = GenericTypeItem.from(
            CaseNote.builder()
                .title("Title")
                .date(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .author("Random User").build());
        note1.setId("id1");
        GenericTypeItem<CaseNote> note2 = GenericTypeItem.from(
            CaseNote.builder()
                .title("Title 2")
                .date(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .author("Random User").build());
        note2.setId("id2");
        caseData.setCaseNotesCollection(new ArrayList<>(List.of(note1, note2)));
        caseData.setEditOrDeleteCaseNote("Delete");
        DynamicValueType value = DynamicValueType.create("id1", "Title");
        caseData.setCaseNoteList(DynamicFixedListType.from(List.of(value)));
        caseData.getCaseNoteList().setValue(value);
        caseNotesService.submitCaseNoteUpdate(caseData);
        assertEquals(1, caseData.getCaseNotesCollection().size());
        assertEquals("id2", caseData.getCaseNotesCollection().getFirst().getId());
        assertNull(caseData.getCaseNoteList());
        assertNull(caseData.getAddCaseNote());
        assertNull(caseData.getEditOrDeleteCaseNote());
    }

    @Test
    void submitCaseNoteUpdate_editsNote_whenEditSelected() {
        CaseData caseData = new CaseData();
        GenericTypeItem<CaseNote> note = GenericTypeItem.from(
            CaseNote.builder()
                .title("Title")
                .note("old note")
                .date(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .author("Random User").build());
        note.setId("id1");
        caseData.setCaseNotesCollection(new ArrayList<>(List.of(note)));
        caseData.setEditOrDeleteCaseNote("Edit");
        DynamicValueType value = DynamicValueType.create("id1", "Title");
        caseData.setCaseNoteList(DynamicFixedListType.from(List.of(value)));
        caseData.getCaseNoteList().setValue(value);
        CaseNote updated = CaseNote.builder().title("updated title").note("updated note").build();
        caseData.setAddCaseNote(updated);
        caseNotesService.submitCaseNoteUpdate(caseData);
        assertEquals("updated title", note.getValue().getTitle());
        assertEquals("updated note", note.getValue().getNote());
        assertNull(caseData.getCaseNoteList());
        assertNull(caseData.getAddCaseNote());
        assertNull(caseData.getEditOrDeleteCaseNote());
    }

    @Test
    void submitCaseNoteUpdate_throwsException_whenInvalidOption() {
        CaseData caseData = new CaseData();
        caseData.setEditOrDeleteCaseNote("Invalid");
        IllegalArgumentException ex =
            assertThrows(IllegalArgumentException.class, () -> caseNotesService.submitCaseNoteUpdate(caseData));
        assertEquals("Invalid edit or delete case note option: Invalid", ex.getMessage());
    }

}

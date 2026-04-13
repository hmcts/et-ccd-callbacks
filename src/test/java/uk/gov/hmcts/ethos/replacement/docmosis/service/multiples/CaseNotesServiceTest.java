package uk.gov.hmcts.ethos.replacement.docmosis.service.multiples;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.CaseNote;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.MultipleUtil;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseNotesService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.UserIdamService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.EUROPE_LONDON;

@ExtendWith(SpringExtension.class)
class CaseNotesServiceTest {
    private  static final String EMAIL = "email@email.com";
    private static final String NAME = "Mr Magoo";

    @MockBean
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
}

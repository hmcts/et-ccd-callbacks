package uk.gov.hmcts.ethos.replacement.docmosis.service.multiples;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.CaseNote;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.MultipleUtil;
import uk.gov.hmcts.ethos.replacement.docmosis.service.UserIdamService;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

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
    public void setUp() {
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

        var caseNoteCollection = multipleData.getMultipleCaseNotesCollection();
        CaseNote createdNote = caseNoteCollection.get(0).getValue();
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

        var caseNoteCollection = multipleData.getMultipleCaseNotesCollection();
        CaseNote createdNote = caseNoteCollection.get(0).getValue();
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

        var caseNoteCollection = multipleData.getMultipleCaseNotesCollection();
        assertEquals(2, caseNoteCollection.size());
        assertNotNull(caseNoteCollection.get(1).getValue().getDate());
        assertNull(multipleData.getCaseNote());

    }
}

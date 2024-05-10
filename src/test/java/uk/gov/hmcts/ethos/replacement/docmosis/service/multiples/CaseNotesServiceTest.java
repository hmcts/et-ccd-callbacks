package uk.gov.hmcts.ethos.replacement.docmosis.service.multiples;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.CaseNote;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.MultipleUtil;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(SpringExtension.class)
class CaseNotesServiceTest {
    private CaseNotesService caseNotesService;
    private CaseNote caseNote;

    @BeforeEach
    public void setUp() {
        caseNotesService = new CaseNotesService();
        caseNote = CaseNote.builder()
                .title("Test note")
                .note("Will multiples ever get released?")
                .author("Mr Magoo")
                .build();
    }

    @Test
    void verifyCaseIsSetWithCaseNotes_emptyCollection() {
        MultipleData multipleData = MultipleUtil.getMultipleData();
        multipleData.setCaseNote(caseNote);
        caseNotesService.addCaseNote(multipleData);

        var caseNoteCollection = multipleData.getMultipleCaseNotesCollection();
        assertEquals(1, caseNoteCollection.size());
        assertEquals(caseNoteCollection.get(0).getValue(), caseNote);
        assertNotNull(caseNoteCollection.get(0).getValue().getDate());
        assertNull(multipleData.getCaseNote());

    }

    @Test
    void verifyCaseIsSetWithCaseNotes_nonEmptyCollection() {
        MultipleData multipleData = MultipleUtil.getMultipleData();
        multipleData.setCaseNote(caseNote);
        multipleData.setMultipleCaseNotesCollection(new ArrayList<>());
        multipleData.getMultipleCaseNotesCollection().add(GenericTypeItem.from(caseNote));

        caseNotesService.addCaseNote(multipleData);

        var caseNoteCollection = multipleData.getMultipleCaseNotesCollection();
        assertEquals(2, caseNoteCollection.size());
        assertNotNull(caseNoteCollection.get(1).getValue().getDate());
        assertNull(multipleData.getCaseNote());

    }
}

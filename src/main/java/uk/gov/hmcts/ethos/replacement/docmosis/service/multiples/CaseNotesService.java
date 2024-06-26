package uk.gov.hmcts.ethos.replacement.docmosis.service.multiples;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.CaseNote;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.ethos.replacement.docmosis.service.UserIdamService;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import static java.util.UUID.randomUUID;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

@Slf4j
@RequiredArgsConstructor
@Service("caseNotesService")
public class CaseNotesService {
    private static final String DATE_FORMAT = "dd-MMM-yyyy HH:mm";

    private final UserIdamService userIdamService;

    /**
     * Adds case note to collection with date stamp and clears the form.
     *
     * @param multipleData multiple data
     */
    public void addCaseNote(MultipleData multipleData, String userToken) {
        log.info("Adding Case Note");
        CaseNote caseNote = multipleData.getCaseNote();

        DateFormat formatter = new SimpleDateFormat(DATE_FORMAT, Locale.ENGLISH);
        caseNote.setDate(formatter.format(new Date()));

        UserDetails userDetails = userIdamService.getUserDetails(userToken);
        String author = isNotEmpty(userDetails.getName()) ? userDetails.getName() : userDetails.getEmail();
        caseNote.setAuthor(author);

        if (multipleData.getMultipleCaseNotesCollection() == null) {
            multipleData.setMultipleCaseNotesCollection(new ArrayList<>());
        }
        GenericTypeItem<CaseNote> caseNoteListTypeItem = new GenericTypeItem<>();
        caseNoteListTypeItem.setId(String.valueOf(randomUUID()));
        caseNoteListTypeItem.setValue(caseNote);
        multipleData.getMultipleCaseNotesCollection().add(caseNoteListTypeItem);

        multipleData.setCaseNote(null);
    }
}

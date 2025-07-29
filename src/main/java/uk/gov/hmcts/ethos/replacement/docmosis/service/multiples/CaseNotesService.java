package uk.gov.hmcts.ethos.replacement.docmosis.service.multiples;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
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

/**
 * Service for managing case notes in multiple data and case data.
 */
@Slf4j
@RequiredArgsConstructor
@Service("caseNotesService")
public class CaseNotesService {
    private final UserIdamService userIdamService;

    /**
     * Adds a case note to the multiple data.
     *
     * @param multipleData the multiple data to which the case note will be added
     * @param userToken    the user token for authentication
     */
    public void addCaseNote(MultipleData multipleData, String userToken) {
        CaseNote caseNote = multipleData.getCaseNote();
        GenericTypeItem<CaseNote> caseNoteListTypeItem = getCaseNoteGenericTypeItem(userToken, caseNote);
        if (multipleData.getMultipleCaseNotesCollection() == null) {
            multipleData.setMultipleCaseNotesCollection(new ArrayList<>());
        }
        multipleData.getMultipleCaseNotesCollection().add(caseNoteListTypeItem);
        multipleData.setCaseNote(null);
    }

    /**
     * Adds a case note to the case data.
     *
     * @param caseData  the case data to which the case note will be added
     * @param userToken the user token for authentication
     */
    public void addCaseNote(CaseData caseData, String userToken) {
        CaseNote caseNote = caseData.getAddCaseNote();

        GenericTypeItem<CaseNote> caseNoteListTypeItem = getCaseNoteGenericTypeItem(userToken, caseNote);

        if (caseData.getCaseNotesCollection() == null) {
            caseData.setCaseNotesCollection(new ArrayList<>());
        }
        caseData.getCaseNotesCollection().add(caseNoteListTypeItem);
        caseData.setAddCaseNote(null);
    }

    private GenericTypeItem<CaseNote> getCaseNoteGenericTypeItem(String userToken, CaseNote caseNote) {
        DateFormat formatter = new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.ENGLISH);
        caseNote.setDate(formatter.format(new Date()));

        UserDetails userDetails = userIdamService.getUserDetails(userToken);
        caseNote.setAuthor(isNotEmpty(userDetails.getName()) ? userDetails.getName() : userDetails.getEmail());

        GenericTypeItem<CaseNote> caseNoteListTypeItem = new GenericTypeItem<>();
        caseNoteListTypeItem.setId(String.valueOf(randomUUID()));
        caseNoteListTypeItem.setValue(caseNote);
        return caseNoteListTypeItem;
    }
}

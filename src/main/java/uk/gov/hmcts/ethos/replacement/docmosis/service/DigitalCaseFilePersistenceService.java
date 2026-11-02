package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.bundle.Bundle;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.DigitalCaseFileType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.ccd.DigitalCaseFile;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.ccd.DigitalCaseFileRepository;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.DigitalCaseFileHelper;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DigitalCaseFilePersistenceService {

    private final DigitalCaseFileRepository digitalCaseFileRepository;

    public void start(long caseReference, CaseData caseData) {
        Bundle bundle = caseData.getCaseBundles().getFirst();
        digitalCaseFileRepository.save(DigitalCaseFile.create(
            caseReference,
            caseData.getDigitalCaseFile(),
            UUID.fromString(bundle.value().getId())
        ));
    }

    public void save(long caseReference, DigitalCaseFileType digitalCaseFile) {
        digitalCaseFileRepository.save(DigitalCaseFile.create(caseReference, digitalCaseFile, null));
    }

    public void complete(long caseReference, CaseData caseData) {
        Bundle completedBundle = DigitalCaseFileHelper.findStitchedBundle(caseData).orElse(null);
        if (completedBundle == null) {
            return;
        }
        DigitalCaseFileHelper.addDcfToDocumentCollection(caseData, completedBundle);
        digitalCaseFileRepository.save(
            DigitalCaseFile.create(caseReference, caseData.getDigitalCaseFile(), null)
        );
    }
}

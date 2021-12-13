package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.ecm.common.model.ccd.CaseData;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.ecm.common.model.multiples.MultipleData;
import uk.gov.hmcts.ethos.replacement.docmosis.service.referencedata.FileLocationService;

@Service
public class FileLocationSelectionService {
    private final FileLocationService fileLocationService;

    public FileLocationSelectionService(FileLocationService fileLocationService) {
        this.fileLocationService = fileLocationService;
    }

    public void initialiseFileLocation(CaseData caseData) {
        caseData.setFileLocation(createFileLocations(caseData.getManagingOffice(), caseData.getFileLocation()));
    }

    public void initialiseFileLocation(MultipleData multipleData) {
        multipleData.setFileLocation(createFileLocations(multipleData.getManagingOffice(),
                multipleData.getFileLocation()));
    }

    private DynamicFixedListType createFileLocations(String owningOffice, DynamicFixedListType existingFileLocations) {
        var fileLocations = fileLocationService.getFileLocations(TribunalOffice.valueOfOfficeName(owningOffice));
        return DynamicFixedListType.from(fileLocations, existingFileLocations);
    }
}

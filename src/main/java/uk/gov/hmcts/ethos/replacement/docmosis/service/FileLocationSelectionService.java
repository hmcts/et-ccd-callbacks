package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.ethos.replacement.docmosis.service.referencedata.FileLocationService;

import java.util.List;

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
        List<DynamicValueType> fileLocations = fileLocationService.getFileLocations(
            TribunalOffice.valueOfOfficeName(owningOffice));
        return DynamicFixedListType.from(fileLocations, existingFileLocations);
    }
}

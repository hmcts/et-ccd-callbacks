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
public class ScotlandFileLocationSelectionService {
    private final FileLocationService fileLocationService;

    public ScotlandFileLocationSelectionService(FileLocationService fileLocationService) {
        this.fileLocationService = fileLocationService;
    }

    public void initialiseFileLocation(CaseData caseData) {
        caseData.setFileLocationAberdeen(createFileLocations(TribunalOffice.ABERDEEN.getOfficeName(),
                caseData.getFileLocationAberdeen()));
        caseData.setFileLocationDundee(createFileLocations(TribunalOffice.DUNDEE.getOfficeName(),
                caseData.getFileLocationDundee()));
        caseData.setFileLocationEdinburgh(createFileLocations(TribunalOffice.EDINBURGH.getOfficeName(),
                caseData.getFileLocationEdinburgh()));
        caseData.setFileLocationGlasgow(createFileLocations(TribunalOffice.GLASGOW.getOfficeName(),
                caseData.getFileLocationGlasgow()));
    }

    public void initialiseFileLocation(MultipleData multipleData) {
        multipleData.setFileLocationAberdeen(createFileLocations(TribunalOffice.ABERDEEN.getOfficeName(),
                multipleData.getFileLocationAberdeen()));
        multipleData.setFileLocationDundee(createFileLocations(TribunalOffice.DUNDEE.getOfficeName(),
                multipleData.getFileLocationDundee()));
        multipleData.setFileLocationEdinburgh(createFileLocations(TribunalOffice.EDINBURGH.getOfficeName(),
                multipleData.getFileLocationEdinburgh()));
        multipleData.setFileLocationGlasgow(createFileLocations(TribunalOffice.GLASGOW.getOfficeName(),
                multipleData.getFileLocationGlasgow()));
    }

    private DynamicFixedListType createFileLocations(String owningOffice, DynamicFixedListType existingFileLocations) {
        List<DynamicValueType> fileLocations = fileLocationService.getFileLocations(
            TribunalOffice.valueOfOfficeName(owningOffice));
        return DynamicFixedListType.from(fileLocations, existingFileLocations);
    }
}

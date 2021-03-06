package uk.gov.hmcts.ethos.replacement.docmosis.service.referencedata;

import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;

import java.util.List;

public interface FileLocationService {
    List<DynamicValueType> getFileLocations(TribunalOffice tribunalOffice);
}

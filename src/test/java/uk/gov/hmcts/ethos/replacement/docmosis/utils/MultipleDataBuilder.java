package uk.gov.hmcts.ethos.replacement.docmosis.utils;

import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.et.common.model.multiples.CaseImporterFile;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.et.common.model.multiples.MultipleDetails;
import uk.gov.hmcts.et.common.model.multiples.MultipleRequest;

public class MultipleDataBuilder {

    private final MultipleData multipleData = new MultipleData();

    public static MultipleDataBuilder builder() {
        return new MultipleDataBuilder();
    }

    public MultipleDataBuilder withMultipleReference(String multipleReference) {
        multipleData.setMultipleReference(multipleReference);
        return this;
    }

    public MultipleDataBuilder withManagingOffice(String managingOffice) {
        multipleData.setManagingOffice(managingOffice);
        return this;
    }

    public MultipleDataBuilder withCaseImporterFile(String documentBinaryUrl) {
        var uploadedDocument = new UploadedDocumentType();
        uploadedDocument.setDocumentBinaryUrl(documentBinaryUrl);
        var caseImporterFile = new CaseImporterFile();
        caseImporterFile.setUploadedDocument(uploadedDocument);
        multipleData.setCaseImporterFile(caseImporterFile);

        return this;
    }

    public MultipleDataBuilder withCaseTransfer(String officeCT, String reasonForCT) {
        multipleData.setOfficeMultipleCT(DynamicFixedListType.of(DynamicValueType.create(officeCT, officeCT)));
        multipleData.setReasonForCT(reasonForCT);

        return this;
    }

    public MultipleRequest buildAsMultipleRequest() {
        var multipleDetails = new MultipleDetails();
        multipleDetails.setCaseData(multipleData);
        var multipleRequest = new MultipleRequest();
        multipleRequest.setCaseDetails(multipleDetails);

        return multipleRequest;
    }

    public MultipleDetails buildAsMultipleDetails(String caseId, String caseTypeId, String jurisdiction) {
        var multipleDetails = new MultipleDetails();
        multipleDetails.setCaseId(caseId);
        multipleDetails.setCaseTypeId(caseTypeId);
        multipleDetails.setJurisdiction(jurisdiction);
        multipleDetails.setCaseData(multipleData);

        return multipleDetails;
    }
}

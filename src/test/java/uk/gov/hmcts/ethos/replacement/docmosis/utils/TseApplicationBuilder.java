package uk.gov.hmcts.ethos.replacement.docmosis.utils;

import org.elasticsearch.core.List;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationType;
import uk.gov.hmcts.et.common.model.ccd.items.TseRespondentReplyTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.TseRespondentReplyType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;

import java.util.UUID;

/**
 * Contains helper methods to build a GenericTseApplicationType object. Each method returns an instance of itself to aid with
 * chaining method calls to build the object.
 */
public class TseApplicationBuilder {
    private final GenericTseApplicationType genericTseApplicationType = new GenericTseApplicationType();

    public static TseApplicationBuilder builder() {
        return new TseApplicationBuilder();
    }

    public GenericTseApplicationType build() {
        return genericTseApplicationType;
    }

    public TseApplicationBuilder withNumber(String number) {
        genericTseApplicationType.setNumber(number);
        return this;
    }

    public TseApplicationBuilder withType(String type) {
        genericTseApplicationType.setType(type);
        return this;
    }

    public TseApplicationBuilder withApplicant(String applicant) {
        genericTseApplicationType.setApplicant(applicant);
        return this;
    }

    public TseApplicationBuilder withDate(String date) {
        genericTseApplicationType.setDate(date);
        return this;
    }

    public TseApplicationBuilder withDocumentUpload(UploadedDocumentType documentUpload) {
        genericTseApplicationType.setDocumentUpload(documentUpload);
        return this;
    }

    public TseApplicationBuilder withDocumentUpload(String filename, String uuid) {
        genericTseApplicationType.setDocumentUpload(
            UploadedDocumentBuilder.builder().withFilename(filename).withUuid(uuid).build()
        );
        return this;
    }

    public TseApplicationBuilder withDetails(String details) {
        genericTseApplicationType.setDetails(details);
        return this;
    }

    public TseApplicationBuilder withCopyToOtherPartyYesOrNo(String copyToOtherPartyYesOrNo) {
        genericTseApplicationType.setCopyToOtherPartyYesOrNo(copyToOtherPartyYesOrNo);
        return this;
    }

    public TseApplicationBuilder withCopyToOtherPartyText(String copyToOtherPartyText) {
        genericTseApplicationType.setCopyToOtherPartyText(copyToOtherPartyText);
        return this;
    }

    public TseApplicationBuilder withDue(String due) {
        genericTseApplicationType.setDue(due);
        return this;
    }

    public TseApplicationBuilder withResponsesCount(String responsesCount) {
        genericTseApplicationType.setResponsesCount(responsesCount);
        return this;
    }

    public TseApplicationBuilder withReply(TseRespondentReplyType reply) {
        TseRespondentReplyTypeItem tseRespondentReplyTypeItem = new TseRespondentReplyTypeItem();
        tseRespondentReplyTypeItem.setId(UUID.randomUUID().toString());
        tseRespondentReplyTypeItem.setValue(reply);
        genericTseApplicationType.setRespondentReply(List.of(tseRespondentReplyTypeItem));
        return this;
    }

    public TseApplicationBuilder withStatus(String status) {
        genericTseApplicationType.setStatus(status);
        return this;
    }
}
package uk.gov.hmcts.ethos.replacement.docmosis.utils;

import org.elasticsearch.core.List;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.TseRespondentReplyType;

import java.util.UUID;

/**
 * Contains helper methods to build a TseRespondentReplyType object. Each method returns an instance of itself for
 * chaining method calls to build the object.
 */
public class TseRespondentReplyBuilder {
    private final TseRespondentReplyType tseRespondentReplyType = new TseRespondentReplyType();

    public static TseRespondentReplyBuilder builder() {
        return new TseRespondentReplyBuilder();
    }

    public TseRespondentReplyType build() {
        tseRespondentReplyType.setFrom("Respondent");
        return tseRespondentReplyType;
    }

    public TseRespondentReplyBuilder withDate(String date) {
        tseRespondentReplyType.setDate(date);
        return this;
    }

    public TseRespondentReplyBuilder withResponse(String response) {
        tseRespondentReplyType.setResponse(response);
        return this;
    }

    public TseRespondentReplyBuilder withHasSupportingMaterial(String hasSupportingMaterial) {
        tseRespondentReplyType.setHasSupportingMaterial(hasSupportingMaterial);
        return this;
    }

    public TseRespondentReplyBuilder withSupportingMaterial(String fileName, String uuid, String description) {
        DocumentTypeItem documentTypeItem = new DocumentTypeItem();
        DocumentType documentType = DocumentTypeBuilder.builder()
            .withUploadedDocument(fileName, uuid)
            .withShortDescription(description)
            .build();
        documentTypeItem.setValue(documentType);
        documentTypeItem.setId(UUID.randomUUID().toString());
        tseRespondentReplyType.setSupportingMaterial(List.of(documentTypeItem));
        return this;
    }

    public TseRespondentReplyBuilder withCopyToOtherParty(String copyToOtherParty) {
        tseRespondentReplyType.setCopyToOtherParty(copyToOtherParty);
        return this;
    }

    public TseRespondentReplyBuilder withCopyNoGiveDetails(String copyNoGiveDetails) {
        tseRespondentReplyType.setCopyNoGiveDetails(copyNoGiveDetails);
        return this;
    }
}
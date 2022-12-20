package uk.gov.hmcts.ethos.replacement.docmosis.utils;

import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationType;

/**
 * Contains helper methods to build a GenericTseApplicationType object. Each method returns an instance of itself for
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

    public TseApplicationBuilder withDetails(String details) {
        genericTseApplicationType.setDetails(details);
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

    public TseApplicationBuilder withStatus(String status) {
        genericTseApplicationType.setStatus(status);
        return this;
    }
}
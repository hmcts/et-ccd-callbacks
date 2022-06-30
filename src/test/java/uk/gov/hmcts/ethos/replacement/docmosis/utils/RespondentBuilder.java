package uk.gov.hmcts.ethos.replacement.docmosis.utils;

import uk.gov.hmcts.et.common.model.ccd.Address;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

/**
 * Contains helper methods to build a respondent object. Each method returns an instance of itself to aid with
 * chaining method calls to build the object.
 */
public class RespondentBuilder {
    private final RespondentSumType respondent = new RespondentSumType();

    public static RespondentBuilder builder() {
        return new RespondentBuilder();
    }

    public RespondentSumType build() {
        return respondent;
    }

    public RespondentBuilder withExtension(String requested, String granted, String date) {
        respondent.setExtensionRequested(requested);
        respondent.setExtensionGranted(granted);
        respondent.setExtensionDate(date);
        return this;
    }

    public RespondentBuilder withExtension() {
        return withExtension(YES, YES, "2022-03-01");
    }

    public RespondentBuilder withName(String name) {
        respondent.setRespondentName(name);
        return this;
    }

    public RespondentBuilder withReceived(String responseReceived, String receivedDate) {
        respondent.setResponseReceived(responseReceived);
        respondent.setResponseReceivedDate(receivedDate);
        return this;
    }

    public RespondentBuilder withAddress(String addressLine1, String addressLine2, String addressLine3,
                                         String postTown, String postcode) {
        Address address = new Address();
        address.setAddressLine1(addressLine1);
        address.setAddressLine2(addressLine2);
        address.setAddressLine3(addressLine3);
        address.setPostTown(postTown);
        address.setPostCode(postcode);

        respondent.setRespondentAddress(address);
        return this;
    }

    public RespondentBuilder withET3ResponseRespondentAddress(String addressLine1, String addressLine2,
                                                              String addressLine3, String postTown, String postcode) {
        Address address = new Address();
        address.setAddressLine1(addressLine1);
        address.setAddressLine2(addressLine2);
        address.setAddressLine3(addressLine3);
        address.setPostTown(postTown);
        address.setPostCode(postcode);

        respondent.setResponseRespondentAddress(address);
        return this;
    }

    public RespondentBuilder withET3ResponseRespondentName(String name) {
        respondent.setResponseRespondentName(name);
        return this;
    }
}
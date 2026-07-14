package uk.gov.hmcts.et.common.model.listing;

import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.et.common.model.ccd.EnglandWalesDefinition;
import uk.gov.hmcts.et.common.model.ccd.ListingAccess;
import uk.gov.hmcts.et.common.model.ccd.ScotlandDefinition;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.ccd.Address;
import uk.gov.hmcts.et.common.model.listing.items.AdhocReportTypeItem;
import uk.gov.hmcts.et.common.model.listing.items.BFDateTypeItem;
import uk.gov.hmcts.et.common.model.listing.items.ListingTypeItem;
import uk.gov.hmcts.et.common.model.listing.types.AdhocReportType;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@CCD(access = ListingAccess.class)
public class ListingData {

    @CCD(label = "Correspondence Address", typeOverride = FieldType.AddressUK)
    @JsonProperty("tribunalCorrespondenceAddress")
    private Address tribunalCorrespondenceAddress;
    @CCD(label = "Correspondence Telephone")
    @JsonProperty("tribunalCorrespondenceTelephone")
    private String tribunalCorrespondenceTelephone;
    @CCD(label = "Correspondence Fax")
    @JsonProperty("tribunalCorrespondenceFax")
    private String tribunalCorrespondenceFax;
    @CCD(label = "Correspondence DX")
    @JsonProperty("tribunalCorrespondenceDX")
    private String tribunalCorrespondenceDX;
    @CCD(label = "Correspondence Email")
    @JsonProperty("tribunalCorrespondenceEmail")
    private String tribunalCorrespondenceEmail;
    @CCD(label = "Enter today's date", typeOverride = FieldType.Date)
    @JsonProperty("reportDate")
    private String reportDate;
    @CCD(label = "Single or Range", typeOverride = FieldType.FixedRadioList, typeParameterOverride = "fl_HearingDateType")
    @JsonProperty("hearingDateType")
    private String hearingDateType;
    @CCD(label = "Date", typeOverride = FieldType.Date)
    @JsonProperty("listingDate")
    private String listingDate;
    @CCD(label = "From", typeOverride = FieldType.Date)
    @JsonProperty("listingDateFrom")
    private String listingDateFrom;
    @CCD(label = "To", typeOverride = FieldType.Date)
    @JsonProperty("listingDateTo")
    private String listingDateTo;
    @CCD(label = "Venue", typeOverride = FieldType.DynamicList)
    @JsonProperty("listingVenue")
    private DynamicFixedListType listingVenue;
    @CCD(ignore = true)
    @JsonProperty("listingVenueScotland")
    private String listingVenueScotland;
    @CCD(label = "Daily Cause List")
    @JsonProperty("listingCollection")
    private List<ListingTypeItem> listingCollection;
    @CCD(label = "List for ${listingDate}", typeOverride = FieldType.Label)
    @JsonProperty("listingLabel")
    private String listingLabel;
    @CCD(label = "List for ${listingDateFrom} to ${listingDateTo}", typeOverride = FieldType.Label)
    @JsonProperty("listingLabelRange")
    private String listingLabelRange;
    @CCD(label = "Hearing Venue", typeOverride = FieldType.DynamicList, includeInProfiles = ScotlandDefinition.class)
    @JsonProperty("listingVenueOfficeGlas")
    private String listingVenueOfficeGlas;
    @CCD(label = "Hearing Venue", typeOverride = FieldType.DynamicList, includeInProfiles = ScotlandDefinition.class)
    @JsonProperty("listingVenueOfficeAber")
    private String listingVenueOfficeAber;
    @CCD(label = "Hearing Venue", typeOverride = FieldType.DynamicList, includeInProfiles = ScotlandDefinition.class)
    @JsonProperty("venueGlasgow")
    private DynamicFixedListType venueGlasgow;
    @CCD(label = "Hearing Venue", typeOverride = FieldType.DynamicList, includeInProfiles = ScotlandDefinition.class)
    @JsonProperty("venueAberdeen")
    private DynamicFixedListType venueAberdeen;
    @CCD(label = "Hearing Venue", typeOverride = FieldType.DynamicList, includeInProfiles = ScotlandDefinition.class)
    @JsonProperty("venueDundee")
    private DynamicFixedListType venueDundee;
    @CCD(label = "Hearing Venue", typeOverride = FieldType.DynamicList, includeInProfiles = ScotlandDefinition.class)
    @JsonProperty("venueEdinburgh")
    private DynamicFixedListType venueEdinburgh;
    @CCD(label = "Hearing Document", typeOverride = FieldType.FixedList, typeParameterOverride = "fl_HearingDocType")
    @JsonProperty("hearingDocType")
    private String hearingDocType;
    @CCD(label = "Type", typeOverride = FieldType.FixedRadioList, typeParameterOverride = "fl_HearingDocETCL")
    @JsonProperty("hearingDocETCL")
    private String hearingDocETCL;
    @CCD(label = "Split by room", typeOverride = FieldType.YesOrNo)
    @JsonProperty("roomOrNoRoom")
    private String roomOrNoRoom;
    @CCD(id = "docMarkUp ", authorisationId = "docMarkUp", label = "Doc MarkUp")
    @JsonProperty("docMarkUp")
    private String docMarkUp;
    @CCD(label = " ")
    @JsonProperty("bfDateCollection")
    private List<BFDateTypeItem> bfDateCollection;
    @CCD(label = "Clerk", typeOverride = FieldType.DynamicList)
    @JsonProperty("clerkResponsible")
    private DynamicFixedListType clerkResponsible;
    @CCD(label = "Type", typeOverride = FieldType.FixedList, typeParameterOverride = "fl_reportCaseType")
    @JsonProperty("reportType")
    private String reportType;
    @CCD(label = "Document Name")
    @JsonProperty("documentName")
    private String documentName;
    @CCD(label = "Show all cases?", hint = "Includes Settled, Withdrawn, Postponed and Vacated", typeOverride = FieldType.YesOrNo)
    @JsonProperty("showAll")
    private String showAll;

    @CCD(label = " ")
    @JsonProperty("localReportsSummaryHdr")
    private AdhocReportType localReportsSummaryHdr;
    @CCD(label = " ")
    @JsonProperty("localReportsSummary")
    private List<AdhocReportTypeItem> localReportsSummary;
    @CCD(label = " ")
    @JsonProperty("localReportsSummaryHdr2")
    private AdhocReportType localReportsSummaryHdr2;
    @CCD(label = " ")
    @JsonProperty("localReportsSummary2")
    private List<AdhocReportTypeItem> localReportsSummary2;
    @CCD(label = " ")
    @JsonProperty("localReportsDetailHdr")
    private AdhocReportType localReportsDetailHdr;
    @CCD(label = " ")
    @JsonProperty("localReportsDetail")
    private List<AdhocReportTypeItem> localReportsDetail;
    @CCD(label = "Tribunal Office", typeOverride = FieldType.FixedList, typeParameterOverride = "fl_TribunalOffice", includeInProfiles = EnglandWalesDefinition.class)
    @CCD(label = "Managing Office", typeOverride = FieldType.FixedList, typeParameterOverride = "VenueScotlandAll", includeInProfiles = ScotlandDefinition.class)
    @JsonProperty("managingOffice")
    private String managingOffice;

    public void clearReportFields() {
        listingVenueOfficeAber = null;
        listingVenueOfficeGlas = null;
        venueGlasgow = null;
        venueAberdeen = null;
        venueDundee = null;
        venueEdinburgh = null;
        clerkResponsible = null;
    }

    public boolean hasListingVenue() {
        return listingVenue != null && listingVenue.getValue() != null;
    }
}

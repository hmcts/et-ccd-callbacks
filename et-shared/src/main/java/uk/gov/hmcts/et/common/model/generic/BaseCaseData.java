package uk.gov.hmcts.et.common.model.generic;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bundle.Bundle;
import uk.gov.hmcts.et.common.model.ccd.EnglandWalesMultipleCftlibDefinition;
import uk.gov.hmcts.et.common.model.ccd.EnglandWalesMultipleProdDefinition;
import uk.gov.hmcts.et.common.model.ccd.MultipleAccess;
import uk.gov.hmcts.et.common.model.ccd.ScotlandMultipleCftlibDefinition;
import uk.gov.hmcts.et.common.model.ccd.ScotlandMultipleProdDefinition;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.ReferralTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.CaseLocation;
import uk.gov.hmcts.et.common.model.ccd.types.DigitalCaseFileType;

import java.util.List;

/** Data common to single and multiple cases. */
@Getter
@Setter
public class BaseCaseData {
    // Referral
    @JsonProperty("referralCollection")
    private List<ReferralTypeItem> referralCollection;

    @JsonProperty("referralHearingDetails")
    private String referralHearingDetails;

    @JsonProperty("selectReferral")
    private DynamicFixedListType selectReferral;

    // Referral Type
    @JsonProperty("referCaseTo")
    private String referCaseTo;

    @JsonProperty("referentEmail")
    private String referentEmail;

    @JsonProperty("isUrgent")
    private String isUrgent;

    @JsonProperty("referralSubject")
    private String referralSubject;

    @JsonProperty("referralSubjectSpecify")
    private String referralSubjectSpecify;

    @JsonProperty("referralDetails")
    private String referralDetails;

    @JsonProperty("referralDocument")
    private List<DocumentTypeItem> referralDocument;

    @JsonProperty("referralInstruction")
    private String referralInstruction;

    @JsonProperty("referredBy")
    @CCD(
            excludeFromProfiles = {
                EnglandWalesMultipleCftlibDefinition.class,
                ScotlandMultipleCftlibDefinition.class,
                EnglandWalesMultipleProdDefinition.class,
                ScotlandMultipleProdDefinition.class
            })
    private String referredBy;

    @JsonProperty("referralDate")
    @CCD(
            excludeFromProfiles = {
                EnglandWalesMultipleCftlibDefinition.class,
                ScotlandMultipleCftlibDefinition.class,
                EnglandWalesMultipleProdDefinition.class,
                ScotlandMultipleProdDefinition.class
            })
    private String referralDate;

    // Referral Update
    @JsonProperty("updateReferralNumber")
    @CCD(
            excludeFromProfiles = {
                EnglandWalesMultipleCftlibDefinition.class,
                ScotlandMultipleCftlibDefinition.class,
                EnglandWalesMultipleProdDefinition.class,
                ScotlandMultipleProdDefinition.class
            })
    private String updateReferralNumber;

    @JsonProperty("updateReferCaseTo")
    private String updateReferCaseTo;

    @JsonProperty("updateReferentEmail")
    private String updateReferentEmail;

    @JsonProperty("updateIsUrgent")
    private String updateIsUrgent;

    @JsonProperty("updateReferralSubject")
    private String updateReferralSubject;

    @JsonProperty("updateReferralSubjectSpecify")
    private String updateReferralSubjectSpecify;

    @JsonProperty("updateReferralDetails")
    private String updateReferralDetails;

    @JsonProperty("updateReferralDocument")
    private List<DocumentTypeItem> updateReferralDocument;

    @JsonProperty("updateReferralInstruction")
    private String updateReferralInstruction;

    // Referral Reply
    @JsonProperty("hearingAndReferralDetails")
    private String hearingAndReferralDetails;

    @JsonProperty("directionTo")
    private String directionTo;

    @JsonProperty("replyToEmailAddress")
    private String replyToEmailAddress;

    @JsonProperty("isUrgentReply")
    private String isUrgentReply;

    @JsonProperty("directionDetails")
    private String directionDetails;

    @JsonProperty("replyDocument")
    private List<DocumentTypeItem> replyDocument;

    @JsonProperty("replyGeneralNotes")
    private String replyGeneralNotes;

    @JsonProperty("replyTo")
    private String replyTo;

    @JsonProperty("replyDetails")
    private String replyDetails;

    @JsonProperty("isJudge")
    private String isJudge;

    // Close Referral
    @JsonProperty("closeReferralHearingDetails")
    private String closeReferralHearingDetails;

    @JsonProperty("confirmCloseReferral")
    private List<String> confirmCloseReferral;

    @JsonProperty("closeReferralGeneralNotes")
    private String closeReferralGeneralNotes;

    // Document collection
    private List<DocumentTypeItem> documentCollection;
    private List<DocumentTypeItem> claimantDocumentCollection;
    private List<DocumentTypeItem> legalrepDocumentCollection;

    @CCD(
            label = "Digital Case File",
            typeNameOverride = "DigitalCaseFile",
            access = MultipleAccess.Access10.class,
            includeInProfiles = {EnglandWalesMultipleCftlibDefinition.class})
    @CCD(
            label = "Digital Case File",
            typeNameOverride = "DigitalCaseFile",
            access = MultipleAccess.Access37.class,
            includeInProfiles = {ScotlandMultipleCftlibDefinition.class})
    @JsonProperty("digitalCaseFile")
    private DigitalCaseFileType digitalCaseFile;

    @CCD(
            label = "Digital Case File configuration",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "configurationFiles",
            access = MultipleAccess.Access10.class,
            includeInProfiles = {EnglandWalesMultipleCftlibDefinition.class})
    @CCD(
            label = "Digital Case File configuration",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "configurationFiles",
            access = MultipleAccess.Access37.class,
            includeInProfiles = {ScotlandMultipleCftlibDefinition.class})
    @JsonProperty("bundleConfiguration")
    private String bundleConfiguration;

    @CCD(
            label = "DCF",
            hint = "DCF Collection",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "Bundle",
            access = MultipleAccess.Access10.class,
            includeInProfiles = {EnglandWalesMultipleCftlibDefinition.class})
    @CCD(
            label = "DCF",
            hint = "DCF Collection",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "Bundle",
            access = MultipleAccess.Access37.class,
            includeInProfiles = {ScotlandMultipleCftlibDefinition.class})
    @JsonProperty("caseBundles")
    private List<Bundle> caseBundles;

    @CCD(
            label = "Next Listed Date",
            typeOverride = FieldType.Date,
            access = MultipleAccess.Access05.class,
            includeInProfiles = {EnglandWalesMultipleCftlibDefinition.class})
    @CCD(
            label = "Next Listed Date",
            typeOverride = FieldType.Date,
            access = MultipleAccess.Access29.class,
            includeInProfiles = {ScotlandMultipleCftlibDefinition.class})
    private String nextListedDate;

    @CCD(
            typeOverride = FieldType.CaseLocation,
            includeSearchable = true,
            access = MultipleAccess.Access54.class,
            includeInProfiles = {
                EnglandWalesMultipleCftlibDefinition.class,
                ScotlandMultipleCftlibDefinition.class
            })
    @JsonProperty("caseManagementLocation")
    private CaseLocation caseManagementLocation;

    @CCD(
            label = "Tribunal Office",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_TribunalOffice",
            access = MultipleAccess.Access12.class,
            includeInProfiles = {EnglandWalesMultipleCftlibDefinition.class})
    @CCD(
            label = "Managing Office",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "VenueScotland",
            access = MultipleAccess.Access39.class,
            includeInProfiles = {ScotlandMultipleCftlibDefinition.class})
    @CCD(
            label = "Tribunal Office",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_TribunalOffice",
            access = MultipleAccess.Access14.class,
            includeInProfiles = {EnglandWalesMultipleProdDefinition.class})
    @CCD(
            label = "Managing Office",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "VenueScotland",
            access = MultipleAccess.Access41.class,
            includeInProfiles = {ScotlandMultipleProdDefinition.class})
    @JsonProperty("managingOffice")
    private String managingOffice;
}

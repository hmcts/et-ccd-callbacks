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
import uk.gov.hmcts.et.common.model.ccd.MultipleCftlibDefinition;
import uk.gov.hmcts.et.common.model.ccd.ScotlandMultipleCftlibDefinition;
import uk.gov.hmcts.et.common.model.ccd.ScotlandMultipleProdDefinition;
import uk.gov.hmcts.et.common.model.ccd.EnglandWalesSingleDefinition;
import uk.gov.hmcts.et.common.model.ccd.ScotlandSingleDefinition;
import uk.gov.hmcts.et.common.model.ccd.SingleAccess;
import uk.gov.hmcts.et.common.model.ccd.SingleDefinition;
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
                ScotlandMultipleProdDefinition.class,
                SingleDefinition.class
            })
    private String referredBy;

    @JsonProperty("referralDate")
    @CCD(
            excludeFromProfiles = {
                EnglandWalesMultipleCftlibDefinition.class,
                ScotlandMultipleCftlibDefinition.class,
                EnglandWalesMultipleProdDefinition.class,
                ScotlandMultipleProdDefinition.class,
                SingleDefinition.class
            })
    private String referralDate;

    // Referral Update
    @JsonProperty("updateReferralNumber")
    @CCD(
            excludeFromProfiles = {
                EnglandWalesMultipleCftlibDefinition.class,
                ScotlandMultipleCftlibDefinition.class,
                EnglandWalesMultipleProdDefinition.class,
                ScotlandMultipleProdDefinition.class,
                SingleDefinition.class
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
    @CCD(
            label = "Case documentation",
            hint = "Upload documentation for the case",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "DocumentUpload",
            searchable = false,
            access = SingleAccess.Access013.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Case documentation",
            hint = "Upload documentation for the case",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "DocumentUpload",
            searchable = false,
            access = SingleAccess.Access014.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private List<DocumentTypeItem> documentCollection;
    @CCD(
            label = "Claimant Documents",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "DocumentUpload",
            searchable = false,
            access = SingleAccess.Access056.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Claimant Documents",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "DocumentUpload",
            searchable = false,
            access = SingleAccess.Access093.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private List<DocumentTypeItem> claimantDocumentCollection;
    @CCD(
            label = "Documents",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "DocumentUpload",
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Documents",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "DocumentUpload",
            searchable = false,
            access = SingleAccess.Access086.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
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
    @CCD(
            label = "Digital Case File",
            typeNameOverride = "DigitalCaseFile",
            access = SingleAccess.Access145.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Digital Case File",
            typeNameOverride = "DigitalCaseFile",
            access = SingleAccess.Access176.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
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
    @CCD(
            label = "Digital Case File configuration",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "configurationFiles",
            searchable = false,
            access = SingleAccess.Access145.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Digital Case File configuration",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "configurationFiles",
            searchable = false,
            access = SingleAccess.Access176.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
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
    @CCD(
            label = "DCF",
            hint = "DCF Collection",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "Bundle",
            access = SingleAccess.Access145.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "DCF",
            hint = "DCF Collection",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "Bundle",
            access = SingleAccess.Access176.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
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
    @CCD(
            label = "Next Listed Date",
            typeOverride = FieldType.Date,
            searchable = false,
            access = SingleAccess.Access135.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Next Listed Date",
            typeOverride = FieldType.Date,
            searchable = false,
            access = SingleAccess.Access165.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String nextListedDate;

    @CCD(
            typeOverride = FieldType.CaseLocation,
            includeSearchable = true,
            access = MultipleAccess.Access54.class,
            includeInProfiles = MultipleCftlibDefinition.class)
    @JsonProperty("caseManagementLocation")
    @CCD(
            label = " ",
            typeNameOverride = "CaseLocation",
            includeSearchable = true,
            access = SingleAccess.Access196.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = " ",
            typeNameOverride = "CaseLocation",
            includeSearchable = true,
            access = SingleAccess.Access196.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
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

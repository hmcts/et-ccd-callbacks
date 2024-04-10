package uk.gov.hmcts.ethos.replacement.docmosis.domain.documents;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.ListTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.ReferralReplyTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.UpdateReferralType;

import java.util.List;

/**
 * This object captures information related to the referral object during creation event for docmosis.
 */
@SuperBuilder
@Data
public class ReferralTypeData {
    @JsonProperty("referralStatus")
    private String referralStatus;
    @JsonProperty("caseNumber")
    private String caseNumber;
    @JsonProperty("referralDate")
    private String referralDate;
    @JsonProperty("referredBy")
    private String referredBy;
    @JsonProperty("referCaseTo")
    private String referCaseTo;
    @JsonProperty("referentEmail")
    private String referentEmail;
    @JsonProperty("isUrgent")
    private String urgent;
    @JsonProperty("nextHearingDate")
    private String nextHearingDate;
    @JsonProperty("referralSubject")
    private String referralSubject;
    @JsonProperty("referralDetails")
    private String referralDetails;
    @JsonProperty("referralDocument")
    private List<DocumentTypeItem> referralDocument;
    @JsonProperty("referralInstruction")
    private String referralInstruction;
    @JsonProperty("referralReplyCollection")
    private List<ReferralReplyTypeItem> referralReplyCollection;
    @JsonProperty("updateReferralCollection")
    private ListTypeItem<UpdateReferralType> updateReferralCollection;
}

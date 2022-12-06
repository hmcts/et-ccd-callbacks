package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections4.CollectionUtils;

import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper.getRespondentNames;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentTseTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentTseType;

public final class RespondentTellSomethingElseHelper {

    private RespondentTellSomethingElseHelper() {}

    public static Map<String, String> buildPersonalisation(CaseDetails detail,
                                                           String customisedText,
                                                           String applicationType) {
        CaseData caseData = detail.getCaseData();
        Map<String, String> personalisation = new ConcurrentHashMap<>();
        personalisation.put("caseNumber", caseData.getEthosCaseReference());
        personalisation.put("claimant", caseData.getClaimant());
        personalisation.put("respondents", getRespondentNames(caseData));
        personalisation.put("customisedText", customisedText);
        personalisation.put("shortText", applicationType);
        personalisation.put("caseId", detail.getCaseId());
        return personalisation;
    }

    /**
     * Creates a new Respondent TSE collection if it doesn't exist.
     * Create a new element in the list and assign the TSE data from CaseData to it.
     * At last, clears the existing TSE data from CaseData to ensure fields will be empty when user 
     * starts a new application in the same case.
     * @param caseData contains all the case data
     */
    public static void createRespondentApplication(CaseData caseData) {
        if (CollectionUtils.isEmpty(caseData.getResTseCollection())) {
            caseData.setReferralCollection(new ArrayList<>());
        }

        RespondentTseType respondentTseType = new RespondentTseType();

        respondentTseType.setResTseSelectApplication(caseData.getResTseSelectApplication());
        
        // respondentTseType.setResTseGiveDetails(caseData.getResTseGiveDetails());
        respondentTseType.setResTseCopyToOtherPartyYesOrNo(caseData.getResTseCopyToOtherPartyYesOrNo());
        respondentTseType.setResTseCopyToOtherPartyTextArea(caseData.getResTseCopyToOtherPartyTextArea());

        RespondentTseTypeItem respondentTseTypeItem = new RespondentTseTypeItem();
        respondentTseTypeItem.setId(UUID.randomUUID().toString());
        respondentTseTypeItem.setValue(respondentTseType);

        List<RespondentTseTypeItem> respondentTseCollection = caseData.getResTseCollection();
        respondentTseCollection.add(respondentTseTypeItem);
        caseData.setResTseCollection(respondentTseCollection);

        clearRespondentTseDataFromCaseData(caseData);
    }

    private static void clearRespondentTseDataFromCaseData(CaseData caseData) {
        caseData.setResTseSelectApplication(null);
        // caseData.setResTseGiveDetails(null);
        caseData.setResTseCopyToOtherPartyYesOrNo(null);
        caseData.setResTseCopyToOtherPartyTextArea(null);
    }
}

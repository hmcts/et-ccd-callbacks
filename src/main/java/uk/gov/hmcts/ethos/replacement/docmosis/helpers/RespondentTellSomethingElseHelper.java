package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper.getRespondentNames;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;

public final class RespondentTellSomethingElseHelper {

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
        return personalisation;
    }
}

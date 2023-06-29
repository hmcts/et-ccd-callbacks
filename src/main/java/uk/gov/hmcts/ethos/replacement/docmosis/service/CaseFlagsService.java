package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.webjars.NotFoundException;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.FlagDetailType;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.ListTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.CaseFlagsType;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Objects;

import static uk.gov.hmcts.ecm.common.model.helper.CaseFlagConstants.ACTIVE;
import static uk.gov.hmcts.ecm.common.model.helper.CaseFlagConstants.DISRUPTIVE_CUSTOMER;
import static uk.gov.hmcts.ecm.common.model.helper.CaseFlagConstants.LANGUAGE_INTERPRETER;
import static uk.gov.hmcts.ecm.common.model.helper.CaseFlagConstants.SIGN_LANGUAGE_INTERPRETER;
import static uk.gov.hmcts.ecm.common.model.helper.CaseFlagConstants.VEXATIOUS_LITIGANT;

@Slf4j
@Service
public class CaseFlagsService {
    /**
     * Setup case flags for Claimant, Respondent and Case level.
     * @param caseData Data about the current case
     */
    public void setupCaseFlags(CaseData caseData) {
        caseData.setCaseFlags(CaseFlagsType.builder().build());

        caseData.setClaimantFlags(CaseFlagsType.builder()
                .partyName(caseData.getClaimant())
                .roleOnCase("claimant")
                .build()
        );

        caseData.setRespondentFlags(CaseFlagsType.builder()
                .partyName(caseData.getRespondent())
                .roleOnCase("respondent")
                .build()
        );
    }

    /**
     * Set default flag values for a newly created case.
     * @param caseData Data about the current case
     */
    public void setDefaultFlags(CaseData caseData) {
        caseData.setCaseRestrictedFlag(false);
        caseData.setAutoListFlag(true);
        caseData.setCaseInterpreterRequiredFlag(false);
        caseData.setCaseAdditionalSecurityFlag(false);
    }

    /**
     * Sets additional flags on CaseData dependent on CaseFlags raised.
     * @param caseData Data about the current case.
     */
    public void processNewlySetCaseFlags(CaseData caseData) {
        ListTypeItem<FlagDetailType> partyLevel = getPartyCaseFlags(caseData);
        caseData.setCaseInterpreterRequiredFlag(
                areAnyFlagsActive(partyLevel, SIGN_LANGUAGE_INTERPRETER, LANGUAGE_INTERPRETER)
        );

        caseData.setCaseAdditionalSecurityFlag(
                areAnyFlagsActive(partyLevel, VEXATIOUS_LITIGANT, DISRUPTIVE_CUSTOMER)
        );
    }

    private ListTypeItem<FlagDetailType> getPartyCaseFlags(CaseData caseData) {
        return ListTypeItem.concat(
                caseData.getClaimantFlags().getDetails(),
                caseData.getRespondentFlags().getDetails()
        );
    }

    @Nullable
    private FlagDetailType findFlagByName(ListTypeItem<FlagDetailType> flags, String name) {
        return flags.stream()
                .map(GenericTypeItem::getValue)
                .filter(o -> name.equals(o.getName()))
                .findFirst()
                .orElse(null);
    }

    private boolean areAnyFlagsActive(ListTypeItem<FlagDetailType> flags, String...names) {
        return Arrays.stream(names)
                .map(o -> findFlagByName(flags, o))
                .filter(Objects::nonNull)
                .anyMatch(o -> ACTIVE.equals(o.getStatus()));
    }
}

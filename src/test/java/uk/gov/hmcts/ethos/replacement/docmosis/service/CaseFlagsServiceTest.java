package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.FlagDetailType;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.ListTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.CaseFlagsType;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static uk.gov.hmcts.ecm.common.model.helper.CaseFlagConstants.ACTIVE;
import static uk.gov.hmcts.ecm.common.model.helper.CaseFlagConstants.DISRUPTIVE_CUSTOMER;
import static uk.gov.hmcts.ecm.common.model.helper.CaseFlagConstants.INACTIVE;
import static uk.gov.hmcts.ecm.common.model.helper.CaseFlagConstants.LANGUAGE_INTERPRETER;
import static uk.gov.hmcts.ecm.common.model.helper.CaseFlagConstants.SIGN_LANGUAGE_INTERPRETER;
import static uk.gov.hmcts.ecm.common.model.helper.CaseFlagConstants.VEXATIOUS_LITIGANT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@ExtendWith(SpringExtension.class)
class CaseFlagsServiceTest {
    public static final String CLAIMANT_NAME = "Claimant Name";
    public static final String RESPONDENT_NAME = "Respondent Name";
    private CaseFlagsService caseFlagsService;
    private CaseData caseData;

    @BeforeEach
    void setUp() {
        caseFlagsService = new CaseFlagsService();
        caseData = CaseDataBuilder.builder().build();
        caseData.setClaimant(CLAIMANT_NAME);
        caseData.setRespondent(RESPONDENT_NAME);
    }

    @Test
    void setupCaseFlags_setsShellCaseFlags() {
        caseFlagsService.setupCaseFlags(caseData);

        CaseFlagsType caseFlags = caseData.getCaseFlags();
        assertThat(caseFlags.getPartyName(), is(nullValue()));
        assertThat(caseFlags.getRoleOnCase(), is(nullValue()));

        CaseFlagsType claimantFlags = caseData.getClaimantFlags();
        assertThat(claimantFlags.getPartyName(), is(CLAIMANT_NAME));
        assertThat(claimantFlags.getRoleOnCase(), is("claimant"));

        CaseFlagsType respondentFlags = caseData.getRespondentFlags();
        assertThat(respondentFlags.getPartyName(), is(RESPONDENT_NAME));
        assertThat(respondentFlags.getRoleOnCase(), is("respondent"));
    }

    @Test
    void setDefaultFlags_setsDefaultFlags() {
        caseFlagsService.setDefaultFlags(caseData);

        assertThat(caseData.getCaseRestrictedFlag(), is(NO));
        assertThat(caseData.getAutoListFlag(), is(YES));
        assertThat(caseData.getCaseInterpreterRequiredFlag(), is(NO));
        assertThat(caseData.getCaseAdditionalSecurityFlag(), is(NO));
    }

    @Test
    void processNewlySetCaseFlags_claimantNeedsLanguageInterpreter_setsInterpreterRequiredFlagTrue() {
        caseData.setClaimantFlags(CaseFlagsType.builder().details(ListTypeItem.from()).build());
        caseData.setRespondentFlags(CaseFlagsType.builder().details(ListTypeItem.from()).build());

        caseData.getClaimantFlags().getDetails().add(
                GenericTypeItem.from(FlagDetailType.builder().name(LANGUAGE_INTERPRETER).status(ACTIVE).build())
        );

        caseFlagsService.processNewlySetCaseFlags(caseData);

        assertThat(caseData.getCaseInterpreterRequiredFlag(), is(YES));
    }

    @Test
    void processNewlySetCaseFlags_respondentNeedsLanguageInterpreter_setsInterpreterRequiredFlagTrue() {
        caseData.setClaimantFlags(CaseFlagsType.builder().details(ListTypeItem.from()).build());
        caseData.setRespondentFlags(CaseFlagsType.builder().details(ListTypeItem.from()).build());

        caseData.getRespondentFlags().getDetails().add(
                GenericTypeItem.from(FlagDetailType.builder().name(LANGUAGE_INTERPRETER).status(ACTIVE).build())
        );

        caseFlagsService.processNewlySetCaseFlags(caseData);

        assertThat(caseData.getCaseInterpreterRequiredFlag(), is(YES));
    }

    @Test
    void processNewlySetCaseFlags_claimantNeedsSignLanguageInterpreter_setsInterpreterRequiredFlagTrue() {
        caseData.setClaimantFlags(CaseFlagsType.builder().details(ListTypeItem.from()).build());
        caseData.setRespondentFlags(CaseFlagsType.builder().details(ListTypeItem.from()).build());

        caseData.getClaimantFlags().getDetails().add(
                GenericTypeItem.from(FlagDetailType.builder().name(SIGN_LANGUAGE_INTERPRETER).status(ACTIVE).build())
        );

        caseFlagsService.processNewlySetCaseFlags(caseData);

        assertThat(caseData.getCaseInterpreterRequiredFlag(), is(YES));
    }

    @Test
    void processNewlySetCaseFlags_respondentNeedsSignLanguageInterpreter_setsInterpreterRequiredFlagTrue() {
        caseData.setClaimantFlags(CaseFlagsType.builder().details(ListTypeItem.from()).build());
        caseData.setRespondentFlags(CaseFlagsType.builder().details(ListTypeItem.from()).build());

        caseData.getRespondentFlags().getDetails().add(
                GenericTypeItem.from(FlagDetailType.builder().name(SIGN_LANGUAGE_INTERPRETER).status(ACTIVE).build())
        );

        caseFlagsService.processNewlySetCaseFlags(caseData);

        assertThat(caseData.getCaseInterpreterRequiredFlag(), is(YES));
    }

    @Test
    void processNewlySetCaseFlags_noInterpretersNeeded_setsInterpreterRequiredFlagFalse() {
        caseData.setClaimantFlags(CaseFlagsType.builder().details(ListTypeItem.from()).build());
        caseData.setRespondentFlags(CaseFlagsType.builder().details(ListTypeItem.from()).build());

        caseData.getClaimantFlags().getDetails().add(
                GenericTypeItem.from(FlagDetailType.builder().name(LANGUAGE_INTERPRETER).status(INACTIVE).build())
        );

        caseData.getRespondentFlags().getDetails().add(
                GenericTypeItem.from(FlagDetailType.builder().name(SIGN_LANGUAGE_INTERPRETER).status(INACTIVE).build())
        );

        caseFlagsService.processNewlySetCaseFlags(caseData);

        assertThat(caseData.getCaseInterpreterRequiredFlag(), is(NO));
    }

    @Test
    void processNewlySetCaseFlags_claimantIsVexatious_setsAdditionalSecurityFlagTrue() {
        caseData.setClaimantFlags(CaseFlagsType.builder().details(ListTypeItem.from()).build());
        caseData.setRespondentFlags(CaseFlagsType.builder().details(ListTypeItem.from()).build());

        caseData.getClaimantFlags().getDetails().add(
                GenericTypeItem.from(FlagDetailType.builder().name(VEXATIOUS_LITIGANT).status(ACTIVE).build())
        );

        caseFlagsService.processNewlySetCaseFlags(caseData);

        assertThat(caseData.getCaseAdditionalSecurityFlag(), is(YES));
    }

    @Test
    void processNewlySetCaseFlags_respondentIsVexatious_setsAdditionalSecurityFlagTrue() {
        caseData.setClaimantFlags(CaseFlagsType.builder().details(ListTypeItem.from()).build());
        caseData.setRespondentFlags(CaseFlagsType.builder().details(ListTypeItem.from()).build());

        caseData.getRespondentFlags().getDetails().add(
                GenericTypeItem.from(FlagDetailType.builder().name(VEXATIOUS_LITIGANT).status(ACTIVE).build())
        );

        caseFlagsService.processNewlySetCaseFlags(caseData);

        assertThat(caseData.getCaseAdditionalSecurityFlag(), is(YES));
    }

    @Test
    void processNewlySetCaseFlags_claimantIsDisruptive_setsAdditionalSecurityFlagTrue() {
        caseData.setClaimantFlags(CaseFlagsType.builder().details(ListTypeItem.from()).build());
        caseData.setRespondentFlags(CaseFlagsType.builder().details(ListTypeItem.from()).build());

        caseData.getClaimantFlags().getDetails().add(
                GenericTypeItem.from(FlagDetailType.builder().name(DISRUPTIVE_CUSTOMER).status(ACTIVE).build())
        );

        caseFlagsService.processNewlySetCaseFlags(caseData);

        assertThat(caseData.getCaseAdditionalSecurityFlag(), is(YES));
    }

    @Test
    void processNewlySetCaseFlags_respondentIsDisruptive_setsAdditionalSecurityFlagTrue() {
        caseData.setClaimantFlags(CaseFlagsType.builder().details(ListTypeItem.from()).build());
        caseData.setRespondentFlags(CaseFlagsType.builder().details(ListTypeItem.from()).build());

        caseData.getRespondentFlags().getDetails().add(
                GenericTypeItem.from(FlagDetailType.builder().name(DISRUPTIVE_CUSTOMER).status(ACTIVE).build())
        );

        caseFlagsService.processNewlySetCaseFlags(caseData);

        assertThat(caseData.getCaseAdditionalSecurityFlag(), is(YES));
    }

    @Test
    void processNewlySetCaseFlags_noPartyIsVexatiousOrDisruptive_setsAdditionalSecurityFlagFalse() {
        caseData.setClaimantFlags(CaseFlagsType.builder().details(ListTypeItem.from()).build());
        caseData.setRespondentFlags(CaseFlagsType.builder().details(ListTypeItem.from()).build());

        caseData.getClaimantFlags().getDetails().add(
                GenericTypeItem.from(FlagDetailType.builder().name(VEXATIOUS_LITIGANT).status(INACTIVE).build())
        );

        caseData.getRespondentFlags().getDetails().add(
                GenericTypeItem.from(FlagDetailType.builder().name(DISRUPTIVE_CUSTOMER).status(INACTIVE).build())
        );

        caseFlagsService.processNewlySetCaseFlags(caseData);

        assertThat(caseData.getCaseInterpreterRequiredFlag(), is(NO));
    }
}

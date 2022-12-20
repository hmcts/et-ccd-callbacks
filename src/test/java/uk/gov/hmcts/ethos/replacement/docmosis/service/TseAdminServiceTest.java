package uk.gov.hmcts.ethos.replacement.docmosis.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantIndType;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantType;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.CaseDataBuilder;

@ExtendWith(SpringExtension.class)
@SuppressWarnings("squid:S5961")
class TseAdminServiceTest {
    private TseAdminService tseAdminService;

    @MockBean
    private EmailService emailService;

    private static final String TEMPLATE_ID = "someTemplateId";
    private static final String CASE_NUMBER = "Some Case Number";

    private static final String BOTH = "Both parties";
    private static final String CLAIMANT_ONLY = "Claimant only";
    private static final String RESPONDENT_ONLY = "Respondent only";
    private static final String CLAIMANT_EMAIL = "Claimant@mail.com";
    private static final String CLAIMANT_FIRSTNAME = "Claim";
    private static final String CLAIMANT_LASTNAME = "Ant";

    private static final String RESPONDENT_EMAIL = "Respondent@mail.com";
    private static final String RESPONDENT_NAME = "Respondent";

    @BeforeEach
    void setUp() {
        tseAdminService = new TseAdminService(emailService);
        ReflectionTestUtils.setField(tseAdminService, "emailTemplateId", TEMPLATE_ID);
    }
    @ParameterizedTest
    @CsvSource({BOTH, CLAIMANT_ONLY, RESPONDENT_ONLY})
    void sendRecordADecisionEmails(String partyNotified) {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setEthosCaseReference(CASE_NUMBER);
        createClaimant(caseData);
        createRespondent(caseData);
        caseData.setTseAdminSelectPartyNotify(partyNotified);

        Map<String, String> expectedPersonalisationClaimant =
            createPersonalisation(caseData, CLAIMANT_FIRSTNAME + " " + CLAIMANT_LASTNAME);
        Map<String, String> expectedPersonalisationRespondent = createPersonalisation(caseData, RESPONDENT_NAME);

        tseAdminService.sendRecordADecisionEmails(caseData);

        if (CLAIMANT_ONLY.equals(partyNotified)) {
            verify(emailService).sendEmail(TEMPLATE_ID, CLAIMANT_EMAIL, expectedPersonalisationClaimant);
            verify(emailService, never()).sendEmail(TEMPLATE_ID, RESPONDENT_EMAIL, expectedPersonalisationRespondent);
        } else if (RESPONDENT_ONLY.equals(partyNotified)) {
            verify(emailService, never()).sendEmail(TEMPLATE_ID, CLAIMANT_EMAIL, expectedPersonalisationClaimant);
            verify(emailService).sendEmail(TEMPLATE_ID, RESPONDENT_EMAIL, expectedPersonalisationRespondent);
        } else {
            verify(emailService).sendEmail(TEMPLATE_ID, CLAIMANT_EMAIL, expectedPersonalisationClaimant);
            verify(emailService).sendEmail(TEMPLATE_ID, RESPONDENT_EMAIL, expectedPersonalisationRespondent);
        }
    }

    private void createClaimant(CaseData caseData) {
        ClaimantType claimantType = new ClaimantType();
        claimantType.setClaimantEmailAddress(CLAIMANT_EMAIL);

        ClaimantIndType claimantIndType = new ClaimantIndType();
        claimantIndType.setClaimantFirstNames(CLAIMANT_FIRSTNAME);
        claimantIndType.setClaimantLastName(CLAIMANT_LASTNAME);

        caseData.setClaimantType(claimantType);
        caseData.setClaimantIndType(claimantIndType);
    }

    private void createRespondent(CaseData caseData) {
        RespondentSumType respondentSumType = new RespondentSumType();
        respondentSumType.setRespondentName(RESPONDENT_NAME);
        respondentSumType.setRespondentEmail(RESPONDENT_EMAIL);

        RespondentSumTypeItem respondentSumTypeItem = new RespondentSumTypeItem();
        respondentSumTypeItem.setValue(respondentSumType);

        caseData.setRespondentCollection(new ArrayList<>(Collections.singletonList(respondentSumTypeItem)));
    }

    private Map<String, String> createPersonalisation(CaseData caseData,
                                                      String expectedName) {
        Map<String, String> personalisation = new ConcurrentHashMap<>();
        personalisation.put("caseNumber", caseData.getEthosCaseReference());
        personalisation.put("name", expectedName);
        return personalisation;
    }
}

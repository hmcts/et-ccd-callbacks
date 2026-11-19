package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.ethos.replacement.docmosis.service.noc.NocRespondentRepresentativeService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class RespondentServiceTest {

    @Mock
    private NocRespondentRepresentativeService nocRespondentRepresentativeService;

    @InjectMocks
    private RespondentService respondentService;

    private static final String RESPONDENT_ID = "6281d99e-1a94-4369-870e-9f527801d913";
    private static final String RESPONDENT_NAME = "John Smith";
    private static final String RESPONSE_RESPONDENT_EMAIL = "response_respondent@test.com";
    private static final String RESPONDENT_EMAIL = "respondent@test.com";
    private static final String REPRESENTATIVE_ID = "6281d99e-1a94-4369-870e-9f527801d914";
    private static final String RESPONDENT_REPRESENTATIVE_EMAIL = "respondent_representative@test.com";

    @Test
    void theResolveRepresentativeOrRespondentEmail() {
        // when respondent is not valid should return empty string
        CaseData caseData = new CaseData();
        RespondentSumTypeItem respondent = new RespondentSumTypeItem();
        assertThat(respondentService.resolveRepresentativeOrRespondentEmail(caseData, respondent)).isEmpty();
        // respondent has response respondent email should return that email
        respondent.setId(RESPONDENT_ID);
        respondent.setValue(RespondentSumType.builder().respondentName(RESPONDENT_NAME).build());
        respondent.getValue().setResponseRespondentEmail(RESPONSE_RESPONDENT_EMAIL);
        assertThat(respondentService.resolveRepresentativeOrRespondentEmail(caseData, respondent))
                .isEqualTo(RESPONSE_RESPONDENT_EMAIL);
        // respondent has email should return that email
        respondent.getValue().setRespondentEmail(RESPONDENT_EMAIL);
        assertThat(respondentService.resolveRepresentativeOrRespondentEmail(caseData, respondent))
                .isEqualTo(RESPONDENT_EMAIL);
        // when respondent representative has email should return it
        respondent.getValue().setRepresentativeId(REPRESENTATIVE_ID);
        RepresentedTypeRItem representative = new RepresentedTypeRItem();
        representative.setId(REPRESENTATIVE_ID);
        representative.setValue(RepresentedTypeR.builder().build());
        representative.getValue().setRepresentativeEmailAddress(RESPONDENT_REPRESENTATIVE_EMAIL);
        caseData.setRepCollection(List.of(representative));
        when(nocRespondentRepresentativeService.resolveRepresentativeEmail(representative))
                .thenReturn(RESPONDENT_REPRESENTATIVE_EMAIL);
        assertThat(respondentService.resolveRepresentativeOrRespondentEmail(caseData, respondent))
                .isEqualTo(RESPONDENT_REPRESENTATIVE_EMAIL);
    }
}

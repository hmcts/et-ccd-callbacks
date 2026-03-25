package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RemoveOwnRepAsRespondentCallbackHandlerTest {

    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    private RemoveOwnRepAsRespondentCallbackHandler handler;

    @BeforeEach
    void setUp() {
        handler = new RemoveOwnRepAsRespondentCallbackHandler(caseDetailsConverter);
    }

    @Test
    void aboutToSubmitShouldRemoveSelectedRepAndClearRemovalCollection() {
        RepresentedTypeRItem repItem = new RepresentedTypeRItem();
        repItem.setId("1");

        CaseData caseData = new CaseData();
        caseData.setRepCollection(new ArrayList<>(List.of(repItem)));
        caseData.setRepCollectionToRemove(new ArrayList<>(List.of(repItem)));
        stubConverter(caseData);

        handler.aboutToSubmit(callbackCaseDetails());

        assertThat(caseData.getRepCollection()).isEmpty();
        assertThat(caseData.getRepCollectionToRemove()).isNull();
    }

    @Test
    void submittedShouldThrowWhenCallbackTypeUnsupported() {
        assertThatThrownBy(() -> handler.submitted(callbackCaseDetails()))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("does not support submitted callbacks");
    }

    private void stubConverter(CaseData caseData) {
        uk.gov.hmcts.et.common.model.ccd.CaseDetails ccdCaseDetails =
            new uk.gov.hmcts.et.common.model.ccd.CaseDetails();
        ccdCaseDetails.setCaseData(caseData);
        ccdCaseDetails.setCaseTypeId("ET_EnglandWales");
        ccdCaseDetails.setCaseId("123");
        when(caseDetailsConverter.convert(any(CaseDetails.class))).thenReturn(ccdCaseDetails);
    }

    private CaseDetails callbackCaseDetails() {
        return CaseDetails.builder()
            .id(123L)
            .caseTypeId("ET_EnglandWales")
            .state("Open")
            .build();
    }
}

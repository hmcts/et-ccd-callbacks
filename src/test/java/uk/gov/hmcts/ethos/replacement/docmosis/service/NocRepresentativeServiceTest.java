package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.types.ChangeOrganisationRequest;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.ClaimantSolicitorRole;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class NocRepresentativeServiceTest {

    @Mock
    private NocRespondentRepresentativeService nocRespondentRepresentativeService;
    @Mock
    private NocClaimantRepresentativeService nocClaimantRepresentativeService;

    private NocRepresentativeService nocRepresentativeService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        nocRepresentativeService = new NocRepresentativeService(
                nocRespondentRepresentativeService,
                nocClaimantRepresentativeService
        );
    }

    @Test
    void updateRepresentation_shouldCallClaimantService_whenClaimantSolicitorRole() throws Exception {
        CaseData caseData = new CaseData();
        ChangeOrganisationRequest change =
                buildChangeRequest(ClaimantSolicitorRole.CLAIMANTSOLICITOR.getCaseRoleLabel());
        caseData.setChangeOrganisationRequestField(change);
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseData(caseData);

        when(nocClaimantRepresentativeService.updateClaimantRepresentation(any(), any()))
                .thenReturn(caseData);

        CaseData result = nocRepresentativeService.updateRepresentation(caseDetails, "token");

        assertThat(result).isSameAs(caseData);
        verify(nocClaimantRepresentativeService).updateClaimantRepresentation(caseDetails, "token");
        verifyNoInteractions(nocRespondentRepresentativeService);
    }

    @Test
    void updateRepresentation_shouldCallRespondentService_whenNotClaimantSolicitorRole() throws Exception {
        CaseData caseData = new CaseData();
        ChangeOrganisationRequest change = buildChangeRequest("RESPONDENTSOLICITOR");
        caseData.setChangeOrganisationRequestField(change);
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseData(caseData);

        when(nocRespondentRepresentativeService.updateRespondentRepresentation(any()))
                .thenReturn(caseData);
        when(nocRespondentRepresentativeService.prepopulateOrgAddress(any(), any()))
                .thenReturn(caseData);

        CaseData result = nocRepresentativeService.updateRepresentation(caseDetails, "token");

        assertThat(result).isSameAs(caseData);
        verify(nocRespondentRepresentativeService).updateRespondentRepresentation(caseDetails);
        verify(nocRespondentRepresentativeService).prepopulateOrgAddress(caseData, "token");
        verifyNoInteractions(nocClaimantRepresentativeService);
    }

    @Test
    void updateRepresentation_shouldThrowException_whenChangeRequestInvalid() {
        CaseData caseData = new CaseData();
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseData(caseData);

        assertThrows(IllegalStateException.class, () ->
                nocRepresentativeService.updateRepresentation(caseDetails, "token"));
    }

    @Test
    void updateRepresentation_shouldThrowException_whenChangeRequestIsNull() {
        CaseData caseData = new CaseData();
        caseData.setChangeOrganisationRequestField(null);
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseData(caseData);

        assertThrows(IllegalStateException.class, () ->
                nocRepresentativeService.updateRepresentation(caseDetails, "token"));
    }

    @Test
    void updateRepresentation_shouldThrowException_whenCaseRoleIdIsNull() {
        ChangeOrganisationRequest change = ChangeOrganisationRequest.builder()
                .caseRoleId(null)
                .organisationToAdd(mock(uk.gov.hmcts.et.common.model.ccd.types.Organisation.class))
                .build();
        CaseData caseData = new CaseData();
        caseData.setChangeOrganisationRequestField(change);
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseData(caseData);

        assertThrows(IllegalStateException.class, () ->
                nocRepresentativeService.updateRepresentation(caseDetails, "token"));
    }

    @Test
    void updateRepresentation_shouldThrowException_whenOrganisationToAddIsNull() {
        ChangeOrganisationRequest change = ChangeOrganisationRequest.builder()
                .caseRoleId(new DynamicFixedListType())
                .organisationToAdd(null)
                .build();
        CaseData caseData = new CaseData();
        caseData.setChangeOrganisationRequestField(change);
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseData(caseData);

        assertThrows(IllegalStateException.class, () ->
                nocRepresentativeService.updateRepresentation(caseDetails, "token"));
    }

    private ChangeOrganisationRequest buildChangeRequest(String caseRoleLabel) {
        DynamicFixedListType caseRole = new DynamicFixedListType();
        DynamicValueType value = new DynamicValueType();
        value.setCode(caseRoleLabel);
        value.setLabel(caseRoleLabel);
        caseRole.setValue(value);

        return ChangeOrganisationRequest.builder()
                .caseRoleId(caseRole)
                .organisationToAdd(mock(uk.gov.hmcts.et.common.model.ccd.types.Organisation.class))
                .build();
    }
}
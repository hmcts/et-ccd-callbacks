package uk.gov.hmcts.ethos.replacement.docmosis.service.noc;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.types.ChangeOrganisationRequest;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.ClaimantSolicitorRole;
import java.io.IOException;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class NocRepresentativeService {
    private final NocRespondentRepresentativeService nocRespondentRepresentativeService;
    private final NocClaimantRepresentativeService nocClaimantRepresentativeService;

    public CaseData updateRepresentation(CaseDetails caseDetails, String userToken) throws IOException {
        CaseData caseData = caseDetails.getCaseData();
        ChangeOrganisationRequest change = validateChangeRequest(caseData);
        DynamicFixedListType caseRoleId = change.getCaseRoleId();

        if (caseRoleId.getValue().getCode().equals(ClaimantSolicitorRole.CLAIMANTSOLICITOR.getCaseRoleLabel())) {
            caseData = nocClaimantRepresentativeService.updateClaimantRepresentation(caseDetails, userToken);
        } else {
            caseData = nocRespondentRepresentativeService.updateRespondentRepresentation(caseDetails);
            caseData = nocRespondentRepresentativeService.prepopulateOrgAddress(caseData, userToken);
        }
        return caseData;
    }

    private ChangeOrganisationRequest validateChangeRequest(CaseData caseData) {
        ChangeOrganisationRequest change = caseData.getChangeOrganisationRequestField();
        if (Objects.isNull(change)
                || Objects.isNull(change.getCaseRoleId())
                || Objects.isNull(change.getOrganisationToAdd())) {
            throw new IllegalStateException("Invalid or missing ChangeOrganisationRequest: " + change);
        }
        return change;
    }
}

package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC;

import java.util.Objects;
import java.util.UUID;

import static com.google.common.base.Strings.isNullOrEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@Slf4j
@Service
@RequiredArgsConstructor
public class AddAmendClaimantRepresentativeService {
    public void addAmendClaimantRepresentative(CaseData caseData) {
        // remove
        if (NO.equals(caseData.getClaimantRepresentedQuestion())
                && caseData.getRepresentativeClaimantType() != null) {
            caseData.setRepresentativeClaimantType(null);
            caseData.setClaimantRepresentativeRemoved(YES);
            return;
        }
        // add or amend
        setRepresentativeId(caseData);
    }

    private void setRepresentativeId(CaseData caseData) {
        RepresentedTypeC claimantRep = caseData.getRepresentativeClaimantType();
        if (Objects.isNull(claimantRep)) {
            return;
        }

        if (isNullOrEmpty(claimantRep.getRepresentativeId())) {
            claimantRep.setRepresentativeId(UUID.randomUUID().toString());
        }

        if (isNullOrEmpty(claimantRep.getOrganisationId())) {
            claimantRep.setOrganisationId(UUID.randomUUID().toString());
        }
    }
}

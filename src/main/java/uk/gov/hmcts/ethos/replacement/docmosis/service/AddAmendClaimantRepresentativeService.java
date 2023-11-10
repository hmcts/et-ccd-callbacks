package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC;

import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AddAmendClaimantRepresentativeService {
    public void setRepresentativeId(CaseData caseData) {
        RepresentedTypeC claimantRep = caseData.getRepresentativeClaimantType();
        if (Objects.isNull(claimantRep)) {
            return;
        }

        claimantRep.setRepresentativeId(UUID.randomUUID().toString());
    }
}

package uk.gov.hmcts.ethos.replacement.docmosis.domain.caseview;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.CaseView;
import uk.gov.hmcts.ccd.sdk.CaseViewRequest;
import uk.gov.hmcts.et.common.model.bundle.Bundle;
import uk.gov.hmcts.et.common.model.bundle.BundleDetails;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.caseview.state.CaseState;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.ccd.HubLinkStatus;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.ccd.DigitalCaseFileRepository;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.ccd.HubLinkStatusRepository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;

@Component
@RequiredArgsConstructor
public class ETCaseView implements CaseView<CaseData, CaseState> {

    private final HubLinkStatusRepository hubLinkStatusRepository;
    private final DigitalCaseFileRepository digitalCaseFileRepository;

    @Override
    public Set<String> caseTypeIds() {
        return Set.of(ENGLANDWALES_CASE_TYPE_ID, SCOTLAND_CASE_TYPE_ID);
    }

    @Override
    public CaseData getCase(CaseViewRequest<CaseState> request, CaseData blobCase) {
        // Set hublink status from table if found, falling back to any blob value.
        hubLinkStatusRepository.findById(request.caseRef())
            .map(HubLinkStatus::getData)
            .ifPresent(blobCase::setHubLinksStatuses);

        // Only an absent row falls back to the blob, including its in-flight bundles.
        digitalCaseFileRepository.findById(request.caseRef())
            .ifPresent(dcf -> {
                blobCase.setDigitalCaseFile(dcf.getData());
                blobCase.setCaseBundles(dcf.getPendingBundleId() == null
                    ? null : List.of(pendingBundle(dcf.getPendingBundleId())));
            });

        return blobCase;
    }

    private Bundle pendingBundle(UUID pendingBundleId) {
        String id = pendingBundleId.toString();
        // EM matches value.id and supplies the completion status and document itself.
        return Bundle.builder().id(id)
            .value(BundleDetails.builder().id(id).stitchStatus("IN_PROGRESS").build())
            .build();
    }
}

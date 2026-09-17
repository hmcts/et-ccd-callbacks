package uk.gov.hmcts.ethos.replacement.docmosis.domain.caseview;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.CaseView;
import uk.gov.hmcts.ccd.sdk.CaseViewRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.caseview.state.CaseState;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.ccd.HubLinkStatus;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.ccd.DigitalCaseFileRepository;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.ccd.HubLinkStatusRepository;

import java.util.Set;

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

        digitalCaseFileRepository.findById(request.caseRef())
            .ifPresent(digitalCaseFile -> {
                blobCase.setDigitalCaseFile(digitalCaseFile.getData());
                if (digitalCaseFile.getActiveBundleId() == null) {
                    blobCase.setCaseBundles(null);
                }
            });

        return blobCase;
    }
}

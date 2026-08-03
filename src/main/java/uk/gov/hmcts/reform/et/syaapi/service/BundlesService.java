package uk.gov.hmcts.reform.et.syaapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.HearingBundleType;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.et.syaapi.enums.CaseEvent;
import uk.gov.hmcts.reform.et.syaapi.helper.CaseDetailsConverter;
import uk.gov.hmcts.reform.et.syaapi.helper.EmployeeObjectMapper;
import uk.gov.hmcts.reform.et.syaapi.models.ClaimantBundlesRequest;
import uk.gov.hmcts.reform.et.syaapi.models.RespondentBundlesRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

@RequiredArgsConstructor
@Service
@Slf4j
public class BundlesService {

    private final CaseService caseService;
    private final CaseDetailsConverter caseDetailsConverter;
    private final NotificationService notificationService;

    /**
     * Submit Claimant Bundles.
     *
     * @param authorization - authorization
     * @param request       - bundles request from the claimant
     * @return the associated {@link CaseDetails} for the ID provided in request
     */
    public CaseDetails submitBundles(String authorization, ClaimantBundlesRequest request) {
        return submitHearingBundle(authorization, new BundleSubmission(
            request.getCaseId(),
            request.getCaseTypeId(),
            CaseEvent.SUBMIT_CLAIMANT_BUNDLES,
            request.getClaimantBundles(),
            CaseData::getBundlesClaimantCollection,
            CaseData::setBundlesClaimantCollection,
            notificationService::sendBundlesEmails
        ));
    }

    /**
     * Submit Respondent Bundles.
     *
     * @param authorization - authorization
     * @param request       - bundles request from the respondent
     * @return the associated {@link CaseDetails} for the ID provided in request
     */
    public CaseDetails submitRespondentBundles(String authorization, RespondentBundlesRequest request) {
        return submitHearingBundle(authorization, new BundleSubmission(
            request.getCaseId(),
            request.getCaseTypeId(),
            CaseEvent.SUBMIT_RESPONDENT_BUNDLES,
            request.getRespondentBundles(),
            CaseData::getBundlesRespondentCollection,
            CaseData::setBundlesRespondentCollection,
            notificationService::sendRespondentBundlesEmails
        ));
    }

    private CaseDetails submitHearingBundle(String authorization, BundleSubmission submission) {
        StartEventResponse startEventResponse = caseService.startUpdate(
            authorization,
            submission.caseId(),
            submission.caseTypeId(),
            submission.caseEvent()
        );

        CaseData caseData = EmployeeObjectMapper
            .convertCaseDataMapToCaseDataObject(startEventResponse.getCaseDetails().getData());

        List<GenericTypeItem<HearingBundleType>> collection = submission.collectionGetter().apply(caseData);
        if (CollectionUtils.isEmpty(collection)) {
            collection = new ArrayList<>();
            submission.collectionSetter().accept(caseData, collection);
        }
        collection.add(GenericTypeItem.from(submission.hearingBundle()));

        CaseDataContent content = caseDetailsConverter.caseDataContent(startEventResponse, caseData);

        CaseDetails response = caseService.submitUpdate(
            authorization,
            submission.caseId(),
            content,
            submission.caseTypeId()
        );

        submission.emailSender().send(caseData, submission.caseId(), submission.hearingBundle().getHearing());

        return response;
    }

    private record BundleSubmission(
        String caseId,
        String caseTypeId,
        CaseEvent caseEvent,
        HearingBundleType hearingBundle,
        Function<CaseData, List<GenericTypeItem<HearingBundleType>>> collectionGetter,
        BiConsumer<CaseData, List<GenericTypeItem<HearingBundleType>>> collectionSetter,
        BundleEmailSender emailSender
    ) {
    }

    @FunctionalInterface
    private interface BundleEmailSender {
        void send(CaseData caseData, String caseId, String hearingId);
    }
}

package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationType;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.citizenhub.ClaimantTse;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLAIMANT_TITLE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.STORED;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.STORED_STATE;

@Slf4j
@Service
@RequiredArgsConstructor
public class TseStoreService {

    /**
     * Creates a new TSE collection if it doesn't exist.
     * Create a new application in the list and assign the TSE data from CaseData to it.
     * At last, clears the existing TSE data from CaseData to ensure fields will be empty when user
     * starts a new application in the same case.
     *
     * @param caseData   contains all the case data.
     */

    public void storeClaimantApplication(CaseData caseData) {
        if (isEmpty(caseData.getTseApplicationStoredCollection())) {
            caseData.setTseApplicationStoredCollection(new ArrayList<>());
        }

        GenericTseApplicationTypeItem tseApplicationTypeItem = getToStoreTseApplicationTypeItem(caseData);

        List<GenericTseApplicationTypeItem> tseApplicationCollection = caseData.getTseApplicationStoredCollection();
        tseApplicationCollection.add(tseApplicationTypeItem);

        caseData.setTseApplicationStoredCollection(tseApplicationCollection);
    }

    @NotNull
    private static GenericTseApplicationTypeItem getToStoreTseApplicationTypeItem(CaseData caseData) {
        GenericTseApplicationType application = getToStoreTseApplicationType(caseData);

        GenericTseApplicationTypeItem tseApplicationTypeItem = new GenericTseApplicationTypeItem();
        tseApplicationTypeItem.setId(UUID.randomUUID().toString());
        tseApplicationTypeItem.setValue(application);

        return tseApplicationTypeItem;
    }

    @NotNull
    private static GenericTseApplicationType getToStoreTseApplicationType(CaseData caseData) {
        GenericTseApplicationType application = new GenericTseApplicationType();

        application.setDate(UtilHelper.formatCurrentDate(LocalDate.now()));
        application.setApplicant(CLAIMANT_TITLE);
        application.setApplicationState(STORED);
        application.setStatus(STORED_STATE);

        ClaimantTse claimantTse = caseData.getClaimantTse();
        application.setType(ClaimantTse.APP_TYPE_MAP.get(claimantTse.getContactApplicationType()));
        application.setDetails(claimantTse.getContactApplicationText());
        application.setDocumentUpload(claimantTse.getContactApplicationFile());
        application.setCopyToOtherPartyYesOrNo(claimantTse.getCopyToOtherPartyYesOrNo());
        application.setCopyToOtherPartyText(claimantTse.getCopyToOtherPartyText());

        return application;
    }
}

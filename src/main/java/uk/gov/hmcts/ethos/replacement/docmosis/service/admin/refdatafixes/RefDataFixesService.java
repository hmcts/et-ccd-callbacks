package uk.gov.hmcts.ethos.replacement.docmosis.service.admin.refdatafixes;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.model.ccd.CaseData;
import uk.gov.hmcts.ecm.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.ecm.common.model.ccd.items.HearingTypeItem;
import uk.gov.hmcts.ecm.common.model.ccd.types.HearingType;
import uk.gov.hmcts.ecm.common.model.listing.items.ListingTypeItem;
import uk.gov.hmcts.ecm.common.model.listing.types.ListingType;
import uk.gov.hmcts.et.common.client.CcdClient;
import uk.gov.hmcts.et.common.exceptions.CaseRetrievalException;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.AdminData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.CaseDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.ReportParams;

//import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.ClaimServedDateFixRepository;

@Slf4j
@Service("refDataFixesService")
public class RefDataFixesService {

    private static final String CASES_SEARCHED = "Cases searched: ";
    private static final String MESSAGE = "Failed to retrieve reference data for case id : ";

    private final CcdClient ccdClient;
   // private final ClaimServedDateFixRepository claimServedDateFixRepository;

    @Autowired
    public RefDataFixesService(CcdClient ccdClient) {
        this.ccdClient = ccdClient;
    }

    public AdminData updateJudgesItcoReferences(CaseDetails caseDetails, String authToken) {
        String caseTypeId = caseDetails.getAdminData().getTribunalOffice();
        AdminData adminData = caseDetails.getAdminData();
        ReportParams params = new ReportParams(
                caseTypeId,
                caseDetails.getAdminData().getTribunalOffice(),
                adminData.getListingDateFrom(),
                adminData.getListingDateTo());
        try {
            RefDataFixesCcdDataSource dataSource = new RefDataFixesCcdDataSource(authToken, ccdClient);
            List<SubmitEvent> submitEvents = dataSource.getData(params);
            List<JudgeCodes> judges = getJudges();
            if (CollectionUtils.isNotEmpty(submitEvents)) {
                log.info(CASES_SEARCHED + submitEvents.size());
                for (SubmitEvent submitEvent : submitEvents) {
                  CaseData caseData = submitEvent.getCaseData();
                  if (CollectionUtils.isNotEmpty(caseData.getHearingCollection())) {
                      for (HearingTypeItem hearingTypeItem : caseData.getHearingCollection()) {
                          HearingType hearingType = hearingTypeItem.getValue();
                          hearingType.setJudge(updateJudgeName(
                                  judges,
                                  hearingType.getJudge(),
                                  caseTypeId));
                      }
                  }
                    if (caseData.getPrintHearingCollection() != null &&
                            CollectionUtils.isNotEmpty(caseData.getPrintHearingCollection().getListingCollection())) {
                        for (ListingTypeItem listingTypeItem : caseData.getPrintHearingCollection().getListingCollection()) {
                            ListingType listingType = listingTypeItem.getValue();
                            listingType.setHearingJudgeName(updateJudgeName(
                                    judges,
                                    listingType.getHearingJudgeName(),
                                    caseTypeId));
                        }
                    }
                    uk.gov.hmcts.ecm.common.model.ccd.CCDRequest returnedRequest = ccdClient.startEventForCaseEcm(authToken, caseTypeId,
                            caseDetails.getJurisdiction(), String.valueOf(submitEvent.getCaseId()));

                    ccdClient.submitEventForCaseEcm(authToken, caseData, caseTypeId,
                            caseDetails.getJurisdiction(), returnedRequest, String.valueOf(submitEvent.getCaseId()));
                }
            }
            return adminData;
        } catch (Exception ex) {
            throw new CaseRetrievalException(MESSAGE +  caseDetails.getCaseId() + ex.getMessage());
        }
    }



    private String updateJudgeName(List<JudgeCodes> judges, String judgeCode, String office) {
        Optional<JudgeCodes> t = judges.stream().filter(i -> i.existingJudgeCode.equals(judgeCode) && i.office.equals(office)).findFirst();
        if (t.isPresent()) {
            return t.get().requiredJudgeCode;
        }
        return judgeCode;
    }

    private List<JudgeCodes> getJudges() throws Exception {
        try {
            String json = new String(Files.readAllBytes(Paths.get(Objects.requireNonNull(getClass().getClassLoader()
                    .getResource("Judges.json")).toURI())));
            ObjectMapper mapper = new ObjectMapper();

            return Arrays.asList(mapper.readValue(json, JudgeCodes[].class));
        } catch (Exception ex) {
            throw new Exception(MESSAGE + ex.toString());
        }
    }


//    public AdminData insertClaimServedDate(CaseDetails caseDetails) {
//        AdminData adminData = caseDetails.getAdminData();
//        claimServedDateFixRepository.addClaimServedDate(Date.valueOf(adminData.getListingDateFrom()),
//                Date.valueOf(adminData.getListingDateTo()),adminData.getTribunalOffice());
//        return adminData;
//    }


}

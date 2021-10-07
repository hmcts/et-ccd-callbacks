package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.ecm.common.model.ccd.Address;
import uk.gov.hmcts.ecm.common.model.ccd.CaseData;
import uk.gov.hmcts.ecm.common.model.ccd.CaseDetails;
import uk.gov.hmcts.ecm.common.model.helper.DefaultValues;
import uk.gov.hmcts.ecm.common.model.listing.ListingData;
import uk.gov.hmcts.ethos.replacement.docmosis.config.CaseDefaultValuesConfiguration;
import uk.gov.hmcts.ethos.replacement.docmosis.config.TribunalOfficesConfiguration;

import static org.junit.Assert.fail;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
        DefaultValuesReaderService.class,
        TribunalOfficesService.class,
})
@EnableConfigurationProperties({CaseDefaultValuesConfiguration.class, TribunalOfficesConfiguration.class})
public class DefaultValuesReaderServiceTest {

    @Autowired
    DefaultValuesReaderService defaultValuesReaderService;

    private String defaultClaimantTypeOfClaimant;
    private DefaultValues postDefaultValuesManchester;
    private DefaultValues postDefaultValuesGlasgow;
    private DefaultValues postDefaultValuesAberdeen;
    private DefaultValues postDefaultValuesDundee;
    private DefaultValues postDefaultValuesEdinburgh;
    private DefaultValues postDefaultValuesBristol;
    private DefaultValues postDefaultValuesLeeds;
    private DefaultValues postDefaultValuesLondonCentral;
    private DefaultValues postDefaultValuesLondonEast;
    private DefaultValues postDefaultValuesLondonSouth;
    private DefaultValues postDefaultValuesMidlandsEast;
    private DefaultValues postDefaultValuesMidlandsWest;
    private DefaultValues postDefaultValuesNewcastle;
    private DefaultValues postDefaultValuesWales;
    private DefaultValues postDefaultValuesWatford;
    private CaseData caseData;
    private CaseDetails manchesterCaseDetails;
    private CaseDetails glasgowCaseDetails;
    private CaseDetails bristolCaseDetails;
    private CaseDetails leedsCaseDetails;
    private CaseDetails londonCentralCaseDetails;
    private CaseDetails londonEastCaseDetails;
    private CaseDetails londonSouthCaseDetails;
    private CaseDetails midlandsEastCaseDetails;
    private CaseDetails midlandsWestCaseDetails;
    private CaseDetails newcastleCaseDetails;
    private CaseDetails walesCaseDetails;
    private CaseDetails watfordCaseDetails;
    private ListingData listingData;

    private CaseDetails getCaseDetails(String caseTypeId) {
        CaseDetails caseDetails = new CaseDetails();
        CaseData caseData = new CaseData();
        caseDetails.setCaseData(caseData);
        caseDetails.setCaseId("123456");
        caseDetails.setCaseTypeId(caseTypeId);
        caseDetails.setJurisdiction("TRIBUNALS");
        return caseDetails;
    }

    private ListingData getListingDataSetUp() {
        listingData = new ListingData();
        listingData.setTribunalCorrespondenceDX("DX");
        listingData.setTribunalCorrespondenceEmail("m@m.com");
        listingData.setTribunalCorrespondenceFax("100300200");
        listingData.setTribunalCorrespondenceTelephone("077123123");
        Address address = new Address();
        address.setAddressLine1("AddressLine1");
        address.setAddressLine2("AddressLine2");
        address.setAddressLine3("AddressLine3");
        address.setPostTown("Manchester");
        address.setCountry("UK");
        address.setPostCode("L1 122");
        listingData.setTribunalCorrespondenceAddress(address);
        return listingData;
    }

    @Test
    public void rework() {
        fail("Recreate tests");
    }
}
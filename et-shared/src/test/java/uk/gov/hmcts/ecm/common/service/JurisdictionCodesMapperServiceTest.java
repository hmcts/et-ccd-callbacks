package uk.gov.hmcts.ecm.common.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ecm.common.constants.ClaimTypesConstants;
import uk.gov.hmcts.ecm.common.constants.JurisdictionCodesConstants;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.JurCodesTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantRequestType;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JurisdictionCodesMapperServiceTest {

    private JurisdictionCodesMapperService jurisdictionCodesMapperService;
    private CaseData data;

    @BeforeEach
    void setUp() {
        data = new CaseData();
        jurisdictionCodesMapperService = new JurisdictionCodesMapperService();
        ClaimantRequestType requestType = new ClaimantRequestType();
        data.setClaimantRequests(requestType);
    }

    @Test
    void shouldMapJurisdictionCodesWithNonBlankIds() {
        data.setTypesOfClaim(List.of(ClaimTypesConstants.BREACH_OF_CONTRACT, ClaimTypesConstants.DISCRIMINATION));
        data.getClaimantRequests().setDiscriminationClaims(List.of(
                ClaimTypesConstants.AGE,
                ClaimTypesConstants.DISABILITY
        ));
        data.getClaimantRequests().setPayClaims(List.of(
                ClaimTypesConstants.ARREARS,
                ClaimTypesConstants.HOLIDAY_PAY
        ));

        List<JurCodesTypeItem> items = jurisdictionCodesMapperService.mapToJurCodes(data);
        List<String> plainCodes = items.stream()
                .map(it -> it.getValue().getJuridictionCodesList())
                .toList();
        List<String> expectedCodes = List.of(
                JurisdictionCodesConstants.BOC,
                JurisdictionCodesConstants.DAG,
                JurisdictionCodesConstants.DDA,
                JurisdictionCodesConstants.WA,
                JurisdictionCodesConstants.WTR_AL
        );

        assertThat(items).hasSize(expectedCodes.size())
            .allSatisfy(item -> assertThat(item.getId()).isNotBlank());
        assertThat(plainCodes).containsAll(expectedCodes);
    }

    @Test
    void shouldNotMapTypesWithoutJurCodes() {
        data.getClaimantRequests().setPayClaims(List.of(
                ClaimTypesConstants.OTHER_TYPES,
                ClaimTypesConstants.OTHER_PAYMENTS
        ));
        data.setTypesOfClaim(List.of(ClaimTypesConstants.PAY_RELATED_CLAIM));

        List<JurCodesTypeItem> items = jurisdictionCodesMapperService.mapToJurCodes(data);

        assertThat(items).isEmpty();
    }

    @Test
    void shouldNotMapIfTypesNotPresented() {
        List<JurCodesTypeItem> items = jurisdictionCodesMapperService.mapToJurCodes(data);

        assertThat(items).isEmpty();
    }
}

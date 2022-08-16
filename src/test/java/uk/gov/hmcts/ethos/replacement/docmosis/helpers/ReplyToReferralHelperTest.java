package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.ReferralTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.ReferralType;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.CaseDataBuilder;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ReplyToReferralHelperTest {
    private CaseData caseData;

    @BeforeEach
    void setUp() {
        caseData = CaseDataBuilder.builder().build();
    }

    @Test
    void whenCalledWithNoReferrals_ReturnEmptyDropdown() {
        ReplyToReferralHelper.populateSelectReferralDropdown(caseData);

        assertNull(caseData.getSelectReferralToReply());
    }

    @Test
    void whenCalledWithOneReferral_ReturnOneDropdownItem() {
        ReferralTypeItem referralTypeItem = new ReferralTypeItem();
        ReferralType referralType = new ReferralType();
        referralType.setReferralNumber("1");
        referralType.setReferralSubject("Other");
        referralTypeItem.setValue(referralType);
        caseData.setReferralCollection(List.of(referralTypeItem));
        ReplyToReferralHelper.populateSelectReferralDropdown(caseData);

        assertEquals(1, caseData.getSelectReferralToReply().getListItems().size());
    }

    @Test
    void whenCalledWithMultipleReferrals_ReturnMultipleDropdownItems() {
        ReferralTypeItem referralTypeItem = new ReferralTypeItem();
        ReferralType referralType = new ReferralType();
        referralType.setReferralNumber("1");
        referralType.setReferralSubject("Other");
        referralTypeItem.setValue(referralType);
        caseData.setReferralCollection(List.of(referralTypeItem, referralTypeItem, referralTypeItem));
        ReplyToReferralHelper.populateSelectReferralDropdown(caseData);

        assertEquals(3, caseData.getSelectReferralToReply().getListItems().size());
    }
}
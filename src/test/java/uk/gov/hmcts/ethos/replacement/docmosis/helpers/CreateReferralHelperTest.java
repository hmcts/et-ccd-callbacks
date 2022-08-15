package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.ethos.replacement.docmosis.service.UserService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.CaseDataBuilder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_STATUS_HEARD;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_STATUS_POSTPONED;

class CreateReferralHelperTest {

    private UserService userService;
    private CreateReferralHelper createReferralHelper;
    private CaseData caseData;

    private final String expectedSingleHearingDetails = "<hr>To help you complete this form, open the <a href=\"url\">"
        + "referral guidance documents</a><hr><h3>Hearing details </h3><pre>Date &nbsp;&#09&#09&#09&#09&#09&nbsp; 25 "
        + "December 2021 00:00<br><br>Hearing &#09&#09&#09&#09&nbsp; test<br><br>Type &nbsp;&nbsp;&#09&#09&#09&#09&#09"
        + " null</pre><hr>";

    private final String expectedMultipleHearingDetails = "<hr>To help you complete this form, open the <a href="
        + "\"url\">referral guidance documents</a><hr><h3>Hearing details 1</h3><pre>Date &nbsp;&#09&#09&#09&#09&#0"
        + "9&nbsp; 25 December 2021 00:00<br><br>Hearing &#09&#09&#09&#09&nbsp; test<br><br>Type &nbsp;&nbsp;&#09&#0"
        + "9&#09&#09&#09 null</pre><hr><h3>Hearing details 2</h3><pre>Date &nbsp;&#09&#09&#09&#09&#09&nbsp; 26 December"
        + " 2021 00:00<br><br>Hearing &#09&#09&#09&#09&nbsp; test<br><br>Type &nbsp;&nbsp;&#09&#09&#09&#09&#09 null<"
        + "/pre><hr>";

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        UserDetails userDetails = new UserDetails();
        userDetails.setFirstName("Judge");
        userDetails.setLastName("Judy");
        userDetails.setEmail("judge.judy@aol.com");
        when(userService.getUserDetails("")).thenReturn(userDetails);
        createReferralHelper = new CreateReferralHelper(userService);
        caseData = CaseDataBuilder.builder().build();
    }

    @Test
    void populateSingleHearingDetails() {
        caseData = CaseDataBuilder.builder()
            .withHearing("1", "test", "Judy", "Venue", List.of("Telephone", "Video"),
                "length num", "type", "Yes")
            .withHearingSession(0, "1", "2021-12-25T00:00:00.000",
                HEARING_STATUS_POSTPONED, false)
            .build();

        createReferralHelper.populateHearingDetails(caseData);

        assertThat(caseData.getReferralHearingDetails())
            .isEqualTo(expectedSingleHearingDetails);
    }

    @Test
    void populateMultipleHearingDetails() {
        caseData = CaseDataBuilder.builder()
            .withHearing("1", "test", "Judy", "Venue", List.of("Telephone", "Video"),
                "length num", "type", "Yes")
            .withHearingSession(0, "1", "2021-12-25T00:00:00.000",
                HEARING_STATUS_POSTPONED, false)
            .withHearing("1", "test", "Judy", "Venue", List.of("Telephone", "Video"),
                "length num", "type", "Yes")
            .withHearingSession(1, "1", "2021-12-26T00:00:00.000",
                HEARING_STATUS_HEARD, false)
            .build();

        createReferralHelper.populateHearingDetails(caseData);

        assertThat(caseData.getReferralHearingDetails())
            .isEqualTo(expectedMultipleHearingDetails);
    }

    @Test
    void saveReferralToTheReferralCollection() {
        DocumentTypeItem document1 = new DocumentTypeItem();
        document1.setId("1");
        DocumentTypeItem document2 = new DocumentTypeItem();
        document2.setId("2");
        caseData.setReferCaseTo("Judge Judy");
        caseData.setIsUrgent("Yes");
        caseData.setReferralSubject("Subject line here");
        caseData.setReferralSubjectSpecify("Custom subject line");
        caseData.setReferralDetails("This is an explanation");
        caseData.setReferralDocument(List.of(document1, document2));
        caseData.setReferralInstruction("Custom instructions for judge");

        createReferralHelper.createReferral(caseData, "");

        String expected = "ReferralType(referralNumber=1, referCaseTo=Judge Judy, "
            + "referrerEmail=judge.judy@aol.com, isUrgent=Yes, "
            + "referralSubject=Subject line here, referralSubjectSpecify=Custom subject line, referralDetails=This "
            + "is an explanation, referralDocument=[DocumentTypeItem(id=1, value=null), DocumentTypeItem(id=2, "
            + "value=null)], referralInstruction=Custom instructions for judge, referredBy=Judge Judy, "
            + "referralDate="
            + Helper.getCurrentDate()
            + ", referralStatus=Open)";
        String actual = caseData.getReferralCollection().get(0).getValue().toString();
        assertEquals(expected, actual);
    }

    @Test
    void addNewReferralToReferralCollection() {
        createReferralHelper.createReferral(caseData, "");
        createReferralHelper.createReferral(caseData, "");

        assertEquals(2, caseData.getReferralCollection().size());
    }

    @Test
    void saveTheUserDetailsOfTheReferrerWithTheReferral() {
        createReferralHelper.createReferral(caseData, "");

        String email = caseData.getReferralCollection().get(0).getValue().getReferrerEmail();
        String referredBy = caseData.getReferralCollection().get(0).getValue().getReferredBy();

        assertEquals("judge.judy@aol.com", email);
        assertEquals("Judge Judy", referredBy);
    }

    @Test
    void clearReferralInformationFromCaseDataAfterSaving() {
        DocumentTypeItem document1 = new DocumentTypeItem();
        document1.setId("1");
        DocumentTypeItem document2 = new DocumentTypeItem();
        document2.setId("2");
        caseData.setReferCaseTo("Judge Judy");
        caseData.setIsUrgent("Yes");
        caseData.setReferralSubject("Subject line here");
        caseData.setReferralSubjectSpecify("Custom subject line");
        caseData.setReferralDetails("This is an explanation");
        caseData.setReferralDocument(List.of(document1, document2));
        caseData.setReferralInstruction("Custom instructions for judge");

        createReferralHelper.createReferral(caseData, "");

        assertNull(caseData.getReferCaseTo());
        assertNull(caseData.getIsUrgent());
        assertNull(caseData.getReferralSubject());
        assertNull(caseData.getReferralSubjectSpecify());
        assertNull(caseData.getReferralDetails());
        assertNull(caseData.getReferralDocument());
        assertNull(caseData.getReferralInstruction());
    }
}
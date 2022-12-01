package uk.gov.hmcts.ethos.replacement.docmosis.controllers;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.RespondentTellSomethingElseHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.EmailService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.UserService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityNoErrors;

@Slf4j
@RestController
@RequestMapping("/respondentTSE")
public class RespondentTellSomethingElseController {

    private final VerifyTokenService verifyTokenService;
    private final EmailService emailService;
    private final UserService userService;
    private String emailTemplateId;
    private static final String NO = "I do not want to copy";
    private final String rule92AnsweredNoText = "You have said that you do not want to copy this correspondence to "
        + "the other party. \n \n"
        + "The tribunal will consider all correspondence and let you know what happens next.";
    private final String rule92AnsweredYesGroupA = "The other party will be notified that any objections to your "
        + "%s application should be sent to the tribunal as soon as possible, and in any event "
        + "within 7 days.";
    private final String rule92AnsweredYesGroupB = "The other party is not expected to respond to this application.\n"
        + " \n"
        + "However, they have been notified that any objections to your %s application should be "
        + "sent to the tribunal as soon as possible, and in any event within 7 days.";

    private static final String INVALID_TOKEN = "Invalid Token {}";

    public RespondentTellSomethingElseController(
        @Value("${respondent.tse.template.id}") String emailTemplateId,
        VerifyTokenService verifyTokenService, EmailService emailService,
        UserService userService) {
        this.emailTemplateId = emailTemplateId;
        this.verifyTokenService = verifyTokenService;
        this.emailService = emailService;
        this.userService = userService;
    }

    @PostMapping(value = "/aboutToSubmit", consumes = APPLICATION_JSON_VALUE)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json",
                    schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> aboutToSubmitRespondentTSE(
        @RequestBody CCDRequest ccdRequest,
        @RequestHeader(value = "Authorization") String userToken) {
        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        String legalRepEmail = userService.getUserDetails(userToken).getEmail();
        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        String customisedText = null;

        if (NO.equals(caseData.getResTseCopyToOtherPartyYesOrNo())) {
            customisedText = rule92AnsweredNoText;
        } else {
            switch (caseData.getResTseSelectApplication()) {
                case "Amend response":
                case "Strike out all or part of a claim":
                case "Contact the tribunal":
                case "Postpone a hearing":
                case "Vary or revoke an order":
                case "Order other party":
                case "Claimant not complied":
                case "Restrict publicity":
                    customisedText = String.format(rule92AnsweredYesGroupA, caseData.getResTseSelectApplication());
                    break;
                case "Change personal details":
                case "Consider a decision afresh":
                case "Reconsider judgement":
                    customisedText = String.format(rule92AnsweredYesGroupB, caseData.getResTseSelectApplication());
                    break;
                case "Order a witness to attend to give evidence":
                    // No need to send email for Group C
                    break;
                default:
                    break;
            }
        }

        if (customisedText != null) {
            emailService.sendEmail(
                emailTemplateId,
                legalRepEmail,
                RespondentTellSomethingElseHelper.buildPersonalisation(
                    ccdRequest.getCaseDetails(),
                    customisedText,
                    caseData.getResTseSelectApplication()
                ));
        }

        return getCallbackRespEntityNoErrors(caseData);
    }

}

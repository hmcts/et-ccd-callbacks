package uk.gov.hmcts.ethos.replacement.docmosis.controllers;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
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
import uk.gov.hmcts.ethos.replacement.docmosis.service.EmailService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.RespondentTellSomethingElseService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.UserService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityNoErrors;

@Slf4j
@RestController
@RequestMapping("/respondentTSE")
public class RespondentTellSomethingElseController {

    private static final String NO = "I do not want to copy this correspondence";
    private static final String INVALID_TOKEN = "Invalid Token {}";
    private final VerifyTokenService verifyTokenService;
    private final RespondentTellSomethingElseService respondentTellSomethingElseService;
    private final EmailService emailService;
    private final UserService userService;
    private final String emailTemplateId;

    public RespondentTellSomethingElseController(@Value("${respondent.tse.template.id}") String emailTemplateId,
                                                 VerifyTokenService verifyTokenService,
                                                 RespondentTellSomethingElseService respondentTellSomethingElseService,
                                                 EmailService emailService,
                                                 UserService userService) {
        this.emailTemplateId = emailTemplateId;
        this.verifyTokenService = verifyTokenService;
        this.respondentTellSomethingElseService = respondentTellSomethingElseService;
        this.emailService = emailService;
        this.userService = userService;
    }

    /**
     * This service Gets userToken as a parameter for security validation
     * and ccdRequest data which has caseData as an object.
     * @param ccdRequest        CaseData which is a generic data type for most of the
     *                          methods which holds ET1 case data
     * @param  userToken        Used for authorisation
     * @return ResponseEntity   It is an HTTPEntity response which has CCDCallbackResponse that
     *                          includes caseData which contains the upload document names of
     *                          type "Another type of document" in a html string format.
     */
    @PostMapping(value = "/midVariableContent", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Respondent Tell Something Else About To Start Event")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json",
                    schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> midVariableContent(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader(value = "Authorization") String userToken) {

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        caseData.setResTseVariableContent(respondentTellSomethingElseService.resTseSetVariableContent(caseData));

        return getCallbackRespEntityNoErrors(caseData);
    }

//    @PostMapping(value = "/aboutToSubmit", consumes = APPLICATION_JSON_VALUE)
//    @ApiResponses(value = {
//        @ApiResponse(responseCode = "200", description = "Accessed successfully",
//            content = {
//                @Content(mediaType = "application/json",
//                    schema = @Schema(implementation = CCDCallbackResponse.class))
//            }),
//        @ApiResponse(responseCode = "400", description = "Bad Request"),
//        @ApiResponse(responseCode = "500", description = "Internal Server Error")
//    })
//    public ResponseEntity<CCDCallbackResponse> aboutToSubmitRespondentTSE(
//        @RequestBody CCDRequest ccdRequest,
//        @RequestHeader(value = "Authorization") String userToken) {
//        String legalRepEmail = userService.getUserDetails(userToken).getEmail();
        //check if the respondent selected NO
//        if (ccdRequest.getCaseDetails().getCaseData().getResTseCopyToOtherPartyYesOrNo().equals(NO)) {
//            emailService.sendEmail(emailTemplateId, legalRepEmail);
//        }
        //if true send template 6.1
        //if false check if application selected is in group A or B
        //if group A send template 6.2
        //if group B send template 6.3
//    }

}

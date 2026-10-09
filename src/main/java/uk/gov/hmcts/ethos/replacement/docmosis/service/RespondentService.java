package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.ethos.replacement.docmosis.service.noc.NocRespondentRepresentativeService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.RespondentUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.noc.RespondentRepresentativeUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class RespondentService {
    private final NocRespondentRepresentativeService nocRespondentRepresentativeService;

    /**
     * Resolves the most appropriate email address for the given respondent.
     * <p>
     * The method first attempts to identify the respondent's representative and resolve
     * the representative's email address. If a non-blank representative email is found,
     * it is returned immediately.
     * </p>
     *
     * <p>
     * If no representative email can be resolved, the method falls back to the
     * respondent's own email address. It first checks {@code respondentEmail} and, if
     * that is blank, returns {@code responseRespondentEmail}.
     * </p>
     *
     * <p><strong>Assumptions:</strong></p>
     * <ul>
     *     <li>A representative's email address takes precedence over the respondent's
     *         own email address.</li>
     *     <li>The respondent must be valid according to
     *         {@link RespondentUtils#isValidRespondent(RespondentSumTypeItem)}.</li>
     *     <li>If the respondent is invalid, {@link StringUtils#EMPTY} is returned.</li>
     *     <li>If no representative can be found, or no representative email can be
     *         resolved, the respondent's email fields are used as a fallback.</li>
     *     <li>{@code respondentEmail} takes precedence over
     *         {@code responseRespondentEmail} when both are available.</li>
     *     <li>If both respondent email fields are blank, the method may return a blank
     *         or {@code null} value from {@code responseRespondentEmail}, depending on
     *         the underlying case data.</li>
     * </ul>
     *
     * @param caseData the case data containing respondent and representative information
     * @param respondent the respondent for whom the email address is to be resolved
     * @return the representative's email address if available; otherwise the respondent's
     *         email address, or {@link StringUtils#EMPTY} if the respondent is invalid
     */
    public String resolveRepresentativeOrRespondentEmail(CaseData caseData, RespondentSumTypeItem respondent) {
        if (!RespondentUtils.isValidRespondent(respondent)) {
            return StringUtils.EMPTY;
        }
        RepresentedTypeRItem respondentRepresentative = RespondentRepresentativeUtils
                .findRepresentativeByRespondent(caseData, respondent);
        String respondentRepresentativeEmail = nocRespondentRepresentativeService
                .resolveRepresentativeEmail(respondentRepresentative);
        if (StringUtils.isNotBlank(respondentRepresentativeEmail)) {
            return respondentRepresentativeEmail;
        }
        return StringUtils.isNotBlank(respondent.getValue().getRespondentEmail())
                ? respondent.getValue().getRespondentEmail()
                : respondent.getValue().getResponseRespondentEmail();
    }
}

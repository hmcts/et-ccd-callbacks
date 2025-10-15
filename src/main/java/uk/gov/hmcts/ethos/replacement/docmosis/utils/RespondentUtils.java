package uk.gov.hmcts.ethos.replacement.docmosis.utils;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.UpdateRespondentRepresentativeRequest;

import java.util.List;
import java.util.Objects;

import static uk.gov.hmcts.ethos.replacement.docmosis.constants.GenericServiceConstants.YES;

public final class RespondentUtils {

    private RespondentUtils() {
        // Utility classes should not have a public or default constructor.
    }

    /**
     * Marks a respondent as having their representative removed within the given {@link CaseData}.
     *
     * <p>This method locates the first respondent in the respondent collection whose
     * {@code respondentName} matches the name provided in the
     * {@link UpdateRespondentRepresentativeRequest}. If such a respondent is found,
     * their {@code representativeRemoved} field is set to {@code YES}.</p>
     *
     * <p>Key points:</p>
     * <ul>
     *   <li>If {@code caseData} or {@code updateReq} is {@code null} or empty, no action is taken.</li>
     *   <li>If the {@code respondentName} in {@code updateReq} is blank, no action is taken.</li>
     *   <li>If the respondent collection in {@code caseData} is {@code null} or empty, no action is taken.</li>
     *   <li>Only respondents with a non-null value and a non-blank {@code respondentName} are considered.</li>
     *   <li>The first matching respondent found will be updated; others (if any) are ignored.</li>
     * </ul>
     *
     * @param caseData   the {@link CaseData} object containing the respondent collection
     * @param updateReq  the {@link UpdateRespondentRepresentativeRequest} containing the target respondent name
     */
    public static void markRespondentRepresentativeRemoved(
            CaseData caseData, UpdateRespondentRepresentativeRequest updateReq) {

        if (ObjectUtils.isEmpty(caseData) || ObjectUtils.isEmpty(updateReq)) {
            return;
        }

        final String name = updateReq.getRespondentName();
        if (StringUtils.isBlank(name)) {
            return;
        }

        final List<RespondentSumTypeItem> respondents = caseData.getRespondentCollection();
        if (CollectionUtils.isEmpty(respondents)) {
            return;
        }
        respondents.stream()
                .filter(Objects::nonNull)
                .map(RespondentSumTypeItem::getValue)
                .filter(Objects::nonNull)
                .filter(r -> StringUtils.isNotBlank(r.getRespondentName()))
                .filter(r -> name.equals(r.getRespondentName()))
                .findFirst()
                .ifPresent(r -> r.setRepresentativeRemoved(YES));
    }
}

package uk.gov.hmcts.ethos.replacement.docmosis.helpers.noc;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.Organisation;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.isNullOrEmpty;
import static uk.gov.hmcts.ecm.common.constants.PdfMapperConstants.STRING_EMPTY;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.STRING_COMMA_WITH_SPACE;

public final class NocRespondentMapper {

    private NocRespondentMapper() {
        // access through static methods
    }

    /**
     * Retrieves the organisation ID of the first representative organisation in the provided list.
     *
     * @param repList List of {@link RepresentedTypeRItem} representing representatives
     * @return Organisation ID of the first representative, or null if not found
     */
    public static String getFirstRepOrganisationId(List<RepresentedTypeRItem> repList) {
        return repList.stream()
            .map(RepresentedTypeRItem::getValue)
            .filter(Objects::nonNull)
            .map(RepresentedTypeR::getRespondentOrganisation)
            .filter(Objects::nonNull)
            .map(Organisation::getOrganisationID)
            .findFirst()
            .orElse(null);
    }

    /**
     * Returns a list of respondents from the case data who do not have representation and are not in the revoke list.
     *
     * @param caseData The case data containing respondent and representative collections
     * @param respondentIdToRevoke List of respondent IDs whose representation is to be revoked
     * @return List of {@link RespondentSumTypeItem} to email
     */
    public static List<RespondentSumTypeItem> getRespondentCollectionToEmail(
        CaseData caseData,
        List<String> respondentIdToRevoke
    ) {
        Set<String> respondentIdWithRep = getRespondentIdsWithRepresentation(caseData);
        return caseData.getRespondentCollection().stream()
            .filter(item -> item.getId() != null)
            .filter(item -> !respondentIdWithRep.contains(item.getId()))
            .filter(item -> !respondentIdToRevoke.contains(item.getId()))
            .collect(Collectors.toList());
    }

    private static Set<String> getRespondentIdsWithRepresentation(CaseData caseData) {
        if (CollectionUtils.isEmpty(caseData.getRepCollection())) {
            return Set.of();
        }
        return caseData.getRepCollection().stream()
            .map(RepresentedTypeRItem::getValue)
            .filter(Objects::nonNull)
            .map(RepresentedTypeR::getRespondentId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
    }

    /**
     * Retrieves the organisation name of the first representative in the provided list.
     *
     * @param repList List of {@link RepresentedTypeRItem} representing representatives
     * @return Name of the first organisation, or an empty string if not found
     */
    public static String getOrganisationName(List<RepresentedTypeRItem> repList) {
        return repList.stream()
            .map(RepresentedTypeRItem::getValue)
            .filter(Objects::nonNull)
            .map(RepresentedTypeR::getNameOfOrganisation)
            .filter(name -> !isNullOrEmpty(name))
            .findFirst()
            .orElse(STRING_EMPTY);
    }

    /**
     * Returns a comma-separated string of unique representative names from the provided list.
     *
     * @param repList List of {@link RepresentedTypeRItem} representing representatives
     * @return Comma-separated representative names, or an empty string if none found
     */
    public static String getRepresentativeNames(List<RepresentedTypeRItem> repList) {
        return repList.stream()
            .map(RepresentedTypeRItem::getValue)
            .filter(Objects::nonNull)
            .map(RepresentedTypeR::getNameOfRepresentative)
            .filter(name -> !isNullOrEmpty(name))
            .distinct()
            .collect(Collectors.joining(STRING_COMMA_WITH_SPACE));
    }

    /**
     * Returns a list of unique representative email addresses from the provided list.
     *
     * @param repList List of {@link RepresentedTypeRItem} representing representatives
     * @return List of unique representative email addresses
     */
    public static List<String> getRepresentativeEmails(List<RepresentedTypeRItem> repList) {
        return repList.stream()
            .map(RepresentedTypeRItem::getValue)
            .filter(Objects::nonNull)
            .map(RepresentedTypeR::getRepresentativeEmailAddress)
            .filter(email -> !isNullOrEmpty(email))
            .distinct()
            .toList();
    }

    /**
     * Returns a comma-separated string of respondent party names from the provided representative list.
     *
     * @param repList List of {@link RepresentedTypeRItem} representing representatives
     * @return Comma-separated respondent party names, or an empty string if none found
     */
    public static String getRespondentPartyNames(List<RepresentedTypeRItem> repList) {
        List<String> respondentNames = repList.stream()
            .map(RepresentedTypeRItem::getValue)
            .filter(Objects::nonNull)
            .map(RepresentedTypeR::getRespRepName)
            .filter(Objects::nonNull)
            .toList();
        return String.join(STRING_COMMA_WITH_SPACE, respondentNames);
    }

    /**
     * Returns a list of respondent IDs from the provided representative list.
     *
     * @param repList List of {@link RepresentedTypeRItem} representing representatives
     * @return List of respondent IDs, or an empty list if repList is null
     */
    public static List<String> getRespondentIds(List<RepresentedTypeRItem> repList) {
        if (repList == null) {
            return List.of();
        }
        return repList.stream()
            .map(RepresentedTypeRItem::getValue)
            .filter(Objects::nonNull)
            .map(RepresentedTypeR::getRespondentId)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    /**
     * Retrieves the respondent's email address, preferring the response email if present.
     *
     * @param respondent The respondent object
     * @return Respondent's email address, or null if not available
     */
    public static String getRespondentEmail(RespondentSumType respondent) {
        return StringUtils.isNotBlank(respondent.getResponseRespondentEmail())
            ? respondent.getResponseRespondentEmail()
            : respondent.getRespondentEmail();
    }
}

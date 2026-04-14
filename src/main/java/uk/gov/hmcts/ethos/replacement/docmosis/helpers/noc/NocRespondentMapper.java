package uk.gov.hmcts.ethos.replacement.docmosis.helpers.noc;

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
        return caseData.getRepCollection().stream()
            .map(RepresentedTypeRItem::getValue)
            .filter(Objects::nonNull)
            .map(RepresentedTypeR::getRespondentId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
    }

    public static String getOrganisationName(List<RepresentedTypeRItem> repList) {
        return repList.stream()
            .map(RepresentedTypeRItem::getValue)
            .filter(Objects::nonNull)
            .map(RepresentedTypeR::getNameOfOrganisation)
            .filter(name -> !isNullOrEmpty(name))
            .findFirst()
            .orElse(STRING_EMPTY);
    }

    public static String getRepresentativeNames(List<RepresentedTypeRItem> repList) {
        return repList.stream()
            .map(RepresentedTypeRItem::getValue)
            .filter(Objects::nonNull)
            .map(RepresentedTypeR::getNameOfRepresentative)
            .filter(name -> !isNullOrEmpty(name))
            .distinct()
            .collect(Collectors.joining(STRING_COMMA_WITH_SPACE));
    }

    public static List<String> getRepresentativeEmails(List<RepresentedTypeRItem> repList) {
        return repList.stream()
            .map(RepresentedTypeRItem::getValue)
            .filter(Objects::nonNull)
            .map(RepresentedTypeR::getRepresentativeEmailAddress)
            .filter(email -> !isNullOrEmpty(email))
            .distinct()
            .toList();
    }

    public static String getRespondentPartyNames(List<RepresentedTypeRItem> repList) {
        List<String> respondentNames = repList.stream()
            .map(RepresentedTypeRItem::getValue)
            .filter(Objects::nonNull)
            .map(RepresentedTypeR::getRespRepName)
            .filter(Objects::nonNull)
            .toList();
        return String.join(STRING_COMMA_WITH_SPACE, respondentNames);
    }

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

    public static String getRespondentEmail(RespondentSumType respondent) {
        return StringUtils.isNotBlank(respondent.getResponseRespondentEmail())
            ? respondent.getResponseRespondentEmail()
            : respondent.getRespondentEmail();
    }
}

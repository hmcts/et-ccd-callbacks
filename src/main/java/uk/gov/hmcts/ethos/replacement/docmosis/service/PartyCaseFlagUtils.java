package uk.gov.hmcts.ethos.replacement.docmosis.service;

import uk.gov.hmcts.et.common.model.ccd.items.FlagDetailType;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.AllPartyFlags;
import uk.gov.hmcts.et.common.model.ccd.types.CaseFlagsType;

import java.util.stream.Stream;

final class PartyCaseFlagUtils {
    private PartyCaseFlagUtils() {
    }

    static Stream<FlagDetailType> respondentFlagDetails(AllPartyFlags flags) {
        if (flags == null) {
            return Stream.empty();
        }
        return flagDetails(
                flags.getRespondentFlags(), flags.getRespondentExternalFlags(),
                flags.getRespondent1Flags(), flags.getRespondent1ExternalFlags(),
                flags.getRespondent2Flags(), flags.getRespondent2ExternalFlags(),
                flags.getRespondent3Flags(), flags.getRespondent3ExternalFlags(),
                flags.getRespondent4Flags(), flags.getRespondent4ExternalFlags(),
                flags.getRespondent5Flags(), flags.getRespondent5ExternalFlags(),
                flags.getRespondent6Flags(), flags.getRespondent6ExternalFlags(),
                flags.getRespondent7Flags(), flags.getRespondent7ExternalFlags(),
                flags.getRespondent8Flags(), flags.getRespondent8ExternalFlags(),
                flags.getRespondent9Flags(), flags.getRespondent9ExternalFlags());
    }

    static Stream<GenericTypeItem<FlagDetailType>> allFlagItems(AllPartyFlags flags) {
        if (flags == null) {
            return Stream.empty();
        }
        return Stream.concat(
                flagItems(flags.getClaimantFlags(), flags.getClaimantExternalFlags()),
                Stream.concat(respondentFlagItems(flags), representativeFlagItems(flags)));
    }

    static Stream<FlagDetailType> flagDetails(CaseFlagsType... flags) {
        return flagItems(flags).map(GenericTypeItem::getValue);
    }

    private static Stream<GenericTypeItem<FlagDetailType>> respondentFlagItems(AllPartyFlags flags) {
        return flagItems(
                flags.getRespondentFlags(), flags.getRespondentExternalFlags(),
                flags.getRespondent1Flags(), flags.getRespondent1ExternalFlags(),
                flags.getRespondent2Flags(), flags.getRespondent2ExternalFlags(),
                flags.getRespondent3Flags(), flags.getRespondent3ExternalFlags(),
                flags.getRespondent4Flags(), flags.getRespondent4ExternalFlags(),
                flags.getRespondent5Flags(), flags.getRespondent5ExternalFlags(),
                flags.getRespondent6Flags(), flags.getRespondent6ExternalFlags(),
                flags.getRespondent7Flags(), flags.getRespondent7ExternalFlags(),
                flags.getRespondent8Flags(), flags.getRespondent8ExternalFlags(),
                flags.getRespondent9Flags(), flags.getRespondent9ExternalFlags());
    }

    private static Stream<GenericTypeItem<FlagDetailType>> representativeFlagItems(AllPartyFlags flags) {
        return flagItems(
                flags.getClaimantRepresentativeFlags(), flags.getClaimantRepresentativeExternalFlags(),
                flags.getRepresentativeFlags(), flags.getRepresentativeExternalFlags(),
                flags.getRepresentative1Flags(), flags.getRepresentative1ExternalFlags(),
                flags.getRepresentative2Flags(), flags.getRepresentative2ExternalFlags(),
                flags.getRepresentative3Flags(), flags.getRepresentative3ExternalFlags(),
                flags.getRepresentative4Flags(), flags.getRepresentative4ExternalFlags(),
                flags.getRepresentative5Flags(), flags.getRepresentative5ExternalFlags(),
                flags.getRepresentative6Flags(), flags.getRepresentative6ExternalFlags(),
                flags.getRepresentative7Flags(), flags.getRepresentative7ExternalFlags(),
                flags.getRepresentative8Flags(), flags.getRepresentative8ExternalFlags(),
                flags.getRepresentative9Flags(), flags.getRepresentative9ExternalFlags());
    }

    private static Stream<GenericTypeItem<FlagDetailType>> flagItems(CaseFlagsType... flags) {
        return Stream.of(flags)
                .filter(flag -> flag != null && flag.getDetails() != null)
                .flatMap(flag -> flag.getDetails().stream())
                .filter(item -> item != null && item.getValue() != null);
    }
}

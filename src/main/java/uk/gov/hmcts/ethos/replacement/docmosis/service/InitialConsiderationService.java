package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.EnumUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.exceptions.DocumentManagementException;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;
import uk.gov.hmcts.et.common.model.ccd.items.DateListedTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.HearingTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.JurCodesTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.HearingType;
import uk.gov.hmcts.et.common.model.ccd.types.JurCodesType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.JurisdictionCode;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.IntWrapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.DocumentHelper.getHearingDuration;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper.nullCheck;

@Service
@RequiredArgsConstructor
public class InitialConsiderationService {

    private final TornadoService tornadoService;

    private static final String RESPONDENT_NAME =
        "| Respondent %s name given | |\r\n"
            + "|-------------|:------------|\r\n"
            + "|In ET1 by claimant | %s|\r\n"
            + "|In ET3 by respondent | %s|\r\n"
            + "\r\n";

    private static final String HEARING_DETAILS =
        "|Hearing details | |\r\n"
            + "|-------------|:------------|\r\n"
            + "|Date | %s|\r\n"
            + "|Type | %s|\r\n"
            + "|Duration | %s|";

    private static final String JURISDICTION_HEADER = "<h2>Jurisdiction codes</h2><a target=\"_blank\" "
        + "href=\"https://intranet.justice.gov.uk/documents/2017/11/jurisdiction-list.pdf\">View all "
        + "jurisdiction codes and descriptors (opens in new tab)</a><br><br>";
    private static final String HEARING_MISSING = String.format(HEARING_DETAILS, "-", "-", "-");
    private static final String RESPONDENT_MISSING = String.format(RESPONDENT_NAME, "", "", "");
    private static final String DOCGEN_ERROR = "Failed to generate document for case id: %s";
    private static final String IC_OUTPUT_NAME = "Initial Consideration.pdf";

    /**
     * Creates the respondent detail section for Initial Consideration.
     * Only shows details from the first record
     *
     * @param respondentCollection collection of respondents
     * @return table with respondent details
     */
    public String getRespondentName(List<RespondentSumTypeItem> respondentCollection) {
        if (respondentCollection == null) {
            return RESPONDENT_MISSING;
        }

        IntWrapper respondentCount = new IntWrapper(0);
        return respondentCollection.stream()
                .map(respondent -> String.format(
                        RESPONDENT_NAME,
                        respondentCollection.size() > 1 ? respondentCount.incrementAndReturnValue() : "",
                        nullCheck(respondent.getValue().getRespondentName()),
                        nullCheck(respondent.getValue().getResponseRespondentName())))
                .collect(Collectors.joining());
    }

    /**
     * Creates hearing detail section for Initial Consideration.
     * Display details of the hearing with the earliest hearing date from the collection of hearings
     *
     * @param hearingCollection the collection of hearings
     * @return return table with details of hearing
     */
    public String getHearingDetails(List<HearingTypeItem> hearingCollection) {
        if (hearingCollection == null) {
            return HEARING_MISSING;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");

        return hearingCollection.stream()
            .filter(hearingTypeItem -> hearingTypeItem != null && hearingTypeItem.getValue() != null)
            .map(HearingTypeItem::getValue)
            .filter(
                hearing -> hearing.getHearingDateCollection() != null && !hearing.getHearingDateCollection().isEmpty())
            .min(
                Comparator.comparing(
                    (HearingType hearing) ->
                        getEarliestHearingDate(hearing.getHearingDateCollection()).orElse(
                            LocalDate.now().plusYears(100))))
            .map(hearing -> String.format(HEARING_DETAILS,
                getEarliestHearingDate(hearing.getHearingDateCollection()).map(formatter::format).orElse(""),
                hearing.getHearingType(),
                getHearingDuration(hearing)))
            .orElse(HEARING_MISSING);
    }

    public Optional<LocalDate> getEarliestHearingDate(List<DateListedTypeItem> hearingDates) {
        return hearingDates.stream()
            .filter(dateListedTypeItem -> dateListedTypeItem != null && dateListedTypeItem.getValue() != null)
            .map(DateListedTypeItem::getValue)
            .filter(hearingDate -> hearingDate.getListedDate() != null && !hearingDate.getListedDate().isEmpty())
            .map(hearingDateItem -> LocalDateTime.parse(hearingDateItem.getListedDate()).toLocalDate())
            .min(Comparator.naturalOrder());
    }

    /**
     * Creates the jurisdiction section for Initial Consideration.
     *
     * @param jurisdictionCodes the list of jurisdiction codes assigned to the case
     * @return jurisdiction code section
     */
    public String generateJurisdictionCodesHtml(List<JurCodesTypeItem> jurisdictionCodes) {
        if (jurisdictionCodes == null) {
            return "";
        }

        List<String> validJurisdictionCodes = jurisdictionCodes.stream().map(JurCodesTypeItem::getValue)
            .map(JurCodesType::getJuridictionCodesList)
            .filter(code -> EnumUtils.isValidEnum(JurisdictionCode.class, code))
            .collect(Collectors.toList());

        if (validJurisdictionCodes.isEmpty()) {
            return  "";
        }

        StringBuilder sb = new StringBuilder()
            .append(JURISDICTION_HEADER);

        validJurisdictionCodes
            .forEach(codeName -> sb.append("<strong>")
                .append(codeName)
                .append("</strong>")
                .append(" - ")
                .append(JurisdictionCode.valueOf(codeName).getDescription())
                .append("<br><br>"));

        return sb.append("<hr>").toString();
    }

    /**
     * This calls the Tornado service to generate the pdf for the ET1 Vetting journey.
     * @param caseData gets the casedata
     * @param userToken user authentication token
     * @param caseTypeId reference which casetype the document will be uploaded to
     * @return DocumentInfo which contains the url and markup for the uploaded document
     */
    public DocumentInfo generateDocument(CaseData caseData, String userToken, String caseTypeId) {
        try {
            return tornadoService.generateEt1VettingDocument(caseData, userToken, caseTypeId, IC_OUTPUT_NAME);
        } catch (Exception e) {
            throw new DocumentManagementException(String.format(DOCGEN_ERROR, caseData.getEthosCaseReference()), e);
        }
    }
}

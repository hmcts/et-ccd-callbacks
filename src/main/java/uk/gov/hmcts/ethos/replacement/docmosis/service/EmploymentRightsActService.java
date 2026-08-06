package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.JurCodesTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.AdditionalCaseInfoType;
import uk.gov.hmcts.et.common.model.ccd.types.JurCodesType;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.FlagsImageHelper;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.UUID;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

/**
 * Service class dedicated to logic relating to the Employment Rights Act.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmploymentRightsActService {

    private static final LocalDate ERA_START_DATE = LocalDate.of(2026, Month.OCTOBER, 1);
    public static final String UDL_JURISDICTION_CODE = "UDL";
    public static final String NOT_APPLICABLE = "Not applicable";

    /**
     * Sets the era flag on AdditionalCaseInfoType to No if receiptDate is before 1st October 2026.
     *
     * @param caseData the case data
     */
    public void setEraFlagByReceiptDate(CaseData caseData) {
        if (ObjectUtils.isEmpty(caseData) || ObjectUtils.isEmpty(caseData.getReceiptDate())) {
            return;
        }

        try {
            LocalDate receiptDate = LocalDate.parse(caseData.getReceiptDate());
            if (receiptDate.isBefore(ERA_START_DATE)) {
                if (ObjectUtils.isEmpty(caseData.getAdditionalCaseInfoType())) {
                    caseData.setAdditionalCaseInfoType(new AdditionalCaseInfoType());
                }
                caseData.getAdditionalCaseInfoType().setEra(NO);
            }
        } catch (Exception e) {
            log.error("Error parsing receiptDate: {}", caseData.getReceiptDate(), e);
        }
    }

    /**
     * Sets etICUnfairDismissalEra to Not applicable if receiptDate is before 1st October 2026.
     *
     * @param caseData the case data
     */
    public void setUnfairDismissalEraByReceiptDate(CaseData caseData) {
        if (ObjectUtils.isEmpty(caseData) || ObjectUtils.isEmpty(caseData.getReceiptDate())) {
            return;
        }

        try {
            LocalDate receiptDate = LocalDate.parse(caseData.getReceiptDate());
            if (receiptDate.isBefore(ERA_START_DATE)) {
                caseData.setEtICUnfairDismissalEra(NOT_APPLICABLE);
            }
        } catch (Exception e) {
            log.error("Error parsing receiptDate: {}", caseData.getReceiptDate(), e);
        }
    }

    /**
     * Processes the Initial Consideration response for unfair dismissal ERA question.
     * When etICUnfairDismissalEra response is "Yes":
     * - Adds jurisdiction code UDL to Jurisdictions if not present.
     * - Sets ERA radio button (era) to "Yes" on AdditionalCaseInfoType.
     * - Displays ERA flag.
     *
     * @param caseTypeId the case type ID
     * @param caseData the case data
     */
    public void processUnfairDismissalEra(String caseTypeId, CaseData caseData) {
        if (caseData == null || !YES.equalsIgnoreCase(caseData.getEtICUnfairDismissalEra())) {
            return;
        }

        if (ObjectUtils.isEmpty(caseData.getAdditionalCaseInfoType())) {
            caseData.setAdditionalCaseInfoType(new AdditionalCaseInfoType());
        }

        caseData.getAdditionalCaseInfoType().setEra(YES);

        if (caseData.getJurCodesCollection() == null) {
            caseData.setJurCodesCollection(new ArrayList<>());
        }

        boolean hasUdl = caseData.getJurCodesCollection().stream()
                .anyMatch(item -> item.getValue() != null
                        && UDL_JURISDICTION_CODE.equalsIgnoreCase(item.getValue().getJuridictionCodesList()));

        if (!hasUdl) {
            JurCodesType jurCodesType = new JurCodesType();
            jurCodesType.setJuridictionCodesList(UDL_JURISDICTION_CODE);
            JurCodesTypeItem item = new JurCodesTypeItem();
            item.setId(UUID.randomUUID().toString());
            item.setValue(jurCodesType);
            caseData.getJurCodesCollection().add(item);
        }

        FlagsImageHelper.buildFlagsImageFileName(caseTypeId, caseData);
    }
}

package uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3;

import lombok.SneakyThrows;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.util.ResourceLoader;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.platform.commons.util.StringUtils.isNotBlank;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.ANNUALLY_CAPITALISED;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.ANNUALLY_LOWERCASE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.CHECKBOX_PDF_ACAS_FIELD_AGREEMENT_NO;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.CHECKBOX_PDF_ACAS_FIELD_AGREEMENT_YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.CHECKBOX_PDF_DISABILITY_NO;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.CHECKBOX_PDF_DISABILITY_NOT_SURE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.CHECKBOX_PDF_DISABILITY_YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.CHECKBOX_PDF_EARNINGS_BENEFITS_FIELD_EARNING_DETAILS_CORRECT_NO;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.CHECKBOX_PDF_EARNINGS_BENEFITS_FIELD_EARNING_DETAILS_CORRECT_NOT_APPLICABLE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.CHECKBOX_PDF_EARNINGS_BENEFITS_FIELD_EARNING_DETAILS_CORRECT_YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.CHECKBOX_PDF_EARNINGS_BENEFITS_FIELD_HOURS_OF_WORK_CORRECT_NO;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.CHECKBOX_PDF_EARNINGS_BENEFITS_FIELD_HOURS_OF_WORK_CORRECT_NOT_APPLICABLE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.CHECKBOX_PDF_EARNINGS_BENEFITS_FIELD_HOURS_OF_WORK_CORRECT_YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.CHECKBOX_PDF_EARNINGS_BENEFITS_FIELD_NORMAL_TAKE_HOME_PAY_ANNUALLY;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.CHECKBOX_PDF_EARNINGS_BENEFITS_FIELD_NORMAL_TAKE_HOME_PAY_MONTHLY;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.CHECKBOX_PDF_EARNINGS_BENEFITS_FIELD_NORMAL_TAKE_HOME_PAY_WEEKLY;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.CHECKBOX_PDF_EARNINGS_BENEFITS_FIELD_NOTICE_PERIOD_CORRECT_NO;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.CHECKBOX_PDF_EARNINGS_BENEFITS_FIELD_NOTICE_PERIOD_CORRECT_NOT_APPLICABLE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.CHECKBOX_PDF_EARNINGS_BENEFITS_FIELD_NOTICE_PERIOD_CORRECT_YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.CHECKBOX_PDF_EARNINGS_BENEFITS_FIELD_PAY_BEFORE_TAX_ANNUALLY;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.CHECKBOX_PDF_EARNINGS_BENEFITS_FIELD_PAY_BEFORE_TAX_MONTHLY;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.CHECKBOX_PDF_EARNINGS_BENEFITS_FIELD_PAY_BEFORE_TAX_WEEKLY;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.CHECKBOX_PDF_EARNINGS_BENEFITS_FIELD_PENSION_AND_OTHER_BENEFITS_CORRECT_NO;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.CHECKBOX_PDF_EARNINGS_BENEFITS_FIELD_PENSION_AND_OTHER_BENEFITS_CORRECT_NOT_APPLICABLE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.CHECKBOX_PDF_EARNINGS_BENEFITS_FIELD_PENSION_AND_OTHER_BENEFITS_CORRECT_YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.CHECKBOX_PDF_EMPLOYER_CONTRACT_CLAIM_FIELD_YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.CHECKBOX_PDF_EMPLOYMENT_FIELD_CONTINUES_NO;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.CHECKBOX_PDF_EMPLOYMENT_FIELD_CONTINUES_NOT_APPLICABLE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.CHECKBOX_PDF_EMPLOYMENT_FIELD_CONTINUES_YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.CHECKBOX_PDF_EMPLOYMENT_FIELD_DATES_CORRECT_NO;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.CHECKBOX_PDF_EMPLOYMENT_FIELD_DATES_CORRECT_NOT_APPLICABLE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.CHECKBOX_PDF_EMPLOYMENT_FIELD_DATES_CORRECT_YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.CHECKBOX_PDF_EMPLOYMENT_FIELD_JOB_TITLE_CORRECT_NO;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.CHECKBOX_PDF_EMPLOYMENT_FIELD_JOB_TITLE_CORRECT_NOT_APPLICABLE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.CHECKBOX_PDF_EMPLOYMENT_FIELD_JOB_TITLE_CORRECT_YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.CHECKBOX_PDF_REPRESENTATIVE_FIELD_COMMUNICATION_PREFERENCE_EMAIL;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.CHECKBOX_PDF_REPRESENTATIVE_FIELD_COMMUNICATION_PREFERENCE_POST;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.CHECKBOX_PDF_REPRESENTATIVE_FIELD_PHONE_HEARINGS;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.CHECKBOX_PDF_REPRESENTATIVE_FIELD_VIDEO_HEARINGS;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.CHECKBOX_PDF_RESPONDENT_EXPECTED_VALUE_TITLE_MISS;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.CHECKBOX_PDF_RESPONDENT_EXPECTED_VALUE_TITLE_MR;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.CHECKBOX_PDF_RESPONDENT_EXPECTED_VALUE_TITLE_MRS;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.CHECKBOX_PDF_RESPONDENT_EXPECTED_VALUE_TITLE_MS;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.CHECKBOX_PDF_RESPONDENT_FIELD_CONTACT_TYPE_EMAIL;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.CHECKBOX_PDF_RESPONDENT_FIELD_CONTACT_TYPE_POST;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.CHECKBOX_PDF_RESPONDENT_FIELD_HEARING_TYPE_PHONE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.CHECKBOX_PDF_RESPONDENT_FIELD_HEARING_TYPE_VIDEO;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.CHECKBOX_PDF_RESPONDENT_FIELD_MORE_THAN_ONE_SITE_GREAT_BRITAIN_NO;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.CHECKBOX_PDF_RESPONDENT_FIELD_MORE_THAN_ONE_SITE_GREAT_BRITAIN_YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.CHECKBOX_PDF_RESPONDENT_FIELD_TITLE_MISS;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.CHECKBOX_PDF_RESPONDENT_FIELD_TITLE_MR;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.CHECKBOX_PDF_RESPONDENT_FIELD_TITLE_MRS;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.CHECKBOX_PDF_RESPONDENT_FIELD_TITLE_MS;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.CHECKBOX_PDF_RESPONDENT_FIELD_TITLE_OTHER;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.CHECKBOX_PDF_RESPONSE_FIELD_CONTEST_CLAIM_NO;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.CHECKBOX_PDF_RESPONSE_FIELD_CONTEST_CLAIM_YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.EMAIL_CAPITALISED;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.EMAIL_LOWERCASE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.MONTHLY_CAPITALISED;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.MONTHLY_LOWERCASE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.NO_CAPITALISED;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.NO_LOWERCASE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.POST_CAPITALISED;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.POST_LOWERCASE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.STRING_EMPTY;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.TXT_PDF_ACAS_FIELD_AGREEMENT_NO_REASON;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.TXT_PDF_CLAIMANT_FIELD_NAME;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.TXT_PDF_DISABILITY_DETAILS;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.TXT_PDF_EARNINGS_BENEFITS_FIELD_NORMAL_TAKE_HOME_PAY;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.TXT_PDF_EARNINGS_BENEFITS_FIELD_NOTICE_PERIOD_NOT_CORRECT_INFORMATION;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.TXT_PDF_EARNINGS_BENEFITS_FIELD_PAY_BEFORE_TAX;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.TXT_PDF_EARNINGS_BENEFITS_FIELD_PENSION_AND_OTHER_BENEFITS_NOT_CORRECT_INFORMATION;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.TXT_PDF_EARNINGS_BENEFITS_FIELD_WORK_HOURS_DETAILS;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.TXT_PDF_EMPLOYER_CONTRACT_CLAIM_FIELD_DETAILS;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.TXT_PDF_EMPLOYMENT_FIELD_DATES_FURTHER_INFO;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.TXT_PDF_EMPLOYMENT_FIELD_END_DATE_DAY;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.TXT_PDF_EMPLOYMENT_FIELD_END_DATE_MONTH;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.TXT_PDF_EMPLOYMENT_FIELD_END_DATE_YEAR;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.TXT_PDF_EMPLOYMENT_FIELD_JOB_TITLE_CORRECT_DETAILS;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.TXT_PDF_EMPLOYMENT_FIELD_START_DATE_DAY;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.TXT_PDF_EMPLOYMENT_FIELD_START_DATE_MONTH;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.TXT_PDF_EMPLOYMENT_FIELD_START_DATE_YEAR;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.TXT_PDF_HEADER_FIELD_CASE_NUMBER;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.TXT_PDF_HEADER_FIELD_DATE_RECEIVED;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.TXT_PDF_HEADER_FIELD_RFT;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.TXT_PDF_HEADER_VALUE_ADDITIONAL_DOCUMENT_EXISTS;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.TXT_PDF_HEADER_VALUE_ADDITIONAL_DOCUMENT_NOT_EXISTS;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.TXT_PDF_REPRESENTATIVE_FIELD_ADDRESS;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.TXT_PDF_REPRESENTATIVE_FIELD_EMAIL_ADDRESS;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.TXT_PDF_REPRESENTATIVE_FIELD_MOBILE_PHONE_NUMBER;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.TXT_PDF_REPRESENTATIVE_FIELD_NAME;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.TXT_PDF_REPRESENTATIVE_FIELD_ORGANISATION_NAME;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.TXT_PDF_REPRESENTATIVE_FIELD_PHONE_NUMBER;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.TXT_PDF_REPRESENTATIVE_FIELD_POSTCODE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.TXT_PDF_REPRESENTATIVE_FIELD_REFERENCE_FOR_CORRESPONDENCE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.TXT_PDF_RESPONDENT_FIELD_ADDRESS;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.TXT_PDF_RESPONDENT_FIELD_CONTACT_NAME;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.TXT_PDF_RESPONDENT_FIELD_DX;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.TXT_PDF_RESPONDENT_FIELD_EMAIL;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.TXT_PDF_RESPONDENT_FIELD_EMPLOYEE_NUMBER_CLAIMANT_WORK_PLACE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.TXT_PDF_RESPONDENT_FIELD_EMPLOYEE_NUMBER_GREAT_BRITAIN;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.TXT_PDF_RESPONDENT_FIELD_MOBILE_NUMBER;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.TXT_PDF_RESPONDENT_FIELD_NAME;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.TXT_PDF_RESPONDENT_FIELD_NUMBER;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.TXT_PDF_RESPONDENT_FIELD_PHONE_NUMBER;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.TXT_PDF_RESPONDENT_FIELD_POSTCODE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.TXT_PDF_RESPONDENT_FIELD_TITLE_OTHER;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.TXT_PDF_RESPONDENT_FIELD_TYPE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.TXT_PDF_RESPONSE_FIELD_CONTEST_CLAIM_CORRECT_FACTS;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.WEEKLY_CAPITALISED;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.WEEKLY_LOWERCASE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.YES_CAPITALISED;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormConstants.YES_LOWERCASE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormMapper.mapEt3Form;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_DUMMY_VALUE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_ET3_FORM_CASE_DATA_FILE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_PDF_CASE_DATA_NOT_FOUND_EXCEPTION_MESSAGE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_PDF_CLAIMANT_VALUE_ET1_CLAIMANT_NAME;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_PDF_CLAIMANT_VALUE_ET3_CLAIMANT_NAME;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_PDF_DISABILITY_DETAIL;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_PDF_EARNINGS_BENEFITS_CORRECT_HOURS;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_PDF_EARNINGS_BENEFITS_CORRECT_NOTICE_PERIOD;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_PDF_EARNINGS_BENEFITS_CORRECT_PENSION_AND_OTHER_BENEFITS;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_PDF_EARNINGS_BENEFITS_PAY_BEFORE_TAX;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_PDF_EARNINGS_BENEFITS_PAY_TAKE_HOME;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_PDF_EMPLOYMENT_CORRECT_JOB_TITLE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_PDF_EMPLOYMENT_DATE_INFORMATION;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_PDF_EMPLOYMENT_END_DAY;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_PDF_EMPLOYMENT_END_MONTH;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_PDF_EMPLOYMENT_END_YEAR;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_PDF_EMPLOYMENT_START_DAY;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_PDF_EMPLOYMENT_START_MONTH;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_PDF_EMPLOYMENT_START_YEAR;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_PDF_HEADER_VALUE_CASE_NUMBER;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_PDF_HEADER_VALUE_DATE_RECEIVED;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_PDF_REPRESENTATIVE_ADDRESS;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_PDF_REPRESENTATIVE_EMAIL_ADDRESS;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_PDF_REPRESENTATIVE_MOBILE_PHONE_NUMBER;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_PDF_REPRESENTATIVE_NAME;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_PDF_REPRESENTATIVE_ORGANISATION_NAME;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_PDF_REPRESENTATIVE_PHONE_NUMBER;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_PDF_REPRESENTATIVE_POSTCODE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_PDF_REPRESENTATIVE_REFERENCE_FOR_CORRESPONDENCE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_PDF_RESPONDENT_COLLECTION_NOT_FOUND_EXCEPTION_MESSAGE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_PDF_RESPONDENT_EXPECTED_VALUE_ADDRESS;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_PDF_RESPONDENT_EXPECTED_VALUE_CONTACT_NAME;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_PDF_RESPONDENT_EXPECTED_VALUE_CONTACT_TYPE_EMAIL;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_PDF_RESPONDENT_EXPECTED_VALUE_DX;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_PDF_RESPONDENT_EXPECTED_VALUE_EMAIL;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_PDF_RESPONDENT_EXPECTED_VALUE_EMPLOYEE_NUMBER_CLAIMANT_WORK_PLACE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_PDF_RESPONDENT_EXPECTED_VALUE_EMPLOYEE_NUMBER_GREAT_BRITAIN;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_PDF_RESPONDENT_EXPECTED_VALUE_HEARING_TYPE_PHONE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_PDF_RESPONDENT_EXPECTED_VALUE_HEARING_TYPE_VIDEO;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_PDF_RESPONDENT_EXPECTED_VALUE_MOBILE_NUMBER;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_PDF_RESPONDENT_EXPECTED_VALUE_MORE_THAN_ONE_SITE_GREAT_BRITAIN_YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_PDF_RESPONDENT_EXPECTED_VALUE_NAME;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_PDF_RESPONDENT_EXPECTED_VALUE_NUMBER;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_PDF_RESPONDENT_EXPECTED_VALUE_PHONE_NUMBER;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_PDF_RESPONDENT_EXPECTED_VALUE_POSTCODE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_PDF_RESPONDENT_EXPECTED_VALUE_TYPE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_PDF_RESPONDENT_NAME_NOT_FOUND_IN_CASE_DATA_EXCEPTION_MESSAGE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_PDF_RESPONDENT_NOT_FOUND_IN_CASE_DATA_EXCEPTION_MESSAGE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_PDF_RESPONDENT_NOT_FOUND_IN_RESPONDENT_COLLECTION_EXCEPTION_MESSAGE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_PDF_RESPONSE_CONTEST_CLAIM_CORRECT_FACTS;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_PDF_RESPONSE_EMPLOYER_CONTRACT_CLAIM_CORRECT_DETAILS;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.util.ET3FormTestUtil.getCheckBoxNotApplicableValue;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.util.ET3FormTestUtil.getCheckboxValue;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.util.ET3FormTestUtil.getCorrectedCheckboxValue;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.util.ET3FormTestUtil.getCorrectedDetailValue;

class ET3FormMapperTest {

    @ParameterizedTest
    @MethodSource("provideMapCaseTestData")
    @SneakyThrows
    void testMapRespondent(CaseData caseData) {
        if (ObjectUtils.isEmpty(caseData)) {
            assertThatThrownBy(() -> mapEt3Form(caseData)).hasMessage(TEST_PDF_CASE_DATA_NOT_FOUND_EXCEPTION_MESSAGE);
            return;
        }
        if (CollectionUtils.isEmpty(caseData.getRespondentCollection())) {
            assertThatThrownBy(() -> mapEt3Form(caseData))
                    .hasMessage(TEST_PDF_RESPONDENT_COLLECTION_NOT_FOUND_EXCEPTION_MESSAGE);
            return;
        }
        if (ObjectUtils.isEmpty(caseData.getSubmitEt3Respondent())) {
            assertThatThrownBy(() -> mapEt3Form(caseData))
                    .hasMessage(TEST_PDF_RESPONDENT_NOT_FOUND_IN_CASE_DATA_EXCEPTION_MESSAGE);
            return;
        }
        if (ObjectUtils.isEmpty(caseData.getSubmitEt3Respondent().getValue())) {
            assertThatThrownBy(() -> mapEt3Form(caseData)).hasMessage(
                    TEST_PDF_RESPONDENT_NOT_FOUND_IN_CASE_DATA_EXCEPTION_MESSAGE);
            return;
        }
        if (isBlank(caseData.getSubmitEt3Respondent().getValue().getLabel())) {
            assertThatThrownBy(() -> mapEt3Form(caseData)).hasMessage(
                    TEST_PDF_RESPONDENT_NAME_NOT_FOUND_IN_CASE_DATA_EXCEPTION_MESSAGE);
            return;
        }
        if (TEST_DUMMY_VALUE.equals(caseData.getSubmitEt3Respondent().getValue().getLabel())) {
            assertThatThrownBy(() -> mapEt3Form(caseData))
                    .hasMessage(TEST_PDF_RESPONDENT_NOT_FOUND_IN_RESPONDENT_COLLECTION_EXCEPTION_MESSAGE);
            return;
        }
        RespondentSumType respondentSumType = caseData.getRespondentCollection().stream()
                .filter(r -> caseData.getSubmitEt3Respondent()
                        .getSelectedLabel().equals(r.getValue().getRespondentName()))
                .toList().get(0).getValue();
        Map<String, Optional<String>> pdfFields = mapEt3Form(caseData);
        checkRespondent(pdfFields, respondentSumType);
        checkHeader(pdfFields, respondentSumType);
        checkClaimant(pdfFields, respondentSumType);
        checkAcas(pdfFields, respondentSumType);
        checkEmployment(pdfFields, respondentSumType);
        checkEarningAndBenefits(pdfFields, respondentSumType);
        checkResponse(pdfFields, respondentSumType);
        checkEmployerContractClaim(pdfFields, respondentSumType);
        checkRepresentative(pdfFields, caseData);
        checkDisability(pdfFields, respondentSumType);
    }

    private static void checkClaimant(Map<String, Optional<String>> pdfFields, RespondentSumType respondentSumType) {
        if (isBlank(respondentSumType.getEt3ResponseClaimantNameCorrection())) {
            assertThat(pdfFields.get(TXT_PDF_CLAIMANT_FIELD_NAME))
                    .contains(TEST_PDF_CLAIMANT_VALUE_ET1_CLAIMANT_NAME);
        } else {
            assertThat(pdfFields.get(TXT_PDF_CLAIMANT_FIELD_NAME))
                    .contains(TEST_PDF_CLAIMANT_VALUE_ET3_CLAIMANT_NAME);
        }
    }

    private static void checkHeader(Map<String, Optional<String>> pdfFields, RespondentSumType respondentSumType) {
        assertThat(pdfFields.get(TXT_PDF_HEADER_FIELD_CASE_NUMBER)).contains(TEST_PDF_HEADER_VALUE_CASE_NUMBER);
        assertThat(pdfFields.get(TXT_PDF_HEADER_FIELD_DATE_RECEIVED)).contains(TEST_PDF_HEADER_VALUE_DATE_RECEIVED);
        if (ObjectUtils.isEmpty(respondentSumType.getEt3ResponseRespondentSupportDocument())
                && ObjectUtils.isEmpty(respondentSumType.getEt3ResponseEmployerClaimDocument())
                && CollectionUtils.isEmpty(respondentSumType.getEt3ResponseContestClaimDocument())) {
            assertThat(pdfFields.get(TXT_PDF_HEADER_FIELD_RFT))
                    .contains(TXT_PDF_HEADER_VALUE_ADDITIONAL_DOCUMENT_NOT_EXISTS);
        } else {
            assertThat(pdfFields.get(TXT_PDF_HEADER_FIELD_RFT))
                    .contains(TXT_PDF_HEADER_VALUE_ADDITIONAL_DOCUMENT_EXISTS);
        }
    }

    private static void checkRespondent(Map<String, Optional<String>> pdfFields, RespondentSumType respondentSumType) {
        String selectedTitle = respondentSumType.getEt3ResponseRespondentPreferredTitle();
        assertThat(pdfFields.get(CHECKBOX_PDF_RESPONDENT_FIELD_TITLE_MR)).contains(
                CHECKBOX_PDF_RESPONDENT_EXPECTED_VALUE_TITLE_MR.equals(selectedTitle)
                        ? YES_CAPITALISED : STRING_EMPTY);
        assertThat(pdfFields.get(CHECKBOX_PDF_RESPONDENT_FIELD_TITLE_MS)).contains(
                CHECKBOX_PDF_RESPONDENT_EXPECTED_VALUE_TITLE_MS.equals(selectedTitle)
                        ? YES_CAPITALISED : STRING_EMPTY);
        assertThat(pdfFields.get(CHECKBOX_PDF_RESPONDENT_FIELD_TITLE_MRS)).contains(
                CHECKBOX_PDF_RESPONDENT_EXPECTED_VALUE_TITLE_MRS.equals(selectedTitle)
                        ? YES_CAPITALISED : STRING_EMPTY);
        assertThat(pdfFields.get(CHECKBOX_PDF_RESPONDENT_FIELD_TITLE_MISS)).contains(
                CHECKBOX_PDF_RESPONDENT_EXPECTED_VALUE_TITLE_MISS.equals(selectedTitle)
                        ? YES_CAPITALISED : STRING_EMPTY);
        assertThat(pdfFields.get(CHECKBOX_PDF_RESPONDENT_FIELD_TITLE_OTHER)).contains(isOtherTitle(selectedTitle)
                ? YES_CAPITALISED : STRING_EMPTY);
        assertThat(pdfFields.get(TXT_PDF_RESPONDENT_FIELD_TITLE_OTHER)).contains(isOtherTitle(selectedTitle)
                ? selectedTitle : STRING_EMPTY);
        assertThat(pdfFields.get(TXT_PDF_RESPONDENT_FIELD_NAME)).contains(TEST_PDF_RESPONDENT_EXPECTED_VALUE_NAME);
        assertThat(pdfFields.get(TXT_PDF_RESPONDENT_FIELD_NUMBER)).contains(TEST_PDF_RESPONDENT_EXPECTED_VALUE_NUMBER);
        assertThat(pdfFields.get(TXT_PDF_RESPONDENT_FIELD_TYPE)).contains(TEST_PDF_RESPONDENT_EXPECTED_VALUE_TYPE);
        assertThat(pdfFields.get(TXT_PDF_RESPONDENT_FIELD_CONTACT_NAME))
                .contains(TEST_PDF_RESPONDENT_EXPECTED_VALUE_CONTACT_NAME);
        assertThat(pdfFields.get(TXT_PDF_RESPONDENT_FIELD_POSTCODE))
                .contains(TEST_PDF_RESPONDENT_EXPECTED_VALUE_POSTCODE);
        assertThat(pdfFields.get(TXT_PDF_RESPONDENT_FIELD_DX)).contains(TEST_PDF_RESPONDENT_EXPECTED_VALUE_DX);
        assertThat(pdfFields.get(TXT_PDF_RESPONDENT_FIELD_PHONE_NUMBER))
                .contains(TEST_PDF_RESPONDENT_EXPECTED_VALUE_PHONE_NUMBER);
        assertThat(pdfFields.get(TXT_PDF_RESPONDENT_FIELD_MOBILE_NUMBER))
                .contains(TEST_PDF_RESPONDENT_EXPECTED_VALUE_MOBILE_NUMBER);
        assertThat(pdfFields.get(CHECKBOX_PDF_RESPONDENT_FIELD_CONTACT_TYPE_EMAIL))
                .contains(TEST_PDF_RESPONDENT_EXPECTED_VALUE_CONTACT_TYPE_EMAIL);
        assertThat(pdfFields.get(CHECKBOX_PDF_RESPONDENT_FIELD_CONTACT_TYPE_POST)).contains(STRING_EMPTY);
        assertThat(pdfFields.get(TXT_PDF_RESPONDENT_FIELD_EMAIL)).contains(TEST_PDF_RESPONDENT_EXPECTED_VALUE_EMAIL);
        assertThat(pdfFields.get(CHECKBOX_PDF_RESPONDENT_FIELD_HEARING_TYPE_VIDEO))
                .contains(TEST_PDF_RESPONDENT_EXPECTED_VALUE_HEARING_TYPE_VIDEO);
        assertThat(pdfFields.get(CHECKBOX_PDF_RESPONDENT_FIELD_HEARING_TYPE_PHONE))
                .contains(TEST_PDF_RESPONDENT_EXPECTED_VALUE_HEARING_TYPE_PHONE);
        assertThat(pdfFields.get(TXT_PDF_RESPONDENT_FIELD_EMPLOYEE_NUMBER_GREAT_BRITAIN))
                .contains(TEST_PDF_RESPONDENT_EXPECTED_VALUE_EMPLOYEE_NUMBER_GREAT_BRITAIN);
        assertThat(pdfFields.get(CHECKBOX_PDF_RESPONDENT_FIELD_MORE_THAN_ONE_SITE_GREAT_BRITAIN_YES))
                .contains(TEST_PDF_RESPONDENT_EXPECTED_VALUE_MORE_THAN_ONE_SITE_GREAT_BRITAIN_YES);
        assertThat(pdfFields.get(CHECKBOX_PDF_RESPONDENT_FIELD_MORE_THAN_ONE_SITE_GREAT_BRITAIN_NO))
                .contains(STRING_EMPTY);
        assertThat(pdfFields.get(TXT_PDF_RESPONDENT_FIELD_EMPLOYEE_NUMBER_CLAIMANT_WORK_PLACE))
                .contains(TEST_PDF_RESPONDENT_EXPECTED_VALUE_EMPLOYEE_NUMBER_CLAIMANT_WORK_PLACE);
        assertThat(pdfFields.get(TXT_PDF_RESPONDENT_FIELD_ADDRESS))
                .contains(TEST_PDF_RESPONDENT_EXPECTED_VALUE_ADDRESS);
    }

    private static void checkAcas(Map<String, Optional<String>> pdfFields, RespondentSumType respondentSumType) {
        String acasAgreed = respondentSumType.getEt3ResponseAcasAgree();
        assertThat(pdfFields.get(CHECKBOX_PDF_ACAS_FIELD_AGREEMENT_YES)).contains(YES_CAPITALISED.equals(acasAgreed)
                ? YES_CAPITALISED : STRING_EMPTY);
        assertThat(pdfFields.get(CHECKBOX_PDF_ACAS_FIELD_AGREEMENT_NO)).contains(NO_CAPITALISED.equals(acasAgreed)
                ? NO_CAPITALISED : STRING_EMPTY);
        assertThat(pdfFields.get(TXT_PDF_ACAS_FIELD_AGREEMENT_NO_REASON)).contains(NO_CAPITALISED.equals(acasAgreed)
                ? respondentSumType.getEt3ResponseAcasAgreeReason() : STRING_EMPTY);
    }

    private static void checkEmployment(Map<String, Optional<String>> pdfFields, RespondentSumType respondentSumType) {
        assertThat(pdfFields.get(CHECKBOX_PDF_EMPLOYMENT_FIELD_DATES_CORRECT_YES))
                .contains(getCheckboxValue(respondentSumType.getEt3ResponseAreDatesCorrect(),
                        YES_CAPITALISED, YES_LOWERCASE));
        assertThat(pdfFields.get(CHECKBOX_PDF_EMPLOYMENT_FIELD_DATES_CORRECT_NO))
                .contains(getCheckboxValue(respondentSumType.getEt3ResponseAreDatesCorrect(),
                        NO_CAPITALISED, NO_LOWERCASE));
        assertThat(pdfFields.get(CHECKBOX_PDF_EMPLOYMENT_FIELD_DATES_CORRECT_NOT_APPLICABLE))
                .contains(getCheckBoxNotApplicableValue(respondentSumType.getEt3ResponseAreDatesCorrect(),
                        List.of(YES_CAPITALISED, NO_CAPITALISED), NO_LOWERCASE));

        assertThat(pdfFields.get(TXT_PDF_EMPLOYMENT_FIELD_START_DATE_DAY))
                .contains(getCorrectedDetailValue(respondentSumType.getEt3ResponseAreDatesCorrect(), NO_CAPITALISED,
                        respondentSumType.getEt3ResponseEmploymentStartDate(), TEST_PDF_EMPLOYMENT_START_DAY));
        assertThat(pdfFields.get(TXT_PDF_EMPLOYMENT_FIELD_START_DATE_MONTH))
                .contains(getCorrectedDetailValue(respondentSumType.getEt3ResponseAreDatesCorrect(), NO_CAPITALISED,
                        respondentSumType.getEt3ResponseEmploymentStartDate(), TEST_PDF_EMPLOYMENT_START_MONTH));
        assertThat(pdfFields.get(TXT_PDF_EMPLOYMENT_FIELD_START_DATE_YEAR))
                .contains(getCorrectedDetailValue(respondentSumType.getEt3ResponseAreDatesCorrect(), NO_CAPITALISED,
                        respondentSumType.getEt3ResponseEmploymentStartDate(), TEST_PDF_EMPLOYMENT_START_YEAR));
        assertThat(pdfFields.get(TXT_PDF_EMPLOYMENT_FIELD_END_DATE_DAY))
                .contains(getCorrectedDetailValue(respondentSumType.getEt3ResponseAreDatesCorrect(), NO_CAPITALISED,
                        respondentSumType.getEt3ResponseEmploymentEndDate(), TEST_PDF_EMPLOYMENT_END_DAY));
        assertThat(pdfFields.get(TXT_PDF_EMPLOYMENT_FIELD_END_DATE_MONTH))
                .contains(getCorrectedDetailValue(respondentSumType.getEt3ResponseAreDatesCorrect(), NO_CAPITALISED,
                        respondentSumType.getEt3ResponseEmploymentEndDate(), TEST_PDF_EMPLOYMENT_END_MONTH));
        assertThat(pdfFields.get(TXT_PDF_EMPLOYMENT_FIELD_END_DATE_YEAR))
                .contains(getCorrectedDetailValue(respondentSumType.getEt3ResponseAreDatesCorrect(), NO_CAPITALISED,
                        respondentSumType.getEt3ResponseEmploymentEndDate(), TEST_PDF_EMPLOYMENT_END_YEAR));
        assertThat(pdfFields.get(TXT_PDF_EMPLOYMENT_FIELD_DATES_FURTHER_INFO))
                .contains(getCorrectedDetailValue(respondentSumType.getEt3ResponseAreDatesCorrect(),
                        NO_CAPITALISED, respondentSumType.getEt3ResponseEmploymentInformation(),
                        TEST_PDF_EMPLOYMENT_DATE_INFORMATION));
        assertThat(pdfFields.get(CHECKBOX_PDF_EMPLOYMENT_FIELD_CONTINUES_YES))
                .contains(getCheckboxValue(respondentSumType.getEt3ResponseContinuingEmployment(),
                        YES_CAPITALISED, YES_LOWERCASE));
        assertThat(pdfFields.get(CHECKBOX_PDF_EMPLOYMENT_FIELD_CONTINUES_NO))
                .contains(getCheckboxValue(respondentSumType.getEt3ResponseContinuingEmployment(),
                        NO_CAPITALISED, NO_LOWERCASE));
        assertThat(pdfFields.get(CHECKBOX_PDF_EMPLOYMENT_FIELD_CONTINUES_NOT_APPLICABLE))
                .contains(getCheckBoxNotApplicableValue(respondentSumType.getEt3ResponseContinuingEmployment(),
                        List.of(YES_CAPITALISED, NO_CAPITALISED), NO_LOWERCASE));
        assertThat(pdfFields.get(CHECKBOX_PDF_EMPLOYMENT_FIELD_JOB_TITLE_CORRECT_YES))
                .contains(getCheckboxValue(respondentSumType.getEt3ResponseIsJobTitleCorrect(),
                        YES_CAPITALISED, YES_LOWERCASE));
        assertThat(pdfFields.get(CHECKBOX_PDF_EMPLOYMENT_FIELD_JOB_TITLE_CORRECT_NO))
                .contains(getCheckboxValue(respondentSumType.getEt3ResponseIsJobTitleCorrect(),
                        NO_CAPITALISED, NO_LOWERCASE));
        assertThat(pdfFields.get(CHECKBOX_PDF_EMPLOYMENT_FIELD_JOB_TITLE_CORRECT_NOT_APPLICABLE))
                .contains(getCheckBoxNotApplicableValue(respondentSumType.getEt3ResponseIsJobTitleCorrect(),
                        List.of(YES_CAPITALISED, NO_CAPITALISED), NO_LOWERCASE));
        assertThat(pdfFields.get(TXT_PDF_EMPLOYMENT_FIELD_JOB_TITLE_CORRECT_DETAILS))
                .contains(getCorrectedDetailValue(respondentSumType.getEt3ResponseIsJobTitleCorrect(), NO_CAPITALISED,
                        respondentSumType.getEt3ResponseCorrectJobTitle(), TEST_PDF_EMPLOYMENT_CORRECT_JOB_TITLE));
    }

    private static void checkEarningAndBenefits(Map<String, Optional<String>> pdfFields,
                                                RespondentSumType respondentSumType) {
        assertThat(pdfFields.get(CHECKBOX_PDF_EARNINGS_BENEFITS_FIELD_HOURS_OF_WORK_CORRECT_YES))
                .contains(getCheckboxValue(respondentSumType.getEt3ResponseClaimantWeeklyHours(),
                        YES_CAPITALISED, YES_LOWERCASE));
        assertThat(pdfFields.get(CHECKBOX_PDF_EARNINGS_BENEFITS_FIELD_HOURS_OF_WORK_CORRECT_NO))
                .contains(getCheckboxValue(respondentSumType.getEt3ResponseClaimantWeeklyHours(),
                        NO_CAPITALISED, NO_LOWERCASE));
        assertThat(pdfFields.get(CHECKBOX_PDF_EARNINGS_BENEFITS_FIELD_HOURS_OF_WORK_CORRECT_NOT_APPLICABLE))
                .contains(getCheckBoxNotApplicableValue(respondentSumType.getEt3ResponseClaimantWeeklyHours(),
                        List.of(NO_CAPITALISED, YES_CAPITALISED), NO_LOWERCASE));
        assertThat(pdfFields.get(TXT_PDF_EARNINGS_BENEFITS_FIELD_WORK_HOURS_DETAILS))
                .contains(getCorrectedDetailValue(respondentSumType.getEt3ResponseClaimantWeeklyHours(),
                        NO_CAPITALISED, respondentSumType.getEt3ResponseClaimantCorrectHours(),
                        TEST_PDF_EARNINGS_BENEFITS_CORRECT_HOURS));
        assertThat(pdfFields.get(CHECKBOX_PDF_EARNINGS_BENEFITS_FIELD_EARNING_DETAILS_CORRECT_YES))
                .contains(getCheckboxValue(respondentSumType.getEt3ResponseEarningDetailsCorrect(),
                        YES_CAPITALISED, YES_LOWERCASE));
        assertThat(pdfFields.get(CHECKBOX_PDF_EARNINGS_BENEFITS_FIELD_EARNING_DETAILS_CORRECT_NO))
                .contains(getCheckboxValue(respondentSumType.getEt3ResponseEarningDetailsCorrect(),
                        NO_CAPITALISED, NO_LOWERCASE));
        assertThat(pdfFields.get(CHECKBOX_PDF_EARNINGS_BENEFITS_FIELD_EARNING_DETAILS_CORRECT_NOT_APPLICABLE))
                .contains(getCheckBoxNotApplicableValue(respondentSumType.getEt3ResponseEarningDetailsCorrect(),
                        List.of(NO_CAPITALISED, YES_CAPITALISED), NO_LOWERCASE));
        assertThat(pdfFields.get(TXT_PDF_EARNINGS_BENEFITS_FIELD_PAY_BEFORE_TAX))
                .contains(getCorrectedDetailValue(respondentSumType.getEt3ResponseEarningDetailsCorrect(),
                        NO_CAPITALISED, respondentSumType.getEt3ResponsePayBeforeTax(),
                        TEST_PDF_EARNINGS_BENEFITS_PAY_BEFORE_TAX));
        assertThat(pdfFields.get(CHECKBOX_PDF_EARNINGS_BENEFITS_FIELD_PAY_BEFORE_TAX_WEEKLY))
                .contains(getCorrectedCheckboxValue(respondentSumType.getEt3ResponseEarningDetailsCorrect(),
                        NO_LOWERCASE, respondentSumType.getEt3ResponsePayFrequency(), WEEKLY_CAPITALISED,
                        WEEKLY_LOWERCASE));
        assertThat(pdfFields.get(CHECKBOX_PDF_EARNINGS_BENEFITS_FIELD_PAY_BEFORE_TAX_MONTHLY))
                .contains(getCorrectedCheckboxValue(respondentSumType.getEt3ResponseEarningDetailsCorrect(),
                        NO_LOWERCASE, respondentSumType.getEt3ResponsePayFrequency(), MONTHLY_CAPITALISED,
                        MONTHLY_LOWERCASE));
        assertThat(pdfFields.get(CHECKBOX_PDF_EARNINGS_BENEFITS_FIELD_PAY_BEFORE_TAX_ANNUALLY))
                .contains(getCorrectedCheckboxValue(respondentSumType.getEt3ResponseEarningDetailsCorrect(),
                        NO_LOWERCASE, respondentSumType.getEt3ResponsePayFrequency(), ANNUALLY_CAPITALISED,
                        ANNUALLY_LOWERCASE));
        assertThat(pdfFields.get(TXT_PDF_EARNINGS_BENEFITS_FIELD_NORMAL_TAKE_HOME_PAY))
                .contains(getCorrectedDetailValue(respondentSumType.getEt3ResponseEarningDetailsCorrect(),
                        NO_CAPITALISED, respondentSumType.getEt3ResponsePayTakehome(),
                        TEST_PDF_EARNINGS_BENEFITS_PAY_TAKE_HOME));
        assertThat(pdfFields.get(CHECKBOX_PDF_EARNINGS_BENEFITS_FIELD_NORMAL_TAKE_HOME_PAY_WEEKLY))
                .contains(getCorrectedCheckboxValue(respondentSumType.getEt3ResponseEarningDetailsCorrect(),
                        NO_LOWERCASE, respondentSumType.getEt3ResponsePayFrequency(), WEEKLY_CAPITALISED,
                        WEEKLY_LOWERCASE));
        assertThat(pdfFields.get(CHECKBOX_PDF_EARNINGS_BENEFITS_FIELD_NORMAL_TAKE_HOME_PAY_MONTHLY))
                .contains(getCorrectedCheckboxValue(respondentSumType.getEt3ResponseEarningDetailsCorrect(),
                        NO_LOWERCASE, respondentSumType.getEt3ResponsePayFrequency(), MONTHLY_CAPITALISED,
                        MONTHLY_LOWERCASE));
        assertThat(pdfFields.get(CHECKBOX_PDF_EARNINGS_BENEFITS_FIELD_NORMAL_TAKE_HOME_PAY_ANNUALLY))
                .contains(getCorrectedCheckboxValue(respondentSumType.getEt3ResponseEarningDetailsCorrect(),
                        NO_LOWERCASE, respondentSumType.getEt3ResponsePayFrequency(), ANNUALLY_CAPITALISED,
                        ANNUALLY_LOWERCASE));
        assertThat(pdfFields.get(CHECKBOX_PDF_EARNINGS_BENEFITS_FIELD_NOTICE_PERIOD_CORRECT_YES))
                .contains(getCheckboxValue(respondentSumType.getEt3ResponseIsNoticeCorrect(),
                        YES_CAPITALISED, YES_LOWERCASE));
        assertThat(pdfFields.get(CHECKBOX_PDF_EARNINGS_BENEFITS_FIELD_NOTICE_PERIOD_CORRECT_NO))
                .contains(getCheckboxValue(respondentSumType.getEt3ResponseIsNoticeCorrect(),
                        NO_CAPITALISED, NO_LOWERCASE));
        assertThat(pdfFields.get(CHECKBOX_PDF_EARNINGS_BENEFITS_FIELD_NOTICE_PERIOD_CORRECT_NOT_APPLICABLE))
                .contains(getCheckBoxNotApplicableValue(respondentSumType.getEt3ResponseIsNoticeCorrect(),
                        List.of(NO_CAPITALISED, YES_CAPITALISED), NO_LOWERCASE));
        assertThat(pdfFields.get(TXT_PDF_EARNINGS_BENEFITS_FIELD_NOTICE_PERIOD_NOT_CORRECT_INFORMATION))
                .contains(getCorrectedDetailValue(respondentSumType.getEt3ResponseIsNoticeCorrect(),
                        NO_CAPITALISED, respondentSumType.getEt3ResponseCorrectNoticeDetails(),
                        TEST_PDF_EARNINGS_BENEFITS_CORRECT_NOTICE_PERIOD));
        assertThat(pdfFields.get(CHECKBOX_PDF_EARNINGS_BENEFITS_FIELD_PENSION_AND_OTHER_BENEFITS_CORRECT_YES))
                .contains(getCheckboxValue(respondentSumType.getEt3ResponseIsPensionCorrect(),
                        YES_CAPITALISED, YES_LOWERCASE));
        assertThat(pdfFields.get(CHECKBOX_PDF_EARNINGS_BENEFITS_FIELD_PENSION_AND_OTHER_BENEFITS_CORRECT_NO))
                .contains(getCheckboxValue(respondentSumType.getEt3ResponseIsPensionCorrect(),
                        NO_CAPITALISED, NO_LOWERCASE));
        assertThat(pdfFields.get(
                CHECKBOX_PDF_EARNINGS_BENEFITS_FIELD_PENSION_AND_OTHER_BENEFITS_CORRECT_NOT_APPLICABLE))
                .contains(getCheckBoxNotApplicableValue(respondentSumType.getEt3ResponseIsPensionCorrect(),
                        List.of(NO_CAPITALISED, YES_CAPITALISED), NO_LOWERCASE));
        assertThat(pdfFields.get(TXT_PDF_EARNINGS_BENEFITS_FIELD_PENSION_AND_OTHER_BENEFITS_NOT_CORRECT_INFORMATION))
                .contains(getCorrectedDetailValue(respondentSumType.getEt3ResponseIsPensionCorrect(),
                        NO_CAPITALISED, respondentSumType.getEt3ResponsePensionCorrectDetails(),
                        TEST_PDF_EARNINGS_BENEFITS_CORRECT_PENSION_AND_OTHER_BENEFITS));
    }

    private static void checkResponse(Map<String, Optional<String>> pdfFields, RespondentSumType respondentSumType) {
        assertThat(pdfFields.get(CHECKBOX_PDF_RESPONSE_FIELD_CONTEST_CLAIM_YES)).contains(
                getCheckboxValue(respondentSumType.getEt3ResponseRespondentContestClaim(),
                        YES_CAPITALISED, YES_LOWERCASE));
        assertThat(pdfFields.get(CHECKBOX_PDF_RESPONSE_FIELD_CONTEST_CLAIM_NO)).contains(
                getCheckboxValue(respondentSumType.getEt3ResponseRespondentContestClaim(),
                        NO_CAPITALISED, NO_LOWERCASE));
        assertThat(pdfFields.get(TXT_PDF_RESPONSE_FIELD_CONTEST_CLAIM_CORRECT_FACTS)).contains(
                getCorrectedDetailValue(respondentSumType.getEt3ResponseRespondentContestClaim(), NO_CAPITALISED,
                        respondentSumType.getEt3ResponseContestClaimDetails(),
                        TEST_PDF_RESPONSE_CONTEST_CLAIM_CORRECT_FACTS)
        );
    }

    private static void checkEmployerContractClaim(Map<String, Optional<String>> pdfFields,
                                                   RespondentSumType respondentSumType) {
        assertThat(pdfFields.get(CHECKBOX_PDF_EMPLOYER_CONTRACT_CLAIM_FIELD_YES)).contains(
                getCheckboxValue(respondentSumType.getEt3ResponseEmployerClaim(), YES_CAPITALISED, YES_LOWERCASE));
        assertThat(pdfFields.get(TXT_PDF_EMPLOYER_CONTRACT_CLAIM_FIELD_DETAILS)).contains(
                getCorrectedDetailValue(respondentSumType.getEt3ResponseEmployerClaim(), YES_CAPITALISED,
                        respondentSumType.getEt3ResponseEmployerClaimDetails(),
                        TEST_PDF_RESPONSE_EMPLOYER_CONTRACT_CLAIM_CORRECT_DETAILS));
    }

    private static void checkRepresentative(Map<String, Optional<String>> pdfFields, CaseData caseData) {
        assumeTrue(ObjectUtils.isNotEmpty(caseData));
        assumeTrue(CollectionUtils.isNotEmpty(caseData.getRepCollection()));
        assumeTrue(ObjectUtils.isNotEmpty(caseData.getRepCollection().get(0)));
        assumeTrue(ObjectUtils.isNotEmpty(caseData.getRepCollection().get(0).getValue()));
        assumeTrue(!TEST_DUMMY_VALUE.equals(caseData.getRepCollection().get(0).getValue().getRespRepName()));
        assertThat(pdfFields.get(TXT_PDF_REPRESENTATIVE_FIELD_NAME)).contains(TEST_PDF_REPRESENTATIVE_NAME);
        assertThat(pdfFields.get(TXT_PDF_REPRESENTATIVE_FIELD_ORGANISATION_NAME))
                .contains(TEST_PDF_REPRESENTATIVE_ORGANISATION_NAME);
        assertThat(pdfFields.get(TXT_PDF_REPRESENTATIVE_FIELD_ADDRESS)).contains(TEST_PDF_REPRESENTATIVE_ADDRESS);
        assertThat(pdfFields.get(TXT_PDF_REPRESENTATIVE_FIELD_POSTCODE)).contains(TEST_PDF_REPRESENTATIVE_POSTCODE);
        assertThat(pdfFields.get(TXT_PDF_REPRESENTATIVE_FIELD_PHONE_NUMBER)).contains(isNotBlank(
                caseData.getRepCollection().get(0).getValue().getRepresentativePhoneNumber())
                ? TEST_PDF_REPRESENTATIVE_PHONE_NUMBER : STRING_EMPTY);
        assertThat(pdfFields.get(TXT_PDF_REPRESENTATIVE_FIELD_MOBILE_PHONE_NUMBER)).contains(isNotBlank(
                caseData.getRepCollection().get(0).getValue().getRepresentativeMobileNumber())
                ? TEST_PDF_REPRESENTATIVE_MOBILE_PHONE_NUMBER : STRING_EMPTY);
        assertThat(pdfFields.get(TXT_PDF_REPRESENTATIVE_FIELD_REFERENCE_FOR_CORRESPONDENCE))
                .contains(isNotBlank(caseData.getRepCollection().get(0).getValue()
                        .getRepresentativeReference()) ? TEST_PDF_REPRESENTATIVE_REFERENCE_FOR_CORRESPONDENCE
                        : STRING_EMPTY);
        assertThat(pdfFields.get(CHECKBOX_PDF_REPRESENTATIVE_FIELD_COMMUNICATION_PREFERENCE_EMAIL)).contains(
                getCheckboxValue(caseData.getRepCollection().get(0).getValue().getRepresentativePreference(),
                        EMAIL_CAPITALISED, EMAIL_LOWERCASE));
        assertThat(pdfFields.get(CHECKBOX_PDF_REPRESENTATIVE_FIELD_COMMUNICATION_PREFERENCE_POST)).contains(
                getCheckboxValue(caseData.getRepCollection().get(0).getValue().getRepresentativePreference(),
                        POST_CAPITALISED, POST_LOWERCASE));
        assertThat(pdfFields.get(TXT_PDF_REPRESENTATIVE_FIELD_EMAIL_ADDRESS)).contains(isNotBlank(
                caseData.getRepCollection().get(0).getValue().getRepresentativeEmailAddress())
                ? TEST_PDF_REPRESENTATIVE_EMAIL_ADDRESS : STRING_EMPTY);
        assertThat(pdfFields.get(CHECKBOX_PDF_REPRESENTATIVE_FIELD_VIDEO_HEARINGS)).contains(YES_CAPITALISED);
        assertThat(pdfFields.get(CHECKBOX_PDF_REPRESENTATIVE_FIELD_PHONE_HEARINGS)).contains(YES_CAPITALISED);
    }

    private static void checkDisability(Map<String, Optional<String>> pdfFields, RespondentSumType respondentSumType) {
        assertThat(pdfFields.get(CHECKBOX_PDF_DISABILITY_YES)).contains(
                getCheckboxValue(respondentSumType.getEt3ResponseRespondentSupportNeeded(),
                        YES_CAPITALISED, YES_LOWERCASE));
        assertThat(pdfFields.get(CHECKBOX_PDF_DISABILITY_NO)).contains(
                getCheckboxValue(respondentSumType.getEt3ResponseRespondentSupportNeeded(),
                        NO_CAPITALISED, NO_LOWERCASE));
        assertThat(pdfFields.get(CHECKBOX_PDF_DISABILITY_NOT_SURE)).contains(
                getCheckBoxNotApplicableValue(respondentSumType.getEt3ResponseRespondentSupportNeeded(),
                        List.of(YES_CAPITALISED, NO_CAPITALISED), NO_LOWERCASE));
        assertThat(pdfFields.get(TXT_PDF_DISABILITY_DETAILS)).contains(
                getCorrectedDetailValue(respondentSumType.getEt3ResponseRespondentSupportNeeded(), YES_CAPITALISED,
                        respondentSumType.getEt3ResponseRespondentSupportDetails(),
                        TEST_PDF_DISABILITY_DETAIL));
    }

    private static boolean isOtherTitle(String selectedTitle) {
        return isNotBlank(selectedTitle)
                && !CHECKBOX_PDF_RESPONDENT_EXPECTED_VALUE_TITLE_MR.equals(selectedTitle)
                && !CHECKBOX_PDF_RESPONDENT_EXPECTED_VALUE_TITLE_MS.equals(selectedTitle)
                && !CHECKBOX_PDF_RESPONDENT_EXPECTED_VALUE_TITLE_MRS.equals(selectedTitle)
                && !CHECKBOX_PDF_RESPONDENT_EXPECTED_VALUE_TITLE_MISS.equals(selectedTitle);
    }

    private static Stream<CaseData> provideMapCaseTestData() {
        CaseData caseDataNullEt3Respondent = ResourceLoader.fromString(TEST_ET3_FORM_CASE_DATA_FILE, CaseData.class);
        caseDataNullEt3Respondent.setSubmitEt3Respondent(null);
        CaseData caseDataNullEt3RespondentValue = ResourceLoader.fromString(
                TEST_ET3_FORM_CASE_DATA_FILE, CaseData.class);
        caseDataNullEt3RespondentValue.getSubmitEt3Respondent().setValue(null);
        CaseData caseDataEmptyEt3RespondentLabel = ResourceLoader.fromString(
                TEST_ET3_FORM_CASE_DATA_FILE, CaseData.class);
        caseDataEmptyEt3RespondentLabel.getSubmitEt3Respondent().getValue().setLabel(STRING_EMPTY);
        CaseData caseDataEmptyEt3RespondentWrongLabel = ResourceLoader.fromString(
                TEST_ET3_FORM_CASE_DATA_FILE, CaseData.class);
        caseDataEmptyEt3RespondentWrongLabel.getSubmitEt3Respondent().getValue().setLabel(TEST_DUMMY_VALUE);
        CaseData caseDataEmpty = new CaseData();
        CaseData caseDataFull = ResourceLoader.fromString(TEST_ET3_FORM_CASE_DATA_FILE, CaseData.class);
        return Stream.of(null, caseDataEmpty, caseDataFull, caseDataNullEt3Respondent,
                caseDataNullEt3RespondentValue, caseDataEmptyEt3RespondentLabel, caseDataEmptyEt3RespondentWrongLabel);
    }

}

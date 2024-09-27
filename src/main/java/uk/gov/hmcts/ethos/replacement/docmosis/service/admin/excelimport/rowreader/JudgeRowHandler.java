package uk.gov.hmcts.ethos.replacement.docmosis.service.admin.excelimport.rowreader;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.Judge;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.JudgeEmploymentStatus;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.JudgeRepository;

import java.util.Map;

import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;

@Component
@StaffRowHandler
@RequiredArgsConstructor
public class JudgeRowHandler implements RowHandler {
    static final String JUDGE_ROW_ID = "fl_Judge";

    static final Map<String, JudgeEmploymentStatus> EMPLOYMENT_STATUS_IMPORT_CODES = Map.of(
            "FP", JudgeEmploymentStatus.FEE_PAID,
            "S", JudgeEmploymentStatus.SALARIED,
            "Unknown", JudgeEmploymentStatus.UNKNOWN
    );

    private final JudgeRepository judgeRepository;

    @Override
    public boolean accept(Row row) {
        Cell cell = row.getCell(0);
        return cell != null && JUDGE_ROW_ID.equals(cell.getStringCellValue());
    }

    @Override
    public void handle(TribunalOffice tribunalOffice, Row row) {
        Judge judge = rowToJudge(tribunalOffice, row);
        judgeRepository.save(judge);
    }

    private Judge rowToJudge(TribunalOffice tribunalOffice, Row row) {
        String code = row.getCell(1).getStringCellValue();
        String name = row.getCell(2).getStringCellValue();
        JudgeEmploymentStatus employmentStatus = convertImportStatusCode(ObjectUtils.isEmpty(row.getCell(4))
                        ? JudgeEmploymentStatus.UNKNOWN.name()
                        : defaultIfEmpty(row.getCell(4).getStringCellValue(), JudgeEmploymentStatus.UNKNOWN.name()));

        Judge judge =  new Judge();
        judge.setCode(code);
        judge.setName(name);
        judge.setEmploymentStatus(employmentStatus);
        judge.setTribunalOffice(tribunalOffice);
        return judge;
    }

    private JudgeEmploymentStatus convertImportStatusCode(String statusCode) {
        return EMPLOYMENT_STATUS_IMPORT_CODES.getOrDefault(
                defaultIfEmpty(statusCode, ""), JudgeEmploymentStatus.UNKNOWN);
    }
}

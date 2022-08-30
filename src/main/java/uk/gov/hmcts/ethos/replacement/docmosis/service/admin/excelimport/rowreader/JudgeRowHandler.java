package uk.gov.hmcts.ethos.replacement.docmosis.service.admin.excelimport.rowreader;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.et.common.model.helper.TribunalOffice;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.Judge;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.JudgeEmploymentStatus;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.JudgeRepository;

import java.util.Map;

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
        var cell = row.getCell(0);
        return cell != null && JUDGE_ROW_ID.equals(cell.getStringCellValue());
    }

    @Override
    public void handle(TribunalOffice tribunalOffice, Row row) {
        var judge = rowToJudge(tribunalOffice, row);
        judgeRepository.save(judge);
    }

    private Judge rowToJudge(TribunalOffice tribunalOffice, Row row) {
        var code = row.getCell(1).getStringCellValue();
        var name = row.getCell(2).getStringCellValue();
        var employmentStatus = convertImportStatusCode(row.getCell(4).getStringCellValue());

        var judge =  new Judge();
        judge.setCode(code);
        judge.setName(name);
        judge.setEmploymentStatus(employmentStatus);
        judge.setTribunalOffice(tribunalOffice);
        return judge;
    }

    private JudgeEmploymentStatus convertImportStatusCode(String statusCode) {
        return EMPLOYMENT_STATUS_IMPORT_CODES.getOrDefault(statusCode, JudgeEmploymentStatus.UNKNOWN);
    }
}

package uk.gov.hmcts.ethos.replacement.docmosis.service.admin.excelimport.rowreader;

import lombok.RequiredArgsConstructor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.et.common.model.helper.TribunalOffice;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Component
public class SimpleSheetHandler implements SheetHandler {
    final Map<String, TribunalOffice> sheetNameMap = Map.ofEntries(
            Map.entry("Bristol", TribunalOffice.BRISTOL),
            Map.entry("Leeds", TribunalOffice.LEEDS),
            Map.entry("LondonCentral", TribunalOffice.LONDON_CENTRAL),
            Map.entry("LondonEast", TribunalOffice.LONDON_EAST),
            Map.entry("LondonSouth", TribunalOffice.LONDON_SOUTH),
            Map.entry("Manchester", TribunalOffice.MANCHESTER),
            Map.entry("MidlandsEast", TribunalOffice.MIDLANDS_EAST),
            Map.entry("MidlandsWest", TribunalOffice.MIDLANDS_WEST),
            Map.entry("Newcastle", TribunalOffice.NEWCASTLE),
            Map.entry("Scotland", TribunalOffice.SCOTLAND),
            Map.entry("Wales", TribunalOffice.WALES),
            Map.entry("Watford", TribunalOffice.WATFORD));

    @Override
    public Iterator<OfficeSheet> sheets(XSSFWorkbook workbook) {
        var sheets = new ArrayList<OfficeSheet>();
        for (var i = 0; i < workbook.getNumberOfSheets(); i++) {
            var sheet = workbook.getSheetAt(i);
            if (sheetNameMap.containsKey(sheet.getSheetName())) {
                sheets.add(new OfficeSheet(sheetNameMap.get(sheet.getSheetName()), sheet));
            }
        }
        return new OfficeSheetIterator(sheets);
    }

    @RequiredArgsConstructor
    static class OfficeSheetIterator implements Iterator<OfficeSheet> {

        private final List<OfficeSheet> sheets;
        private int index = 0;

        @Override
        public boolean hasNext() {
            return index < sheets.size();
        }

        @Override
        public OfficeSheet next() {
            if (hasNext()) {
                return sheets.get(index++);
            } else {
                throw new NoSuchElementException();
            }
        }
    }
}

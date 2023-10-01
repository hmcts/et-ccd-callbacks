package uk.gov.hmcts.ethos.replacement.docmosis.utils;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

class DateUtilTest {

    @Test
    @SneakyThrows
    void getCurrentDateFormattedAsYearMonthDayHourMinute() {
        SimpleDateFormat simpleDateFormatter = new SimpleDateFormat("yyyyMMdd");
        String formattedDate = simpleDateFormatter.format(new Date());
        assertThat(DateUtil.getCurrentDateFormattedAsYearMonthDayHourMinute(), containsString(formattedDate));
    }

}

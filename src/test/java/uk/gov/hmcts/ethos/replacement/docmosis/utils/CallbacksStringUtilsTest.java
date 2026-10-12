package uk.gov.hmcts.ethos.replacement.docmosis.utils;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

final class CallbacksStringUtilsTest {

    private static final String TITLE = "Mr";
    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Smith";

    @Test
    void theBuildFullName() {
        // when title, firstname and lastname provided should return full name
        assertThat(CallbacksStringUtils.buildFullName(TITLE, FIRST_NAME, LAST_NAME))
                .isEqualTo(TITLE + StringUtils.SPACE + FIRST_NAME + StringUtils.SPACE + LAST_NAME);
        // when only firstname and lastname provided should return name without title
        assertThat(CallbacksStringUtils.buildFullName(StringUtils.EMPTY, FIRST_NAME, LAST_NAME))
                .isEqualTo(FIRST_NAME + StringUtils.SPACE + LAST_NAME);
        // when only title and firstname provided should return title and first name
        assertThat(CallbacksStringUtils.buildFullName(TITLE, FIRST_NAME, StringUtils.EMPTY))
                .isEqualTo(TITLE + StringUtils.SPACE + FIRST_NAME);
        // when only lastname provided should return only last name
        assertThat(CallbacksStringUtils.buildFullName(StringUtils.EMPTY, StringUtils.EMPTY, LAST_NAME))
                .isEqualTo(LAST_NAME);
        // when only firstname provided should return only first name
        assertThat(CallbacksStringUtils.buildFullName(StringUtils.EMPTY, FIRST_NAME, StringUtils.EMPTY))
                .isEqualTo(FIRST_NAME);
        // when none of them provided should return empty string
        assertThat(CallbacksStringUtils.buildFullName(StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY))
                .isEmpty();

    }
}

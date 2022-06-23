package uk.gov.hmcts.ethos.replacement.docmosis.utils;

public class IntWrapper {

    private int value;

    public IntWrapper(int initialValue) {
        value = initialValue;
    }

    public int getValue() {
        return value;
    }

    public int incrementAndReturnValue() {
        value++;
        return value;
    }

}

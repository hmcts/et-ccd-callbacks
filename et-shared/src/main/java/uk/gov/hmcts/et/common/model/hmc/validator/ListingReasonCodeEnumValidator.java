package uk.gov.hmcts.et.common.model.hmc.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ListingReasonCodeEnumValidator implements ConstraintValidator<ListingReasonCodeEnum, String> {
    private List<String> acceptedValues;

    @Override
    public void initialize(final ListingReasonCodeEnum annotation) {
        acceptedValues = Stream.of(annotation.enumClass().getEnumConstants())
                .map(Enum::toString)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value == null || acceptedValues.contains(value);
    }

}

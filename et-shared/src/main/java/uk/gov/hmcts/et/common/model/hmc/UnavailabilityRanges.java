package uk.gov.hmcts.et.common.model.hmc;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.et.common.model.hmc.validator.EnumPattern;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class UnavailabilityRanges {

    @NotNull(message = ValidationError.UNAVAILABLE_FROM_DATE_EMPTY)
    private LocalDate unavailableFromDate;

    @NotNull(message = ValidationError.UNAVAILABLE_TO_DATE_EMPTY)
    private LocalDate unavailableToDate;

    @EnumPattern(enumClass = DayOfWeekUnavailableType.class, fieldName = "unavailabilityType")
    private String unavailabilityType;
}

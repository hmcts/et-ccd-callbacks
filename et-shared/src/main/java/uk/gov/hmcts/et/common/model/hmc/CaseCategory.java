package uk.gov.hmcts.et.common.model.hmc;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.et.common.model.hmc.validator.EnumPattern;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CaseCategory {
    @NotEmpty(message = ValidationError.CATEGORY_TYPE_EMPTY)
    @EnumPattern(enumClass = CaseCategoryType.class, fieldName = "categoryType")
    private String categoryType;

    @NotEmpty(message = ValidationError.CATEGORY_VALUE_EMPTY)
    @Size(max = 70, message = ValidationError.CATEGORY_VALUE)
    private String categoryValue;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Size(max = 70, message = ValidationError.CATEGORY_VALUE)
    private String categoryParent;
}

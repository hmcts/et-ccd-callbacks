package uk.gov.hmcts.ethos.replacement.docmosis.domain.ccd;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import uk.gov.hmcts.et.common.model.ccd.types.DigitalCaseFileType;

import java.util.UUID;

@Entity
@Table(name = "digital_case_file")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DigitalCaseFile {

    @Id
    @Column(name = "case_reference")
    private Long caseReference;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private DigitalCaseFileType data;

    @Column(name = "active_bundle_id")
    private UUID activeBundleId;

    @Column(name = "completed_bundle_id")
    private UUID completedBundleId;

    public static DigitalCaseFile create(long caseReference,
                                         DigitalCaseFileType data,
                                         UUID activeBundleId,
                                         UUID completedBundleId) {
        return new DigitalCaseFile(caseReference, data, activeBundleId, completedBundleId);
    }
}

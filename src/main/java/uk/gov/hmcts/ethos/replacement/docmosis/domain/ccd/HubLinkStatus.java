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
import uk.gov.hmcts.et.common.model.ccd.types.citizenhub.HubLinksStatuses;

@Entity
@Table(name = "hub_link_status")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class HubLinkStatus {

    @Id
    @Column(name = "case_reference")
    private Long caseReference;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private HubLinksStatuses data;

    public static HubLinkStatus create(long caseReference,
                                       HubLinksStatuses data) {
        return new HubLinkStatus(caseReference, data);
    }
}

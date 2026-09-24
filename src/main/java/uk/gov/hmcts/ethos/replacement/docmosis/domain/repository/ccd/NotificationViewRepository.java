package uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.ccd;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.ccd.NotificationView;

import java.util.Set;

public interface NotificationViewRepository extends JpaRepository<NotificationView, NotificationView.Key> {

    @Query("SELECT v.id.itemId FROM NotificationView v WHERE v.id.caseReference = :caseReference")
    Set<String> findItemIds(@Param("caseReference") long caseReference);

    // Concurrent viewers may mark the same item, so an existing row is not an error.
    @Modifying
    @Query(value = "INSERT INTO public.notification_view (case_reference, item_id) "
        + "VALUES (:caseReference, :itemId) ON CONFLICT DO NOTHING", nativeQuery = true)
    void markViewed(@Param("caseReference") long caseReference, @Param("itemId") String itemId);
}

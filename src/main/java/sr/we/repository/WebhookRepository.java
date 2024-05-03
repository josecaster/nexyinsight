package sr.we.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sr.we.entity.Webhook;

public interface WebhookRepository extends JpaRepository<Webhook, Long>, JpaSpecificationExecutor<Webhook> {

    @Query("from Webhook where typee = :typee and businessId = :businessId")
    Webhook getByTypeAndBusinessId(@Param("typee") Webhook.Type typee, @Param("businessId") Long businessId);

}

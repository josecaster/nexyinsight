package sr.we.schedule;

import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;
import sr.we.entity.Integration;
import sr.we.entity.Task;
import sr.we.repository.IntegrationRepository;
import sr.we.services.IntegrationService;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;

public class RefreshTokenTaskTrigger implements Trigger {

    private final Long businessId;
    private final IntegrationRepository integrationRepository;

    public RefreshTokenTaskTrigger(Long businessId, IntegrationRepository integrationRepository) {
        this.businessId = businessId;
        this.integrationRepository = integrationRepository;
    }

    public Long getBusinessId() {
        return businessId;
    }

    @Override
    public Instant nextExecution(TriggerContext triggerContext) {
        Integration byTypeAndBusinessId = integrationRepository.getByBusinessId(businessId);
        if (byTypeAndBusinessId != null && byTypeAndBusinessId.getExpireDate() != null) {
            Instant now = Instant.now();
            if(byTypeAndBusinessId.getExpireDate().isEqual(LocalDateTime.now()) || byTypeAndBusinessId.getExpireDate().isBefore(LocalDateTime.now())){
                return now;
            } else {
                Duration between = Duration.between(LocalDateTime.now(), byTypeAndBusinessId.getExpireDate());
                long days = between.toDays();
                return LocalDateTime.now().plusDays(days/2).atZone(ZoneOffset.UTC).toInstant();
            }
        }
        return null;// this will stop the thread
    }
}

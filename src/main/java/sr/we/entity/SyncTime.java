package sr.we.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.time.LocalDateTime;

@Entity
public class SyncTime extends AbstractEntity {

    private LocalDateTime maxTime;
    @Enumerated(EnumType.STRING)
    private SyncType type;

    private Long businessId;

    public Long getBusinessId() {
        return businessId;
    }

    public void setBusinessId(Long businessId) {
        this.businessId = businessId;
    }

    public LocalDateTime getMaxTime() {
        return maxTime;
    }

    public void setMaxTime(LocalDateTime maxTime) {
        this.maxTime = maxTime;
    }

    public SyncType getType() {
        return type;
    }

    public void setType(SyncType type) {
        this.type = type;
    }

    public enum SyncType {
        ITEMS,RECEIPTS
    }
}

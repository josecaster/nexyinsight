package sr.we.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.time.LocalDateTime;

@Entity
public class Task extends AbstractEntity {

    private LocalDateTime maxTime;
    @Enumerated(EnumType.STRING)
    private Type type;
    private Boolean enabled;

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

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }



    public enum Type {
        MAIN
    }

    @Override
    public String toString() {
        return "Task{" +
                "maxTime=" + maxTime +
                ", type=" + type +
                ", businessId=" + businessId +
                '}';
    }
}

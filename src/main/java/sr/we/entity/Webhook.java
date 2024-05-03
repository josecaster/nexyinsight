package sr.we.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

@Entity
public class Webhook {
    private Long businessId;

    @Id
    private String id;
    private String merchant_id, url;
    @Enumerated(EnumType.STRING)
    private Type typee;
    @Enumerated(EnumType.STRING)
    private Status status;
    @Transient
    private String type;
    private LocalDateTime created_at, updated_at;

    public Long getBusinessId() {
        return businessId;
    }

    public void setBusinessId(Long businessId) {
        this.businessId = businessId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMerchant_id() {
        return merchant_id;
    }

    public void setMerchant_id(String merchant_id) {
        this.merchant_id = merchant_id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Type getTypee() {
        return typee;
    }

    public void setTypee(Type typee) {
        this.typee = typee;
        if(this.typee != null){
            this.type = this.typee.getKey();
        } else {
            this.type = null;
        }
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
        Optional<Type> any = Arrays.stream(Type.values()).filter(f -> f.getKey().equalsIgnoreCase(type)).findAny();
        any.ifPresent(value -> typee = value);
    }

    public LocalDateTime getCreated_at() {
        return created_at;
    }

    public void setCreated_at(LocalDateTime created_at) {
        this.created_at = created_at;
    }

    public LocalDateTime getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(LocalDateTime updated_at) {
        this.updated_at = updated_at;
    }

    public enum Type {
        ILU("inventory_levels.update"), IU("items.update"), CU("customers.update"), RU("receipts.update"), SU("shifts.create");
        private final String key;

        Type(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }

        public String getUrl(String url) {
            return switch (this){

                case ILU -> url+"/inventory";
                case IU -> url+"/items";
                case CU -> url+"/customers";
                case RU -> url+"/receipts";
                case SU -> url+"/shifts";
            };
        }
    }

    public enum Status {
        ENABLED, DISABLED
    }


}

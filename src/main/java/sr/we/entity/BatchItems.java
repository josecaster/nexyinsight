package sr.we.entity;

import jakarta.persistence.Entity;
import sr.we.entity.AbstractEntity;

import java.math.BigDecimal;

@Entity
public class BatchItems extends AbstractEntity {

    private String sku,code,name, description;
    private BigDecimal price,cost;
    private boolean optional;
    private Integer quantity, realQuantity;
    private Long batchId;
    private String itemId;
    private Boolean upload;

    public BatchItems() {
    }

    public BatchItems(Long batchId, String sku, String code, String name, Integer quantity, BigDecimal price, BigDecimal cost, boolean optional, String description) {
        this.batchId = batchId;
        this.sku = sku;
        this.code = code;
        this.name = name;
        this.quantity = quantity;
        this.price = price;
        this.cost = cost;
        this.optional = optional;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isOptional() {
        return optional;
    }

    public void setOptional(boolean optional) {
        this.optional = optional;
    }

    public Integer getRealQuantity() {
        return realQuantity;
    }

    public void setRealQuantity(Integer realQuantity) {
        this.realQuantity = realQuantity;
    }

    public Long getBatchId() {
        return batchId;
    }

    public void setBatchId(Long batchId) {
        this.batchId = batchId;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public void setCost(BigDecimal cost) {
        this.cost = cost;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public Boolean getUpload() {
        return upload;
    }

    public void setUpload(Boolean upload) {
        this.upload = upload;
    }

    public boolean isUpload() {
        return upload == null || upload;
    }
}

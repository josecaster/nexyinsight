package sr.we.entity;

import jakarta.persistence.Entity;

import java.math.BigDecimal;

@Entity
public class BatchItems extends AbstractEntity {

    private String sku,code,name, description, variantId;
    private String optionName1, optionValue1,optionName2, optionValue2,optionName3, optionValue3;
    private BigDecimal price,cost;
    private boolean optional;
    private Integer quantity, realQuantity;
    private Long batchId;
    private String itemId;
    private Boolean upload;

    public BatchItems() {
    }

    public BatchItems(Long batchId, String sku, String code, String name, Integer quantity, BigDecimal price, BigDecimal cost, boolean optional, String description, String optionName1, String optionValue1, String optionName2, String optionValue2, String optionName3, String optionValue3) {
        this.batchId = batchId;
        this.sku = sku;
        this.code = code;
        this.name = name;
        this.quantity = quantity;
        this.price = price;
        this.cost = cost;
        this.optional = optional;
        this.description = description;
        this.optionName1 = optionName1;
        this.optionValue1 = optionValue1;
        this.optionName2 = optionName2;
        this.optionValue2 = optionValue2;
        this.optionName3 = optionName3;
        this.optionValue3 = optionValue3;
    }

    public String getOptionName2() {
        return optionName2;
    }

    public void setOptionName2(String optionName2) {
        this.optionName2 = optionName2;
    }

    public String getOptionValue2() {
        return optionValue2;
    }

    public void setOptionValue2(String optionValue2) {
        this.optionValue2 = optionValue2;
    }

    public String getOptionName3() {
        return optionName3;
    }

    public void setOptionName3(String optionName3) {
        this.optionName3 = optionName3;
    }

    public String getOptionValue3() {
        return optionValue3;
    }

    public void setOptionValue3(String optionValue3) {
        this.optionValue3 = optionValue3;
    }

    public String getOptionName1() {
        return optionName1;
    }

    public void setOptionName1(String optionName1) {
        this.optionName1 = optionName1;
    }

    public String getOptionValue1() {
        return optionValue1;
    }

    public void setOptionValue1(String optionValue1) {
        this.optionValue1 = optionValue1;
    }

    public String getVariantId() {
        return variantId;
    }

    public void setVariantId(String variantId) {
        this.variantId = variantId;
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

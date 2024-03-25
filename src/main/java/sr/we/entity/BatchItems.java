package sr.we.entity;

import jakarta.persistence.Entity;
import sr.we.entity.AbstractEntity;

@Entity
public class BatchItems extends AbstractEntity {

    private String sku,code,name,quantity,price,cost;
    private Long batchId;

    public BatchItems() {
    }

    public BatchItems(Long batchId, String sku, String code, String name, String quantity, String price, String cost) {
        this.batchId = batchId;
        this.sku = sku;
        this.code = code;
        this.name = name;
        this.quantity = quantity;
        this.price = price;
        this.cost = cost;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getCost() {
        return cost;
    }

    public void setCost(String cost) {
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

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }
}

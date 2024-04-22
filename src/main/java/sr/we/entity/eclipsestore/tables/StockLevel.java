package sr.we.entity.eclipsestore.tables;

import java.time.LocalDateTime;

public class StockLevel {
    private Long businessId;
    private String variant_id;
    private String store_id;
    private int in_stock, stock_after;
    private LocalDateTime updated_at;

    public String getVariant_id() {
        return variant_id;
    }

    public void setVariant_id(String variant_id) {
        this.variant_id = variant_id;
    }

    public String getStore_id() {
        return store_id;
    }

    public void setStore_id(String store_id) {
        this.store_id = store_id;
    }

    public int getIn_stock() {
        return in_stock;
    }

    public void setIn_stock(int in_stock) {
        this.in_stock = in_stock;
    }

    public LocalDateTime getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(LocalDateTime updated_at) {
        this.updated_at = updated_at;
    }

    public Long getBusinessId() {
        return businessId;
    }

    public void setBusinessId(Long businessId) {
        this.businessId = businessId;
    }

    public int getStock_after() {
        return stock_after;
    }

    public void setStock_after(int stock_after) {
        this.stock_after = stock_after;
    }
}

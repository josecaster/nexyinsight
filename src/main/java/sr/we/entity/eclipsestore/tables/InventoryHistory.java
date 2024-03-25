package sr.we.entity.eclipsestore.tables;

import java.time.LocalDateTime;

public class InventoryHistory extends SuperDao {
    public enum Type {
        SALE,REFUND,STOCK_ADJUSTMENT,PURCHASE_ORDER,TRANSFER,INVENTORY_COUNT,DAMAGE,LOSS,OTHER,PRODUCTION,ITEM_EDIT,SYNC
    }

    private Long businessId;
    private String section_id, item_id;
    private LocalDateTime localDateTime;
    private Type type;
    private int adjustment;
    private int stock_after;

    public String getItem_id() {
        return item_id;
    }

    public void setItem_id(String item_id) {
        this.item_id = item_id;
    }

    public Long getBusinessId() {
        return businessId;
    }

    public void setBusinessId(Long businessId) {
        this.businessId = businessId;
    }

    public String getSection_id() {
        return section_id;
    }

    public void setSection_id(String section_id) {
        this.section_id = section_id;
    }

    public LocalDateTime getLocalDateTime() {
        return localDateTime;
    }

    public void setLocalDateTime(LocalDateTime localDateTime) {
        this.localDateTime = localDateTime;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public int getAdjustment() {
        return adjustment;
    }

    public void setAdjustment(int adjustment) {
        this.adjustment = adjustment;
    }

    public int getStock_after() {
        return stock_after;
    }

    public void setStock_after(int stock_after) {
        this.stock_after = stock_after;
    }
}

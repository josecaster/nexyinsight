package sr.we.entity;

import jakarta.persistence.Entity;

import java.math.BigDecimal;

@Entity
public class StockAdjustmentItems extends AbstractEntity {

    private String itemId, itemName, sku,barcode;
    private BigDecimal cost;
    private Integer inStock,adjustment,stockAfter;
    private Long stockAdjustmentId;

    public StockAdjustmentItems() {
    }

    public StockAdjustmentItems(String itemId, BigDecimal cost, Integer inStock, Integer adjustment, Integer stockAfter, Long stockAdjustmentId) {
        this.itemId = itemId;
        this.cost = cost;
        this.inStock = inStock;
        this.adjustment = adjustment;
        this.stockAfter = stockAfter;
        this.stockAdjustmentId = stockAdjustmentId;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public void setCost(BigDecimal cost) {
        this.cost = cost;
    }

    public Integer getInStock() {
        return inStock;
    }

    public void setInStock(Integer inStock) {
        this.inStock = inStock;
    }

    public Integer getAdjustment() {
        return adjustment;
    }

    public void setAdjustment(Integer adjustment) {
        this.adjustment = adjustment;
    }

    public Integer getStockAfter() {
        return stockAfter;
    }

    public void setStockAfter(Integer stockAfter) {
        this.stockAfter = stockAfter;
    }

    public Long getStockAdjustmentId() {
        return stockAdjustmentId;
    }

    public void setStockAdjustmentId(Long stockAdjustmentId) {
        this.stockAdjustmentId = stockAdjustmentId;
    }
}

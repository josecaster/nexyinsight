package sr.we.entity.eclipsestore.tables;

import java.util.List;

public class InventoryLevels {
    private List<StockLevel> inventory_levels;
    private String cursor;

    public List<StockLevel> getInventory_levels() {
        return inventory_levels;
    }

    public void setInventory_levels(List<StockLevel> inventory_levels) {
        this.inventory_levels = inventory_levels;
    }

    public String getCursor() {
        return cursor;
    }

    public void setCursor(String cursor) {
        this.cursor = cursor;
    }
}

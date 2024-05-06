package sr.we.entity.eclipsestore.tables;


import java.util.List;

public class ApiInventoryLevels extends ApiCommunication {

    private List<InventoryLevels> inventory_levels;

    public List<InventoryLevels> getInventory_levels() {
        return inventory_levels;
    }

    public void setInventory_levels(List<InventoryLevels> inventory_levels) {
        this.inventory_levels = inventory_levels;
    }
}

package sr.we.entity.eclipsestore;

import sr.we.entity.eclipsestore.tables.*;

import java.io.Serializable;
import java.lang.reflect.Field;

/**
 * Eclipse store root object
 */
public class Database implements Serializable {
    /**
     * Trying to simply mimic database tables, every field here would be a Lazy List
     */
    @Table(Section.class)
    private final Grid<Section> stores = new Grid<>();
    @Table(Item.class)
    private final Grid<Item> items = new Grid<>();
    @Table(Device.class)
    private final Grid<Device> devices = new Grid<>();
    @Table(Category.class)
    private final Grid<Category> categories = new Grid<>();
    @Table(Receipt.class)
    private final Grid<Receipt> receipts = new Grid<>();
    @Table(InventoryHistory.class)
    private final Grid<InventoryHistory> inventoryHistory = new Grid<>();

    @Table(InventoryValuation.class)
    private final Grid<InventoryValuation> inventoryValuation = new Grid<>();

    /**
     * This gets the list of data within the lazy field
     *
     * @param c
     * @param <T>
     * @return
     */
    public <T extends SuperDao> Grid<T> getListByClass(Class<T> c) {
        return getLazyByClass(c);
    }

    /**
     * This gets the lazy field by the provided class
     *
     * @param c
     * @param <T>
     * @return
     */
    public <T extends SuperDao> Grid<T> getLazyByClass(Class<T> c) {
        if (c != null) {
            for (Field field : Database.class.getDeclaredFields()) {
                if (field.isAnnotationPresent(Table.class)) {
                    Table annotation = field.getAnnotation(Table.class);
                    if (annotation.value().equals(c)) {
                        try {
                            return (Grid<T>) field.get(this);
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }
        return null;
    }
}

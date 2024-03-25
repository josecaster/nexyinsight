package sr.we.entity.eclipsestore.tables;


import java.util.List;

public class ListLoyItems {
    private List<Item> items;
    private String cursor;

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public String getCursor() {
        return cursor;
    }

    public void setCursor(String cursor) {
        this.cursor = cursor;
    }
}

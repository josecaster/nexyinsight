package sr.we.entity.eclipsestore.tables;


import java.util.List;

public class ApiItems extends ApiCommunication {

    private List<Item> items;

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }
}

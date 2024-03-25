package sr.we.entity.eclipsestore.tables;

import java.util.List;

public class CollectCategories {
    private List<Category> categories;
    private String cursor;

    public List<Category> getCategories() {
        return categories;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

    public String getCursor() {
        return cursor;
    }

    public void setCursor(String cursor) {
        this.cursor = cursor;
    }
}

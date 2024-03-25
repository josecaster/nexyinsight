package sr.we.controllers;

import org.eclipse.store.storage.embedded.types.EmbeddedStorageManager;
import org.springframework.stereotype.Controller;
import sr.we.entity.eclipsestore.tables.Category;
import sr.we.integration.Parent;
import sr.we.storage.impl.CategoryStorage;

import java.util.List;

@Controller
public class CategoryRestController extends Parent {
    EmbeddedStorageManager storageManager;
    CategoryStorage categoryStorage;

    public CategoryRestController(EmbeddedStorageManager storageManager, CategoryStorage categoryStorage) {
        this.storageManager = storageManager;
        this.categoryStorage = categoryStorage;
    }

    public List<Category> allStores(Long businessId) {
        return categoryStorage.allStores(businessId);
    }


}

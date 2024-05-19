package sr.we.controllers;

import org.eclipse.store.storage.embedded.types.EmbeddedStorageManager;
import org.springframework.stereotype.Controller;
import sr.we.entity.eclipsestore.tables.Category;
import sr.we.storage.ICategoryStorage;

import java.util.List;


@Controller
public class CategoryController {
    EmbeddedStorageManager storageManager;
    ICategoryStorage categoryStorage;

    /**
     *
     * @param storageManager {@link EmbeddedStorageManager}
     * @param categoryStorage {@link ICategoryStorage}
     */
    public CategoryController(EmbeddedStorageManager storageManager, ICategoryStorage categoryStorage) {
        this.storageManager = storageManager;
        this.categoryStorage = categoryStorage;
    }

    /**
     * Get all categories
     * @param businessId Fill in business ID
     * @return Returns a list of categories
     */
    public List<Category> findCategories(Long businessId) {
        return categoryStorage.allStores(businessId);
    }


}

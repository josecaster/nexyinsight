package sr.we.storage.impl;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.store.integrations.spring.boot.types.concurrent.Read;
import org.eclipse.store.integrations.spring.boot.types.concurrent.Write;
import org.eclipse.store.storage.embedded.types.EmbeddedStorageManager;
import org.springframework.stereotype.Component;
import sr.we.entity.eclipsestore.tables.Category;
import sr.we.storage.ICategoryStorage;

import java.util.List;

@Component
public class CategoryStorage extends EclipseStoreSuperService<Category> implements ICategoryStorage {

    public CategoryStorage(EmbeddedStorageManager storageManager) {
        super(storageManager, Category.class);
    }

    @Override
    @Read
    public Category oneStore(String uuId) {
        return get(uuId);
    }

    @Override
    @Read
    public List<Category> allStores(Long businessId) {
        return stream().filter(store -> store.getBusinessId() != null && store.getBusinessId().compareTo(businessId) == 0).toList();
    }

    @Override
    @Write
    public Category saveOrUpdate(Category Category) {

        // these few lines will ensure that there is always a default store available
        Category defaultCategory = oneStore(Category.getId());
        String uuId = null;
        if (defaultCategory == null) {
            if (StringUtils.isNotBlank(Category.getUuId())) {
                delete(Category.getUuId());
            }
            uuId = Category.getId();
        }
        return update(Category, f -> {
            f.setBusinessId(Category.getBusinessId());
            f.setId(Category.getId());
            f.setName(Category.getName());
            f.setColor(Category.getColor());
            f.setCreated_at(Category.getCreated_at());
            f.setDeleted_at(Category.getDeleted_at());
            return f;
        }, uuId);
    }

    @Override
    @Write
    public boolean deleteStore(String uuId) {
        return delete(uuId);
    }
}

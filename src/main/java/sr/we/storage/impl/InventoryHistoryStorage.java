package sr.we.storage.impl;

import org.eclipse.store.integrations.spring.boot.types.concurrent.Read;
import org.eclipse.store.integrations.spring.boot.types.concurrent.Write;
import org.eclipse.store.storage.embedded.types.EmbeddedStorageManager;
import org.springframework.stereotype.Component;
import sr.we.entity.eclipsestore.tables.InventoryHistory;
import sr.we.storage.IInventoryHistoryStorage;

import java.util.List;

@Component
public class InventoryHistoryStorage extends EclipseStoreSuperService<InventoryHistory> implements IInventoryHistoryStorage {

    public InventoryHistoryStorage(EmbeddedStorageManager storageManager) {
        super(storageManager, InventoryHistory.class);
    }

    @Override
    @Read
    public InventoryHistory oneInventoryHistory(String uuId) {
        return get(uuId);
    }

    @Override
    @Read
    public List<InventoryHistory> allInventoryHistorys(Long businessId) {
        return stream().filter(store -> store.getBusinessId() != null && store.getBusinessId().compareTo(businessId) == 0).toList();
    }

    @Override
    @Write
    public InventoryHistory saveOrUpdate(InventoryHistory inventoryHistory) {

        return update(inventoryHistory, f -> {
            f.setLocalDateTime(f.getLocalDateTime());
            f.setBusinessId(inventoryHistory.getBusinessId());
            f.setSection_id(inventoryHistory.getSection_id());
            f.setAdjustment(inventoryHistory.getAdjustment());
            f.setStock_after(inventoryHistory.getStock_after());
            f.setType(inventoryHistory.getType());
            f.setItem_id(inventoryHistory.getItem_id());

            return f;
        });
    }

    @Override
    @Write
    public boolean deleteInventoryHistory(String uuId) {
        return delete(uuId);
    }
}

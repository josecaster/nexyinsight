package sr.we.storage.impl;

import io.micrometer.common.util.StringUtils;
import org.eclipse.store.integrations.spring.boot.types.concurrent.Read;
import org.eclipse.store.integrations.spring.boot.types.concurrent.Write;
import org.eclipse.store.storage.embedded.types.EmbeddedStorageManager;
import org.springframework.stereotype.Component;
import sr.we.entity.eclipsestore.tables.Item;
import sr.we.storage.IItemStorage;

import java.util.List;

@Deprecated
@Component
public class ItemStorage extends EclipseStoreSuperService<Item>/* implements IItemStorage*/ {

    public ItemStorage(EmbeddedStorageManager storageManager) {
        super(storageManager, Item.class);
    }

//    @Override
    @Read
    public Item oneItem(String uuId) {
        return get(uuId);
    }

//    @Override
    @Read
    public List<Item> allItems(Long businessId) {
        return stream().filter(Item -> Item.getBusinessId() != null && Item.getBusinessId().compareTo(businessId) == 0).toList();
    }

//    @Override
    @Write
    public Item saveOrUpdate(Item item) {
        return update(item, f-> {
            f.setBusinessId(item.getBusinessId());
            f.setItem_name(item.getItem_name());
            f.setCategory_id(item.getCategory_id());
            f.setColor(item.getColor());
            f.setId(item.getId());// this is the loyverse Id
            f.setComponents(item.getComponents());
            f.setForm(item.getForm());
            f.setHandle(item.getHandle());
            f.setImage_url(item.getImage_url());
            f.setIs_composite(item.isIs_composite());
            f.setIs_composite_string(item.getIs_composite_string());
            f.setVariants(item.getVariants());
            f.setUse_production(item.isUse_production());
            f.setTrack_stock(item.isTrack_stock());
            f.setTax_ids(item.getTax_ids());
            f.setSold_by_weight(item.isSold_by_weight());
            f.setSeaqnsUuId(item.getSeaqnsUuId());
            f.setReference_id(item.getReference_id());
            f.setPrimary_supplier_id(item.getPrimary_supplier_id());
            f.setOption3_name(item.getOption3_name());
            f.setOption2_name(item.getOption2_name());
            f.setOption1_name(item.getOption1_name());
            f.setModifiers_ids(item.getModifiers_ids());
            f.setStoreCountMap(item.getStoreCountMap());
            f.setLastUpdateStockLevel(item.getLastUpdateStockLevel());
            return f;
        });
    }

//    @Override
    @Write
    public boolean deleteItem(String uuId) {
        return delete(uuId);
    }

//    @Override
    public Item oneItemByLoyId(String id) {
        return stream().filter(l -> StringUtils.isNotBlank(l.getId()) && l.getId().equalsIgnoreCase(id)).findAny().orElse(null);
    }
}

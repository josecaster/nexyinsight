package sr.we.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import sr.we.controllers.ItemsController;
import sr.we.entity.StockAdjustment;
import sr.we.entity.StockAdjustmentItems;
import sr.we.entity.eclipsestore.tables.InventoryLevels;
import sr.we.entity.eclipsestore.tables.Item;
import sr.we.entity.eclipsestore.tables.StockLevel;
import sr.we.integration.LoyInventoryController;
import sr.we.repository.StockAdjustmentItemsRepository;
import sr.we.repository.StockAdjustmentRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class StockAdjustmentService {

    private final StockAdjustmentRepository repository;
    private final StockAdjustmentItemsRepository itemsRepository;
    private final ItemsController itemsController;
    private final LoyInventoryController inventoryController;

    @Value("${sr.we.loyverse.token}")
    private String loyverseToken;


    @Value("${sr.we.business.id}")
    private Long businessId;

    public StockAdjustmentService(StockAdjustmentRepository repository, StockAdjustmentItemsRepository itemsRepository, ItemsController itemsController, LoyInventoryController inventoryController) {
        this.repository = repository;
        this.itemsRepository = itemsRepository;
        this.itemsController = itemsController;
        this.inventoryController = inventoryController;
    }

    public Optional<StockAdjustment> get(Long id) {
        return repository.findById(id);
    }

    public StockAdjustment update(StockAdjustment entity) throws IOException {
        StockAdjustment save = repository.save(entity);
        List<StockAdjustmentItems> items = entity.getItems();
        if (items != null) {


            InventoryLevels inventoryLevels = new InventoryLevels();
            List<StockLevel> list = new ArrayList<>();


            for (StockAdjustmentItems stockAdjustmentItems : items) {
                stockAdjustmentItems.setStockAdjustmentId(save.getId());
                Item item = itemsController.oneItem(stockAdjustmentItems.getItemId());
                if (item != null && item.getVariantStore() != null && item.getVariant() != null && stockAdjustmentItems.getStockAfter() != null) {
                    StockAdjustmentItems save1 = itemsRepository.save(stockAdjustmentItems);
                    list.add(new StockLevel(item.getVariant().getVariant_id(), item.getVariantStore().getStore_id(), save1.getStockAfter()));
                }
            }

            if(!list.isEmpty()){
                inventoryLevels.setInventory_levels(list);
                inventoryController.add(loyverseToken, inventoryLevels);
            }
        }
        return save;
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public Page<StockAdjustment> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<StockAdjustment> list(Pageable pageable, Specification<StockAdjustment> filter) {
        return repository.findAll(filter, pageable);
    }

    public int count() {
        return (int) repository.count();
    }

}

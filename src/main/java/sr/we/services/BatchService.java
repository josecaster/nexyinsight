package sr.we.services;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import sr.we.controllers.ItemsController;
import sr.we.controllers.StoresController;
import sr.we.entity.Batch;
import sr.we.entity.BatchItems;
import sr.we.entity.eclipsestore.tables.*;
import sr.we.integration.LoyInventoryController;
import sr.we.integration.LoyItemsController;
import sr.we.repository.BatchItemsRepository;
import sr.we.repository.BatchRepository;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class BatchService {

    private final BatchRepository repository;
    private final BatchItemsRepository batchItemsRepository;
    private final LoyItemsController loyItemsController;
    private final StoresController storesController;
    private final LoyInventoryController inventoryController;
    private final ItemsController ItemService;

    @Value("${sr.we.loyverse.token}")
    private String loyverseToken;


    @Value("${sr.we.business.id}")
    private Long businessId;

    public BatchService(ItemsController ItemService, BatchRepository repository, BatchItemsRepository batchItemsRepository, LoyItemsController loyItemsController, StoresController storesController, LoyInventoryController inventoryController) {
        this.repository = repository;
        this.batchItemsRepository = batchItemsRepository;
        this.loyItemsController = loyItemsController;
        this.storesController = storesController;
        this.inventoryController = inventoryController;
        this.ItemService = ItemService;
    }

    public Optional<Batch> get(Long id) {
        return repository.findById(id);
    }

    public Batch update(Batch entity) throws IOException {

        if (entity.getId() != null) {

            Section section = storesController.oneStore(businessId, entity.getSectionId());

            Optional<Batch> batch = get(entity.getId());
            if (batch.isPresent() && batch.get().getStatus().compareTo(Batch.Status.APPROVED) != 0 && entity.getStatus().compareTo(Batch.Status.APPROVED) == 0) {
                List<BatchItems> byBatchId = batchItemsRepository.findByBatchId(entity.getId());
                // import items
                for (BatchItems batchItems : byBatchId) {
                    Item item = null;
                    if (StringUtils.isNotBlank(batchItems.getItemId())) {
                        item = ItemService.oneItem(businessId, batchItems.getItemId());
                    }
                    if (item == null) {
                        item = new Item();
                    } else {
                        item.setId(item.getId().split("\\|")[0]);
                    }
                    item.setItem_name(batchItems.getName());
                    item.setTrack_stock(true);
                    // category
                    if (section.getCategories() != null && !section.getCategories().isEmpty()) {
                        item.setCategory_id(section.getCategories().stream().findFirst().get());
                    }
                    if(section.getForm() != null){
                        item.setForm(section.getForm().name());
                    }
                    if(section.getColor() != null){
                        item.setColor(section.getColor().name());
                    }
                    Variant variant = new Variant();
//                    variant.setSku(batchItems.getSku());
                    variant.setBarcode(batchItems.getCode());
                    variant.setCost(batchItems.getCost());
                    variant.setDefault_price(batchItems.getPrice());
                    variant.setDefault_pricing_type("FIXED");

                    item.setVariants(List.of(variant));
                    Item add = loyItemsController.add(loyverseToken, item);

                    if (add != null && add.getVariants() != null && !add.getVariants().isEmpty() && batchItems.getRealQuantity() != null) {
                        Variant variant1 = add.getVariants().get(0);
                        String id = section.getId();
                        Integer stockAfter = batchItems.getRealQuantity();
                        InventoryLevels inventoryLevels = new InventoryLevels();
                        StockLevel stockLevel = new StockLevel();
                        stockLevel.setBusinessId(businessId);
                        stockLevel.setStock_after(stockAfter);
                        stockLevel.setVariant_id(variant1.getVariant_id());
                        stockLevel.setStore_id(id);
                        inventoryLevels.setInventory_levels(List.of(stockLevel));
                        inventoryController.add(loyverseToken, inventoryLevels);
                    }

                }
            }
        }

        return repository.save(entity);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public Page<Batch> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<Batch> list(Pageable pageable, Specification<Batch> filter) {
        return repository.findAll(filter, pageable);
    }

    public int count() {
        return (int) repository.count();
    }

}

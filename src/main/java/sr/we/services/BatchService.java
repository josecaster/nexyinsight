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
import sr.we.integration.LoyVariantController;
import sr.we.repository.BatchItemsRepository;
import sr.we.repository.BatchRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class BatchService {

    private final BatchRepository repository;
    private final BatchItemsRepository batchItemsRepository;
    private final LoyItemsController loyItemsController;
    private final LoyVariantController loyVariantController;
    private final StoresController storesController;
    private final LoyInventoryController inventoryController;
    private final ItemsController ItemService;

    @Value("${sr.we.loyverse.token}")
    private String loyverseToken;


    @Value("${sr.we.business.id}")
    private Long businessId;

    public BatchService(ItemsController ItemService, BatchRepository repository, BatchItemsRepository batchItemsRepository, LoyItemsController loyItemsController, StoresController storesController, LoyInventoryController inventoryController, LoyVariantController loyVariantController) {
        this.repository = repository;
        this.batchItemsRepository = batchItemsRepository;
        this.loyItemsController = loyItemsController;
        this.storesController = storesController;
        this.inventoryController = inventoryController;
        this.ItemService = ItemService;
        this.loyVariantController = loyVariantController;
    }

    public Optional<Batch> get(Long id) {
        return repository.findById(id);
    }

    public Batch update(Batch entity) throws IOException {

        if (entity.getId() != null) {

            Section section = storesController.oneStore(entity.getSectionId());

            Optional<Batch> batch = get(entity.getId());
            if (batch.isPresent() && batch.get().getStatus().compareTo(Batch.Status.APPROVED) != 0 && entity.getStatus().compareTo(Batch.Status.APPROVED) == 0) {
                List<BatchItems> byBatchId = batchItemsRepository.findByBatchId(entity.getId());
                // import items
                for (BatchItems batchItems : byBatchId) {
                    if (batchItems.isUpload()) {
                        Item item = null;
                        if (StringUtils.isNotBlank(batchItems.getItemId())) {
                            item = ItemService.oneItem(batchItems.getItemId());
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
                        if (section.getForm() != null) {
                            item.setForm(section.getForm().name());
                        }
                        if (section.getColor() != null) {
                            item.setColor(section.getColor().name());
                        }
                        item.setOption1_name(batchItems.getOptionName1());
                        item.setOption2_name(batchItems.getOptionName2());
                        item.setOption3_name(batchItems.getOptionName3());
                        Item add = loyItemsController.add(loyverseToken, item);
//                        List<Variant> variants = new ArrayList<>();
                        Variant variant = new Variant();
                        variant.setBarcode(batchItems.getCode());
                        variant.setCost(batchItems.getCost());
                        variant.setDefault_price(batchItems.isOptional() ? null : batchItems.getPrice());
                        variant.setDefault_pricing_type(batchItems.isOptional() ? "VARIABLE" : "FIXED");
                        variant.setOption1_value(batchItems.getOptionValue1());
                        variant.setOption2_value(batchItems.getOptionValue2());
                        variant.setOption3_value(batchItems.getOptionValue3());
                        VariantStore e1 = new VariantStore();
                        e1.setPricing_type(batchItems.isOptional() ? "VARIABLE" : "FIXED");
                        e1.setPrice(batchItems.isOptional() ? 0d : batchItems.getPrice().doubleValue());
                        e1.setStore_id(section.getUuId());
                        e1.setAvailable_for_sale(true);
                        variant.setStores(List.of(e1));
//                        variants.add(variant);
//                        item.setVariants(variants);

                        Variant add1 = loyVariantController.add(loyverseToken, variant);

                        if (add1 != null) {
                            batchItems.setSku(add1.getSku());
                            String id = add.getId() + "|" + add1.getVariant_id() + "|" + (add1.getStores().isEmpty() ? "" : add1.getStores().get(0).getStore_id());
                            batchItems.setItemId(id);
                            batchItems.setUpload(false);
                            batchItems = batchItemsRepository.save(batchItems);

                            if (batchItems.getRealQuantity() != null) {
                                InventoryLevels inventoryLevels = getInventoryLevels(batchItems, add1, section);
                                inventoryController.add(loyverseToken, inventoryLevels);
                            }

                        }
                    }
                }
            }
        }

        return repository.save(entity);
    }

    private InventoryLevels getInventoryLevels(BatchItems batchItems, Variant variant1, Section section) {
        String id = section.getId();
        Integer stockAfter = batchItems.getRealQuantity();
        InventoryLevels inventoryLevels = new InventoryLevels();
        StockLevel stockLevel = new StockLevel();
        stockLevel.setBusinessId(businessId);
        stockLevel.setStock_after(stockAfter);
        stockLevel.setVariant_id(variant1.getVariant_id());
        stockLevel.setStore_id(id);
        inventoryLevels.setInventory_levels(List.of(stockLevel));
        return inventoryLevels;
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

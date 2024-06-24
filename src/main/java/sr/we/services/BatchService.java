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
import java.util.*;

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
            Map<String, String> codeItemMap = new HashMap<>();
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
                        item.setOption1_name(StringUtils.isBlank(batchItems.getOptionName1()) ? null : batchItems.getOptionName1());
                        item.setOption2_name(StringUtils.isBlank(batchItems.getOptionName2()) ? null : batchItems.getOptionName2());
                        item.setOption3_name(StringUtils.isBlank(batchItems.getOptionName3()) ? null : batchItems.getOptionName3());
                        codeItemMap.put(batchItems.getCode(), item.getId());
//                        List<Variant> variants = new ArrayList<>();
                        Variant variant = new Variant();
                        variant.setBarcode(batchItems.getCode());
                        variant.setCost(batchItems.getCost());
                        variant.setDefault_price(batchItems.isOptional() ? null : batchItems.getPrice());
                        variant.setDefault_pricing_type(batchItems.isOptional() ? "VARIABLE" : "FIXED");
                        variant.setOption1_value(StringUtils.isBlank(batchItems.getOptionValue1()) ? null : batchItems.getOptionValue1());
                        variant.setOption2_value(StringUtils.isBlank(batchItems.getOptionValue2()) ? null : batchItems.getOptionValue2());
                        variant.setOption3_value(StringUtils.isBlank(batchItems.getOptionValue3()) ? null : batchItems.getOptionValue3());
//                        variant.setItem_id(item.getId());
                        VariantStore e1 = new VariantStore();
                        e1.setPricing_type(batchItems.isOptional() ? "VARIABLE" : "FIXED");
                        if(!batchItems.isOptional()) {
                            e1.setPrice(batchItems.getPrice());
                        }
                        e1.setStore_id(section.getId());
                        e1.setAvailable_for_sale(true);
                        variant.setStores(List.of(e1));
//                        variants.add(variant);
                        item.setVariants(List.of(variant));
                        item = loyItemsController.add(loyverseToken, item);

//                        variant = loyVariantController.add(loyverseToken, variant);

                        if (item.getVariants() != null && !item.getVariants().isEmpty()) {
                            variant = item.getVariants().get(0);
                            batchItems.setSku(variant.getSku());
                            String id = item.getId() + "|" + variant.getVariant_id() + "|" + (variant.getStores().isEmpty() ? "" : variant.getStores().get(0).getStore_id());
                            batchItems.setItemId(id);
                            batchItems.setUpload(false);
                            batchItems = batchItemsRepository.save(batchItems);

                            if (batchItems.getRealQuantity() != null) {
                                InventoryLevels inventoryLevels = getInventoryLevels(batchItems, variant, section);
                                inventoryController.add(loyverseToken, inventoryLevels);
                            }

                        }
                    }
                }
            }
        }

        return repository.save(entity);
    }

    private InventoryLevels getInventoryLevels(BatchItems batchItems, Variant variant, Section section) {
        String id = section.getId();
        Integer stockAfter = batchItems.getRealQuantity();
        InventoryLevels inventoryLevels = new InventoryLevels();
        StockLevel stockLevel = new StockLevel();
        stockLevel.setBusinessId(businessId);
        stockLevel.setStock_after(stockAfter);
        stockLevel.setVariant_id(variant.getVariant_id());
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

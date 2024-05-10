package sr.we.schedule;

import jakarta.annotation.PostConstruct;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import sr.we.entity.SyncTime;
import sr.we.entity.eclipsestore.tables.*;
import sr.we.integration.*;
import sr.we.repository.SyncTimeRepository;
import sr.we.repository.TaskRepository;
import sr.we.services.TaskService;
import sr.we.storage.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Component
public class JobbyLauncher implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobbyLauncher.class);
    private final TaskScheduler taskScheduler;
    private final TaskRepository taskRepository;
    private final TaskService taskService;
    @Autowired
    IStoreStorage storeStorage;
    @Autowired
    IReceiptsStorage receiptsStorage;
    @Autowired
    IInventoryHistoryStorage inventoryHistoryStorage;
    @Autowired
    ICategoryStorage categoryStorage;
    @Autowired
    IDeviceStorage deviceStorage;
    @Autowired
    IItemStorage itemStorage;
    @Autowired
    SyncTimeRepository syncTimeRepository;
    @Autowired
    LoyStoresController loyStoreController;
    @Autowired
    LoyCategoryController loyCategoryController;
    @Autowired
    LoyDeviceController loyDeviceController;
    @Autowired
    LoyItemsController loyItemsController;
    @Autowired
    LoyInventoryController inventoryController;
    @Autowired
    LoyReceiptsController loyReceiptsController;
    @Autowired
    private IInventoryValuationStorage iInventoryValuationStorage;
    @Value("${sr.we.loyverse.token}")
    private String loyverseToken;
    @Value("${sr.we.business.id}")
    private Long businessId;
    private boolean itemsBusy = false;

    public JobbyLauncher(TaskScheduler taskScheduler, TaskRepository taskRepository, TaskService taskService) {
        this.taskScheduler = taskScheduler;
        this.taskRepository = taskRepository;
        this.taskService = taskService;

    }

    @PostConstruct
    public void init() {
        taskScheduler.schedule(this, new MainTaskTrigger(businessId, taskRepository, taskService));
    }

    @Override
    public void run() {
        runItems();
        updateStockLevels();
        runReceipts();
    }

    private String getLoyverseToken() {
        return loyverseToken;
    }

    private Long getBusinessId() {
        return businessId;
    }

    //    @Scheduled(fixedDelay = 300000)
    public void runItems() {
        LOGGER.info("SCHEDULED run of items STARTED");
        if (!itemsBusy) {
            itemsBusy = true;
            try {
                storeStores();
                storeCategories();
                storeDevices();
                storeItems();
//                syncItems();
            } catch (Exception ex) {
                LOGGER.error(ex.getMessage());
            }
            itemsBusy = false;
        }
    }

    private void syncItems() {
        List<Item> items = itemStorage.allItems(getBusinessId());
        if (items != null && !items.isEmpty()) {

//            String itemIds = items.stream().map(l -> {
//                return l.getId().split("\\|")[0];
//            }).collect(Collectors.joining(","));
            LOGGER.info("Sync size [" + items.size() + "]");
            for (Item iitem : items) {
                ListLoyItems listLoyItems = loyItemsController.getListLoyItems(iitem.getId().split("\\|")[0], null, null, null, null, null, null, loyverseToken);
                if (listLoyItems.getItems() != null && !listLoyItems.getItems().isEmpty()) {
                    for (Item item : listLoyItems.getItems()) {
                        String itemId = item.getId();
                        if (item.getVariants() == null) {
                            continue;
                        }
                        for (Variant variant : item.getVariants()) {
                            for (VariantStore store : variant.getStores()) {
                                String id = itemId + "|" + variant.getVariant_id() + "|" + store.getStore_id();
                                Item oneItem = itemStorage.oneItemByLoyId(id);
                                if (oneItem != null) {
                                    oneItem.setDeleted_at(item.getDeleted_at());
                                    itemStorage.saveOrUpdate(oneItem);
                                }
                            }
                        }
                    }
                }
            }
            LOGGER.info("Sync done");

        }
    }

    //    @Scheduled(fixedDelay = 300000, initialDelay = 300000)
    private void updateStockLevels() {
        LOGGER.info("SCHEDULED run of stock levels STARTED");
        doForItems(itemStorage.allItems(getBusinessId()), true);
    }

    //    @Scheduled(fixedDelay = 300000, initialDelay = 300000)
    public void runReceipts() {
        LOGGER.info("SCHEDULED run of receipts STARTED");
        if (!itemsBusy) {
            itemsBusy = true;
            try {
                storeReceipts();
            } catch (Exception ex) {
                LOGGER.error(ex.getMessage());
            }
            itemsBusy = false;
        }
    }

    /**
     * Get Loyverse ITEM List
     *
     * @param items_ids      items_ids
     * @param created_at_min created_at_min
     * @param created_at_max created_at_max
     * @param updated_at_min updated_at_min
     * @param updated_at_max updated_at_max
     * @param limit          limit
     * @param cursor         cursor
     * @return ListLoyItems
     */
    public ListLoyItems getListLoyItems(String items_ids, LocalDateTime created_at_min, LocalDateTime created_at_max, LocalDateTime updated_at_min, LocalDateTime updated_at_max, Integer limit, String cursor) {
        String url = "https://api.loyverse.com/v1.0/items?show_deleted=false";
        StringBuilder stringBuilder = new StringBuilder(url);
        if (StringUtils.isNotBlank(items_ids)) {
            stringBuilder.append("&items_ids=").append(items_ids);
        }
        String pattern = "YYYY-MM-dd'T'HH:mm:ss.SSS'Z'";
        if (created_at_min != null) {
            stringBuilder.append("&created_at_min=").append(DateTimeFormatter.ofPattern(pattern).format(created_at_min));
        }
        if (created_at_max != null) {
            stringBuilder.append("&created_at_max=").append(DateTimeFormatter.ofPattern(pattern).format(created_at_max));
        }
        if (updated_at_min != null) {
            stringBuilder.append("&updated_at_min=").append(DateTimeFormatter.ofPattern(pattern).format(updated_at_min));
        }
        if (updated_at_max != null) {
            stringBuilder.append("&updated_at_max=").append(DateTimeFormatter.ofPattern(pattern).format(updated_at_max));
        }
        if (limit != null) {
            stringBuilder.append("&limit=").append(limit);
        }
        if (cursor != null) {
            stringBuilder.append("&cursor=").append(cursor);
        }
        url = stringBuilder.toString();


        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<String> httpEntity = getAuthHttpEntity(getLoyverseToken());
        ResponseEntity<ListLoyItems> exchange = restTemplate.exchange(url, HttpMethod.GET, httpEntity, ListLoyItems.class);
        return exchange.getBody();
    }

    /**
     * Get & save Loyverse STORES
     */
    private void storeStores() {
        ListLoyStores list = loyStoreController.getListLoyStores(null, null, null, null, null, getLoyverseToken());
        if (list != null && !list.getStores().isEmpty()) {
            for (LoyStore store : list.getStores()) {
                if (storeStorage.oneStore(store.id()) == null) {
                    Section section1 = new Section(getBusinessId(), store.id(), store.name());
                    section1.setDefault_name(store.name());
                    storeStorage.saveOrUpdate(section1);
                }
            }
        }
    }


    /**
     * Get & save Loyverse Categories
     */
    private void storeCategories() {
        CollectCategories categoryCollect = loyCategoryController.getSwicthList(null, getLoyverseToken());
        if (categoryCollect != null && !categoryCollect.getCategories().isEmpty()) {
            for (Category category : categoryCollect.getCategories()) {
                if (categoryStorage.oneStore(category.getId()) == null) {
                    category.setBusinessId(getBusinessId());
                    categoryStorage.saveOrUpdate(category);
                }
            }
        }
    }

    /**
     * Get & Save Loyverse Devices
     */
    private void storeDevices() {
        CollectDevices deviceCollect = loyDeviceController.getSwitchList(null, getLoyverseToken());
        if (deviceCollect != null && !deviceCollect.getPos_devices().isEmpty()) {
            for (Device device : deviceCollect.getPos_devices()) {
                if (deviceStorage.oneStore(device.getId()) == null) {
                    device.setBusinessId(getBusinessId());
                    deviceStorage.saveOrUpdate(device);
                }
            }
        }
    }

    /**
     * Save ITEMS in this system
     */
    private void storeItems() {
        String cursor = null;

        LocalDateTime maxTime = getItemsLastUpdated(false);
        boolean run;

        run = true;
        List<Item> items = new ArrayList<>();
        while (run) {
            ListLoyItems listLoyItems = loyItemsController.getListLoyItems(null, null, null, null, null, 250, cursor, getLoyverseToken());

            if (listLoyItems != null && listLoyItems.getItems() != null) {
                items.addAll(listLoyItems.getItems());
            }

            cursor = listLoyItems == null ? null : listLoyItems.getCursor();
            run = StringUtils.isNotBlank(cursor);
            LOGGER.info("Items: " + (listLoyItems == null ? 0 : (listLoyItems.getItems() == null ? 0 : listLoyItems.getItems().size())));
        }

        doForItems(items, true);

        getItemsLastUpdated(true);
    }

    public void doForItems(List<Item> items, boolean b) {
        if(b) {
            List<StockLevel> levels = getStockLevels(); // get stock levels
            iterateItems(items, levels, true);// rectify the amounts
        } else {
            iterateItems(items, new ArrayList<>(), false);// rectify the amounts
        }
    }


    private LocalDateTime getItemsLastUpdated(boolean update) {
        SyncTime byType = syncTimeRepository.getByTypeAndBusinessId(SyncTime.SyncType.ITEMS, getBusinessId());
        LocalDateTime maxTime = null;
        LOGGER.info("Item Update?[" + update + "] " + byType);
        if (!update) {
            if (byType == null) {
                List<Item> itemss = itemStorage.allItems(getBusinessId());
                if (itemss != null && !itemss.isEmpty()) {
                    maxTime = itemss.stream().map(Item::getUpdated_at).max(LocalDateTime::compareTo).get();
                }
                byType = new SyncTime();
                byType.setType(SyncTime.SyncType.ITEMS);
                byType.setMaxTime(maxTime);
                byType.setBusinessId(getBusinessId());
            } else {
                maxTime = byType.getMaxTime();
            }

            if (maxTime == null && byType.getId() != null) {
                syncTimeRepository.delete(byType);
            } else {
                syncTimeRepository.save(byType);
            }
        } else {
            List<Item> itemss = itemStorage.allItems(getBusinessId());
            if (itemss != null && !itemss.isEmpty()) {
                maxTime = itemss.stream().map(Item::getUpdated_at).max(LocalDateTime::compareTo).get();
            }
            if (byType == null) {
                byType = new SyncTime();
                byType.setType(SyncTime.SyncType.ITEMS);
                byType.setBusinessId(getBusinessId());
            }
            byType.setMaxTime(maxTime);
            syncTimeRepository.save(byType);
        }
        LOGGER.info("Item Update?[" + update + "] MaxTime[" + maxTime + "]");
        return maxTime;
    }

    /**
     * Get Loyverse Stock Levels
     *
     * @return List<StockLevel>
     */
    private List<StockLevel> getStockLevels() {
        boolean run = true;
        List<StockLevel> levels = new ArrayList<>();
        String cursor = null;
        while (run) {
            InventoryLevels listLoyItems = inventoryController.getListLoyItems(null, null, null, null, 250, cursor, getLoyverseToken());
            cursor = listLoyItems == null ? null : listLoyItems.getCursor();
            run = StringUtils.isNotBlank(cursor);

            if (listLoyItems != null && listLoyItems.getInventory_levels() != null) {
                levels.addAll(listLoyItems.getInventory_levels());
            }
        }
        return levels;
    }

    /**
     * Save Receipts in this system
     */
    private void storeReceipts() {
        String cursor = null;
        boolean run;
        run = true;
        List<Receipt> receipts = new ArrayList<>();

        LocalDateTime maxTime = getReceiptsMaxTime(false);


        while (run) {
            CollectReceipts listLoyItems = loyReceiptsController.getListLoyStores(null, null, null, null, 250, cursor, getLoyverseToken());
            cursor = listLoyItems == null ? null : listLoyItems.getCursor();
            run = StringUtils.isNotBlank(cursor);

            if (listLoyItems != null && listLoyItems.getReceipts() != null) {
                receipts.addAll(listLoyItems.getReceipts());
            }
        }


        doForReceipt(receipts);

        getReceiptsMaxTime(true);
    }

    public void doForReceipt(List<Receipt> receipts) {
        if (!receipts.isEmpty()) {
            for (Receipt receipt : receipts) {
                String receiptNumber = receipt.getReceipt_number();
                for (LineItem item : receipt.getLine_items()) {
                    String id = receiptNumber + "-" + item.getId();
                    Receipt receipt1 = receiptsStorage.oneReceiptNumber(id);
                    if (receipt1 != null) {
//                        receipt.setUuId(receipt1.getUuId());// dont proceed yet, still need to figure out how we will handle it
                        update(receipt1, item, id);
                    } else {
                        if (StringUtils.isNotBlank(receipt.getUuId())) {
                            update(receipt.clone(), item, id);
                        } else {
                            update(receipt, item, id);
                        }
                    }


                }
            }
        }
    }

    private LocalDateTime getReceiptsMaxTime(boolean update) {

        SyncTime byType = syncTimeRepository.getByTypeAndBusinessId(SyncTime.SyncType.RECEIPTS, getBusinessId());
        LocalDateTime maxTime = null;
        LOGGER.info("Receipt Update?[" + update + "] " + byType);
        if (!update) {
            if (byType == null) {
                List<Receipt> items = receiptsStorage.allReceipts(getBusinessId());
                if (items != null && !items.isEmpty()) {
                    maxTime = items.stream().map(Receipt::getUpdated_at).max(LocalDateTime::compareTo).get();
                }
                byType = new SyncTime();
                byType.setMaxTime(maxTime);
                byType.setType(SyncTime.SyncType.RECEIPTS);
                byType.setBusinessId(getBusinessId());
            } else {
                maxTime = byType.getMaxTime();
            }

            if (maxTime == null && byType.getId() != null) {
                syncTimeRepository.delete(byType);
            } else {
                syncTimeRepository.save(byType);
            }
        } else {
            List<Receipt> items = receiptsStorage.allReceipts(getBusinessId());
            if (items != null && !items.isEmpty()) {
                maxTime = items.stream().map(Receipt::getUpdated_at).max(LocalDateTime::compareTo).get();
            }
            byType.setMaxTime(maxTime);
            syncTimeRepository.save(byType);
        }
        LOGGER.info("Receipt Update?[" + update + "] MaxTime[" + maxTime + "]");
        return maxTime;
    }

    private void update(Receipt receipt, LineItem item, String id) {
        receipt.setLine_items(null);
        receipt.setLine_item(item);
        Item item1 = itemStorage.oneItemByLoyId(item.getItem_id() + "|" + receipt.getLine_item().getVariant_id() + "|" + receipt.getStore_id());
        receipt.setCategory_id(item1 == null ? null : item1.getCategory_id());
        receipt.setForm(item1 == null ? null : item1.getForm());
        receipt.setColor(item1 == null ? null : item1.getColor());
        receipt.setReceipt_number(id);
        receipt.setBusinessId(getBusinessId());
        receiptsStorage.saveOrUpdate(receipt);
    }

    /**
     * Item Iterate
     *
     * @param list   list
     * @param levels levels
     * @param b
     */
    private void iterateItems(List<Item> list, List<StockLevel> levels, boolean b) {
        for (Item item : list) {
            String itemId = item.getId();
            if (item.getVariants() == null) {
                continue;
            }
            for (Variant variant : item.getVariants()) {

                for (VariantStore store : variant.getStores()) {
                    String id = itemId + "|" + variant.getVariant_id() + "|" + store.getStore_id();

                    Item oneItem = itemStorage.oneItemByLoyId(id);
                    if (oneItem != null) {
                        oneItem.setBusinessId(item.getBusinessId());
                        oneItem.setItem_name(item.getItem_name());
                        oneItem.setCategory_id(item.getCategory_id());
                        oneItem.setColor(item.getColor());
                        oneItem.setId(item.getId());// this is the loyverse Id
                        oneItem.setComponents(item.getComponents());
                        oneItem.setForm(item.getForm());
                        oneItem.setHandle(item.getHandle());
                        oneItem.setImage_url(item.getImage_url());
                        oneItem.setIs_composite(item.isIs_composite());
                        oneItem.setIs_composite_string(item.getIs_composite_string());
                        oneItem.setVariants(item.getVariants());
                        oneItem.setUse_production(item.isUse_production());
                        oneItem.setTrack_stock(item.isTrack_stock());
                        oneItem.setTax_ids(item.getTax_ids());
                        oneItem.setSold_by_weight(item.isSold_by_weight());
                        oneItem.setSeaqnsUuId(item.getSeaqnsUuId());
                        oneItem.setReference_id(item.getReference_id());
                        oneItem.setPrimary_supplier_id(item.getPrimary_supplier_id());
                        oneItem.setOption3_name(item.getOption3_name());
                        oneItem.setOption2_name(item.getOption2_name());
                        oneItem.setOption1_name(item.getOption1_name());
                        oneItem.setModifiers_ids(item.getModifiers_ids());
                        oneItem.setStoreCountMap(item.getStoreCountMap());
                        oneItem.setLastUpdateStockLevel(item.getLastUpdateStockLevel());
                        iterateStockLevels(oneItem, variant, store, levels, id,b);
                    } else {
                        if (StringUtils.isNotBlank(item.getUuId())) {// this is to split item variants in different nexy-insight items
                            iterateStockLevels(item.clone(), variant, store, levels, id, b);
                        } else {
                            iterateStockLevels(item, variant, store, levels, id, b);
                        }
                    }


                }
            }

        }

        updateInventoryEvaluation();
    }

    private void updateInventoryEvaluation() {
        // update inventory evaluation
        List<Item> items = itemStorage.allItems(getBusinessId());
        LocalDate now = LocalDate.now();
        Optional<InventoryValuation> inventoryValuationOptional = iInventoryValuationStorage.getInventoryValuation(getBusinessId(), now);
        InventoryValuation inventoryValuation = null;
        if (inventoryValuationOptional.isPresent()) {
            inventoryValuation = inventoryValuationOptional.get();
        } else {
            inventoryValuation = new InventoryValuation();
            inventoryValuation.setLocalDate(now);
            inventoryValuation.setBusinessId(getBusinessId());
            inventoryValuation.setType(InventoryValuation.Type.AUTOMATIC);
        }
        if (inventoryValuation.getType().compareTo(InventoryValuation.Type.AUTOMATIC) == 0) {
            BigDecimal inventoryValue = items.stream().filter(f -> f.getStock_level() > 0 && f.getDeleted_at() == null).map(item -> item.getVariant().getCost() != null ? item.getVariant().getCost().multiply(BigDecimal.valueOf(item.getStock_level())) : BigDecimal.ZERO).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal retailValue = items.stream().filter(f -> f.getStock_level() > 0 && f.getDeleted_at() == null).map(item -> item.getVariantStore().getPrice() != 0 ? BigDecimal.valueOf(item.getVariantStore().getPrice()).multiply(BigDecimal.valueOf(item.getStock_level())) : BigDecimal.ZERO).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal profit = retailValue.subtract(inventoryValue);
            BigDecimal divide = inventoryValue.divide(retailValue, 8, RoundingMode.HALF_UP);
            BigDecimal margin = BigDecimal.valueOf(100).subtract(divide.multiply(BigDecimal.valueOf(100)));
            inventoryValuation.setRetailValue(retailValue);
            inventoryValuation.setInventoryValue(inventoryValue);
            inventoryValuation.setPotentialProfit(profit);
            inventoryValuation.setMargin(margin);
            inventoryValuation = iInventoryValuationStorage.saveOrUpdate(inventoryValuation);
            LOGGER.info(inventoryValuation.toString());
        } else {
            LOGGER.info("Can't update inventory valuation manual [" + now + "]");
        }
    }

    /**
     * Iterate stock levels
     *
     * @param item    item
     * @param variant variant
     * @param store   store
     * @param levels  levels
     * @param id      id
     * @param b
     */
    private void iterateStockLevels(Item item, Variant variant, VariantStore store, List<StockLevel> levels, String id, boolean b) {
        item.setStoreCountMap(new HashMap<>());
        if (b && !levels.isEmpty()) {
            Optional<StockLevel> any = levels.stream().filter(l -> l.getStore_id().equalsIgnoreCase(store.getStore_id()) && l.getVariant_id().equalsIgnoreCase(variant.getVariant_id())).findAny();
            any.ifPresent(stockLevel -> {
                int itemStockLevel = item.getStock_level();
                int inStock = stockLevel.getIn_stock();
                item.setStock_level(inStock);
                item.setLastUpdateStockLevel(stockLevel.getUpdated_at());
                InventoryHistory inventoryHistory = new InventoryHistory();
                inventoryHistory.setBusinessId(getBusinessId());
                Section section1 = storeStorage.oneStore(store.getStore_id());
                inventoryHistory.setSection_id(section1.getUuId());
                inventoryHistory.setLocalDateTime(stockLevel.getUpdated_at());
                inventoryHistory.setItem_id(item.getUuId());
                inventoryHistory.setStock_after(inStock);
                inventoryHistory.setAdjustment(inStock - itemStockLevel);
                inventoryHistory.setType(InventoryHistory.Type.SYNC);
                inventoryHistoryStorage.saveOrUpdate(inventoryHistory);
            });
        }

        item.setVariants(null);
        item.setVariant(variant);
        item.setVariantStore(store);
        item.setId(id);
        item.setBusinessId(getBusinessId());
        itemStorage.saveOrUpdate(item);
    }

    protected HttpEntity<String> getAuthHttpEntity(String body, String accessToken) {
        HttpHeaders headers = getAuthHttpHeaders(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }

    protected HttpEntity<String> getAuthHttpEntity(String accessToken) {
        return getAuthHttpEntity(null, accessToken);
    }


    protected HttpHeaders getAuthHttpHeaders(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "BEARER " + accessToken);
        return headers;
    }

    public void doForInventoryLevels(List<StockLevel> levels) {



        List<Item> items = itemStorage.allItems(getBusinessId());
        for (Item item : items) {
            VariantStore store = item.getVariantStore();
            Variant variant = item.getVariant();
            Optional<StockLevel> any = levels.stream().filter(l -> l.getStore_id().equalsIgnoreCase(store.getStore_id()) && l.getVariant_id().equalsIgnoreCase(variant.getVariant_id())).findAny();
            any.ifPresent(stockLevel -> {
                int itemStockLevel = item.getStock_level();
                int inStock = stockLevel.getIn_stock();
                item.setStock_level(inStock);
                item.setLastUpdateStockLevel(stockLevel.getUpdated_at());
                InventoryHistory inventoryHistory = new InventoryHistory();
                inventoryHistory.setBusinessId(getBusinessId());
                Section section1 = storeStorage.oneStore(store.getStore_id());
                inventoryHistory.setSection_id(section1.getUuId());
                inventoryHistory.setLocalDateTime(stockLevel.getUpdated_at());
                inventoryHistory.setItem_id(item.getUuId());
                inventoryHistory.setStock_after(inStock);
                inventoryHistory.setAdjustment(inStock - itemStockLevel);
                inventoryHistory.setType(InventoryHistory.Type.SYNC);
                inventoryHistoryStorage.saveOrUpdate(inventoryHistory);

                itemStorage.saveOrUpdate(item);
            });

        }
    }
}

package sr.we.schedule;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import sr.we.entity.eclipsestore.tables.*;
import sr.we.integration.*;
import sr.we.storage.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Component
public class JobbyLauncher {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobbyLauncher.class);
    @Value("${sr.we.loyverse.token}")
    private String loyverseToken;
    @Value("${sr.we.business.id}")
    private Long businessId;
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
    private boolean itemsBusy = false;

    private String getLoyverseToken() {
        return loyverseToken;
    }

    private Long getBusinessId() {
        return businessId;
    }

    @Scheduled(cron = "0 */5 * * * *")
    public void runItems() {
        LOGGER.info("SCHEDULED run of items STARTED");
        if (!itemsBusy) {
            itemsBusy = true;
            try {
                storeStores();
                storeCategories();
                storeDevices();
                storeItems();
            } catch (Exception ex) {
                LOGGER.error(ex.getMessage());
            }
            itemsBusy = false;
        }
    }

    @Scheduled(cron = "0 */10 * * * *")
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

        LocalDateTime maxTime = getItemsLastUpdated();
        boolean run;

        run = true;
        List<Item> items = new ArrayList<>();
        while (run) {
            ListLoyItems listLoyItems = loyItemsController.getListLoyItems(null, null, null, maxTime, null, 250, cursor, getLoyverseToken());

            if (listLoyItems != null && listLoyItems.getItems() != null) {
                items.addAll(listLoyItems.getItems());
            }

            cursor = listLoyItems == null ? null : listLoyItems.getCursor();
            run = StringUtils.isNotBlank(cursor);
            LOGGER.debug("Items: " + (listLoyItems == null ? 0 : (listLoyItems.getItems() == null ? 0 : listLoyItems.getItems().size())));
        }

        List<StockLevel> levels = getStockLevels(); // get stock levels
        iterateItems(items, levels);// rectify the amounts


    }

    private LocalDateTime getItemsLastUpdated() {
        List<Item> itemss = itemStorage.allItems(getBusinessId());
        LocalDateTime maxTime = null;
        if (itemss != null && !itemss.isEmpty()) {
            maxTime = itemss.stream().map(Item::getUpdated_at).max(LocalDateTime::compareTo).get();
        }
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

        List<Receipt> items = receiptsStorage.allReceipts(getBusinessId());

        LocalDateTime maxTime = null;
        if (items != null && !items.isEmpty()) {
            maxTime = items.stream().map(Receipt::getCreated_at).max(LocalDateTime::compareTo).get();
        }


        while (run) {
            CollectReceipts listLoyItems = loyReceiptsController.getListLoyStores(maxTime, null, null, null, 250, cursor, getLoyverseToken());
            cursor = listLoyItems == null ? null : listLoyItems.getCursor();
            run = StringUtils.isNotBlank(cursor);

            if (listLoyItems != null && listLoyItems.getReceipts() != null) {
                for (Receipt receipt : listLoyItems.getReceipts()) {
                    Receipt receipt1 = receiptsStorage.oneReceiptNumber(receipt.getReceipt_number());
                    if (receipt1 != null) {
                        receipt.setUuId(receipt1.getUuId());// dont proceed yet, still need to figure out how we will handle it
                    } else {
                        receipts.add(receipt);
                    }
                }
            }
        }

//        if (items != null && !items.isEmpty()) {
//            for (Receipt receipt : items){
//                Optional<Receipt> any = receipts.stream().filter(l -> l.getReceipt_number().compareTo(receipt.getReceipt_number()) == 0).findAny();
//                if(any.isEmpty()) {
//                    receipts.add(receipt);
//                }
//            }
//        }


        if (!receipts.isEmpty()) {
            for (Receipt receipt : receipts) {
                if (receiptsStorage.oneReceipt(receipt.getReceipt_number()) == null) {
                    receipt.setBusinessId(getBusinessId());
//                    String uuId = receipt.getUuId();
                    receiptsStorage.saveOrUpdate(receipt);
                    for (LineItem item : receipt.getLine_items()) {
                        String id = item.getItem_id() + "|" + item.getVariant_id() + "|" + receipt.getStore_id();
                        Item item1 = itemStorage.oneItemByLoyId(id);

                        if (item1 != null && item1.isTrack_stock() && (item1.getLastUpdateStockLevel() == null || receipt.getReceipt_date().isAfter(item1.getLastUpdateStockLevel()))) {
                            Optional<Section> section = storeStorage.findSection(getBusinessId(), receipt.getStore_id(), item1.getCategory_id(), receipt.getPos_device_id(), true);
                            if (section.isPresent()) {
                                InventoryHistory inventoryHistory = new InventoryHistory();
                                inventoryHistory.setBusinessId(getBusinessId());

                                Section section1 = section.get();
                                inventoryHistory.setSection_id(section1.getUuId());
                                inventoryHistory.setLocalDateTime(receipt.getReceipt_date());
                                inventoryHistory.setItem_id(item1.getUuId());

                                BigDecimal bigDecimal = item1.getStoreCountMap().get(section1.getUuId());
                                BigDecimal sale = (bigDecimal == null ? BigDecimal.ZERO : bigDecimal).add(BigDecimal.valueOf(receipt.getReceipt_type().equalsIgnoreCase("SALE") ? item.getQuantity() * -1 : item.getQuantity()));
                                inventoryHistory.setStock_after(sale.intValue());

                                inventoryHistory.setAdjustment(receipt.getReceipt_type().equalsIgnoreCase("SALE") ? item.getQuantity() * -1 : item.getQuantity());
                                inventoryHistory.setType(receipt.getReceipt_type().equalsIgnoreCase("SALE") ? InventoryHistory.Type.SALE : InventoryHistory.Type.REFUND);

                                item1.addStoreCount(section1.getUuId(), sale);
                                itemStorage.saveOrUpdate(item1);
                                inventoryHistoryStorage.saveOrUpdate(inventoryHistory);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Item Iterate
     *
     * @param list   list
     * @param levels levels
     */
    private void iterateItems(List<Item> list, List<StockLevel> levels) {
        for (Item item : list) {
            String itemId = item.getId();
            for (Variant variant : item.getVariants()) {

                for (VariantStore store : variant.getStores()) {
                    String id = itemId + "|" + variant.getVariant_id() + "|" + store.getStore_id();

                    Item oneItem = itemStorage.oneItemByLoyId(id);
                    if (oneItem != null) {
                        // transfer all needed fields from oneTime to item
//                                        .setUuId(oneItem.getUuId());
                        iterateStockLevels(oneItem, variant, store, levels, id);
                    } else {
                        if (StringUtils.isNotBlank(item.getUuId())) {
                            iterateStockLevels(item.clone(), variant, store, levels, id);
                        } else {
                            iterateStockLevels(item, variant, store, levels, id);
                        }
                    }


                }
            }

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
     */
    private void iterateStockLevels(Item item, Variant variant, VariantStore store, List<StockLevel> levels, String id) {
        item.setStoreCountMap(new HashMap<>());
        if (!levels.isEmpty()) {
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

}

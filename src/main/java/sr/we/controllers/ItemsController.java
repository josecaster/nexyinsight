package sr.we.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import sr.we.entity.Role;
import sr.we.entity.User;
import sr.we.entity.eclipsestore.tables.Item;
import sr.we.entity.eclipsestore.tables.Receipt;
import sr.we.entity.eclipsestore.tables.Section;
import sr.we.integration.LoyItemsController;
import sr.we.integration.Parent;
import sr.we.storage.IItemStorage;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;


@Controller
public class ItemsController extends Parent {

    @Autowired
    private IItemStorage itemStorage;

    @Autowired
    private LoyItemsController loyItemsController;

    @Autowired
    private StoresRestController storesRestController;

    private static Predicate<Item> getItemPredicate(List<Section> sectionIds) {
        return f -> {
            boolean contains = true;
            if (sectionIds != null) {
                contains = sectionIds.stream().anyMatch(l -> {
                    boolean cont = l.getId().equalsIgnoreCase(f.getVariantStore().getStore_id());
                    if (cont && l.getCategories() != null && !l.getCategories().isEmpty()) {
                        cont = l.getCategories().contains(f.getCategory_id());
                    }
                    return cont;
                });
            }
            return contains && f.getStock_level() != 0;
        };
    }

    public Item oneItem(Long businessId, String id) {
        return itemStorage.oneItem(id);
    }

    /**
     * For now we would like to show each variant as  different item
     *
     * @param businessId
     * @param page
     * @param pageSize
     * @param user
     * @return
     */
    public Stream<Item> allItems(Long businessId, Integer page, Integer pageSize, User user, Predicate<? super Item> predicate) {

        List<String> sectionIds;
        List<Section> sections = storesRestController.allStores(businessId);
        if (user != null && !user.getRoles().contains(Role.ADMIN)) {
            sectionIds = user.getLinkSections();
            if (sectionIds != null) {
                sections = sections.stream().filter(f -> sectionIds.contains(f.getUuId())).toList();
            }
        } else {
            sectionIds = null;
        }

        return (page == null || pageSize == null) ? itemStorage.allItems(businessId).stream().filter(getItemPredicate(sections)).filter(predicate) : itemStorage.allItems(businessId).stream().filter(getItemPredicate(sections)).filter(predicate).sorted(Comparator.comparing(Item::getItem_name)).skip((long) page * pageSize).limit(pageSize);
    }

    public Item addNewItem(@RequestBody Item item) {
        return itemStorage.saveOrUpdate(item);
    }

//    public Void sync(@RequestHeader(name = "Authorization") @PathVariable("businessId") Long businessId) {
//
//        ExecutorService executorService = Executors.newFixedThreadPool(1);
//        ThisUser user = getUser();
//        executorService.execute(new Runnable() {
//            @Override
//            public void run() {
//
//                List<Item> items = itemStorage.allItems(businessId);
//                LocalDateTime maxTime = null;
//                String cursor = null;
////                if (items != null && !items.isEmpty()) {
////                    maxTime = items.stream().map(Item::getUpdated_at).max(LocalDateTime::compareTo).get();
////                }
//                IBusinessRepository businessRepository = ContextProvider.getBean(IBusinessRepository.class);
//                Long id1 = user.getId();
//                Business business = businessRepository.getBusiness(businessId, id1);
//                LoyInventoryController inventoryController = ContextProvider.getBean(LoyInventoryController.class);
//                boolean run = true;
//                List<StockLevel> levels = new ArrayList<>();
//                while (run) {
//                    InventoryLevels listLoyItems = inventoryController.getListLoyItems(null, null, null, null, 250, cursor, business);
//                    cursor = listLoyItems == null ? null : listLoyItems.getCursor();
//                    run = StringUtils.isNotBlank(cursor);
//
//                    if (listLoyItems != null && listLoyItems.getInventory_levels() != null) {
//                        levels.addAll(listLoyItems.getInventory_levels());
//                    }
//                }
//
//                run = true;
//                cursor = null;
//                List<Item> itams = new ArrayList<>();
//                while (run) {
//
//                    ListLoyItems list = loyItemsController.getListLoyItems(null, null, null, maxTime, null, 250, cursor, business);
//
//                    if (list != null && list.getItems() != null) {
//                        itams.addAll(list.getItems());
//                    }
//
//                    cursor = list == null ? null : list.getCursor();
//                    run = StringUtils.isNotBlank(cursor);
//                    System.out.println("Items: " + (list == null ? 0 : (list.getItems() == null ? 0 : list.getItems().size())));
//                }
//
//                extracted(itams, levels);
//            }
//
//            private void extracted(List<Item> list, List<StockLevel> levels) {
//                for (Item item : list) {
//                    String itemId = item.getId();
//                    for (Variant variant : item.getVariants()) {
//
//                        for (VariantStore store : variant.getStores()) {
//                            String id = itemId + "|" + variant.getItem_id() + "|" + store.getStore_id();
//
//                            Item oneItem = itemStorage.oneItemByLoyId(id);
//                            if (oneItem != null) {
//                                // transfer all needed fields from oneTime to item
////                                        item.setUuId(oneItem.getUuId());
//                                extracted(oneItem, variant, store, levels, id);
//                            } else {
//                                if (StringUtils.isNotBlank(item.getUuId())) {
//                                    extracted(item.clone(), variant, store, levels, id);
//                                } else {
//                                    extracted(item, variant, store, levels, id);
//                                }
//                            }
//
//
//                        }
//                    }
//
//                }
//            }
//
//            private void extracted(Item item, Variant variant, VariantStore store, List<StockLevel> levels, String id) {
//                item.setStoreCountMap(new HashMap<>());
//                if (!levels.isEmpty()) {
//                    Optional<StockLevel> any = levels.stream().filter(l -> l.getStore_id().equalsIgnoreCase(store.getStore_id()) && l.getVariant_id().equalsIgnoreCase(variant.getVariant_id())).findAny();
//                    any.ifPresent(stockLevel -> {
//                        item.setStock_level(stockLevel.getIn_stock());
//                        item.addStoreCount(store.getStore_id(), BigDecimal.valueOf(item.getStock_level()));
//                    });
//                }
//
//                item.setVariants(null);
//                item.setVariant(variant);
//                item.setVariantStore(store);
//                item.setId(id);
//                item.setBusinessId(businessId);
//                itemStorage.saveOrUpdate(item);
//            }
//        });
//        return null;
//    }

    public Item updateItem(Item item) {
        return itemStorage.saveOrUpdate(item);
    }

    public boolean deleteItem(String id) {
        return itemStorage.deleteItem(id);
    }

}

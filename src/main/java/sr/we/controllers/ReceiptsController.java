package sr.we.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import sr.we.entity.eclipsestore.tables.Receipt;
import sr.we.integration.LoyReceiptsController;
import sr.we.integration.Parent;
import sr.we.storage.impl.ReceiptStorage;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.function.Predicate;
import java.util.stream.Stream;


@Controller
public class ReceiptsController extends Parent{

    @Autowired
    private ReceiptStorage receiptStorage;

    @Autowired
    private LoyReceiptsController loyReceiptsController;

    public Receipt oneReceipt(Long businessId, String id) {
        return receiptStorage.oneReceipt(id);
    }

    /**
     * For now we would like to show each variant as  different Receipt
     *
     * @param businessId
     * @param page
     * @param pageSize
     * @return
     */
    public Stream<Receipt> allReceipts(Long businessId, Integer page, Integer pageSize, Predicate<? super Receipt> predicate) {
        return (page == null || pageSize == null) ? receiptStorage.allReceipts(businessId).stream().filter(predicate): receiptStorage.allReceipts(businessId).stream().filter(predicate).sorted(Comparator.comparing(Receipt::getReceipt_date).reversed()).skip((long) page * pageSize).limit(pageSize);
    }

    public Receipt addNewReceipt(@RequestBody Receipt Receipt) {
        return receiptStorage.saveOrUpdate(Receipt);
    }

//    public Void sync(@RequestHeader(name = "Authorization") @PathVariable("businessId") Long businessId) {
//
//        ExecutorService executorService = Executors.newFixedThreadPool(1);
//        ThisUser user = getUser();
//        executorService.execute(new Runnable() {
//            @Override
//            public void run() {
//
//                List<Receipt> Receipts = ReceiptStorage.allReceipts(businessId);
//                LocalDateTime maxTime = null;
//                String cursor = null;
////                if (Receipts != null && !Receipts.isEmpty()) {
////                    maxTime = Receipts.stream().map(Receipt::getUpdated_at).max(LocalDateTime::compareTo).get();
////                }
//                IBusinessRepository businessRepository = ContextProvider.getBean(IBusinessRepository.class);
//                Long id1 = user.getId();
//                Business business = businessRepository.getBusiness(businessId, id1);
//                LoyInventoryController inventoryController = ContextProvider.getBean(LoyInventoryController.class);
//                boolean run = true;
//                List<StockLevel> levels = new ArrayList<>();
//                while (run) {
//                    InventoryLevels listLoyReceipts = inventoryController.getListLoyReceipts(null, null, null, null, 250, cursor, business);
//                    cursor = listLoyReceipts == null ? null : listLoyReceipts.getCursor();
//                    run = StringUtils.isNotBlank(cursor);
//
//                    if (listLoyReceipts != null && listLoyReceipts.getInventory_levels() != null) {
//                        levels.addAll(listLoyReceipts.getInventory_levels());
//                    }
//                }
//
//                run = true;
//                cursor = null;
//                List<Receipt> itams = new ArrayList<>();
//                while (run) {
//
//                    ListLoyReceipts list = loyReceiptsController.getListLoyReceipts(null, null, null, maxTime, null, 250, cursor, business);
//
//                    if (list != null && list.getReceipts() != null) {
//                        itams.addAll(list.getReceipts());
//                    }
//
//                    cursor = list == null ? null : list.getCursor();
//                    run = StringUtils.isNotBlank(cursor);
//                    System.out.println("Receipts: " + (list == null ? 0 : (list.getReceipts() == null ? 0 : list.getReceipts().size())));
//                }
//
//                extracted(itams, levels);
//            }
//
//            private void extracted(List<Receipt> list, List<StockLevel> levels) {
//                for (Receipt Receipt : list) {
//                    String ReceiptId = Receipt.getId();
//                    for (Variant variant : Receipt.getVariants()) {
//
//                        for (VariantStore store : variant.getStores()) {
//                            String id = ReceiptId + "|" + variant.getReceipt_id() + "|" + store.getStore_id();
//
//                            Receipt oneReceipt = ReceiptStorage.oneReceiptByLoyId(id);
//                            if (oneReceipt != null) {
//                                // transfer all needed fields from oneTime to Receipt
////                                        Receipt.setUuId(oneReceipt.getUuId());
//                                extracted(oneReceipt, variant, store, levels, id);
//                            } else {
//                                if (StringUtils.isNotBlank(Receipt.getUuId())) {
//                                    extracted(Receipt.clone(), variant, store, levels, id);
//                                } else {
//                                    extracted(Receipt, variant, store, levels, id);
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
//            private void extracted(Receipt Receipt, Variant variant, VariantStore store, List<StockLevel> levels, String id) {
//                Receipt.setStoreCountMap(new HashMap<>());
//                if (!levels.isEmpty()) {
//                    Optional<StockLevel> any = levels.stream().filter(l -> l.getStore_id().equalsIgnoreCase(store.getStore_id()) && l.getVariant_id().equalsIgnoreCase(variant.getVariant_id())).findAny();
//                    any.ifPresent(stockLevel -> {
//                        Receipt.setStock_level(stockLevel.getIn_stock());
//                        Receipt.addStoreCount(store.getStore_id(), BigDecimal.valueOf(Receipt.getStock_level()));
//                    });
//                }
//
//                Receipt.setVariants(null);
//                Receipt.setVariant(variant);
//                Receipt.setVariantStore(store);
//                Receipt.setId(id);
//                Receipt.setBusinessId(businessId);
//                ReceiptStorage.saveOrUpdate(Receipt);
//            }
//        });
//        return null;
//    }

    public Receipt updateReceipt(Receipt Receipt) {
        return receiptStorage.saveOrUpdate(Receipt);
    }
    
    public boolean deleteReceipt(String id) {
        return receiptStorage.deleteReceipt(id);
    }


}

package sr.we.storage.impl;

import org.eclipse.store.integrations.spring.boot.types.concurrent.Read;
import org.eclipse.store.integrations.spring.boot.types.concurrent.Write;
import org.eclipse.store.storage.embedded.types.EmbeddedStorageManager;
import org.springframework.stereotype.Component;
import sr.we.entity.eclipsestore.tables.Receipt;
import sr.we.storage.IReceiptsStorage;

import java.util.List;

@Deprecated
@Component
public class ReceiptStorage extends EclipseStoreSuperService<Receipt> /*implements IReceiptsStorage*/ {

    public ReceiptStorage(EmbeddedStorageManager storageManager) {
        super(storageManager, Receipt.class);
    }

//    @Override
    @Read
    public Receipt oneReceipt(String uuId) {
        return get(uuId);
    }

//    @Override
    public Receipt oneReceiptNumber(String receiptNumber) {
        return stream().filter(receipt -> receipt.getReceipt_number().equalsIgnoreCase(receiptNumber)).findAny().orElse(null);
    }

//    @Override
    @Read
    public List<Receipt> allReceipts(Long businessId) {
        return stream().filter(store -> store.getBusinessId() != null && store.getBusinessId().compareTo(businessId) == 0).toList();
    }

//    @Override
    @Write
    public Receipt saveOrUpdate(Receipt receipt) {

        return update(receipt, f -> {
            f.setCancelled_at(receipt.getCancelled_at());
            f.setCreated_at(receipt.getCreated_at());
            f.setCustomer_id(receipt.getCustomer_id());
            f.setNote(receipt.getNote());
            f.setUpdated_at(receipt.getUpdated_at());
            f.setEmployee_id(receipt.getEmployee_id());
            f.setPayments(receipt.getPayments());
            f.setLine_items(receipt.getLine_items());
            f.setReceipt_date(receipt.getReceipt_date());
            f.setPoints_balance(receipt.getPoints_balance());
            f.setStore_id(receipt.getStore_id());
            f.setSource(receipt.getSource());
            f.setPoints_deducted(receipt.getPoints_deducted());
            f.setPoints_earned(receipt.getPoints_earned());
            f.setPos_device_id(receipt.getPos_device_id());
            f.setReceipt_type(receipt.getReceipt_type());
            f.setRefund_for(receipt.getRefund_for());
            f.setReceipt_number(receipt.getReceipt_number());
            f.setTotal_tax(receipt.getTotal_tax());
            f.setTotal_money(receipt.getTotal_money());
            f.setTotal_discount(receipt.getTotal_discount());
            f.setTip(receipt.getTip());
            f.setSurcharge(receipt.getSurcharge());
            f.setBusinessId(receipt.getBusinessId());
            f.setCategory_id(receipt.getCategory_id());
            f.setForm(receipt.getForm());
            f.setColor(receipt.getColor());
            return f;
        });
    }

//    @Override
    @Write
    public boolean deleteReceipt(String uuId) {
        return delete(uuId);
    }
}

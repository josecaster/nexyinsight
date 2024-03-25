package sr.we.storage;


import sr.we.entity.eclipsestore.tables.Receipt;

import java.util.List;

public interface IReceiptsStorage {

    /**
     * Returns a single store element
     * @param uuId
     * @return the store with the specified ID
     */
    Receipt oneReceipt(String uuId);

    /**
     * Returns a single store element
     * @param uuId
     * @return the store with the specified ID
     */
    Receipt oneReceiptNumber(String uuId);

    /**
     * Returns all Receipts in the storage
     * @param businessId
     * @return A list of all Receipts
     */
    List<Receipt> allReceipts(Long businessId);

    /**
     * Adds a Receipt record to the storage
     * @param Receipt
     * @return the added Receipt in Storage
     */
    Receipt saveOrUpdate(Receipt Receipt);

    /**
     * Delete the Receipt containing the given ID
     *
     * @param uuId
     * @return boolean
     */
    boolean deleteReceipt(String uuId);
}

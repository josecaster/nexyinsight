package sr.we.storage;


import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import sr.we.entity.eclipsestore.tables.Receipt;

import java.util.List;

public interface IReceiptsStorage extends MongoRepository<Receipt, String> {

    /**
     * Returns a single store element
     *
     * @param uuId
     * @return the store with the specified ID
     */
    @Query("{uuId:?0}")
    Receipt oneReceipt(String uuId);

    /**
     * Returns a single store element
     *
     * @param uuId
     * @return the store with the specified ID
     */
    @Query("{receipt_number:?0}")
    Receipt oneReceiptNumber(String uuId);

    /**
     * Returns all Receipts in the storage
     *
     * @param businessId
     * @return A list of all Receipts
     */
    @Query("{businessId:?0}")
    List<Receipt> allReceipts(Long businessId);

    /**
     * Adds a Receipt record to the storage
     *
     * @param Receipt
     * @return the added Receipt in Storage
     */
    default Receipt saveOrUpdate(Receipt Receipt) {
        return save(Receipt);
    }

    /**
     * Delete the Receipt containing the given ID
     *
     * @param uuId
     * @return boolean
     */
    default boolean deleteReceipt(String uuId) {
        deleteById(uuId);
        return true;
    }
}

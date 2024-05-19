package sr.we.storage;


import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import sr.we.entity.eclipsestore.tables.InventoryHistory;

import java.util.List;

public interface IInventoryHistoryStorage extends MongoRepository<InventoryHistory, String> {

    /**
     * Returns a single store element
     *
     * @param uuId
     * @return the store with the specified ID
     */
    @Query("{uuId:?0}")
    InventoryHistory oneInventoryHistory(String uuId);

    /**
     * Returns all InventoryHistorys in the storage
     *
     * @param businessId
     * @return A list of all InventoryHistorys
     */
    @Query("{businessId:?0}")
    List<InventoryHistory> allInventoryHistorys(Long businessId);

    /**
     * Adds a InventoryHistory record to the storage
     *
     * @param InventoryHistory
     * @return the added InventoryHistory in Storage
     */
    default InventoryHistory saveOrUpdate(InventoryHistory InventoryHistory) {
        return save(InventoryHistory);
    }

    /**
     * Delete the InventoryHistory containing the given ID
     *
     * @param uuId
     * @return boolean
     */
    default boolean deleteInventoryHistory(String uuId) {
        deleteById(uuId);
        return true;
    }
}

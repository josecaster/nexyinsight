package sr.we.storage;


import sr.we.entity.eclipsestore.tables.InventoryHistory;

import java.util.List;

public interface IInventoryHistoryStorage {

    /**
     * Returns a single store element
     * @param uuId
     * @return the store with the specified ID
     */
    InventoryHistory oneInventoryHistory(String uuId);

    /**
     * Returns all InventoryHistorys in the storage
     * @param businessId
     * @return A list of all InventoryHistorys
     */
    List<InventoryHistory> allInventoryHistorys(Long businessId);

    /**
     * Adds a InventoryHistory record to the storage
     * @param InventoryHistory
     * @return the added InventoryHistory in Storage
     */
    InventoryHistory saveOrUpdate(InventoryHistory InventoryHistory);

    /**
     * Delete the InventoryHistory containing the given ID
     *
     * @param uuId
     * @return boolean
     */
    boolean deleteInventoryHistory(String uuId);
}

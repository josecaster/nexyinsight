package sr.we.storage;


import sr.we.entity.eclipsestore.tables.Item;

import java.util.List;

public interface IItemStorage {

    /**
     * Returns a single Item element
     * @param uuId
     * @return the Item with the specified ID
     */
    Item oneItem(String uuId);

    /**
     * Returns all Items in the storage
     * @param businessId
     * @return A list of all Items
     */
    List<Item> allItems(Long businessId);

    /**
     * Adds a Item record to the storage
     * @param Item
     * @return the added Item in Storage
     */
    Item saveOrUpdate(Item Item);

    /**
     * Delete the Item containing the given ID
     *
     * @param uuId
     * @return boolean
     */
    boolean deleteItem(String uuId);

    Item oneItemByLoyId(String id);
}

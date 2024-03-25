package sr.we.storage;


import sr.we.entity.eclipsestore.tables.Category;

import java.util.List;

public interface ICategoryStorage {

    /**
     * Returns a single store element
     * @param uuId
     * @return the store with the specified ID
     */
    Category oneStore(String uuId);

    /**
     * Returns all stores in the storage
     * @param businessId
     * @return A list of all stores
     */
    List<Category> allStores(Long businessId);

    /**
     * Adds a store record to the storage
     * @param Category
     * @return the added store in Storage
     */
    Category saveOrUpdate(Category Category);

    /**
     * Delete the store containing the given ID
     *
     * @param uuId
     * @return boolean
     */
    boolean deleteStore(String uuId);
}

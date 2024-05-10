package sr.we.storage;


import sr.we.entity.eclipsestore.tables.Section;

import java.util.List;
import java.util.Optional;

public interface IStoreStorage {

    /**
     * Returns a single store element
     * @param uuId
     * @return the store with the specified ID
     */
    Section oneStore(String uuId);

    /**
     * Returns all stores in the storage
     * @param businessId
     * @return A list of all stores
     */
    List<Section> allStores(Long businessId);

    /**
     * Adds a store record to the storage
     * @param section
     * @return the added store in Storage
     */
    Section saveOrUpdate(Section section);

    /**
     * Delete the store containing the given ID
     *
     * @param uuId
     * @return boolean
     */
    boolean deleteStore(String uuId);

    Optional<Section> findSection(Long businessId, String storeId, String variantId, String posDeviceId, Section.Color color, Section.Form form, boolean door);
}

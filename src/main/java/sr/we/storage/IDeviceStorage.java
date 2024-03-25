package sr.we.storage;


import sr.we.entity.eclipsestore.tables.Device;

import java.util.List;

public interface IDeviceStorage {

    /**
     * Returns a single store element
     * @param uuId
     * @return the store with the specified ID
     */
    Device oneStore(String uuId);

    /**
     * Returns all stores in the storage
     * @param businessId
     * @return A list of all stores
     */
    List<Device> allStores(Long businessId);

    /**
     * Adds a store record to the storage
     * @param Device
     * @return the added store in Storage
     */
    Device saveOrUpdate(Device Device);

    /**
     * Delete the store containing the given ID
     *
     * @param uuId
     * @return boolean
     */
    boolean deleteStore(String uuId);
}

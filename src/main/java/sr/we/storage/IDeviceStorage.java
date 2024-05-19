package sr.we.storage;


import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import sr.we.entity.eclipsestore.tables.Device;

import java.util.List;

public interface IDeviceStorage extends MongoRepository<Device,String> {

    /**
     * Returns a single store element
     * @param uuId
     * @return the store with the specified ID
     */
    @Query("{uuId:?0}")
    Device oneStore(String uuId);

    /**
     * Returns all stores in the storage
     * @param businessId
     * @return A list of all stores
     */
    @Query("{businessId:?0}")
    List<Device> allStores(Long businessId);

    /**
     * Adds a store record to the storage
     * @param Device
     * @return the added store in Storage
     */
    default Device saveOrUpdate(Device Device){
        return save(Device);
    }

    /**
     * Delete the store containing the given ID
     *
     * @param uuId
     * @return boolean
     */
    default boolean deleteStore(String uuId){
        deleteById(uuId);
        return true;
    }
}

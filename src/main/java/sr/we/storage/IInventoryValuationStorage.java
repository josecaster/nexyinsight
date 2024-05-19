package sr.we.storage;


import org.eclipse.store.integrations.spring.boot.types.concurrent.Read;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import sr.we.entity.eclipsestore.tables.InventoryValuation;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

public interface IInventoryValuationStorage extends MongoRepository<InventoryValuation, String> {

    /**
     * Returns a single store element
     *
     * @param uuId
     * @return the store with the specified ID
     */
    @Query("{uuId:?0}")
    InventoryValuation oneInventoryValuation(String uuId);

    /**
     * Returns all InventoryValuations in the storage
     *
     * @param businessId
     * @return A list of all InventoryValuations
     */
    @Query("{businessId:?0}")
    List<InventoryValuation> allInventoryValuations(Long businessId);

    //    @Read
    @Query("{businessId:?0,localDate:?1}")
    Optional<InventoryValuation> getInventoryValuation(Long businessId, LocalDate localDate);

    /**
     * Adds a InventoryValuation record to the storage
     *
     * @param InventoryValuation
     * @return the added InventoryValuation in Storage
     */
    default InventoryValuation saveOrUpdate(InventoryValuation InventoryValuation) {
        return save(InventoryValuation);
    }

    /**
     * Delete the InventoryValuation containing the given ID
     *
     * @param uuId
     * @return boolean
     */
    default boolean deleteInventoryValuation(String uuId) {
        deleteById(uuId);
        return true;
    }
}

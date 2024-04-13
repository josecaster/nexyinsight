package sr.we.storage;


import org.eclipse.store.integrations.spring.boot.types.concurrent.Read;
import sr.we.entity.eclipsestore.tables.InventoryValuation;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

public interface IInventoryValuationStorage {

    /**
     * Returns a single store element
     *
     * @param uuId
     * @return the store with the specified ID
     */
    InventoryValuation oneInventoryValuation(String uuId);

    /**
     * Returns all InventoryValuations in the storage
     *
     * @param businessId
     * @return A list of all InventoryValuations
     */
    List<InventoryValuation> allInventoryValuations(Long businessId);

    @Read
    Optional<InventoryValuation> getInventoryValuation(Long businessId, LocalDate localDate);

    /**
     * Adds a InventoryValuation record to the storage
     *
     * @param InventoryValuation
     * @return the added InventoryValuation in Storage
     */
    InventoryValuation saveOrUpdate(InventoryValuation InventoryValuation);

    /**
     * Delete the InventoryValuation containing the given ID
     *
     * @param uuId
     * @return boolean
     */
    boolean deleteInventoryValuation(String uuId);

    @Read
    Stream<InventoryValuation> allInventoryValuations(Long businessId, Integer page, Integer pageSize, Predicate<? super InventoryValuation> predicate);
}

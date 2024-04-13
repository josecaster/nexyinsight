package sr.we.storage.impl;

import org.eclipse.store.integrations.spring.boot.types.concurrent.Read;
import org.eclipse.store.integrations.spring.boot.types.concurrent.Write;
import org.eclipse.store.storage.embedded.types.EmbeddedStorageManager;
import org.springframework.stereotype.Component;
import sr.we.entity.eclipsestore.tables.InventoryValuation;
import sr.we.storage.IInventoryValuationStorage;
import sr.we.storage.impl.EclipseStoreSuperService;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Component
public class InventoryValuationStorage extends EclipseStoreSuperService<InventoryValuation> implements IInventoryValuationStorage {

    public InventoryValuationStorage(EmbeddedStorageManager storageManager) {
        super(storageManager, InventoryValuation.class);
    }

    @Override
    @Read
    public InventoryValuation oneInventoryValuation(String uuId) {
        return get(uuId);
    }

    @Override
    @Read
    public List<InventoryValuation> allInventoryValuations(Long businessId) {
        return stream().filter(store -> store.getBusinessId() != null && store.getBusinessId().compareTo(businessId) == 0).toList();
    }

    @Override
    @Read
    public Optional<InventoryValuation> getInventoryValuation(Long businessId, LocalDate localDate) {
        return stream().filter(store -> store.getBusinessId() != null && store.getBusinessId().compareTo(businessId) == 0 && store.getLocalDate().isEqual(localDate)).findAny();
    }

    @Override
    @Write
    public InventoryValuation saveOrUpdate(InventoryValuation InventoryValuation) {

        return update(InventoryValuation, f -> {
            f.setBusinessId(InventoryValuation.getBusinessId());
            f.setType(InventoryValuation.getType());
            f.setInventoryValue(InventoryValuation.getInventoryValue());
            f.setMargin(InventoryValuation.getMargin());
            f.setPotentialProfit(InventoryValuation.getPotentialProfit());
            f.setRetailValue(InventoryValuation.getRetailValue());
            f.setLocalDate(InventoryValuation.getLocalDate());
            return f;
        });
    }

    @Override
    @Write
    public boolean deleteInventoryValuation(String uuId) {
        return delete(uuId);
    }

    @Override
    @Read
    public Stream<InventoryValuation> allInventoryValuations(Long businessId, Integer page, Integer pageSize, Predicate<? super InventoryValuation> predicate) {
        return (page == null || pageSize == null) ? (predicate != null ? allInventoryValuations(businessId).stream().filter(predicate) : allInventoryValuations(businessId).stream()) : (predicate != null ? allInventoryValuations(businessId).stream().filter(predicate).sorted(Comparator.comparing(InventoryValuation::getLocalDate).reversed()).skip((long) page * pageSize).limit(pageSize) : allInventoryValuations(businessId).stream().sorted(Comparator.comparing(InventoryValuation::getLocalDate).reversed()).skip((long) page * pageSize).limit(pageSize));
    }
}

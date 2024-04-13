package sr.we.controllers;

import org.springframework.stereotype.Controller;
import sr.we.entity.eclipsestore.tables.InventoryValuation;
import sr.we.integration.Parent;
import sr.we.storage.IInventoryValuationStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;


@Controller
public class InventoryValuationController extends Parent {

    private final IInventoryValuationStorage inventoryValuationStorage;

    public InventoryValuationController(IInventoryValuationStorage inventoryValuationStorage) {
        this.inventoryValuationStorage = inventoryValuationStorage;
    }

    public InventoryValuation oneInventoryValuation(String uuId) {
        return inventoryValuationStorage.oneInventoryValuation(uuId);
    }

    public List<InventoryValuation> allInventoryValuations(Long businessId) {
        return inventoryValuationStorage.allInventoryValuations(businessId);
    }

    public Optional<InventoryValuation> getInventoryValuation(Long businessId, LocalDate localDate) {
        return inventoryValuationStorage.getInventoryValuation(businessId, localDate);
    }

    public InventoryValuation saveOrUpdate(InventoryValuation InventoryValuation) {
        return inventoryValuationStorage.saveOrUpdate(InventoryValuation);
    }

    public boolean deleteInventoryValuation(String uuId) {
        return inventoryValuationStorage.deleteInventoryValuation(uuId);
    }

    public Stream<InventoryValuation> allInventoryValuations(Long businessId, Integer page, Integer pageSize, Predicate<? super InventoryValuation> predicate) {
        return inventoryValuationStorage.allInventoryValuations(businessId, page, pageSize, predicate);
    }

}

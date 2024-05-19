package sr.we.controllers;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Controller;
import sr.we.entity.eclipsestore.tables.InventoryValuation;
import sr.we.storage.IInventoryValuationStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;


@Controller
public class InventoryValuationController {

    private final IInventoryValuationStorage inventoryValuationStorage;
    private final MongoTemplate mongoTemplate;

    /**
     * @param inventoryValuationStorage {@link IInventoryValuationStorage}
     * @param mongoTemplate             {@link MongoTemplate}
     */
    public InventoryValuationController(IInventoryValuationStorage inventoryValuationStorage, MongoTemplate mongoTemplate) {
        this.inventoryValuationStorage = inventoryValuationStorage;
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * Get Inventory Valuation
     *
     * @param uuId Provide the Database Primary Key
     * @return InventoryValuation
     */
    public InventoryValuation oneInventoryValuation(String uuId) {
        return inventoryValuationStorage.oneInventoryValuation(uuId);
    }

    /**
     * Get InventoryValuation List
     *
     * @param businessId Provide business ID
     * @return List of InventoryValuation
     */
    public List<InventoryValuation> allInventoryValuations(Long businessId) {
        return inventoryValuationStorage.allInventoryValuations(businessId);
    }

    /**
     * Get Inventory Valuation based on given date
     *
     * @param businessId provide Business ID
     * @param localDate  provide date
     * @return returns an optional for Inventory Valuation
     */
    public Optional<InventoryValuation> getInventoryValuation(Long businessId, LocalDate localDate) {
        return inventoryValuationStorage.getInventoryValuation(businessId, localDate);
    }

    /**
     * Save or update
     *
     * @param InventoryValuation Provide Valuation to save
     * @return saved result
     */
    public InventoryValuation saveOrUpdate(InventoryValuation InventoryValuation) {
        return inventoryValuationStorage.saveOrUpdate(InventoryValuation);
    }

//    /**
//     * Delete IV based on provided primary key
//     * @param uuId provide Database primary Key
//     * @return delete status
//     */
//    public boolean deleteInventoryValuation(String uuId) {
//        return inventoryValuationStorage.deleteInventoryValuation(uuId);
//    }

    /**
     * Stream value for list of Inventory valuations
     *
     * @param businessId Provide business Id
     * @param page       provide pagination page
     * @param pageSize   provide pagination page size
     * @return stream inventory valuations
     */
    public Stream<InventoryValuation> allInventoryValuations(Long businessId, Integer page, Integer pageSize) {


        Query query = new Query();
        query.addCriteria(Criteria.where("businessId").is(businessId));
        if (page != null && pageSize != null) {
            query.with(PageRequest.of(page, pageSize));
        }
        query.addCriteria(new Criteria()).with(Sort.by(Sort.Direction.DESC, "localDate"));
        return mongoTemplate.find(query, InventoryValuation.class).stream();
    }

}

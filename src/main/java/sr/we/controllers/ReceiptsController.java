package sr.we.controllers;

import org.bson.Document;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.MongoExpression;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Controller;
import sr.we.entity.eclipsestore.tables.Receipt;
import sr.we.entity.eclipsestore.tables.Section;
import sr.we.storage.IReceiptsStorage;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;
import static org.springframework.data.mongodb.core.aggregation.Fields.fields;


@Controller
public class ReceiptsController {

    private final IReceiptsStorage receiptStorage;
    private final StoresController storesController;
    private final MongoTemplate mongoTemplate;

    /**
     * @param receiptStorage   {@link IReceiptsStorage}
     * @param storesController {@link StoresController}
     * @param mongoTemplate    {@link MongoTemplate}
     */
    public ReceiptsController(IReceiptsStorage receiptStorage, StoresController storesController, MongoTemplate mongoTemplate) {
        this.receiptStorage = receiptStorage;
        this.storesController = storesController;
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * @param businessId provide Business Id
     * @param page       provide page for pagination
     * @param pageSize   provide page size for pagination
     * @param sections   provide section for further filtering
     * @param predicate  provide query criteria for further filtering
     * @return Stream Receipt
     */
    public Stream<Receipt> allReceipts(Long businessId, Integer page, Integer pageSize, Set<String> sections, Collection<Criteria> predicate) {
        Query query = new Query();
        query.addCriteria(Criteria.where("businessId").is(businessId));
        Criteria criteria1 = new Criteria();

        List<Criteria> ors = new ArrayList<>();

        for (String n : sections) {
            Section l = storesController.oneStore(n);
            List<Criteria> criteria = ItemsController.linkSection(l, false);
            Criteria criteria2 = new Criteria().andOperator(criteria);
            ors.add(criteria2);
        }
        criteria1.orOperator(ors);

        query.with(PageRequest.of(page, pageSize));
        if (predicate != null) {
            for (Criteria criteria : predicate) {
                query.addCriteria(criteria);
            }
        }
        query.addCriteria(criteria1).with(Sort.by(Sort.Direction.DESC, "receipt_date"));
        return mongoTemplate.find(query, Receipt.class).stream();

    }


    /**
     * @param businessId provide Business Id
     * @param start      provide start date for filtering
     * @param end        provide end date for filtering
     * @param sections   provide section ids for further filtering
     * @return list of receipts
     */
    public List<Receipt> receipts(Long businessId, LocalDate start, LocalDate end, Set<String> sections) {
        Query query = new Query();


        for (String n : sections) {
            Section l = storesController.oneStore(n);
            List<Criteria> criteria = ItemsController.linkSection(l, false);
            query.addCriteria(new Criteria().andOperator(criteria));
        }

        LocalDateTime starting = LocalDateTime.of(start, LocalTime.MIN);
        LocalDateTime ending = LocalDateTime.of(end, LocalTime.MAX);

        query.addCriteria(Criteria.where("businessId").is(businessId));
        query.addCriteria(Criteria.where("receipt_date").gte(starting).andOperator(Criteria.where("receipt_date").lte(ending)));

        return mongoTemplate.find(query, Receipt.class);
    }

    /**
     * @param businessId Provide Business Id
     * @param start      provide start date for filtering
     * @param end        provide end date for filtering
     * @return list of receipts
     */
    public List<Receipt> receipts(Long businessId, LocalDate start, LocalDate end) {

        LocalDateTime starting = LocalDateTime.of(start, LocalTime.MIN);
        LocalDateTime ending = LocalDateTime.of(end, LocalTime.MAX);

        Query query = new Query();
        query.addCriteria(Criteria.where("businessId").is(businessId));
        query.addCriteria(Criteria.where("receipt_date").gte(starting).andOperator(Criteria.where("receipt_date").lte(ending)));
        return mongoTemplate.find(query, Receipt.class);
    }

    /**
     * @param businessId provide Business Id
     * @param start      provide start date
     * @param end        provide end date
     * @param sectionIds provide list of section ids
     * @return Discount Value
     */
    public Value discount(Long businessId, LocalDate start, LocalDate end, Set<String> sectionIds) {
        return sum(businessId, start, end, sectionIds, "SALE", "total_discount");
    }

    /**
     * @param businessId provide Business Id
     * @param start      provide start date
     * @param end        provide end date
     * @param sectionIds provide list of section ids
     * @return Gross Value
     */
    public Value gross(Long businessId, LocalDate start, LocalDate end, Set<String> sectionIds) {
        return sum(businessId, start, end, sectionIds, "SALE", "gross_total_money");
    }

//    /**
//     * @param businessId provide Business Id
//     * @param start      provide start date
//     * @param end        provide end date
//     * @param sectionIds provide list of section ids
//     * @return Gross Sections Value
//     */
//    public List<ValueSection> grossSection(Long businessId, LocalDate start, LocalDate end, Set<String> sectionIds) {
//        LocalDateTime starting = LocalDateTime.of(start, LocalTime.MIN);
//        LocalDateTime ending = LocalDateTime.of(end, LocalTime.MAX);
//
//        List<Criteria> criterias = new ArrayList<>();
//        List<Criteria> orOperator = orOperators(businessId, sectionIds);
//        criterias.add(new Criteria().orOperator(orOperator));
//
//
//        MatchOperation filterStates = match(//
//                Criteria.where("receipt_type").is("SALE")//
//                        .andOperator(Criteria.where("businessId").is(businessId).andOperator(Criteria.where("receipt_date").gte(starting)//
//                                .andOperator(Criteria.where("receipt_date").lte(ending).andOperator(criterias))))//
//        );
//
//        // Define the aggregation operations
//        AggregationOperation groupOperation = Aggregation.group("store_id","category_id","form","color","line_item.gross_total_money").sum(AggregationExpression.from(MongoExpression.create("$toDouble: '$line_item.gross_total_money'"))).as("get");
//
//        AggregationOperation projectOperation = project("store_id","category_id","form","color","get").andExclude("_id");
//
//        // Combine operations into an aggregation pipeline
//        Aggregation aggregation = newAggregation(filterStates, groupOperation, projectOperation);
//
//        // Execute the aggregation
//        AggregationResults<ValueSection> results = mongoTemplate.aggregate(aggregation, "receipt", ValueSection.class);
//        return results.getMappedResults();
//    }


    /**
     * @param businessId provide Business Id
     * @param start      provide start date
     * @param end        provide end date
     * @param sectionIds provide list of section ids
     * @return Refund Value
     */
    public Value refund(Long businessId, LocalDate start, LocalDate end, Set<String> sectionIds) {
        return sum(businessId, start, end, sectionIds, "REFUND", "gross_total_money");
    }

    /**
     * @param businessId Provide Business Id
     * @param year       provide Year
     * @param sectionIds provide List of section Ids
     * @return Bar results
     */
    public List<Bar> chart(Long businessId, Long year, Set<String> sectionIds) {

        Criteria businessId1 = Criteria.where("businessId").is(businessId);
        Criteria cancelledAt = Criteria.where("cancelled_at").isNull();


        List<Criteria> criteriaList = new ArrayList<>();
        criteriaList.add(new Criteria().andOperator(businessId1, cancelledAt));
        List<Criteria> orOperator = orOperators(businessId, sectionIds);
        criteriaList.add(new Criteria().orOperator(orOperator));


        Criteria andCriteria = Criteria.where("receipt_type").is("SALE")//
                .andOperator(criteriaList);


        MatchOperation filterStates = match(//
                andCriteria//
        );


        AggregationOperation projectionOperation = project().andExpression("year(receipt_date)").as("year").andExpression("month(receipt_date)").as("month").and("category_id").as("category").and("form").as("form").and("color").as("color").and("line_item.gross_total_money").as("get").andExclude("_id");

        AggregationOperation groupOperation = Aggregation.group(fields().and("year").and("month").and("category").and("form").and("color")).sum(f -> new Document("$toDouble", "$get")).as("get");


        Aggregation aggregation = newAggregation(filterStates, projectionOperation, groupOperation, Aggregation.project().andExclude("_id").andInclude("year", "month", "category", "form", "color", "get"));

        // Execute the aggregation
        AggregationResults<Bar> results = mongoTemplate.aggregate(aggregation, "receipt", Bar.class);

        return results.getMappedResults();
    }

    /**
     * Receipt count
     *
     * @param businessId provide Business Id
     * @param start      provide start date
     * @param end        provide end date
     * @param sectionIds provide list of section ids
     * @return Count Value
     */
    public Value count(Long businessId, LocalDate start, LocalDate end, Set<String> sectionIds) {
        return countInner(businessId, start, end, sectionIds);
    }

    private Value countInner(Long businessId, LocalDate start, LocalDate end, Set<String> sectionIds) {


        LocalDateTime starting = LocalDateTime.of(start, LocalTime.MIN);
        LocalDateTime ending = LocalDateTime.of(end, LocalTime.MAX);

        List<Criteria> criterias = new ArrayList<>();
        List<Criteria> orOperator = orOperators(businessId, sectionIds);
        criterias.add(new Criteria().orOperator(orOperator));


        MatchOperation filterStates = match(//
                Criteria.where("receipt_type").is("SALE")//
                        .andOperator(Criteria.where("businessId").is(businessId).andOperator(Criteria.where("receipt_date").gte(starting)//
                                .andOperator(Criteria.where("receipt_date").lte(ending).andOperator(criterias))))//
        );

        ProjectionOperation projectionOperation = project("receipt_number").and(StringOperators.valueOf("receipt_number").regexFind("^([^-]+-[^-]+)")).as("receipt_number_prefix");

        GroupOperation groupOperation = group("receipt_number_prefix").count().as("get");
        ProjectionOperation projectionOperation1 = project("get").andExclude("_id");
        GroupOperation groupOperation1 = group().count().as("get");

        // Combine operations into an aggregation pipeline
        Aggregation aggregation = newAggregation(filterStates, projectionOperation, groupOperation, projectionOperation1, groupOperation1);

        // Execute the aggregation
        AggregationResults<Value> results = mongoTemplate.aggregate(aggregation, "receipt", Value.class);


        Value uniqueMappedResult = results.getUniqueMappedResult();
        if (uniqueMappedResult == null || uniqueMappedResult.get() == null) {
            return new Value(0d);
        }
        return uniqueMappedResult;
    }

    private List<Criteria> orOperators(Long businessId, Set<String> sectionIds) {
        List<Criteria> orOperator = new ArrayList<>();
        for (String n : sectionIds) {
            Section l = storesController.oneStore(n);
//            if (!l.isDefault()) {
                List<Criteria> criteria1 = ItemsController.linkSection(l, false);
                orOperator.add(new Criteria().andOperator(criteria1.toArray(new Criteria[0])));
//            }
        }
        return orOperator;
    }

    private Value sum(Long businessId, LocalDate start, LocalDate end, Set<String> sectionIds, String receiptType, String field) {


        LocalDateTime starting = LocalDateTime.of(start, LocalTime.MIN);
        LocalDateTime ending = LocalDateTime.of(end, LocalTime.MAX);

        List<Criteria> criterias = new ArrayList<>();
        List<Criteria> orOperator = orOperators(businessId, sectionIds);
        criterias.add(new Criteria().orOperator(orOperator));


        MatchOperation filterStates = match(//
                Criteria.where("receipt_type").is(receiptType)//
                        .andOperator(Criteria.where("businessId").is(businessId).andOperator(Criteria.where("receipt_date").gte(starting)//
                                .andOperator(Criteria.where("receipt_date").lte(ending).andOperator(criterias))))//
        );

        // Define the aggregation operations
        AggregationOperation groupOperation = Aggregation.group().sum(AggregationExpression.from(MongoExpression.create("$toDouble: '$line_item." + field + "'"))).as("get");

        AggregationOperation projectOperation = project("get").andExclude("_id");

        // Combine operations into an aggregation pipeline
        Aggregation aggregation = newAggregation(filterStates, groupOperation, projectOperation);

        // Execute the aggregation
        AggregationResults<Value> results = mongoTemplate.aggregate(aggregation, "receipt", Value.class);


        Value uniqueMappedResult = results.getUniqueMappedResult();
        if (uniqueMappedResult == null || uniqueMappedResult.get() == null) {
            return new Value(0d);
        }
        return uniqueMappedResult;
    }

    public record Bar(Long year, Long month, String category, String form, String color, BigDecimal get) {

    }

    public record ValueSection(Double get, String store_id, String category_id, String form, String color) {

    }

    public record Value(Double get) {

    }
}

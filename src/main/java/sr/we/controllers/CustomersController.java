package sr.we.controllers;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Controller;
import sr.we.entity.Role;
import sr.we.entity.User;
import sr.we.entity.eclipsestore.tables.Customer;
import sr.we.entity.eclipsestore.tables.Section;
import sr.we.integration.LoyCustomersController;
import sr.we.storage.ICustomerStorage;

import java.util.*;
import java.util.stream.Stream;


@Controller
public class CustomersController {


    private final ICustomerStorage customerStorage;


    private final LoyCustomersController loycustomersController;

    private final StoresController storesController;

    private final MongoTemplate mongoTemplate;

    /**
     * @param customerStorage        {@link ICustomerStorage}
     * @param loycustomersController {@link LoyCustomersController}
     * @param storesController   {@link StoresController}
     * @param mongoTemplate      {@link MongoTemplate}
     */
    public CustomersController(ICustomerStorage customerStorage, LoyCustomersController loycustomersController, StoresController storesController, MongoTemplate mongoTemplate) {
        this.customerStorage = customerStorage;
        this.loycustomersController = loycustomersController;
        this.storesController = storesController;
        this.mongoTemplate = mongoTemplate;
    }

    public static boolean linkSection(String section, String category, String form, String color, Section l) {
        boolean cont = l.getId().equalsIgnoreCase(section);
        if (cont && l.getCategories() != null && !l.getCategories().isEmpty()) {
            cont = l.getCategories().contains(category);
        }
        if (cont && l.getForm() != null && StringUtils.isNotBlank(form)) {
            cont = l.getForm().compareTo(Section.Form.valueOf(form)) == 0;
        }
        if (cont && l.getColor() != null && StringUtils.isNotBlank(color)) {
            cont = l.getColor().compareTo(Section.Color.valueOf(color)) == 0;
        }
        return cont;
    }

    /**
     * Get criterias to link customer to section
     *
     * @param section provide a Section
     * @return List of query criteria 's
     */
    public static List<Criteria> linkSection(Section section, boolean customer) {
        List<Criteria> criteriaList = new ArrayList<>();
        Criteria e = Criteria.where(!customer ? "store_id" : "variantStore.store_id").is(section.getId());
        if (section.isDefault()) {
            e = e.andOperator(Criteria.where("category_id").isNull());
        } else {
            if (section.getCategories() != null && !section.getCategories().isEmpty()) {
                e = e.andOperator(Criteria.where("category_id").in(section.getCategories()));
            }
            if (section.getForm() != null) {
                e.andOperator(Criteria.where("form").is(section.getForm()));
            }
            if (section.getColor() != null) {
                e.andOperator(Criteria.where("color").is(section.getColor()));
            }
        }
        criteriaList.add(e);
        return criteriaList;
    }

    /**
     * @param id provide database primary key
     * @return Customer
     */
    public Customer onecustomer(String id) {
        return customerStorage.oneCustomer(id);
    }

    /**
     * @param businessId provide Business Id
     * @param page       provide pagination page
     * @param pageSize   provide pagination page size
     * @param sections   provide sectionIds for further validation
     * @param predicate  provide collection of Criterias to further filter data
     * @return Stream of Customer
     */
    public Stream<Customer> allcustomers(Long businessId, Integer page, Integer pageSize, Set<String> sections, Collection<Criteria> predicate) {

        Query query = new Query();
        query.addCriteria(Criteria.where("businessId").is(businessId));
        if (page != null && pageSize != null) {
            query.with(PageRequest.of(page, pageSize));
        }

        List<Criteria> ors = new ArrayList<>();

        for (String n : sections) {
            Section l = storesController.oneStore(n);
            List<Criteria> criteria = CustomersController.linkSection(l, true);
            Criteria criteria2 = new Criteria().andOperator(criteria);
            ors.add(criteria2);
        }
        Criteria criteria1 = new Criteria();
        if(!ors.isEmpty()) {
            criteria1.orOperator(ors);
        } else {
            return Stream.empty();
        }

        if (predicate != null) {
            for (Criteria criteria : predicate) {
                query.addCriteria(criteria);
            }
        }
        query.addCriteria(criteria1).with(Sort.by(Sort.Direction.ASC, "name"));
        return mongoTemplate.find(query, Customer.class).stream();
    }

    /**
     * @param businessId provide Business Id
     * @param page       provide pagination page
     * @param pageSize   provide pagination page size
     * @param sections   provide sectionIds for further validation
     * @param filter     provide filter string to query some base fields
     * @return Stream of Customer
     */
    public Stream<Customer> allcustomers(Long businessId, Integer page, Integer pageSize, Set<String> sections, Optional<String> filter) {




        Query query = new Query();
        query.addCriteria(Criteria.where("businessId").is(businessId));
        if (page != null && pageSize != null) {
            query.with(PageRequest.of(page, pageSize));
        }

        Criteria criteria = new Criteria();

        List<Criteria> orSectionCriteria = new ArrayList<>();

        for (String n : sections) {
            if(StringUtils.isBlank(n)){
                continue;
            }
            Section l = storesController.oneStore(n);
            if(l == null){
                continue;
            }
            List<Criteria> criterias = CustomersController.linkSection(l, true);
            Criteria criteria2 = new Criteria().andOperator(criterias);
            orSectionCriteria.add(criteria2);
        }

//        query.addCriteria(new Criteria().orOperator(orSectionCriteria.toArray(new Criteria[0])));

        // Combine section OR conditions
        Criteria finalCriteria = new Criteria();
        if (!orSectionCriteria.isEmpty()) {
            finalCriteria = finalCriteria.orOperator(orSectionCriteria.toArray(new Criteria[0]));
        } else {
            return Stream.empty();
        }

        if (filter.isPresent()) {
            List<Criteria> orFilterCriteria = new ArrayList<>();
            String s = filter.get().toUpperCase();
            if (StringUtils.isNotBlank(s)) {
                orFilterCriteria.add(Criteria.where("name").regex(s, "i"));
                orFilterCriteria.add(Criteria.where("phone_number").regex(s, "i"));
//                orFilterCriteria.add(Criteria.where("variant.barcode").regex(s, "i"));
//                orFilterCriteria.add(Criteria.where("description").regex(s, "i"));
                // Add filter criteria to the query
                finalCriteria = finalCriteria.andOperator(new Criteria().orOperator(orFilterCriteria.toArray(new Criteria[0])));
            }
        }

        // Add final combined criteria to the query
        query.addCriteria(finalCriteria);


        query.with(Sort.by(Sort.Direction.ASC, "name"));
        return mongoTemplate.find(query, Customer.class).stream();
    }

    /**
     * @param businessId Provide Business Id
     * @param page       provide pagination page
     * @param pageSize   provide pagination page size
     * @param user       provide user for further querying
     * @param filter     provide filter string to query some base fields
     * @return Stream of Customer
     */
    public Stream<Customer> allcustomers(Long businessId, Integer page, Integer pageSize, User user, Optional<String> filter) {

//        List<Section> sections = storesController.allStores(businessId);
//        if (user != null && !user.getRoles().contains(Role.ADMIN)) {
//            if (user.getLinkSections() != null) {
//                sections = sections.stream().filter(f -> user.getLinkSections().contains(f.getUuId())).toList();
//            }
//        }


        Query query = new Query();
        query.addCriteria(Criteria.where("businessId").is(businessId));
        if (page != null && pageSize != null) {
            query.with(PageRequest.of(page, pageSize));
        }

        Criteria criteria = new Criteria();

        List<Criteria> orSectionCriteria = new ArrayList<>();

//        for (Section l : sections) {
////            if(!l.isDefault()) {
//            List<Criteria> sectionCriteria = CustomersController.linkSection(l, true);
//            if (!sectionCriteria.isEmpty()) {
//                orSectionCriteria.add(new Criteria().andOperator(sectionCriteria.toArray(new Criteria[0])));
//            }
////            }
//        }

//        query.addCriteria(new Criteria().orOperator(orSectionCriteria.toArray(new Criteria[0])));

        // Combine section OR conditions
        Criteria finalCriteria = new Criteria();
        if (!orSectionCriteria.isEmpty()) {
            finalCriteria = finalCriteria.orOperator(orSectionCriteria.toArray(new Criteria[0]));
        }

        if (filter.isPresent()) {
            List<Criteria> orFilterCriteria = new ArrayList<>();
            String s = filter.get().toUpperCase();
            if (StringUtils.isNotBlank(s)) {
                orFilterCriteria.add(Criteria.where("name").regex(s, "i"));
                orFilterCriteria.add(Criteria.where("phone_number").regex(s, "i"));
//                orFilterCriteria.add(Criteria.where("variant.barcode").regex(s, "i"));
//                orFilterCriteria.add(Criteria.where("description").regex(s, "i"));
                // Add filter criteria to the query
                finalCriteria = finalCriteria.andOperator(new Criteria().orOperator(orFilterCriteria.toArray(new Criteria[0])));
            }
        }

        // Add final combined criteria to the query
        query.addCriteria(finalCriteria);


        query.with(Sort.by(Sort.Direction.ASC, "name"));
        return mongoTemplate.find(query, Customer.class).stream();

    }


//    public String sku(String sku, String sectionId) {
//        Query query = new Query();
//        query.addCriteria(Criteria.where("businessId").is(0L));
//
//        Criteria criteria = new Criteria();
//
//        List<Criteria> orSectionCriteria = new ArrayList<>();
//
//        Section l = storesController.oneStore(sectionId);
//        List<Criteria> sectionCriteria = CustomersController.linkSection(l, true);
//        if (!sectionCriteria.isEmpty()) {
//            orSectionCriteria.add(new Criteria().andOperator(sectionCriteria.toArray(new Criteria[0])));
//        }
//
//        // Combine section OR conditions
//        Criteria finalCriteria = new Criteria();
//        if (!orSectionCriteria.isEmpty()) {
//            finalCriteria = finalCriteria.orOperator(orSectionCriteria.toArray(new Criteria[0]));
//        }
//        List<Criteria> orFilterCriteria = new ArrayList<>();
//        orFilterCriteria.add(Criteria.where("name").is(sku));
//        // Add filter criteria to the query
//        finalCriteria = finalCriteria.andOperator(new Criteria().orOperator(orFilterCriteria.toArray(new Criteria[0])));
//
//        // Add final combined criteria to the query
//        query.addCriteria(finalCriteria);
//
//
//        List<Customer> customers = mongoTemplate.find(query, Customer.class);
//        if(customers.size() != 1){
//            return null;
//        } else {
//            return customers.get(0).getId();
//        }
//    }

    public Customer id(String id) {
        Query query = new Query();
        query.addCriteria(Criteria.where("businessId").is(0L));

        Criteria criteria = new Criteria();

//        List<Criteria> orSectionCriteria = new ArrayList<>();
//
//        Section l = storesController.oneStore(sectionId);
//        List<Criteria> sectionCriteria = customersController.linkSection(l, true);
//        if (!sectionCriteria.isEmpty()) {
//            orSectionCriteria.add(new Criteria().andOperator(sectionCriteria.toArray(new Criteria[0])));
//        }

        // Combine section OR conditions
        Criteria finalCriteria = new Criteria();
        List<Criteria> orFilterCriteria = new ArrayList<>();
        orFilterCriteria.add(Criteria.where("id").is(id));
        // Add filter criteria to the query
        finalCriteria = finalCriteria.andOperator(new Criteria().orOperator(orFilterCriteria.toArray(new Criteria[0])));

        // Add final combined criteria to the query
        query.addCriteria(finalCriteria);


        List<Customer> customers = mongoTemplate.find(query, Customer.class);
        if(customers.size() != 1){
            return null;
        } else {
            return customers.get(0);
        }
    }
}

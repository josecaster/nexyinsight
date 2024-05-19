package sr.we.controllers;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Controller;
import sr.we.entity.Role;
import sr.we.entity.User;
import sr.we.entity.eclipsestore.tables.Category;
import sr.we.entity.eclipsestore.tables.Section;
import sr.we.storage.ICategoryStorage;
import sr.we.storage.IStoreStorage;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

@Controller
public class StoresController {

    private final ICategoryStorage categoryStorage;

    private final IStoreStorage storeStorage;
    private final MongoTemplate mongoTemplate;

    /**
     *
     * @param categoryStorage {@link ICategoryStorage}
     * @param storeStorage {@link IStoreStorage}
     * @param mongoTemplate {@link MongoTemplate}
     */
    public StoresController(ICategoryStorage categoryStorage, IStoreStorage storeStorage, MongoTemplate mongoTemplate) {
        this.categoryStorage = categoryStorage;
        this.storeStorage = storeStorage;
        this.mongoTemplate = mongoTemplate;
    }

    /**
     *
     * @param businessId Provide business Id
     * @return List of Sections
     */
    public List<Section> allStores(Long businessId) {
        return storeStorage.allStores(businessId);
    }

    /**
     *
     * @param section Provide Section to be saved
     * @return saved section
     */
    public Section addNewStore(Section section) {
        return storeStorage.saveOrUpdate(section);
    }

    /**
     *
     * @param section Provide Section to be saved
     * @return saved section
     */
    public Section updateStore(Section section) {
        return storeStorage.saveOrUpdate(section);
    }

    /**
     *
     * @param businessId Provide Business Id
     * @param page provide page for pagination
     * @param pageSize provide page size for paging
     * @return Stream of sections
     */
    public Stream<Section> allSections(Long businessId, Integer page, Integer pageSize) {
        Query query = new Query();
        query.addCriteria(Criteria.where("businessId").is(businessId));
        query.with(PageRequest.of(page, pageSize));
        return mongoTemplate.find(query, Section.class).stream();
    }

    /**
     *
     * @param businessId Provide Business Id
     * @param page provide page for Paging
     * @param pageSize provide page size for paging
     * @param userOptional provide user optional for further filtering
     * @return stream of section
     */
    public Stream<Section> allSections(Long businessId, Integer page, Integer pageSize, Optional<User> userOptional) {
        Query query = new Query();
        query.addCriteria(Criteria.where("businessId").is(businessId));
        query.with(PageRequest.of(page, pageSize));


        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (!user.getRoles().contains(Role.ADMIN)) {
                // check sections uu ids
                Set<String> linkSections = user.getLinkSections();
                if (linkSections.isEmpty()) {
                    return null;
                }
                Criteria criteria = Criteria.where("uuId").in(linkSections);
                query.addCriteria(criteria);
            }
        }

        return mongoTemplate.find(query, Section.class).stream();
    }

    /**
     * Procedure to sync categories to given store
     * @param businessId provide businessId
     * @param storeId Provide store Id to be linked
     */
    public void synCategories(Long businessId, String storeId) {
        List<Category> categories = categoryStorage.allStores(businessId);
        List<Section> sections = allStores(businessId);
        for (Category category : categories) {
            boolean present = sections.stream().anyMatch(f -> f.getCategories() != null && f.getCategories().contains(category.getId()));
            if (!present) {
                Section section = new Section();
                section.setCategories(new HashSet<>(List.of(category.getId())));
                section.setName(category.getName());
                section.setBusinessId(businessId);
                section.setId(storeId);
                addNewStore(section);
            }
        }
    }

    /**
     *
     * @param uuId Provide primary key
     * @return section based on given key
     */
    public Section oneStore(String uuId) {
        return storeStorage.oneStore(uuId);
    }
}

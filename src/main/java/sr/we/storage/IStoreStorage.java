package sr.we.storage;


import org.apache.commons.lang3.StringUtils;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import sr.we.entity.eclipsestore.tables.Section;

import java.util.List;
import java.util.Optional;

public interface IStoreStorage extends MongoRepository<Section, String> {

    /**
     * Returns a single store element
     *
     * @param uuId
     * @return the store with the specified ID
     */
    @Query("{uuId:?0}")
    Section oneStore(String uuId);

    /**
     * Returns all stores in the storage
     *
     * @param businessId
     * @return A list of all stores
     */
    @Query("{businessId:?0}")
    List<Section> allStores(Long businessId);

    /**
     * Adds a store record to the storage
     *
     * @param section
     * @return the added store in Storage
     */
    default Section saveOrUpdate(Section section) {
        Section defaultSection = oneStore(section.getId());
        String uuId = null;
        if (defaultSection == null) {
            if (StringUtils.isNotBlank(section.getUuId())) {
                delete(section);
            }
            uuId = section.getId();
            section.setUuId(uuId);
        }
        return save(section);
    }

    /**
     * Delete the store containing the given ID
     *
     * @param uuId
     * @return boolean
     */
    default boolean deleteStore(String uuId) {
        deleteById(uuId);
        return true;
    }

//    Optional<Section> findSection(Long businessId, String storeId, String variantId, String posDeviceId, Section.Color color, Section.Form form, boolean door);
}

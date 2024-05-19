package sr.we.storage;


import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import sr.we.entity.eclipsestore.tables.Category;

import java.util.List;

public interface ICategoryStorage extends MongoRepository<Category,String> {

    /**
     * Returns a single store element
     * @param uuId
     * @return the store with the specified ID
     */
    @Query("{uuId:?0}")
    Category oneStore(String uuId);

    /**
     * Returns all stores in the storage
     * @param businessId
     * @return A list of all stores
     */
    @Query("{businessId:?0}")
    List<Category> allStores(Long businessId);

    /**
     * Adds a store record to the storage
     * @param Category
     * @return the added store in Storage
     */
    default Category saveOrUpdate(Category Category){
        return save(Category);
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

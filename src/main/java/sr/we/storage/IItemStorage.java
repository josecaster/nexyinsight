package sr.we.storage;


import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import sr.we.entity.eclipsestore.tables.Item;

import java.util.List;
import java.util.Optional;

public interface IItemStorage extends MongoRepository<Item, String> {

    /**
     * Returns a single Item element
     * @param uuId
     * @return the Item with the specified ID
     */
    default Item oneItem(String uuId){
        Optional<Item> byId = findById(uuId);
        return byId.orElse(null);
    }

    /**
     * Returns all Items in the storage
     * @param businessId
     * @return A list of all Items
     */
    @Query("{businessId:?0}")
    List<Item> allItems(Long businessId);

    /**
     * Adds a Item record to the storage
     * @param Item
     * @return the added Item in Storage
     */
    default Item saveOrUpdate(Item Item) {
        return save(Item);
    }

    /**
     * Delete the Item containing the given ID
     *
     * @param uuId
     * @return boolean
     */
    default boolean deleteItem(String uuId) {
        deleteById(uuId);
        return true;
    }

    @Query("{id:?0}")
    Item oneItemByLoyId(String id);

    @Query("{id: /^?0/}")
    List<Item> itemsByHalfLoyId(String id);
}

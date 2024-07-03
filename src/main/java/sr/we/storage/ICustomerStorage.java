package sr.we.storage;


import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import sr.we.entity.eclipsestore.tables.Customer;

import java.util.List;
import java.util.Optional;

public interface ICustomerStorage extends MongoRepository<Customer, String> {

    /**
     * Returns a single Customer element
     *
     * @param uuId
     * @return the Customer with the specified ID
     */
    default Customer oneCustomer(String uuId) {
        Optional<Customer> byId = findById(uuId);
        return byId.orElse(null);
    }

    /**
     * Returns all Customers in the storage
     *
     * @param businessId
     * @return A list of all Customers
     */
    @Query("{businessId:?0}")
    List<Customer> allCustomers(Long businessId);

    /**
     * Adds a Customer record to the storage
     *
     * @param Customer
     * @return the added Customer in Storage
     */
    default Customer saveOrUpdate(Customer Customer) {
        return save(Customer);
    }

    /**
     * Delete the Customer containing the given ID
     *
     * @param uuId
     * @return boolean
     */
    default boolean deleteCustomer(String uuId) {
        deleteById(uuId);
        return true;
    }

    @Query("{id:?0}")
    Customer oneCustomerByLoyId(String id);
}

package sr.we.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import sr.we.entity.eclipsestore.tables.Section;
import sr.we.integration.LoyItemsController;
import sr.we.integration.Parent;
import sr.we.storage.*;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Controller
public class StoresRestController extends Parent {

    @Autowired
    IStoreStorage storeStorage;
    @Autowired
    IReceiptsStorage receiptsStorage;
    @Autowired
    IInventoryHistoryStorage inventoryHistoryStorage;
    @Autowired
    ICategoryStorage categoryStorage;
    @Autowired
    IDeviceStorage deviceStorage;
    @Autowired
    IItemStorage itemStorage;
    @Autowired
    LoyItemsController loyItemsController;


    public Section oneStore(Long businessId, String id) {
        return storeStorage.oneStore(id);
    }

    public List<Section> allStores(Long businessId) {
        return storeStorage.allStores(businessId);
    }

    public Section addNewStore(Section section) {
        return storeStorage.saveOrUpdate(section);
    }

    public Section updateStore(Section section) {
        return storeStorage.saveOrUpdate(section);
    }

    public boolean deleteStore(String id) {
        return storeStorage.deleteStore(id);
    }


    public Stream<Section> allSections(Long businessId, Integer page, Integer pageSize, Predicate<? super Section> predicate) {
        return (page == null || pageSize == null) ? (predicate != null ? storeStorage.allStores(businessId).stream().filter(predicate) : storeStorage.allStores(businessId).stream()) : (predicate != null ? storeStorage.allStores(businessId).stream().filter(predicate).sorted(Comparator.comparing(Section::getId).reversed()).skip((long) page * pageSize).limit(pageSize) : storeStorage.allStores(businessId).stream().sorted(Comparator.comparing(Section::getId).reversed()).skip((long) page * pageSize).limit(pageSize));
    }
}

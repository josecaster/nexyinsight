package sr.we.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import sr.we.entity.eclipsestore.tables.Section;
import sr.we.integration.LoyItemsController;
import sr.we.integration.Parent;
import sr.we.storage.*;

import java.util.List;

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


}

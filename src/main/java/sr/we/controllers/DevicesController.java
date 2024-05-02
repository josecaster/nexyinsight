package sr.we.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import sr.we.entity.eclipsestore.tables.Device;
import sr.we.integration.Parent;
import sr.we.storage.IDeviceStorage;

import java.util.List;

@Controller
public class DevicesController {
    @Autowired
    private IDeviceStorage deviceStorage;

    public List<Device> allStores(Long businessId) {
        return deviceStorage.allStores(businessId);
    }


}

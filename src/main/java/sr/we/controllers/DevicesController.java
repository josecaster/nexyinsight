package sr.we.controllers;

import org.springframework.stereotype.Controller;
import sr.we.entity.eclipsestore.tables.Device;
import sr.we.storage.IDeviceStorage;

import java.util.List;

@Controller
public class DevicesController {

    private final IDeviceStorage deviceStorage;

    /**
     *
     * @param deviceStorage {@link IDeviceStorage}
     */
    public DevicesController(IDeviceStorage deviceStorage) {
        this.deviceStorage = deviceStorage;
    }

    /**
     *
     * @param businessId Fill in BusinessId
     * @return Returns a list of devices
     */
    public List<Device> findDevices(Long businessId) {
        return deviceStorage.allStores(businessId);
    }


}

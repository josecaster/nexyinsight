package sr.we.storage.impl;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.store.integrations.spring.boot.types.concurrent.Read;
import org.eclipse.store.integrations.spring.boot.types.concurrent.Write;
import org.eclipse.store.storage.embedded.types.EmbeddedStorageManager;
import org.springframework.stereotype.Component;
import sr.we.entity.eclipsestore.tables.Device;
import sr.we.storage.IDeviceStorage;

import java.util.List;

@Component
public class DeviceStorage extends EclipseStoreSuperService<Device> implements IDeviceStorage {

    public DeviceStorage(EmbeddedStorageManager storageManager) {
        super(storageManager, Device.class);
    }

    @Override
    @Read
    public Device oneStore(String uuId) {
        return get(uuId);
    }

    @Override
    @Read
    public List<Device> allStores(Long businessId) {
        return stream().filter(store -> store.getBusinessId() != null && store.getBusinessId().compareTo(businessId) == 0).toList();
    }

    @Override
    @Write
    public Device saveOrUpdate(Device Device) {

        // these few lines will ensure that there is always a default store available
        Device defaultDevice = oneStore(Device.getId());
        String uuId = null;
        if (defaultDevice == null) {
            if (StringUtils.isNotBlank(Device.getUuId())) {
                delete(Device.getUuId());
            }
            uuId = Device.getId();
        }
        return update(Device, f -> {
            f.setBusinessId(Device.getBusinessId());
            f.setId(Device.getId());
            f.setName(Device.getName());
            f.setActivated(Device.isActivated());
            f.setDeleted_at(Device.getDeleted_at());
            f.setStore_id(Device.getStore_id());
            return f;
        }, uuId);
    }

    @Override
    @Write
    public boolean deleteStore(String uuId) {
        return delete(uuId);
    }
}

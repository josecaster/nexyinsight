package sr.we.storage.impl;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.store.integrations.spring.boot.types.concurrent.Read;
import org.eclipse.store.integrations.spring.boot.types.concurrent.Write;
import org.eclipse.store.storage.embedded.types.EmbeddedStorageManager;
import org.springframework.stereotype.Component;
import sr.we.entity.eclipsestore.tables.Section;
import sr.we.storage.IStoreStorage;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Component
public class StoreStorage extends EclipseStoreSuperService<Section> implements IStoreStorage {

    public StoreStorage(EmbeddedStorageManager storageManager) {
        super(storageManager, Section.class);
    }

    @Override
    @Read
    public Section oneStore(String uuId) {
        return get(uuId);
    }

    @Override
    @Read
    public List<Section> allStores(Long businessId) {
        return stream().filter(store -> store.getBusinessId() != null && store.getBusinessId().compareTo(businessId) == 0).toList();
    }

    @Override
    @Write
    public Section saveOrUpdate(Section section) {

        // these few lines will ensure that there is always a default store available
        Section defaultSection = oneStore(section.getId());
        String uuId = null;
        if (defaultSection == null) {
            if (StringUtils.isNotBlank(section.getUuId())) {
                delete(section.getUuId());
            }
            uuId = section.getId();
        }
        return update(section, f -> {
            f.setAddress(section.getAddress());
            f.setCity(section.getCity());
            f.setBusinessId(section.getBusinessId());
            f.setDescription(section.getDescription());
            f.setCountryCode(section.getCountryCode());
            f.setId(section.getId());
            f.setName(section.getName());
            f.setPhoneNumber(section.getPhoneNumber());
            f.setRegion(section.getRegion());
            f.setPostalCode(section.getPostalCode());
            f.setDefault_name(section.getDefault_name());
            f.setCategories(section.getCategories());
            f.setDevices(section.getDevices());
            f.setColor(section.getColor());
            f.setForm(section.getForm());
            return f;
        }, uuId);
    }

    @Override
    @Write
    public boolean deleteStore(String uuId) {
        return delete(uuId);
    }

    @Override
    public Optional<Section> findSection(Long businessId, String storeId, String categoryId, String posDeviceId, Section.Color color, Section.Form form, boolean door) {
        Stream<Section> sectionStream = allStores(businessId).stream().filter(f -> f.getId().equalsIgnoreCase(storeId));
        if (StringUtils.isNotBlank(categoryId)) {
            sectionStream = sectionStream.filter(f -> f.getCategories() != null && f.getCategories().contains(categoryId));
        }
//        if (StringUtils.isNotBlank(posDeviceId)) {
//            sectionStream = sectionStream.filter(f -> f.getDevices() != null && f.getDevices().contains(posDeviceId));
//        }
        if (color != null) {
            sectionStream = sectionStream.filter(f -> f.getColor() != null && f.getColor().compareTo(color) == 0);
        }
        if (form != null) {
            sectionStream = sectionStream.filter(f -> f.getForm() != null && f.getForm().compareTo(form) == 0);
        }
        //        if (any.isEmpty() && door) {
//            Optional<Section> section = findSection(businessId, storeId, categoryId, null, null, null, false);// find with category
//            if (section.isEmpty()) {
//                section = findSection(businessId, storeId, null, posDeviceId, null, null, false);// find with pos device
//                if (section.isEmpty()) {
//                    section = findSection(businessId, storeId, null, null, null, null, false);// find default
//                    return section;
//                }
//            } else {
//                return section;
//            }
//        }
        return sectionStream.findAny();
    }
}

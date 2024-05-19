package sr.we.storage.impl;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.serializer.collections.lazy.LazyCollection;
import org.eclipse.serializer.collections.lazy.LazyHashMap;
import org.eclipse.serializer.reference.Lazy;
import org.eclipse.store.storage.embedded.types.EmbeddedStorageManager;
import sr.we.entity.eclipsestore.Database;
import sr.we.entity.eclipsestore.tables.Grid;
import sr.we.entity.eclipsestore.tables.SuperDao;
import sr.we.storage.InterExecutable;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

@Deprecated
public abstract class EclipseStoreSuperService<T extends SuperDao> {

    protected final EmbeddedStorageManager storageManager;
    Class<T> typeParameterClass;

    public EclipseStoreSuperService(EmbeddedStorageManager storageManager, Class<T> typeParameterClass) {
        this.storageManager = storageManager;
        this.typeParameterClass = typeParameterClass;
        this.storageManager.storeRoot();
    }


    public T get(String uuId) {

        LazyHashMap<String, T> stringTLazyHashMap1 = getStringTLazyHashMap();
        return stringTLazyHashMap1 == null ? null : stringTLazyHashMap1.get(uuId);
    }

    private LazyHashMap<String, T> getStringTLazyHashMap() {
        Database database = (Database) storageManager.root();
        if(!storageManager.isRunning()){
            storageManager.start();
        }
        Grid<T> list = database.getListByClass(typeParameterClass);
        Map<String, Lazy<LazyHashMap<String, T>>> stringTLazyHashMap = list.getMap();
        if (!stringTLazyHashMap.containsKey(typeParameterClass.getSimpleName())) {
            stringTLazyHashMap.put(typeParameterClass.getSimpleName(), Lazy.Reference(new LazyHashMap<>()));
            storageManager.store(stringTLazyHashMap);
        }
        Lazy<LazyHashMap<String, T>> lazyHashMapLazy = stringTLazyHashMap.get(typeParameterClass.getSimpleName());
        return Lazy.get(lazyHashMapLazy);
    }


    public Stream<T> stream() {
        LazyCollection<T> values = Objects.requireNonNull(getStringTLazyHashMap()).values();
        return values.isEmpty() ? Stream.empty() : values.stream();
    }


    public T update(T dao, InterExecutable<T, T> update) {
        String uuId = UUID.randomUUID().toString();
        return update(dao, update, uuId);
    }

    public T update(T dao, InterExecutable<T, T> update, String uuId) {
        if (StringUtils.isNotBlank(dao.getUuId())) {
            T daoExist = get(dao.getUuId());
            if (update != null && daoExist != null) {
                update.build(daoExist);// this method is provided to update the existed instance
                daoExist.setUuId(dao.getUuId());// to prevent people from overriding the UUID
                storageManager.store(daoExist);
            } else {
                return null;
            }
        } else {
            LazyHashMap<String, T> stringTLazyHashMap = getStringTLazyHashMap();
            dao.setUuId(StringUtils.isBlank(uuId) ? UUID.randomUUID().toString() : uuId);// if new we will add to the list
            stringTLazyHashMap.put(dao.getUuId(), dao);
            storageManager.store(stringTLazyHashMap);
        }

        return get(dao.getUuId());
    }


    public boolean delete(String uuId) {
        Database database = (Database) storageManager.root();
        T dao = get(uuId);
        LazyHashMap<String, T> stringTLazyHashMap1 = getStringTLazyHashMap();
        boolean b = stringTLazyHashMap1 != null && stringTLazyHashMap1.remove(uuId, dao);
        storageManager.store(database.getListByClass(typeParameterClass));
        return b;
    }


}

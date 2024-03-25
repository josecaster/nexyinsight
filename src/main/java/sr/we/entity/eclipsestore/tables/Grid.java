package sr.we.entity.eclipsestore.tables;

import org.eclipse.serializer.collections.lazy.LazyHashMap;
import org.eclipse.serializer.reference.Lazy;

import java.util.HashMap;
import java.util.Map;

public class Grid<T> {
    Map<String, Lazy<LazyHashMap<String,T>>> map = new HashMap<>();

    public Map<String, Lazy<LazyHashMap<String,T>>> getMap() {
        return map;
    }
}

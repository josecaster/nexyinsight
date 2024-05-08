package sr.we.entity.eclipsestore.legacy;

import org.eclipse.serializer.persistence.binary.types.Binary;
import org.eclipse.serializer.persistence.binary.types.BinaryLegacyTypeHandler;
import org.eclipse.serializer.persistence.types.PersistenceLoadHandler;
import org.eclipse.serializer.persistence.types.PersistenceReferenceLoader;
import org.eclipse.serializer.util.X;
import sr.we.entity.eclipsestore.tables.Item;

public class LegacyTypeHandlerItem extends BinaryLegacyTypeHandler.AbstractCustom<Item> {
    private static final long BINARY_OFFSET_is_composite = 0;

    protected LegacyTypeHandlerItem() {
        super(Item.class, X.List(CustomField(String.class, "is_composite")));
    }

    @Override
    public void iterateLoadableReferences(Binary bytes, PersistenceReferenceLoader iterator) {
        iterator.acceptObjectId(bytes.read_long(BINARY_OFFSET_is_composite));
    }

    @Override
    public Item create(Binary binary, PersistenceLoadHandler persistenceLoadHandler) {
        return new Item();
    }

    @Override
    public void updateState(Binary bytes, Item item, PersistenceLoadHandler handler) {

        final String is_composite = (String) handler.lookupObject(bytes.read_long(BINARY_OFFSET_is_composite));
        item.setIs_composite_string(is_composite);

    }    //need to know the binary layout of the persisted legacy class

    @Override
    public boolean hasPersistedReferences() {
        return true;
    }

    @Override
    public boolean hasVaryingPersistedLengthInstances() {
        return false;
    }


}

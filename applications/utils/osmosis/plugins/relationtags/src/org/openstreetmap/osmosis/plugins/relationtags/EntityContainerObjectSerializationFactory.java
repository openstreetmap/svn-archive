package org.openstreetmap.osmosis.plugins.relationtags;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.store.*;

/**
 * Serialization factory for all classes extending EntityContainer.
 *
 * @author Zverik
 */
public class EntityContainerObjectSerializationFactory implements ObjectSerializationFactory {

    public ObjectReader createObjectReader( StoreReader reader, StoreClassRegister scr ) {
        return new GenericObjectReader(reader, scr);
    }

    public ObjectWriter createObjectWriter( StoreWriter writer, StoreClassRegister scr ) {
        return new EntityContainerObjectWriter(writer, scr);
    }

    public static class EntityContainerObjectWriter extends BaseObjectWriter {
        public EntityContainerObjectWriter( StoreWriter storeWriter, StoreClassRegister storeClassRegister ) {
            super(storeWriter, storeClassRegister);
        }

        @Override
        protected void writeClassIdentifier( StoreWriter writer, StoreClassRegister scr, Class<?> type ) {
            if( !EntityContainer.class.isAssignableFrom(type) )
                throw new OsmosisRuntimeException(
                        "Received class " + type.getName() + " is not an instance of EntityContainer.");
            scr.storeIdentifierForClass(writer, type);
        }
    }
}

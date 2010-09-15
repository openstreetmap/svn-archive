package org.openstreetmap.josm.plugins.turnrestrictions.dnd;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.openstreetmap.josm.data.osm.PrimitiveId;
import org.openstreetmap.josm.data.osm.SimplePrimitiveId;

/**
 * To be used for Drag-and-Drop of a set of {@see PrimitiveId}s between
 * two components. 
 *
 */
public class PrimitiveIdTransferable implements Transferable{
    
    /** the data flower for the set of of primitive ids */
    static public final DataFlavor PRIMITIVE_ID_LIST_FLAVOR = 
        new DataFlavor(Set.class, "a set of OSM primitive ids");
    
    /** 
     * this transferable supports two flavors: (1) {@see #PRIMITIVE_ID_LIST_FLAVOR} and
     * (2) {@see DataFlavor#stringFlavor}.
     * 
     * See also {@see #getPrimitiveIds()} and {@see #getAsString()}
     */
    static public final DataFlavor[] SUPPORTED_FLAVORS = new DataFlavor[] {
        PRIMITIVE_ID_LIST_FLAVOR,
        DataFlavor.stringFlavor
    };

    
    private List<PrimitiveId> ids = new ArrayList<PrimitiveId>();
    
    /**
     * Creates a transferable from a collection of {@see PrimitiveId}s
     * 
     * @param ids
     */
    public PrimitiveIdTransferable(List<PrimitiveId> ids) {
        if (ids == null) return;
        for(PrimitiveId id: ids) {
            this.ids.add(new SimplePrimitiveId(id.getUniqueId(), id.getType()));
        }
    }
    
    /**
     * If flavor is {@see #PRIMITIVE_ID_SET_FLAVOR}, replies a the list of
     * transferred {@see PrimitiveId}s 
     * 
     * If flavor is {@see DataFlavor#stringFlavor}, replies a string representation
     * of the list of transferred {@see PrimitiveId}s
     */
    public Object getTransferData(DataFlavor flavor)
            throws UnsupportedFlavorException, IOException {
        if (PRIMITIVE_ID_LIST_FLAVOR.equals(flavor)) {
            return getPrimitiveIds();
        } else if (DataFlavor.stringFlavor.equals(flavor)) {
            return getAsString();
        }
        throw new UnsupportedFlavorException(flavor);
    }
    
    /**
     * Replies the list of OSM primitive ids
     * 
     * @return the list of OSM primitive ids
     */
    public List<PrimitiveId> getPrimitiveIds() {
        return ids;
    }
    
    /**
     * Replies a string representation of the list of OSM primitive ids
     *  
     * @return a string representation of the list of OSM primitive ids
     */
    public String getAsString() {
        StringBuffer sb = new StringBuffer();
        for(PrimitiveId id: ids) {
            if (sb.length() > 0) sb.append(",");
            sb.append(id.getType().getAPIName()).append("/").append(id.getUniqueId());
        }
        return sb.toString();
    }

    public DataFlavor[] getTransferDataFlavors() {
        return SUPPORTED_FLAVORS;
    }

    public boolean isDataFlavorSupported(DataFlavor flavor) {
        for(DataFlavor df: SUPPORTED_FLAVORS) {
            if (df.equals(flavor)) return true;
        }
        return false;
    }           
}

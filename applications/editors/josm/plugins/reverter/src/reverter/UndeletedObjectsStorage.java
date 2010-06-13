package reverter;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.gui.MapView.LayerChangeListener;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;

public class UndeletedObjectsStorage implements LayerChangeListener, PropertyChangeListener {
    private final HashMap<DataSet,HashSet<PrimitiveIdVersion>> undeletedPrimitives =
        new HashMap<DataSet,HashSet<PrimitiveIdVersion>>();

    public boolean haveUndeletedObjects(DataSet dataSet) {
        return undeletedPrimitives.containsKey(dataSet);
    }
    
    public void addPrimitive(DataSet dataSet,PrimitiveIdVersion id) {
        HashSet<PrimitiveIdVersion> set = undeletedPrimitives.get(dataSet);
        if (set == null) {
            set = new HashSet<PrimitiveIdVersion>();
            undeletedPrimitives.put(dataSet, set);
        }
        set.add(id);
    }
    
    /**
     * Checks if primitive still should be treated as "undeleted"
     * @param dataSet DataSet of the primitive
     * @param id Id and version of the primitive
     * @return true if primitive is still should be treated as "undeleted"
     */
    private boolean checkPrimitive(DataSet dataSet, PrimitiveIdVersion id) {
        OsmPrimitive p = dataSet.getPrimitiveById(id.getPrimitiveId());
        if (p == null) return false;
        if (p.getVersion() != id.getVersion()) return false;
        return true;
    }
    
    private void pruneEmptyLayers() {
        for (Iterator<HashSet<PrimitiveIdVersion>> it =
                undeletedPrimitives.values().iterator();it.hasNext();) {
            if (it.next().isEmpty()) it.remove();
        }
    }
    
    private void pruneLayer(DataSet dataSet) {
        HashSet<PrimitiveIdVersion> idSet = undeletedPrimitives.get(dataSet);
        for (Iterator<PrimitiveIdVersion> it = idSet.iterator();it.hasNext();) {
            if (!checkPrimitive(dataSet,it.next())) it.remove();
        }
    }
    
    public void pruneObsolete() {
        for (DataSet ds : undeletedPrimitives.keySet()) {
            pruneLayer(ds);
        }
        pruneEmptyLayers();
    }
    
    public List<OsmPrimitive> getUndeletedObjects(DataSet dataSet) {
        pruneObsolete();
        HashSet<PrimitiveIdVersion> idSet = undeletedPrimitives.get(dataSet);
        if (idSet == null) return null;
        LinkedList<OsmPrimitive> undeleted = new LinkedList<OsmPrimitive>();
        for (PrimitiveIdVersion id : idSet) {
            undeleted.add(dataSet.getPrimitiveById(id.getPrimitiveId()));
        }
        return undeleted;
    }
    
    @Override
    public void activeLayerChange(Layer oldLayer, Layer newLayer) {
    }

    @Override
    public void layerAdded(Layer newLayer) {
        if (!(newLayer instanceof OsmDataLayer)) return;
        newLayer.addPropertyChangeListener(this);
    }

    @Override
    public void layerRemoved(Layer oldLayer) {
        if (!(oldLayer instanceof OsmDataLayer)) return;
        oldLayer.addPropertyChangeListener(this);
        OsmDataLayer dataLayer = (OsmDataLayer)oldLayer;
        if (undeletedPrimitives.containsKey(dataLayer.data)) {
            undeletedPrimitives.remove(dataLayer.data);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt == null) return;
        if (evt.getPropertyName() == OsmDataLayer.REQUIRES_UPLOAD_TO_SERVER_PROP &&
                evt.getOldValue().equals(true) && evt.getNewValue().equals(false)) {
            pruneObsolete();
        }
    }
}

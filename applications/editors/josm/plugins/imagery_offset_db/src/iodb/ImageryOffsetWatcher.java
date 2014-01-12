package iodb;

import java.util.*;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.layer.ImageryLayer;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.tools.Destroyable;

/**
 * This class watches imagery layer offsets and notifies listeners when there's a need to update offset
 * for the current layer.
 *
 * @author Zverik
 * @license WTFPL
 */
public class ImageryOffsetWatcher implements MapView.ZoomChangeListener, MapView.LayerChangeListener, Destroyable {
    private static final double THRESHOLD = 1e-8;
    private static ImageryOffsetWatcher instance;
    private Map<Integer, ImageryLayerData> layers = new TreeMap<Integer, ImageryLayerData>();
    private List<OffsetStateListener> listeners = new ArrayList<OffsetStateListener>();
    private Timer time;
    private double maxDistance;
    private boolean offsetGood = true;

    /**
     * Create an instance and register it as a listener to MapView.
     * Also starts a timer task.
     */
    private ImageryOffsetWatcher() {
        maxDistance = Main.pref.getDouble("iodb.offset.radius", 15);
        MapView.addZoomChangeListener(this);
        MapView.addLayerChangeListener(this);
        checkOffset(); // we assume there's at the most one imagery layer at this moment
        time = new Timer();
        time.schedule(new IntervalOffsetChecker(), 0, 2000);
    }

    /**
     * Unregister all events. This actually gets never called, but it's not a problem.
     */
    public void destroy() {
        MapView.removeZoomChangeListener(this);
        MapView.removeLayerChangeListener(this);
        time.cancel();
    }

    /**
     * This class is a singleton, this method returns the instance,
     * creating it if neccessary.
     */
    public static ImageryOffsetWatcher getInstance() {
        if( instance == null ) {
            instance = new ImageryOffsetWatcher();
        }
        return instance;
    }

    /**
     * Register an offset state listener.
     */
    public void register( OffsetStateListener listener ) {
        listeners.add(listener);
        listener.offsetStateChanged(offsetGood);
    }

    /**
     * Unregister an offset state listener.
     */
    public void unregister( OffsetStateListener listener ) {
        listeners.remove(listener);
    }

    /**
     * Change stored offset state, notify listeners if needed.
     */
    private void setOffsetGood( boolean good ) {
        if( good != offsetGood ) {
            for( OffsetStateListener listener : listeners )
                listener.offsetStateChanged(good);
        }
        offsetGood = good;
    }

    /**
     * Check if the offset state has been changed.
     */
    private synchronized void checkOffset() {
        if( maxDistance <= 0 ) {
            setOffsetGood(true);
            return;
        }
        ImageryLayer layer = ImageryOffsetTools.getTopImageryLayer();
        if( layer == null ) {
            setOffsetGood(true);
            return;
        }
        LatLon center = ImageryOffsetTools.getMapCenter();
        Integer hash = layer.hashCode();
        ImageryLayerData data = layers.get(hash);
        if( data == null ) {
            // create entry for this layer and mark as needing alignment
            data = new ImageryLayerData();
            data.lastDx = layer.getDx();
            data.lastDy = layer.getDy();
            boolean r = false;
            if( Math.abs(data.lastDx) + Math.abs(data.lastDy) > THRESHOLD ) {
                data.lastChecked = center;
                r = true;
            }
            layers.put(hash, data);
            setOffsetGood(r);
        } else {
            // now, we have a returning layer.
            if( Math.abs(data.lastDx - layer.getDx()) + Math.abs(data.lastDy - layer.getDy()) > THRESHOLD ) {
                // offset has changed, record the current position
                data.lastDx = layer.getDx();
                data.lastDy = layer.getDy();
                data.lastChecked = center;
                storeLayerOffset(layer);
                setOffsetGood(true);
            } else {
                setOffsetGood(data.lastChecked != null && center.greatCircleDistance(data.lastChecked) <= maxDistance * 1000);
            }
        }
    }

    /**
     * Mark the current offset as good. This method is called by {@link OffsetDialog}
     * to notify the watcher that an offset button has been clicked, and regardless of
     * whether it has changed an offset, the currect imagery alignment is ok.
     */
    public void markGood() {
        ImageryLayer layer = ImageryOffsetTools.getTopImageryLayer();
        if( layer != null ) {
            LatLon center = ImageryOffsetTools.getMapCenter();
            Integer hash = layer.hashCode();
            ImageryLayerData data = layers.get(hash);
            if( data == null ) {
                // create entry for this layer and mark as good
                data = new ImageryLayerData();
                data.lastDx = layer.getDx();
                data.lastDy = layer.getDy();
                data.lastChecked = center;
                layers.put(hash, data);
            } else {
                data.lastDx = layer.getDx();
                data.lastDy = layer.getDy();
                data.lastChecked = center;
            }
            storeLayerOffset(layer);
        }
        setOffsetGood(true);
    }

    /**
     * This class stores an offset and last location for a single imagery layer.
     * All fields are public, because this is not enterprise.
     */
    private static class ImageryLayerData {
        public double lastDx = 0.0;
        public double lastDy = 0.0;
        public LatLon lastChecked;
    }

    public void zoomChanged() {
        checkOffset();
    }

    public void activeLayerChange( Layer oldLayer, Layer newLayer ) {
        checkOffset();
    }

    public void layerAdded( Layer newLayer ) {
        if( newLayer instanceof ImageryLayer )
            loadLayerOffset((ImageryLayer)newLayer);
        checkOffset();
    }

    public void layerRemoved( Layer oldLayer ) {
        checkOffset();
    }

    /**
     * Saves the current imagery layer offset to preferences. It is stored as a
     * collection of ':'-separated strings: imagery_id:lat:lon:dx:dy. No need for
     * projections: nobody uses them anyway.
     */
    private void storeLayerOffset( ImageryLayer layer ) {
        String id = ImageryOffsetTools.getImageryID(layer);
        if( !Main.pref.getBoolean("iodb.remember.offsets", true) || id == null )
            return;
        Collection<String> offsets = new LinkedList<String>(Main.pref.getCollection("iodb.stored.offsets"));
        for( Iterator<String> iter = offsets.iterator(); iter.hasNext(); ) {
            String[] offset = iter.next().split(":");
            if( offset.length == 5 && offset[0].equals(id) )
                iter.remove();
        }
        LatLon center = ImageryOffsetTools.getMapCenter();
        offsets.add(id + ":" + center.lat() + ":" + center.lon() + ":" + layer.getDx() + ":" + layer.getDy());
        Main.pref.putCollection("iodb.stored.offsets", offsets);
    }

    /**
     * Loads the current imagery layer offset from preferences. 
     */
    private void loadLayerOffset( ImageryLayer layer ) {
        String id = ImageryOffsetTools.getImageryID(layer);
        if( !Main.pref.getBoolean("iodb.remember.offsets", true) || id == null )
            return;
        Collection<String> offsets = Main.pref.getCollection("iodb.stored.offsets");
        for( String offset : offsets ) {
            String[] parts = offset.split(":");
            if( parts.length == 5 && parts[0].equals(id) ) {
                double[] dparts = new double[4];
                try {
                    for( int i = 0; i < 4; i++ )
                        dparts[i] = Double.parseDouble(parts[i+1]);
                } catch( Exception e ) {
                    continue;
                }
                LatLon lastPos = new LatLon(dparts[0], dparts[1]);
                if( lastPos.greatCircleDistance(ImageryOffsetTools.getMapCenter()) < Math.max(maxDistance, 3.0) * 1000 ) {
                    // apply offset
                    layer.setOffset(dparts[2], dparts[3]);
                    return;
                }
            }
        }
    }

    /**
     * This task is run every 1-2 seconds.
     */
    private class IntervalOffsetChecker extends TimerTask {
        /**
         * Reread max radius setting and update offset state.
         */
        @Override
        public void run() {
            maxDistance = Main.pref.getDouble("iodb.offset.radius", 15);
            checkOffset();
        }
    }

    /**
     * The interface for offset listeners.
     */
    public interface OffsetStateListener {
        void offsetStateChanged( boolean isOffsetGood );
    }
}

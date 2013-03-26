package iodb;

import java.util.*;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.layer.ImageryLayer;
import org.openstreetmap.josm.gui.layer.Layer;

/**
 * This class watches imagery layer offsets and notifies listeners when there's a need to update offset
 * for the current layer.
 *
 * @author Zverik
 * @license WTFPL
 */
public class ImageryOffsetWatcher implements MapView.ZoomChangeListener, MapView.LayerChangeListener {
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
                setOffsetGood(true);
            } else {
                setOffsetGood(data.lastChecked != null && center.greatCircleDistance(data.lastChecked) <= maxDistance * 1000);
            }
        }
    }

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
        checkOffset();
    }

    public void layerRemoved( Layer oldLayer ) {
        checkOffset();
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

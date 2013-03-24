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
 * @author zverik
 */
public class ImageryOffsetWatcher implements MapView.ZoomChangeListener, MapView.LayerChangeListener {
    private static final double THRESHOLD = 1e-8;
    private static final double MAX_DISTANCE = Main.pref.getDouble("iodb.offset.radius", 15) * 1000;
    private Map<Integer, ImageryLayerData> layers = new TreeMap<Integer, ImageryLayerData>();
    private List<GetImageryOffsetAction> listeners = new ArrayList<GetImageryOffsetAction>();
    private Timer time;
    private static ImageryOffsetWatcher instance;
    private boolean offsetGood;

    private ImageryOffsetWatcher() {
        if( MAX_DISTANCE <= 0 )
            return;
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

    public static ImageryOffsetWatcher getInstance() {
        if( instance == null ) {
            instance = new ImageryOffsetWatcher();
        }
        return instance;
    }

    public void register( GetImageryOffsetAction listener ) {
        listeners.add(listener);
        listener.offsetStateChanged(offsetGood);
    }

    public void unregister( GetImageryOffsetAction listener ) {
        listeners.remove(listener);
    }

    private void setOffsetGood( boolean good ) {
        if( good != offsetGood ) {
            for( GetImageryOffsetAction listener : listeners )
                listener.offsetStateChanged(good);
        }
        offsetGood = good;
    }

    private void checkOffset() {
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
                setOffsetGood(data.lastChecked != null && center.greatCircleDistance(data.lastChecked) <= MAX_DISTANCE);
            }
        }
    }

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

    private class IntervalOffsetChecker extends TimerTask {
        @Override
        public void run() {
            checkOffset();
        }
    }
}

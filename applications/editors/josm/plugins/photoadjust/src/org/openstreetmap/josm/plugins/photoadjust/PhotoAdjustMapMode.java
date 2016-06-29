package org.openstreetmap.josm.plugins.photoadjust;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Cursor;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.mapmode.MapMode;
import org.openstreetmap.josm.gui.IconToggleButton;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.LayerManager.LayerAddEvent;
import org.openstreetmap.josm.gui.layer.LayerManager.LayerChangeListener;
import org.openstreetmap.josm.gui.layer.LayerManager.LayerOrderChangeEvent;
import org.openstreetmap.josm.gui.layer.LayerManager.LayerRemoveEvent;
import org.openstreetmap.josm.gui.layer.MainLayerManager.ActiveLayerChangeEvent;
import org.openstreetmap.josm.gui.layer.MainLayerManager.ActiveLayerChangeListener;
import org.openstreetmap.josm.gui.layer.geoimage.GeoImageLayer;
import org.openstreetmap.josm.tools.Shortcut;

/**
 * Map mode version of the plug-in.  The associated button is shown
 * only if there is at least one GeoImageLayer.  Otherwise the button
 * is hidden to make room for other buttons.  This map mode is
 * inactive if the active layer is a GeoImageLayer as the main plug-in
 * handles it.
 */
public class PhotoAdjustMapMode extends MapMode implements LayerChangeListener, ActiveLayerChangeListener {

    /** True if this map mode waits for mouse events. */
    private boolean modeActive = false;
    /** True if the map mode button is selected and active. */
    private boolean modeSelected = false;
    private MouseAdapter mouseAdapter;
    private MouseMotionAdapter mouseMotionAdapter;
    private IconToggleButton mmButton;
    private PhotoAdjustWorker worker;

    public PhotoAdjustMapMode(MapFrame mapFrame, PhotoAdjustWorker worker) {
        super(tr("Adjust photos"), "photoadjust.png",
              tr("Move and position photos"),
              // It is almost impossible to find an unused shortcut.
              Shortcut.registerShortcut("mapmode:photoadjust",
                                        tr("Mode: {0}", tr("Adjust photos")),
                                        KeyEvent.CHAR_UNDEFINED, Shortcut.NONE),
              mapFrame, Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        GeoImageLayer.registerSupportedMapMode(this);
        initAdapters();
        this.worker = worker;
        Main.getLayerManager().addLayerChangeListener(this);
        Main.getLayerManager().addActiveLayerChangeListener(this);
    }

    /**
     * The main plugin does nothing but to call this method, which
     * connects this map mode with a button that is registered as map
     * mode.
     *
     * @param mapFrame Map frame the map mode button is added to.
     */
    public void installMapMode(MapFrame mapFrame) {
        mmButton = new IconToggleButton(this);
        mmButton.setAutoHideDisabledButton(true);
        mapFrame.addMapMode(mmButton);
    }

    @Override
    public String getModeHelpText() {
        if (hasLayersToAdjust()) {
            return tr("Click+drag photo, shift+click to position photo, control+click to set direction.");
        }
        else {
            return tr("Please load some photos.");
        }
    }

    @Override
    public boolean layerIsSupported(Layer layer) {
        return hasLayersToAdjust();
    }

    @Override
    protected void updateEnabledState() {
        setEnabled(hasLayersToAdjust());
    }

    /**
     * Update the button state.  We do this more frequently than the
     * JOSM core would do it.
     */
    public void updateButtonState() {
        if (mmButton != null) {
            final boolean enabled = hasLayersToAdjust();
            setEnabled(enabled);
            if (enabled) {
                mmButton.showButton();
            } else {
                mmButton.hideButton();
            }
        }
    }

    /**
     * Activate the map mode, i.e. wait for mouse events.
     */
    private void activateMode() {
        if (modeSelected && !modeActive) {
            Main.map.mapView.addMouseListener(mouseAdapter);
            Main.map.mapView.addMouseMotionListener(mouseMotionAdapter);
            modeActive = true;
            updateStatusLine();
        }
    }

    /**
     * Deactivate map mode.
     */
    private void deactivateMode() {
        if (modeActive) {
            Main.map.mapView.removeMouseListener(mouseAdapter);
            Main.map.mapView.removeMouseMotionListener(mouseMotionAdapter);
            modeActive = false;
        }
    }

    @Override
    public void enterMode() {
        super.enterMode();
        modeSelected = true;
        // Activate the mode only if the current layer is not a
        // GeoImageLayer.  GeoImageLayer's are handled by the plug-in directly.
        if (! (Main.getLayerManager().getActiveLayer() instanceof GeoImageLayer)) {
            activateMode();
        }
    }

    @Override
    public void exitMode() {
        super.exitMode();
        Main.getLayerManager().removeActiveLayerChangeListener(this);
        Main.getLayerManager().removeLayerChangeListener(this);
        deactivateMode();
        modeSelected = false;
    }

    @Override
    public void activeOrEditLayerChanged(ActiveLayerChangeEvent e) {
        // The main part of the plugin takes care of all operations if a GeoImageLayer is active.
        if (Main.getLayerManager().getActiveLayer() instanceof GeoImageLayer) {
            deactivateMode();
        } else {
            activateMode();
        }
    }

    @Override
    public void layerAdded(LayerAddEvent e) {
        if (modeActive) updateStatusLine();
        updateButtonState();
    }

    @Override
    public void layerRemoving(LayerRemoveEvent e) {
        if (modeActive) updateStatusLine();
        updateButtonState();
    }

    @Override
    public void layerOrderChanged(LayerOrderChangeEvent e) {
    }

    /**
     * Create mouse adapters similar to the main plug-in.
     */
    private void initAdapters() {
        // The MapMode implements a MouseAdapter and a
        // MouseMotionAdapter, but we cannot use them because they get
        // activated with the first layer that is created.  This is
        // not what we want, we want to activate them if this map mode
        // gets activated.
        mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent evt) {
                worker.doMousePressed(evt, getVisibleGeoImageLayers());
            }

            @Override
            public void mouseReleased(MouseEvent evt) {
                worker.doMouseReleased(evt);
            }
        };

        mouseMotionAdapter = new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent evt) {
                worker.doMouseDragged(evt);
            }
        };
    }

    /**
     * Replies true if there is at least one geo image layer.
     *
     * @return {@code true} if there is at least one geo image layer
     */
    private boolean hasLayersToAdjust() {
        if (Main.map == null || Main.map.mapView == null) return false;
        return ! Main.getLayerManager().getLayersOfType(GeoImageLayer.class).isEmpty();
    }

    /**
     * Prepare list of visible GeoImageLayer's whose photos can be adjusted.
     *
     * @return list of visible GeoImageLayer's
     */
    private List<GeoImageLayer> getVisibleGeoImageLayers() {
        List<GeoImageLayer> all = new ArrayList<>(Main.getLayerManager().getLayersOfType(GeoImageLayer.class));
        Iterator<GeoImageLayer> it = all.iterator();
        while (it.hasNext()) {
            if (!it.next().isVisible()) it.remove();
        }
        return all;
    }

    @Override
    public void destroy() {
        super.destroy();
        Main.getLayerManager().removeLayerChangeListener(this);
        Main.getLayerManager().removeActiveLayerChangeListener(this);
    }
}

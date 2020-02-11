// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.indoorhelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.autofilter.AutoFilter;
import org.openstreetmap.josm.gui.autofilter.AutoFilterManager;
import org.openstreetmap.josm.gui.layer.MainLayerManager.ActiveLayerChangeEvent;
import org.openstreetmap.josm.gui.layer.MainLayerManager.ActiveLayerChangeListener;
import org.openstreetmap.josm.gui.layer.MapViewPaintable.PaintableInvalidationEvent;
import org.openstreetmap.josm.gui.layer.MapViewPaintable.PaintableInvalidationListener;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.spi.preferences.Config;

import controller.IndoorHelperController;

/**
 * This is the main class for the indoorhelper plug-in.
 *
 * @author egru
 * @author rebsc
 */
public class IndoorHelperPlugin extends Plugin implements PaintableInvalidationListener, ActiveLayerChangeListener {

    private IndoorHelperController controller;
    String sep = System.getProperty("file.separator");

    /**
     * Constructor for the plug-in.
     *
     * Exports the needed files and adds them to the settings.
     *
     * @param info general information about the plug-in
     * @throws IOException if any I/O error occurs
     */
    public IndoorHelperPlugin(PluginInformation info) throws IOException {
        super(info);
        exportStyleFile("sit.mapcss");
        exportStyleFile("entrance_door_icon.png");
        exportStyleFile("entrance_icon.png");
        exportStyleFile("elevator_icon.png");
        MainApplication.getLayerManager().addAndFireActiveLayerChangeListener(this);
    }

    @Override
    public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
        super.mapFrameInitialized(oldFrame, newFrame);

        if (oldFrame == null && newFrame != null) {
            // Secures that the plug-in is only loaded, if a new MapFrame is created.
            controller = new IndoorHelperController();
        }
    }

    /**
     * Exports the mapCSS file to the preferences directory.
     * @param resourceName resource name
     * @throws IOException if any I/O error occurs
     */
    private void exportStyleFile(String resourceName) throws IOException {
        try (InputStream stream = IndoorHelperPlugin.class.getResourceAsStream("/data/" + resourceName)) {
            if (stream == null) {
                throw new IOException("Cannot get resource \"" + resourceName + "\" from Jar file.");
            }

            String outPath;
            int readBytes;
            byte[] buffer = new byte[4096];

            String valDirPath = Config.getDirs().getUserDataDirectory(true) + sep + "styles";
            File valDir = new File(valDirPath);
            valDir.mkdirs();
            outPath = valDir.getAbsolutePath() +sep+ resourceName;

            try (OutputStream resStreamOut = new FileOutputStream(outPath)) {
                while ((readBytes = stream.read(buffer)) > 0) {
                    resStreamOut.write(buffer, 0, readBytes);
                }
            }
        }
    }

    @Override
    public void activeOrEditLayerChanged(ActiveLayerChangeEvent e) {
        OsmDataLayer editLayer = MainApplication.getLayerManager().getEditLayer();
        if (editLayer != null) {
            editLayer.addInvalidationListener(this);
        }
    }

    @Override
    public void paintableInvalidated(PaintableInvalidationEvent event) {
        AutoFilter currentAutoFilter = AutoFilterManager.getInstance().getCurrentAutoFilter();

        if (currentAutoFilter != null) {
            if (controller != null) {
                String currentFilterValue = currentAutoFilter.getLabel();

                controller.setIndoorLevel(currentFilterValue);
                controller.getIndoorLevel(currentFilterValue);
                controller.unsetSpecificKeyFilter("repeat_on");
            }
        } else if (controller != null) {
            controller.setIndoorLevel("");
            controller.getIndoorLevel("");
        }
    }
}

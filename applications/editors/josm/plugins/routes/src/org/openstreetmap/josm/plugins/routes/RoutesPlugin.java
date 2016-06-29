package org.openstreetmap.josm.plugins.routes;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.LayerManager.LayerAddEvent;
import org.openstreetmap.josm.gui.layer.LayerManager.LayerChangeListener;
import org.openstreetmap.josm.gui.layer.LayerManager.LayerOrderChangeEvent;
import org.openstreetmap.josm.gui.layer.LayerManager.LayerRemoveEvent;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.plugins.routes.xml.Routes;
import org.openstreetmap.josm.plugins.routes.xml.RoutesXMLLayer;

public class RoutesPlugin extends Plugin implements LayerChangeListener {

    private final List<RouteLayer> routeLayers = new ArrayList<>();
    private boolean isShown;

    public RoutesPlugin(PluginInformation info) {
        super(info);
        Main.getLayerManager().addLayerChangeListener(this);

        File routesFile = new File(getPluginDir() + File.separator + "routes.xml");
        if (!routesFile.exists()) {
            Main.info("File with route definitions doesn't exist, using default");

            try {
                routesFile.getParentFile().mkdir();
                try (
                        OutputStream outputStream = new FileOutputStream(routesFile);
                        InputStream inputStream = Routes.class.getResourceAsStream("routes.xml");
                        ) {
                    byte[] b = new byte[512];
                    int read;
                    while ((read = inputStream.read(b)) != -1) {
                        outputStream.write(b, 0, read);
                    }
                }
            } catch (IOException e) {
                Main.error(e);
            }
        }

        try {
            JAXBContext context = JAXBContext.newInstance(
                    Routes.class.getPackage().getName(), Routes.class.getClassLoader());
            Unmarshaller unmarshaller = context.createUnmarshaller();
            Routes routes = (Routes)unmarshaller.unmarshal(
                    new FileInputStream(getPluginDir() + File.separator + "routes.xml"));
            for (RoutesXMLLayer layer:routes.getLayer()) {
                if (layer.isEnabled()) {
                    routeLayers.add(new RouteLayer(layer));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //new RelationEditMode(Main.map);
        //Main.main.addLayer(new RouteLayer("Hiking trails"));
    }

    public void activeLayerChange(Layer oldLayer, Layer newLayer) {
        // Do nothing
    }

    private void checkLayers() {
        if (Main.map != null && Main.map.mapView != null) {
            for (Layer layer:Main.getLayerManager().getLayers()) {
                if (layer instanceof OsmDataLayer) {
                    if (!isShown) {
                        isShown = true;
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                for (RouteLayer routeLayer:routeLayers) {
                                    Main.getLayerManager().addLayer(routeLayer);
                                }
                            }
                        });
                    }
                    return;
                }
            }
            if (isShown) {
                isShown = false;
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        for (RouteLayer routeLayer:routeLayers) {
                            Main.getLayerManager().removeLayer(routeLayer);
                        }
                    }
                });
            }
        }
    }

    @Override
    public void layerAdded(LayerAddEvent e) {
        checkLayers();
    }

    @Override
    public void layerRemoving(LayerRemoveEvent e) {
        checkLayers();
    }

    @Override
    public void layerOrderChanged(LayerOrderChangeEvent e) {
        // Do nothing
    }
}

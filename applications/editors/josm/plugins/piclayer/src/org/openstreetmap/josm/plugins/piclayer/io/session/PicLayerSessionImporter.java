package org.openstreetmap.josm.plugins.piclayer.io.session;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.gpx.GpxImageEntry;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.gui.layer.geoimage.ImageEntry;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.io.IllegalDataException;
import org.openstreetmap.josm.io.session.SessionLayerImporter;
import org.openstreetmap.josm.io.session.SessionReader;
import org.openstreetmap.josm.plugins.piclayer.layer.PicLayerAbstract;
import org.openstreetmap.josm.plugins.piclayer.layer.PicLayerFromFile;
import org.openstreetmap.josm.plugins.piclayer.layer.PicLayerFromKML;
import org.openstreetmap.josm.plugins.piclayer.layer.kml.KMLGroundOverlay;
import org.openstreetmap.josm.plugins.piclayer.layer.kml.KMLReader;
import org.openstreetmap.josm.tools.Logging;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Session importer for PicLayer.
 * Code copied and adjusted from JOSM GeoImageSessionImporter-class!
 * @author rebsc
 *
 */
public class PicLayerSessionImporter implements SessionLayerImporter{

	@Override
    public Layer load(Element elem, SessionReader.ImportSupport support, ProgressMonitor progressMonitor)
            throws IOException, IllegalDataException {
        String version = elem.getAttribute("version");
        if (!"0.1".equals(version)) {
            throw new IllegalDataException(tr("Version ''{0}'' of meta data for piclayerImage layer is not supported. Expected: 0.1", version));
        }

        List<ImageEntry> entries = new ArrayList<>();
        NodeList imgNodes = elem.getChildNodes();
        boolean useThumbs = false;
        for (int i = 0; i < imgNodes.getLength(); ++i) {
            Node imgNode = imgNodes.item(i);
            if (imgNode.getNodeType() == Node.ELEMENT_NODE) {
                Element imgElem = (Element) imgNode;
                if ("piclayerImage".equals(imgElem.getTagName())) {
                    ImageEntry entry = new ImageEntry();
                    NodeList attrNodes = imgElem.getChildNodes();
                    for (int j = 0; j < attrNodes.getLength(); ++j) {
                        Node attrNode = attrNodes.item(j);
                        if (attrNode.getNodeType() == Node.ELEMENT_NODE) {
                            handleElement(entry, (Element) attrNode);
                        }
                    }
                    entries.add(entry);
                } else if ("show-thumbnails".equals(imgElem.getTagName())) {
                    useThumbs = Boolean.parseBoolean(imgElem.getTextContent());
                }
            }
        }

        // create empty layer to avoid exceptions, remove layer afterwards
        Layer defaultLayer = new OsmDataLayer(new DataSet(), OsmDataLayer.createNewName(), null);
        MainApplication.getLayerManager().addLayer(defaultLayer);

        PicLayerAbstract layer = null;

        File file = entries.get(0).getFile();
        if(file.getAbsolutePath().contains("kml") || file.getAbsolutePath().contains("KML")) {
        	KMLReader kml = new KMLReader(file);
            kml.process();
            JOptionPane.showMessageDialog(null, tr("KML calibration is in beta stage and may produce incorrectly calibrated layers!\n"+
            "Please use {0} to upload your KMLs that were calibrated incorrectly.",
            "https://josm.openstreetmap.de/ticket/5451"), tr("Notification"), JOptionPane.INFORMATION_MESSAGE);
            List<KMLGroundOverlay> overlays = kml.getGroundOverlays();
            if(!overlays.isEmpty()) {
            	layer = new PicLayerFromKML(file, overlays.get(0));
                layer.initialize();
            }
        }
        else {
            layer = new PicLayerFromFile(file);
            layer.initialize();
        }

        MainApplication.getLayerManager().removeLayer(defaultLayer);

        return layer;
    }

    private static void handleElement(GpxImageEntry entry, Element attrElem) {
        try {
            switch(attrElem.getTagName()) {
            case "file":
                entry.setFile(new File(attrElem.getTextContent()));
                break;
            case "position":
                double lat = Double.parseDouble(attrElem.getAttribute("lat"));
                double lon = Double.parseDouble(attrElem.getAttribute("lon"));
                entry.setPos(new LatLon(lat, lon));
                break;
            default: // Do nothing
            }
            // TODO: handle thumbnail loading
        } catch (NumberFormatException e) {
            Logging.trace(e);
        }
    }

}

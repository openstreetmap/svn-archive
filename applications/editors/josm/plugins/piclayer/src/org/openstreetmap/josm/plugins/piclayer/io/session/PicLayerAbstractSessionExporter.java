package org.openstreetmap.josm.plugins.piclayer.io.session;

import java.awt.Component;
import java.awt.GridBagLayout;
import java.io.IOException;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.openstreetmap.josm.gui.layer.geoimage.ImageEntry;
import org.openstreetmap.josm.io.session.AbstractSessionExporter;
import org.openstreetmap.josm.io.session.SessionWriter;
import org.openstreetmap.josm.io.session.SessionWriter.ExportSupport;
import org.openstreetmap.josm.plugins.piclayer.layer.PicLayerAbstract;
import org.openstreetmap.josm.tools.GBC;
import org.openstreetmap.josm.tools.Logging;
import org.w3c.dom.Element;

/**
 * Session exporter for PicLayer.
 * Code copied and adjusted from JOSM GeoImageSessionExporter-class!
 * @param <T> Type of PicLayerAbstract
 * @author rebsc
 */
public abstract class PicLayerAbstractSessionExporter<T extends PicLayerAbstract> extends AbstractSessionExporter<T>{

    public PicLayerAbstractSessionExporter(T layer) {
        super(layer);
    }

	@Override
	public Component getExportPanel() {
		final JPanel p = new JPanel(new GridBagLayout());
        export.setSelected(true);
        final JLabel lbl = new JLabel(layer.getName(), layer.getIcon(), SwingConstants.LEFT);
        lbl.setToolTipText(layer.getToolTipText());
        lbl.setLabelFor(export);
        p.add(export, GBC.std());
        p.add(lbl, GBC.std());
        p.add(GBC.glue(1, 0), GBC.std().fill(GBC.HORIZONTAL));
        return p;
	}

	@Override
	public Element export(ExportSupport support) throws IOException {
		Element layerElem = support.createElement("layer");
        layerElem.setAttribute("type", "piclayerImage");
        layerElem.setAttribute("version", "0.1");

        for (ImageEntry entry : layer.getImages()) {

            Element imgElem = support.createElement("piclayerImage");

            if (entry.getFile() == null) {
                Logging.warn("No file attribute for image - skipping entry");
                break;
            }
            addAttr("file", entry.getFile().getPath(), imgElem, support);
            // FIXME: relative filenames as option

            addAttr("thumbnail", Boolean.toString(entry.hasThumbnail()), imgElem, support);
            if (entry.getPos() != null) {
                Element posElem = support.createElement("position");
                posElem.setAttribute("lat", Double.toString(entry.getPos().lat()));
                posElem.setAttribute("lon", Double.toString(entry.getPos().lon()));
                imgElem.appendChild(posElem);
            }

            layerElem.appendChild(imgElem);
        }
	    return layerElem;
	}

    protected static void addAttr(String name, String value, Element element, SessionWriter.ExportSupport support) {
        Element attrElem = support.createElement(name);
        attrElem.appendChild(support.createTextNode(value));
        element.appendChild(attrElem);
    }

}

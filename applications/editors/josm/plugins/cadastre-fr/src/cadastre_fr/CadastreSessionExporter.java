package cadastre_fr;

import java.awt.Component;
import java.awt.GridBagLayout;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.io.session.SessionLayerExporter;
import org.openstreetmap.josm.io.session.SessionWriter.ExportSupport;
import org.openstreetmap.josm.tools.GBC;
import org.w3c.dom.Element;

public class CadastreSessionExporter implements SessionLayerExporter {

    private WMSLayer layer;
    private JCheckBox export;

    public CadastreSessionExporter(WMSLayer layer) {
        this.layer = layer;
    }

	@Override
	public Collection<Layer> getDependencies() {
        return Collections.emptySet();
	}

	@Override
	public Component getExportPanel() {
        final JPanel p = new JPanel(new GridBagLayout());
        export = new JCheckBox();
        export.setSelected(true);
        final JLabel lbl = new JLabel(layer.getName(), layer.getIcon(), SwingConstants.LEFT);
        lbl.setToolTipText(layer.getToolTipText());
        p.add(export, GBC.std());
        p.add(lbl, GBC.std());
        p.add(GBC.glue(1,0), GBC.std().fill(GBC.HORIZONTAL));
        return p;
	}

	@Override
	public boolean shallExport() {
        return export.isSelected();
	}

	@Override
	public boolean requiresZip() {
		return false;
	}

	@Override
	public Element export(ExportSupport support) throws IOException {
        Element layerEl = support.createElement("layer");
        layerEl.setAttribute("type", "cadastre-fr");
        layerEl.setAttribute("version", "0.1");

        Element file = support.createElement("file");
        layerEl.appendChild(file);

        URI uri = layer.getAssociatedFile().toURI();
        URL url = null;
        try {
            url = uri.toURL();
        } catch (MalformedURLException e) {
            throw new IOException(e);
        }
        file.appendChild(support.createTextNode(url.toString()));
        return layerEl;
	}

}

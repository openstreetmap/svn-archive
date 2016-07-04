// License: GPL. For details, see LICENSE file.
package cadastre_fr;

import static org.openstreetmap.josm.tools.I18n.tr;

//import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
//import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.gui.layer.Layer;

public class WMSDownloadAction /*extends JosmAction */ {

//    public WMSDownloadAction(String layerName) {
//        super(layerName, "wmsmenu", tr("Download WMS tile from {0}",layerName), null, false);
//    }
//
//    public void actionPerformed(ActionEvent e) {
//        DownloadWMSVectorImage.download(getLayer());
//    }

    public static WMSLayer getLayer() {
        // check if we already have a layer created. if not, create; if yes, reuse.
        ArrayList<WMSLayer> existingWMSlayers = new ArrayList<>();
        if (Main.map != null) {
            Layer activeLayer = Main.getLayerManager().getActiveLayer();
            if (activeLayer instanceof WMSLayer)
                return (WMSLayer) activeLayer;
            for (Layer l : Main.getLayerManager().getLayers()) {
                if (l instanceof WMSLayer) {
                    existingWMSlayers.add((WMSLayer) l);
                }
            }
            if (existingWMSlayers.size() == 1)
                return existingWMSlayers.get(0);
            if (existingWMSlayers.size() == 0)
                return new MenuActionNewLocation().addNewLayer(existingWMSlayers);
            if (Main.pref.getBoolean("cadastrewms.autoFirstLayer", false)) {
                return existingWMSlayers.get(0);
            } else {
                JOptionPane.showMessageDialog(Main.parent,
                        tr("More than one WMS layer present\nSelect one of them first, then retry"));
            }
        } else {
            return new MenuActionNewLocation().addNewLayer(existingWMSlayers);
        }
        return null;
    }
}

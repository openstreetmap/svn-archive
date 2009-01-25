package cadastre_fr;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.util.ArrayList;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.gui.layer.Layer;

public class WMSDownloadAction extends JosmAction {

    private static final long serialVersionUID = 1L;

    //private String layerName;
	
	public WMSDownloadAction(String layerName) {
		super(layerName, "wmsmenu", tr("Download WMS tile from {0}",layerName), null, false);
	}
	
	public void actionPerformed(ActionEvent e) {		
		DownloadWMSTask.download(getLayer());
	}

	public static WMSLayer getLayer() {
		// check if we already have a layer created. if not, create; if yes, reuse.
        if (Main.map != null) {
            Layer activeLayer = Main.map.mapView.getActiveLayer();
            if (activeLayer instanceof WMSLayer)
                return (WMSLayer) activeLayer;
            ArrayList<WMSLayer> existingWMSlayers = new ArrayList<WMSLayer>();
            for (Layer l : Main.map.mapView.getAllLayers()) {
                if (l instanceof WMSLayer) {
                    existingWMSlayers.add((WMSLayer)l);
                }
            }
            if (existingWMSlayers.size() == 1)
                return existingWMSlayers.get(0);
            return new MenuActionNewLocation().addNewLayer(existingWMSlayers);
        }
        return null;
	}
};


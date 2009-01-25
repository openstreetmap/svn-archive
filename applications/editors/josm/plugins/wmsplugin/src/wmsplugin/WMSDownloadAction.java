package wmsplugin;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.data.Bounds;

public class WMSDownloadAction extends JosmAction {

    private WMSInfo info;

    public WMSDownloadAction(WMSInfo info) {
        super(info.name, "wmsmenu", tr("Download WMS tile from {0}",info.name), null, false);
        putValue("toolbar", "wms_" + info.name);
        this.info = info;
    }

    public void actionPerformed(ActionEvent e) {
        System.out.println(info.url);

        WMSLayer wmsLayer = new WMSLayer(info.name, info.url);
        Main.main.addLayer(wmsLayer);
    }

    public static WMSLayer getLayer(WMSInfo info) {
        // FIXME: move this to WMSPlugin/WMSInfo/preferences.
        WMSLayer wmsLayer = new WMSLayer(info.name, info.url);
        Main.main.addLayer(wmsLayer);
        return wmsLayer;
    }
};

package wmsplugin;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;

public class WMSDownloadAction extends JosmAction {

    private final WMSInfo info;

    public WMSDownloadAction(WMSInfo info) {
        super(info.getMenuName(), "wmsmenu", tr("Download WMS tile from {0}",info.name), null, false);
        putValue("toolbar", "wms_" + info.getToolbarName());
        this.info = info;
    }

    public void actionPerformed(ActionEvent e) {
        WMSLayer wmsLayer = new WMSLayer(info);
        Main.main.addLayer(wmsLayer);
    }
};

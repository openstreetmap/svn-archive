package wmsplugin;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.gui.MapView;

public class Map_Rectifier_WMSmenuAction extends JosmAction {

    /**
     * tim waters "chippy"
     */
    private static final long serialVersionUID = 1L;

    public Map_Rectifier_WMSmenuAction() {
        super(tr("Rectified Image ..."), "OLmarker", tr("Download Rectified Image from Metacarta's Map Rectifier WMS"), null, false);
    }

    public void actionPerformed(ActionEvent e) {
        String newid = JOptionPane.showInputDialog(Main.parent, tr("Metacarta Map Rectifier image id"),
        Main.pref.get("wmsplugin.rectifier_id"));

        if (newid != null && !newid.equals("")) {
            String newURL = "http://labs.metacarta.com/rectifier/wms.cgi?id="+newid+
            "&srs=EPSG:4326&Service=WMS&Version=1.1.0&Request=GetMap&format=image/png";

            Main.pref.put("wmsplugin.rectifier_id", newid);
            WMSLayer wmsLayer = new WMSLayer(tr("rectifier id={0}",newid), newURL);
            Main.main.addLayer(wmsLayer);
        }
    }
}

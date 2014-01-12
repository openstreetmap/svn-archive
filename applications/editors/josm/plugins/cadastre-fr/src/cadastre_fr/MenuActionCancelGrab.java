package cadastre_fr;

import static org.openstreetmap.josm.tools.I18n.tr;
import static org.openstreetmap.josm.tools.I18n.marktr;

import java.awt.event.ActionEvent;

import org.openstreetmap.josm.actions.JosmAction;

@SuppressWarnings("serial")
public class MenuActionCancelGrab extends JosmAction {

    public static String name = marktr("Cancel current grab");

    private WMSLayer wmsLayer;
    
    public MenuActionCancelGrab(WMSLayer wmsLayer) {
        super(tr(name), null, tr("Cancel current grab (only vector images)"), null, false);
        this.wmsLayer = wmsLayer;
    }


    @Override
    public void actionPerformed(ActionEvent arg0) {
        if (wmsLayer.grabThread.getImagesToGrabSize() > 0) {
            wmsLayer.grabThread.cancel();
        }
    }

}

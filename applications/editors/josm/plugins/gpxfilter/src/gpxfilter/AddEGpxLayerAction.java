package gpxfilter;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.tools.Shortcut;

@SuppressWarnings("serial")
public class AddEGpxLayerAction extends JosmAction {

    public AddEGpxLayerAction() {
        super(tr("Add EGPX layer"),(String)null,tr("Add EGPX layer"),
            Shortcut.registerShortcut("gpxfilter:egpx", tr("Tool: {0}", tr("Add EGPX layer")),
                 KeyEvent.VK_X, Shortcut.ALT_SHIFT),
            true, "gpxfilter/addegpxlayer", true);
    }

    @Override
    protected void updateEnabledState() {
        setEnabled(getCurrentDataSet() != null);
    }

    @Override
	public void actionPerformed(ActionEvent arg0)  {
        Main.main.addLayer(new EGpxLayer(Main.map.mapView.getRealBounds()));
    }
}
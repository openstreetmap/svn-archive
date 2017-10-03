package ext_tools;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import org.openstreetmap.josm.actions.mapmode.MapMode;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.Shortcut;

// TODO: Merge with ExtTool class?
class ExtToolAction extends MapMode {

    protected MapMode oldMapMode;
    protected ExtTool tool;

    public ExtToolAction(ExtTool tool) {
        super(tr(tool.name), "empty", tool.description,
            Shortcut.registerShortcut(tr("exttool:{0}", tool.name), tr("External Tool: {0}", tool.name), 
                KeyEvent.CHAR_UNDEFINED, Shortcut.NONE),
            ImageProvider.getCursor("crosshair", null));
        this.tool = tool;
        setEnabled(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!MainApplication.isDisplayingMapView())
            return;
        oldMapMode = MainApplication.getMap().mapMode;
        super.actionPerformed(e);
    }

    @Override
    public void enterMode() {
        super.enterMode();
        MainApplication.getMap().mapView.addMouseListener(this);
    }

    @Override
    public void exitMode() {
        super.exitMode();
        MainApplication.getMap().mapView.removeMouseListener(this);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (!MainApplication.isDisplayingMapView()) {
            return;
        }

        tool.runTool(MainApplication.getMap().mapView.getLatLon(e.getX(), e.getY()));
        MainApplication.getMap().selectMapMode(oldMapMode);
    }

    @Override
    public boolean layerIsSupported(Layer l) {
        return l instanceof OsmDataLayer;
    }
}

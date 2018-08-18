// License: GPL. For details, see LICENSE file.
package indoor_sweepline;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.projection.ProjectionRegistry;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.LayerManager.LayerAddEvent;
import org.openstreetmap.josm.gui.layer.LayerManager.LayerChangeListener;
import org.openstreetmap.josm.gui.layer.LayerManager.LayerOrderChangeEvent;
import org.openstreetmap.josm.gui.layer.LayerManager.LayerRemoveEvent;
import org.openstreetmap.josm.gui.layer.MainLayerManager.ActiveLayerChangeEvent;
import org.openstreetmap.josm.gui.layer.MainLayerManager.ActiveLayerChangeListener;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;

public class IndoorSweeplineWizardAction extends JosmAction implements LayerChangeListener, ActiveLayerChangeListener {

    public IndoorSweeplineWizardAction() {
        super(tr("Concourse wizard ..."), null,
                tr("Opens up a wizard to create a concourse"), null, false);
        MainApplication.getLayerManager().addLayerChangeListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        if (layer == null)
            JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(MainApplication.getMainFrame()),
                    "No default layer found.");
        else if (!(layer instanceof OsmDataLayer))
            JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(MainApplication.getMainFrame()),
                    "The default layer is not an OSM layer.");
        else if (MainApplication.getMap() == null)
            JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(MainApplication.getMainFrame()),
                    "No map found.");
        else if (MainApplication.getMap().mapView == null)
            JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(MainApplication.getMainFrame()),
                    "No map view found.");
        else
            new IndoorSweeplineController((OsmDataLayer) layer,
                    ProjectionRegistry.getProjection().eastNorth2latlon(MainApplication.getMap().mapView.getCenter()));
    }

    @Override
    public void activeOrEditLayerChanged(ActiveLayerChangeEvent e) {
        layer = MainApplication.getLayerManager().getActiveLayer();
    }

    @Override
    public void layerOrderChanged(LayerOrderChangeEvent e) {
    }

    @Override
    public void layerAdded(LayerAddEvent e) {
    }

    @Override
    public void layerRemoving(LayerRemoveEvent e) {
        if (layer == e.getRemovedLayer())
            layer = null;
    }

    private Layer layer;
}

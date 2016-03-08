package indoor_sweepline;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import javax.swing.JOptionPane;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.projection.Projections;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.gui.MapView;


public class IndoorSweeplineWizardAction extends JosmAction implements MapView.LayerChangeListener
{
    public IndoorSweeplineWizardAction()
    {
	super(tr("Concourse wizard ..."), null,
	    tr("Opens up a wizard to create a concourse"), null, false);
	MapView.addLayerChangeListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent event)
    {
	if (layer == null)
	    JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(Main.parent),
		"No default layer found.");
	else if (!(layer instanceof OsmDataLayer))
	    JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(Main.parent),
		"The default layer is not an OSM layer.");
	else if (Main.map == null)
	    JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(Main.parent),
		"No map found.");
	else if (Main.map.mapView == null)
	    JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(Main.parent),
		"No map view found.");
	else
	    new IndoorSweeplineController((OsmDataLayer)layer,
		Projections.inverseProject(Main.map.mapView.getCenter()));
    }

    
    @Override
    public void activeLayerChange(Layer oldLayer, Layer newLayer)
    {
	layer = newLayer;
    }

    @Override
    public void layerAdded(Layer newLayer)
    {
    }

    @Override
    public void layerRemoved(Layer oldLayer)
    {
	if (layer == oldLayer)
	    layer = null;
    }
        
    private Layer layer;
}

package indoor_sweepline;

import java.util.List;
import javax.swing.DefaultComboBoxModel;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.gui.MapView;


public class IndoorSweeplineController implements MapView.LayerChangeListener
{
    public IndoorSweeplineController(OsmDataLayer activeLayer, LatLon center)
    {
	MapView.addLayerChangeListener(this);
	layer = activeLayer;
	model = new IndoorSweeplineModel(activeLayer, center);
	dialog = new IndoorSweeplineWizardDialog(this);
	dialog.setVisible(true);
    }


    public IndoorSweeplineWizardDialog view()
    {
	return dialog;
    }

    
    @Override
    public void activeLayerChange(Layer oldLayer, Layer newLayer)
    {
    }

    @Override
    public void layerAdded(Layer newLayer)
    {
    }

    @Override
    public void layerRemoved(Layer oldLayer)
    {
	if (oldLayer == layer)
	    dialog.setVisible(false);
    }
    
    
    public int leftRightCount()
    {
	return model.leftRightCount();
    }
    
    public void addRightStructure()
    {
	if (model.leftRightCount() % 2 == 0)
	    model.addBeam();
	else
	    model.addStrip();
    }
    
    public DefaultComboBoxModel<String> structures()
    {
	return model.structures();
    }
    
    public double getStripWidth(int index)
    {
	return model.getStripWidth(index);
    }
    
    public void setStripWidth(int index, double value)
    {
	model.setStripWidth(index, value);
    }
    
    public List<CorridorPart> getBeamParts(int index)
    {
	return model.getBeamParts(index);
    }
    
    public void addCorridorPart(int beamIndex, boolean append, double value)
    {
	model.addCorridorPart(beamIndex, append, value);
    }
    
    public void setCorridorPartWidth(int beamIndex, int partIndex, double value)
    {
	model.setCorridorPartWidth(beamIndex, partIndex, value);
    }
    
    public void setCorridorPartType(int beamIndex, int partIndex, CorridorPart.Type type)
    {
	model.setCorridorPartType(beamIndex, partIndex, type);
    }
    
    public void setCorridorPartSide(int beamIndex, int partIndex, CorridorPart.ReachableSide side)
    {
	model.setCorridorPartSide(beamIndex, partIndex, side);
    }
    
    public Strip getStrip(int beamIndex)
    {
	return model.getStrip(beamIndex);
    }
        
    public IndoorSweeplineModel.Type getType()
    {
	return model.getType();
    }
    
    public void setType(IndoorSweeplineModel.Type type)
    {
	model.setType(type);
    }
    
    public String getLevel()
    {
	return model.getLevel();
    }
    
    public void setLevel(String level)
    {
	model.setLevel(level);
    }
    
    private OsmDataLayer layer;
    private IndoorSweeplineModel model;
    private IndoorSweeplineWizardDialog dialog;
}

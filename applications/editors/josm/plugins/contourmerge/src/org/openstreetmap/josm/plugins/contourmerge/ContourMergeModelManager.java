package org.openstreetmap.josm.plugins.contourmerge;

import java.util.HashMap;
import java.util.Map;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.MapView.LayerChangeListener;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.plugins.contourmerge.util.Assert;

/**
 * <p>Manages a set of {@link ContourMergeModel}s for each available data layer.</p>
 * 
 * <p>Listens to layer change events and creates new contour merge models for newly added layer, or
 * removes contour merge models, if a layer is deleted.</p>
 */
public class ContourMergeModelManager implements LayerChangeListener{
	
	static private ContourMergeModelManager instance;
	static public ContourMergeModelManager getInstance() {
		if (instance == null){
			instance = new ContourMergeModelManager();
		}
		return instance;
	}
	
	private final Map<OsmDataLayer, ContourMergeModel> models = new HashMap<OsmDataLayer, ContourMergeModel>();
	
	public void wireToJOSM(){
		models.clear();
		MapView.addLayerChangeListener(this);
	}
	
	public void unwireFromJOSM() {
		models.clear();
		MapView.removeLayerChangeListener(this);
	}
	
	/**
	 * <p>Replies the contour merge model for the data layer {@code layer}, or null, if no
	 * such model exists.</p>
	 * 
	 * @param layer the data layer. Must not be null.
	 * @return the model
	 * @throws IllegalArgumentException thrown if {@code layer} is null
	 */
	public ContourMergeModel getModel(OsmDataLayer layer) throws IllegalArgumentException{
		Assert.checkArgNotNull(layer, "layer");
		return models.get(layer);
	}
	
	/**
	 * <p>Replies the contour model for the currently active data layer (the "edit layer"), or null,
	 * if the currently active layer isn't a data layer.</p>
	 * 
	 * @return the model
	 */
	public ContourMergeModel getActiveModel() {
		if (Main.map == null) return null;
		if (Main.map.mapView == null) return null;
		OsmDataLayer layer = Main.map.mapView.getEditLayer();
		if (layer == null) return null;
		return getModel(layer);
	}
	
	
	/* ----------------------------------------------------------------------------------- */
	/* interface LayerChangeListener                                                       */
	/* ----------------------------------------------------------------------------------- */
	@Override
	public void activeLayerChange(Layer oldLayer, Layer newLayer) {/* ignore */}
	@Override
	
	public void layerAdded(Layer newLayer) {
		if (! (newLayer instanceof OsmDataLayer)) return;
		OsmDataLayer dl = (OsmDataLayer)newLayer;
		ContourMergeModel model = new  ContourMergeModel(dl);
		dl.data.addDataSetListener(model);		
		models.put((OsmDataLayer)newLayer, model);
	}
	
	@Override
	public void layerRemoved(Layer oldLayer) {
		if (! (oldLayer instanceof OsmDataLayer)) return;
		OsmDataLayer dl = (OsmDataLayer)oldLayer;
		ContourMergeModel model = models.get(dl);
		dl.data.removeDataSetListener(model);
		models.remove(dl);		
	}
}

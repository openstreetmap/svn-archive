// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.imageryxmlbounds.actions;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.io.File;

import org.openstreetmap.josm.actions.SaveAsAction;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.Layer.LayerSaveAsAction;
import org.openstreetmap.josm.plugins.imageryxmlbounds.XmlBoundsConstants;
import org.openstreetmap.josm.plugins.imageryxmlbounds.XmlBoundsLayer;

/**
 * 
 * @author Don-vip
 *
 */
@SuppressWarnings("serial")
public class BoundsLayerSaveAsAction extends LayerSaveAsAction {

	public static class SaveBoundsAsAction extends SaveAsAction {

		@Override public File getFile(Layer layer) {
	        return openFileDialog(layer);
	    }
		
	    public static File openFileDialog(Layer layer) {
	    	if (layer instanceof XmlBoundsLayer) {
	    		return createAndOpenSaveFileChooser(tr("Save Imagery XML file"), XmlBoundsConstants.EXTENSION);
	    	} else {
	    		return null;
	    	}
	    }
	}
	
	protected XmlBoundsLayer layer;
	
	public BoundsLayerSaveAsAction(XmlBoundsLayer layer) {
		super(layer);
		this.layer = layer;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		new SaveBoundsAsAction().doSave(layer);
	}
}

//    JOSM Imagery XML Bounds plugin.
//    Copyright (C) 2011 Don-vip
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with this program.  If not, see <http://www.gnu.org/licenses/>.
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

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.gui.layer.Layer.LayerSaveAsAction#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		new SaveBoundsAsAction().doSave(layer);
	}
}

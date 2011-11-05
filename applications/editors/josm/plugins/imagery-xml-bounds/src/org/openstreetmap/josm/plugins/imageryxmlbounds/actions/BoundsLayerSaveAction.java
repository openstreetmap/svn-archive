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

import java.awt.event.ActionEvent;
import java.io.File;

import org.openstreetmap.josm.actions.SaveAction;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.Layer.LayerSaveAction;
import org.openstreetmap.josm.plugins.imageryxmlbounds.XmlBoundsLayer;

/**
 * 
 * @author Don-vip
 *
 */
@SuppressWarnings("serial")
public class BoundsLayerSaveAction extends LayerSaveAction {

	public static class SaveBoundsAction extends SaveAction {
		
		@Override public File getFile(Layer layer) {
	        File f = layer.getAssociatedFile();
	        if (f != null && ! f.exists()) {
	            f = null;
	        }
	        return f == null ? BoundsLayerSaveAsAction.SaveBoundsAsAction.openFileDialog(layer) : f;
	    }
	}

	protected XmlBoundsLayer layer;
	
	public BoundsLayerSaveAction(XmlBoundsLayer layer) {
		super(layer);
	}

    public void actionPerformed(ActionEvent e) {
        new SaveBoundsAction().doSave(layer);
    }
}

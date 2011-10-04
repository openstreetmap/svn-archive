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
package org.openstreetmap.josm.plugins.imageryxmlbounds;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Action;
import javax.swing.Icon;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.MapView.LayerChangeListener;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.plugins.imageryxmlbounds.actions.BoundsLayerSaveAction;
import org.openstreetmap.josm.plugins.imageryxmlbounds.actions.BoundsLayerSaveAsAction;
import org.openstreetmap.josm.plugins.imageryxmlbounds.actions.ShowBoundsAction;

/**
 * An "OSM data" layer that cannot be uploaded, merged, and in which real OSM data cannot be imported.
 * Its sole purpose is to allow "classic" OSM edition tools to edit Imagery bounds (as XML files) without compromising OSM database integrity. 
 * 
 * @author Don-vip
 */
public class XmlBoundsLayer extends OsmDataLayer implements LayerChangeListener, XmlBoundsConstants {

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.gui.layer.OsmDataLayer#getMenuEntries()
	 */
	@Override
	public Action[] getMenuEntries() {
		List<Action> result = new ArrayList<Action>();
		for (Action action : super.getMenuEntries()) {
			if (action instanceof LayerSaveAction) {
				result.add(new BoundsLayerSaveAction(this));

			} else if (action instanceof LayerSaveAsAction) {
				result.add(new BoundsLayerSaveAsAction(this));
				
			} else if (!(action instanceof LayerGpxExportAction) && !(action instanceof ConvertToGpxLayerAction)) {
				// Add everything else, expect GPX-related action
				result.add(action);
			}
		}
		result.add(new ShowBoundsAction(this));
		return result.toArray(new Action[0]);
	}

	private static final JosmAction[] actionsToDisable = new JosmAction[] {
		Main.main.menu.download,
		Main.main.menu.downloadPrimitive,
		Main.main.menu.downloadReferrers,
		Main.main.menu.upload,
		Main.main.menu.uploadSelection,
		Main.main.menu.update,
		Main.main.menu.updateModified,
		Main.main.menu.updateSelection,
		Main.main.menu.openLocation
	};
	
	private static final Map<JosmAction, Boolean> actionsStates = new HashMap<JosmAction, Boolean>();
	
	public XmlBoundsLayer(DataSet data, String name, File associatedFile) {
		super(data, name, associatedFile);
		MapView.addLayerChangeListener(this);
	}
	
	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.gui.layer.OsmDataLayer#isMergable(org.openstreetmap.josm.gui.layer.Layer)
	 */
	@Override
	public boolean isMergable(Layer other) {
		return other instanceof XmlBoundsLayer;
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.gui.layer.OsmDataLayer#getIcon()
	 */
	@Override
	public Icon getIcon() {
		return XML_ICON_16;
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.gui.layer.OsmDataLayer#requiresUploadToServer()
	 */
	@Override
	public boolean requiresUploadToServer() {
		return false; // Never !
	}

	@Override
	public void activeLayerChange(Layer oldLayer, Layer newLayer) {
		if (newLayer == this && !(oldLayer instanceof XmlBoundsLayer)) {
			for (JosmAction action : actionsToDisable) {
				actionsStates.put(action, action.isEnabled());
				action.setEnabled(false);
			}
		} else if (oldLayer == this && !(newLayer instanceof XmlBoundsLayer)) {
			for (JosmAction action : actionsToDisable) {
				action.setEnabled(actionsStates.get(action));
			}
		}
	}

	@Override
	public void layerAdded(Layer newLayer) {
	}

	@Override
	public void layerRemoved(Layer oldLayer) {
	    if (Main.main.getEditLayer() instanceof XmlBoundsLayer) {
	        for (JosmAction action : actionsToDisable) {
                action.setEnabled(false);
            }
	    }
	}
}

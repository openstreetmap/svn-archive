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

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.ExtensionFileFilter;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.plugins.imageryxmlbounds.actions.ShowBoundsListAction;
import org.openstreetmap.josm.plugins.imageryxmlbounds.actions.ShowBoundsPropertiesAction;
import org.openstreetmap.josm.plugins.imageryxmlbounds.actions.ShowBoundsSelectionAction;
import org.openstreetmap.josm.plugins.imageryxmlbounds.actions.downloadtask.DownloadXmlBoundsTask;
import org.openstreetmap.josm.plugins.imageryxmlbounds.io.XmlBoundsExporter;
import org.openstreetmap.josm.plugins.imageryxmlbounds.io.XmlBoundsImporter;

/**
 * Main class of Imagery XML bounds plugin.
 * @author Don-vip
 * @version 1.2
 * History:
 * 1.2 17-Oct-2011 Update for #6960 and JOSM 4523 (allow to download imagery XML bounds with Ctrl-L)
 * 1.1 08-Oct-2011 Update for #6934 and JOSM 4506, code refactorisation, removing debug code
 * 1.0 03-Oct-2011 first version
 */
public class ImageryXmlBoundsPlugin extends Plugin {

    /**
     * Action showing bounds of the selected closed ways in Selection dialog 
     */
	private final ShowBoundsListAction selectionListAction = new ShowBoundsListAction();

    /**
     * Action showing bounds of the selected multipolygons in Properties dialog 
     */
    private final ShowBoundsPropertiesAction propertiesListAction = new ShowBoundsPropertiesAction();
    
	/**
     * Action showing bounds of the selected multipolygons in Relations dialog
     */
	private final ShowBoundsListAction relationListAction = new ShowBoundsListAction();

    /**
     * Action showing bounds of the current selection
     */
	private final ShowBoundsSelectionAction selectionAction = new ShowBoundsSelectionAction();
	
	/**
	 * Initializes the plugin.
	 * @param info
	 */
	public ImageryXmlBoundsPlugin(PluginInformation info) {
		super(info);
		// Allow JOSM to import *.imagery.xml files
		ExtensionFileFilter.importers.add(0, new XmlBoundsImporter());
        // Allow JOSM to export *.imagery.xml files
		ExtensionFileFilter.exporters.add(0, new XmlBoundsExporter());
        // Initialize the selection action
		DataSet.addSelectionListener(selectionAction);
		Main.toolbar.register(selectionAction);
		// Allow JOSM to download *.imagery.xml files
		Main.main.menu.openLocation.addDownloadTaskClass(DownloadXmlBoundsTask.class);
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.Plugin#mapFrameInitialized(org.openstreetmap.josm.gui.MapFrame, org.openstreetmap.josm.gui.MapFrame)
	 */
	@Override
	public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
		if (newFrame != null) {
		    // Initialize dialogs actions only after the main frame is created 
            newFrame.selectionListDialog.addPopupMenuSeparator();
            newFrame.selectionListDialog.addPopupMenuAction(selectionListAction);
            newFrame.propertiesDialog.addMembershipPopupMenuSeparator();
            newFrame.propertiesDialog.addMembershipPopupMenuAction(propertiesListAction);
			newFrame.relationListDialog.addPopupMenuSeparator();
			newFrame.relationListDialog.addPopupMenuAction(relationListAction);
		}
	}
}

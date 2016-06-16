// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.imageryxmlbounds;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.ExtensionFileFilter;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.plugins.imageryxmlbounds.actions.ShowBoundsAction;
import org.openstreetmap.josm.plugins.imageryxmlbounds.actions.ShowBoundsSelectionAction;
import org.openstreetmap.josm.plugins.imageryxmlbounds.actions.downloadtask.DownloadXmlBoundsTask;
import org.openstreetmap.josm.plugins.imageryxmlbounds.io.XmlBoundsExporter;
import org.openstreetmap.josm.plugins.imageryxmlbounds.io.XmlBoundsImporter;

/**
 * Main class of Imagery XML bounds plugin.
 * @author Don-vip
 * @version 1.3
 * History:
 * 1.3 05-Nov-2011 Update for JOSM 4577 (allow to edit selected default imagery entries from Preferences dialog)
 * 1.2 17-Oct-2011 Update for #6960 and JOSM 4523 (allow to download imagery XML bounds with Ctrl-L)
 * 1.1 08-Oct-2011 Update for #6934 and JOSM 4506, code refactorisation, removing debug code
 * 1.0 03-Oct-2011 first version
 */
public class ImageryXmlBoundsPlugin extends Plugin {

    /**
     * Action showing bounds of the selected closed ways in Selection dialog
     */
    private final ShowBoundsAction selectionListAction = new ShowBoundsAction();

    /**
     * Action showing bounds of the selected multipolygons in Properties dialog
     */
    private final ShowBoundsAction propertiesListAction = new ShowBoundsAction();

    /**
     * Action showing bounds of the selected multipolygons in Relations dialog
     */
    private final ShowBoundsAction relationListAction = new ShowBoundsAction();

    /**
     * Action showing bounds of the current selection
     */
    private final ShowBoundsSelectionAction selectionAction = new ShowBoundsSelectionAction();

    /**
     * Class modifying the Imagery preferences panel
     */
    private final XmlBoundsPreferenceSetting preferenceSetting = new XmlBoundsPreferenceSetting();

    /**
     * Initializes the plugin.
     * @param info
     */
    public ImageryXmlBoundsPlugin(PluginInformation info) {
        super(info);
        // Allow JOSM to import *.imagery.xml files
        ExtensionFileFilter.addImporterFirst(new XmlBoundsImporter());
        // Allow JOSM to export *.imagery.xml files
        ExtensionFileFilter.addExporterFirst(new XmlBoundsExporter());
        // Initialize the selection action
        DataSet.addSelectionListener(selectionAction);
        Main.toolbar.register(selectionAction);
        // Allow JOSM to download *.imagery.xml files
        Main.main.menu.openLocation.addDownloadTaskClass(DownloadXmlBoundsTask.class);
    }

    @Override
    public PreferenceSetting getPreferenceSetting() {
        return this.preferenceSetting;
    }

    @Override
    public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
        if (newFrame != null) {
            // Initialize dialogs actions only after the main frame is created
            newFrame.selectionListDialog.getPopupMenuHandler().addSeparator();
            newFrame.selectionListDialog.getPopupMenuHandler().addAction(selectionListAction);
            newFrame.propertiesDialog.getMembershipPopupMenuHandler().addSeparator();
            newFrame.propertiesDialog.getMembershipPopupMenuHandler().addAction(propertiesListAction);
            newFrame.relationListDialog.getPopupMenuHandler().addSeparator();
            newFrame.relationListDialog.getPopupMenuHandler().addAction(relationListAction);
        } else if (oldFrame != null) {
            // Remove listeners from previous frame to avoid memory leaks
            if (oldFrame.relationListDialog != null) {
                oldFrame.relationListDialog.getPopupMenuHandler().removeAction(relationListAction);
            }
            if (oldFrame.propertiesDialog != null) {
                oldFrame.propertiesDialog.getMembershipPopupMenuHandler().removeAction(propertiesListAction);
            }
            if (oldFrame.selectionListDialog != null) {
                oldFrame.selectionListDialog.getPopupMenuHandler().removeAction(selectionListAction);
            }
        }
    }
}

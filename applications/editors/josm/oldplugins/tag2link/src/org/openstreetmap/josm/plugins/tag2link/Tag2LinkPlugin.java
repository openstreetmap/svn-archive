//    JOSM tag2link plugin.
//    Copyright (C) 2011-2012 Don-vip & FrViPofm
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
package org.openstreetmap.josm.plugins.tag2link;

import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.plugins.tag2link.listeners.MembershipPopupListener;
import org.openstreetmap.josm.plugins.tag2link.listeners.PropertyPopupListener;
import org.openstreetmap.josm.plugins.tag2link.listeners.RelationPopupListener;
import org.openstreetmap.josm.plugins.tag2link.listeners.SelectionPopupListener;

/**
 * Main class of tag2link plugin.
 * @author Don-vip
 */
public class Tag2LinkPlugin extends Plugin {

    private SelectionPopupListener selectionPopupListener;
    private MembershipPopupListener membershipPopupListener;
    private PropertyPopupListener propertyPopupListener;
    private RelationPopupListener relationPopupListener;
    
    /**
     * Initializes the plugin.
     * @param info The plugin info provided by JOSM
     */
    public Tag2LinkPlugin(PluginInformation info) {
        super(info);
        Tag2LinkRuleChecker.init();
    }

    @Override
    public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
        if (newFrame != null) {
            // Initialize dialogs listeners only after the main frame is created
            newFrame.selectionListDialog.getPopupMenuHandler().addListener(selectionPopupListener = new SelectionPopupListener(newFrame));
            newFrame.propertiesDialog.getMembershipPopupMenuHandler().addListener(membershipPopupListener = new MembershipPopupListener(newFrame));
            newFrame.propertiesDialog.getPropertyPopupMenuHandler().addListener(propertyPopupListener = new PropertyPopupListener(newFrame));
            newFrame.relationListDialog.getPopupMenuHandler().addListener(relationPopupListener = new RelationPopupListener(newFrame));
        } else if (oldFrame != null) {
            // Remove listeners from previous frame to avoid memory leaks
            if (oldFrame.selectionListDialog != null) {
                oldFrame.selectionListDialog.getPopupMenuHandler().removeListener(selectionPopupListener);
            }
            if (oldFrame.propertiesDialog != null) {
                oldFrame.propertiesDialog.getMembershipPopupMenuHandler().removeListener(membershipPopupListener);
                oldFrame.propertiesDialog.getPropertyPopupMenuHandler().removeListener(propertyPopupListener);
            }
            if (oldFrame.relationListDialog != null) {
                oldFrame.relationListDialog.getPopupMenuHandler().removeListener(relationPopupListener);
            }
            selectionPopupListener = null;
            membershipPopupListener = null;
            propertyPopupListener = null;
            relationPopupListener = null;
        }
    }
}

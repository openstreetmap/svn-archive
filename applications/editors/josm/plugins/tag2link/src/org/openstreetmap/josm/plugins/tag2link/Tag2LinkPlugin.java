//    JOSM tag2link plugin.
//    Copyright (C) 2011 Don-vip & FrViPofm
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
import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.plugins.tag2link.listeners.MembershipPopupListener;
import org.openstreetmap.josm.plugins.tag2link.listeners.PropertyPopupListener;
import org.openstreetmap.josm.plugins.tag2link.listeners.RelationPopupListener;
import org.openstreetmap.josm.plugins.tag2link.listeners.SelectionPopupListener;

/**
 * Main class of tag2link plugin.
 * @author Don-vip
 * History:
 * 0.2d 24-Oct-2011 Icon for Mail action
 * 0.2c 24-Oct-2011 Another MHS rule
 * 0.2b 24-Oct-2011 UNESCO WHC rule working
 * 0.2a 23-Oct-2011 add Mail support + initial work on UNESCO WHC
 * 0.1c 23-Oct-2011 add MHS rule (French heritage)
 * 0.1b 22-Oct-2011 add CEF rule (French christian churches)
 * 0.1  22-Oct-2011 first working prototype
 */
public class Tag2LinkPlugin extends Plugin {

    private Tag2LinkPreferenceSetting preferenceSetting;
    
	/**
	 * Initializes the plugin.
	 * @param info
	 */
	public Tag2LinkPlugin(PluginInformation info) {
		super(info);
		this.preferenceSetting = new Tag2LinkPreferenceSetting();
		Tag2LinkRuleChecker.init();
	}

    /* (non-Javadoc)
     * @see org.openstreetmap.josm.plugins.Plugin#getPreferenceSetting()
     */
    @Override
    public PreferenceSetting getPreferenceSetting() {
        return this.preferenceSetting;
    }
    
    /* (non-Javadoc)
     * @see org.openstreetmap.josm.plugins.Plugin#mapFrameInitialized(org.openstreetmap.josm.gui.MapFrame, org.openstreetmap.josm.gui.MapFrame)
     */
    @Override
    public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
        if (newFrame != null) {
            // Initialize dialogs listeners only after the main frame is created 
            newFrame.selectionListDialog.addPopupMenuListener(new SelectionPopupListener(newFrame));
            newFrame.propertiesDialog.addMembershipPopupMenuListener(new MembershipPopupListener(newFrame));
            newFrame.propertiesDialog.addPropertyPopupMenuListener(new PropertyPopupListener(newFrame));
            newFrame.relationListDialog.addPopupMenuListener(new RelationPopupListener(newFrame));
        }
    }
}

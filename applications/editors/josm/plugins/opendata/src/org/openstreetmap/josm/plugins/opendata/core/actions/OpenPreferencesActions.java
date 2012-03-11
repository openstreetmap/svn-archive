//    JOSM opendata plugin.
//    Copyright (C) 2011-2012 Don-vip
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
package org.openstreetmap.josm.plugins.opendata.core.actions;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.gui.preferences.PreferenceDialog;
import org.openstreetmap.josm.plugins.opendata.core.OdConstants;
import org.openstreetmap.josm.plugins.opendata.core.gui.OdPreferenceSetting;
import org.openstreetmap.josm.tools.ImageProvider;

public class OpenPreferencesActions extends JosmAction implements OdConstants {
	
    public OpenPreferencesActions() {
    	super(false);
        putValue(NAME, tr("Preferences"));
        putValue(SMALL_ICON, ImageProvider.get("dialogs", ICON_CORE_24));
		putValue("toolbar", "opendata_open_preferences");
    }
    
	@Override
	public void actionPerformed(ActionEvent e) {
        final PreferenceDialog p = new PreferenceDialog(Main.parent);
        p.selectPreferencesTabByClass(OdPreferenceSetting.class);
        p.setVisible(true);
	}
}

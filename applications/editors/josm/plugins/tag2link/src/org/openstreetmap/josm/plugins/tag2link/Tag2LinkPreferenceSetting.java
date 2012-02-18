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

import static org.openstreetmap.josm.tools.I18n.tr;

import org.openstreetmap.josm.gui.preferences.DefaultTabPreferenceSetting;
import org.openstreetmap.josm.gui.preferences.PreferenceTabbedPane;

public class Tag2LinkPreferenceSetting extends DefaultTabPreferenceSetting implements Tag2LinkConstants {

    public Tag2LinkPreferenceSetting() {
        super(ICON_48, tr("Tag2Link Preferences"), tr("Tag2Link Preferences"));
    }

    @Override
    public void addGui(PreferenceTabbedPane gui) {
        /*JPanel p = gui.createPreferenceTab(ICON_48, tr("Tag2Link Preferences"),
                tr("Tag2Link Preferences"), false);*/
        // TODO
    }

    @Override
    public boolean ok() {
        return false;
    }
}

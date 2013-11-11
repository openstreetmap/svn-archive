/**
 *  Tracer2 - plug-in for JOSM to capture contours
 *  
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.openstreetmap.josm.plugins.tracer2.preferences;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.GridBagConstraints;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.openstreetmap.josm.gui.preferences.DefaultTabPreferenceSetting;
import org.openstreetmap.josm.gui.preferences.PreferenceTabbedPane;
import org.openstreetmap.josm.plugins.tracer2.TracerPlugin;
import org.openstreetmap.josm.tools.GBC;

public class ServerParamPreference extends DefaultTabPreferenceSetting {
	
	TracerPlugin m_oPlugin;
	
    public ServerParamPreference(TracerPlugin plugin) {
    	super("tracer2", tr("Tracer2") + " - " + tr("Preferences"), tr("Modify list of parameter for server request."));

        m_oPlugin = plugin;
    }
    
    @Override
    public void addGui(PreferenceTabbedPane gui) {
        JPanel p = gui.createPreferenceTab(this);
        ServerParamPanel spp = new ServerParamPanel(m_oPlugin.m_oParamList);
        spp.refresh();
        JScrollPane sp = new JScrollPane(spp);
        p.add(sp, GBC.eol().fill(GridBagConstraints.BOTH));
    }
    
    @Override
    public boolean ok() {
        m_oPlugin.m_oParamList.save();
        return false;
    }

}

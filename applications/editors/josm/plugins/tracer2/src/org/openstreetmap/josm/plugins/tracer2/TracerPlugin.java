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

package org.openstreetmap.josm.plugins.tracer2;

import java.io.File;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.plugins.tracer2.preferences.ServerParamList;
import org.openstreetmap.josm.plugins.tracer2.preferences.ServerParamPreference;

public class TracerPlugin extends Plugin {
	
    public static TracerPlugin s_oPlugin;
    
    public final ServerParamList m_oParamList;
	
    public TracerPlugin(PluginInformation info) {
        super(info);
        MainMenu.add(Main.main.menu.moreToolsMenu, new TracerAction(Main.map));
        
        s_oPlugin = this;
        
        File plugindir = new File(this.getPluginDir());
        if (!plugindir.exists()) {
            plugindir.mkdirs();
        }
        
        m_oParamList = new ServerParamList(new File(plugindir, "serverParam.cfg").getAbsolutePath());
    }
    
    @Override
    public PreferenceSetting getPreferenceSetting() {
        return new ServerParamPreference(this);
    }
    
}

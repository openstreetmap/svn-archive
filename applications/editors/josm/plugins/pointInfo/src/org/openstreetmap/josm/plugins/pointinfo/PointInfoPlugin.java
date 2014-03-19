/**
 *  PointInfo - plugin for JOSM
 *  Marián Kyral
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

package org.openstreetmap.josm.plugins.pointinfo;

import java.util.ArrayList;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.MainMenu;
// import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;



/**
 * This is the main class for the PointInfo plugin.
 *
 */
public class PointInfoPlugin extends Plugin{

  public PointInfoPlugin(PluginInformation info) {
  super(info);
  MainMenu.add(Main.main.menu.moreToolsMenu, new PointInfoAction(Main.map));

//     @Override
//     public PreferenceSetting getPreferenceSetting() {
//         return new PreferenceEditor();
    }
}

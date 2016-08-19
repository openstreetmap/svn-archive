//    JOSM o5m plugin.
//    Copyright (C) 2013 Gerd Petermann
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
package org.openstreetmap.josm.plugins.o5m;

import org.openstreetmap.josm.actions.ExtensionFileFilter;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.plugins.o5m.io.O5mImporter;

/**
 * 
 * @author GerdP
 *
 */
public class O5mPlugin extends Plugin {

    public O5mPlugin(PluginInformation info) {
        super(info);
        // Allow JOSM to import *.o5m files
        ExtensionFileFilter.addImporter(new O5mImporter());
    }
}

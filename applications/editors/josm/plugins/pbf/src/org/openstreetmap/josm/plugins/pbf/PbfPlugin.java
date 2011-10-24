//    JOSM PBF plugin.
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
package org.openstreetmap.josm.plugins.pbf;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.ExtensionFileFilter;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.plugins.pbf.action.DownloadPbfTask;
import org.openstreetmap.josm.plugins.pbf.io.PbfImporter;

/**
 * 
 * @author Don-vip
 *
 */
public class PbfPlugin extends Plugin {

    public PbfPlugin(PluginInformation info) {
        super(info);
        // Allow JOSM to import *.osm.pbf files
        ExtensionFileFilter.importers.add(new PbfImporter());
        // Allow JOSM to export *.osm.pbf files
        //ExtensionFileFilter.exporters.add(new PbfExporter());// TODO: PBF export
        // Allow JOSM to download remote *.osm.pbf files
        Main.main.menu.openLocation.addDownloadTaskClass(DownloadPbfTask.class);
    }
}

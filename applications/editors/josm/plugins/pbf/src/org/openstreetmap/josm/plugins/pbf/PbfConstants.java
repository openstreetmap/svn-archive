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

import static org.openstreetmap.josm.tools.I18n.tr;

import org.openstreetmap.josm.actions.ExtensionFileFilter;

/**
 * 
 * @author Don-vip
 *
 */
public interface PbfConstants {
    
    /**
     * File extension.
     */
    public static final String EXTENSION = "osm.pbf";
    
    /**
     * File filter used in import/export dialogs.
     */
    public static final ExtensionFileFilter FILE_FILTER = new ExtensionFileFilter(EXTENSION, EXTENSION, tr("OSM Server Files pbf compressed") + " (*."+EXTENSION+")");
}

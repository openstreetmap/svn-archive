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
package org.openstreetmap.josm.plugins.opendata.modules.fr.cg41;

import org.openstreetmap.josm.plugins.opendata.core.modules.AbstractModule;
import org.openstreetmap.josm.plugins.opendata.core.modules.ModuleInformation;
import org.openstreetmap.josm.plugins.opendata.modules.fr.cg41.datasets.environnement.ZonesInondablesBrayeHandler;
import org.openstreetmap.josm.plugins.opendata.modules.fr.cg41.datasets.transport.ArretsBusHandler;

public class Cg41Module extends AbstractModule {

	public Cg41Module(ModuleInformation info) {
		super(info);
		handlers.add(new ArretsBusHandler());
		handlers.add(new ZonesInondablesBrayeHandler());
    }
}

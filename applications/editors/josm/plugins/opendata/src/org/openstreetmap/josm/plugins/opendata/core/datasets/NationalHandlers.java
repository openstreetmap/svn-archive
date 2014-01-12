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
package org.openstreetmap.josm.plugins.opendata.core.datasets;

import org.openstreetmap.josm.plugins.opendata.core.datasets.at.AustrianGmlHandler;
import org.openstreetmap.josm.plugins.opendata.core.datasets.fr.FrenchShpHandler;
import org.openstreetmap.josm.plugins.opendata.core.io.geographic.GmlHandler;
import org.openstreetmap.josm.plugins.opendata.core.io.geographic.ShpHandler;

public abstract class NationalHandlers {

	public static final GmlHandler[] DEFAULT_GML_HANDLERS = new GmlHandler[]{
		new AustrianGmlHandler()
	};

	public static final ShpHandler[] DEFAULT_SHP_HANDLERS = new ShpHandler[]{
		new FrenchShpHandler()
	};
}

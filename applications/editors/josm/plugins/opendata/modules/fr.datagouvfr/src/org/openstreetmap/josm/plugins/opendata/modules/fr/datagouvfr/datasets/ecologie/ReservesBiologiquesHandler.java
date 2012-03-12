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
package org.openstreetmap.josm.plugins.opendata.modules.fr.datagouvfr.datasets.ecologie;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.plugins.opendata.modules.fr.datagouvfr.datasets.DataGouvDataSetHandler;

public class ReservesBiologiquesHandler extends DataGouvDataSetHandler {

	public ReservesBiologiquesHandler() {
		super("Réserves-biologiques-de-métropole-30378855");
		setName("Réserves biologiques de métropole");
		setDownloadFileName("ONF_RB_2011_Officielles_L93.zip");
	}
	
	@Override
	public boolean acceptsFilename(String filename) {
		return acceptsZipFilename(filename, "ONF_RB_20.._Officielles_L93") || acceptsShpFilename(filename, "ONF_RB_20.._Officielles_L93");
	}

	@Override
	public void updateDataSet(DataSet ds) {
		// TODO
	}
}

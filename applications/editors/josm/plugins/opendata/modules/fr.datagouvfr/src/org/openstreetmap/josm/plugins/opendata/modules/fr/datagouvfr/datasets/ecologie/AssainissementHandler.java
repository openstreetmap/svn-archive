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

public class AssainissementHandler extends DataGouvDataSetHandler {

	public AssainissementHandler() {
		super("assainissement-collectif-30381843");
	}

	@Override
	public boolean acceptsFilename(String filename) {
		return acceptsOdsFilename(filename, "Export_ERU_20..");
	}

	@Override
	public void updateDataSet(DataSet ds) {
		// TODO Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.opendata.core.datasets.AbstractDataSetHandler#getSheetNumber()
	 */
	@Override
	public int getSheetNumber() {
		return 1;
	}
}

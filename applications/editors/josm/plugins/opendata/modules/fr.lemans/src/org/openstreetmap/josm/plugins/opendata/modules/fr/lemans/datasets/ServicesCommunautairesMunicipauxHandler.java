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
package org.openstreetmap.josm.plugins.opendata.modules.fr.lemans.datasets;

import org.openstreetmap.josm.data.osm.DataSet;

public class ServicesCommunautairesMunicipauxHandler extends LeMansDataSetHandler {

	public ServicesCommunautairesMunicipauxHandler() {
		super("F7F65F15-550EA533-37695DD8-F7A74F05");
		setName("Services communautaires et municipaux");
		setCsvKmzShpUuid("66C8C51F-550EA533-7E7BB44A-B9216F89", "66C925DA-550EA533-7E7BB44A-BCF0B629", "66C972AD-550EA533-7E7BB44A-E842FFAD");
	}

	@Override
	public boolean acceptsFilename(String filename) {
		return acceptsCsvKmzShpFilename(filename, "SERVICES_VDM_LMM") || acceptsZipFilename(filename, "Les services de le Mans Métropole et de la Ville du Mans .*");
	}

	@Override
	public void updateDataSet(DataSet ds) {
		// TODO Auto-generated method stub
	}
}

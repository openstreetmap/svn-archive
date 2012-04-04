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
package org.openstreetmap.josm.plugins.opendata.core.io.geographic;

import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

public abstract class DefaultGeographicHandler implements GeographicHandler {

	private boolean useNodeMap = true;
	private boolean checkNodeProximity = false;
	private boolean preferMultipolygonToSimpleWay = false;

	@Override
	public boolean preferMultipolygonToSimpleWay() {
		return preferMultipolygonToSimpleWay;
	}

	@Override
	public void setPreferMultipolygonToSimpleWay(boolean prefer) {
		preferMultipolygonToSimpleWay = prefer;
	}

	@Override
	public boolean checkNodeProximity() {
		return checkNodeProximity;
	}

	@Override
	public void setCheckNodeProximity(boolean check) {
		checkNodeProximity = check;
	}

	@Override
	public void setUseNodeMap(boolean use) {
		useNodeMap = use;
	}

	@Override
	public boolean useNodeMap() {
		return useNodeMap;
	}

	@Override
	public CoordinateReferenceSystem getCrsFor(String crsName) throws NoSuchAuthorityCodeException, FactoryException {
		if (crsName.equalsIgnoreCase("GCS_ETRS_1989")) {
			return CRS.decode("EPSG:4258");
		}
		return null;
	}
}

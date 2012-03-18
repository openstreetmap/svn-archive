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
package org.openstreetmap.josm.plugins.opendata.core.datasets.fr;

import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.openstreetmap.josm.plugins.opendata.core.io.geographic.DefaultShpHandler;

public class FrenchShpHandler extends DefaultShpHandler {

	/* (non-Javadoc)
	 * @see org.openstreetmap.josm.plugins.opendata.core.datasets.AbstractDataSetHandler#findMathTransform(org.opengis.referencing.crs.CoordinateReferenceSystem, org.opengis.referencing.crs.CoordinateReferenceSystem, boolean)
	 */
	@Override
	public MathTransform findMathTransform(CoordinateReferenceSystem sourceCRS, CoordinateReferenceSystem targetCRS, boolean lenient)
			throws FactoryException {
		if (sourceCRS.getName().getCode().equalsIgnoreCase("RGM04")) {
			return CRS.findMathTransform(CRS.decode("EPSG:4471"), targetCRS, lenient);
		} else if (sourceCRS.getName().getCode().equalsIgnoreCase("RGFG95_UTM_Zone_22N")) {
			return CRS.findMathTransform(CRS.decode("EPSG:2972"), targetCRS, lenient);
		} else {
			return super.findMathTransform(sourceCRS, targetCRS, lenient);
		}
	}
}

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

import java.nio.charset.Charset;
import java.util.Set;

import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.OsmPrimitive;

public interface ShpHandler {

	public MathTransform findMathTransform(CoordinateReferenceSystem sourceCRS, CoordinateReferenceSystem targetCRS, boolean lenient) throws FactoryException;

	public void setPreferMultipolygonToSimpleWay(boolean prefer);

	public boolean preferMultipolygonToSimpleWay();

	public void setCheckNodeProximity(boolean check);
	
	public boolean checkNodeProximity();
	
	public void setUseNodeMap(boolean use);
	
	public boolean useNodeMap();

	public void notifyFeatureParsed(Object feature, DataSet result, Set<OsmPrimitive> featurePrimitives);

	public void setDbfCharset(Charset charset);
	
	public Charset getDbfCharset();
}

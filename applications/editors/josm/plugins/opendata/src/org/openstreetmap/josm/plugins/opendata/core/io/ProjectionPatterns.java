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
package org.openstreetmap.josm.plugins.opendata.core.io;

import java.util.regex.Pattern;

import org.openstreetmap.josm.data.projection.Projection;
import org.openstreetmap.josm.plugins.opendata.core.OdConstants;

public class ProjectionPatterns implements OdConstants {

	private final Pattern xPattern;
	private final Pattern yPattern;
	private final Projection projection;
	
	public ProjectionPatterns(Pattern xPattern, Pattern yPattern, Projection projection) {
		this.xPattern = xPattern;
		this.yPattern = yPattern;
		this.projection = projection;
		PROJECTIONS.add(this);
	}

	public ProjectionPatterns(Pattern xPattern, Pattern yPattern) {
		this(xPattern, yPattern, null);
	}

	public ProjectionPatterns(String proj, Projection projection) {
		this(getCoordinatePattern(X_STRING, proj), getCoordinatePattern(Y_STRING, proj), projection);
	}

	public ProjectionPatterns(String proj) {
		this(getCoordinatePattern(X_STRING, proj), getCoordinatePattern(Y_STRING, proj), null);
	}
	
	public final Pattern getXPattern() {
		return xPattern;
	}
	
	public final Pattern getYPattern() {
		return yPattern;
	}

	public Projection getProjection(String xFieldName, String yFieldName) {
		return getProjection();
	}

	public final Projection getProjection() {
		return projection;
	}

	public static final Pattern getCoordinatePattern(String coor, String proj) {
    	return Pattern.compile("(?:.*(?:"+coor+").*(?:"+proj+").*)|(?:.*("+proj+").*(?:"+coor+").*)", Pattern.CASE_INSENSITIVE);
    }
}

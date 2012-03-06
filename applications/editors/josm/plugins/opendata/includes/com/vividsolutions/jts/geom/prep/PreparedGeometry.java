/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */
package com.vividsolutions.jts.geom.prep;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;

/**
 * An interface for classes which prepare {@link Geometry}s 
 * in order to optimize the performance 
 * of repeated calls to specific geometric operations.
 * <p>
 * A given implementation may provide optimized implementations
 * for only some of the specified methods, 
 * and delegate the remaining methods to the original {@link Geometry} operations.
 * An implementation may also only optimize certain situations,
 * and delegate others. 
 * See the implementing classes for documentation about which methods and situations
 * they optimize.
 * 
 * @author Martin Davis
 *
 */
public interface PreparedGeometry 
{
	
	/**
	 * Gets the original {@link Geometry} which has been prepared.
	 * 
	 * @return the base geometry
	 */
	Geometry getGeometry();

	/**
	 * Tests whether the base {@link Geometry} contains a given geometry.
	 * 
	 * @param geom the Geometry to test
	 * @return true if this Geometry contains the given Geometry
	 * 
	 * @see Geometry#contains(Geometry)
	 */
	boolean contains(Geometry geom);


	/**
	 * Tests whether the base {@link Geometry} is disjoint from a given geometry.
	 * This method supports {@link GeometryCollection}s as input
	 * 
	 * @param geom the Geometry to test
	 * @return true if this Geometry is disjoint from the given Geometry
	 * 
	 * @see Geometry#disjoint(Geometry)
	 */
	boolean disjoint(Geometry geom);

	/**
	 * Tests whether the base {@link Geometry} intersects a given geometry.
	 * This method supports {@link GeometryCollection}s as input
	 * 
	 * @param geom the Geometry to test
	 * @return true if this Geometry intersects the given Geometry
	 * 
	 * @see Geometry#intersects(Geometry)
	 */
	boolean intersects(Geometry geom);
}

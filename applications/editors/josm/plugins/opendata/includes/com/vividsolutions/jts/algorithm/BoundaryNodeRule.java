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
package com.vividsolutions.jts.algorithm;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Lineal;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.operation.IsSimpleOp;
import com.vividsolutions.jts.operation.relate.RelateOp;

/**
 * An interface for rules which determine whether node points
 * which are in boundaries of {@link Lineal} geometry components
 * are in the boundary of the parent geometry collection.
 * The SFS specifies a single kind of boundary node rule,
 * the {@link Mod2BoundaryNodeRule} rule.
 * However, other kinds of Boundary Node Rules are appropriate
 * in specific situations (for instance, linear network topology
 * usually follows the {@link EndPointBoundaryNodeRule}.)
 * Some JTS operations allow the BoundaryNodeRule to be specified,
 * and respect this rule when computing the results of the operation.
 *
 * @author Martin Davis
 * @version 1.7
 *
 * @see RelateOp
 * @see IsSimpleOp
 * @see PointLocator
 */
public interface BoundaryNodeRule
{

	/**
	 * Tests whether a point that lies in <tt>boundaryCount</tt>
	 * geometry component boundaries is considered to form part of the boundary
	 * of the parent geometry.
	 * 
	 * @param boundaryCount the number of component boundaries that this point occurs in
	 * @return true if points in this number of boundaries lie in the parent boundary
	 */
  boolean isInBoundary(int boundaryCount);

  /**
   * The Mod-2 Boundary Node Rule (which is the rule specified in the OGC SFS).
   * @see Mod2BoundaryNodeRule
   */
  public static final BoundaryNodeRule MOD2_BOUNDARY_RULE = new Mod2BoundaryNodeRule();

  /**
   * The Boundary Node Rule specified by the OGC Simple Features Specification,
   * which is the same as the Mod-2 rule.
   * @see Mod2BoundaryNodeRule
   */
  public static final BoundaryNodeRule OGC_SFS_BOUNDARY_RULE = MOD2_BOUNDARY_RULE;

  /**
   * A {@link BoundaryNodeRule} specifies that points are in the
   * boundary of a lineal geometry iff
   * the point lies on the boundary of an odd number
   * of components.
   * Under this rule {@link LinearRing}s and closed
   * {@link LineString}s have an empty boundary.
   * <p>
   * This is the rule specified by the <i>OGC SFS</i>,
   * and is the default rule used in JTS.
   *
   * @author Martin Davis
   * @version 1.7
   */
  public static class Mod2BoundaryNodeRule
      implements BoundaryNodeRule
  {
    public boolean isInBoundary(int boundaryCount)
    {
      // the "Mod-2 Rule"
      return boundaryCount % 2 == 1;
    }
  }
}
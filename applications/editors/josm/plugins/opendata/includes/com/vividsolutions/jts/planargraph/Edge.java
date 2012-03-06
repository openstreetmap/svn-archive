
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
package com.vividsolutions.jts.planargraph;

/**
 * Represents an undirected edge of a {@link PlanarGraph}. An undirected edge
 * in fact simply acts as a central point of reference for two opposite
 * {@link DirectedEdge}s.
 * <p>
 * Usually a client using a <code>PlanarGraph</code> will subclass <code>Edge</code>
 * to add its own application-specific data and methods.
 *
 * @version 1.7
 */
public class Edge
    extends GraphComponent
{

  /**
   * The two DirectedEdges associated with this Edge.
   * Index 0 is forward, 1 is reverse.
   */
  protected DirectedEdge[] dirEdge;

  /**
   * Constructs an Edge whose DirectedEdges are not yet set. Be sure to call
   * {@link #setDirectedEdges(DirectedEdge, DirectedEdge)}
   */
  public Edge()
  {
  }


  /**
   * Tests whether this edge has been removed from its containing graph
   *
   * @return <code>true</code> if this edge is removed
   */
  public boolean isRemoved()
  {
    return dirEdge == null;
  }

}

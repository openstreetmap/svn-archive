
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
 * The base class for all graph component classes.
 * Maintains flags of use in generic graph algorithms.
 * Provides two flags:
 * <ul>
 * <li><b>marked</b> - typically this is used to indicate a state that persists
 * for the course of the graph's lifetime.  For instance, it can be
 * used to indicate that a component has been logically deleted from the graph.
 * <li><b>visited</b> - this is used to indicate that a component has been processed
 * or visited by an single graph algorithm.  For instance, a breadth-first traversal of the
 * graph might use this to indicate that a node has already been traversed.
 * The visited flag may be set and cleared many times during the lifetime of a graph.
 *
 * <p>
 * Graph components support storing user context data.  This will typically be
 * used by client algorithms which use planar graphs.
 *
 * @version 1.7
 */
public abstract class GraphComponent
{


  public GraphComponent() {
  }

  /**
   * Tests whether this component has been removed from its containing graph
   *
   * @return <code>true</code> if this component is removed
   */
  public abstract boolean isRemoved();
}

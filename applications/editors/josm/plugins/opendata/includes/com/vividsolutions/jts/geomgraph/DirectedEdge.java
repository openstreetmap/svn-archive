


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
package com.vividsolutions.jts.geomgraph;

import java.io.PrintStream;

import com.vividsolutions.jts.geom.Location;


/**
 * @version 1.7
 */
public class DirectedEdge
  extends EdgeEnd
{


  protected boolean isForward;
  private boolean isInResult = false;
  private boolean isVisited = false;

  private DirectedEdge sym; // the symmetric edge
  private DirectedEdge next;  // the next edge in the edge ring for the polygon containing this edge
  private DirectedEdge nextMin;  // the next edge in the MinimalEdgeRing that contains this edge
  private EdgeRing edgeRing;  // the EdgeRing that this edge is part of
  private EdgeRing minEdgeRing;  // the MinimalEdgeRing that this edge is part of
  /**
   * The depth of each side (position) of this edge.
   * The 0 element of the array is never used.
   */
  private int[] depth = { 0, -999, -999 };

  public DirectedEdge(Edge edge, boolean isForward)
  {
    super(edge);
    this.isForward = isForward;
    if (isForward) {
      init(edge.getCoordinate(0), edge.getCoordinate(1));
    }
    else {
      int n = edge.getNumPoints() - 1;
      init(edge.getCoordinate(n), edge.getCoordinate(n-1));
    }
    computeDirectedLabel();
  }
  public Edge getEdge() { return edge; }
  public void setInResult(boolean isInResult) { this.isInResult = isInResult; }
  public boolean isInResult() { return isInResult; }
  public boolean isVisited() { return isVisited; }
  public void setVisited(boolean isVisited) { this.isVisited = isVisited; }
  public void setEdgeRing(EdgeRing edgeRing) { this.edgeRing = edgeRing; }
  public EdgeRing getEdgeRing() { return edgeRing; }
  public void setMinEdgeRing(EdgeRing minEdgeRing) { this.minEdgeRing = minEdgeRing; }
  public EdgeRing getMinEdgeRing() { return minEdgeRing; }


  public int getDepthDelta()
  {
    int depthDelta = edge.getDepthDelta();
    if (! isForward) depthDelta = -depthDelta;
    return depthDelta;
  }

  /**
   * setVisitedEdge marks both DirectedEdges attached to a given Edge.
   * This is used for edges corresponding to lines, which will only
   * appear oriented in a single direction in the result.
   */
  public void setVisitedEdge(boolean isVisited)
  {
    setVisited(isVisited);
    sym.setVisited(isVisited);
  }
  /**
   * Each Edge gives rise to a pair of symmetric DirectedEdges, in opposite
   * directions.
   * @return the DirectedEdge for the same Edge but in the opposite direction
   */
  public DirectedEdge getSym() { return sym; }
  public boolean isForward() { return isForward; }
  public void setSym(DirectedEdge de)
  {
    sym = de;
  }
  public DirectedEdge getNext() { return next; }
  public void setNext(DirectedEdge next) { this.next = next; }
  public DirectedEdge getNextMin() { return nextMin; }
  public void setNextMin(DirectedEdge nextMin) { this.nextMin = nextMin; }

  /**
   * This edge is a line edge if
   * <ul>
   * <li> at least one of the labels is a line label
   * <li> any labels which are not line labels have all Locations = EXTERIOR
   * </ul>
   */
  public boolean isLineEdge()
  {
    boolean isLine = label.isLine(0) || label.isLine(1);
    boolean isExteriorIfArea0 =
      ! label.isArea(0) || label.allPositionsEqual(0, Location.EXTERIOR);
    boolean isExteriorIfArea1 =
      ! label.isArea(1) || label.allPositionsEqual(1, Location.EXTERIOR);

    return isLine && isExteriorIfArea0 && isExteriorIfArea1;
  }
  /**
   * This is an interior Area edge if
   * <ul>
   * <li> its label is an Area label for both Geometries
   * <li> and for each Geometry both sides are in the interior.
   * </ul>
   *
   * @return true if this is an interior Area edge
   */
  public boolean isInteriorAreaEdge()
  {
    boolean isInteriorAreaEdge = true;
    for (int i = 0; i < 2; i++) {
      if (! ( label.isArea(i)
            && label.getLocation(i, Position.LEFT ) == Location.INTERIOR
            && label.getLocation(i, Position.RIGHT) == Location.INTERIOR) ) {
        isInteriorAreaEdge = false;
      }
    }
    return isInteriorAreaEdge;
  }

  /**
   * Compute the label in the appropriate orientation for this DirEdge
   */
  private void computeDirectedLabel()
  {
    label = new Label(edge.getLabel());
    if (! isForward)
      label.flip();
  }


  public void print(PrintStream out)
  {
    super.print(out);
    out.print(" " + depth[Position.LEFT] + "/" + depth[Position.RIGHT]);
    out.print(" (" + getDepthDelta() + ")");
    //out.print(" " + this.hashCode());
    //if (next != null) out.print(" next:" + next.hashCode());
    if (isInResult) out.print(" inResult");
  }

}

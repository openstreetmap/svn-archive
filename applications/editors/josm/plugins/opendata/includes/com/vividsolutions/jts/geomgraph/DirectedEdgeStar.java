


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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.vividsolutions.jts.geom.Location;
import com.vividsolutions.jts.geom.TopologyException;
import com.vividsolutions.jts.util.Assert;

/**
 * A DirectedEdgeStar is an ordered list of <b>outgoing</b> DirectedEdges around a node.
 * It supports labelling the edges as well as linking the edges to form both
 * MaximalEdgeRings and MinimalEdgeRings.
 *
 * @version 1.7
 */
public class DirectedEdgeStar
  extends EdgeEndStar
{

  /**
   * A list of all outgoing edges in the result, in CCW order
   */
  private List resultAreaEdgeList;
  private Label label;

  public DirectedEdgeStar() {
  }
  /**
   * Insert a directed edge in the list
   */
  public void insert(EdgeEnd ee)
  {
    DirectedEdge de = (DirectedEdge) ee;
    insertEdgeEnd(de, de);
  }

  public Label getLabel() { return label; }

  public int getOutgoingDegree(EdgeRing er)
  {
    int degree = 0;
    for (Iterator it = iterator(); it.hasNext(); ) {
      DirectedEdge de = (DirectedEdge) it.next();
      if (de.getEdgeRing() == er) degree++;
    }
    return degree;
  }

  /**
   * Compute the labelling for all dirEdges in this star, as well
   * as the overall labelling
   */
  public void computeLabelling(GeometryGraph[] geom)
  {
//Debug.print(this);
    super.computeLabelling(geom);

    // determine the overall labelling for this DirectedEdgeStar
    // (i.e. for the node it is based at)
    label = new Label(Location.NONE);
    for (Iterator it = iterator(); it.hasNext(); ) {
      EdgeEnd ee = (EdgeEnd) it.next();
      Edge e = ee.getEdge();
      Label eLabel = e.getLabel();
      for (int i = 0; i < 2; i++) {
        int eLoc = eLabel.getLocation(i);
        if (eLoc == Location.INTERIOR || eLoc == Location.BOUNDARY)
          label.setLocation(i, Location.INTERIOR);
      }
    }
//Debug.print(this);
  }

  /**
   * For each dirEdge in the star,
   * merge the label from the sym dirEdge into the label
   */
  public void mergeSymLabels()
  {
    for (Iterator it = iterator(); it.hasNext(); ) {
      DirectedEdge de = (DirectedEdge) it.next();
      Label label = de.getLabel();
      label.merge(de.getSym().getLabel());
    }
  }

  /**
   * Update incomplete dirEdge labels from the labelling for the node
   */
  public void updateLabelling(Label nodeLabel)
  {
    for (Iterator it = iterator(); it.hasNext(); ) {
      DirectedEdge de = (DirectedEdge) it.next();
      Label label = de.getLabel();
      label.setAllLocationsIfNull(0, nodeLabel.getLocation(0));
      label.setAllLocationsIfNull(1, nodeLabel.getLocation(1));
    }
  }

  private List getResultAreaEdges()
  {
//print(System.out);
    if (resultAreaEdgeList != null) return resultAreaEdgeList;
    resultAreaEdgeList = new ArrayList();
    for (Iterator it = iterator(); it.hasNext(); ) {
      DirectedEdge de = (DirectedEdge) it.next();
      if (de.isInResult() || de.getSym().isInResult() )
        resultAreaEdgeList.add(de);
    }
    return resultAreaEdgeList;
  }

  private final int SCANNING_FOR_INCOMING = 1;
  private final int LINKING_TO_OUTGOING = 2;
  /**
   * Traverse the star of DirectedEdges, linking the included edges together.
   * To link two dirEdges, the <next> pointer for an incoming dirEdge
   * is set to the next outgoing edge.
   * <p>
   * DirEdges are only linked if:
   * <ul>
   * <li>they belong to an area (i.e. they have sides)
   * <li>they are marked as being in the result
   * </ul>
   * <p>
   * Edges are linked in CCW order (the order they are stored).
   * This means that rings have their face on the Right
   * (in other words,
   * the topological location of the face is given by the RHS label of the DirectedEdge)
   * <p>
   * PRECONDITION: No pair of dirEdges are both marked as being in the result
   */
  public void linkResultDirectedEdges()
  {
    // make sure edges are copied to resultAreaEdges list
    getResultAreaEdges();
    // find first area edge (if any) to start linking at
    DirectedEdge firstOut = null;
    DirectedEdge incoming = null;
    int state = SCANNING_FOR_INCOMING;
    // link edges in CCW order
    for (int i = 0; i < resultAreaEdgeList.size(); i++) {
      DirectedEdge nextOut = (DirectedEdge) resultAreaEdgeList.get(i);
      DirectedEdge nextIn = nextOut.getSym();

      // skip de's that we're not interested in
      if (! nextOut.getLabel().isArea()) continue;

      // record first outgoing edge, in order to link the last incoming edge
      if (firstOut == null && nextOut.isInResult()) firstOut = nextOut;
      // assert: sym.isInResult() == false, since pairs of dirEdges should have been removed already

      switch (state) {
      case SCANNING_FOR_INCOMING:
        if (! nextIn.isInResult()) continue;
        incoming = nextIn;
        state = LINKING_TO_OUTGOING;
        break;
      case LINKING_TO_OUTGOING:
        if (! nextOut.isInResult()) continue;
        incoming.setNext(nextOut);
        state = SCANNING_FOR_INCOMING;
        break;
      }
    }
//Debug.print(this);
    if (state == LINKING_TO_OUTGOING) {
//Debug.print(firstOut == null, this);
      if (firstOut == null)
        throw new TopologyException("no outgoing dirEdge found", getCoordinate());
      //Assert.isTrue(firstOut != null, "no outgoing dirEdge found (at " + getCoordinate() );
      Assert.isTrue(firstOut.isInResult(), "unable to link last incoming dirEdge");
      incoming.setNext(firstOut);
    }
  }
  public void linkMinimalDirectedEdges(EdgeRing er)
  {
    // find first area edge (if any) to start linking at
    DirectedEdge firstOut = null;
    DirectedEdge incoming = null;
    int state = SCANNING_FOR_INCOMING;
    // link edges in CW order
    for (int i = resultAreaEdgeList.size() - 1; i >= 0; i--) {
      DirectedEdge nextOut = (DirectedEdge) resultAreaEdgeList.get(i);
      DirectedEdge nextIn = nextOut.getSym();

      // record first outgoing edge, in order to link the last incoming edge
      if (firstOut == null && nextOut.getEdgeRing() == er) firstOut = nextOut;

      switch (state) {
      case SCANNING_FOR_INCOMING:
        if (nextIn.getEdgeRing() != er) continue;
        incoming = nextIn;
        state = LINKING_TO_OUTGOING;
        break;
      case LINKING_TO_OUTGOING:
        if (nextOut.getEdgeRing() != er) continue;
        incoming.setNextMin(nextOut);
        state = SCANNING_FOR_INCOMING;
        break;
      }
    }
//print(System.out);
    if (state == LINKING_TO_OUTGOING) {
      Assert.isTrue(firstOut != null, "found null for first outgoing dirEdge");
      Assert.isTrue(firstOut.getEdgeRing() == er, "unable to link last incoming dirEdge");
      incoming.setNextMin(firstOut);
    }
  }

  /**
   * Traverse the star of edges, maintaing the current location in the result
   * area at this node (if any).
   * If any L edges are found in the interior of the result, mark them as covered.
   */
  public void findCoveredLineEdges()
  {
//Debug.print("findCoveredLineEdges");
//Debug.print(this);
    // Since edges are stored in CCW order around the node,
    // as we move around the ring we move from the right to the left side of the edge

    /**
     * Find first DirectedEdge of result area (if any).
     * The interior of the result is on the RHS of the edge,
     * so the start location will be:
     * - INTERIOR if the edge is outgoing
     * - EXTERIOR if the edge is incoming
     */
    int startLoc = Location.NONE ;
    for (Iterator it = iterator(); it.hasNext(); ) {
      DirectedEdge nextOut  = (DirectedEdge) it.next();
      DirectedEdge nextIn   = nextOut.getSym();
      if (! nextOut.isLineEdge()) {
        if (nextOut.isInResult()) {
          startLoc = Location.INTERIOR;
          break;
        }
        if (nextIn.isInResult()) {
          startLoc = Location.EXTERIOR;
          break;
        }
      }
    }
    // no A edges found, so can't determine if L edges are covered or not
    if (startLoc == Location.NONE) return;

    /**
     * move around ring, keeping track of the current location
     * (Interior or Exterior) for the result area.
     * If L edges are found, mark them as covered if they are in the interior
     */
    int currLoc = startLoc;
    for (Iterator it = iterator(); it.hasNext(); ) {
      DirectedEdge nextOut  = (DirectedEdge) it.next();
      DirectedEdge nextIn   = nextOut.getSym();
      if (nextOut.isLineEdge()) {
        nextOut.getEdge().setCovered(currLoc == Location.INTERIOR);
//Debug.println(nextOut);
      }
      else {  // edge is an Area edge
        if (nextOut.isInResult())
          currLoc = Location.EXTERIOR;
        if (nextIn.isInResult())
          currLoc = Location.INTERIOR;
      }
    }
  }


  public void print(PrintStream out)
  {
    System.out.println("DirectedEdgeStar: " + getCoordinate());
    for (Iterator it = iterator(); it.hasNext(); ) {
      DirectedEdge de = (DirectedEdge) it.next();
      out.print("out ");
      de.print(out);
      out.println();
      out.print("in ");
      de.getSym().print(out);
      out.println();
    }
  }
}

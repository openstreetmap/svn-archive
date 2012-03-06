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
package com.vividsolutions.jts.operation.distance;

import java.util.*;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.util.*;
import com.vividsolutions.jts.algorithm.*;

/**
 * Find two points on two {@link Geometry}s which lie
 * within a given distance, or else are the nearest points
 * on the geometries (in which case this also
 * provides the distance between the geometries).
 * <p>
 * The distance computation also finds a pair of points in the input geometries
 * which have the minimum distance between them.
 * If a point lies in the interior of a line segment,
 * the coordinate computed is a close
 * approximation to the exact point.
 * <p>
 * The algorithms used are straightforward O(n^2)
 * comparisons.  This worst-case performance could be improved on
 * by using Voronoi techniques or spatial indexes.
 *
 * @version 1.7
 */
public class DistanceOp
{


  /**
   * Test whether two geometries lie within a given distance of each other.
   * @param g0 a {@link Geometry}
   * @param g1 another {@link Geometry}
   * @param distance the distance to test
   * @return true if g0.distance(g1) <= distance
   */
  public static boolean isWithinDistance(Geometry g0, Geometry g1, double distance)
  {
    DistanceOp distOp = new DistanceOp(g0, g1, distance);
    return distOp.distance() <= distance;
  }


  // input
  private Geometry[] geom;
  private double terminateDistance = 0.0;
  // working
  private PointLocator ptLocator = new PointLocator();
  private Coordinate[] minDistanceLocation;
  private double minDistance = Double.MAX_VALUE;


  /**
   * Constructs a DistanceOp that computes the distance and nearest points between
   * the two specified geometries.
   * @param g0 a Geometry
   * @param g1 a Geometry
   * @param terminateDistance the distance on which to terminate the search
   */
  public DistanceOp(Geometry g0, Geometry g1, double terminateDistance)
  {
    this.geom = new Geometry[2];
    geom[0] = g0;
    geom[1] = g1;
    this.terminateDistance = terminateDistance;
  }

  /**
   * Report the distance between the nearest points on the input geometries.
   *
   * @return the distance between the geometries
   * @return 0 if either input geometry is empty
   * @throws IllegalArgumentException if either input geometry is null
   */
  public double distance()
  {
  	if (geom[0] == null || geom[1] == null)
  		throw new IllegalArgumentException("null geometries are not supported");
  	if (geom[0].isEmpty() || geom[1].isEmpty()) 
  		return 0.0;
  	
    computeMinDistance();
    return minDistance;
  }


  private void updateMinDistance(Coordinate[] locGeom, boolean flip)
  {
    // if not set then don't update
    if (locGeom[0] == null) return;

    if (flip) {
      minDistanceLocation[0] = locGeom[1];
      minDistanceLocation[1] = locGeom[0];
    }
    else {
      minDistanceLocation[0] = locGeom[0];
      minDistanceLocation[1] = locGeom[1];
    }
  }

  private void computeMinDistance()
  {
    // only compute once!
    if (minDistanceLocation != null) return;

    minDistanceLocation = new Coordinate[2];
    computeContainmentDistance();
    if (minDistance <= terminateDistance) return;
    computeFacetDistance();
  }

  private void computeContainmentDistance()
  {
    Coordinate[] locPtPoly = new Coordinate[2];
    // test if either geometry has a vertex inside the other
    computeContainmentDistance(0, locPtPoly);
    if (minDistance <= terminateDistance) return;
    computeContainmentDistance(1, locPtPoly);
  }
  
  private void computeContainmentDistance(int polyGeomIndex, Coordinate[] locPtPoly)
  {
  	int locationsIndex = 1 - polyGeomIndex;
    List polys = PolygonExtracter.getPolygons(geom[polyGeomIndex]);
    if (polys.size() > 0) {
      List insideLocs = ConnectedElementLocationFilter.getLocations(geom[locationsIndex]);
      computeContainmentDistance(insideLocs, polys, locPtPoly);
      if (minDistance <= terminateDistance) {
      	// this assigment is determined by the order of the args in the computeInside call above
        minDistanceLocation[locationsIndex] = locPtPoly[0];
        minDistanceLocation[polyGeomIndex] 	= locPtPoly[1];
        return;
      }
    }	
  }
  
  private void computeContainmentDistance(List locs, List polys, Coordinate[] locPtPoly)
  {
    for (int i = 0; i < locs.size(); i++) {
      Coordinate loc = (Coordinate) locs.get(i);
      for (int j = 0; j < polys.size(); j++) {
      	computeContainmentDistance(loc, (Polygon) polys.get(j), locPtPoly);
        if (minDistance <= terminateDistance) return;
      }
    }
  }

  private void computeContainmentDistance(Coordinate ptLoc,
      Polygon poly,
      Coordinate[] locPtPoly)
  {
    Coordinate pt = ptLoc;
    // if pt is not in exterior, distance to geom is 0
    if (Location.EXTERIOR != ptLocator.locate(pt, poly)) {
      minDistance = 0.0;
      locPtPoly[0] = ptLoc;
      locPtPoly[1] = new Coordinate(pt);
      return;
    }
  }

  /**
   * Computes distance between facets (lines and points)
   * of input geometries.
   *
   */
  private void computeFacetDistance()
  {
    Coordinate[] locGeom = new Coordinate[2];

    /**
     * Geometries are not wholely inside, so compute distance from lines and points
     * of one to lines and points of the other
     */
    List lines0 = LinearComponentExtracter.getLines(geom[0]);
    List lines1 = LinearComponentExtracter.getLines(geom[1]);

    List pts0 = PointExtracter.getPoints(geom[0]);
    List pts1 = PointExtracter.getPoints(geom[1]);

    // exit whenever minDistance goes LE than terminateDistance
    computeMinDistanceLines(lines0, lines1, locGeom);
    updateMinDistance(locGeom, false);
    if (minDistance <= terminateDistance) return;

    locGeom[0] = null;
    locGeom[1] = null;
    computeMinDistanceLinesPoints(lines0, pts1, locGeom);
    updateMinDistance(locGeom, false);
    if (minDistance <= terminateDistance) return;

    locGeom[0] = null;
    locGeom[1] = null;
    computeMinDistanceLinesPoints(lines1, pts0, locGeom);
    updateMinDistance(locGeom, true);
    if (minDistance <= terminateDistance) return;

    locGeom[0] = null;
    locGeom[1] = null;
    computeMinDistancePoints(pts0, pts1, locGeom);
    updateMinDistance(locGeom, false);
  }

  private void computeMinDistanceLines(List lines0, List lines1, Coordinate[] locGeom)
  {
    for (int i = 0; i < lines0.size(); i++) {
      LineString line0 = (LineString) lines0.get(i);
      for (int j = 0; j < lines1.size(); j++) {
        LineString line1 = (LineString) lines1.get(j);
        computeMinDistance(line0, line1, locGeom);
        if (minDistance <= terminateDistance) return;
      }
    }
  }

  private void computeMinDistancePoints(List points0, List points1, Coordinate[] locGeom)
  {
    for (int i = 0; i < points0.size(); i++) {
      Point pt0 = (Point) points0.get(i);
      for (int j = 0; j < points1.size(); j++) {
        Point pt1 = (Point) points1.get(j);
        double dist = pt0.getCoordinate().distance(pt1.getCoordinate());
        if (dist < minDistance) {
          minDistance = dist;
          locGeom[0] = new Coordinate(pt0.getCoordinate());
          locGeom[1] = new Coordinate(pt1.getCoordinate());
        }
        if (minDistance <= terminateDistance) return;
      }
    }
  }

  private void computeMinDistanceLinesPoints(List lines, List points,
      Coordinate[] locGeom)
  {
    for (int i = 0; i < lines.size(); i++) {
      LineString line = (LineString) lines.get(i);
      for (int j = 0; j < points.size(); j++) {
        Point pt = (Point) points.get(j);
        computeMinDistance(line, pt, locGeom);
        if (minDistance <= terminateDistance) return;
      }
    }
  }

  private void computeMinDistance(LineString line0, LineString line1,
                                  Coordinate[] locGeom)
  {
    if (line0.getEnvelopeInternal().distance(line1.getEnvelopeInternal())
        > minDistance)
          return;
    Coordinate[] coord0 = line0.getCoordinates();
    Coordinate[] coord1 = line1.getCoordinates();
      // brute force approach!
    for (int i = 0; i < coord0.length - 1; i++) {
      for (int j = 0; j < coord1.length - 1; j++) {
        double dist = CGAlgorithms.distanceLineLine(
                                        coord0[i], coord0[i + 1],
                                        coord1[j], coord1[j + 1] );
        if (dist < minDistance) {
          minDistance = dist;
          LineSegment seg0 = new LineSegment(coord0[i], coord0[i + 1]);
          LineSegment seg1 = new LineSegment(coord1[j], coord1[j + 1]);
          Coordinate[] closestPt = seg0.closestPoints(seg1);
          locGeom[0] = new Coordinate(closestPt[0]);
          locGeom[1] = new Coordinate(closestPt[1]);
        }
        if (minDistance <= terminateDistance) return;
      }
    }
  }

  private void computeMinDistance(LineString line, Point pt,
                                  Coordinate[] locGeom)
  {
    if (line.getEnvelopeInternal().distance(pt.getEnvelopeInternal())
        > minDistance)
          return;
    Coordinate[] coord0 = line.getCoordinates();
    Coordinate coord = pt.getCoordinate();
      // brute force approach!
    for (int i = 0; i < coord0.length - 1; i++) {
        double dist = CGAlgorithms.distancePointLine(
            coord, coord0[i], coord0[i + 1] );
        if (dist < minDistance) {
          minDistance = dist;
          LineSegment seg = new LineSegment(coord0[i], coord0[i + 1]);
          Coordinate segClosestPoint = seg.closestPoint(coord);
          locGeom[0] = new Coordinate(segClosestPoint);
          locGeom[1] = new Coordinate(coord);
        }
        if (minDistance <= terminateDistance) return;

    }
  }

}

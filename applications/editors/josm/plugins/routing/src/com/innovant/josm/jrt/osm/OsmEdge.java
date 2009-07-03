/*
 * Copyright (C) 2008 Innovant
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,USA.
 *
 * For more information, please contact:
 *
 *  Innovant
 *   juangui@gmail.com
 *   vidalfree@gmail.com
 *
 *  http://public.grupoinnovant.com/blog
 *
 */
package com.innovant.josm.jrt.osm;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;

/**
 * Class that represents an edge of the graph.
 * @author jose
 */
public class OsmEdge extends DefaultWeightedEdge {
 /**
  * Serial
  */
  private static final long serialVersionUID = 1L;
  /**
   * Way associated
   */
  private Way way;
  /**
   * Nodes in the edge
   */
  private Node from, to;
  /**
   * Length edge
   */
  private double length;
  /**
   * Speed edge.
   */
  private double speed;


/**
   * Constructor
   * @param way
   * @param length
   */
  public OsmEdge(Way way, Node from, Node to) {
        super();
        this.way = way;
        this.from = from;
        this.to = to;
        this.length = from.getCoor().greatCircleDistance(to.getCoor());
      }

  /**
   * @return the way
   */
  public Way getWay() {
      return this.way;
  }

  public EastNorth fromEastNorth() {
      return this.from.getEastNorth();
  }

  public EastNorth toEastNorth() {
      return this.to.getEastNorth();
  }

  /**
   * Returns length of segment in meters
   * @return length of segment in meters.
   */
  public double getLength() {
    return length;
  }
  
  public void setLength(double length) {
    this.length = length;
}

public double getSpeed() {
        return speed;
  }

  public void setSpeed(double speed) {
        this.speed = speed;
  }
}

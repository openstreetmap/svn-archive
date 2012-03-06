

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
package com.vividsolutions.jts.geom;

import java.util.Collection;

/**
 * Useful utility functions for handling Coordinate arrays
 *
 * @version 1.7
 */
public class CoordinateArrays {

  private final static Coordinate[] coordArrayType = new Coordinate[0];


  /**
   * Determines which orientation of the {@link Coordinate} array
   * is (overall) increasing.
   * In other words, determines which end of the array is "smaller"
   * (using the standard ordering on {@link Coordinate}).
   * Returns an integer indicating the increasing direction.
   * If the sequence is a palindrome, it is defined to be
   * oriented in a positive direction.
   *
   * @param pts the array of Coordinates to test
   * @return <code>1</code> if the array is smaller at the start
   * or is a palindrome,
   * <code>-1</code> if smaller at the end
   */
  public static int increasingDirection(Coordinate[] pts) {
    for (int i = 0; i < pts.length / 2; i++) {
      int j = pts.length - 1 - i;
      // skip equal points on both ends
      int comp = pts[i].compareTo(pts[j]);
      if (comp != 0)
        return comp;
    }
    // array must be a palindrome - defined to be in positive direction
    return 1;
  }

  /**
   * Converts the given Collection of Coordinates into a Coordinate array.
   */
  public static Coordinate[] toCoordinateArray(Collection<Coordinate> coordList)
  {
    return coordList.toArray(coordArrayType);
  }

  /**
   * Returns whether #equals returns true for any two consecutive Coordinates
   * in the given array.
   */
  public static boolean hasRepeatedPoints(Coordinate[] coord)
  {
    for (int i = 1; i < coord.length; i++) {
      if (coord[i - 1].equals(coord[i]) ) {
        return true;
      }
    }
    return false;
  }

  /**
   * If the coordinate array argument has repeated points,
   * constructs a new array containing no repeated points.
   * Otherwise, returns the argument.
   * @see #hasRepeatedPoints(Coordinate[])
   */
  public static Coordinate[] removeRepeatedPoints(Coordinate[] coord)
  {
    if (! hasRepeatedPoints(coord)) return coord;
    CoordinateList coordList = new CoordinateList(coord, false);
    return coordList.toCoordinateArray();
  }

  /**
   * Reverses the coordinates in an array in-place.
   */
  public static void reverse(Coordinate[] coord)
  {
    int last = coord.length - 1;
    int mid = last / 2;
    for (int i = 0; i <= mid; i++) {
      Coordinate tmp = coord[i];
      coord[i] = coord[last - i];
      coord[last - i] = tmp;
    }
  }

  /**
   *  Returns the minimum coordinate, using the usual lexicographic comparison.
   *
   *@param  coordinates  the array to search
   *@return              the minimum coordinate in the array, found using <code>compareTo</code>
   *@see Coordinate#compareTo(Object)
   */
  public static Coordinate minCoordinate(Coordinate[] coordinates)
  {
    Coordinate minCoord = null;
    for (int i = 0; i < coordinates.length; i++) {
      if (minCoord == null || minCoord.compareTo(coordinates[i]) > 0) {
        minCoord = coordinates[i];
      }
    }
    return minCoord;
  }
  /**
   *  Shifts the positions of the coordinates until <code>firstCoordinate</code>
   *  is first.
   *
   *@param  coordinates      the array to rearrange
   *@param  firstCoordinate  the coordinate to make first
   */
  public static void scroll(Coordinate[] coordinates, Coordinate firstCoordinate) {
    int i = indexOf(firstCoordinate, coordinates);
    if (i < 0) return;
    Coordinate[] newCoordinates = new Coordinate[coordinates.length];
    System.arraycopy(coordinates, i, newCoordinates, 0, coordinates.length - i);
    System.arraycopy(coordinates, 0, newCoordinates, coordinates.length - i, i);
    System.arraycopy(newCoordinates, 0, coordinates, 0, coordinates.length);
  }

  /**
   *  Returns the index of <code>coordinate</code> in <code>coordinates</code>.
   *  The first position is 0; the second, 1; etc.
   *
   *@param  coordinate   the <code>Coordinate</code> to search for
   *@param  coordinates  the array to search
   *@return              the position of <code>coordinate</code>, or -1 if it is
   *      not found
   */
  public static int indexOf(Coordinate coordinate, Coordinate[] coordinates) {
    for (int i = 0; i < coordinates.length; i++) {
      if (coordinate.equals(coordinates[i])) {
        return i;
      }
    }
    return -1;
  }
}

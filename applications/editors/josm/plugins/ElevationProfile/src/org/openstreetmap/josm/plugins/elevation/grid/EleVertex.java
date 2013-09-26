/**
 * This program is free software: you can redistribute it and/or modify it under 
 * the terms of the GNU General Public License as published by the 
 * Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU General Public License for more details. 
 * 
 * You should have received a copy of the GNU General Public License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.openstreetmap.josm.plugins.elevation.grid;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.plugins.elevation.ElevationHelper;

public class EleVertex {
    private static final int NPOINTS = 3;
    private static final double MIN_DIST = 90;
    
    private double avrgEle = Double.NaN;
    private double area = Double.NaN;
    private EleCoordinate[] points = new EleCoordinate[NPOINTS];
    
    public EleVertex(EleCoordinate p1, EleCoordinate p2, EleCoordinate p3) {
	points[0] = p1;
	points[1] = p2;
	points[2] = p3;
	
	// compute elevation
	double z = 0D;
	boolean eleValid = true;
	for (EleCoordinate point : points) {
	    if (ElevationHelper.isValidElevation(p1.getEle())) {
		z += point.getEle();
	    } else {
		eleValid = false;
		break;
	    }
	}
	
	if (eleValid) {	
	    avrgEle = z / NPOINTS;
	} else {
	    avrgEle = ElevationHelper.NO_ELEVATION;
	}
	
	// compute the (approx.!) area of the vertex using heron's formula
	double a = p1.greatCircleDistance(p2);
	double b = p2.greatCircleDistance(p3);
	double c = p1.greatCircleDistance(p3);
	
	double s = (a + b + c) / 2D;
	double sq = s * (s - a) * (s - b) * (s - c);
	area = Math.sqrt(sq);
    }
    
    public List<EleVertex> divide() {
	TriangleEdge[] edges = new TriangleEdge[NPOINTS];
	
	int k = 0;
	for (int i = 0; i < points.length; i++) {
	    EleCoordinate c1 = points[i];
	    
	    for (int j = i + 1; j < points.length; j++) {
		EleCoordinate c2 = points[j];
		
		edges[k++] = new TriangleEdge(i, j, c1.greatCircleDistance(c2));
	    }
	}

	/*
	for (int i = 0; i < edges.length; i++) {
	    TriangleEdge triangleEdge = edges[i];
	    System.out.println("#" + i + ": " +triangleEdge);
	}*/
	
	// sort by distance
	Arrays.sort(edges);
	// pick the longest edge
	TriangleEdge longest = edges[0];
	
	
	//System.out.println("Longest " + longest);
	EleCoordinate pI = points[longest.getI()];
	EleCoordinate pJ = points[longest.getJ()];
	EleCoordinate pK = points[longest.getK()];
	EleCoordinate newP = getMid(pI, pJ);
	/*
	System.out.println(pI);
	System.out.println(pJ);
	System.out.println(pK);
	System.out.println(newP);
	*/
	List<EleVertex> res = new ArrayList<EleVertex>();
	res.add(new EleVertex(pI, pK, newP));
	res.add(new EleVertex(pJ, pK, newP));
	
	return res;
    }
    
    /**
     * Checks if vertex requires further processing or is finished. Currently this
     * method returns <code>true</code>, if the average deviation is < 5m 
     *
     * @return true, if is finished
     */
    public boolean isFinished() {
	double z = 0D;
	double avrgEle = getEle();
	
	for (EleCoordinate point : points) {
	    z += (avrgEle - point.getEle()) * (avrgEle - point.getEle());
	}
	
	// TODO: Check for proper limit
	return /*z < 75 || */getArea() < (30 * 30); // = 3 * 25
    }    
    
    /**
     * Gets the approximate area of this vertex in square meters.
     *
     * @return the area
     */
    public double getArea() {
        return area;
    }

    /**
     * Gets the (linear interpolated) mid point of c1 and c2.
     *
     * @param c1 the first coordinate
     * @param c2 the second coordinate
     * @return the mid point
     */
    public EleCoordinate getMid(EleCoordinate c1, EleCoordinate c2) {
	double x = (c1.getX() + c2.getX()) / 2.0;
	double y = (c1.getY() + c2.getY()) / 2.0;
	
	double z = (c1.getEle() + c2.getEle()) / 2.0;
	if (c1.greatCircleDistance(c2) > MIN_DIST) {
	    double hgtZ = ElevationHelper.getElevation(new LatLon(y, x));
	    
	    if (ElevationHelper.isValidElevation(hgtZ)) {
		z = hgtZ;
	    }	    
    	}
	
	return new EleCoordinate(y, x, z);
    }
    
    /**
     * Gets the coordinate for the given index.
     *
     * @param index the index between 0 and NPOINTS: 
     * @return the elevation coordinate instance
     * @throws IllegalArgumentException, if index is invalid
     */
    public EleCoordinate get(int index) {
	if (index < 0 || index >= NPOINTS) throw new IllegalArgumentException("Invalid index: " + index);
	
	return points[index];
    }
    
    /**
     * Gets the average elevation of this vertex.
     *
     * @return the ele
     */
    public double getEle() {
	
	return avrgEle;
    }
    
    @Override
    public String toString() {
	return "EleVertex [avrgEle=" + avrgEle + ", area=" + area + ", points="
		+ Arrays.toString(points) + "]";
    }




    class TriangleEdge implements Comparable<TriangleEdge> {
	private int i;
	private int j;
	private double dist;
	
	public TriangleEdge(int i, int j, double dist) {
	    super();
	    this.i = i;
	    this.j = j;
	    this.dist = dist;
	}

	public int getI() {
	    return i;
	}

	public int getJ() {
	    return j;
	}
	
	public int getK() {
	    if (i == 0) {
		return j == 1 ? 2 : 1;
	    } else if (i == 1) {
		return j == 0 ? 2 : 0;
	    } else {
		return j == 0 ? 1 : 0;
	    }
	}

	public double getDist() {
	    return dist;
	}

	@Override
	public int compareTo(TriangleEdge o) {
	    return (int) (o.getDist() - dist);
	}

	@Override
	public String toString() {
	    return "TriangleEdge [i=" + i + ", j=" + j + ", dist=" + dist + "]";
	}
    }
}

package com.tilusnet.josm.plugins.alignways.geometry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.openstreetmap.josm.data.coor.EastNorth;

public class AlignWaysGeomPoint {
    double x;
    double y;

    public AlignWaysGeomPoint(double x, double y) {
        setX(x);
        setY(y);
    }

    public AlignWaysGeomPoint(EastNorth eastNorth) {
    	this.x = eastNorth.getX();
    	this.y = eastNorth.getY();
	}

	public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

	public static boolean isSetCollinear(ArrayList<AlignWaysGeomPoint> awPts) {
		if (awPts.size() <= 1)
			return false;
		
		if (awPts.size() == 2)
			return true;
		else {
			// at least 3 points
			// First create a line of the first two points in the set
			AlignWaysGeomLine line = new AlignWaysGeomLine(awPts.get(0), awPts.get(1));
			// ...then check the subsequent points whether they are on the line
			for (int i = 2; i < awPts.size(); i++) {
				if (!line.isPointOnLine(awPts.get(i))) {
					return false;
				}
			}
			return true;
		}
	}

	/**
	 * Determines which (EastNorth) point falls between the other two.
	 * Ideally to be used with collinear points.
	 * 
	 * @return 1, 2 or 3 for pt1, pt2 and pt3, respectively. 
	 * 0 if middle value cannot be determined (i.e. some values are equal). 
	 */
	public static int getMiddleOf3(
			AlignWaysGeomPoint pt1, 
			AlignWaysGeomPoint pt2, 
			AlignWaysGeomPoint pt3) {
		
		int midPtXIdx = getMiddleOf3(pt1.x, pt2.x, pt3.x);
		int midPtYIdx = getMiddleOf3(pt1.y, pt2.y, pt3.y);
		
		if ((midPtXIdx == 0) && (midPtYIdx == 0))
			// All 3 points overlap: 
			// Design decision: return the middle point (could be any other or none)
			return 2;
		
		if (midPtXIdx == 0) return midPtYIdx; 
		if (midPtYIdx == 0) return midPtXIdx;
		
		// Both x and y middle points could be determined;
		// their indexes must coincide
		if (midPtXIdx == midPtYIdx)
			// Success
			return midPtXIdx; // (or midPtYIdx)
		else
			// Fail
			return 0;
		
	}

	/**
	 * Determine which value, d1, d2 or d3 falls in the middle of the other two.
	 * @return 1, 2 or 3 for d1, d2 and d3, respectively. 
	 * 0 if middle value cannot be determined (i.e. some values are equal). 
	 */
	private static int getMiddleOf3(double d1, double d2, double d3) {
		
		Double[] dValues = {d1, d2, d3};
		ArrayList<Double> alValues = new ArrayList<Double>(Arrays.asList(dValues));
		Collections.sort(alValues);
		
		if ((Math.abs(alValues.get(1) - alValues.get(0)) < 0.01) ||
		    (Math.abs(alValues.get(1) - alValues.get(2)) < 0.01))
			// Cannot determine absolute middle value
			return 0;
		else {
			if (Math.abs(alValues.get(1) - d1) < 0.01) return 1;
			if (Math.abs(alValues.get(1) - d2) < 0.01) return 2;
			if (Math.abs(alValues.get(1) - d3) < 0.01) return 3;
		}
		
		// Should never happen
		return 0;
	}
}

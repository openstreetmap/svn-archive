package org.openstreetmap.labelling.map;

import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.util.Vector;

public class LineFeature
	extends Feature
{
	private Vector<Line2D> lines;
	private Area lineArea;
    private double length;

	public LineFeature(String id, Vector<Line2D> lines, Area lineArea)
	{
        super(id);
		this.lines = lines;
		this.lineArea = lineArea;
        this.length = 0;
        for (Line2D l : lines)
        {
            this.length += Math.sqrt(Math.pow(l.getX2() - l.getX1(), 2) + Math.pow(l.getY2() - l.getY1(), 2)); 
        }
	}

	public Vector<Line2D> getLines()
	{
		return lines;
	}
    
    public double getLength()
    {
        return length;
    }

	public Area getLineArea()
	{
		return lineArea;
	}
}

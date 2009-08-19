package org.openstreetmap.labelling.map;

import java.awt.Shape;
import java.awt.geom.Point2D;

public class PointFeature
	extends Feature
{
	private Point2D point;
	private Shape shape;

	public PointFeature(String id, Point2D point, Shape shape)
	{
        super(id);
		this.point = point;
		this.shape = shape;
	}

	public Point2D getPoint()
	{
		return point;
	}

	public Shape getShape()
	{
		return shape;
	}
}

package org.openstreetmap.gui.jmapviewer;

//License: GPL. Copyright 2009

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;

import org.openstreetmap.gui.jmapviewer.interfaces.MapRectangle;


/**
 * An implementation of the {@link MapRectangle} interface 
 * to draw rectangular regions on the map.
 */
public class MapArea implements MapRectangle {

	Coordinate topLeft = new Coordinate(0, 0); 
	Coordinate bottomRight = new Coordinate(0, 0);
	double latHeight; 
	double lonWidth;
	Color colorBorder;
	Color colorFill;

	public MapArea(Coordinate topLeft, Coordinate bottomRight) {
		this(Color.BLACK, Color.GREEN, topLeft, bottomRight);
	}

	public MapArea(Color colorBorder, Color colorFill, 
				Coordinate topLeft, Coordinate bottomRight) {
		super();
		this.colorBorder = colorBorder;
		this.colorFill = colorFill;
		setRectangle(topLeft, bottomRight);
	}
	
	public void setRectangle(Coordinate topLeft, Coordinate bottomRight) {
		if (topLeft.getLat() > bottomRight.getLat()) {
			this.topLeft.setLat(topLeft.getLat());
			this.bottomRight.setLat(bottomRight.getLat());
		} else {
			this.topLeft.setLat(bottomRight.getLat());
			this.bottomRight.setLat(topLeft.getLat());
		}
		if (topLeft.getLon() < bottomRight.getLon()) {
			this.topLeft.setLon(topLeft.getLon());
			this.bottomRight.setLon(bottomRight.getLon());
		} else {
			this.topLeft.setLon(bottomRight.getLon());
			this.bottomRight.setLon(topLeft.getLon());
		}
		
		latHeight = bottomRight.getLat() - topLeft.getLat();
		lonWidth = topLeft.getLon() - bottomRight.getLon();
	}
	
	/* (non-Javadoc)
	 * @see org.openstreetmap.gui.jmapviewer.interfaces.MapSquare#getBottomRight()
	 */
	public Coordinate getBottomRight() {
		return bottomRight;
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.gui.jmapviewer.interfaces.MapSquare#getTopLeft()
	 */
	public Coordinate getTopLeft() {
		return topLeft;
	}

	public double getLat() {
		return latHeight;
	}

	public double getLon() {
		return lonWidth;
	}

	/* (non-Javadoc)
	 * @see org.openstreetmap.gui.jmapviewer.interfaces.MapSquare#paint(java.awt.Graphics, java.awt.Point, java.awt.Point)
	 */
	public void paint(Graphics g, Point topLeft, Point bottomRight) {
		if (topLeft == null || bottomRight == null) {
			return;
		}
		g.setColor(colorFill);
		g.fillRect(topLeft.x, topLeft.y, bottomRight.x - topLeft.x, bottomRight.y - topLeft.y);
		g.setColor(colorBorder);
		g.drawRect(topLeft.x, topLeft.y, bottomRight.x - topLeft.x, bottomRight.y - topLeft.y);
	}

	@Override
	public String toString() {
		return "Rectangular MapMarker at (" + topLeft.getLat() + "," + topLeft.getLon() + 
			"|" + bottomRight.getLat() + "," + bottomRight.getLon() + ") spans " + 
			latHeight + " " + lonWidth;
	}

}

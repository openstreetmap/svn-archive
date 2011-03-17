package org.openstreetmap.josm.plugins.turnlanes.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;

import org.openstreetmap.josm.plugins.turnlanes.model.Junction;
import org.openstreetmap.josm.plugins.turnlanes.model.Lane;
import org.openstreetmap.josm.plugins.turnlanes.model.Road;

class GuiContainer {
	static final Color RED = new Color(234, 66, 108);
	static final Color GREEN = new Color(66, 234, 108);
	
	private final Point2D translation;
	/**
	 * Meters per pixel.
	 */
	private final double mpp;
	/**
	 * Meters per source unit.
	 */
	private final double mpsu;
	private final double scale;
	private final double laneWidth;
	
	private final Map<Junction, JunctionGui> junctions = new HashMap<Junction, JunctionGui>();
	private final Map<Road, RoadGui> roads = new HashMap<Road, RoadGui>();
	
	private final Stroke connectionStroke;
	
	public GuiContainer(Point2D origin, double mpsu) {
		this.translation = new Point2D.Double(-origin.getX(), -origin.getY());
		this.mpp = 0.2;
		this.mpsu = mpsu;
		this.scale = mpsu / mpp;
		this.laneWidth = 2 / mpp;
		
		this.connectionStroke = new BasicStroke((float) (laneWidth / 4), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
	}
	
	public JunctionGui getGui(Junction j) {
		final JunctionGui existing = junctions.get(j);
		if (existing != null) {
			return existing;
		}
		
		return new JunctionGui(this, j);
	}
	
	void register(JunctionGui j) {
		if (junctions.put(j.getModel(), j) != null) {
			throw new IllegalStateException();
		}
	}
	
	public RoadGui getGui(Road r) {
		final RoadGui gui = roads.get(r);
		
		if (gui == null) {
			final RoadGui newGui = new RoadGui(this, r);
			roads.put(r, newGui);
			return newGui;
		}
		
		return gui;
	}
	
	Point2D translateAndScale(Point2D loc) {
		return new Point2D.Double((loc.getX() + translation.getX()) * scale, (loc.getY() + translation.getY()) * scale);
	}
	
	/**
	 * @return meters per pixel
	 */
	public double getMpp() {
		return mpp;
	}
	
	public double getScale() {
		return scale;
	}
	
	public double getLaneWidth() {
		return laneWidth;
	}
	
	public Stroke getConnectionStroke() {
		return connectionStroke;
	}
	
	public LaneGui getGui(Lane lane) {
		final RoadGui roadGui = roads.get(lane.getRoad());
		
		for (LaneGui l : roadGui.getLanes()) {
			if (l.getModel().equals(lane)) {
				return l;
			}
		}
		
		throw new IllegalArgumentException("No such lane.");
	}
	
	public GuiContainer empty() {
		return new GuiContainer(new Point2D.Double(-translation.getX(), -translation.getY()), mpsu);
	}
}

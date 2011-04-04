package org.openstreetmap.josm.plugins.turnlanes.gui;

import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.hypot;
import static java.lang.Math.max;
import static java.lang.Math.tan;
import static org.openstreetmap.josm.plugins.turnlanes.gui.GuiUtil.angle;
import static org.openstreetmap.josm.plugins.turnlanes.gui.GuiUtil.closest;
import static org.openstreetmap.josm.plugins.turnlanes.gui.GuiUtil.cpf;
import static org.openstreetmap.josm.plugins.turnlanes.gui.GuiUtil.intersection;
import static org.openstreetmap.josm.plugins.turnlanes.gui.GuiUtil.loc;
import static org.openstreetmap.josm.plugins.turnlanes.gui.GuiUtil.normalize;
import static org.openstreetmap.josm.plugins.turnlanes.gui.GuiUtil.relativePoint;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import org.openstreetmap.josm.plugins.turnlanes.model.Junction;
import org.openstreetmap.josm.plugins.turnlanes.model.Road;
import org.openstreetmap.josm.plugins.turnlanes.model.Turn;

class JunctionGui {
	private final class TurnConnection extends InteractiveElement {
		private final Turn turn;
		
		private Point2D dragBegin;
		private double dragOffsetX = 0;
		private double dragOffsetY = 0;
		
		public TurnConnection(Turn turn) {
			this.turn = turn;
		}
		
		@Override
		void paint(Graphics2D g2d, State state) {
			if (isVisible(state)) {
				g2d.setStroke(getContainer().getConnectionStroke());
				g2d.setColor(isRemoveDragOffset() ? GuiContainer.RED : GuiContainer.GREEN);
				g2d.translate(dragOffsetX, dragOffsetY);
				g2d.draw(getPath());
				g2d.translate(-dragOffsetX, -dragOffsetY);
			}
		}
		
		private Path2D getPath() {
			final Path2D path = new Path2D.Double();
			
			final LaneGui laneGui = getContainer().getGui(turn.getFrom());
			final RoadGui roadGui = getContainer().getGui(turn.getTo().getRoad());
			
			path.moveTo(laneGui.outgoing.getCenter().getX(), laneGui.outgoing.getCenter().getY());
			
			Junction j = laneGui.getModel().getOutgoingJunction();
			for (Road v : turn.getVia()) {
				final PathIterator it;
				if (v.getFromEnd().getJunction().equals(j)) {
					it = getContainer().getGui(v).getLaneMiddle(true).getIterator();
					j = v.getToEnd().getJunction();
				} else {
					it = getContainer().getGui(v).getLaneMiddle(false).getIterator();
					j = v.getFromEnd().getJunction();
				}
				
				path.append(it, true);
			}
			
			path.lineTo(roadGui.getConnector(turn.getTo()).getCenter().getX(), roadGui.getConnector(turn.getTo()).getCenter()
			    .getY());
			
			return path;
		}
		
		private boolean isVisible(State state) {
			if (state instanceof State.AllTurns) {
				return true;
			} else if (state instanceof State.OutgoingActive) {
				return turn.getFrom().equals(((State.OutgoingActive) state).getLane().getModel());
			} else if (state instanceof State.IncomingActive) {
				return turn.getTo().equals(((State.IncomingActive) state).getRoadEnd());
			}
			
			return false;
		}
		
		@Override
		boolean contains(Point2D p, State state) {
			if (!isVisible(state)) {
				return false;
			}
			
			final PathIterator it = new FlatteningPathIterator(getPath().getPathIterator(null), 0.05 / getContainer()
			    .getMpp());
			final double[] coords = new double[6];
			double lastX = 0;
			double lastY = 0;
			while (!it.isDone()) {
				if (it.currentSegment(coords) == PathIterator.SEG_LINETO) {
					final Point2D closest = closest(new Line2D.Double(lastX, lastY, coords[0], coords[1]), p);
					
					if (p.distance(closest) <= strokeWidth() / 2) {
						return true;
					}
				}
				
				lastX = coords[0];
				lastY = coords[1];
				it.next();
			}
			
			return false;
		}
		
		private double strokeWidth() {
			final BasicStroke stroke = (BasicStroke) getContainer().getConnectionStroke();
			return stroke.getLineWidth();
		}
		
		@Override
		Type getType() {
			return Type.TURN_CONNECTION;
		}
		
		@Override
		int getZIndex() {
			return 0;
		}
		
		@Override
		boolean beginDrag(double x, double y) {
			dragBegin = new Point2D.Double(x, y);
			dragOffsetX = 0;
			dragOffsetY = 0;
			return true;
		}
		
		@Override
		State drag(double x, double y, InteractiveElement target, State old) {
			dragOffsetX = x - dragBegin.getX();
			dragOffsetY = y - dragBegin.getY();
			return old;
		}
		
		@Override
		State drop(double x, double y, InteractiveElement target, State old) {
			drag(x, y, target, old);
			
			if (isRemoveDragOffset()) {
				turn.remove();
			}
			
			dragBegin = null;
			dragOffsetX = 0;
			dragOffsetY = 0;
			return new State.Dirty(old);
		}
		
		private boolean isRemoveDragOffset() {
			final double r = getContainer().getGui(turn.getFrom().getRoad()).connectorRadius;
			final double max = r - strokeWidth() / 2;
			return hypot(dragOffsetX, dragOffsetY) > max;
		}
	}
	
	private final class Corner {
		final double x1;
		final double y1;
		
		final double cx1;
		final double cy1;
		
		final double cx2;
		final double cy2;
		
		final double x2;
		final double y2;
		
		public Corner(Point2D c1, Point2D cp1, Point2D cp2, Point2D c2) {
			this.x1 = c1.getX();
			this.y1 = c1.getY();
			this.cx1 = cp1.getX();
			this.cy1 = cp1.getY();
			this.cx2 = cp2.getX();
			this.cy2 = cp2.getY();
			this.x2 = c2.getX();
			this.y2 = c2.getY();
		}
		
		@Override
		public String toString() {
			return "Corner [x1=" + x1 + ", y1=" + y1 + ", cx1=" + cx1 + ", cy1=" + cy1 + ", cx2=" + cx2 + ", cy2=" + cy2
			    + ", x2=" + x2 + ", y2=" + y2 + "]";
		}
	}
	
	private final class Linkage implements Comparable<Linkage> {
		final RoadGui roadGui;
		final Road.End roadEnd;
		final double angle;
		
		double lTrim;
		double rTrim;
		
		public Linkage(Road.End roadEnd) {
			this.roadGui = getContainer().getGui(roadEnd.getRoad());
			this.roadEnd = roadEnd;
			this.angle = normalize(roadGui.getAngle(roadEnd) + PI);
			
			roads.put(angle, this);
		}
		
		@Override
		public int compareTo(Linkage o) {
			return Double.compare(angle, o.angle);
		}
		
		public void trimLeft(Linkage right) {
			right.trimRight(this);
			
			final Line2D leftCurb = roadGui.getLeftCurb(roadEnd);
			final Line2D rightCurb = right.roadGui.getRightCurb(right.roadEnd);
			
			final double leftAngle = angle(leftCurb);
			final double rightAngle = angle(rightCurb);
			
			final Point2D isect;
			if (abs(PI - normalize(rightAngle - leftAngle)) > PI / 12) {
				isect = intersection(leftCurb, rightCurb);
			} else {
				isect = GuiUtil.relativePoint(leftCurb.getP1(), roadGui.getWidth(roadEnd) / 2, angle);
			}
			
			if (Math.abs(leftAngle - angle(leftCurb.getP1(), isect)) < 0.1) {
				lTrim = leftCurb.getP1().distance(isect);
			}
		}
		
		private void trimRight(Linkage left) {
			final Line2D rightCurb = roadGui.getRightCurb(roadEnd);
			final Line2D leftCurb = left.roadGui.getLeftCurb(left.roadEnd);
			
			final double rightAngle = angle(rightCurb);
			final double leftAngle = angle(leftCurb);
			
			final Point2D isect;
			if (abs(PI - normalize(rightAngle - leftAngle)) > PI / 12) {
				isect = intersection(rightCurb, leftCurb);
			} else {
				isect = GuiUtil.relativePoint(rightCurb.getP1(), roadGui.getWidth(roadEnd) / 2, angle);
			}
			
			if (Math.abs(rightAngle - angle(rightCurb.getP1(), isect)) < 0.1) {
				rTrim = rightCurb.getP1().distance(isect);
			}
		}
		
		public void trimAdjust() {
			final double MAX_TAN = tan(PI / 2 - MAX_ANGLE);
			
			final double sin = roadGui.getWidth(roadEnd);
			final double cos = abs(lTrim - rTrim);
			final double tan = sin / cos;
			
			if (tan < MAX_TAN) {
				lTrim = max(lTrim, rTrim - sin / MAX_TAN);
				rTrim = max(rTrim, lTrim - sin / MAX_TAN);
			}
			
			lTrim += container.getLaneWidth() / 2;
			rTrim += container.getLaneWidth() / 2;
		}
	}
	
	// max angle between corners
	private static final double MAX_ANGLE = Math.toRadians(30);
	
	private final GuiContainer container;
	private final Junction junction;
	
	final double x;
	final double y;
	
	private final NavigableMap<Double, Linkage> roads = new TreeMap<Double, Linkage>();
	
	private final Path2D area = new Path2D.Double();
	
	public JunctionGui(GuiContainer container, Junction j) {
		this.container = container;
		this.junction = j;
		
		container.register(this);
		
		final Point2D loc = container.translateAndScale(loc(j.getNode()));
		this.x = loc.getX();
		this.y = loc.getY();
		
		final Set<Road> done = new HashSet<Road>();
		for (Road r : j.getRoads()) {
			if (!done.contains(r)) {
				done.add(r);
				
				if (r.getFromEnd().getJunction().equals(j)) {
					new Linkage(r.getFromEnd());
				}
				if (r.getToEnd().getJunction().equals(j)) {
					new Linkage(r.getToEnd());
				}
			}
		}
		
		recalculate();
	}
	
	void recalculate() {
		for (Linkage l : roads.values()) {
			l.lTrim = 0;
			l.rTrim = 0;
		}
		
		area.reset();
		if (roads.size() < 2) {
			return;
		}
		
		Linkage last = roads.lastEntry().getValue();
		for (Linkage l : roads.values()) {
			l.trimLeft(last);
			last = l;
		}
		for (Linkage l : roads.values()) {
			l.trimAdjust();
		}
		
		boolean first = true;
		for (Corner c : corners()) {
			if (first) {
				area.moveTo(c.x1, c.y1);
				first = false;
			} else {
				area.lineTo(c.x1, c.y1);
			}
			
			area.curveTo(c.cx1, c.cy1, c.cx2, c.cy2, c.x2, c.y2);
		}
		
		area.closePath();
	}
	
	private Iterable<Corner> corners() {
		final List<Corner> result = new ArrayList<JunctionGui.Corner>(roads.size());
		
		Linkage last = roads.lastEntry().getValue();
		for (Linkage l : roads.values()) {
			result.add(corner(last, l));
			last = l;
		}
		
		return result;
	}
	
	private Corner corner(Linkage right, Linkage left) {
		final Line2D rightCurb = right.roadGui.getRightCurb(right.roadEnd);
		final Line2D leftCurb = left.roadGui.getLeftCurb(left.roadEnd);
		
		final double rightAngle = angle(rightCurb);
		final double leftAngle = angle(leftCurb);
		
		final double delta = normalize(leftAngle - rightAngle);
		
		final boolean wide = delta > PI;
		final double a = wide ? max(0, delta - (PI + 2 * MAX_ANGLE)) : delta;
		
		final double cpf1 = cpf(a, container.getLaneWidth() / 2 + (wide ? right.roadGui.getWidth(right.roadEnd) : 0));
		final double cpf2 = cpf(a, container.getLaneWidth() / 2 + (wide ? left.roadGui.getWidth(left.roadEnd) : 0));
		
		final Point2D c1 = relativePoint(rightCurb.getP1(), cpf1, right.angle + PI);
		final Point2D c2 = relativePoint(leftCurb.getP1(), cpf2, left.angle + PI);
		
		return new Corner(rightCurb.getP1(), c1, c2, leftCurb.getP1());
	}
	
	public Set<RoadGui> getRoads() {
		final Set<RoadGui> result = new HashSet<RoadGui>();
		
		for (Linkage l : roads.values()) {
			result.add(l.roadGui);
		}
		
		return Collections.unmodifiableSet(result);
	}
	
	double getLeftTrim(Road.End end) {
		return getLinkage(end).lTrim;
	}
	
	private Linkage getLinkage(Road.End end) {
		final double a = normalize(getContainer().getGui(end.getRoad()).getAngle(end) + PI);
		final Map.Entry<Double, Linkage> e = roads.floorEntry(a);
		return e != null ? e.getValue() : null;
	}
	
	double getRightTrim(Road.End end) {
		return getLinkage(end).rTrim;
	}
	
	Point2D getPoint() {
		return new Point2D.Double(x, y);
	}
	
	public GuiContainer getContainer() {
		return container;
	}
	
	public Junction getModel() {
		return junction;
	}
	
	public List<InteractiveElement> paint(Graphics2D g2d) {
		g2d.setColor(new Color(96, 96, 96));
		g2d.fill(area);
		
		final List<InteractiveElement> result = new ArrayList<InteractiveElement>();
		
		if (getModel().isPrimary()) {
			for (Road.End r : new HashSet<Road.End>(getModel().getRoadEnds())) {
				for (Turn t : r.getTurns()) {
					result.add(new TurnConnection(t));
				}
			}
		}
		
		return result;
	}
	
	public Rectangle2D getBounds() {
		return area.getBounds2D();
	}
}

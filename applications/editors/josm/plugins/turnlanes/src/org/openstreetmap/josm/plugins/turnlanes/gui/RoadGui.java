package org.openstreetmap.josm.plugins.turnlanes.gui;

import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.tan;
import static org.openstreetmap.josm.plugins.turnlanes.gui.GuiUtil.angle;
import static org.openstreetmap.josm.plugins.turnlanes.gui.GuiUtil.area;
import static org.openstreetmap.josm.plugins.turnlanes.gui.GuiUtil.closest;
import static org.openstreetmap.josm.plugins.turnlanes.gui.GuiUtil.intersection;
import static org.openstreetmap.josm.plugins.turnlanes.gui.GuiUtil.line;
import static org.openstreetmap.josm.plugins.turnlanes.gui.GuiUtil.loc;
import static org.openstreetmap.josm.plugins.turnlanes.gui.GuiUtil.minAngleDiff;
import static org.openstreetmap.josm.plugins.turnlanes.gui.GuiUtil.relativePoint;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.turnlanes.CollectionUtils;
import org.openstreetmap.josm.plugins.turnlanes.model.Lane;
import org.openstreetmap.josm.plugins.turnlanes.model.Road;
import org.openstreetmap.josm.plugins.turnlanes.model.Utils;

class RoadGui {
	private final class Extender extends InteractiveElement {
		private final Way way;
		
		private final Line2D line;
		
		public Extender(Way way, double angle) {
			this.way = way;
			this.line = new Line2D.Double(a.getPoint(), relativePoint(a.getPoint(), getContainer().getLaneWidth() * 4, angle));
		}
		
		@Override
		void paint(Graphics2D g2d, State state) {
			g2d.setStroke(getContainer().getConnectionStroke());
			g2d.setColor(Color.CYAN);
			g2d.draw(line);
		}
		
		@Override
		boolean contains(Point2D p, State state) {
			final BasicStroke stroke = (BasicStroke) getContainer().getConnectionStroke();
			final double strokeWidth = stroke.getLineWidth();
			
			final Point2D closest = closest(line, p);
			return p.distance(closest) <= strokeWidth / 2;
		}
		
		@Override
		State click(State old) {
			getModel().extend(way);
			return new State.Invalid(old);
		}
		
		@Override
		Type getType() {
			return Type.EXTENDER;
		}
		
		@Override
		int getZIndex() {
			return 0;
		}
		
	}
	
	private final class LaneAdder extends InteractiveElement {
		private final Road.End end;
		private final boolean left;
		
		private final Point2D center;
		private final Ellipse2D background;
		
		public LaneAdder(Road.End end, boolean left) {
			this.end = end;
			this.left = left;
			
			final double a = getAngle(end) + PI;
			final Point2D lc = getLeftCorner(end);
			final Point2D rc = getRightCorner(end);
			
			final double r = connectorRadius;
			final double cx;
			final double cy;
			if (left) {
				final JunctionGui j = getContainer().getGui(end.getJunction());
				final Point2D i = intersection(line(j.getPoint(), a), new Line2D.Double(lc, rc));
				
				cx = i.getX() + 21d / 16 * r * (2 * cos(a) + cos(a - PI / 2));
				cy = i.getY() - 21d / 16 * r * (2 * sin(a) + sin(a - PI / 2));
			} else {
				cx = rc.getX() + 21d / 16 * r * (2 * cos(a) + cos(a + PI / 2));
				cy = rc.getY() - 21d / 16 * r * (2 * sin(a) + sin(a + PI / 2));
			}
			
			center = new Point2D.Double(cx, cy);
			background = new Ellipse2D.Double(cx - r, cy - r, 2 * r, 2 * r);
		}
		
		@Override
		void paint(Graphics2D g2d, State state) {
			if (!isVisible(state)) {
				return;
			}
			
			g2d.setColor(Color.DARK_GRAY);
			g2d.fill(background);
			
			final double l = 2 * connectorRadius / 3;
			final Line2D v = new Line2D.Double(center.getX(), center.getY() - l, center.getX(), center.getY() + l);
			final Line2D h = new Line2D.Double(center.getX() - l, center.getY(), center.getX() + l, center.getY());
			
			g2d.setStroke(new BasicStroke((float) (connectorRadius / 5)));
			g2d.setColor(Color.WHITE);
			g2d.draw(v);
			g2d.draw(h);
		}
		
		private boolean isVisible(State state) {
			return end.getJunction().equals(state.getJunction().getModel());
		}
		
		@Override
		boolean contains(Point2D p, State state) {
			return isVisible(state) && background.contains(p);
		}
		
		@Override
		Type getType() {
			return Type.LANE_ADDER;
		}
		
		@Override
		int getZIndex() {
			return 2;
		}
		
		@Override
		public State click(State old) {
			end.addExtraLane(left);
			return new State.Invalid(old);
		}
	}
	
	final class IncomingConnector extends InteractiveElement {
		private final boolean forward;
		private final Point2D center = new Point2D.Double();
		private final Ellipse2D circle = new Ellipse2D.Double();
		
		private final List<LaneGui> lanes = new ArrayList<LaneGui>();
		
		private IncomingConnector(boolean forward) {
			this.forward = forward;
		}
		
		@Override
		public void paintBackground(Graphics2D g2d, State state) {
			if (isActive(state)) {
				final Composite old = g2d.getComposite();
				g2d.setComposite(((AlphaComposite) old).derive(0.2f));
				
				g2d.setColor(new Color(255, 127, 31));
				
				for (LaneGui l : lanes) {
					l.fill(g2d);
				}
				
				g2d.setComposite(old);
			}
		}
		
		@Override
		public void paint(Graphics2D g2d, State state) {
			if (isVisible(state)) {
				final Composite old = g2d.getComposite();
				if (isActive(state)) {
					g2d.setComposite(((AlphaComposite) old).derive(1f));
				}
				
				g2d.setColor(Color.LIGHT_GRAY);
				g2d.fill(circle);
				
				g2d.setComposite(old);
			}
		}
		
		private boolean isActive(State state) {
			if (!(state instanceof State.IncomingActive)) {
				return false;
			}
			
			final Road.End roadEnd = ((State.IncomingActive) state).getRoadEnd();
			
			return roadEnd.equals(getRoadEnd());
		}
		
		private boolean isVisible(State state) {
			if (!getRoadEnd().getJunction().equals(state.getJunction().getModel())) {
				return false;
			}
			
			// must be at least one lane in that direction
			final boolean reverse = getRoadEnd().isToEnd();
			for (Lane l : getModel().getLanes()) {
				if (l.isReverse() == reverse) {
					return true;
				}
			}
			
			return false;
		}
		
		@Override
		public boolean contains(Point2D p, State state) {
			if (!isVisible(state)) {
				return false;
			} else if (circle.contains(p)) {
				return true;
			}
			
			for (LaneGui l : lanes) {
				if (l.contains(p)) {
					return true;
				}
			}
			
			return false;
		}
		
		@Override
		public Type getType() {
			return Type.INCOMING_CONNECTOR;
		}
		
		@Override
		public State activate(State old) {
			return new State.IncomingActive(old.getJunction(), getRoadEnd());
		}
		
		public Point2D getCenter() {
			return (Point2D) center.clone();
		}
		
		void move(double x, double y) {
			final double r = connectorRadius;
			
			center.setLocation(x, y);
			circle.setFrame(x - r, y - r, 2 * r, 2 * r);
		}
		
		Road.End getRoadEnd() {
			return forward ? getModel().getFromEnd() : getModel().getToEnd();
		}
		
		@Override
		int getZIndex() {
			return 1;
		}
		
		public void add(LaneGui lane) {
			lanes.add(lane);
		}
	}
	
	// TODO rework to be a SegmentGui (with getModel())
	private final class Segment {
		final Point2D to;
		final Point2D from;
		
		final Segment prev;
		final Segment next;
		
		final double length;
		final double angle;
		
		public Segment(Segment next, List<Point2D> bends, JunctionGui a) {
			final Point2D head = (Point2D) bends.get(0).clone();
			final List<Point2D> tail = bends.subList(1, bends.size());
			
			this.next = next;
			this.to = head;
			this.from = (Point2D) (tail.isEmpty() ? a.getPoint() : tail.get(0)).clone();
			this.prev = tail.isEmpty() ? null : new Segment(this, tail, a);
			this.length = from.distance(to);
			this.angle = angle(from, to);
			
			// TODO create a factory method for the segments list and pass it to
			// the constructor(s)
			segments.add(this);
		}
		
		public Segment(JunctionGui b, List<Point2D> bends, JunctionGui a) {
			this((Segment) null, prepended(bends, (Point2D) b.getPoint().clone()), a);
		}
		
		private double getFromOffset() {
			return prev == null ? 0 : prev.getFromOffset() + prev.length;
		}
		
		public double getOffset(double x, double y) {
			return getOffsetInternal(new Point2D.Double(x, y), -1, Double.POSITIVE_INFINITY);
		}
		
		private double getOffsetInternal(Point2D p, double offset, double quality) {
			final Point2D closest = closest(new Line2D.Double(from, to), p);
			final double myQuality = closest.distance(p);
			
			if (myQuality < quality) {
				quality = myQuality;
				
				final Line2D normal = line(p, angle + PI / 2);
				final Point2D isect = intersection(normal, new Line2D.Double(from, to));
				final double d = from.distance(isect);
				final boolean negative = Math.abs(angle(from, isect) - angle) > 1;
				
				offset = getFromOffset() + (negative ? -1 : 1) * d;
			}
			
			return next == null ? offset : next.getOffsetInternal(p, offset, quality);
		}
		
		public Path append(Path path, boolean forward, double offset) {
			if (ROUND_CORNERS) {
				final Segment n = forward ? prev : next;
				final Point2D s = forward ? to : from;
				final Point2D e = forward ? from : to;
				
				if (n == null) {
					return path.lineTo(e.getX(), e.getY(), length - offset);
				}
				
				final double a = minAngleDiff(angle, n.angle);
				final double d = 3 * outerMargin + getWidth(getModel().getToEnd(), (forward && a < 0) || (!forward && a > 0));
				final double l = d * tan(abs(a));
				
				if (length - offset < l / 2 || n.length < l / 2) {
					return n.append(path.lineTo(e.getX(), e.getY(), length - offset), forward, 0);
				} else {
					final Point2D p = relativePoint(e, l / 2, angle(e, s));
					
					final Path line = path.lineTo(p.getX(), p.getY(), length - l / 2 - offset);
					final Path curve = line.curveTo(d, d, a, l);
					
					return n.append(curve, forward, l / 2);
				}
			} else if (forward) {
				final Path tmp = path.lineTo(from.getX(), from.getY(), length);
				return prev == null ? tmp : prev.append(tmp, forward, 0);
			} else {
				final Path tmp = path.lineTo(to.getX(), to.getY(), length);
				return next == null ? tmp : next.append(tmp, forward, 0);
			}
		}
	}
	
	/**
	 * This should become a setting, but rounding is (as of yet) still slightly buggy and a low
	 * priority.
	 */
	private static final boolean ROUND_CORNERS = false;
	
	private static final List<Point2D> prepended(List<Point2D> bends, Point2D point) {
		final List<Point2D> result = new ArrayList<Point2D>(bends.size() + 1);
		result.add(point);
		result.addAll(bends);
		return result;
	}
	
	private final GuiContainer container;
	private final double innerMargin;
	private final double outerMargin;
	
	private final float lineWidth;
	private final Stroke regularStroke;
	private final Stroke dashedStroke;
	
	private final JunctionGui a;
	private final JunctionGui b;
	private final double length;
	
	private final IncomingConnector incomingA = new IncomingConnector(true);
	private final IncomingConnector incomingB = new IncomingConnector(false);
	
	private final Road road;
	private final List<LaneGui> lanes = new ArrayList<LaneGui>();
	private final List<Segment> segments = new ArrayList<Segment>();
	
	final double connectorRadius;
	
	public RoadGui(GuiContainer container, Road r) {
		this.container = container;
		
		this.a = container.getGui(r.getFromEnd().getJunction());
		this.b = container.getGui(r.getToEnd().getJunction());
		
		this.road = r;
		
		final List<Point2D> bends = new ArrayList<Point2D>();
		final List<Node> nodes = r.getRoute().getNodes();
		for (int i = nodes.size() - 2; i > 0; --i) {
			bends.add(container.translateAndScale(loc(nodes.get(i))));
		}
		
		// they add themselves to this.segments
		new Segment(b, bends, a);
		
		double l = 0;
		for (Segment s : segments) {
			l += s.length;
		}
		
		this.length = l;
		
		for (Lane lane : r.getLanes()) {
			final LaneGui gui = new LaneGui(this, lane);
			lanes.add(gui);
			(lane.isReverse() ? incomingB : incomingA).add(gui);
		}
		
		this.innerMargin = getLaneCount(true) > 0 && getLaneCount(false) > 0 ? 1 * container.getLaneWidth() / 15 : 0;
		this.outerMargin = container.getLaneWidth() / 6;
		this.connectorRadius = 3 * container.getLaneWidth() / 8;
		this.lineWidth = (float) (container.getLaneWidth() / 30);
		this.regularStroke = new BasicStroke(2 * lineWidth);
		this.dashedStroke = new BasicStroke(lineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10f, new float[] {
		    (float) (container.getLaneWidth() / 2), (float) (container.getLaneWidth() / 3)
		}, 0);
	}
	
	public JunctionGui getA() {
		return a;
	}
	
	public JunctionGui getB() {
		return b;
	}
	
	private int laneMiddle() {
		int i = 0;
		while (lanes.size() > i && lanes.get(i).getModel().isReverse()) {
			++i;
		}
		return i;
	}
	
	public Line2D getLeftCurb(Road.End end) {
		return GuiUtil.line(getCorner(end, true), getAngle(end) + PI);
	}
	
	public Line2D getRightCurb(Road.End end) {
		return GuiUtil.line(getCorner(end, false), getAngle(end) + PI);
	}
	
	private Point2D getLeftCorner(Road.End end) {
		return getCorner(end, true);
	}
	
	private Point2D getRightCorner(Road.End end) {
		return getCorner(end, false);
	}
	
	private Point2D getCorner(Road.End end, boolean left) {
		final JunctionGui j = end.isFromEnd() ? a : b;
		final double w = left ? getWidth(end, true) : getWidth(end, false);
		final double s = (left ? 1 : -1);
		final double a = getAngle(end) + PI;
		final double t = left ? j.getLeftTrim(end) : j.getRightTrim(end);
		
		final double dx = s * cos(PI / 2 - a) * w + cos(a) * t;
		final double dy = s * sin(PI / 2 - a) * w - sin(a) * t;
		
		return new Point2D.Double(j.x + dx, j.y + dy);
	}
	
	private double getWidth(Road.End end, boolean left) {
		if (!end.getRoad().equals(road)) {
			throw new IllegalArgumentException();
		}
		
		final int lcForward = getLaneCount(false);
		final int lcBackward = getLaneCount(true);
		
		final double LW = getContainer().getLaneWidth();
		final double M = innerMargin + outerMargin;
		
		if (end.isToEnd()) {
			return (left ? lcBackward : lcForward) * LW + M;
		} else {
			return (left ? lcForward : lcBackward) * LW + M;
		}
	}
	
	private int getLaneCount(boolean reverse) {
		int result = 0;
		
		for (LaneGui l : lanes) {
			if (l.getModel().isReverse() == reverse) {
				++result;
			}
		}
		
		return result;
	}
	
	List<InteractiveElement> paint(Graphics2D g2d) {
		final List<InteractiveElement> result = new ArrayList<InteractiveElement>();
		
		result.addAll(paintLanes(g2d));
		result.addAll(laneAdders());
		result.addAll(extenders());
		
		g2d.setColor(Color.RED);
		for (Segment s : segments) {
			g2d.fill(new Ellipse2D.Double(s.from.getX() - 1, s.from.getY() - 1, 2, 2));
		}
		
		return result;
	}
	
	private List<LaneAdder> laneAdders() {
		final List<LaneAdder> result = new ArrayList<LaneAdder>(4);
		
		boolean to = false;
		boolean from = false;
		for (Lane l : getModel().getLanes()) {
			to = to || (!l.isReverse() && !l.isExtra());
			from = from || (l.isReverse() && !l.isExtra());
		}
		
		if (to) {
			result.add(new LaneAdder(getModel().getToEnd(), true));
			result.add(new LaneAdder(getModel().getToEnd(), false));
		}
		
		if (from) {
			result.add(new LaneAdder(getModel().getFromEnd(), true));
			result.add(new LaneAdder(getModel().getFromEnd(), false));
		}
		
		return result;
	}
	
	private List<Extender> extenders() {
		if (!getModel().isExtendable()) {
			return Collections.emptyList();
		}
		
		final List<Extender> result = new ArrayList<Extender>();
		
		final Node n = a.getModel().getNode();
		for (OsmPrimitive p : n.getReferrers()) {
			if (p.getType() != OsmPrimitiveType.WAY) {
				continue;
			}
			
			Way w = (Way) p;
			if (w.isFirstLastNode(n) && w.getNodesCount() > 1 && Utils.isRoad(w)) {
				if (getModel().getRoute().getFirstSegment().getWay().equals(w)) {
					continue;
				}
				
				final Node nextNode = w.firstNode().equals(n) ? w.getNode(1) : w.getNode(w.getNodesCount() - 2);
				final Point2D nextNodeLoc = getContainer().translateAndScale(loc(nextNode));
				result.add(new Extender(w, angle(a.getPoint(), nextNodeLoc)));
			}
		}
		
		return result;
	}
	
	public Road getModel() {
		return road;
	}
	
	public IncomingConnector getConnector(Road.End end) {
		return end.isFromEnd() ? incomingA : incomingB;
	}
	
	private List<InteractiveElement> paintLanes(Graphics2D g2d) {
		final Path2D middleLines = new Path2D.Double();
		
		g2d.setStroke(regularStroke);
		
		final boolean forward = getLaneCount(false) > 0;
		final boolean backward = getLaneCount(true) > 0;
		
		final Path2D middleArea;
		if (forward && backward) {
			paintLanes(g2d, middleLines, true);
			paintLanes(g2d, middleLines, false);
			
			middleLines.closePath();
			middleArea = middleLines;
			g2d.setColor(new Color(160, 160, 160));
		} else if (forward || backward) {
			paintLanes(g2d, middleLines, forward);
			
			middleArea = new Path2D.Double();
			middleArea.append(middleLines.getPathIterator(null), false);
			middleArea.append(middlePath(backward).offset(outerMargin, -1, -1, outerMargin).getIterator(), true);
			middleArea.closePath();
			g2d.setColor(Color.GRAY);
		} else {
			throw new AssertionError();
		}
		
		g2d.fill(middleArea);
		g2d.setColor(Color.WHITE);
		g2d.draw(middleLines);
		
		moveIncoming(getModel().getFromEnd());
		moveIncoming(getModel().getToEnd());
		
		final int laneMiddle = laneMiddle();
		for (int i = laneMiddle; i < lanes.size(); ++i) {
			moveOutgoing(lanes.get(i), i - laneMiddle);
		}
		for (int i = laneMiddle - 1; i >= 0; --i) {
			moveOutgoing(lanes.get(i), laneMiddle - i - 1);
		}
		
		final List<InteractiveElement> result = new ArrayList<InteractiveElement>();
		
		result.add(incomingA);
		result.add(incomingB);
		for (LaneGui l : lanes) {
			result.add(l.outgoing);
			
			if (l.getModel().isExtra()) {
				result.add(l.lengthSlider);
			}
		}
		
		return result;
	}
	
	private void paintLanes(Graphics2D g2d, Path2D middleLines, boolean forward) {
		final Shape clip = clip();
		g2d.clip(clip);
		
		final Path middle = middlePath(forward);
		
		Path innerPath = middle.offset(innerMargin, -1, -1, innerMargin);
		final List<Path> linePaths = new ArrayList<Path>();
		linePaths.add(innerPath);
		
		for (LaneGui l : lanes(forward)) {
			l.setClip(clip);
			innerPath = l.recalculate(innerPath, middleLines);
			linePaths.add(innerPath);
		}
		
		final Path2D area = new Path2D.Double();
		area(area, middle, innerPath.offset(outerMargin, -1, -1, outerMargin));
		g2d.setColor(Color.GRAY);
		g2d.fill(area);
		
		g2d.setColor(Color.WHITE);
		final Path2D lines = new Path2D.Double();
		lines.append(innerPath.getIterator(), false);
		g2d.draw(lines);
		
		// g2d.setColor(new Color(32, 128, 192));
		g2d.setColor(Color.WHITE);
		g2d.setStroke(dashedStroke);
		for (Path p : linePaths) {
			lines.reset();
			lines.append(p.getIterator(), false);
			g2d.draw(lines);
		}
		g2d.setStroke(regularStroke);
		
		// g2d.setColor(new Color(32, 128, 192));
		// lines.reset();
		// lines.append(middle.getIterator(), false);
		// g2d.draw(lines);
		
		g2d.setClip(null);
	}
	
	private Shape clip() {
		final Area clip = new Area(new Rectangle2D.Double(-100000, -100000, 200000, 200000));
		clip.subtract(new Area(negativeClip(true)));
		clip.subtract(new Area(negativeClip(false)));
		
		return clip;
	}
	
	private Shape negativeClip(boolean forward) {
		final Road.End end = forward ? getModel().getToEnd() : getModel().getFromEnd();
		final JunctionGui j = forward ? b : a;
		
		final Line2D lc = getLeftCurb(end);
		final Line2D rc = getRightCurb(end);
		
		final Path2D negativeClip = new Path2D.Double();
		
		final double d = rc.getP1().distance(j.getPoint()) + lc.getP1().distance(j.getPoint());
		
		final Point2D r1 = relativePoint(rc.getP1(), 1, angle(lc.getP1(), rc.getP1()));
		final Point2D r2 = relativePoint(r1, d, angle(rc) + PI);
		final Point2D l1 = relativePoint(lc.getP1(), 1, angle(rc.getP1(), lc.getP1()));
		final Point2D l2 = relativePoint(l1, d, angle(lc) + PI);
		
		negativeClip.moveTo(r1.getX(), r1.getY());
		negativeClip.lineTo(r2.getX(), r2.getY());
		negativeClip.lineTo(l2.getX(), l2.getY());
		negativeClip.lineTo(l1.getX(), l1.getY());
		negativeClip.closePath();
		
		return negativeClip;
	}
	
	private Iterable<LaneGui> lanes(boolean forward) {
		final int laneMiddle = laneMiddle();
		
		return forward ? lanes.subList(laneMiddle, lanes.size()) : CollectionUtils.reverse(lanes.subList(0, laneMiddle));
	}
	
	private Path middlePath(boolean forward) {
		final Path path = forward ? Path.create(b.x, b.y) : Path.create(a.x, a.y);
		final Segment first = forward ? segments.get(segments.size() - 1) : segments.get(0);
		
		return first.append(path, forward, 0);
	}
	
	private void moveIncoming(Road.End end) {
		final Point2D lc = getLeftCorner(end);
		final Point2D rc = getRightCorner(end);
		final Line2D cornerLine = new Line2D.Double(lc, rc);
		
		final double a = getAngle(end);
		final Line2D roadLine = line(getContainer().getGui(end.getJunction()).getPoint(), a);
		
		final Point2D i = intersection(roadLine, cornerLine);
		// TODO fix depending on angle(i, lc)
		final double offset = innerMargin + (getWidth(end, true) - innerMargin - outerMargin) / 2;
		final Point2D loc = relativePoint(i, offset, angle(i, lc));
		
		getConnector(end).move(loc.getX(), loc.getY());
	}
	
	private void moveOutgoing(LaneGui lane, int offset) {
		final Road.End end = lane.getModel().getOutgoingRoadEnd();
		
		final Point2D lc = getLeftCorner(end);
		final Point2D rc = getRightCorner(end);
		final Line2D cornerLine = new Line2D.Double(lc, rc);
		
		final double a = getAngle(end);
		final Line2D roadLine = line(getContainer().getGui(end.getJunction()).getPoint(), a);
		
		final Point2D i = intersection(roadLine, cornerLine);
		// TODO fix depending on angle(i, rc)
		final double d = innerMargin + (2 * offset + 1) * getContainer().getLaneWidth() / 2;
		final Point2D loc = relativePoint(i, d, angle(i, rc));
		
		lane.outgoing.move(loc.getX(), loc.getY());
	}
	
	public double getAngle(Road.End end) {
		if (!getModel().equals(end.getRoad())) {
			throw new IllegalArgumentException();
		}
		
		if (end.isToEnd()) {
			return segments.get(segments.size() - 1).angle;
		} else {
			final double angle = segments.get(0).angle;
			return angle > PI ? angle - PI : angle + PI;
		}
	}
	
	public double getWidth(Road.End end) {
		return getWidth(end, true) + getWidth(end, false);
	}
	
	public double getLength() {
		return length;
	}
	
	public List<LaneGui> getLanes() {
		return Collections.unmodifiableList(lanes);
	}
	
	public double getOffset(double x, double y) {
		return segments.get(0).getOffset(x, y);
	}
	
	public GuiContainer getContainer() {
		return container;
	}
}

package org.openstreetmap.josm.plugins.turnlanes.gui;

import static java.lang.Math.max;
import static org.openstreetmap.josm.plugins.turnlanes.gui.GuiUtil.area;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

import org.openstreetmap.josm.plugins.turnlanes.gui.RoadGui.IncomingConnector;
import org.openstreetmap.josm.plugins.turnlanes.model.Junction;
import org.openstreetmap.josm.plugins.turnlanes.model.Lane;

final class LaneGui {
	final class LengthSlider extends InteractiveElement {
		private final Point2D center = new Point2D.Double();
		private final Ellipse2D circle = new Ellipse2D.Double();
		
		private Point2D dragDelta;
		
		private LengthSlider() {}
		
		@Override
		public void paint(Graphics2D g2d, State state) {
			if (isVisible(state)) {
				g2d.setColor(Color.BLUE);
				g2d.fill(circle);
				
				final String len = METER_FORMAT.format(getModel().getLength());
				final Rectangle2D bounds = circle.getBounds2D();
				g2d.setFont(g2d.getFont().deriveFont(Font.BOLD, (float) bounds.getHeight()));
				g2d.drawString(len, (float) bounds.getMaxX(), (float) bounds.getMaxY());
			}
		}
		
		private boolean isVisible(State state) {
			if (state instanceof State.OutgoingActive) {
				return LaneGui.this.equals(((State.OutgoingActive) state).getLane());
			}
			
			return false;
		}
		
		@Override
		public boolean contains(Point2D p, State state) {
			return isVisible(state) && circle.contains(p);
		}
		
		@Override
		public Type getType() {
			return Type.INCOMING_CONNECTOR;
		}
		
		@Override
		boolean beginDrag(double x, double y) {
			dragDelta = new Point2D.Double(center.getX() - x, center.getY() - y);
			return true;
		}
		
		@Override
		State drag(double x, double y, InteractiveElement target, State old) {
			move(x + dragDelta.getX(), y + dragDelta.getY());
			return new State.Dirty(old);
		}
		
		void move(double x, double y) {
			final double r = getRoad().connectorRadius;
			
			final double offset = getRoad().getOffset(x, y);
			final double newLength = getModel().isReverse() ? offset : getRoad().getLength() - offset;
			if (newLength > 0) {
				getModel().setLength(newLength * getRoad().getContainer().getMpp());
			}
			
			center.setLocation(x, y);
			circle.setFrame(x - r, y - r, 2 * r, 2 * r);
		}
		
		public void move(Point2D loc) {
			final double x = loc.getX();
			final double y = loc.getY();
			final double r = getRoad().connectorRadius;
			
			center.setLocation(x, y);
			circle.setFrame(x - r, y - r, 2 * r, 2 * r);
		}
		
		@Override
		int getZIndex() {
			return 2;
		}
	}
	
	final class OutgoingConnector extends InteractiveElement {
		private final Point2D center = new Point2D.Double();
		private final Ellipse2D circle = new Ellipse2D.Double();
		
		private Point2D dragLocation;
		private IncomingConnector dropTarget;
		
		private OutgoingConnector() {}
		
		@Override
		public void paintBackground(Graphics2D g2d, State state) {
			if (isActive(state)) {
				final Composite old = g2d.getComposite();
				g2d.setComposite(((AlphaComposite) old).derive(0.2f));
				
				g2d.setColor(new Color(255, 127, 31));
				LaneGui.this.fill(g2d);
				
				g2d.setComposite(old);
			}
			
			if (dragLocation != null) {
				g2d.setStroke(getContainer().getConnectionStroke());
				g2d.setColor(dropTarget == null ? GuiContainer.RED : GuiContainer.GREEN);
				g2d.draw(new Line2D.Double(getCenter(), dropTarget == null ? dragLocation : dropTarget.getCenter()));
			}
		}
		
		@Override
		public void paint(Graphics2D g2d, State state) {
			if (isVisible(state)) {
				final Composite old = g2d.getComposite();
				if (isActive(state)) {
					g2d.setComposite(((AlphaComposite) old).derive(1f));
				}
				
				g2d.setColor(Color.WHITE);
				g2d.fill(circle);
				g2d.setComposite(old);
			}
		}
		
		private boolean isActive(State state) {
			return state instanceof State.OutgoingActive && LaneGui.this.equals(((State.OutgoingActive) state).getLane());
		}
		
		private boolean isVisible(State state) {
			return getModel().getOutgoingJunction().equals(state.getJunction().getModel());
		}
		
		@Override
		public boolean contains(Point2D p, State state) {
			return isVisible(state) && (circle.contains(p) || LaneGui.this.contains(p));
		}
		
		@Override
		public Type getType() {
			return Type.OUTGOING_CONNECTOR;
		}
		
		@Override
		public State activate(State old) {
			return new State.OutgoingActive(old.getJunction(), LaneGui.this);
		}
		
		@Override
		boolean beginDrag(double x, double y) {
			return circle.contains(x, y);
		}
		
		@Override
		State drag(double x, double y, InteractiveElement target, State old) {
			dragLocation = new Point2D.Double(x, y);
			dropTarget = target != null && target.getType() == Type.INCOMING_CONNECTOR ? (IncomingConnector) target : null;
			return old;
		}
		
		@Override
		State drop(double x, double y, InteractiveElement target, State old) {
			drag(x, y, target, old);
			dragLocation = null;
			
			if (dropTarget == null) {
				return old;
			}
			
			final Junction j = (getModel().isReverse() ? getRoad().getA() : getRoad().getB()).getModel();
			
			j.addTurn(getModel(), dropTarget.getRoadEnd());
			
			dropTarget = null;
			return new State.Dirty(old);
		}
		
		public Point2D getCenter() {
			return (Point2D) center.clone();
		}
		
		void move(double x, double y) {
			final double r = getRoad().connectorRadius;
			
			center.setLocation(x, y);
			circle.setFrame(x - r, y - r, 2 * r, 2 * r);
		}
		
		@Override
		int getZIndex() {
			return 1;
		}
	}
	
	static final NumberFormat METER_FORMAT = new DecimalFormat("0.0m");
	
	private final RoadGui road;
	private final Lane lane;
	
	final Path2D area = new Path2D.Double();
	
	final OutgoingConnector outgoing = new OutgoingConnector();
	final LengthSlider lengthSlider;
	
	private Shape clip;
	
	public LaneGui(RoadGui road, Lane lane) {
		this.road = road;
		this.lane = lane;
		this.lengthSlider = lane.isExtra() ? new LengthSlider() : null;
	}
	
	public double getLength() {
		return getModel().isExtra() ? lane.getLength() / getRoad().getContainer().getMpp() : getRoad().getLength();
	}
	
	public Lane getModel() {
		return lane;
	}
	
	public RoadGui getRoad() {
		return road;
	}
	
	public GuiContainer getContainer() {
		return getRoad().getContainer();
	}
	
	public Path recalculate(Path inner, Path2D innerLine) {
		area.reset();
		
		final double W = getContainer().getLaneWidth();
		final double L = getLength();
		
		final double WW = 3 / getContainer().getMpp();
		
		final List<LaneGui> lanes = getRoad().getLanes();
		final int i = lanes.indexOf(this);
		final LaneGui left = getModel().isReverse() ? (i < lanes.size() - 1 ? lanes.get(i + 1) : null) : (i > 0 ? lanes
		    .get(i - 1) : null);
		final Lane leftModel = left == null ? null : left.getModel();
		final double leftLength = leftModel == null || leftModel.isReverse() != getModel().isReverse() ? Double.NEGATIVE_INFINITY
		    : leftModel.getKind() == Lane.Kind.EXTRA_LEFT ? left.getLength() : L;
		
		final Path outer;
		if (getModel().getKind() == Lane.Kind.EXTRA_LEFT) {
			final double AL = 30 / getContainer().getMpp();
			final double SL = max(L, leftLength + AL);
			
			outer = inner.offset(W, SL, SL + AL, 0);
			area(area, inner.subpath(0, L), outer.subpath(0, L + WW));
			
			lengthSlider.move(inner.getPoint(L));
			
			if (L > leftLength) {
				innerLine.append(inner.subpath(max(0, leftLength + WW), L).getIterator(), leftLength >= 0
				    || getModel().isReverse());
				final Point2D op = outer.getPoint(L + WW);
				innerLine.lineTo(op.getX(), op.getY());
			}
		} else if (getModel().getKind() == Lane.Kind.EXTRA_RIGHT) {
			outer = inner.offset(W, L, L + WW, 0);
			area(area, inner.subpath(0, L + WW), outer.subpath(0, L));
			
			lengthSlider.move(outer.getPoint(L));
		} else {
			outer = inner.offset(W, -1, -1, W);
			area(area, inner, outer);
			
			if (leftLength < L) {
				innerLine.append(inner.subpath(max(0, leftLength + WW), L).getIterator(), leftLength >= 0
				    || getModel().isReverse());
			}
		}
		
		return outer;
	}
	
	public void fill(Graphics2D g2d) {
		final Shape old = g2d.getClip();
		g2d.clip(clip);
		g2d.fill(area);
		g2d.setClip(old);
	}
	
	public void setClip(Shape clip) {
		this.clip = clip;
	}
	
	public boolean contains(Point2D p) {
		return area.contains(p) && clip.contains(p);
	}
}
package org.openstreetmap.josm.plugins.turnlanes.gui;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static org.openstreetmap.josm.plugins.turnlanes.gui.GuiUtil.area;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.josm.plugins.turnlanes.gui.RoadGui.IncomingConnector;
import org.openstreetmap.josm.plugins.turnlanes.model.Lane;
import org.openstreetmap.josm.plugins.turnlanes.model.Road;

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
                
                final String len = METER_FORMAT.format(getLength() * getRoad().getContainer().getMpp());
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
            move(x + dragDelta.getX(), y + dragDelta.getY(), false);
            return new State.Dirty(old);
        }
        
        @Override
        State drop(double x, double y, InteractiveElement target, State old) {
            move(x + dragDelta.getX(), y + dragDelta.getY(), true);
            return old;
        }
        
        void move(double x, double y, boolean updateModel) {
            final double r = getRoad().connectorRadius;
            
            final double offset = getRoad().getOffset(x, y);
            final double newLength = getModel().getOutgoingRoadEnd().isFromEnd() ? offset : getRoad().getLength()
                    - offset;
            final double adjustedLength = min(max(newLength, 0.1), getRoad().getLength());
            
            length = adjustedLength;
            if (updateModel) {
                getModel().setLength(adjustedLength * getRoad().getContainer().getMpp());
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
                final State.Connecting s = (State.Connecting) state;
                final Path2D path = new Path2D.Double();
                path.moveTo(center.getX(), center.getY());
                
                final List<RoadGui.ViaConnector> vias = s.getViaConnectors();
                for (int i = 0; i < vias.size() - 1; i += 2) {
                    final RoadGui.ViaConnector v = vias.get(i);
                    final PathIterator it = v.getRoad().getLaneMiddle(v.getRoadEnd().isFromEnd()).getIterator();
                    path.append(it, true);
                }
                if ((vias.size() & 1) != 0) {
                    final RoadGui.ViaConnector last = vias.get(vias.size() - 1);
                    path.lineTo(last.getCenter().getX(), last.getCenter().getY());
                }
                
                if (dropTarget == null) {
                    g2d.setColor(GuiContainer.RED);
                    path.lineTo(dragLocation.getX(), dragLocation.getY());
                } else {
                    g2d.setColor(GuiContainer.GREEN);
                    path.lineTo(dropTarget.getCenter().getX(), dropTarget.getCenter().getY());
                }
                
                g2d.setStroke(getContainer().getConnectionStroke());
                g2d.draw(path);
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
            return state instanceof State.OutgoingActive
                    && LaneGui.this.equals(((State.OutgoingActive) state).getLane());
        }
        
        private boolean isVisible(State state) {
            if (state instanceof State.Connecting) {
                return ((State.Connecting) state).getLane().equals(getModel());
            }
            
            return !getRoad().getModel().isPrimary() && getModel().getOutgoingJunction().isPrimary();
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
            return new State.OutgoingActive(LaneGui.this);
        }
        
        @Override
        boolean beginDrag(double x, double y) {
            return circle.contains(x, y);
        }
        
        @Override
        State.Connecting drag(double x, double y, InteractiveElement target, State old) {
            dragLocation = new Point2D.Double(x, y);
            dropTarget = null;
            
            if (!(old instanceof State.Connecting)) {
                return new State.Connecting(getModel());
            }
            
            final State.Connecting s = (State.Connecting) old;
            if (target != null && target.getType() == Type.INCOMING_CONNECTOR) {
                dropTarget = (IncomingConnector) target;
                
                return (s.getViaConnectors().size() & 1) == 0 ? s : s.pop();
            } else if (target != null && target.getType() == Type.VIA_CONNECTOR) {
                return s.next((RoadGui.ViaConnector) target);
            }
            
            return s;
        }
        
        @Override
        State drop(double x, double y, InteractiveElement target, State old) {
            final State.Connecting s = drag(x, y, target, old);
            dragLocation = null;
            if (dropTarget == null) {
                return activate(old);
            }
            
            final List<Road> via = new ArrayList<Road>();
            assert (s.getViaConnectors().size() & 1) == 0;
            for (int i = 0; i < s.getViaConnectors().size(); i += 2) {
                final RoadGui.ViaConnector a = s.getViaConnectors().get(i);
                final RoadGui.ViaConnector b = s.getViaConnectors().get(i + 1);
                assert a.getRoadEnd().getOppositeEnd().equals(b.getRoadEnd());
                via.add(a.getRoadEnd().getRoad());
            }
            
            getModel().addTurn(via, dropTarget.getRoadEnd());
            dropTarget = null;
            return new State.Dirty(activate(old));
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
    private double length;
    
    public LaneGui(RoadGui road, Lane lane) {
        this.road = road;
        this.lane = lane;
        this.lengthSlider = lane.isExtra() ? new LengthSlider() : null;
        this.length = lane.isExtra() ? lane.getLength() / road.getContainer().getMpp() : Double.NaN;
    }
    
    public double getLength() {
        return lane.isExtra() ? length : road.getLength();
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
        
        final LaneGui left = left();
        final Lane leftModel = left == null ? null : left.getModel();
        final double leftLength = leftModel == null
                || !leftModel.getOutgoingRoadEnd().equals(getModel().getOutgoingRoadEnd()) ? Double.NEGATIVE_INFINITY
                : leftModel.getKind() == Lane.Kind.EXTRA_LEFT ? left.getLength() : L;
        
        final Path outer;
        if (getModel().getKind() == Lane.Kind.EXTRA_LEFT) {
            final double AL = 30 / getContainer().getMpp();
            final double SL = max(L, leftLength + AL);
            
            outer = inner.offset(W, SL, SL + AL, 0);
            area(area, inner.subpath(0, L, true), outer.subpath(0, L + WW, true));
            
            lengthSlider.move(inner.getPoint(L, true));
            
            if (L > leftLength) {
                innerLine.append(inner.subpath(leftLength + WW, L, true).getIterator(), leftLength >= 0
                        || getModel().getOutgoingRoadEnd().isFromEnd());
                final Point2D op = outer.getPoint(L + WW, true);
                innerLine.lineTo(op.getX(), op.getY());
            }
        } else if (getModel().getKind() == Lane.Kind.EXTRA_RIGHT) {
            outer = inner.offset(W, L, L + WW, 0);
            area(area, inner.subpath(0, L + WW, true), outer.subpath(0, L, true));
            
            lengthSlider.move(outer.getPoint(L, true));
        } else {
            outer = inner.offset(W, -1, -1, W);
            area(area, inner, outer);
            
            if (leftLength < L) {
                innerLine.append(inner.subpath(leftLength + WW, L, true).getIterator(), leftLength >= 0
                        || getModel().getOutgoingRoadEnd().isFromEnd());
            }
        }
        
        return outer;
    }
    
    private LaneGui left() {
        final List<LaneGui> lanes = getRoad().getLanes(getModel().getOutgoingRoadEnd());
        final int i = lanes.indexOf(this);
        return i > 0 ? lanes.get(i - 1) : null;
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

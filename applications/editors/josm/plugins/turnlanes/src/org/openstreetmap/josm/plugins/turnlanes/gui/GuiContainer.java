package org.openstreetmap.josm.plugins.turnlanes.gui;

import static java.lang.Math.sqrt;
import static org.openstreetmap.josm.plugins.turnlanes.gui.GuiUtil.locs;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.plugins.turnlanes.model.Junction;
import org.openstreetmap.josm.plugins.turnlanes.model.Lane;
import org.openstreetmap.josm.plugins.turnlanes.model.ModelContainer;
import org.openstreetmap.josm.plugins.turnlanes.model.Road;

class GuiContainer {
    static final Color RED = new Color(234, 66, 108);
    static final Color GREEN = new Color(66, 234, 108);
    
    private static final GuiContainer EMPTY = new GuiContainer(ModelContainer.empty());
    
    public static GuiContainer empty() {
        return EMPTY;
    }
    
    private final ModelContainer mc;
    
    private final Point2D translation;
    /**
     * Meters per pixel.
     */
    private final double mpp;
    private final double scale;
    private final double laneWidth;
    
    private final Map<Junction, JunctionGui> junctions = new HashMap<Junction, JunctionGui>();
    private final Map<Road, RoadGui> roads = new HashMap<Road, RoadGui>();
    
    private final Stroke connectionStroke;
    
    public GuiContainer(ModelContainer mc) {
        final Point2D origin = avgOrigin(locs(mc.getPrimaryJunctions()));
        
        final LatLon originCoor = Main.getProjection().eastNorth2latlon(new EastNorth(origin.getX(), origin.getY()));
        final LatLon relCoor = Main.getProjection().eastNorth2latlon(
                new EastNorth(origin.getX() + 1, origin.getY() + 1));
        
        // meters per source unit
        final double mpsu = relCoor.greatCircleDistance(originCoor) / sqrt(2);
        
        this.mc = mc;
        this.translation = new Point2D.Double(-origin.getX(), -origin.getY());
        this.mpp = 0.2;
        this.scale = mpsu / mpp;
        this.laneWidth = 2 / mpp;
        
        this.connectionStroke = new BasicStroke((float) (laneWidth / 4), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        
        for (Junction j : mc.getPrimaryJunctions()) {
            getGui(j);
        }
    }
    
    private static Point2D avgOrigin(List<Point2D> locs) {
        if (locs.isEmpty()) {
            return new Point2D.Double(0, 0);
        }
        
        double x = 0;
        double y = 0;
        
        for (Point2D l : locs) {
            x += l.getX();
            y += l.getY();
        }
        
        return new Point2D.Double(x / locs.size(), y / locs.size());
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
        
        throw new IllegalArgumentException(tr("No such lane."));
    }
    
    public ModelContainer getModel() {
        return mc;
    }
    
    public Rectangle2D getBounds() {
        if (isEmpty()) {
            return new Rectangle2D.Double(-1, -1, 2, 2);
        }
        
        final List<Junction> primaries = new ArrayList<Junction>(mc.getPrimaryJunctions());
        final List<Double> top = new ArrayList<Double>();
        final List<Double> left = new ArrayList<Double>();
        final List<Double> right = new ArrayList<Double>();
        final List<Double> bottom = new ArrayList<Double>();
        
        for (Junction j : primaries) {
            final JunctionGui g = getGui(j);
            final Rectangle2D b = g.getBounds();
            
            top.add(b.getMinY());
            left.add(b.getMinX());
            right.add(b.getMaxX());
            bottom.add(b.getMaxY());
        }
        
        final double t = Collections.min(top);
        final double l = Collections.min(left);
        final double r = Collections.max(right);
        final double b = Collections.max(bottom);
        
        return new Rectangle2D.Double(l, t, r - l, b - t);
    }
    
    public GuiContainer recalculate() {
        return new GuiContainer(mc.recalculate());
    }
    
    public Iterable<RoadGui> getRoads() {
        return roads.values();
    }
    
    public Iterable<JunctionGui> getJunctions() {
        return junctions.values();
    }
    
    public boolean isEmpty() {
        return mc.isEmpty();
    }
}

/**
 * 
 */
package at.dallermassl.josm.plugin.navigator;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.util.List;

import javax.swing.Icon;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.tools.ColorHelper;
import org.openstreetmap.josm.tools.ImageProvider;

import org.openstreetmap.josm.data.osm.Segment;

/**
 * @author cdaller
 * 
 */
public class NavigatorLayer extends Layer {
    private static final String KEY_ROUTE_COLOR = "color.navigator.route";
    private static final String KEY_ROUTE_WIDTH = "navigator.route.width";
    private static final String KEY_ROUTE_SELECT = "navigator.route.select";
    protected static final double ARROW_PHI = Math.toRadians(20);
    private NavigatorModel navigatorNodeModel;
    private Icon startIcon;
    private Icon middleIcon;
    private Icon endIcon;

    /**
     * Constructor
     * 
     * @param name
     *            the name of the layer.
     */
    public NavigatorLayer(String name) {
        super(name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openstreetmap.josm.gui.layer.Layer#getIcon()
     */
    @Override
    public Icon getIcon() {
        Icon icon = ImageProvider.get("layer", "navigation_small");
        return icon;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openstreetmap.josm.gui.layer.Layer#getInfoComponent()
     */
    @Override
    public Object getInfoComponent() {
        return "Navigation layer"; // FIXXME return info in html about navigation
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openstreetmap.josm.gui.layer.Layer#getMenuEntries()
     */
    @Override
    public Component[] getMenuEntries() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openstreetmap.josm.gui.layer.Layer#getToolTipText()
     */
    @Override
    public String getToolTipText() {
        return "Tool Tip for Navigation"; // FIXXME
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openstreetmap.josm.gui.layer.Layer#isMergable(org.openstreetmap.josm.gui.layer.Layer)
     */
    @Override
    public boolean isMergable(Layer other) {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openstreetmap.josm.gui.layer.Layer#mergeFrom(org.openstreetmap.josm.gui.layer.Layer)
     */
    @Override
    public void mergeFrom(Layer from) {
        // do nothing
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openstreetmap.josm.gui.layer.Layer#paint(java.awt.Graphics,
     *      org.openstreetmap.josm.gui.MapView)
     */
    @Override
    public void paint(Graphics g, MapView mv) {
        List<Node> nodes = navigatorNodeModel.getSelectedNodes();
        if(nodes == null || nodes.size() == 0) {
            return;
        }
        
        if(startIcon == null) {
            startIcon = ImageProvider.get("navigation", "startflag");
            middleIcon = ImageProvider.get("navigation", "middleflag");
            endIcon = ImageProvider.get("navigation", "endflag");
        }
        // start icon:
        Node node = nodes.get(0);
        Point screen = mv.getPoint(node.eastNorth);
        startIcon.paintIcon(mv, g, screen.x, screen.y - startIcon.getIconHeight());
        
        // middle icons:
        for(int index = 1; index < nodes.size() - 1; ++index) {
            node = nodes.get(index);
            screen = mv.getPoint(node.eastNorth);
            middleIcon.paintIcon(mv, g, screen.x, screen.y - middleIcon.getIconHeight());
        }
        // end icon:
        if(nodes.size() > 1) {
            node = nodes.get(nodes.size() - 1);
            screen = mv.getPoint(node.eastNorth);
            endIcon.paintIcon(mv, g, screen.x, screen.y - endIcon.getIconHeight());
        }
        
        String colorString = Main.pref.get(KEY_ROUTE_COLOR);
        if(colorString.length() == 0) {
            colorString = ColorHelper.color2html(Color.GREEN);
            // FIXXME add after good color is found: Main.pref.put(KEY_ROUTE_COLOR, colorString);
        }
        Color color = ColorHelper.html2color(colorString);

        String widthString = Main.pref.get(KEY_ROUTE_WIDTH);
        if(widthString.length() == 0) {
            widthString = "5";
            // FIXXME add after good width is found: Main.pref.put(KEY_ROUTE_WIDTH, widthString);
        }
        int width = Integer.parseInt(widthString);

        List<SegmentEdge>edgePath = navigatorNodeModel.getEdgePath();
        if(edgePath != null) {
            for(SegmentEdge edge : edgePath) {
                drawSegmentEdge(g, mv, edge, color, width, true);
            }
        }
    }
    
    /**
     * Draw a line with the given color.
     */
    protected void drawSegmentEdge(Graphics g, MapView mv, SegmentEdge edge, Color col, int width, boolean showDirection) {
        g.setColor(col);
        Point from;
        Point to;
        if(!edge.isInverted()) {
            from = mv.getPoint(edge.getSegment().from.eastNorth);
            to = mv.getPoint(edge.getSegment().to.eastNorth);
        } else {
            from = mv.getPoint(edge.getSegment().to.eastNorth);
            to = mv.getPoint(edge.getSegment().from.eastNorth);            
        }
        
        Rectangle screen = g.getClipBounds();
        Line2D line = new Line2D.Double(from.x, from.y, to.x, to.y);
        if (screen.contains(from.x, from.y, to.x, to.y) || screen.intersectsLine(line))
        {
            Graphics2D g2d = (Graphics2D)g;
            Stroke oldStroke = g2d.getStroke();
            g2d.setStroke(new BasicStroke(width)); // thickness
            g.drawLine(from.x, from.y, to.x, to.y);
            if (showDirection) {
                double t = Math.atan2(to.y-from.y, to.x-from.x) + Math.PI;
                g.drawLine(to.x,to.y, (int)(to.x + 10*Math.cos(t-ARROW_PHI)), (int)(to.y + 10*Math.sin(t-ARROW_PHI)));
                g.drawLine(to.x,to.y, (int)(to.x + 10*Math.cos(t+ARROW_PHI)), (int)(to.y + 10*Math.sin(t+ARROW_PHI)));
            }
            g2d.setStroke(oldStroke);
        }
    }


    /*
     * (non-Javadoc)
     * 
     * @see org.openstreetmap.josm.gui.layer.Layer#visitBoundingBox(org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor)
     */
    @Override
    public void visitBoundingBox(BoundingXYVisitor v) {
        for (Node node : navigatorNodeModel.getSelectedNodes()) {
            v.visit(node);
        }

    }

    /**
     * @return the navigatorNodeModel
     */
    public NavigatorModel getNavigatorNodeModel() {
        return this.navigatorNodeModel;
    }

    /**
     * 
     */
    public void navigate() {
        navigatorNodeModel.calculateShortesPath();
        Main.pref.hasKey(KEY_ROUTE_COLOR);
        String selectString = Main.pref.get(KEY_ROUTE_SELECT);
        if(selectString.length() == 0) {
            selectString = "true";
            Main.pref.put(KEY_ROUTE_SELECT, selectString);
        }
        
        if(Boolean.parseBoolean(selectString)) {
            List<Segment> path = navigatorNodeModel.getSegmentPath();
            if(path != null) {
                synchronized(path) {
                    Main.ds.setSelected(path);
                }
            }
        }
        Main.map.repaint();        
    }

    /**
     * @param navigatorNodeModel the navigatorNodeModel to set
     */
    public void setNavigatorNodeModel(NavigatorModel navigatorNodeModel) {
        this.navigatorNodeModel = navigatorNodeModel;
    }
}

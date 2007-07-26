/**
 * 
 */
package at.dallermassl.josm.plugin.navigator;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Point;
import java.util.List;

import javax.swing.Icon;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.tools.ImageProvider;

import org.openstreetmap.josm.data.osm.Segment;

/**
 * @author cdaller
 * 
 */
public class NavigatorLayer extends Layer {
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
        
        List<Segment> path = navigatorNodeModel.getSegmentPath();
        if(path != null) {
            synchronized(path) {
                // TODO paint path
            }
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
        Main.map.repaint();        
    }

    /**
     * @param navigatorNodeModel the navigatorNodeModel to set
     */
    public void setNavigatorNodeModel(NavigatorModel navigatorNodeModel) {
        this.navigatorNodeModel = navigatorNodeModel;
    }
}

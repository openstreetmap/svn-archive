package org.openstreetmap.josm.plugins.validator;

import java.awt.Component;
import java.awt.Graphics;
import java.util.Enumeration;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.RenameLayerAction;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.MapView.LayerChangeListener;
import org.openstreetmap.josm.gui.dialogs.LayerListDialog;
import org.openstreetmap.josm.gui.dialogs.LayerListPopup;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.tools.ImageProvider;

/**
 * A layer showing error messages.
 * 
 * @author frsantos
 */
public class ErrorLayer extends Layer implements LayerChangeListener
{
	/**
	 * Constructor 
	 * @param name
	 */
	public ErrorLayer(String name) 
    {
		super(name);
        Main.map.mapView.addLayerChangeListener(this); 
	}

	/**
	 * Return a static icon.
	 */
	@Override public Icon getIcon() {
		return ImageProvider.get("preferences", "validator");
	}

    /**
     * Draw all primitives in this layer but do not draw modified ones (they
     * are drawn by the edit layer).
     * Draw nodes last to overlap the segments they belong to.
     */
    @SuppressWarnings("unchecked")
    @Override 
    public void paint(final Graphics g, final MapView mv) 
    {
        DefaultMutableTreeNode root = OSMValidatorPlugin.getPlugin().validationDialog.tree.getRoot();
        if( root == null || root.getChildCount() == 0)
            return;
        
        DefaultMutableTreeNode severity = (DefaultMutableTreeNode)root.getLastChild();
        while( severity != null )
        {
            Enumeration<DefaultMutableTreeNode> errorMessages = severity.children();
            while( errorMessages.hasMoreElements() )
            {
                DefaultMutableTreeNode errorMessage = errorMessages.nextElement();
                Enumeration<DefaultMutableTreeNode> errors = errorMessage.children();
                while( errors.hasMoreElements() )
                {
                    TestError error = (TestError)errors.nextElement().getUserObject();
                    error.paint(g, mv);
                }
            }
            
            // Severities in inverse order
            severity= severity.getPreviousSibling();
        }
	}

	@Override 
    public String getToolTipText() 
    {
        return null;
	}

	@Override public void mergeFrom(Layer from) {}

	@Override public boolean isMergable(Layer other) {
		return false;
	}

	@Override public void visitBoundingBox(BoundingXYVisitor v) {}

	@Override public Object getInfoComponent() 
    {
        /*
		StringBuilder b = new StringBuilder();
		int points = 0;
		for (Collection<GpsPoint> c : data) {
			b.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"+trn("a track with {0} point","a track with {0} points", c.size(), c.size())+"<br>");
			points += c.size();
		}
		b.append("</html>");
		return "<html>"+trn("{0} consists of {1} track", "{0} consists of {1} tracks", data.size(), name, data.size())+" ("+trn("{0} point", "{0} points", points, points)+")<br>"+b.toString();
        */
        return "<html>Validation errors</html>"; // TODO
	}

	@Override public Component[] getMenuEntries() 
    {
        return new Component[]{
                new JMenuItem(new LayerListDialog.ShowHideLayerAction(this)),
                new JMenuItem(new LayerListDialog.DeleteLayerAction(this)),
                new JSeparator(),
                new JMenuItem(new RenameLayerAction(null, this)),
                new JSeparator(),
                new JMenuItem(new LayerListPopup.InfoAction(this))};
    }

	@Override public void destroy() { }

    public void activeLayerChange(Layer oldLayer, Layer newLayer) { }

    public void layerAdded(Layer newLayer) { }

    /**
     * If layer is the OSM Data layer, remove all errors
     */
    public void layerRemoved(Layer oldLayer)
    {
        if(oldLayer == Main.map.mapView.editLayer ) 
        {
            Main.map.mapView.removeLayer(this); 
        }
    }
}

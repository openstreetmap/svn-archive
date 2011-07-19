// License: GPL. See LICENSE file for details.
package org.openstreetmap.josm.plugins.licensechange;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Graphics2D;
import java.util.Enumeration;
import java.util.List;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.tree.DefaultMutableTreeNode;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.RenameLayerAction;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.MapView.LayerChangeListener;
import org.openstreetmap.josm.gui.dialogs.LayerListDialog;
import org.openstreetmap.josm.gui.dialogs.LayerListPopup;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.plugins.licensechange.util.Bag;
import org.openstreetmap.josm.tools.ImageProvider;

/**
 * A layer showing problem messages.
 */
public class ProblemLayer extends Layer implements LayerChangeListener 
{

    private LicenseChangePlugin plugin;

    private int updateCount = -1;


    public ProblemLayer(LicenseChangePlugin plugin) 
    {
        super(tr("Relicensing Problems"));
        this.plugin = plugin;
        MapView.addLayerChangeListener(this);
    }

    /**
     * Return a static icon.
     */
    @Override
    public Icon getIcon() 
    {
        return ImageProvider.get("layer", "licensechange");
    }

    /**
     * Draw all primitives in this layer but do not draw modified ones (they
     * are drawn by the edit layer).
     * Draw nodes last to overlap the ways they belong to.
     */
    @SuppressWarnings("unchecked")
    @Override
    public void paint(final Graphics2D g, final MapView mv, Bounds bounds) 
    {
        updateCount = plugin.problemDialog.tree.getUpdateCount();
        DefaultMutableTreeNode root = plugin.problemDialog.tree.getRoot();
        if (root == null || root.getChildCount() == 0)
            return;

        DefaultMutableTreeNode severity = (DefaultMutableTreeNode) root.getLastChild();
        while (severity != null) 
        {
            Enumeration<DefaultMutableTreeNode> problemMessages = severity.breadthFirstEnumeration();
            while (problemMessages.hasMoreElements()) 
            {
                Object tn = problemMessages.nextElement().getUserObject();
                if (tn instanceof LicenseProblem)
                    ((LicenseProblem) tn).paint(g, mv);
            }

            // Severities in inverse order
            severity = severity.getPreviousSibling();
        }
    }

    @Override
    public String getToolTipText() 
    {
        Bag<Severity, LicenseProblem> problemTree = new Bag<Severity, LicenseProblem>();
        List<LicenseProblem> problems = plugin.problemDialog.tree.getErrors();
        for (LicenseProblem e : problems) {
            problemTree.add(e.getSeverity(), e);
        }

        StringBuilder b = new StringBuilder();
        for (Severity s : Severity.values()) 
        {
            if (problemTree.containsKey(s))
                b.append(tr(s.toString())).append(": ").append(problemTree.get(s).size()).append("<br>");
        }

        if (b.length() == 0)
            return "<html>" + tr("No relicensing problems") + "</html>";
        else
            return "<html>" + tr("Relicensing problems") + ":<br>" + b + "</html>";
    }

    @Override
    public void mergeFrom(Layer from) 
    {
    }

    @Override
    public boolean isMergable(Layer other) 
    {
        return false;
    }

    @Override
    public boolean isChanged() 
    {
        return updateCount != plugin.problemDialog.tree.getUpdateCount();
    }

    @Override
    public void visitBoundingBox(BoundingXYVisitor v) 
    {
    }

    @Override
    public Object getInfoComponent() 
    {
        return getToolTipText();
    }

    @Override
    public Action[] getMenuEntries() 
    {
        return new Action[] {
                LayerListDialog.getInstance().createShowHideLayerAction(),
                LayerListDialog.getInstance().createDeleteLayerAction(),
                SeparatorLayerAction.INSTANCE,
                new RenameLayerAction(null, this),
                SeparatorLayerAction.INSTANCE,
                new LayerListPopup.InfoAction(this) };
    }

    @Override
    public void destroy() 
    {
    }

    public void activeLayerChange(Layer oldLayer, Layer newLayer) 
    {
    }

    public void layerAdded(Layer newLayer) 
    {
    }

    /**
     * If layer is the OSM Data layer, remove all problems
     */
    public void layerRemoved(Layer oldLayer) 
    {
        if (oldLayer instanceof OsmDataLayer &&  Main.map.mapView.getEditLayer() == null) 
        {
            Main.map.mapView.removeLayer(this);
        } 
        else if (oldLayer == this) 
        {
            MapView.removeLayerChangeListener(this);
            LicenseChangePlugin.problemLayer = null;
        }
    }
}

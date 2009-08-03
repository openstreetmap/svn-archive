package org.openstreetmap.josm.plugins.validator;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Component;
import java.awt.Graphics;
import java.util.Enumeration;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.tree.DefaultMutableTreeNode;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.RenameLayerAction;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.dialogs.LayerListDialog;
import org.openstreetmap.josm.gui.dialogs.LayerListPopup;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.Layer.LayerChangeListener;
import org.openstreetmap.josm.plugins.validator.util.Bag;
import org.openstreetmap.josm.tools.ImageProvider;

/**
 * A layer showing error messages.
 * 
 * @author frsantos
 */
public class ErrorLayer extends Layer implements LayerChangeListener {
    private OSMValidatorPlugin plugin;

    public ErrorLayer(OSMValidatorPlugin plugin) {
        super(tr("Validation errors"));
        this.plugin = plugin;
        Layer.listeners.add(this);
    }

    /**
     * Return a static icon.
     */
    @Override
    public Icon getIcon() {
        return ImageProvider.get("layer", "validator");
    }

    /**
     * Draw all primitives in this layer but do not draw modified ones (they
     * are drawn by the edit layer).
     * Draw nodes last to overlap the ways they belong to.
     */
    @SuppressWarnings("unchecked")
    @Override
    public void paint(final Graphics g, final MapView mv) {
        DefaultMutableTreeNode root = plugin.validationDialog.tree.getRoot();
        if (root == null || root.getChildCount() == 0)
            return;

        DefaultMutableTreeNode severity = (DefaultMutableTreeNode) root.getLastChild();
        while (severity != null) {
            Enumeration<DefaultMutableTreeNode> errorMessages = severity.breadthFirstEnumeration();
            while (errorMessages.hasMoreElements()) {
                Object tn = errorMessages.nextElement().getUserObject();
                if (tn instanceof TestError)
                    ((TestError) tn).paint(g, mv);
            }

            // Severities in inverse order
            severity = severity.getPreviousSibling();
        }
    }

    @Override
    public String getToolTipText() {
        Bag<Severity, TestError> errorTree = new Bag<Severity, TestError>();
        List<TestError> errors = plugin.validationDialog.tree.getErrors();
        for (TestError e : errors) {
            errorTree.add(e.getSeverity(), e);
        }

        StringBuilder b = new StringBuilder();
        for (Severity s : Severity.values()) {
            if (errorTree.containsKey(s))
                b.append(tr(s.toString())).append(": ").append(errorTree.get(s).size()).append("<br>");
        }

        if (b.length() == 0)
            return "<html>" + tr("No validation errors") + "</html>";
        else
            return "<html>" + tr("Validation errors") + ":<br>" + b + "</html>";
    }

    @Override
    public void mergeFrom(Layer from) {
    }

    @Override
    public boolean isMergable(Layer other) {
        return false;
    }

    @Override
    public void visitBoundingBox(BoundingXYVisitor v) {
    }

    @Override
    public Object getInfoComponent() {
        return getToolTipText();
    }

    @Override
    public Component[] getMenuEntries() {
        return new Component[] { new JMenuItem(LayerListDialog.getInstance().createShowHideLayerAction(this)),
                new JMenuItem(LayerListDialog.getInstance().createDeleteLayerAction(this)), 
                new JSeparator(),
                new JMenuItem(new RenameLayerAction(null, this)), new JSeparator(),
                new JMenuItem(new LayerListPopup.InfoAction(this)) };
    }

    @Override
    public void destroy() {
    }

    public void activeLayerChange(Layer oldLayer, Layer newLayer) {
    }

    public void layerAdded(Layer newLayer) {
    }

    /**
     * If layer is the OSM Data layer, remove all errors
     */
    public void layerRemoved(Layer oldLayer) {
        if (oldLayer == Main.map.mapView.getEditLayer()) {
            Main.map.mapView.removeLayer(this);
        } else if (oldLayer == this) {
            OSMValidatorPlugin.errorLayer = null;
        }
    }
}

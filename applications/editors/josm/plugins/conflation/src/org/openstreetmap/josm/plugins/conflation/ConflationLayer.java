// License: GPL. See LICENSE file for details.
package org.openstreetmap.josm.plugins.conflation;

import java.awt.BasicStroke;
import java.awt.Color;
import javax.swing.Action;
import javax.swing.Icon;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.GeneralPath;
import org.openstreetmap.josm.actions.RenameLayerAction;


import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.conflict.Conflict;
import org.openstreetmap.josm.data.conflict.ConflictCollection;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.MapView.LayerChangeListener;
import org.openstreetmap.josm.gui.dialogs.LayerListDialog;
import org.openstreetmap.josm.gui.dialogs.LayerListPopup;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.Layer.SeparatorLayerAction;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.tools.ImageProvider;

/**
 * A layer to show arrows and other symbols to indicate what primitives have been matched.
 *
 * @author Josh Doe <josh@joshdoe.com>
 */
public class ConflationLayer extends OsmDataLayer implements LayerChangeListener {
    // TODO: this really shouldn't be OsmDataLayer, but easy to use conflict dialog

    ConflictCollection conflicts;

    public ConflationLayer(DataSet ds, ConflictCollection conflicts) {
        super(ds, tr("Conflation symbols"), null);
        MapView.addLayerChangeListener(this);
        this.conflicts = conflicts;
    }

    /**
     * Draw symbols connecting matched primitives.
     */
    @SuppressWarnings("unchecked")
    @Override
    public void paint(final Graphics2D g, final MapView mv, Bounds bounds) {
        Graphics2D g2 = g;
        BasicStroke line = new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        g2.setColor(Color.blue);
        g2.setStroke(line);

        GeneralPath path = new GeneralPath();

        final double PHI = Math.toRadians(20);
        final double cosPHI = Math.cos(PHI);
        final double sinPHI = Math.sin(PHI);

        for (Conflict c : conflicts) {
            OsmPrimitive p = c.getMy();
            OsmPrimitive q = c.getTheir();
            if (p != null && q != null) {
                // we have a pair, so draw line between them
                Point p1 = mv.getPoint(ConflationAction.getCenter(p));
                Point p2 = mv.getPoint(ConflationAction.getCenter(q));
                path.moveTo(p1.x, p1.y);
                path.lineTo(p2.x, p2.y);
                //logger.info(String.format("Line %d,%d to %d,%d", p1.x, p1.y, p2.x, p2.y));

                // draw arrow head
                if (true) {
                    final double segmentLength = p1.distance(p2);
                    if (segmentLength != 0.0) {
                        final double l = (10. + line.getLineWidth()) / segmentLength;

                        final double sx = l * (p1.x - p2.x);
                        final double sy = l * (p1.y - p2.y);

                        path.moveTo(p2.x + cosPHI * sx - sinPHI * sy, p2.y + sinPHI * sx + cosPHI * sy);
                        path.lineTo(p2.x, p2.y);
                        path.lineTo(p2.x + cosPHI * sx + sinPHI * sy, p2.y - sinPHI * sx + cosPHI * sy);
                    }
                }
            }
            // TODO: handle other than just Node->Node cases
            // TODO: draw color coded circle around unmatched nodes
        }

        g2.draw(path);
    }

    
    @Override
    public Icon getIcon() {
        // TODO: change icon
        return ImageProvider.get("layer", "gpx_small");
    }

    @Override
    public String getToolTipText() {
        return "Conflation tool tip text goes here";
    }

    @Override
    public void mergeFrom(Layer layer) {
        // we can't merge, so do nothing
    }

    @Override
    public boolean isMergable(Layer layer) {
        return false;
    }

    @Override
    public void visitBoundingBox(BoundingXYVisitor v) {
        //TODO: handle Way/Relation types
        for (Conflict c : conflicts) {
            OsmPrimitive my = c.getMy();
            OsmPrimitive their = c.getTheir();
            if (my != null && my instanceof Node)
                v.visit((Node)my);
            if (their != null && their instanceof Node)
                v.visit((Node)their);
        }
    }

    @Override
    public Object getInfoComponent() {
        return getToolTipText();
    }

    @Override
    public Action[] getMenuEntries() {
        return new Action[]{
                    LayerListDialog.getInstance().createShowHideLayerAction(),
                    LayerListDialog.getInstance().createDeleteLayerAction(),
                    SeparatorLayerAction.INSTANCE,
                    new RenameLayerAction(this.getAssociatedFile(), this),
                    SeparatorLayerAction.INSTANCE,
                    new LayerListPopup.InfoAction(this)};
    }

    public void activeLayerChange(Layer layer, Layer layer1) {
        //TODO: possibly change arrow styling depending on active layer?
    }

    public void layerAdded(Layer layer) {
        // shouldn't have to do anything here
    }

    public void layerRemoved(Layer layer) {
        //TODO: if ref or non-ref layer removed, remove arrows
    }

    /**
     * replies the set of conflicts currently managed in this layer
     *
     * @return the set of conflicts currently managed in this layer
     */
    public ConflictCollection getConflicts() {
        return conflicts;
    }
}

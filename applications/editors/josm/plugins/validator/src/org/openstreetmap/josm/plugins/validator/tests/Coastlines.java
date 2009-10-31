package org.openstreetmap.josm.plugins.validator.tests;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.util.*;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.command.ChangeCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.plugins.validator.Severity;
import org.openstreetmap.josm.plugins.validator.Test;
import org.openstreetmap.josm.plugins.validator.TestError;

/**
 * Check coastlines for errors
 *
 * @author frsantos
 */
public class Coastlines extends Test
{
    protected static int UNORDERED_COASTLINE = 901;
    protected static int REVERSED_COASTLINE = 902;
    protected static int UNCONNECTED_COASTLINE = 903;

    private boolean fixable = false;

    /**
     * Constructor
     */
    public Coastlines()
    {
        super(tr("Coastlines."),
              tr("This test checks that coastlines are correct."));
    }

    @Override
    public void visit(Way way)
    {
        if(!way.isUsable() || way.isClosed())
            return;

        String natural = way.get("natural");
        if(natural == null || !natural.equals("coastline"))
            return;

        Node firstNode = way.firstNode();
        Node lastNode = way.lastNode();
        Way previousWay = null;
        Way nextWay = null;

        for (OsmPrimitive parent: this.backreferenceDataSet.getParents(firstNode)) {
            natural = parent.get("natural");
            if (parent instanceof Way && !way.equals(parent) && (natural != null && "coastline".equals(natural))) {
                previousWay = (Way)parent;
                break;
            }
        }
        for (OsmPrimitive parent: this.backreferenceDataSet.getParents(lastNode)) {
            natural = parent.get("natural");
            if (parent instanceof Way && !way.equals(parent) && (natural != null && "coastline".equals(natural))) {
                nextWay = (Way)parent;
                break;
            }
        }

        List<OsmPrimitive> primitives = new ArrayList<OsmPrimitive>();
        List<OsmPrimitive> highlight = new ArrayList<OsmPrimitive>();
        primitives.add(way);

        OsmDataLayer layer = Main.map.mapView.getEditLayer();
        Area downloadedArea = null;
        if (layer != null)
            downloadedArea = layer.data.getDataSourceArea();

        if (previousWay == null || nextWay == null) {
            boolean firstNodeUnconnected = false;
            boolean lastNodeUnconnected = false;

            if (previousWay == null && (downloadedArea == null || downloadedArea.contains(firstNode.getCoor()))) {
                firstNodeUnconnected = true;
                highlight.add(firstNode);
            }
            if (nextWay == null && (downloadedArea == null || downloadedArea.contains(lastNode.getCoor()))) {
                lastNodeUnconnected = true;
                highlight.add(lastNode);
            }

            if (firstNodeUnconnected || lastNodeUnconnected)
                errors.add(new TestError(this, Severity.ERROR, tr("Unconnected coastline"),
                                         UNCONNECTED_COASTLINE, primitives, highlight));
        }

        boolean firstNodeUnordered = (previousWay != null && !firstNode.equals(previousWay.lastNode()));
        boolean lastNodeUnordered = (nextWay != null && !lastNode.equals(nextWay.firstNode()));

        if (firstNodeUnordered || lastNodeUnordered) {
            if (firstNodeUnordered && lastNodeUnordered && !previousWay.equals(nextWay)) {
                errors.add(new TestError(this, Severity.ERROR, tr("Reversed coastline"),
                                         REVERSED_COASTLINE, primitives));

            } else {
                if (firstNodeUnordered)
                    highlight.add(firstNode);
                if (lastNodeUnordered)
                    highlight.add(lastNode);

                errors.add(new TestError(this, Severity.ERROR, tr("Unordered coastline"),
                                         UNORDERED_COASTLINE, primitives, highlight));
            }
        }
    }

    @Override
    public Command fixError(TestError testError) {
        if (isFixable(testError)) {
            Way way = (Way) testError.getPrimitives().iterator().next();
            Way newWay = new Way(way);

            List<Node> nodesCopy = newWay.getNodes();
            Collections.reverse(nodesCopy);
            newWay.setNodes(nodesCopy);

            return new ChangeCommand(way, newWay);
        }

        return null;
    }

    @Override
    public boolean isFixable(TestError testError) {
        if (testError.getTester() instanceof Coastlines) {
            return (testError.getCode() == REVERSED_COASTLINE);
        }

        return false;
    }
}

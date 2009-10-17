package org.openstreetmap.josm.plugins.validator.tests;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.geom.Point2D;
import java.util.*;

import org.openstreetmap.josm.command.ChangeCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;
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
    public void visit(Way w)
    {
        if(!w.isUsable() || w.isClosed())
            return;

        String natural = w.get("natural");
        if(natural == null || !natural.equals("coastline"))
            return;

        Node f = w.firstNode();
        Node l = w.lastNode();
        Way prev = null;
        Way next = null;

        for (OsmPrimitive parent: this.backreferenceDataSet.getParents(f)) {
            natural = parent.get("natural");
            if (parent instanceof Way && !w.equals(parent) && (natural != null && "coastline".equals(natural))) {
                prev = (Way)parent;
                break;
            }
        }
        for (OsmPrimitive parent: this.backreferenceDataSet.getParents(l)) {
            natural = parent.get("natural");
            if (parent instanceof Way && !w.equals(parent) && (natural != null && "coastline".equals(natural))) {
                next = (Way)parent;
                break;
            }
        }

        List<OsmPrimitive> primitives = new ArrayList<OsmPrimitive>();
        List<OsmPrimitive> highlight = new ArrayList<OsmPrimitive>();
        primitives.add(w);

        if (prev == null || next == null) {
            if (prev == null)
                highlight.add(f);
            if (next == null)
                highlight.add(l);

            errors.add(new TestError(this, Severity.ERROR, tr("Unconnected coastline"),
                                     UNCONNECTED_COASTLINE, primitives, highlight));
        }

        boolean fuo = (prev != null && !f.equals(prev.lastNode()));
        boolean luo = (next != null && !l.equals(next.firstNode()));

        if (fuo || luo) {
            if (fuo && luo) {
                errors.add(new TestError(this, Severity.ERROR, tr("Reversed coastline"),
                                         REVERSED_COASTLINE, primitives));

            } else {
                if (fuo)
                    highlight.add(f);
                if (luo)
                    highlight.add(l);

                errors.add(new TestError(this, Severity.ERROR, tr("Unordered coastline"),
                                         UNORDERED_COASTLINE, primitives, highlight));
            }
        }
    }

    @Override
    public Command fixError(TestError testError) {
        if (isFixable(testError)) {
            Way w = (Way) testError.getPrimitives().iterator().next();
            Way wnew = new Way(w);

            List<Node> nodesCopy = wnew.getNodes();
            Collections.reverse(nodesCopy);
            wnew.setNodes(nodesCopy);

            return new ChangeCommand(w, wnew);
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

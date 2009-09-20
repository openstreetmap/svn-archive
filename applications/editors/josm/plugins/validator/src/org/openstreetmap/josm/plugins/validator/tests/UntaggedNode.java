package org.openstreetmap.josm.plugins.validator.tests;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.DeleteCommand;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.plugins.validator.Severity;
import org.openstreetmap.josm.plugins.validator.Test;
import org.openstreetmap.josm.plugins.validator.TestError;

/**
 * Checks for untagged nodes that are in no way
 *
 * @author frsantos
 */
public class UntaggedNode extends Test
{
    protected static int UNTAGGED_NODE = 201;

    /** Bag of all nodes */
    Set<Node> emptyNodes;

    /**
     * Constructor
     */
    public UntaggedNode()
    {
        super(tr("Untagged and unconnected nodes")+".",
              tr("This test checks for untagged nodes that are not part of any way."));
    }

    @Override
    public void startTest(ProgressMonitor monitor)
    {
    	super.startTest(monitor);
        emptyNodes = new HashSet<Node>(100);
    }

    @Override
    public void visit(Collection<OsmPrimitive> selection)
    {
        // If there is a partial selection, it may be false positives if a
        // node is selected, but not the container way. So, in this
        // case, we must visit all ways, selected or not.
        if (partialSelection) {
            for (OsmPrimitive p : selection) {
                if (p.isUsable() && p instanceof Node) {
                    p.visit(this);
                }
            }
            for (Way w : Main.main.getCurrentDataSet().ways) {
                visit(w);
            }
        } else {
            for (OsmPrimitive p : selection) {
                if (p.isUsable()) {
                    p.visit(this);
                }
            }
        }
    }

    @Override
    public void visit(Node n)
    {
        if(n.isUsable() && !n.isTagged())
            emptyNodes.add(n);
    }

    @Override
    public void visit(Way w)
    {
        for (Node n : w.getNodes()) {
            emptyNodes.remove(n);
        }
    }

    @Override
    public void endTest()
    {
        for(Node node : emptyNodes)
        {
            errors.add( new TestError(this, Severity.OTHER, tr("Untagged and unconnected nodes"), UNTAGGED_NODE, node) );
        }
        emptyNodes = null;
        super.endTest();
    }

    @Override
    public Command fixError(TestError testError)
    {
        return DeleteCommand.delete(Main.map.mapView.getEditLayer(), testError.getPrimitives());
    }

    @Override
    public boolean isFixable(TestError testError)
    {
        return (testError.getTester() instanceof UntaggedNode);
    }
}

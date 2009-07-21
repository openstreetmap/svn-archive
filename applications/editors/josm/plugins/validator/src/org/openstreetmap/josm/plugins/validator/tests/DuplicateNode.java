package org.openstreetmap.josm.plugins.validator.tests;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.openstreetmap.josm.actions.MergeNodesAction;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.plugins.validator.Severity;
import org.openstreetmap.josm.plugins.validator.Test;
import org.openstreetmap.josm.plugins.validator.TestError;
import org.openstreetmap.josm.plugins.validator.util.Bag;
/**
 * Tests if there are duplicate nodes
 *
 * @author frsantos
 */
public class DuplicateNode extends Test
{
    protected static int DUPLICATE_NODE = 1;

    /** Bag of all nodes */
    Bag<LatLon, OsmPrimitive> nodes;

    /**
     * Constructor
     */
    public DuplicateNode()
    {
        super(tr("Duplicated nodes")+".",
              tr("This test checks that there are no nodes at the very same location."));
    }


    @Override
    public void startTest()
    {
        nodes = new Bag<LatLon, OsmPrimitive>(1000);
    }

    @Override
    public void endTest()
    {
        for(List<OsmPrimitive> duplicated : nodes.values() )
        {
            if( duplicated.size() > 1)
            {
                TestError testError = new TestError(this, Severity.ERROR, tr("Duplicated nodes"), DUPLICATE_NODE, duplicated);
                errors.add( testError );
            }
        }
        nodes = null;
    }

    @Override
    public void visit(Node n)
    {
        if(!n.deleted && !n.incomplete)
            nodes.add(n.getCoor(), n);
    }

    /**
     * Merge the nodes into one.
     * Copied from UtilsPlugin.MergePointsAction
     */
    @Override
    public Command fixError(TestError testError)
    {
        Collection<? extends OsmPrimitive> sel = testError.getPrimitives();
        LinkedList<Node> nodes = new LinkedList<Node>();

        for (OsmPrimitive osm : sel)
            if (osm instanceof Node)
                nodes.add((Node)osm);

        if( nodes.size() < 2 )
            return null;

        Node target = null;
        // select the target node in the same way as in the core action MergeNodesAction, rev.1084
        for (Node n: nodes) {
            if (n.id > 0) {
                target = n;
                break;
            }
        }
        if (target == null)
            target = nodes.iterator().next();

        new  MergeNodesAction().mergeNodes(nodes, target);

        return null; // undoRedo handling done in mergeNodes
    }

    @Override
    public boolean isFixable(TestError testError)
    {
        return (testError.getTester() instanceof DuplicateNode);
    }
}

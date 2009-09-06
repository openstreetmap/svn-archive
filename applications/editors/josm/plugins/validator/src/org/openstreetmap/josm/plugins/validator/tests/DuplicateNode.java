package org.openstreetmap.josm.plugins.validator.tests;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.geom.Area;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.MergeNodesAction;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.gui.ConditionalOptionPaneUtil;
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
        if(n.isUsable())
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
            if (n.getId() > 0) {
                target = n;
                break;
            }
        }
        if (target == null)
            target = nodes.iterator().next();

        if(checkAndConfirmOutlyingDeletes(nodes))
            new MergeNodesAction().mergeNodes(nodes, target);

        return null; // undoRedo handling done in mergeNodes
    }

    @Override
    public boolean isFixable(TestError testError)
    {
        return (testError.getTester() instanceof DuplicateNode);
    }

    /**
     * Check whether user is about to delete data outside of the download area.
     * Request confirmation if he is.
     */
    private static boolean checkAndConfirmOutlyingDeletes(LinkedList<Node> del) {
        Area a = Main.main.getCurrentDataSet().getDataSourceArea();
        if (a != null) {
            for (OsmPrimitive osm : del) {
                if (osm instanceof Node && osm.getId() != 0) {
                    Node n = (Node) osm;
                    if (!a.contains(n.getCoor())) {
                        return ConditionalOptionPaneUtil.showConfirmationDialog(
                            "delete_outside_nodes",
                            Main.parent,
                            tr("You are about to delete nodes outside of the area you have downloaded." +
                                                "<br>" +
                                                "This can cause problems because other objects (that you don't see) might use them." +
                                                "<br>" +
                                        "Do you really want to delete?") + "</html>",
                            tr("Confirmation"),
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE,
                            JOptionPane.YES_OPTION);
                    }
                }
            }
        }
        return true;
    }
}

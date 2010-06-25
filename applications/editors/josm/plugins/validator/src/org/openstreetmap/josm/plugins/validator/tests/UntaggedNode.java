// License: GPL. See LICENSE file for details.
package org.openstreetmap.josm.plugins.validator.tests;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.DeleteCommand;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
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
    protected static int COMMENT_NODE = 202;

    /** Bag of all nodes */
    List<Node> emptyNodes;

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
        emptyNodes = new ArrayList<Node>();
    }

    @Override
    public void visit(Collection<OsmPrimitive> selection)
    {
        for (OsmPrimitive p : selection) {
            if (p.isUsable() && p instanceof Node) {
                p.visit(this);
            }
        }
    }

    @Override
    public void visit(Node n)
    {
        if(n.isUsable() && !n.isTagged() && n.getReferrers().isEmpty())
            emptyNodes.add(n);
    }

    @Override
    public void endTest()
    {
        for(Node node : emptyNodes)
        {
            if(node.hasKeys())
                errors.add( new TestError(this, Severity.OTHER, tr("Untagged and unconnected nodes (commented)"), COMMENT_NODE, node) );
            else
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

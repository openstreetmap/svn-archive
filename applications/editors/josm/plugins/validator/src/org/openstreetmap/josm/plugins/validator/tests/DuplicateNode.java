package org.openstreetmap.josm.plugins.validator.tests;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.geom.Area;
import java.util.Collection;
import java.util.LinkedList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.MergeNodesAction;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.BackreferencedDataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.gui.ConditionalOptionPaneUtil;
import org.openstreetmap.josm.gui.progress.ProgressMonitor;
import org.openstreetmap.josm.plugins.validator.Severity;
import org.openstreetmap.josm.plugins.validator.Test;
import org.openstreetmap.josm.plugins.validator.TestError;
import org.openstreetmap.josm.plugins.validator.util.Bag;
/**
 * Tests if there are duplicate nodes
 *
 * @author frsantos
 */
public class DuplicateNode extends Test{

	private static BackreferencedDataSet backreferences;

	public static BackreferencedDataSet getBackreferenceDataSet() {
		if (backreferences == null) {
			backreferences = new BackreferencedDataSet();
		}
		return backreferences;
	}

	public static void clearBackreferences() {
		backreferences = null;
	}

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
    public void startTest(ProgressMonitor monitor) {
    	super.startTest(monitor);
        nodes = new Bag<LatLon, OsmPrimitive>(1000);
    }

	@Override
	public void endTest() {
		for (List<OsmPrimitive> duplicated : nodes.values()) {
			if (duplicated.size() > 1) {
				boolean sameTags = true;
				Map<String, String> keys0 = duplicated.get(0).getKeys();
				keys0.remove("created_by");
				for (int i = 0; i < duplicated.size(); i++) {
					Map<String, String> keysI = duplicated.get(i).getKeys();
					keysI.remove("created_by");
					if (!keys0.equals(keysI))
						sameTags = false;
				}
				if (!sameTags) {
					TestError testError = new TestError(this, Severity.WARNING,
							tr("Nodes at same position"), DUPLICATE_NODE,
							duplicated);
					errors.add(testError);
				} else {
					TestError testError = new TestError(this, Severity.ERROR,
							tr("Duplicated nodes"), DUPLICATE_NODE, duplicated);
					errors.add(testError);
				}
			}
		}
		super.endTest();
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
        Collection<OsmPrimitive> sel = new LinkedList<OsmPrimitive>(testError.getPrimitives());
        LinkedHashSet<Node> nodes = new LinkedHashSet<Node>(OsmPrimitive.getFilteredList(sel, Node.class));
        Node target = MergeNodesAction.selectTargetNode(nodes);
        if(checkAndConfirmOutlyingDeletes(nodes))
            return MergeNodesAction.mergeNodes(Main.main.getEditLayer(),getBackreferenceDataSet(), nodes, target);

        return null;// undoRedo handling done in mergeNodes
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
    private static boolean checkAndConfirmOutlyingDeletes(LinkedHashSet<Node> del) {
        Area a = Main.main.getCurrentDataSet().getDataSourceArea();
        if (a != null) {
            for (OsmPrimitive osm : del) {
                if (osm instanceof Node && !osm.isNew()) {
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

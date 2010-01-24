package org.openstreetmap.josm.plugins.validator.tests;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.MergeNodesAction;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.data.coor.LatLon;
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

    protected static int DUPLICATE_NODE = 1;

    /** The map of potential duplicates.
     *
     * If there is exactly one node for a given pos, the map includes a pair <pos, Node>.
     * If there are multiple nodes for a given pos, the map includes a pair
     * <pos, NodesByEqualTagsMap>
     */
    Map<LatLon,Object> potentialDuplicates;

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
        potentialDuplicates = new HashMap<LatLon, Object>();
    }


	@Override
	public void endTest() {
		for (Entry<LatLon, Object> entry: potentialDuplicates.entrySet()) {
			Object v = entry.getValue();
			if (v instanceof Node) {
				// just one node at this position. Nothing to report as
				// error
				continue;
			}

			// multiple nodes at the same position -> report errors
			//
			NodesByEqualTagsMap map = (NodesByEqualTagsMap)v;
			errors.addAll(map.buildTestErrors(this));
		}
		super.endTest();
		potentialDuplicates = null;
	}

	@Override
	public void visit(Node n) {
		if (n.isUsable()) {
			LatLon rounded = n.getCoor().getRoundedToOsmPrecision();
			if (potentialDuplicates.get(rounded) == null) {
				// in most cases there is just one node at a given position. We
				// avoid to create an extra object and add remember the node
				// itself at this position
				potentialDuplicates.put(rounded, n);
			} else if (potentialDuplicates.get(rounded) instanceof Node) {
				// we have an additional node at the same position. Create an extra
				// object to keep track of the nodes at this position.
				//
				Node n1 = (Node)potentialDuplicates.get(rounded);
				NodesByEqualTagsMap map = new NodesByEqualTagsMap();
				map.add(n1);
				map.add(n);
				potentialDuplicates.put(rounded, map);
			} else if (potentialDuplicates.get(rounded) instanceof NodesByEqualTagsMap) {
				// we have multiple nodes at the same position.
				//
				NodesByEqualTagsMap map = (NodesByEqualTagsMap)potentialDuplicates.get(rounded);
				map.add(n);
			}
		}
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

        // Use first existing node or first node if all nodes are new
        Node target = null;
        for (Node n: nodes) {
            if (!n.isNew()) {
                target = n;
                break;
            }
        }
        if (target == null) {
            target = nodes.iterator().next();
        }

        if(checkAndConfirmOutlyingDeletes(nodes))
            return MergeNodesAction.mergeNodes(Main.main.getEditLayer(), nodes, target);

        return null;// undoRedo handling done in mergeNodes
    }

	@Override
	public boolean isFixable(TestError testError) {
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

    static private class NodesByEqualTagsMap {
    	/**
    	 * a bag of primitives with the same position. The bag key is represented
    	 * by the tag set of the primitive. This allows for easily find nodes at
    	 * the same position with the same tag sets later.
    	 */
    	private Bag<Map<String,String>, OsmPrimitive> bag;

    	public NodesByEqualTagsMap() {
    		bag = new Bag<Map<String,String>, OsmPrimitive>();
    	}

    	public void add(Node n) {
    		bag.add(n.getKeys(), n);
    	}

    	public List<TestError> buildTestErrors(Test parentTest) {
    		List<TestError> errors = new ArrayList<TestError>();
    		// check whether we have multiple nodes at the same position with
    		// the same tag set
    		//
    		for (Iterator<Map<String,String>> it = bag.keySet().iterator(); it.hasNext(); ) {
    			Map<String,String> tagSet = it.next();
    			if (bag.get(tagSet).size() > 1) {
    				errors.add(new TestError(
    						parentTest,
    						Severity.ERROR,
    						tr("Duplicated nodes"),
    						DUPLICATE_NODE,
    						bag.get(tagSet)
    				));
    				it.remove();
    			}

    		}

    		// check whether we have multiple nodes at the same position with
    		// differing tag sets
    		//
    		if (!bag.isEmpty()) {
    			List<OsmPrimitive> duplicates = new ArrayList<OsmPrimitive>();
    			for (List<OsmPrimitive> l: bag.values()) {
    				duplicates.addAll(l);
    			}
    			if (duplicates.size() > 1) {
	    			errors.add(new TestError(
	    					parentTest,
	    					Severity.WARNING,
							tr("Nodes at same position"),
							DUPLICATE_NODE,
							duplicates
					));
    			}
    		}
    		return errors;
    	}
    }
}

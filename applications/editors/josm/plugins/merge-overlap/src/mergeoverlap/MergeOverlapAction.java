package mergeoverlap;

import static org.openstreetmap.josm.gui.conflict.tags.TagConflictResolutionUtil.combineTigerTags;
import static org.openstreetmap.josm.gui.conflict.tags.TagConflictResolutionUtil.completeTagCollectionForEditing;
import static org.openstreetmap.josm.gui.conflict.tags.TagConflictResolutionUtil.normalizeTagCollectionBeforeEditing;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.actions.SplitWayAction;
import org.openstreetmap.josm.actions.CombineWayAction.NodeGraph;
import org.openstreetmap.josm.actions.SplitWayAction.SplitWayResult;
import org.openstreetmap.josm.command.AddCommand;
import org.openstreetmap.josm.command.ChangeCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.DeleteCommand;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.corrector.ReverseWayTagCorrector;
import org.openstreetmap.josm.corrector.UserCancelException;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.TagCollection;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.tools.Pair;
import org.openstreetmap.josm.tools.Shortcut;

/**
 * Merge overlapping part of ways.
 */
@SuppressWarnings("serial")
public class MergeOverlapAction extends JosmAction {

	public MergeOverlapAction() {
		super(tr("Merge overlap"), "merge_overlap",
				tr("Merge overlap of ways."), 
				Shortcut.registerShortcut("tools:mergeoverlap",tr("Tool: {0}", tr("Merge overlap")), KeyEvent.VK_O,
				Shortcut.GROUP_EDIT, Shortcut.SHIFT_DEFAULT)
				, true);
	}

	Map<Way, List<Relation>> relations = new HashMap<Way, List<Relation>>();
	Map<Way, Way> oldWays = new HashMap<Way, Way>();
	Map<Relation, Relation> newRelations = new HashMap<Relation, Relation>();
	Set<Way> deletes = new HashSet<Way>();

	/**
	 * The action button has been clicked
	 * 
	 * @param e
	 *            Action Event
	 */
	public void actionPerformed(ActionEvent e) {

		// List of selected ways
		List<Way> ways = new ArrayList<Way>();
		relations.clear();
		newRelations.clear();

		// For every selected way
		for (OsmPrimitive osm : Main.main.getCurrentDataSet().getSelected()) {
			if (osm instanceof Way && !osm.isDeleted()) {
				Way way = (Way) osm;
				ways.add(way);
				List<Relation> rels = new ArrayList<Relation>();
				for (Relation r : OsmPrimitive.getFilteredList(way
						.getReferrers(), Relation.class)) {
					rels.add(r);
				}
				relations.put(way, rels);
			}
		}

		List<Way> sel = new ArrayList<Way>(ways);
		Collection<Command> cmds = new LinkedList<Command>();

		// *****
		// split
		// *****
		for (Way way : ways) {
			Set<Node> nodes = new HashSet<Node>();
			for (Way opositWay : ways) {
				if (way != opositWay) {
					List<NodePos> nodesPos = new LinkedList<NodePos>();

					int pos = 0;
					for (Node node : way.getNodes()) {
						int opositPos = 0;
						for (Node opositNode : opositWay.getNodes()) {
							if (node == opositNode) {
								if (opositWay.isClosed()) {
									opositPos %= opositWay.getNodesCount() - 1;
								}
								nodesPos.add(new NodePos(node, pos, opositPos));
								break;
							}
							opositPos++;
						}
						pos++;
					}

					NodePos start = null;
					NodePos end = null;
					int increment = 0;

					boolean hasFirst = false;
					for (NodePos node : nodesPos) {
						if (start == null) {
							start = node;
						} else {
							if (end == null) {
								if (follows(way, opositWay, start, node, 1)) {
									end = node;
									increment = +1;
								} else if (follows(way, opositWay, start, node,
										-1)) {
									end = node;
									increment = -1;
								} else {
									start = node;
									end = null;
								}
							} else {
								if (follows(way, opositWay, end, node,
										increment)) {
									end = node;
								} else {
									hasFirst = addNodes(start, end, way, nodes,
											hasFirst);
									start = node;
									end = null;
								}
							}
						}
					}

					if (start != null && end != null) {
						hasFirst = addNodes(start, end, way, nodes, hasFirst);
						start = null;
						end = null;
					}
				}
			}
			if (!nodes.isEmpty() && !way.isClosed() || nodes.size() >= 2) {
				List<List<Node>> wayChunks = SplitWayAction.buildSplitChunks(
						way, new ArrayList<Node>(nodes));
				SplitWayResult result = splitWay(getEditLayer(), way, wayChunks);

				cmds.add(result.getCommand());
				sel.remove(way);
				sel.add(result.getOriginalWay());
				sel.addAll(result.getNewWays());
				List<Relation> rels = relations.remove(way);
				relations.put(result.getOriginalWay(), rels);
				for (Way w : result.getNewWays()) {
					relations.put(w, rels);
				}
			}
		}

		// *****
		// merge
		// *****
		ways = new ArrayList<Way>(sel);
		while (!ways.isEmpty()) {
			Way way = ways.get(0);
			List<Way> combine = new ArrayList<Way>();
			combine.add(way);
			for (Way opositWay : ways) {
				if (way != opositWay
						&& way.getNodesCount() == opositWay.getNodesCount()) {
					boolean equals1 = true;
					for (int i = 0; i < way.getNodesCount(); i++) {
						if (way.getNode(i) != opositWay.getNode(i)) {
							equals1 = false;
							break;
						}
					}
					boolean equals2 = true;
					for (int i = 0; i < way.getNodesCount(); i++) {
						if (way.getNode(i) != opositWay.getNode(way
								.getNodesCount()
								- i - 1)) {
							equals2 = false;
							break;
						}
					}
					if (equals1 || equals2) {
						combine.add(opositWay);
					}
				}
			}
			ways.removeAll(combine);
			if (combine.size() > 1) {
				sel.removeAll(combine);
				// combine
				Pair<Way, List<Command>> combineResult;
				try {
					combineResult = combineWaysWorker(combine);
				} catch (UserCancelException e1) {
					return;
				}
				sel.add(combineResult.a);
				cmds.addAll(combineResult.b);
			}
		}

		for (Relation old : newRelations.keySet()) {
			cmds.add(new ChangeCommand(old, newRelations.get(old)));
		}

		List<Way> del = new LinkedList<Way>();
		for (Way w : deletes) {
			if (!w.isDeleted()) {
				del.add(w);
			}
		}
		if (!del.isEmpty()) {
			cmds.add(new DeleteCommand(del));
		}

		// Commit
		Main.main.undoRedo.add(new SequenceCommand(
				tr("Merge Overlap (combine)"), cmds));
		getCurrentDataSet().setSelected(sel);
		Main.map.repaint();

		relations.clear();
		newRelations.clear();
		oldWays.clear();
	}

	private class NodePos {
		Node node;
		int pos;
		int opositPos;

		NodePos(Node n, int p, int op) {
			node = n;
			pos = p;
			opositPos = op;
		}

		public String toString() {
			return "NodePos: " + pos + ", " + opositPos + ", " + node;
		}
	}

	private boolean addNodes(NodePos start, NodePos end, Way way,
			Set<Node> nodes, boolean hasFirst) {
		if (way.isClosed()
				|| (start.node != way.getNode(0) && start.node != way
						.getNode(way.getNodesCount() - 1))) {
			hasFirst = hasFirst || start.node == way.getNode(0);
			nodes.add(start.node);
		}
		if (way.isClosed()
				|| (end.node != way.getNode(0) && end.node != way.getNode(way
						.getNodesCount() - 1))) {
			if (hasFirst && (end.node == way.getNode(way.getNodesCount() - 1))) {
				nodes.remove(way.getNode(0));
			} else {
				nodes.add(end.node);
			}
		}
		return hasFirst;
	}

	private boolean follows(Way way1, Way way2, NodePos np1, NodePos np2,
			int incr) {
		if (way2.isClosed() && incr == 1
				&& np1.opositPos == way2.getNodesCount() - 2) {
			return np2.pos == np1.pos + 1 && np2.opositPos == 0;
		} else if (way2.isClosed() && incr == 1 && np1.opositPos == 0) {
			return np2.pos == np1.pos && np2.opositPos == 0
					|| np2.pos == np1.pos + 1 && np2.opositPos == 1;
		} else if (way2.isClosed() && incr == -1 && np1.opositPos == 0) {
			return np2.pos == np1.pos && np2.opositPos == 0
					|| np2.pos == np1.pos + 1
					&& np2.opositPos == way2.getNodesCount() - 2;
		} else {
			return np2.pos == np1.pos + 1
					&& np2.opositPos == np1.opositPos + incr;
		}
	}

	/**
	 * Splits a way
	 * 
	 * @param layer
	 * @param way
	 * @param wayChunks
	 * @return
	 */
	private SplitWayResult splitWay(OsmDataLayer layer, Way way,
			List<List<Node>> wayChunks) {
		// build a list of commands, and also a new selection list
		Collection<Command> commandList = new ArrayList<Command>(wayChunks
				.size());

		Iterator<List<Node>> chunkIt = wayChunks.iterator();
		Collection<String> nowarnroles = Main.pref.getCollection(
				"way.split.roles.nowarn", Arrays.asList(new String[] { "outer",
						"inner", "forward", "backward" }));

		// First, change the original way
		Way changedWay = new Way(way);
		oldWays.put(changedWay, way);
		changedWay.setNodes(chunkIt.next());
		commandList.add(new ChangeCommand(way, changedWay));

		List<Way> newWays = new ArrayList<Way>();
		// Second, create new ways
		while (chunkIt.hasNext()) {
			Way wayToAdd = new Way();
			wayToAdd.setKeys(way.getKeys());
			newWays.add(wayToAdd);
			wayToAdd.setNodes(chunkIt.next());
			commandList.add(new AddCommand(layer, wayToAdd));
		}
		boolean warnmerole = false;
		boolean warnme = false;
		// now copy all relations to new way also

		for (Relation r : getParentRelations(way)) {
			if (!r.isUsable()) {
				continue;
			}
			Relation c = null;
			String type = r.get("type");
			if (type == null) {
				type = "";
			}

			int i_c = 0, i_r = 0;
			List<RelationMember> relationMembers = r.getMembers();
			for (RelationMember rm : relationMembers) {
				if (rm.isWay() && rm.getMember() == way) {
					boolean insert = true;
					if ("restriction".equals(type)) {
						/*
						 * this code assumes the restriction is correct. No real
						 * error checking done
						 */
						String role = rm.getRole();
						if ("from".equals(role) || "to".equals(role)) {
							OsmPrimitive via = null;
							for (RelationMember rmv : r.getMembers()) {
								if ("via".equals(rmv.getRole())) {
									via = rmv.getMember();
								}
							}
							List<Node> nodes = new ArrayList<Node>();
							if (via != null) {
								if (via instanceof Node) {
									nodes.add((Node) via);
								} else if (via instanceof Way) {
									nodes.add(((Way) via).lastNode());
									nodes.add(((Way) via).firstNode());
								}
							}
							Way res = null;
							for (Node n : nodes) {
								if (changedWay.isFirstLastNode(n)) {
									res = way;
								}
							}
							if (res == null) {
								for (Way wayToAdd : newWays) {
									for (Node n : nodes) {
										if (wayToAdd.isFirstLastNode(n)) {
											res = wayToAdd;
										}
									}
								}
								if (res != null) {
									if (c == null) {
										c = getNew(r);
									}
									c.addMember(new RelationMember(role, res));
									c.removeMembersFor(way);
									insert = false;
								}
							} else {
								insert = false;
							}
						} else if (!"via".equals(role)) {
							warnme = true;
						}
					} else if (!("route".equals(type))
							&& !("multipolygon".equals(type))) {
						warnme = true;
					}
					if (c == null) {
						c = getNew(r);
					}

					if (insert) {
						if (rm.hasRole() && !nowarnroles.contains(rm.getRole())) {
							warnmerole = true;
						}

						Boolean backwards = null;
						int k = 1;
						while (i_r - k >= 0 || i_r + k < relationMembers.size()) {
							if ((i_r - k >= 0)
									&& relationMembers.get(i_r - k).isWay()) {
								Way w = relationMembers.get(i_r - k).getWay();
								if ((w.lastNode() == way.firstNode())
										|| w.firstNode() == way.firstNode()) {
									backwards = false;
								} else if ((w.firstNode() == way.lastNode())
										|| w.lastNode() == way.lastNode()) {
									backwards = true;
								}
								break;
							}
							if ((i_r + k < relationMembers.size())
									&& relationMembers.get(i_r + k).isWay()) {
								Way w = relationMembers.get(i_r + k).getWay();
								if ((w.lastNode() == way.firstNode())
										|| w.firstNode() == way.firstNode()) {
									backwards = true;
								} else if ((w.firstNode() == way.lastNode())
										|| w.lastNode() == way.lastNode()) {
									backwards = false;
								}
								break;
							}
							k++;
						}

						int j = i_c;
						for (Way wayToAdd : newWays) {
							RelationMember em = new RelationMember(
									rm.getRole(), wayToAdd);
							j++;
							if ((backwards != null) && backwards) {
								c.addMember(i_c, em);
							} else {
								c.addMember(j, em);
							}
						}
						i_c = j;
					}
				}
				i_c++;
				i_r++;
			}

			if (c != null) {
				// commandList.add(new ChangeCommand(layer, r, c));
				newRelations.put(r, c);
			}
		}
		if (warnmerole) {
			JOptionPane
					.showMessageDialog(
							Main.parent,
							tr("<html>A role based relation membership was copied to all new ways.<br>You should verify this and correct it when necessary.</html>"),
							tr("Warning"), JOptionPane.WARNING_MESSAGE);
		} else if (warnme) {
			JOptionPane
					.showMessageDialog(
							Main.parent,
							tr("<html>A relation membership was copied to all new ways.<br>You should verify this and correct it when necessary.</html>"),
							tr("Warning"), JOptionPane.WARNING_MESSAGE);
		}

		return new SplitWayResult(
				new SequenceCommand(tr("Split way"), commandList), null,
				changedWay, newWays);
	}

	/**
	 * @param ways
	 * @return null if ways cannot be combined. Otherwise returns the combined
	 *         ways and the commands to combine
	 * @throws UserCancelException
	 */
	private Pair<Way, List<Command>> combineWaysWorker(Collection<Way> ways)
			throws UserCancelException {

		// prepare and clean the list of ways to combine
		if (ways == null || ways.isEmpty())
			return null;
		ways.remove(null); // just in case - remove all null ways from the
							// collection

		// remove duplicates, preserving order
		ways = new LinkedHashSet<Way>(ways);

		// try to build a new way which includes all the combined ways
		NodeGraph graph = NodeGraph.createUndirectedGraphFromNodeWays(ways);
		List<Node> path = graph.buildSpanningPath();

		// check whether any ways have been reversed in the process
		// and build the collection of tags used by the ways to combine
		TagCollection wayTags = TagCollection.unionOfAllPrimitives(ways);

		List<Way> reversedWays = new LinkedList<Way>();
		List<Way> unreversedWays = new LinkedList<Way>();
		for (Way w : ways) {
			if ((path.indexOf(w.getNode(0)) + 1) == path.lastIndexOf(w
					.getNode(1))) {
				unreversedWays.add(w);
			} else {
				reversedWays.add(w);
			}
		}
		// reverse path if all ways have been reversed
		if (unreversedWays.isEmpty()) {
			Collections.reverse(path);
			unreversedWays = reversedWays;
			reversedWays = null;
		}
		if ((reversedWays != null) && !reversedWays.isEmpty()) {
			// filter out ways that have no direction-dependent tags
			unreversedWays = ReverseWayTagCorrector
					.irreversibleWays(unreversedWays);
			reversedWays = ReverseWayTagCorrector
					.irreversibleWays(reversedWays);
			// reverse path if there are more reversed than unreversed ways with
			// direction-dependent tags
			if (reversedWays.size() > unreversedWays.size()) {
				Collections.reverse(path);
				List<Way> tempWays = unreversedWays;
				unreversedWays = reversedWays;
				reversedWays = tempWays;
			}
			// if there are still reversed ways with direction-dependent tags,
			// reverse their tags
			if (!reversedWays.isEmpty()) {
				List<Way> unreversedTagWays = new ArrayList<Way>(ways);
				unreversedTagWays.removeAll(reversedWays);
				ReverseWayTagCorrector reverseWayTagCorrector = new ReverseWayTagCorrector();
				List<Way> reversedTagWays = new ArrayList<Way>();
				Collection<Command> changePropertyCommands = null;
				for (Way w : reversedWays) {
					Way wnew = new Way(w);
					reversedTagWays.add(wnew);
					changePropertyCommands = reverseWayTagCorrector.execute(w,
							wnew);
				}
				if ((changePropertyCommands != null)
						&& !changePropertyCommands.isEmpty()) {
					for (Command c : changePropertyCommands) {
						c.executeCommand();
					}
				}
				wayTags = TagCollection.unionOfAllPrimitives(reversedTagWays);
				wayTags.add(TagCollection
						.unionOfAllPrimitives(unreversedTagWays));
			}
		}

		// create the new way and apply the new node list
		Way targetWay = getTargetWay(ways);
		Way modifiedTargetWay = new Way(targetWay);
		modifiedTargetWay.setNodes(path);

		TagCollection completeWayTags = new TagCollection(wayTags);
		combineTigerTags(completeWayTags);
		normalizeTagCollectionBeforeEditing(completeWayTags, ways);
		TagCollection tagsToEdit = new TagCollection(completeWayTags);
		completeTagCollectionForEditing(tagsToEdit);

		MyCombinePrimitiveResolverDialog dialog = MyCombinePrimitiveResolverDialog
				.getInstance();
		dialog.getTagConflictResolverModel().populate(tagsToEdit,
				completeWayTags.getKeysWithMultipleValues());
		dialog.setTargetPrimitive(targetWay);
		Set<Relation> parentRelations = getParentRelations(ways);
		dialog.getRelationMemberConflictResolverModel().populate(
				parentRelations, ways, oldWays);
		dialog.prepareDefaultDecisions();

		// resolve tag conflicts if necessary
		if (askForMergeTag(ways) || duplicateParentRelations(ways)) {
			dialog.setVisible(true);
			if (dialog.isCancelled())
				throw new UserCancelException();
		}

		LinkedList<Command> cmds = new LinkedList<Command>();
		deletes.addAll(ways);
		deletes.remove(targetWay);

		cmds.add(new ChangeCommand(targetWay, modifiedTargetWay));
		cmds.addAll(dialog.buildWayResolutionCommands());
		dialog.buildRelationCorrespondance(newRelations, oldWays);

		return new Pair<Way, List<Command>>(targetWay, cmds);
	}

	private static Way getTargetWay(Collection<Way> combinedWays) {
		// init with an arbitrary way
		Way targetWay = combinedWays.iterator().next();

		// look for the first way already existing on
		// the server
		for (Way w : combinedWays) {
			targetWay = w;
			if (!w.isNew()) {
				break;
			}
		}
		return targetWay;
	}

	/**
	 * @return has tag to be merged (=> ask)
	 */
	private static boolean askForMergeTag(Collection<Way> ways) {
		for (Way way : ways) {
			for (Way oposite : ways) {
				for (String key : way.getKeys().keySet()) {
					if (!"source".equals(key) && oposite.hasKey(key)
							&& !way.get(key).equals(oposite.get(key))) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * @return has duplicate parent relation
	 */
	private boolean duplicateParentRelations(Collection<Way> ways) {
		Set<Relation> relations = new HashSet<Relation>();
		for (Way w : ways) {
			List<Relation> rs = getParentRelations(w);
			for (Relation r : rs) {
				if (relations.contains(r)) {
					return true;
				}
			}
			relations.addAll(rs);
		}
		return false;
	}

	/**
	 * Replies the set of referring relations
	 * 
	 * @return the set of referring relations
	 */
	private List<Relation> getParentRelations(Way way) {
		List<Relation> rels = new ArrayList<Relation>();
		for (Relation r : relations.get(way)) {
			if (newRelations.containsKey(r)) {
				rels.add(newRelations.get(r));
			} else {
				rels.add(r);
			}
		}
		return rels;
	}

	private Relation getNew(Relation r) {
		return getNew(r, newRelations);
	}

	public static Relation getNew(Relation r,
			Map<Relation, Relation> newRelations) {
		if (newRelations.containsValue(r)) {
			return r;
		} else {
			Relation c = new Relation(r);
			newRelations.put(r, c);
			return c;
		}
	}

	private Way getOld(Way r) {
		return getOld(r, oldWays);
	}

	public static Way getOld(Way w, Map<Way, Way> oldWays) {
		if (oldWays.containsKey(w)) {
			return oldWays.get(w);
		} else {
			return w;
		}
	}

	/**
	 * Replies the set of referring relations
	 * 
	 * @return the set of referring relations
	 */
	private Set<Relation> getParentRelations(Collection<Way> ways) {
		HashSet<Relation> ret = new HashSet<Relation>();
		for (Way w : ways) {
			ret.addAll(getParentRelations(w));
		}
		return ret;
	}

	/** Enable this action only if something is selected */
	@Override
	protected void updateEnabledState() {
		if (getCurrentDataSet() == null) {
			setEnabled(false);
		} else {
			updateEnabledState(getCurrentDataSet().getSelected());
		}
	}

	/** Enable this action only if something is selected */
	@Override
	protected void updateEnabledState(
			Collection<? extends OsmPrimitive> selection) {
		if (selection == null) {
			setEnabled(false);
			return;
		}
		for (OsmPrimitive primitive : selection) {
			if (!(primitive instanceof Way) || primitive.isDeleted()) {
				setEnabled(false);
				return;
			}
		}
		setEnabled(selection.size() >= 2);
	}
}

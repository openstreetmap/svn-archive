// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.pt_assistant.actions;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.AlignInCircleAction;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.actions.SplitWayAction;
import org.openstreetmap.josm.actions.SplitWayAction.SplitWayResult;
import org.openstreetmap.josm.actions.downloadtasks.DownloadOsmTask;
import org.openstreetmap.josm.actions.relation.DownloadSelectedIncompleteMembersAction;
import org.openstreetmap.josm.command.ChangeCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.osm.BBox;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.dialogs.relation.DownloadRelationMemberTask;
import org.openstreetmap.josm.plugins.pt_assistant.utils.RouteUtils;
import org.openstreetmap.josm.tools.Pair;

/**
 * This action allows the user to split a selected roundabout.
 * The action will look for ways going in and out of the roudabout
 * which also are member of a public transport route. Having found
 * those, the roundabout will be split on the common points between
 * the ways and it. The routes will be fixed by connecting the entry
 * point to the exit point of the roundabout.
 *
 * @author giacomo
 */
public class SplitRoundaboutAction extends JosmAction {

    private static final String ACTION_NAME = "Split Roundabout";
    private static final long serialVersionUID = 8912249304286025356L;

    /**
     * Creates a new SplitRoundaboutAction
     */
    public SplitRoundaboutAction() {
        super(ACTION_NAME, "icons/splitroundabout", ACTION_NAME, null, true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        Way roundabout = (Way) getLayerManager().getEditDataSet().getSelected().iterator().next();

        //download the bbox around the roundabout
        DownloadOsmTask task = new DownloadOsmTask();
        task.setZoomAfterDownload(true);
        BBox rbbox = roundabout.getBBox();
        double latOffset = (rbbox.getTopLeftLat() - rbbox.getBottomRightLat()) / 10;
        double lonOffset = (rbbox.getBottomRightLon() - rbbox.getTopLeftLon()) / 10;
        Bounds area = new Bounds(
                rbbox.getBottomRightLat() - latOffset,
                rbbox.getTopLeftLon() - lonOffset,
                rbbox.getTopLeftLat() + latOffset,
                rbbox.getBottomRightLon() + lonOffset);
        Future<?> future = task.download(false, area, null);

        Main.worker.submit(() -> {
            try {
                future.get();
                downloadIncompleteRelations(roundabout);
            } catch (InterruptedException | ExecutionException e1) {
                 Main.error(e1);
                return;
            }
        });
    }

    private void continueAfterDownload(Way roundabout)
    {
        //make the roundabout round, if requested
        if(Main.pref.getBoolean("pt_assistant.roundabout-splitter.alignalways") ||
                JOptionPane.YES_OPTION == JOptionPane.showOptionDialog(Main.parent,
                tr("Do you want to make the roundabout round?"), tr("Roundabout round"),
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, null, null)) {
            new AlignInCircleAction().actionPerformed(null);
        }

        //save the position of the roundabout inside each relation
        Map<Relation, List<Integer>> savedPositions = getSavedPositions(roundabout);

        //remove the roundabout from each relation
        Main.main.undoRedo.add(getRemoveRoundaboutFromRelationsCommand(roundabout));

        //split the roundabout on the designed nodes
        List<Node> splitNodes = getSplitNodes(roundabout);
        SplitWayResult result = SplitWayAction.split(getLayerManager().getEditLayer(),
                roundabout, splitNodes, Collections.emptyList());
        Main.main.undoRedo.add(result.getCommand());
        Collection<Way> splitWays = result.getNewWays();
        splitWays.add(result.getOriginalWay());

        //update the relations.
        Main.main.undoRedo.add(getUpdateRelationsCommand(savedPositions, splitNodes, splitWays));
    }

    private void downloadIncompleteRelations(Way roundabout) {

        List<Relation> parents = getPTRouteParents(roundabout);
        parents.removeIf(r -> !r.hasIncompleteMembers());
        if(parents.isEmpty())
            continueAfterDownload(roundabout);

        Future <?>future = Main.worker.submit(new DownloadRelationMemberTask(
            parents,
            DownloadSelectedIncompleteMembersAction.buildSetOfIncompleteMembers(parents),
            Main.getLayerManager().getEditLayer()));

        Main.worker.submit(() -> {
            try {
                future.get();
                continueAfterDownload(roundabout);
            } catch (InterruptedException | ExecutionException e1) {
                 Main.error(e1);
                return;
            }
        });
    }

    public Command getUpdateRelationsCommand(Map<Relation,
            List<Integer>> savedPositions,
            List<Node> splitNodes, Collection<Way> splitWays) {

        Map<Relation, Relation> changingRelations =
                updateRelations(savedPositions, splitNodes, splitWays);

        List<Command> commands = new ArrayList<>();
        changingRelations.forEach((oldR, newR) ->
            commands.add(new ChangeCommand(oldR, newR)));

        return new SequenceCommand("Updating Relations for SplitRoundabout", commands);
    }

    private Map<Relation, Relation> updateRelations(Map<Relation,
            List<Integer>> savedPositions,
            List<Node> splitNodes, Collection<Way> splitWays) {
        Map<Relation, Relation> changingRelation = new HashMap<>();
        Map<Relation, Integer> memberOffset = new HashMap<>();
        savedPositions.forEach((r, positions) ->
            positions.forEach(i -> {

                if(!changingRelation.containsKey(r))
                    changingRelation.put(r, new Relation(r));

                Relation c = changingRelation.get(r);

                if(!memberOffset.containsKey(r))
                    memberOffset.put(r, 0);
                int offset = memberOffset.get(r);

                Pair<Way, Way> entryExitWays= getEntryExitWays(c, i + offset);
                Way entryWay = entryExitWays.a;
                Way exitWay = entryExitWays.b;

                if(entryWay == null || exitWay == null)
                    return;

                //get the entry and exit nodes, exit if not found
                Node entryNode = getNodeInCommon(splitNodes, entryWay);
                Node exitNode = getNodeInCommon(splitNodes, exitWay);

                if(entryNode == null || exitNode == null)
                    return;

                //starting from the entry node, add split ways until the
                //exit node is reached
                List<Way> parents = entryNode.getParentWays();
                parents.removeIf(w -> !w.firstNode().equals(entryNode));
                parents.removeIf(w -> w.equals(entryWay));

                Way curr = parents.get(0);

                while(!curr.lastNode().equals(exitNode)) {
                    c.addMember(i + offset++, new RelationMember(null, curr));
                    parents = curr.lastNode().getParentWays();
                    parents.remove(curr);
                    parents.removeIf(w -> !splitWays.contains(w));
                    curr = parents.get(0);
                }
                c.addMember(i + offset++, new RelationMember(null, curr));
                memberOffset.put(r, offset);
            }));
        return changingRelation;
    }

    private Node getNodeInCommon(List<Node> nodes, Way way) {
        if(nodes.contains(way.lastNode()))
            return way.lastNode();
        else if(nodes.contains(way.firstNode()))
            return way.firstNode();

        return null;
    }

    //given a relation and the position where the roundabout was, it returns
    //the entry and exit ways of that occurrence of the roundabout
    private Pair<Way, Way> getEntryExitWays(Relation r, Integer position) {

        //the ways returned are the one exactly before and after the roundabout
        Pair<Way, Way> ret = new Pair<>(null, null);

        RelationMember before = r.getMember(position-1);
        if(before.isWay())
            ret.a = before.getWay();

        RelationMember after = r.getMember(position);
        if(after.isWay())
            ret.b = after.getWay();

        return ret;
    }

    //split only on the nodes which might be the
    //entry or exit point for some public transport route
    public List<Node> getSplitNodes(Way roundabout) {
        Set<Node> noDuplicateSplitNodes = new HashSet<>(roundabout.getNodes());
        List<Node> splitNodes = new ArrayList<>(noDuplicateSplitNodes);

        splitNodes.removeIf(n -> {
            List<Way> parents = n.getParentWays();
            if(parents.size() == 1)
                return true;
            parents.remove(roundabout);
            for(Way parent: parents) {
                if(!getPTRouteParents(parent).isEmpty()) {
                        return false;
                }
            }

            return true;
        });
        return splitNodes;
    }

    public Command getRemoveRoundaboutFromRelationsCommand(Way roundabout) {
        List<Command> commands = new ArrayList<>();
        getPTRouteParents(roundabout).forEach(r -> {
            Relation c = new Relation(r);
            c.removeMembersFor(roundabout);
            commands.add(new ChangeCommand(r, c));
        });

        return new SequenceCommand("Remove roundabout from relations", commands);
    }

    //save the position of the roundabout inside each public transport route
    //it is contained in
    public Map<Relation, List<Integer>> getSavedPositions(Way roundabout) {

        Map<Relation, List<Integer>> savedPositions = new HashMap<>();

        for(Relation curr : getPTRouteParents(roundabout)) {
            for(int j = 0; j < curr.getMembersCount(); j++) {
                if(curr.getMember(j).getUniqueId() == roundabout.getUniqueId()) {
                    if(!savedPositions.containsKey(curr))
                        savedPositions.put(curr, new ArrayList<>());
                    List<Integer> positions = savedPositions.get(curr);
                    positions.add(j - positions.size());
                }
            }
        }

        return savedPositions;
    }

    private List<Relation> getPTRouteParents(Way roundabout) {
        List <Relation> referrers = OsmPrimitive.getFilteredList(
                roundabout.getReferrers(), Relation.class);
        referrers.removeIf(r -> !RouteUtils.isPTRoute(r));
        return referrers;
    }

    @Override
    protected void updateEnabledState(
            Collection<? extends OsmPrimitive> selection) {
        setEnabled(false);
        if (selection == null || selection.size() != 1)
            return;
        OsmPrimitive selected = selection.iterator().next();
        if(selected.getType() != OsmPrimitiveType.WAY)
            return;
        if(((Way)selected).isClosed()
                && (selected.hasTag("junction", "roundabout")
                        || selected.hasTag("oneway", "yes"))) {
            setEnabled(true);
            return;
        }
    }
}

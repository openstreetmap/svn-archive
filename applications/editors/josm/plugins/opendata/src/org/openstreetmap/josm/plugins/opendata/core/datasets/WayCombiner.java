//    JOSM opendata plugin.
//    Copyright (C) 2011-2012 Don-vip
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with this program.  If not, see <http://www.gnu.org/licenses/>.
package org.openstreetmap.josm.plugins.opendata.core.datasets;

import static org.openstreetmap.josm.gui.conflict.tags.TagConflictResolutionUtil.combineTigerTags;
import static org.openstreetmap.josm.gui.conflict.tags.TagConflictResolutionUtil.completeTagCollectionForEditing;
import static org.openstreetmap.josm.gui.conflict.tags.TagConflictResolutionUtil.normalizeTagCollectionBeforeEditing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.openstreetmap.josm.actions.CombineWayAction;
import org.openstreetmap.josm.actions.CombineWayAction.NodeGraph;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.corrector.ReverseWayTagCorrector;
import org.openstreetmap.josm.corrector.UserCancelException;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.TagCollection;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.conflict.tags.CombinePrimitiveResolverDialog;

// FIXME: Try to refactor CombineWayAction instead of using this class
public class WayCombiner {
	
    protected static Way getTargetWay(Collection<Way> combinedWays) {
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
     * @param ways
     * @throws UserCancelException
     */
    public static void combineWays(Collection<Way> ways) throws UserCancelException {

        // prepare and clean the list of ways to combine
        //
        if (ways == null || ways.isEmpty())
            return;
        ways.remove(null); // just in case -  remove all null ways from the collection

        // remove duplicates, preserving order
        ways = new LinkedHashSet<Way>(ways);

        // try to build a new way which includes all the combined
        // ways
        //
        NodeGraph graph = NodeGraph.createUndirectedGraphFromNodeWays(ways);
        List<Node> path = graph.buildSpanningPath();
        if (path == null) {
            return;
        }
        // check whether any ways have been reversed in the process
        // and build the collection of tags used by the ways to combine
        //
        TagCollection wayTags = TagCollection.unionOfAllPrimitives(ways);

        List<Way> reversedWays = new LinkedList<Way>();
        List<Way> unreversedWays = new LinkedList<Way>();
        for (Way w: ways) {
            if ((path.indexOf(w.getNode(0)) + 1) == path.lastIndexOf(w.getNode(1))) {
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
            unreversedWays = ReverseWayTagCorrector.irreversibleWays(unreversedWays);
            reversedWays = ReverseWayTagCorrector.irreversibleWays(reversedWays);
            // reverse path if there are more reversed than unreversed ways with direction-dependent tags
            if (reversedWays.size() > unreversedWays.size()) {
                Collections.reverse(path);
                List<Way> tempWays = unreversedWays;
                unreversedWays = reversedWays;
                reversedWays = tempWays;
            }
            // if there are still reversed ways with direction-dependent tags, reverse their tags
            if (!reversedWays.isEmpty()) {
                List<Way> unreversedTagWays = new ArrayList<Way>(ways);
                unreversedTagWays.removeAll(reversedWays);
                ReverseWayTagCorrector reverseWayTagCorrector = new ReverseWayTagCorrector();
                List<Way> reversedTagWays = new ArrayList<Way>();
                Collection<Command> changePropertyCommands =  null;
                for (Way w : reversedWays) {
                    Way wnew = new Way(w);
                    reversedTagWays.add(wnew);
                    changePropertyCommands = reverseWayTagCorrector.execute(w, wnew);
                }
                if ((changePropertyCommands != null) && !changePropertyCommands.isEmpty()) {
                    for (Command c : changePropertyCommands) {
                        c.executeCommand();
                    }
                }
                wayTags = TagCollection.unionOfAllPrimitives(reversedTagWays);
                wayTags.add(TagCollection.unionOfAllPrimitives(unreversedTagWays));
            }
        }

        // create the new way and apply the new node list
        //
        Way targetWay = getTargetWay(ways);
        Way modifiedTargetWay = new Way(targetWay);
        modifiedTargetWay.setNodes(path);

        TagCollection completeWayTags = new TagCollection(wayTags);
        combineTigerTags(completeWayTags);
        normalizeTagCollectionBeforeEditing(completeWayTags, ways);
        TagCollection tagsToEdit = new TagCollection(completeWayTags);
        completeTagCollectionForEditing(tagsToEdit);

        CombinePrimitiveResolverDialog dialog = CombinePrimitiveResolverDialog.getInstance();
        dialog.getTagConflictResolverModel().populate(tagsToEdit, completeWayTags.getKeysWithMultipleValues());
        dialog.setTargetPrimitive(targetWay);
        Set<Relation> parentRelations = CombineWayAction.getParentRelations(ways);
        dialog.getRelationMemberConflictResolverModel().populate(
                parentRelations,
                ways
        );
        dialog.prepareDefaultDecisions();

        // resolve tag conflicts if necessary
        //
        if (!completeWayTags.isApplicableToPrimitive() || !parentRelations.isEmpty()) {
            dialog.setVisible(true);
            //if (dialog.isCanceled()) // FIXME
                throw new UserCancelException();
        }

        LinkedList<Way> deletedWays = new LinkedList<Way>(ways);
        deletedWays.remove(targetWay);

        //new ChangeCommand(targetWay, modifiedTargetWay).executeCommand();
        targetWay.cloneFrom(modifiedTargetWay);
        /*for (Command c : dialog.buildResolutionCommands()) {
        	c.executeCommand();//FIXME
        }*/
        //new DeleteCommand(deletedWays).executeCommand();
        for (Way way: deletedWays) {
            way.setNodes(null);
            way.setDeleted(true);
            way.getDataSet().removePrimitive(way);
        }
    }
}

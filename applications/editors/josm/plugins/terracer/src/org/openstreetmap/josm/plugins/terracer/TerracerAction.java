// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.terracer;

import static org.openstreetmap.josm.tools.I18n.tr;
import static org.openstreetmap.josm.tools.I18n.trn;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.command.AddCommand;
import org.openstreetmap.josm.command.ChangeCommand;
import org.openstreetmap.josm.command.ChangePropertyCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.DeleteCommand;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.UndoRedoHandler;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Tag;
import org.openstreetmap.josm.data.osm.TagCollection;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.ExtendedDialog;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.conflict.tags.CombinePrimitiveResolverDialog;
import org.openstreetmap.josm.tools.Logging;
import org.openstreetmap.josm.tools.Pair;
import org.openstreetmap.josm.tools.Shortcut;
import org.openstreetmap.josm.tools.UserCancelException;
import org.openstreetmap.josm.tools.Utils;

/**
 * Terraces a quadrilateral, closed way into a series of quadrilateral,
 * closed ways. If two ways are selected and one of them can be identified as
 * a street (highway=*, name=*) then the given street will be added
 * to the 'associatedStreet' relation.
 *
 *
 * At present it only works on quadrilaterals, but there is no reason
 * why it couldn't be extended to work with other shapes too. The
 * algorithm employed is naive, but it works in the simple case.
 *
 * @author zere - Copyright 2009 CloudMade Ltd
 */
public final class TerracerAction extends JosmAction {

    private Collection<Command> commands;
    private Collection<OsmPrimitive> primitives;
    private TagCollection tagsInConflict;

    public TerracerAction() {
        super(tr("Terrace a building"), "terrace",
                tr("Creates individual buildings from a long building."),
                Shortcut.registerShortcut("tools:Terracer", tr("Tool: {0}",
                        tr("Terrace a building")), KeyEvent.VK_T,
                        Shortcut.SHIFT), true);
    }

    protected static Set<Relation> findAssociatedStreets(Collection<OsmPrimitive> objects) {
        Set<Relation> result = new HashSet<>();
        if (objects != null) {
            for (OsmPrimitive c : objects) {
                if (c != null) {
                    for (OsmPrimitive p : c.getReferrers()) {
                        if (p instanceof Relation && "associatedStreet".equals(p.get("type"))) {
                            result.add((Relation) p);
                        }
                    }
                }
            }
        }
        return result;
    }

    private static final class InvalidUserInputException extends Exception {
        InvalidUserInputException(String message) {
            super(message);
        }
    }

    /**
     * Checks that the selection is OK. If not, displays error message. If so
     * calls to terraceBuilding(), which does all the real work.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        Collection<OsmPrimitive> sel = getLayerManager().getEditDataSet().getSelected();
        Way outline = null;
        Way street = null;
        String streetname = null;
        ArrayList<Node> housenumbers = new ArrayList<>();
        Node init = null;

        try {
            if (sel.size() == 1) {
                OsmPrimitive prim = sel.iterator().next();

                if (!(prim instanceof Way))
                    throw new InvalidUserInputException(prim+" is not a way");

                outline = (Way) prim;
            } else if (sel.size() > 1) {
                List<Way> ways = new ArrayList<>(Utils.filteredCollection(sel, Way.class));
                Iterator<Way> wit = ways.iterator();
                while (wit.hasNext()) {
                    Way way = wit.next();
                    if (way.hasKey("building")) {
                        if (outline != null)
                            // already have a building
                            throw new InvalidUserInputException("already have a building");
                        outline = way;
                    } else if (way.hasKey("highway")) {
                        if (street != null)
                            // already have a street
                            throw new InvalidUserInputException("already have a street");
                        street = way;
                        streetname = street.get("name");
                        if (streetname == null)
                            throw new InvalidUserInputException("street does not have any name");
                    } else
                        throw new InvalidUserInputException(way+" is neither a building nor a highway");
                }

                if (outline == null)
                    throw new InvalidUserInputException("no outline way found");

                List<Node> nodes = new ArrayList<>(Utils.filteredCollection(sel, Node.class));
                Iterator<Node> nit = nodes.iterator();
                // Actually this should test if the selected address nodes lie
                // within the selected outline. Any ideas how to do this?
                while (nit.hasNext()) {
                    Node node = nit.next();
                    if (node.hasKey("addr:housenumber")) {
                        String nodesStreetName = node.get("addr:street");
                        // if a node has a street name if must be equal
                        // to the one of the other address nodes
                        if (nodesStreetName != null) {
                            if (streetname == null)
                                streetname = nodesStreetName;
                            else if (!nodesStreetName.equals(streetname))
                                throw new InvalidUserInputException("addr:street does not match street name");
                        }

                        housenumbers.add(node);
                    } else {
                        // A given node might not be an address node but then
                        // it has to be part of the building to help getting
                        // the number direction right.
                        if (!outline.containsNode(node) || init != null)
                            throw new InvalidUserInputException("node problem");
                        init = node;
                    }
                }

                Collections.sort(housenumbers, new HousenumberNodeComparator());
            }

            if (outline == null || !outline.isClosed() || outline.getNodesCount() < 5)
                throw new InvalidUserInputException("wrong or missing outline");

        } catch (InvalidUserInputException ex) {
            Logging.warn("Terracer: "+ex.getMessage());
            new ExtendedDialog(MainApplication.getMainFrame(), tr("Invalid selection"), new String[] {"OK"})
                .setButtonIcons(new String[] {"ok"}).setIcon(JOptionPane.INFORMATION_MESSAGE)
                .setContent(tr("Select a single, closed way of at least four nodes. " +
                    "(Optionally you can also select a street for the addr:street tag " +
                    "and a node to mark the start of numbering.)"))
                .showDialog();
            return;
        }

        Relation associatedStreet = null;

        // Try to find an associatedStreet relation that could be reused from housenumbers, outline and street.
        Set<OsmPrimitive> candidates = new HashSet<>(housenumbers);
        candidates.add(outline);
        if (street != null) {
            candidates.add(street);
        }

        Set<Relation> associatedStreets = findAssociatedStreets(candidates);

        if (!associatedStreets.isEmpty()) {
            associatedStreet = associatedStreets.iterator().next();
            if (associatedStreets.size() > 1) {
                // TODO: Deal with multiple associated Streets
                Logging.warn("Terracer: Found "+associatedStreets.size()+" associatedStreet relations. Considering the first one only.");
            }
        }

        if (streetname == null && associatedStreet != null && associatedStreet.hasKey("name")) {
            streetname = associatedStreet.get("name");
        }

        if (housenumbers.size() == 1) {
            // Special case of one outline and one address node.
            // Don't open the dialog
            try {
                terraceBuilding(outline, init, street, associatedStreet, 0, null, null, 0, 
                        housenumbers, streetname, associatedStreet != null, false, "yes");
            } catch (UserCancelException ex) {
                Logging.trace(ex);
            } finally {
                this.commands.clear();
                this.commands = null;
            }
        } else {
            String title = trn("Change {0} object", "Change {0} objects", sel.size(), sel.size());
            // show input dialog.
            new HouseNumberInputHandler(this, outline, init, street, streetname, outline.get("building"),
                    associatedStreet, housenumbers, title).dialog.showDialog();
        }
    }

    public Integer getNumber(String number) {
        try {
            return Integer.parseInt(number);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    /**
     * Sorts the house number nodes according their numbers only
     *
     * @param house
     *            number nodes
     */
    static class HousenumberNodeComparator implements Comparator<Node> {
        private final Pattern pat = Pattern.compile("^(\\d+)\\s*(.*)");

        @Override
        public int compare(Node node1, Node node2) {
            // It's necessary to strip off trailing non-numbers so we can
            // compare the numbers itself numerically since string comparison
            // doesn't work for numbers with different number of digits,
            // e.g. 9 is higher than 11
            String node1String = node1.get("addr:housenumber");
            String node2String = node2.get("addr:housenumber");
            Matcher mat = pat.matcher(node1String);
            if (mat.find()) {
                Integer node1Int = Integer.valueOf(mat.group(1));
                String node1Rest = mat.group(2);
                mat = pat.matcher(node2String);
                if (mat.find()) {
                    Integer node2Int = Integer.valueOf(mat.group(1));
                    // If the numbers are the same, the rest has to make the decision,
                    // e.g. when comparing 23, 23a and 23b.
                    if (node1Int.equals(node2Int)) {
                      String node2Rest = mat.group(2);
                      return node1Rest.compareTo(node2Rest);
                    }

                    return node1Int.compareTo(node2Int);
                }
            }

            return node1String.compareTo(node2String);
        }
    }

    /**
     * Terraces a single, closed, quadrilateral way.
     *
     * Any node must be adjacent to both a short and long edge, we naively
     * choose the longest edge and its opposite and interpolate along them
     * linearly to produce new nodes. Those nodes are then assembled into
     * closed, quadrilateral ways and left in the selection.
     *
     * @param outline The closed, quadrilateral way to terrace.
     * @param init The node that hints at which side to start the numbering
     * @param street The street, the buildings belong to (may be null)
     * @param associatedStreet associated street relation
     * @param segments The number of segments to generate
     * @param start Starting housenumber
     * @param end Ending housenumber
     * @param step The step width to use
     * @param housenumbers List of housenumbers to use. From and To are ignored
     *        if this is set.
     * @param streetName the name of the street, derived from the street line
     *        or the house numbers (may be null)
     * @param handleRelations If the user likes to add a relation or extend an
     *        existing relation
     * @param keepOutline If the outline way should be kept
     * @param buildingValue The value for {@code building} key to add
     * @throws UserCancelException if user cancels the operation
     */
    public void terraceBuilding(final Way outline, Node init, Way street, Relation associatedStreet, Integer segments,
                String start, String end, int step, List<Node> housenumbers, String streetName, boolean handleRelations,
                boolean keepOutline, String buildingValue) throws UserCancelException {
        final int nb;
        Integer to = null, from = null;
        if (housenumbers == null || housenumbers.isEmpty()) {
            to = getNumber(end);
            from = getNumber(start);
            if (to != null && from != null) {
                nb = 1 + (to.intValue() - from.intValue()) / step;
            } else if (segments != null) {
                nb = segments.intValue();
            } else {
                // if we get here, there is is a bug in the input validation.
                throw new TerracerRuntimeException(
                        "Could not determine segments from parameters, this is a bug. "
                                + "Parameters were: segments " + segments
                                + " from " + from + " to " + to + " step " + step);
            }
        } else {
            nb = housenumbers.size();
        }

        // now find which is the longest side connecting the first node
        Pair<Way, Way> interp = findFrontAndBack(outline);

        final boolean swap = init != null && (interp.a.lastNode().equals(init) || interp.b.lastNode().equals(init));

        final double frontLength = wayLength(interp.a);
        final double backLength = wayLength(interp.b);

        // new nodes array to hold all intermediate nodes
        // This set will contain at least 4 existing nodes from the original outline
        // (those, which coordinates match coordinates of outline nodes)
        Node[][] newNodes = new Node[2][nb + 1];
        // This list will contain nodes of the outline that are used in new lines.
        // These nodes will not be deleted with the outline (if deleting was prompted).
        List<Node> reusedNodes = new ArrayList<>();

        this.commands = new LinkedList<>();
        Collection<Way> ways = new LinkedList<>();
        DataSet ds = getLayerManager().getEditDataSet();

        if (nb > 1) {
            // add required new nodes and build list of nodes to reuse
            for (int i = 0; i <= nb; ++i) {
                int iDir = swap ? nb - i : i;
                newNodes[0][i] = interpolateAlong(interp.a, frontLength * iDir / nb);
                newNodes[1][i] = interpolateAlong(interp.b, backLength * iDir / nb);
                if (!outline.containsNode(newNodes[0][i]))
                    this.commands.add(new AddCommand(ds, newNodes[0][i]));
                else
                    reusedNodes.add(newNodes[0][i]);
                if (!outline.containsNode(newNodes[1][i]))
                    this.commands.add(new AddCommand(ds, newNodes[1][i]));
                else
                    reusedNodes.add(newNodes[1][i]);
            }

            // assemble new quadrilateral, closed ways
            for (int i = 0; i < nb; ++i) {
                final Way terr;
                boolean createNewWay = i > 0 || keepOutline;
                if (createNewWay) {
                    terr = new Way();
                    // add the tags of the outline to each building (e.g. source=*)
                    TagCollection.from(outline).applyTo(terr);
                } else {
                    terr = new Way(outline);
                    terr.setNodes(null);
                }

                terr.addNode(newNodes[0][i]);
                terr.addNode(newNodes[0][i + 1]);
                terr.addNode(newNodes[1][i + 1]);
                terr.addNode(newNodes[1][i]);
                terr.addNode(newNodes[0][i]);

                addressBuilding(terr, street, streetName, associatedStreet, housenumbers, i,
                        from != null ? Integer.toString(from + i * step) : null, buildingValue);

                if (createNewWay) {
                    ways.add(terr);
                    this.commands.add(new AddCommand(ds, terr));
                } else {
                    ways.add(outline);
                    this.commands.add(new ChangeCommand(outline, terr));
                }
            }

            if (!keepOutline) {
                // Delete outline nodes having no tags and referrers but the outline itself
                List<Node> nodes = outline.getNodes();
                ArrayList<Node> nodesToDelete = new ArrayList<>();
                for (Node n : nodes) {
                    if (!n.hasKeys() && n.getReferrers().size() == 1 && !reusedNodes.contains(n))
                        nodesToDelete.add(n);
                }
                if (!nodesToDelete.isEmpty())
                    this.commands.add(DeleteCommand.delete(nodesToDelete));
            }
        } else {
            // Single building, just add the address details
            addressBuilding(outline, street, streetName, associatedStreet, housenumbers, 0, start, buildingValue);
            ways.add(outline);
        }

        // Remove the address nodes since their tags have been incorporated into the terraces.
        // Or should removing them also be an option?
        if (!housenumbers.isEmpty()) {
            commands.add(DeleteCommand.delete(housenumbers, true, true));
        }

        if (handleRelations) { // create a new relation or merge with existing
            if (associatedStreet == null) {  // create a new relation
                addNewAssociatedStreetRelation(street, streetName, ways);
            } else { // relation exists already - add new members
                updateAssociatedStreetRelation(associatedStreet, housenumbers, ways);
            }
        }

        UndoRedoHandler.getInstance().add(createTerracingCommand(outline));
        if (nb <= 1 && street != null) {
            // Select the way (for quick selection of a new house (with the same way))
            MainApplication.getLayerManager().getEditDataSet().setSelected(street);
        } else {
            // Select the new building outlines (for quick reversing)
            MainApplication.getLayerManager().getEditDataSet().setSelected(ways);
        }
    }

    private void updateAssociatedStreetRelation(Relation associatedStreet, List<Node> housenumbers, Collection<Way> ways) {
        Relation newAssociatedStreet = new Relation(associatedStreet);
        // remove housenumbers as they have been deleted
        newAssociatedStreet.removeMembersFor(housenumbers);
        for (Way w : ways) {
            newAssociatedStreet.addMember(new RelationMember("house", w));
        }
        /*if (!keepOutline) {
            newAssociatedStreet.removeMembersFor(outline);
        }*/
        this.commands.add(new ChangeCommand(associatedStreet, newAssociatedStreet));
    }

    private void addNewAssociatedStreetRelation(Way street, String streetName, Collection<Way> ways) {
        Relation associatedStreet = new Relation();
        associatedStreet.put("type", "associatedStreet");
        if (street != null) { // a street was part of the selection
            associatedStreet.put("name", street.get("name"));
            associatedStreet.addMember(new RelationMember("street", street));
        } else {
            associatedStreet.put("name", streetName);
        }
        for (Way w : ways) {
            associatedStreet.addMember(new RelationMember("house", w));
        }
        this.commands.add(new AddCommand(getLayerManager().getEditDataSet(), associatedStreet));
    }

    private Command createTerracingCommand(final Way outline) {
        return new SequenceCommand(tr("Terrace"), commands) {
            @Override
            public boolean executeCommand() {
                boolean result = super.executeCommand();
                if (result && tagsInConflict != null) {
                    try {
                        // Build conflicts commands only after all primitives have been added to dataset to fix #8942
                        List<Command> conflictCommands = CombinePrimitiveResolverDialog.launchIfNecessary(
                                tagsInConflict, primitives, Collections.singleton(outline));
                        if (!conflictCommands.isEmpty()) {
                            List<Command> newCommands = new ArrayList<>(commands);
                            newCommands.addAll(conflictCommands);
                            setSequence(newCommands.toArray(new Command[0]));
                            // Run conflicts commands
                            for (int i = 0; i < conflictCommands.size(); i++) {
                                result = conflictCommands.get(i).executeCommand();
                                if (!result && !continueOnError) {
                                    setSequenceComplete(false);
                                    undoCommands(commands.size()+i-1);
                                    return false;
                                }
                            }
                        }
                    } catch (UserCancelException e) {
                        Logging.trace(e);
                    }
                }
                return result;
            }
        };
    }

    /**
     * Adds address details to a single building
     *
     * @param outline The closed, quadrilateral way to add the address to.
     * @param street The street, the buildings belong to (may be null)
     * @param streetName the name of a street (may be null). Used if not null and street is null.
     * @param associatedStreet The associated street. Used to determine if addr:street should be set or not.
     * @param buildingValue The value for {@code building} key to add
     * @return {@code outline}
     * @throws UserCancelException if user cancels the operation
     */
    private void addressBuilding(Way outline, Way street, String streetName, Relation associatedStreet,
            List<Node> housenumbers, int i, String defaultNumber, String buildingValue) throws UserCancelException {
        Node houseNum = (housenumbers != null && i >= 0 && i < housenumbers.size()) ? housenumbers.get(i) : null;
        boolean buildingAdded = false;
        boolean numberAdded = false;
        Map<String, String> tags = new HashMap<>();
        if (houseNum != null) {
            primitives = Arrays.asList(new OsmPrimitive[]{houseNum, outline});

            TagCollection tagsToCopy = TagCollection.unionOfAllPrimitives(primitives).getTagsFor(houseNum.keySet());
            tagsInConflict = tagsToCopy.getTagsFor(tagsToCopy.getKeysWithMultipleValues());
            tagsToCopy = tagsToCopy.minus(tagsInConflict).minus(TagCollection.from(outline));

            for (Tag tag : tagsToCopy) {
                tags.put(tag.getKey(), tag.getValue());
            }

            buildingAdded = houseNum.hasKey("building");
            numberAdded = houseNum.hasKey("addr:housenumber");
        }
        if (!buildingAdded && buildingValue != null && !buildingValue.isEmpty()) {
            tags.put("building", buildingValue);
        }
        if (defaultNumber != null && !numberAdded) {
            tags.put("addr:housenumber", defaultNumber);
        }
        // Only put addr:street if no relation exists or if it has no name
        if (associatedStreet == null || !associatedStreet.hasKey("name")) {
            if (street != null) {
                tags.put("addr:street", street.get("name"));
            } else if (streetName != null && !streetName.trim().isEmpty()) {
                tags.put("addr:street", streetName.trim());
            }
        }
        if (!tags.isEmpty()) {
            commands.add(new ChangePropertyCommand(getLayerManager().getEditDataSet(), Collections.singleton(outline), tags));
        }
    }

    /**
     * Creates a node at a certain distance along a way, as calculated by the
     * great circle distance.
     *
     * Note that this really isn't an efficient way to do this and leads to
     * O(N^2) running time for the main algorithm, but its simple and easy
     * to understand, and probably won't matter for reasonable-sized ways.
     *
     * @param w The way to interpolate.
     * @param l The length at which to place the node.
     * @return A node at a distance l along w from the first point.
     */
    private Node interpolateAlong(Way w, double l) {
        List<Pair<Node, Node>> pairs = w.getNodePairs(false);
        for (int i = 0; i < pairs.size(); ++i) {
            Pair<Node, Node> p = pairs.get(i);
            final double seg_length = p.a.getCoor().greatCircleDistance(p.b.getCoor());
            if (l <= seg_length || i == pairs.size() - 1) {
                // be generous on the last segment (numerical roudoff can lead to a small overshoot)
                return interpolateNode(p.a, p.b, l / seg_length);
            } else {
                l -= seg_length;
            }
        }
        // we shouldn't get here
        throw new IllegalStateException();
    }

    /**
     * Calculates the great circle length of a way by summing the great circle
     * distance of each pair of nodes.
     *
     * @param w The way to calculate length of.
     * @return The length of the way.
     */
    private double wayLength(Way w) {
        double length = 0.0;
        for (Pair<Node, Node> p : w.getNodePairs(false)) {
            length += p.a.getCoor().greatCircleDistance(p.b.getCoor());
        }
        return length;
    }

    /**
     * Given a way, try and find a definite front and back by looking at the
     * segments to find the "sides". Sides are assumed to be single segments
     * which cannot be contiguous.
     *
     * @param w The way to analyse.
     * @return A pair of ways (front, back) pointing in the same directions.
     */
    private Pair<Way, Way> findFrontAndBack(Way w) {
        // calculate the "side-ness" score for each segment of the way
        double[] sideness = calculateSideness(w);

        // find the largest two sidenesses which are not contiguous
        int[] indexes = sortedIndexes(sideness);
        int side1 = indexes[0];
        int side2 = indexes[1];
        // if side2 is contiguous with side1 then look further down the
        // list. we know there are at least 4 sides, as anything smaller
        // than a quadrilateral would have been rejected at an earlier stage.
        if (indexDistance(side1, side2, indexes.length) < 2) {
            side2 = indexes[2];
        }
        if (indexDistance(side1, side2, indexes.length) < 2) {
            side2 = indexes[3];
        }

        // if the second side has a shorter length and an approximately equal
        // sideness then its better to choose the shorter, as with
        // quadrilaterals
        // created using the orthogonalise tool the sideness will be about the
        // same for all sides.
        if (sideLength(w, side1) > sideLength(w, side1 + 1)
                && Math.abs(sideness[side1] - sideness[(side1 + 1) % (w.getNodesCount() - 1)]) < 0.001) {
            side1 = (side1 + 1) % (w.getNodesCount() - 1);
            side2 = (side2 + 1) % (w.getNodesCount() - 1);
        }

        // swap side1 and side2 into sorted order.
        if (side1 > side2) {
            int tmp = side2;
            side2 = side1;
            side1 = tmp;
        }

        Way front = new Way();
        Way back = new Way();
        for (int i = side2 + 1; i < w.getNodesCount() - 1; ++i) {
            front.addNode(w.getNode(i));
        }
        for (int i = 0; i <= side1; ++i) {
            front.addNode(w.getNode(i));
        }
        // add the back in reverse order so that the front and back ways point
        // in the same direction.
        for (int i = side2; i > side1; --i) {
            back.addNode(w.getNode(i));
        }

        return new Pair<>(front, back);
    }

    /**
     * returns the distance of two segments of a closed polygon
     */
    private int indexDistance(int i1, int i2, int n) {
        return Math.min(positiveModulus(i1 - i2, n), positiveModulus(i2 - i1, n));
    }

    /**
     * return the modulus in the range [0, n)
     */
    private int positiveModulus(int a, int n) {
        if (n <= 0)
            throw new IllegalArgumentException();
        int res = a % n;
        if (res < 0) {
            res += n;
        }
        return res;
    }

    /**
     * Calculate the length of a side (from node i to i+1) in a way. This assumes that
     * the way is closed, but I only ever call it for buildings.
     */
    private double sideLength(Way w, int i) {
        Node a = w.getNode(i);
        Node b = w.getNode((i + 1) % (w.getNodesCount() - 1));
        return a.getCoor().greatCircleDistance(b.getCoor());
    }

    /**
     * Given an array of doubles (but this could made generic very easily) sort
     * into order and return the array of indexes such that, for a returned array
     * x, a[x[i]] is sorted for ascending index i.
     *
     * This isn't efficient at all, but should be fine for the small arrays we're
     * expecting. If this gets slow - replace it with some more efficient algorithm.
     *
     * @param a The array to sort.
     * @return An array of indexes, the same size as the input, such that a[x[i]]
     * is in sorted order.
     */
    private int[] sortedIndexes(final double[] a) {
        class SortWithIndex implements Comparable<SortWithIndex> {
            public double x;
            public int i;

            SortWithIndex(double a, int b) {
                x = a;
                i = b;
            }

            @Override
            public int compareTo(SortWithIndex o) {
                return Double.compare(x, o.x);
            }
        }

        final int length = a.length;
        ArrayList<SortWithIndex> sortable = new ArrayList<>(length);
        for (int i = 0; i < length; ++i) {
            sortable.add(new SortWithIndex(a[i], i));
        }
        Collections.sort(sortable);

        int[] indexes = new int[length];
        for (int i = 0; i < length; ++i) {
            indexes[i] = sortable.get(i).i;
        }

        return indexes;
    }

    /**
     * Calculate "sideness" metric for each segment in a way.
     */
    private double[] calculateSideness(Way w) {
        final int length = w.getNodesCount() - 1;
        double[] sideness = new double[length];

        sideness[0] = calculateSideness(w.getNode(length - 1), w.getNode(0), w
                .getNode(1), w.getNode(2));
        for (int i = 1; i < length - 1; ++i) {
            sideness[i] = calculateSideness(w.getNode(i - 1), w.getNode(i), w
                    .getNode(i + 1), w.getNode(i + 2));
        }
        sideness[length - 1] = calculateSideness(w.getNode(length - 2), w
                .getNode(length - 1), w.getNode(length), w.getNode(1));

        return sideness;
    }

    /**
     * Calculate sideness of a single segment given the nodes which make up that
     * segment and its previous and next segments in order. Sideness is calculated
     * for the segment b-c.
     */
    private double calculateSideness(Node a, Node b, Node c, Node d) {
        final double ndx = b.getCoor().getX() - a.getCoor().getX();
        final double pdx = d.getCoor().getX() - c.getCoor().getX();
        final double ndy = b.getCoor().getY() - a.getCoor().getY();
        final double pdy = d.getCoor().getY() - c.getCoor().getY();

        return (ndx * pdx + ndy * pdy)
                / Math.sqrt((ndx * ndx + ndy * ndy) * (pdx * pdx + pdy * pdy));
    }

    /**
     * Creates a new node at the interpolated position between the argument
     * nodes. Interpolates linearly in projected coordinates.
     *
     * If new node coordinate matches a or b coordinates, a or b is returned.
     *
     * @param a First node, at which f=0.
     * @param b Last node, at which f=1.
     * @param f Fractional position between first and last nodes.
     * @return A new node at the interpolated position (or a or b in case if f ≈ 0 or f ≈ 1).
     */
    private Node interpolateNode(Node a, Node b, double f) {
        Node n = new Node(a.getEastNorth().interpolate(b.getEastNorth(), f));
        if (n.getCoor().equalsEpsilon(a.getCoor()))
            return a;
        if (n.getCoor().equalsEpsilon(b.getCoor()))
            return b;
        return n;
    }

    @Override
    protected void updateEnabledState() {
        setEnabled(getLayerManager().getEditDataSet() != null);
    }
}

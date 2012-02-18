/**
 * Terracer: A JOSM Plugin for terraced houses.
 *
 * Copyright 2009 CloudMade Ltd.
 *
 * Released under the GPLv2, see LICENSE file for details.
 */
package terracer;

import static org.openstreetmap.josm.tools.I18n.tr;
import static org.openstreetmap.josm.tools.I18n.trn;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.command.AddCommand;
import org.openstreetmap.josm.command.ChangeCommand;
import org.openstreetmap.josm.command.ChangePropertyCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.DeleteCommand;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.TagCollection;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.ExtendedDialog;
import org.openstreetmap.josm.tools.Pair;
import org.openstreetmap.josm.tools.Shortcut;

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
 * @author zere
 */
public final class TerracerAction extends JosmAction {

    // smsms1 asked for the last value to be remembered to make it easier to do
    // repeated terraces. this is the easiest, but not necessarily nicest, way.
    // private static String lastSelectedValue = "";

    Collection<Command> commands;

    public TerracerAction() {
        super(tr("Terrace a building"), "terrace",
                tr("Creates individual buildings from a long building."),
                Shortcut.registerShortcut("tools:Terracer", tr("Tool: {0}",
                        tr("Terrace a building")), KeyEvent.VK_T,
                        Shortcut.SHIFT), true);
    }

    /**
     * Checks that the selection is OK. If not, displays error message. If so
     * calls to terraceBuilding(), which does all the real work.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        Collection<OsmPrimitive> sel = getCurrentDataSet().getSelected();
        Way outline = null;
        Way street = null;
        String streetname = null;
        ArrayList<Node> housenumbers = new ArrayList<Node>();
        Node init = null;

        class InvalidUserInputException extends Exception {
            InvalidUserInputException(String message) {
                super(message);
            }

            InvalidUserInputException() {
                super();
            }
        }

        try {
            if (sel.size() == 1) {
                OsmPrimitive prim = sel.iterator().next();

                if (!(prim instanceof Way))
                    throw new InvalidUserInputException();

                outline = (Way) prim;
            } else if (sel.size() > 1) {
                List<Way> ways = OsmPrimitive.getFilteredList(sel, Way.class);
                Iterator<Way> wit = ways.iterator();
                while (wit.hasNext()) {
                    Way way = wit.next();
                    if (way.hasKey("building")) {
                        if (outline != null)
                            // already have a building
                            throw new InvalidUserInputException();
                        outline = way;
                    } else if (way.hasKey("highway")) {
                        if (street != null)
                            // already have a street
                            throw new InvalidUserInputException();
                        street = way;

                        if ((streetname = street.get("name")) == null)
                            throw new InvalidUserInputException();
                    } else
                        throw new InvalidUserInputException();
                }

                if (outline == null)
                    throw new InvalidUserInputException();

                List<Node> nodes = OsmPrimitive.getFilteredList(sel, Node.class);
                Iterator<Node> nit = nodes.iterator();
                // Actually this should test if the selected address nodes lie
                // within the selected outline. Any ideas how to do this?
                while (nit.hasNext()) {
                    Node node = nit.next();
                    if (node.hasKey("addr:housenumber")) {
                        String nodesstreetname = node.get("addr:street");
                        // if a node has a street name if must be equal
                        // to the one of the other address nodes
                        if (nodesstreetname != null) {
                            if (streetname == null)
                                streetname = nodesstreetname;
                            else if (!nodesstreetname.equals(streetname))
                                throw new InvalidUserInputException();
                        }

                        housenumbers.add(node);
                    } else {
                        // A given node might not be an address node but then
                        // it has to be part of the building to help getting
                        // the number direction right.
                        if (!outline.containsNode(node) || init != null)
                            throw new InvalidUserInputException();
                        init = node;
                    }
                }

                Collections.sort(housenumbers, new HousenumberNodeComparator());
            }

            if (outline == null || !outline.isClosed() || outline.getNodesCount() < 5)
                throw new InvalidUserInputException();
        } catch (InvalidUserInputException ex) {
            new ExtendedDialog(Main.parent, tr("Invalid selection"), new String[] {"OK"})
                .setButtonIcons(new String[] {"ok"}).setIcon(JOptionPane.INFORMATION_MESSAGE)
                .setContent(tr("Select a single, closed way of at least four nodes. " +
                    "(Optionally you can also select a street for the addr:street tag " +
                    "and a node to mark the start of numbering.)"))
                .showDialog();
            return;
        }

        // If we have a street, try to find an associatedStreet relation that could be reused.
        Relation associatedStreet = null;
        if (street != null) {
            outer:for (OsmPrimitive osm : Main.main.getCurrentDataSet().allNonDeletedPrimitives()) {
                if (!(osm instanceof Relation)) continue;
                Relation rel = (Relation) osm;
                if ("associatedStreet".equals(rel.get("type")) && street.get("name").equals(rel.get("name"))) {
                    List<RelationMember> members = rel.getMembers();
                    for (RelationMember m : members) {
                        if ("street".equals(m.getRole()) && m.isWay() && m.getMember().equals(street)) {
                            associatedStreet = rel;
                            break outer;
                        }
                    }
                }
            }
        }

        if (housenumbers.size() == 1) {
            // Special case of one outline and one address node.
            // Don't open the dialogue, just copy the node keys
            // to the outline, set building just in case it isn't there
            // and remove the node.
            Collection<Command> commands = new LinkedList<Command>();
            Way newOutline = new Way(outline);
            for (Entry<String, String> entry : housenumbers.get(0).getKeys()
                    .entrySet()) {
                newOutline.put(entry.getKey(), entry.getValue());
            }
            newOutline.put("building", "yes");
            commands.add(new ChangeCommand(outline, newOutline));
            commands.add(DeleteCommand.delete(Main.main.getEditLayer(),
                    housenumbers, true, true));
            Main.main.undoRedo
                    .add(new SequenceCommand(tr("Terrace"), commands));
            Main.main.getCurrentDataSet().setSelected(newOutline);
        } else {
            String title = trn("Change {0} object", "Change {0} objects", sel
                    .size(), sel.size());
            // show input dialog.
            new HouseNumberInputHandler(this, outline, init, street, streetname,
                    associatedStreet, housenumbers, title);
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
    class HousenumberNodeComparator implements Comparator<Node> {
        private final Pattern pat = Pattern.compile("^(\\d+)\\s*(.*)");

        /*
         * (non-Javadoc)
         *
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
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
                    if (node1Int.equals(node2Int))
                    {
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
     * @param associatedStreet
     * @param segments The number of segments to generate
     * @param From Starting housenumber
     * @param To Ending housenumber
     * @param step The step width to use
     * @param housenumbers List of housenumbers to use. From and To are ignored
     *        if this is set.
     * @param streetName the name of the street, derived from the street line
     *        or the house numbers (may be null)
     * @param handleRelations If the user likes to add a relation or extend an
     *        existing relation
     * @param deleteOutline If the outline way should be deleted when done
     */
    public void terraceBuilding(Way outline,
                Node init,
                Way street,
                Relation associatedStreet,
                Integer segments,
                String From,
                String To,
                int step,
                ArrayList<Node> housenumbers,
                String streetName,
                boolean handleRelations,
                boolean deleteOutline) {
        final int nb;
        Integer to = null, from = null;
        if (housenumbers.isEmpty()) {
            to = getNumber(To);
            from = getNumber(From);
            if (to != null && from != null) {
                nb = 1 + (to.intValue() - from.intValue()) / step;
            } else if (segments != null) {
                nb = segments.intValue();
            } else {
                // if we get here, there is is a bug in the input validation.
                throw new TerracerRuntimeException(
                        "Could not determine segments from parameters, this is a bug. "
                                + "Parameters were: segments " + segments
                                + " from " + from + " to " + to + " step "
                                + step);
            }
        } else {
            nb = housenumbers.size();
        }

        // now find which is the longest side connecting the first node
        Pair<Way, Way> interp = findFrontAndBack(outline);

        boolean swap = false;
        if (init != null) {
            if (interp.a.lastNode().equals(init) || interp.b.lastNode().equals(init)) {
                swap = true;
            }
        }

        final double frontLength = wayLength(interp.a);
        final double backLength = wayLength(interp.b);

        // new nodes array to hold all intermediate nodes
        // This set will contain at least 4 existing nodes from the original outline (those, which coordinates match coordinates of outline nodes)
        Node[][] new_nodes = new Node[2][nb + 1];
        // This list will contain nodes of the outline that are used in new lines.
        // These nodes will not be deleted with the outline (if deleting was prompted).
        ArrayList<Node> reused_nodes = new ArrayList<Node>();

        this.commands = new LinkedList<Command>();
        Collection<Way> ways = new LinkedList<Way>();

        if (nb > 1) {
            for (int i = 0; i <= nb; ++i) {
                int i_dir = swap ? nb - i : i;
                new_nodes[0][i] = interpolateAlong(interp.a, frontLength * i_dir / nb);
                new_nodes[1][i] = interpolateAlong(interp.b, backLength * i_dir / nb);
                if (!outline.containsNode(new_nodes[0][i]))
                    this.commands.add(new AddCommand(new_nodes[0][i]));
                else
                    reused_nodes.add(new_nodes[0][i]);
                if (!outline.containsNode(new_nodes[1][i]))
                    this.commands.add(new AddCommand(new_nodes[1][i]));
                else
                    reused_nodes.add(new_nodes[1][i]);
            }

            // assemble new quadrilateral, closed ways
            for (int i = 0; i < nb; ++i) {
                Way terr = new Way();
                terr.addNode(new_nodes[0][i]);
                terr.addNode(new_nodes[0][i + 1]);
                terr.addNode(new_nodes[1][i + 1]);
                terr.addNode(new_nodes[1][i]);
                terr.addNode(new_nodes[0][i]);

                // add the tags of the outline to each building (e.g. source=*)
                TagCollection.from(outline).applyTo(terr);

                String number = null;
                Set<Entry<String, String>> additionalKeys = null;
                if (housenumbers.isEmpty()) {
                    if (from != null) {
                        // only, if the user has specified house numbers
                        number = Integer.toString(from + i * step);
                    }
                } else {
                    number = housenumbers.get(i).get("addr:housenumber");
                    additionalKeys = housenumbers.get(i).getKeys().entrySet();
                }

                terr = addressBuilding(terr, street, streetName, number,
                        additionalKeys);

                ways.add(terr);
                this.commands.add(new AddCommand(terr));
            }

            if (deleteOutline) {
                // Delete outline nodes having no tags and referrers but the outline itself
                List<Node> nodes = outline.getNodes();
                ArrayList<Node> nodesToDelete = new ArrayList<Node>();
                for (Node n : nodes)
                    if (!n.hasKeys() && n.getReferrers().size() == 1 && !reused_nodes.contains(n))
                        nodesToDelete.add(n);
                if (nodesToDelete.size() > 0)
                    this.commands.add(DeleteCommand.delete(Main.main.getEditLayer(), nodesToDelete));
                // Delete the way itself without nodes
                this.commands.add(DeleteCommand.delete(Main.main.getEditLayer(), Collections.singleton(outline), false, true));

            }
        } else {
            // Single building, just add the address details
            Way newOutline;
            newOutline = addressBuilding(outline, street, streetName, From, null);
            ways.add(newOutline);
            this.commands.add(new ChangeCommand(outline, newOutline));
        }

        if (handleRelations) { // create a new relation or merge with existing
            if (associatedStreet == null) {  // create a new relation
                associatedStreet = new Relation();
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
                this.commands.add(new AddCommand(associatedStreet));
            } else { // relation exists already - add new members
                Relation newAssociatedStreet = new Relation(associatedStreet);
                for (Way w : ways) {
                    newAssociatedStreet.addMember(new RelationMember("house", w));
                }
                this.commands.add(new ChangeCommand(associatedStreet, newAssociatedStreet));
            }
        }

        // Remove the address node since their tags have been incorporated into
        // the terraces.
        // Or should removing them also be an option?
        if (!housenumbers.isEmpty())
            commands.add(DeleteCommand.delete(Main.main.getEditLayer(),
                    housenumbers, true, true));

        Main.main.undoRedo.add(new SequenceCommand(tr("Terrace"), commands));
        if (nb > 1) {
            // Select the new building outlines (for quick reversing)
            Main.main.getCurrentDataSet().setSelected(ways);
        } else if (street != null) {
            // Select the way (for quick selection of a new house (with the same way))
            Main.main.getCurrentDataSet().setSelected(street);
        }
    }

    /**
     * Adds address details to a single building
     *
     * @param outline The closed, quadrilateral way to add the address to.
     * @param street The street, the buildings belong to (may be null)
     * @param streetName the name of a street (may be null). Used if not null and street is null.
     * @param number The house number
     * @param additionalKeys More keys to be copied onto the new outline
     * @return the way with added address details
     */
    private Way addressBuilding(Way outline, Way street, String streetName,
            String number, Set<Entry<String, String>> additionalKeys) {
        Way changedOutline = outline;
        if (number != null) {
            // only, if the user has specified house numbers
            this.commands.add(new ChangePropertyCommand(changedOutline, "addr:housenumber", number));
        }
        if (additionalKeys != null) {
            for (Entry<String, String> entry : additionalKeys) {
                this.commands.add(new ChangePropertyCommand(changedOutline,
                        entry.getKey(), entry.getValue()));
            }
        }
        changedOutline.put("building", "yes");
        if (street != null) {
            this.commands.add(new ChangePropertyCommand(changedOutline, "addr:street", street.get("name")));
        } else if (streetName != null) {
            this.commands.add(new ChangePropertyCommand(changedOutline, "addr:street", streetName));
        }
        return changedOutline;
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
        List<Pair<Node,Node>> pairs = w.getNodePairs(false);
        for (int i = 0; i < pairs.size(); ++i) {
            Pair<Node,Node> p = pairs.get(i);
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

        return new Pair<Way, Way>(front, back);
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

            public SortWithIndex(double a, int b) {
                x = a;
                i = b;
            }

            @Override
            public int compareTo(SortWithIndex o) {
                return Double.compare(x, o.x);
            };
        }

        final int length = a.length;
        ArrayList<SortWithIndex> sortable = new ArrayList<SortWithIndex>(length);
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
        setEnabled(getCurrentDataSet() != null);
    }
}

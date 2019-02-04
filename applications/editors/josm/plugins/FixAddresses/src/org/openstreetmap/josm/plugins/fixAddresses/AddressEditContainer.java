// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fixAddresses;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.osm.event.AbstractDatasetChangedEvent;
import org.openstreetmap.josm.data.osm.event.DataChangedEvent;
import org.openstreetmap.josm.data.osm.event.DataSetListener;
import org.openstreetmap.josm.data.osm.event.NodeMovedEvent;
import org.openstreetmap.josm.data.osm.event.PrimitivesAddedEvent;
import org.openstreetmap.josm.data.osm.event.PrimitivesRemovedEvent;
import org.openstreetmap.josm.data.osm.event.RelationMembersChangedEvent;
import org.openstreetmap.josm.data.osm.event.TagsChangedEvent;
import org.openstreetmap.josm.data.osm.event.WayNodesChangedEvent;
import org.openstreetmap.josm.data.osm.visitor.OsmPrimitiveVisitor;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.layer.MainLayerManager;
import org.openstreetmap.josm.plugins.fixAddresses.gui.AddressEditTableModel;
import org.openstreetmap.josm.plugins.fixAddresses.gui.actions.AbstractAddressEditAction;
import org.openstreetmap.josm.tools.CheckParameterUtil;

/**
 * Provides a container serving streets and unresolved/incomplete addresses. It scans through a
 * set of OSM primitives and checks for incomplete addresses (e. g. missing addr:... tags) or
 * addresses with unknown streets ("unresolved addresses").
 *
 * It listens to changes within instances of {@link IOSMEntity} to notify clients on update.
 *
 * {@link AddressEditContainer} is the central class used within actions and UI models to show
 * and alter OSM data.
 *
 * @author Oliver Wieland &lt;oliver.wieland@online.de>
 *
 * @see AbstractAddressEditAction
 * @see AddressEditTableModel
 */
public class AddressEditContainer
implements OsmPrimitiveVisitor, DataSetListener, IAddressEditContainerListener, IProblemVisitor, IAllKnowingTrashHeap {

    private Collection<? extends OsmPrimitive> workingSet;
    /** The street dictionary collecting all streets to a set of unique street names. */
    private HashMap<String, OSMStreet> streetDict = new HashMap<>(100);

    /** The unresolved (addresses without valid street name) addresses list. */
    private List<OSMAddress> unresolvedAddresses = new ArrayList<>(100);

    /** The incomplete addresses list. */
    private List<OSMAddress> incompleteAddresses = new ArrayList<>(100);

    /** The shadow copy to assemble the street dict during update. */
    private HashMap<String, OSMStreet> shadowStreetDict = new HashMap<>(100);
    /** The shadow copy to assemble the unresolved addresses during update. */
    private List<OSMAddress> shadowUnresolvedAddresses = new ArrayList<>(100);
    /** The shadow copy to assemble the incomplete addresses during update. */
    private List<OSMAddress> shadowIncompleteAddresses = new ArrayList<>(100);

    /** The visited nodes cache to increase iteration speed. */
    private HashSet<Node> visitedNodes = new HashSet<>();
    /** The visited ways cache to increase iteration speed. */
    private HashSet<Way> visitedWays = new HashSet<>();
    /** The tag list used within the data area. */
    private HashSet<String> tags = new HashSet<>();
    /** The tag list used within the data area. */
    private HashMap<String, String> values = new HashMap<>();

    /** The list containing the problems */
    private List<IProblem> problems = new ArrayList<>();

    /** The change listeners. */
    private List<IAddressEditContainerListener> listeners = new ArrayList<>();

    /**
     * Creates an empty container.
     */
    public AddressEditContainer() {
        OSMEntityBase.addChangedListener(this);
    }

    /**
     * Gets the working set used by the container. This can by either the complete or just
     * a subset of the current data layer.
     *
     * @return the workingSet
     */
    protected Collection<? extends OsmPrimitive> getWorkingSet() {
        return workingSet;
    }

    /**
     * Adds a change listener.
     * @param listener change listener
     */
    public void addChangedListener(IAddressEditContainerListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes a change listener.
     * @param listener change listener
     */
    public void removeChangedListener(IAddressEditContainerListener listener) {
        listeners.remove(listener);
    }

    /**
     * Notifies clients that the address container changed.
     */
    protected void fireContainerChanged() {
        List<IAddressEditContainerListener> shadowListeners =
            new ArrayList<>(listeners);

        for (IAddressEditContainerListener listener : shadowListeners) {
            listener.containerChanged(this);
        }
    }

    /**
     * Notifies clients that an entity within the address container changed.
     * @param entity OSM entity
     */
    protected void fireEntityChanged(IOSMEntity entity) {
        if (entity == null) throw new RuntimeException("Entity must not be null");

        List<IAddressEditContainerListener> shadowListeners =
            new ArrayList<>(listeners);

        for (IAddressEditContainerListener listener : shadowListeners) {
            listener.entityChanged(entity);
        }
    }

    /**
     * Marks an OSM node as visited.
     *
     * @param n the node to mark.
     */
    private void markNodeAsVisited(Node n) {
        visitedNodes.add(n);
    }

    /**
     * Checks a node for been visited.
     *
     * @param n the n
     * @return true, if node has been visited
     */
    private boolean hasBeenVisited(Node n) {
        return visitedNodes.contains(n);
    }

    /**
     * Marks a way as visited.
     *
     * @param w the way to mark
     */
    private void markWayAsVisited(Way w) {
        visitedWays.add(w);
    }

    /**
     * Checks a way for been visited.
     *
     * @param w the w to check
     * @return true, if way has been visited
     */
    private boolean hasBeenVisited(Way w) {
        return visitedWays.contains(w);
    }

    @Override
    public void visit(Node n) {
        if (hasBeenVisited(n)) {
            return;
        }

        OSMAddress aNode = null;
        // Address nodes are recycled in order to keep instance variables like guessed names
        aNode = OsmFactory.createNode(n);

        if (aNode != null) {
            addAndClassifyAddress(aNode);
            aNode.visit(this, this);
        }
        markNodeAsVisited(n);
    }

    @Override
    public void visit(Way w) {
        // This doesn't matter, we just need the street name
        //if (w.isIncomplete()) return;

        if (hasBeenVisited(w)) {
            return;
        }

        createNodeFromWay(w);
        markWayAsVisited(w);
    }

    /**
     * Adds and classify an address node according to completeness.
     *
     * @param aNode the address node to add and check
     */
    private void addAndClassifyAddress(OSMAddress aNode) {
        if (!assignAddressToStreet(aNode)) {
            // Assignment failed: Street is not known (yet) -> add to 'unresolved' list
            shadowUnresolvedAddresses.add(aNode);
        }

        if (!aNode.isComplete()) {
            shadowIncompleteAddresses.add(aNode);
        }
    }

    /**
     * Creates the node from an OSM way instance.
     *
     * @param w the way to create the entity from
     */
    private void createNodeFromWay(Way w) {
        IOSMEntity ne = OsmFactory.createNodeFromWay(w);

        if (!processNode(ne, w)) {
            // Look also into nodes for addresses (unlikely, but at least they
            // get marked as visited).
            for (Node n : w.getNodes()) {
                visit(n);
            }

            for (String key : w.keySet()) {
                if (!tags.contains(key)) {
                    tags.add(key);
                }

                String v = w.get(key);
                if (!values.containsKey(v)) {
                    values.put(v, key);
                }
            }
        } // else: node has been processed, no need to look deeper
    }

    /**
     * Process an entity node depending on the type. A street segment is added as a child to the
     * corresponding street dictionary while an address is added to the incomplete/unresolved list
     * depending of it's properties.
     *
     * @param ne the entity node.
     * @param w the corresponding OSM way
     * @return true, if node has been processed
     */
    private boolean processNode(IOSMEntity ne, Way w) {
        if (ne != null) {
            // Node is a street (segment)
            if (ne instanceof OSMStreetSegment) {
                OSMStreetSegment newSegment = (OSMStreetSegment) ne;

                if (newSegment != null) {
                    String name = newSegment.getName();
                    if (StringUtils.isNullOrEmpty(name)) return false;

                    OSMStreet sNode = null;
                    if (shadowStreetDict.containsKey(name)) { // street exists?
                        sNode = shadowStreetDict.get(name);
                    } else { // new street name -> add to dict
                        sNode = new OSMStreet(w);
                        shadowStreetDict.put(name, sNode);
                    }

                    if (sNode != null) {
                        // TODO: Check if segment really belongs to the street, even if the
                        // names are the same. Then the streets should be split up...
                        sNode.addStreetSegment(newSegment);
                        return true;
                    } else {
                        throw new RuntimeException("Street node is null!");
                    }
                }
            }

            // Node is an address
            if (ne instanceof OSMAddress) {
                OSMAddress aNode = (OSMAddress) ne;
                addAndClassifyAddress(aNode);
                return true;
            }
        }
        return false;
    }

    @Override
    public void visit(Relation e) {
    }

    /**
     * Gets the dictionary containing the collected streets.
     * @return dictionary containing the collected streets
     */
    public HashMap<String, OSMStreet> getStreetDict() {
        return streetDict;
    }

    /**
     * Gets the unresolved (addresses without valid street name) addresses.
     *
     * @return the unresolved addresses
     */
    public List<OSMAddress> getUnresolvedAddresses() {
        return unresolvedAddresses;
    }

    /**
     * Gets the list with incomplete addresses.
     *
     * @return the incomplete addresses
     */
    public List<OSMAddress> getIncompleteAddresses() {
        return incompleteAddresses;
    }

    /**
     * Gets the street list.
     *
     * @return the street list
     */
    public List<OSMStreet> getStreetList() {
        ArrayList<OSMStreet> sortedList = new ArrayList<>(streetDict.values());
        Collections.sort(sortedList);
        return sortedList;
    }

    /**
     * Gets all addresses without valid street.
     * @return all addresses without valid street
     */
    public List<OSMAddress> getUnresolvedItems() {
        return unresolvedAddresses;
    }

    /**
     * Gets the tags used in the data layer.
     * @return the tags used in the data layer
     */
    public HashSet<String> getTags() {
        return tags;
    }

    /**
     * @return the values
     */
    protected HashMap<String, String> getValues() {
        return values;
    }

    /**
     * Gets the number of streets in the container.
     * @return the number of streets in the container
     */
    public int getNumberOfStreets() {
        return streetDict != null ? streetDict.size() : 0;
    }

    /**
     * Get the number of incomplete addresses.
     * @return the number of incomplete addresses
     */
    public int getNumberOfIncompleteAddresses() {
        return incompleteAddresses != null ? incompleteAddresses.size() : 0;
    }

    /**
     * Gets the number of unresolved addresses.
     * @return the number of unresolved addresses
     */
    public int getNumberOfUnresolvedAddresses() {
        return unresolvedAddresses != null ? unresolvedAddresses.size() : 0;
    }

    /**
     * Gets the number of invalid (unresolved and/or incomplete) addresses.
     *
     * @return the number of invalid addresses
     */
    public int getNumberOfInvalidAddresses() {
        return getNumberOfIncompleteAddresses() + getNumberOfUnresolvedAddresses();
    }

    /**
     * Gets the number of guessed tags.
     * @return the number of guessed tags
     */
    public int getNumberOfGuesses() {
        int sum = 0;

        for (OSMAddress aNode : getAllAddressesToFix()) {
            if (aNode.hasGuesses()) {
                sum++;
            }
        }
        return sum;
    }

    /**
     * Gets all (incomplete and/or unresolved) address nodes to fix.
     * @return all (incomplete and/or unresolved) address nodes to fix
     */
    public List<OSMAddress> getAllAddressesToFix() {
        List<OSMAddress> all = new ArrayList<>(incompleteAddresses);

        for (OSMAddress aNode : unresolvedAddresses) {
            if (!all.contains(aNode)) {
                all.add(aNode);
            }
        }

        return all;
    }

    /**
     * @return the problems
     */
    protected List<IProblem> getProblems() {
        return problems;
    }

    /**
     * Clears the problem list.
     */
    protected void clearProblems() {
        problems.clear();
    }

    /**
     * Tries to assign an address to a street.
     * @param aNode address
     * @return {@code true} if address node has been assigned to a street or if it was already the case
     */
    private boolean assignAddressToStreet(OSMAddress aNode) {
        String streetName = aNode.getStreetName();

        // street name via relation -> implicitly resolved (see TRAC #8336)
        if (aNode.isPartOfRelation()) {
            return true;
        }

        if (streetName != null && shadowStreetDict.containsKey(streetName)) {
            OSMStreet sNode = shadowStreetDict.get(streetName);
            sNode.addAddress(aNode);
            return true;
        }

        return false;
    }

    /**
     * Walks through the list of unassigned addresses and tries to assign them to streets.
     */
    public void resolveAddresses() {
        List<OSMAddress> resolvedAddresses = new ArrayList<>();
        for (OSMAddress node : shadowUnresolvedAddresses) {
            if (assignAddressToStreet(node)) {
                resolvedAddresses.add(node);
            }
        }

        /* Remove all resolves nodes from unresolved list */
        for (OSMAddress resolved : resolvedAddresses) {
            shadowUnresolvedAddresses.remove(resolved);
        }
    }

    /**
     * Rebuilds the street and address lists using the data set given
     * in  {@link AddressEditContainer#attachToDataSet(Collection)} or the
     * full data set of the current data layer {@link MainLayerManager#getEditDataSet()}.
     */
    public void invalidate() {
        if (workingSet != null) {
            invalidate(workingSet);
        } else {
            DataSet ds = MainApplication.getLayerManager().getEditDataSet();
            if (ds != null) {
                invalidate(ds.allPrimitives());
            }
        }
    }

    /**
     * Invalidate using the given data collection.
     *
     * @param osmData the collection containing the osm data to work on.
     */
    public void invalidate(final Collection<? extends OsmPrimitive> osmData) {
        if (osmData == null || osmData.isEmpty())
            return;

        synchronized (this) {
            clearData();
            clearProblems();
            // visit data set for problems...
            for (OsmPrimitive osmPrimitive : osmData) {
                if (osmPrimitive.isUsable()) {
                    osmPrimitive.accept(this);
                }
            }

            // match streets with addresses...
            resolveAddresses();
            // sort problem lists
            Collections.sort(shadowIncompleteAddresses);
            Collections.sort(shadowUnresolvedAddresses);

            // put results from shadow copy into real lists
            incompleteAddresses = new ArrayList<>(shadowIncompleteAddresses);
            unresolvedAddresses = new ArrayList<>(shadowUnresolvedAddresses);
            streetDict = new HashMap<>(shadowStreetDict);
            // remove temp data
            shadowStreetDict.clear();
            shadowUnresolvedAddresses.clear();
            shadowIncompleteAddresses.clear();

            // update clients
            fireContainerChanged();
        }
    }

    /**
     * Clears the shadowed lists data and resets the 'visited' flag for every OSM object.
     */
    private void clearData() {
        shadowStreetDict.clear();
        shadowUnresolvedAddresses.clear();
        shadowIncompleteAddresses.clear();
        visitedNodes.clear();
        visitedWays.clear();
    }

    /**
     * Connects the listener to the given data set and revisits the data. This method should
     * be called immediately after an edit session finished.
     *
     * If the given data set is not null, calls of {@link AddressEditContainer#invalidate()} will
     * only consider the data given within this set. This can be useful to keep out unimportant
     * areas. However, a side-effect could be that some streets are not included and leads to
     * false alarms regarding unresolved addresses.
     *
     * Calling {@link AddressEditContainer#detachFromDataSet()} drops the explicit data set and uses
     * the full data set on subsequent updates.<p>
     *
     * <b>Example</b><br>
     * <code>
     * attachToDataSet(osmDataSetToWorkOn); // osmDataSetToWorkOn = selected items<br>
     * //... launch dialog or whatever<br>
     * detachFromDataSet();
     * </code>
     *
     * @param osmDataToWorkOn the data to examine
     */
    public void attachToDataSet(Collection<? extends OsmPrimitive> osmDataToWorkOn) {
        if (osmDataToWorkOn != null && !osmDataToWorkOn.isEmpty()) {
            workingSet = new ArrayList<>(osmDataToWorkOn);
        } else {
            detachFromDataSet(); // drop old stuff, if present
        }
        invalidate(); // start our investigation...
    }

    /**
     * Disconnects the listener from the data set. This method should
     * be called immediately before an edit session started in order to
     * prevent updates caused by e. g. a selection change within the map view.
     */
    public void detachFromDataSet() {
        //getCurrentDataSet().removeDataSetListener(this);
        if (workingSet != null) {
            workingSet.clear();
            workingSet = null;
        }
    }

    @Override
    public void dataChanged(DataChangedEvent event) {
    }

    @Override
    public void nodeMoved(NodeMovedEvent event) {

    }

    @Override
    public void otherDatasetChange(AbstractDatasetChangedEvent event) {
    }

    @Override
    public void primitivesAdded(PrimitivesAddedEvent event) {
        invalidate();
    }

    @Override
    public void primitivesRemoved(PrimitivesRemovedEvent event) {
        invalidate();
    }

    @Override
    public void relationMembersChanged(RelationMembersChangedEvent event) {
    }

    @Override
    public void tagsChanged(TagsChangedEvent event) {
        invalidate();
    }

    @Override
    public void wayNodesChanged(WayNodesChangedEvent event) {
    }

    @Override
    public void containerChanged(AddressEditContainer container) {
        invalidate();
    }

    @Override
    public void entityChanged(IOSMEntity entity) {
        fireEntityChanged(entity);
    }

    @Override
    public void addProblem(IProblem problem) {
        problems.add(problem);
    }

    @Override
    public void removeProblemsOfSource(IOSMEntity entity) {
        CheckParameterUtil.ensureParameterNotNull(entity, "entity");

        List<IProblem> problemsToRemove = new ArrayList<>();
        for (IProblem problem : problems) {
            if (problem.getSource() == entity) {
                problemsToRemove.add(problem);
            }
        }

        for (IProblem iProblem : problemsToRemove) {
            problems.remove(iProblem);
        }
    }

    @Override
    public String getClosestStreetName(String name) {
        List<String> matches = getClosestStreetNames(name, 1);

        if (matches != null && matches.size() > 0) {
            return matches.get(0);
        }

        return null;
    }

    @Override
    public List<String> getClosestStreetNames(String name, int maxEntries) {
        CheckParameterUtil.ensureParameterNotNull(name, "name");

        // ensure right number of entries
        if (maxEntries < 1) maxEntries = 1;

        List<StreetScore> scores = new ArrayList<>();
        List<String> matches = new ArrayList<>();

        // Find the longest common sub string
        for (String streetName : streetDict.keySet()) {
            int score = StringUtils.lcsLength(name, streetName);

            if (score > 3) { // reasonable value?
                StreetScore sc = new StreetScore(streetName, score);
                scores.add(sc);
            }
        }

        // sort by score
        Collections.sort(scores);

        // populate result list
        int n = Math.min(maxEntries, scores.size());
        for (int i = 0; i < n; i++) {
            matches.add(scores.get(i).getName());
        }

        return matches;
    }

    @Override
    public boolean isValidStreetName(String name) {
        if (streetDict == null) return false;

        return streetDict.containsKey(name);
    }

    /**
     * Internal class to handle results of {@link AddressEditContainer#getClosestStreetNames(String, int)}.
     */
    private static class StreetScore implements Comparable<StreetScore> {
        private String name;
        private int score;

        /**
         * @param name Name of the street.
         * @param score Score of the street (length of longest common substring)
         */
        StreetScore(String name, int score) {
            super();
            this.name = name;
            this.score = score;
        }

        /**
         * @return the name of the street.
         */
        protected String getName() {
            return name;
        }

        /**
         * @return the score of the street.
         */
        @SuppressWarnings("unused")
        // TODO: Implement properly
        protected int getScore() {
            return score;
        }

        @Override
        public int compareTo(StreetScore o) {
            if (o == null) return 1;

            return Integer.valueOf(score).compareTo(Integer.valueOf(o.score));
        }
    }
}

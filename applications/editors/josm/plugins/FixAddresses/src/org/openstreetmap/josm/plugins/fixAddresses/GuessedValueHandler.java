// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fixAddresses;

import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.osm.visitor.OsmPrimitiveVisitor;

/**
 * GuessedValueHandler visits all nodes and ways in order to find a guessed value for a given tag.
 * The guess is determined by finding the closest way/node with the given tag. If no appropriate node
 * is found within maximum distance, no guess is made.
 *
 * The default maximum distance is 100m.
 */
public class GuessedValueHandler implements OsmPrimitiveVisitor {

    /** Default maximum distance (100m) */
    private static final double DEFAULT_MAX_DIST = 100.0;

    private String tag;
    protected double minDist;
    protected String currentValue;
    private OSMAddress aNode;
    private double maxDist = DEFAULT_MAX_DIST;
    protected OsmPrimitive srcNode;

    /**
     * Instantiates a new guessed value handler without node and default maximum distance.
     *
     * @param tag the tag to find the guessed value for.
     */
    public GuessedValueHandler(String tag) {
        this(tag, null, DEFAULT_MAX_DIST);
    }

    /**
     * Instantiates a new guessed value handler without node.
     *
     * @param tag the tag to find the guessed value for.
     * @param maxDist the maximum distance for a node/way to be considered as guessed value.
     */
    public GuessedValueHandler(String tag, double maxDist) {
        this(tag, null, maxDist);
    }

    /**
     * Instantiates a new guessed value handler with default maximum distance.
     *
     * @param tag the tag to find the guessed value for.
     * @param aNode the address node to guess the values for.
     */
    public GuessedValueHandler(String tag, OSMAddress aNode) {
        this(tag, aNode, DEFAULT_MAX_DIST);
    }

    /**
     * Instantiates a new guessed value handler.
     *
     * @param tag the tag to find the guessed value for.
     * @param aNode the address node to guess the values for.
     * @param maxDist the maximum distance for a node/way to be considered as guessed value.
     */
    public GuessedValueHandler(String tag, OSMAddress aNode, double maxDist) {
        super();

        if (StringUtils.isNullOrEmpty(tag)) {
            throw new RuntimeException("Tag must not be empty or null!");
        }

        if (maxDist < 1.0) { // clip value
            maxDist = 1.0;
        }

        this.tag = tag;
        this.maxDist = maxDist;
        setAddressNode(aNode);
    }

    /**
     * Gets the address node to make the guess for.
     *
     * @return the aNode
     */
    protected OSMAddress getAddressNode() {
        return aNode;
    }


    /**
     * Sets the address node to make the guess for.
     * @param aNode address node
     */
    public void setAddressNode(OSMAddress aNode) {
        this.aNode = aNode;
        // reset search results
        minDist = Double.MAX_VALUE;
        srcNode = null;
        currentValue = null;
    }

    /**
     * Gets the max distance allowed to consider a node as guess.
     * If the distance of the node is greater than maxDist, the
     * node/way will be ignored.
     *
     * @return the maxDist
     */
    protected double getMaxDistance() {
        return maxDist;
    }

    /**
     * Gets the tag name to use as guess.
     *
     * @return the tag
     */
    protected String getTag() {
        return tag;
    }

    /**
     * Gets the distance of the node/way which has been used for the guessed value.
     *
     * @return the minDist
     */
    protected double getMinimumDist() {
        return minDist;
    }

    /**
     * Gets the current guessed value or null, if no guess has been possible (so far).
     *
     * @return the currentValue
     */
    public String getCurrentValue() {
        return currentValue;
    }


    /**
     * Gets the node/way which has been selected for the guess.
     * @return The source node or null; if no appropriate source primitive has been found
     */
    public OsmPrimitive getSourceNode() {
        return srcNode;
    }

    /**
     * Check if we need to visit the OSM data
     *
     * @return true, if successful
     */
    public boolean needsGuess() {
        return aNode.needsGuessedValue(tag);
    }

    @Override
    public void visit(Node n) {
        assert aNode != null;

        if (n.hasKey(tag)) {
            double dist = n.getCoor().greatCircleDistance(aNode.getCoor());
            if (dist < minDist && dist < maxDist) {
                minDist = dist;
                currentValue = n.get(tag);
                srcNode = n;
            }
        }
    }

    @Override
    public void visit(Way w) {
        assert aNode != null;

        if (w.hasKey(tag)) {
            double dist = OsmUtils.getMinimumDistanceToWay(aNode.getCoor(), w);
            if (dist < minDist && dist < maxDist) {
                minDist = dist;
                currentValue = w.get(tag);
                srcNode = w;
            }
        }
    }

    @Override
    public void visit(Relation e) {
        // nothing to do (yet)
    }
}

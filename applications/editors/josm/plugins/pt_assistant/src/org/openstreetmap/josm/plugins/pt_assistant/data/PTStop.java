// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.pt_assistant.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.BBox;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.RelationMember;

/**
 * Model a stop with one or two elements (platform and/or stop_position)
 *
 * @author darya
 *
 */
public class PTStop extends RelationMember {

    /* stop_position element of this stop */
    private Node stopPosition = null;

    /* platform element of this stop */
    private OsmPrimitive platform = null;

    private RelationMember stopPositionRM = null;

    private RelationMember platformRM = null;

    /* the name of this stop */
    private String name = "";

    /**
     * Constructor
     *
     * @param other other relation member
     * @throws IllegalArgumentException
     *             if the given relation member does not fit to the data model
     *             used in the plugin
     */
    public PTStop(RelationMember other) {

        super(other);

        if (isPTStopPosition(other)) {
            stopPosition = other.getNode();
            name = stopPosition.get("name");
            setStopPositionRM(new RelationMember("stop", other.getMember()));
        } else if (isPTPlatform(other)) {
            platform = other.getMember();
            name = platform.get("name");
            setPlatformRM(new RelationMember("platform", other.getMember()));
        } else {
            throw new IllegalArgumentException(
                    "The RelationMember type does not match its role " + other.getMember().getName());
        }
    }

    /**
     * Adds the given element to the stop after a check
     *
     * @param member
     *            Element to add
     * @return true if added successfully, false otherwise. A false value
     *         indicates either that the OsmPrimitiveType of the given
     *         RelationMember does not match its role or that this PTStop
     *         already has an attribute with that role.
     */
    public boolean addStopElement(RelationMember member) {

        if (stopPosition == null && isPTStopPosition(member)) {
            this.stopPosition = member.getNode();
            stopPositionRM = new RelationMember("stop", member.getMember());
            return true;
        } else if (platform == null && isPTPlatform(member)) {
            platform = member.getMember();
            platformRM = new RelationMember("platform", member.getMember());
            return true;
        }

        return false;
    }

    /**
     * Returns the stop_position for this PTstop. If the stop_position is not
     * available directly, the method searches for a stop_area relation
     *
     * @return the stop_position for this PTstop
     */
    public Node getStopPosition() {

        return this.stopPosition;
    }

    /**
     * Returns platform (including platform_entry_only and platform_exit_only)
     *
     * @return platform (including platform_entry_only and platform_exit_only)
     */
    public OsmPrimitive getPlatform() {
        return this.platform;
    }

    /**
     * Returns the name of this stop
     * @return the name of this stop
     */
    protected String getName() {
        return this.name;
    }

    /**
     * Sets the stop_position for this stop to the given node
     * @param newStopPosition the stop_position for this stop to the given node
     */
    public void setStopPosition(Node newStopPosition) {

        this.stopPosition = newStopPosition;

    }

    /**
     * Finds potential stop_positions of the platform of this PTStop. It only
     * makes sense to call this method if the stop_position attribute is null.
     * The stop_positions are potential because they may refer to a different
     * route, which this method does not check.
     *
     * @return List of potential stop_positions for this PTStop
     */
    public List<Node> findPotentialStopPositions() {

        ArrayList<Node> potentialStopPositions = new ArrayList<>();

        if (platform == null) {
            return potentialStopPositions;
        }

        // Look for a stop position within 0.002 degrees (around 100 m) of this
        // platform:

        LatLon platformCenter = platform.getBBox().getCenter();
        Double ax = platformCenter.getX() - 0.002;
        Double bx = platformCenter.getX() + 0.002;
        Double ay = platformCenter.getY() - 0.002;
        Double by = platformCenter.getY() + 0.002;
        BBox platformBBox = new BBox(ax, ay, bx, by);

        Collection<Node> allNodes = platform.getDataSet().getNodes();
        String platName = platform.get("name");
        for (Node currentNode : allNodes) {
            String nodeName = currentNode.get("name");
            if (platformBBox.bounds(currentNode.getBBox())
                    && currentNode.hasTag("public_transport", "stop_position")
                    && ((platName == null || nodeName == null)
                        || platName.equals(nodeName))) {
                    potentialStopPositions.add(currentNode);
            }
        }

        return potentialStopPositions;
    }

    /**
     * Checks if this stop equals to other by comparing if they have the same
     * stop_position or a platform
     *
     * @param other
     *            PTStop to be compared
     * @return true if equal, false otherwise
     */
    public boolean equalsStop(PTStop other) {

        if (other == null) {
            return false;
        }

        if (this.stopPosition != null
                && (this.stopPosition == other.getStopPosition() || this.stopPosition == other.getPlatform())) {
            return true;
        }

        return this.platform != null
                && (this.platform == other.getPlatform()
                    || this.platform == other.getStopPosition());
    }

    /**
     * checks whether the given relation member matches a Stop Position or not
     * @param rm member to check
     * @return true if it matches, false otherwise
     */
    public static boolean isPTStopPosition(RelationMember rm) {
        return rm.getMember().hasTag("public_transport", "stop_position")
                && rm.getType().equals(OsmPrimitiveType.NODE);
    }

    /**
     * checks whether the given relation member matches a Platform or not
     * @param rm member to check
     * @return true if it matches, false otherwise
     */
    public static boolean isPTPlatform(RelationMember rm) {
        return rm.getMember().hasTag("highway", "bus_stop")
                || rm.getMember().hasTag("public_transport", "platform")
                || rm.getMember().hasTag("highway", "platform")
                || rm.getMember().hasTag("railway", "platform");
    }

    public RelationMember getPlatformRM() {
        return platformRM;
    }

    public void setPlatformRM(RelationMember platformRM) {
        this.platformRM = platformRM;
    }

    public RelationMember getStopPositionRM() {
        return stopPositionRM;
    }

    public void setStopPositionRM(RelationMember stopPositionRM) {
        this.stopPositionRM = stopPositionRM;
    }
}

// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fixAddresses;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.josm.command.ChangePropertyCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.BBox;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.tools.CheckParameterUtil;

/**
 * The class OSMEntityBase provides a base implementation for the {@link IOSMEntity} interface.
 *
 * The implementation comprises
 * <ol>
 * <li>Handle change listeners
 * <li>Links the corresponding OSM object
 * <li>Tag handling
 * </ol>
 */
public class OSMEntityBase implements IOSMEntity, Comparable<IOSMEntity> {
    public static final String ANONYMOUS = tr("No name");
    private static List<IAddressEditContainerListener> containerListeners = new ArrayList<>();
    private List<ICommandListener> cmdListeners = new ArrayList<>();

    protected OsmPrimitive osmObject;

    public OSMEntityBase(OsmPrimitive osmObject) {
        super();
        this.osmObject = osmObject;
    }

    /**
     * @param osmObject the osmObject to set
     */
    protected void setOsmObject(OsmPrimitive osmObject) {
        CheckParameterUtil.ensureParameterNotNull(osmObject, "osmObject");
        this.osmObject = osmObject;
    }

    /**
     * Adds a change listener.
     * @param listener change listener
     */
    public static void addChangedListener(IAddressEditContainerListener listener) {
        CheckParameterUtil.ensureParameterNotNull(listener, "listener");
        containerListeners.add(listener);
    }

    /**
     * Removes a change listener.
     * @param listener change listener
     */
    public static void removeChangedListener(IAddressEditContainerListener listener) {
        CheckParameterUtil.ensureParameterNotNull(listener, "listener");
        containerListeners.remove(listener);
    }

    /**
     * Notifies clients that the address container changed.
     * @param entity OSM entity
     */
    protected static void fireEntityChanged(IOSMEntity entity) {
        CheckParameterUtil.ensureParameterNotNull(entity, "entity");
        for (IAddressEditContainerListener listener : containerListeners) {
            listener.entityChanged(entity);
        }
    }

    /**
     * Adds a command listener.
     * @param listener command listener
     */
    @Override
    public void addCommandListener(ICommandListener listener) {
        CheckParameterUtil.ensureParameterNotNull(listener, "listener");
        cmdListeners.add(listener);
    }

    /**
     * Removes a command listener.
     * @param listener command listener
     */
    @Override
    public void removeCommandListener(ICommandListener listener) {
        CheckParameterUtil.ensureParameterNotNull(listener, "listener");
        cmdListeners.remove(listener);
    }

    /**
     * Notifies clients that an entity has issued a command.
     *
     * @param command the command to execute.
     */
    protected void fireCommandIssued(Command command) {
        CheckParameterUtil.ensureParameterNotNull(command, "command");
        if (cmdListeners.size() == 0) {
            throw new RuntimeException("Object has no TX context: " + this);
        }

        for (ICommandListener l : cmdListeners) {
            l.commandIssued(this, command);
        }
    }

    @Override
    public OsmPrimitive getOsmObject() {
        return osmObject;
    }

    @Override
    public List<IOSMEntity> getChildren() {
        return null;
    }

    @Override
    /**
     * Gets the name of the street or ANONYMOUS, if street has no name.
     * @return
     */
    public String getName() {
        if (TagUtils.hasNameTag(osmObject)) {
            return TagUtils.getNameValue(osmObject);
        }
        return "";
    }

    @Override
    public boolean hasName() {
        return TagUtils.hasNameTag(osmObject);
    }

    /**
     * Internal helper method which changes the given property and
     * puts the appropriate command {@link org.openstreetmap.josm.command.Command}
     * into the undo/redo queue.
     * @param tag The tag to change.
     * @param newValue The new value for the tag.
     */
    protected void setOSMTag(String tag, String newValue) {
        CheckParameterUtil.ensureParameterNotNull(tag, "tag");


        if (osmObject != null) {
                String existingValue = osmObject.get(tag);
                // Bugfix #9047: Keep existing values
                if (!StringUtils.isNullOrEmpty(existingValue)) {
                    return;
                }

            if ((osmObject.hasKey(tag) && newValue == null) || newValue != null) {
                fireCommandIssued(new ChangePropertyCommand(osmObject, tag, newValue));
                fireEntityChanged(this);
            }
        }
    }

    /**
     * Removes the given tag from the OSM object.
     *
     * @param tag the tag
     */
    protected void removeOSMTag(String tag) {
        CheckParameterUtil.ensureParameterNotNull(tag, "tag");
        setOSMTag(tag, null); // a value of null removes the tag
    }

    @Override
    public String toString() {
        if (hasName()) {
            return this.getClass().getName() + ": " + getName();
        }
        return this.getClass().getName() + ": " + ANONYMOUS;
    }

    @Override
    public int compareTo(IOSMEntity o) {
        if (o == null || !(o instanceof OSMEntityBase)) return -1;
        return this.getName().compareTo(o.getName());
    }

    @Override
    public void visit(IAllKnowingTrashHeap trashHeap, IProblemVisitor visitor) {
        // do nothing
    }

    @Override
    public LatLon getCoor() {
        OsmPrimitive osm = getOsmObject();
        if (osm == null) return null;

        if (osm instanceof Node) {
            return ((Node) osm).getCoor();
        // way: return center
        } else if (osm instanceof Way) {
            Way w = (Way) osm;
            BBox bb = w.getBBox();
            return bb.getBottomRight().getCenter(bb.getTopLeft());
        }
        // relations??
        return null;
    }
}

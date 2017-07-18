// License: GPL. For details, see LICENSE file.

package org.openstreetmap.josm.plugins.pt_assistant.actions;

import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.pt_assistant.utils.RouteUtils;

/**
 * Sorts the stop positions in a PT route according to the assigned ways
 *
 * @author Polyglot
 *
 */
public class CreatePlatformNodeAction extends JosmAction {

    private static final String ACTION_NAME = "Transfer details of stop to platform node";

    /**
     * Creates a new PlatformAction
     */
    public CreatePlatformNodeAction() {
        super(ACTION_NAME, "icons/sortptstops", ACTION_NAME, null, true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Collection<OsmPrimitive> selection = getLayerManager().getEditDataSet().getSelected();
        Node platformNode = null;
        Node stopPostionNode = null;
        Way  platformWay = null;
        Map<String, List<String>> tagsToCompare = new HashMap<>();
        for (OsmPrimitive item: selection) {
            if (item.getType() == OsmPrimitiveType.NODE &&
                    (item.hasTag("amenity", "shelter") || item.hasTag("public_transport", "pole"))) {
                platformNode = (Node) item;
            }
            if (item.getType() == OsmPrimitiveType.NODE &&
                    item.hasTag("public_transport", "stop_position")) {
                stopPostionNode = (Node) item;
                }
            if (item.getType() == OsmPrimitiveType.WAY &&
                    item.hasTag("public_transport", "platform")) {
                platformWay = (Way) item;
            }
        }
        if (platformNode == null && stopPostionNode == null && platformWay == null) {
            return;
        }
    }
    public void populateMap(OsmPrimitive prim, Map<String, List<String>> tagsToCompare) {
        for ( Entry<String, String> tag: prim.getKeys().entrySet()) {
            //tagsToCompare.put(tag.getKey(), tag.getValue());
        }
    }
    @Override
    protected void updateEnabledState(
            Collection<? extends OsmPrimitive> selection) {
        setEnabled(false);
        if (selection == null || selection.size() != 1)
            return;
        OsmPrimitive selected = selection.iterator().next();
        if (selected.getType() == OsmPrimitiveType.RELATION &&
                RouteUtils.isPTRoute((Relation) selected)) {
            setEnabled(true);
        }
    }
}


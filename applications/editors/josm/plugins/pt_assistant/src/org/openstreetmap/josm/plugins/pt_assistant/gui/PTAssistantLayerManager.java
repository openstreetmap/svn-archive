// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.pt_assistant.gui;

import java.util.ArrayList;
import java.util.Collection;

import org.openstreetmap.josm.data.SelectionChangedListener;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.plugins.pt_assistant.PTAssistantPlugin;
import org.openstreetmap.josm.plugins.pt_assistant.utils.RouteUtils;

public class PTAssistantLayerManager implements SelectionChangedListener {

	public final static PTAssistantLayerManager PTLM = new PTAssistantLayerManager();
	private PTAssistantLayer layer;

    public PTAssistantLayer getLayer() {
        if (layer == null) {
            layer = new PTAssistantLayer();
        }
        return layer;
    }

    public void resetLayer() {
    	layer = null;
    }

    /**
     * Listens to a selection change
     */
    @Override
    public void selectionChanged(Collection<? extends OsmPrimitive> newSelection) {

        ArrayList<OsmPrimitive> routes = new ArrayList<>();

        for (OsmPrimitive primitive : newSelection) {
            if (primitive.getType().equals(OsmPrimitiveType.RELATION)
            		&& RouteUtils.isVersionTwoPTRoute((Relation) primitive)) {
            	routes.add(primitive);
            }
        }

        if (!routes.isEmpty()) {
        	getLayer().setPrimitives(routes);
        	PTAssistantPlugin.clearHighlightedRelations();
        	for(OsmPrimitive primitive : routes)
        		PTAssistantPlugin.addHighlightedRelation((Relation) primitive);
        }
    }
}
